package SevenZip.streams;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;

import SevenZip.Compression.LZMA.Encoder;
import java.io.ByteArrayInputStream;


public class LzmaOutputStream extends FilterOutputStream
{
    protected EncoderThread eth;

    public LzmaOutputStream( OutputStream out ) 
    {
        super( null );
        this.eth = new EncoderThread( out );
        this.out = new ConcurrentBufferOutputStream( eth.q );
        eth.start( );
    }
    
    public void close( ) throws IOException
    {
        out.close( );
        try {
            eth.join( );
        }
        catch( InterruptedException exn ) {
            throw new InterruptedIOException( exn.getMessage() );
        }
        if( eth.exn != null ) {
            throw eth.exn;
        }
    }

    public static void main( String[] args ) throws IOException
    {
        String s1 = "Hello hello hello, world!";
        String s2 = "This is the best test.";        
        OutputStream os = new OutputStream() {
                public void write(int i)
                {
                    System.out.printf("%02x ", i);
                }
            };
        
        LzmaOutputStream zo = new LzmaOutputStream( os );
        PrintStream ps = new PrintStream( zo );
        ps.print(s1);
        ps.print(s2);        
        ps.close( );
        System.out.println();
        //////////////////
        System.out.println("TRADITIONAL WAY:");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ps = new PrintStream( baos );
        ps.print(s1);
        ps.print(s2);
        ps.close();
        byte[] buf = baos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream( buf );
        baos = new ByteArrayOutputStream();
        Encoder enc = new Encoder();
        enc.SetEndMarkerMode(true);
        enc.WriteCoderProperties( baos );
        enc.Code( bis, baos, -1, -1, null );
        buf = baos.toByteArray();
        for( int i = 0;  i < buf.length;  i++ )
            {
                System.out.printf("%02x ", buf[i]);
            }
        System.out.println();
    }
}
