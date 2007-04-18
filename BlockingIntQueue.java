// BlockingIntQueue.java -- a bounded buffer with blocking threads
// Copyright (c)2007 Christopher League <league@contrapunctus.net>

// This is free software, but it comes with ABSOLUTELY NO WARRANTY.
// GNU Lesser General Public License 2.1 or Common Public License 1.0

package net.contrapunctus.lzma;

import java.util.concurrent.Semaphore;
import java.io.PrintStream;

// There is no mutex protecting producer_index and consumer_index,
// because I expect precisely one producer and one consumer!

final class BlockingIntQueue
{
    private int[] array;
    private int producer_index;
    private int consumer_index;
    private Semaphore space;
    private Semaphore data;

    private static final PrintStream dbg = System.err;
    private static final boolean DEBUG = 
        System.getProperty("DEBUG_BlockingIntQueue") != null;
    
    BlockingIntQueue( int size )
    {
        array = new int [size];
        producer_index = 0;
        consumer_index = 0;
        space = new Semaphore( size );
        data = new Semaphore( 0 );
    }

    BlockingIntQueue( )
    {
        this( 4096 );
    }

    void put( int x ) throws InterruptedException
    {
        if(DEBUG) willBlock(space, '<');
        space.acquire( );
        array[producer_index] = x;
        if(DEBUG) dbg.printf("%s < %02x @%d%n", this, x, producer_index);
        producer_index = (producer_index+1) % array.length;
        data.release( );
    }

    int take( ) throws InterruptedException
    {
        if(DEBUG) willBlock(data, '>');
        data.acquire( );
        int x = array[consumer_index];
        if(DEBUG) dbg.printf("%s > %02x @%d%n", this, x, consumer_index);
        consumer_index = (consumer_index+1) % array.length;
        space.release( );
        return x;
    }

    private void willBlock( Semaphore s, char dir )
    {
        if( s.availablePermits() <= 0 )
            {
                dbg.printf("%s %c blocks%n", this, dir);
            }
    }

    public String toString( )
    {
        return String.format("BQ@%x", hashCode());
    }
}
