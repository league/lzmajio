// ConcurrentBufferInputStream.java -- read bytes from blocking queue
// Copyright (c)2007 Christopher League <league@contrapunctus.net>

// This is free software, but it comes with ABSOLUTELY NO WARRANTY.
// GNU Lesser General Public License 2.1 or Common Public License 1.0

package net.contrapunctus.lzma;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.PrintStream;

class ConcurrentBufferInputStream extends InputStream
{
    protected BlockingIntQueue q;
    protected boolean eof;

    private static final PrintStream dbg = System.err;
    private static final boolean DEBUG;

    static {
        String ds = null;
        try { ds = System.getProperty("DEBUG_ConcurrentBuffer"); }
        catch(SecurityException e) { }
        DEBUG = ds != null;
    }

    ConcurrentBufferInputStream( BlockingIntQueue q )
    {
        if(DEBUG) dbg.printf("%s << %s%n", this, q);
        this.q = q;
        this.eof = false;
    }

    public int read( ) throws IOException
    {
        if( eof ) return -1;
        try {
            int i = q.take( );
            if( i == -1 ) {
                if(DEBUG) dbg.printf("%s got EOF%n", this);
                eof = true;   
            }
            else i &= 0xFF;
            return i;
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
