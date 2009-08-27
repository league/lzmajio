// RoundTripTest.java -- a simple test program for LZMA in/out streams
// Copyright (c)2007 Christopher League <league@contrapunctus.net>

// This is free software, but it comes with ABSOLUTELY NO WARRANTY.
// GNU Lesser General Public License 2.1 or Common Public License 1.0

package net.contrapunctus.lzma;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class RoundTripTest
{
    @Parameters public static Collection<Object[]> files()
    {
        File dir = new File("tests/roundtrip");
        File[] fs = dir.listFiles();
        Collection<Object[]> args = new ArrayList<Object[]>();
        for(File f : fs)
            {
                args.add(new Object[] { f });
            }
        return args;
    }

    byte[] original;

    public RoundTripTest(File f0) throws IOException
    {
        RandomAccessFile f = new RandomAccessFile(f0, "r");
        long len = f.length();
        assert len < Integer.MAX_VALUE;
        original = new byte[(int)len];
        f.readFully(original);
    }

    @Test public void run() throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        LzmaOutputStream los = new LzmaOutputStream( baos );
        los.write(original);
        los.close();
        byte[] compressed = baos.toByteArray();
        System.out.printf("original %d, compressed %d\n",
                          original.length, compressed.length);
        // and back again
        ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        LzmaInputStream lis = new LzmaInputStream(bais);
        DataInputStream dis = new DataInputStream(lis);
        byte[] expanded = new byte[original.length];
        dis.readFully(expanded);
        Assert.assertTrue(Arrays.equals(original, expanded));
    }

    public static void doit( ) throws IOException
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

    public static void main( String[] args ) throws IOException
    {
        LzmaOutputStream.LZMA_HEADER = true;
        doit();
        LzmaOutputStream.LZMA_HEADER = false;
        doit();
    }
}
