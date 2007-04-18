package net.contrapunctus.lzma;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;

class ConcurrentBufferOutputStream extends OutputStream
{
    protected BlockingIntQueue q;
    
    private static final PrintStream dbg = System.err;
    private static final boolean DEBUG =
        System.getProperty("DEBUG_ConcurrentBuffer") != null;

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
