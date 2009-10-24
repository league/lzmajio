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

    public synchronized void put(byte x)
        throws InterruptedException
    {
        while(count == buf.length) {
            if(DEBUG) dbg.printf("%s wait to put one%n", this);
            wait();
        }
        if(DEBUG) dbg.printf("%s put one -> ", this);
        buf[in] = x;
        in = (in + 1) % buf.length;
        count++;
        if(DEBUG) dbg.println(this);
        notify();
    }

    public synchronized void setEOF()
    {
        eof = true;
        notify();
    }

    public void put(byte[] src) throws InterruptedException
    {
        put(src, 0, src.length);
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
        notify();
        return num_to_copy;
    }

    public synchronized int read() throws InterruptedException
    {
        while(0 == count) {
            if(eof) return -1;
            if(DEBUG) dbg.printf("%s wait to read one%n", this);
            wait();
        }
        if(DEBUG) dbg.printf("%s read one -> ", this);
        int x = buf[out];
        out = (out + 1) % buf.length;
        count--;
        if(DEBUG) dbg.println(this);
        notify();
        return x & 0xff;
    }

    public synchronized int read(byte[] dst, int off, int len)
        throws InterruptedException
    {
        while(0 == count) {
            if(eof) return -1;
            if(DEBUG) dbg.printf("%s wait to read %d%n", this, len);
            wait();
        }
        int num_contiguous = min(count, buf.length - out);
        int num_to_copy = min(num_contiguous, len);
        if(DEBUG) dbg.printf("%s read %d -> ", this, num_to_copy);
        System.arraycopy(buf, out, dst, off, num_to_copy);
        out = (out + num_to_copy) % buf.length;
        count -= num_to_copy;
        if(DEBUG) dbg.println(this);
        notify();
        return num_to_copy;
    }

    private int min(int x, int y)
    {
        if(x < y) return x;
        else return y;
    }
}
