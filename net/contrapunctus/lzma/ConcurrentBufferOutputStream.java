package SevenZip.streams;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;

class ConcurrentBufferOutputStream extends OutputStream
{
    protected BlockingIntQueue q;

    ConcurrentBufferOutputStream( BlockingIntQueue q )
    {
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
        guarded_put( -1 );
    }

}
