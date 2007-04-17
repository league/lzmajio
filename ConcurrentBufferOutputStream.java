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

    public void write( int i ) throws IOException
    {
        try {
            q.put( i );
        }
        catch( InterruptedException exn ) {
            throw new InterruptedIOException( exn.getMessage() );
        }
    }

    public void close( ) throws IOException
    {
        write( -1 );
    }

}
