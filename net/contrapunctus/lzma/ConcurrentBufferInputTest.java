package net.contrapunctus.lzma;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import org.junit.Assert;
import org.junit.Test;
import static net.contrapunctus.lzma.ConcurrentBufferInputStream.create;
import static net.contrapunctus.lzma.ConcurrentBufferOutputStream.newQueue;
import static net.contrapunctus.lzma.ConcurrentBufferOutputTest.*;

public class ConcurrentBufferInputTest
{
    @Test public void withRandomSeed() throws InterruptedException
    {
        long seed = System.currentTimeMillis();
        System.out.println("seed " + seed);
        withSeed(seed);
    }

    abstract class Writer extends Summer
    {
        protected ArrayBlockingQueue<byte[]> q;

        Writer init(ArrayBlockingQueue<byte[]> q)
        {
            this.q = q;
            return this;
        }

        void write(int i) throws InterruptedException
        {
            byte b[] = new byte[1];
            b[0] = (byte) i;
            q.put(b);
            sum.update(i);
        }

        void write(byte[] buf) throws InterruptedException
        {
            q.put(buf);
            sum.update(buf, 0, buf.length);
        }
    }

    class BoundaryWriter extends Writer
    {
        protected void checkedRun() throws InterruptedException
        {
            // write all the byte values (incl overflowed ones) as ints
            for(int i = -255; i <= 255; i++)
                {
                    write(i);
                }
            write(42);          // one more byte
            write(new byte[0]); // sentinel
        }
    }

    class RandomWriter extends Writer
    {
        protected Random rng;

        RandomWriter(Random rng)
        {
            this.rng = rng;
        }

        void write() throws InterruptedException
        {
            byte[] bs = new byte[rng.nextInt(MAX_BUFFER)+1];
            rng.nextBytes(bs);
            switch(rng.nextInt(4))
                {
                case 0:         // single byte
                    write(bs[0]);
                    break;
                default:
                    write(bs);
                }
        }

        protected void checkedRun() throws InterruptedException
        {
            for(int i = rng.nextInt(MAX_ITERS)+5; i >= 0; i--)
                {
                    if(rng.nextBoolean()) yield();
                    write();
                }
            // write sentinel
            q.put(new byte[0]);
        }
    }

    class Reader extends Summer
    {
        protected Random rng;
        protected InputStream is;

        Reader(Random rng, InputStream is)
        {
            this.rng = rng;
            this.is = is;
        }

        boolean read() throws IOException
        {
            byte[] bs = new byte[rng.nextInt(MAX_BUFFER)+1];
            int n;
            switch(rng.nextInt(4))
                {
                case 0:         // single byte
                    int b = is.read();
                    if(b != -1)
                        {
                            sum.update(b);
                            return true;
                        }
                    return false;
                case 1:         // slice of array
                    int off = rng.nextInt(bs.length-1);
                    int len = rng.nextInt(bs.length-off-1)+1;
                    n = is.read(bs, off, len);
                    if(n != -1)
                        {
                            sum.update(bs, off, n);
                            return true;
                        }
                    return false;
                default:        // entire array
                    n = is.read(bs);
                    if(n != -1)
                        {
                            sum.update(bs, 0, n);
                            return true;
                        }
                    return false;
                }
        }

        protected void checkedRun() throws IOException
        {
            while(read())
                {
                }
            is.close();
        }
    }

    private void testReadWrite(Random rng, Writer wr) throws InterruptedException
    {
        ArrayBlockingQueue<byte[]> q = newQueue();
        InputStream is = create(q);
        wr.init(q);
        wr.start();
        Reader rd = new Reader(rng, is);
        rd.run();
        wr.join();
        Assert.assertNull(wr.exn);
        Assert.assertNull(rd.exn);
        System.out.printf("sums %x -> %x\n", wr.getSum(), rd.getSum());
        Assert.assertEquals(wr.getSum(), rd.getSum());
    }

    private void withSeed(long seed) throws InterruptedException
    {
        Random rng1 = new Random(seed);
        Random rng2 = new Random(rng1.nextLong());
        testReadWrite(rng1, new RandomWriter(rng2));
    }

    @Test public void boundaryTest() throws InterruptedException
    {
        long seed = System.currentTimeMillis();
        System.out.println("seed " + seed);
        Random rng = new Random(seed);
        testReadWrite(rng, new BoundaryWriter());
    }
}
