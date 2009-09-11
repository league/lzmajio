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
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class RoundTripTest
{
    @Parameters public static Collection<Object[]> files()
        throws FileNotFoundException
    {
        File dir = new File("tests/roundtrip");
        File[] fs = dir.listFiles();
        if(null == fs)
            {
                throw new FileNotFoundException
                    ("directory tests/roundtrip not found");
            }
        Collection<Object[]> args = new ArrayList<Object[]>();
        for(File f : fs)
            {
                args.add(new Object[] { f, false });
                args.add(new Object[] { f, true });
            }
        args.add(new Object[] { null, false });
        args.add(new Object[] { null, true });
        return args;
    }

    String name;
    byte[] original;
    boolean header;
    boolean DEBUG;

    public RoundTripTest(File file, boolean header) throws IOException
    {
        this.header = header;
        if(file != null)
            {
                this.name = file.getName();
                RandomAccessFile f = new RandomAccessFile(file, "r");
                long len = f.length();
                assert len < Integer.MAX_VALUE; // huge files will fail, because
                original = new byte[(int)len];  // we read whole thing into mem
                f.readFully(original);
                f.close();
            }
        else
            {
                String s = null;
                try {
                    s = System.getProperty("RoundTripText"); 
                }
                catch(SecurityException e) { }
                if(s == null) s = "Yes yes yes test test test.";
                this.name = "-";
                this.original = s.getBytes();
                this.DEBUG = true;
            }
    }

    public String toString()
    {
        return name + '[' + (header? '+':'-') + ']';
    }

    @Test public void run() throws IOException
    {
        System.out.printf("%s:", this);
        LzmaOutputStream.LZMA_HEADER = header;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        LzmaOutputStream los = new LzmaOutputStream( baos );
        los.write(original);
        los.close();
        byte[] compressed = baos.toByteArray();
        System.out.printf(" original %d, compressed %d\n",
                          original.length, compressed.length);
        if(DEBUG)
            {
                for(int i = 0;  i < compressed.length;  i++)
                    {
                        System.out.printf("%02x ", compressed[i]);
                    }
                System.out.println();
            }
        // and back again
        ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        LzmaInputStream lis = new LzmaInputStream(bais);
        DataInputStream dis = new DataInputStream(lis);
        byte[] expanded = new byte[original.length];
        dis.readFully(expanded);
        Assert.assertTrue(Arrays.equals(original, expanded));
    }

    @Test public void withLzmaCommand()
        throws IOException, InterruptedException
    {
        // header is required for compatibility with lzma(1)
        if(!header) return;
        LzmaOutputStream.LZMA_HEADER = header;
        System.out.printf("%s: ", this);

        // write compressed data to temp file
        File lzfile = File.createTempFile("roundtrip", ".lzma");
        FileOutputStream fos = new FileOutputStream(lzfile);
        LzmaOutputStream los = new LzmaOutputStream(fos);
        los.write(original);
        los.close();

        // ask lzma(1) to decompress it
        System.out.printf("unlzma %s\n", lzfile.getName());
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec("unlzma " + lzfile);
        int r = p.waitFor();
        assertEquals(r, 0);

        // chop off .lzma extension
        String path = lzfile.getPath();
        assertTrue(path.endsWith(".lzma"));
        int k = path.lastIndexOf('.');
        File plain = new File(path.substring(0, k));
        assertTrue(plain.exists());

        // read contents and verify
        RandomAccessFile raf = new RandomAccessFile(plain, "r");
        long len = raf.length();
        assert len < Integer.MAX_VALUE;
        byte[] copy = new byte[(int)len];
        raf.readFully(copy);
        raf.close();
        assertTrue(Arrays.equals(original, copy));

        // clean up
        lzfile.delete();
        plain.delete();
    }

    public static void main( String[] args ) throws IOException
    {
        if(0 == args.length)
            {
                new RoundTripTest(null, false).run();
                new RoundTripTest(null, true).run();
            }
        else
            {
                for(String s : args)
                    {
                        new RoundTripTest(new File(s), false).run();
                        new RoundTripTest(new File(s), true).run();
                    }
            }
    }
}
