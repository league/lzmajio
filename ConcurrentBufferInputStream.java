package SevenZip.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.concurrent.ArrayBlockingQueue;

class ConcurrentBufferInputStream extends InputStream
{
    protected ArrayBlockingQueue<Integer> q;
    protected boolean eof;

    ConcurrentBufferInputStream( ArrayBlockingQueue<Integer> q )
    {
        this.q = q;
        this.eof = false;
    }

    public int read( ) throws IOException
    {
        if( eof ) return -1;
        try {
            int i = q.take( );
            if( i == -1 ) eof = true;
            else i &= 0xFF;
            return i;
        }
        catch( InterruptedException exn ) {
            throw new InterruptedIOException( exn.getMessage() );
        }
    }
}
