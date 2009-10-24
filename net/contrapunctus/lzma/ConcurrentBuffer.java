package net.contrapunctus.lzma;

import java.util.concurrent.ArrayBlockingQueue;

class ConcurrentBuffer extends ArrayBlockingQueue<byte[]>
{
    static final int QUEUESIZE = 4096;

    ConcurrentBuffer()
        {
            super(QUEUESIZE);
        }
}
