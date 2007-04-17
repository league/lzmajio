package SevenZip.streams;

import java.util.concurrent.Semaphore;

class BlockingIntQueue
{
    private int[] array;
    private int producer_index;
    private int consumer_index;
    private Semaphore space;
    private Semaphore data;
    
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

    public void put( int x ) throws InterruptedException
    {
        space.acquire( );
        array[producer_index] = x;
        producer_index = (producer_index+1) % array.length;
        data.release( );
    }

    public int take( ) throws InterruptedException
    {
        data.acquire( );
        int x = array[consumer_index];
        consumer_index = (consumer_index+1) % array.length;
        space.release( );
        return x;
    }
}
