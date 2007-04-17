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

    static final int propSize = 5;
    static final byte[] props = new byte[propSize];

    static {
        // enc.SetEndMarkerMode( true );
        // enc.SetDictionarySize( 1 << 20 );
        props[0] = 0x5d;
        props[1] = 0x00;
        props[2] = 0x00;
        props[3] = 0x10;
        props[4] = 0x00;
    }

    public void run( )
    { 
        try {
            // int n = in.read( props, 0, propSize );
            dec.SetDecoderProperties( props );
            dec.Code( in, out, -1 );
            in.close( );
        }
        catch( IOException exn ) {
            this.exn = exn;
        }
    }
}