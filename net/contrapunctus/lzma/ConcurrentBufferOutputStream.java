// ConcurrentBufferOutputStream.java -- write bytes to blocking queue
// Copyright (c)2007 Christopher League <league@contrapunctus.net>

// This is free software, but it comes with ABSOLUTELY NO WARRANTY.
// GNU Lesser General Public License 2.1 or Common Public License 1.0

package net.contrapunctus.lzma;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;

class ConcurrentBufferOutputStream extends OutputStream
{
    protected ConcurrentBuffer q;
    static final int BUFSIZE = 16384;
    static final int QUEUESIZE = 4096;
    private static final PrintStream dbg = System.err;
    private static final boolean DEBUG;

    static {
        String ds = null;
        try { ds = System.getProperty("DEBUG_ConcurrentBuffer"); }
        catch(SecurityException e) { }
        DEBUG = ds != null;
    }

    private ConcurrentBufferOutputStream( ConcurrentBuffer q )
    {
        if(DEBUG) dbg.printf("%s >> %s%n", this, q);
        this.q = q;
    }

    static OutputStream create( ConcurrentBuffer q )
    {
        OutputStream out = new ConcurrentBufferOutputStream( q );
        out = new BufferedOutputStream( out, BUFSIZE );
        return out;
    }

    static ConcurrentBuffer newQueue( )
    {
        return new ConcurrentBuffer ();
    }

    public void write( int i ) throws IOException
    {
        try {
            q.put((byte) (i & 0xff));
        }
        catch( InterruptedException exn ) {
            throw new InterruptedIOException( exn.getMessage() );
        }
    }

    public void write(byte[] b, int off, int len) throws IOException
    {
        try {
            q.put(b, off, len);
        }
        catch( InterruptedException exn ) {
            throw new InterruptedIOException( exn.getMessage() );
        }
    }

    public void close( ) throws IOException
    {
        if(DEBUG) dbg.printf("%s closed%n", this);
        byte b[] = new byte[0]; // sentinel
        try {
            q.put(b);
        }
        catch( InterruptedException exn ) {
            throw new InterruptedIOException( exn.getMessage() );
        }
    }
}
