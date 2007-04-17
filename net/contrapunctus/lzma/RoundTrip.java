package SevenZip.streams;

import java.io.*;

public class RoundTrip
{
    public static void main( String[] args ) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        LzmaOutputStream lo = new LzmaOutputStream( baos );
        PrintStream ps = new PrintStream( lo );
        String k = "Yes yes yes test test test.";
        ps.print( k );
        ps.close( );
        byte[] buf = baos.toByteArray();

        for(int i = 0;  i < buf.length;  i++)
            {
                System.out.printf("%02x ", buf[i]);
            }
        System.out.println();
        // and back again
        ByteArrayInputStream bais = new ByteArrayInputStream( buf );
        LzmaInputStream li = new LzmaInputStream( bais );
        BufferedReader br = new BufferedReader(new InputStreamReader(li));
        String s = br.readLine();
        System.out.println( s );
        System.out.println( k );
        assert s.equals( k );
    }
}