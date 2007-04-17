package SevenZip.streams;

interface BlockingIntQueue
{
   void put( int x ) throws InterruptedException;
   int take( ) throws InterruptedException;
}
