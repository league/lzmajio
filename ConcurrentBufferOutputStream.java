package SevenZip.streams;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;

class ConcurrentBufferOutputStream extends OutputStream
{
    protected ArrayBlockingQueue<Integer> q;

    ConcurrentBufferOutputStream( ArrayBlockingQueue<Integer> q )
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
