// ConcurrentBufferInputStream.java -- read bytes from blocking queue
// Copyright (c)2007 Christopher League <league@contrapunctus.net>

// This is free software, but it comes with ABSOLUTELY NO WARRANTY.
// GNU Lesser General Public License 2.1 or Common Public License 1.0

package net.contrapunctus.lzma;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.PrintStream;

class ConcurrentBufferInputStream extends InputStream
{
    protected ConcurrentBuffer q;
    protected byte[] buf = null;
    protected int next = 0;
    protected boolean eof = false;

    private static final PrintStream dbg = System.err;
    private static final boolean DEBUG;

    static {
        String ds = null;
        try { ds = System.getProperty("DEBUG_ConcurrentBuffer"); }
        catch(SecurityException e) { }
        DEBUG = ds != null;
    }

    private ConcurrentBufferInputStream( ConcurrentBuffer q )
    {
        if(DEBUG) dbg.printf("%s << %s%n", this, q);
        this.q = q;
        this.eof = false;
    }

    static InputStream create( ConcurrentBuffer q )
    {
        InputStream in = new ConcurrentBufferInputStream( q );
        return in;
    }

    public int read( ) throws IOException
    {
        try {
            return q.read();
        }
        catch( InterruptedException exn ) {
            throw new InterruptedIOException( exn.getMessage() );
        }
    }

    public int read( byte[] b, int off, int len ) throws IOException
    {
        try {
            return q.read(b, off, len);
        }
        catch( InterruptedException exn ) {
            throw new InterruptedIOException( exn.getMessage() );
        }
    }

    public String toString( )
    {
        return String.format("cbIn@%x", hashCode());
    }
}
