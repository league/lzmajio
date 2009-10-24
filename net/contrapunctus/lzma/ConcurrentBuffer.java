package net.contrapunctus.lzma;

import java.io.PrintStream;

class ConcurrentBuffer
{
    static final int BUFSIZE = 16384;
    private byte[] buf;
    private int count;
    private int in;
    private int out;
    private boolean eof;
    private static final boolean DEBUG = false;
    private static final PrintStream dbg = System.err;

    public ConcurrentBuffer()
    {
        buf = new byte[BUFSIZE];
    }

    public synchronized void put(byte[] src)
        throws InterruptedException
    {
        // special case: 0-length array means EOF
        if(0 == src.length) {
            eof = true;
            notify();
        }
        else {
            put(src, 0, src.length);
        }
    }

    public void put(byte[] src, int off, int len)
        throws InterruptedException
    {
        while(len > 0) {
            int k = putSome(src, off, len);
            off += k;
            len -= k;
        }
    }

    public synchronized int putSome(byte[] src, int off, int len)
        throws InterruptedException
    {
        while(count == buf.length) {
            if(DEBUG) dbg.printf("%s wait to put %d%n", this, len);
            wait();
        }
        int num_slots = buf.length - count;
        int num_contiguous = min(num_slots, buf.length - in);
        int num_to_copy = min(num_contiguous, len);
        if(DEBUG) dbg.printf("%s put %d -> ", this, num_to_copy);
        System.arraycopy(src, off, buf, in, num_to_copy);
        in = (in + num_to_copy) % buf.length;
        count += num_to_copy;
        if(DEBUG) dbg.println(this);
        notifyAll();
        return num_to_copy;
    }

    public synchronized byte[] take() throws InterruptedException
    {
        while(0 == count) {
            if(eof) return new byte[0]; // sentinel
            if(DEBUG) dbg.printf("%s wait to take%n", this);
            wait();
        }
        int num_contiguous = min(count, buf.length - out);
        if(DEBUG) dbg.printf("%s take %d -> ", this, num_contiguous);
        byte[] result = new byte[num_contiguous];
        System.arraycopy(buf, out, result, 0, num_contiguous);
        out = (out + num_contiguous) % buf.length;
        count -= num_contiguous;
        if(DEBUG) dbg.println(this);
        notify();
        return result;
    }

    private int min(int x, int y)
    {
        if(x < y) return x;
        else return y;
    }
}
