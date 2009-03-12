// LzmaCompat.java -- test program for compatibility with lzma(1)
// Copyright (c)2009 Christopher League <league@contrapunctus.net>

// This is free software, but it comes with ABSOLUTELY NO WARRANTY.
// GNU Lesser General Public License 2.1 or Common Public License 1.0

package net.contrapunctus.lzma;

import java.io.*;

// Expected performance:
//
// % java -cp lzmajio.jar net.contrapunctus.lzma.LzmaCompat file.7za t
// LZMA_HEADER = true
// writing file.7za
// % lzma -dc file.7za
// Hello, world!
//
// % java -cp lzmajio.jar net.contrapunctus.lzma.LzmaCompat file.7za
// LZMA_HEADER = false
// writing file.7za
// % lzma -dc file.7za
// ===ERROR===

public class LzmaCompat
{
    public static void main( String[] args ) throws IOException
    {
        LzmaOutputStream.LZMA_HEADER = args.length > 1;
        System.out.println("LZMA_HEADER = " + LzmaOutputStream.LZMA_HEADER);
        System.out.println("writing " + args[0]);
        FileOutputStream fos = new FileOutputStream(args[0]);
        LzmaOutputStream lo = new LzmaOutputStream( fos );
        PrintStream ps = new PrintStream( lo );
        ps.println("Hello, world!");
        ps.close(); // is that enough?
    }
}
