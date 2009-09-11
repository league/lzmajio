// LzmaCompatTest.java -- test program for compatibility with lzma(1)
// Copyright (c)2009 Christopher League <league@contrapunctus.net>

// This is free software, but it comes with ABSOLUTELY NO WARRANTY.
// GNU Lesser General Public License 2.1 or Common Public License 1.0

package net.contrapunctus.lzma;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class LzmaCompatTest
{
    @Parameters public static Collection<Object[]> parameters()
        throws FileNotFoundException
    {
        File dir = new File("tests/lzma-cmd");
        File[] fs = dir.listFiles();
        if(null == fs)
            {
                throw new FileNotFoundException
                    ("directory tests/lzma-cmd not found");
            }
        Collection<Object[]> args = new ArrayList<Object[]>();
        for(File f : fs)
            {
                args.add(new Object[] { f });
            }
        return args;
    }

    File file;

    public LzmaCompatTest(File file)
    {
        this.file = file;
    }

    public String toString()
    {
        return file.toString();
    }

    @Test public void decode()
        throws IOException
    {
        System.out.printf("%s:", this);
        FileInputStream fis = new FileInputStream(file);
        LzmaInputStream lis = new LzmaInputStream(fis);
        CRC32 sum = new CRC32();
        CheckedInputStream cis = new CheckedInputStream(lis, sum);
        byte[] buf = new byte[4096];
        int k, n = 0;
        while( -1 != (k = cis.read(buf)))
            {
                n += k;
            }
        cis.close();
        long val = sum.getValue();
        System.out.printf("%d bytes, sum %x\n", n, val);
        assertTrue(file.getName().startsWith(Long.toHexString(val)));
    }
}
