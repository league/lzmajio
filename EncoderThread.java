package SevenZip.streams;

import SevenZip.Compression.LZMA.Encoder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;

class EncoderThread extends Thread
{
    protected BlockingIntQueue q;
    protected InputStream in;
    protected OutputStream out;
    protected Encoder enc;
    protected IOException exn;

    EncoderThread( OutputStream out )
    {
        this.q = new BlockingIntQueue( );
        this.in = new ConcurrentBufferInputStream( q );
        this.out = out;
        this.enc = new Encoder();
        this.exn = null;
    }

    public void run( )
    {
        try {
            enc.SetEndMarkerMode( true );
            enc.SetDictionarySize( 1 << 20 );
            // enc.WriteCoderProperties( out );
            // 5d 00 00 10 00
            enc.Code( in, out, -1, -1, null );
            out.close( );
        }
        catch( IOException exn ) {
            this.exn = exn;
        }
    }
}
