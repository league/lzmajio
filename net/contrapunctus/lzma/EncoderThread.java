package net.contrapunctus.lzma;

import SevenZip.Compression.LZMA.Encoder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.ArrayBlockingQueue;

class EncoderThread extends Thread
{
    protected BlockingIntQueue q;
    protected InputStream in;
    protected OutputStream out;
    protected Encoder enc;
    protected IOException exn;

    private static final PrintStream dbg = System.err;
    private static final boolean DEBUG =
        System.getProperty("DEBUG_LzmaCoders") != null;

    EncoderThread( OutputStream _out )
    {
        q = new BlockingIntQueue( );
        in = new ConcurrentBufferInputStream( q );
        out = _out;
        enc = new Encoder();
        exn = null;
        if(DEBUG) dbg.printf("%s << %s (%s)%n", this, in, q);
    }

    public void run( )
    {
        try {
            enc.SetEndMarkerMode( true );
            enc.SetDictionarySize( 1 << 20 );
            // enc.WriteCoderProperties( out );
            // 5d 00 00 10 00
            if(DEBUG) dbg.printf("%s begins%n", this);
            enc.Code( in, out, -1, -1, null );
            if(DEBUG) dbg.printf("%s ends%n", this);
            out.close( );
        }
        catch( IOException _exn ) {
            exn = _exn;
            if(DEBUG) dbg.printf("%s exception: %s%n", exn.getMessage());
        }
    }

    public String toString( )
    {
        return String.format("Enc@%x", hashCode());
    }
}
