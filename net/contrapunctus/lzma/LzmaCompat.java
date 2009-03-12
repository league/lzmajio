// LzmaCompat.java -- test program for compatibility with lzma(1)
// Copyright (c)2009 Christopher League <league@contrapunctus.net>

// This is free software, but it comes with ABSOLUTELY NO WARRANTY.
// GNU Lesser General Public License 2.1 or Common Public License 1.0

package net.contrapunctus.lzma;

import java.io.*;

public class LzmaCompat
{
    public static void writeFile( String name ) throws IOException
    {
        System.out.printf("writing %s with%s header\n",
                          name,
                          LzmaOutputStream.LZMA_HEADER? "" : " no");
        FileOutputStream fos = new FileOutputStream( name );
        LzmaOutputStream lo = new LzmaOutputStream( fos );
        PrintStream ps = new PrintStream( lo );
        ps.println("Hello, world -- this is a test!");
        ps.close(); // is that enough?
    }

    public static void main( String[] args ) throws IOException
    {
        LzmaOutputStream.LZMA_HEADER = true;
        writeFile("out-true.txt.lzma");
        LzmaOutputStream.LZMA_HEADER = false;
        writeFile("out-false.txt.lzma");

        LzmaOutputStream.LZMA_HEADER = true;
        FileInputStream fis = new FileInputStream( args[0] );
        LzmaInputStream li = new LzmaInputStream( fis );
        InputStreamReader isr = new InputStreamReader( li );
        BufferedReader br = new BufferedReader( isr );
        String s;
        while( null != (s = br.readLine() )) {
            System.out.println(s);
        }
        br.close();
    }
}
