package SevenZip.streams;

import java.util.concurrent.Semaphore;

class SemaphoreIntArrayQueue implements BlockingIntQueue
{
    private int[] array;
    private int producer_index;
    private int consumer_index;
    private Semaphore mutex;
    private Semaphore space;
    private Semaphore data;
    
    SemaphoreIntArrayQueue( int size )
    {
        array = new int [size];
        producer_index = 0;
        consumer_index = 0;
        mutex = new Semaphore( 1 );
        space = new Semaphore( size );
        data = new Semaphore( 0 );
    }

    SemaphoreIntArrayQueue( )
    {
        this( 4096 );
    }

    public void put( int x ) throws InterruptedException
    {
        space.acquire( );
        mutex.acquire( );
        array[producer_index] = x;
        producer_index = (producer_index+1) % array.length;
        mutex.release( );
        data.release( );
    }

    public int take( ) throws InterruptedException
    {
        data.acquire( );
        mutex.acquire( );
        int x = array[consumer_index];
        consumer_index = (consumer_index+1) % array.length;
        mutex.release( );
        space.release( );
        return x;
    }
}
