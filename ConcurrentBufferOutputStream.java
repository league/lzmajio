// ConcurrentBufferOutputStream.java -- write bytes to blocking queue
// Copyright (c)2007 Christopher League <league@contrapunctus.net>

// This is free software, but it comes with ABSOLUTELY NO WARRANTY.
// GNU Lesser General Public License 2.1 or Common Public License 1.0

package net.contrapunctus.lzma;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;

class ConcurrentBufferOutputStream extends OutputStream
{
    protected BlockingIntQueue q;
    
    private static final PrintStream dbg = System.err;
    private static final boolean DEBUG;

    static {
        String ds = null;
        try { ds = System.getProperty("DEBUG_ConcurrentBuffer"); }
        catch(SecurityException e) { }
        DEBUG = ds != null;
    }

    ConcurrentBufferOutputStream( BlockingIntQueue q )
    {
        if(DEBUG) dbg.printf("%s >> %s%n", this, q);
        this.q = q;
    }

    protected void guarded_put( int i ) throws IOException
    {
        try {
            q.put( i );
        }
        catch( InterruptedException exn ) {
            throw new InterruptedIOException( exn.getMessage() );
        }
    }

    public void write( int i ) throws IOException
    {
        guarded_put( i & 0xff );
    }

    public void close( ) throws IOException
    {
        if(DEBUG) dbg.printf("%s closed%n", this);
        guarded_put( -1 );
    }

    public String toString( )
    {
        return String.format("cbOut@%x", hashCode());
    }
}
