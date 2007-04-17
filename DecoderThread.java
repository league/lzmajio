package SevenZip.streams;

import SevenZip.Compression.LZMA.Decoder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;

class DecoderThread extends Thread
{
    protected ArrayBlockingQueue<Integer> q;
    protected InputStream in;
    protected OutputStream out;
    protected Decoder dec;
    protected IOException exn;

    DecoderThread( InputStream in )
    {
        this.q = new ArrayBlockingQueue<Integer>( 4096 );
        this.in = in;
        this.out = new ConcurrentBufferOutputStream( q );
        this.dec = new Decoder();
        this.exn = null;
    }

    public void run( )
    { 
        try {
            final int propSize = 5;
            byte[] props = new byte[propSize];
            int n = in.read( props, 0, propSize );
            // check n == propSize
            dec.SetDecoderProperties( props );
            dec.Code( in, out, -1 );
            out.close( );
        }
        catch( IOException exn ) {
            this.exn = exn;
        }
    }
}