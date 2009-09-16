package net.contrapunctus.lzma;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.Test;

public class ExceptionTest
{
    private void readLzma(byte[] bs) throws IOException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(bs);
        LzmaInputStream lis = new LzmaInputStream(bais);
        int x = lis.read();
    }

    @Test(timeout = 5000, expected = IOException.class) public void truncatedLzma() throws IOException
    {
        readLzma(new byte[] { 0x5d, 0x00, 0x00 });
    }

    @Test(timeout = 5000, expected = IOException.class) public void truncatedLzma2() throws IOException
    {
        readLzma(DecoderThread.props);
    }
}
