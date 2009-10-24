package net.contrapunctus.lzma;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized;
import static net.contrapunctus.lzma.ConcurrentBufferInputStream.create;
import static net.contrapunctus.lzma.ConcurrentBufferOutputStream.newQueue;
import static net.contrapunctus.lzma.ConcurrentBufferOutputTest.*;

@RunWith(Parameterized.class)
public class ConcurrentBufferInputTest
{
    @Parameters public static Collection<Object[]> parameters()
        throws IOException
    {
        Collection<Object[]> args = new ArrayList<Object[]>();
        args.add(new Object[] { System.currentTimeMillis(), true });
        args.add(new Object[] { System.currentTimeMillis(), false });
        args.add(new Object[] { 1251234417455L, false });
        return args;
    }

    public static void main(String[] args) throws InterruptedException
    {
        new ConcurrentBufferInputTest(Long.parseLong(args[0]),
                                      Boolean.parseBoolean(args[1])).run();
    }

    boolean boundary;
    long seed;

    public ConcurrentBufferInputTest(long seed, boolean boundary)
    {
        this.seed = seed;
        this.boundary = boundary;
    }

    public String toString()
    {
        return (boundary? "boundary" : "random") +
            " seed " + seed + 'L';
    }

    @Test(timeout=5000) public void run() throws InterruptedException
    {
        System.out.printf("%s: ", this);
        Random rng = new Random(seed);
        if(boundary)
            {
                testReadWrite(rng, new BoundaryWriter());
            }
        else
            {
                Random rng2 = new Random(rng.nextLong());
                testReadWrite(rng, new RandomWriter(rng2));
            }
    }

    abstract class Writer extends Summer
    {
        protected ConcurrentBuffer q;

        Writer init(ConcurrentBuffer q)
        {
            this.q = q;
            return this;
        }

        void write(int i) throws InterruptedException
        {
            q.put((byte) i);
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
            q.setEOF();
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
            q.setEOF();
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
            byte[] bs = new byte[rng.nextInt(MAX_BUFFER)+2];
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
        ConcurrentBuffer q = newQueue();
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
}
