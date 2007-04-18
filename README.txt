LZMA Streams in Java

Copyright (c)2007 Christopher League <league@contrapunctus.net>

The Lempel-Ziv Markov-chain Algorithm is a very effective and
relatively fast compression technique used in the '7z' format of the
7-Zip archiver.  Implementations of LZMA in C/C++, Java, C#, Python,
and other languages.

I needed a Java implementation of LZMA for a particular project.  In
Java, there is a quasi-standard for FilterOutputStreams -- streams
that transparently compress or encrypt data sent to them (and in
reverse for FilterInputStreams).  Unfortunately, the SevenZip
implementation did not conform to this standard.  It was written in a
completely different (and far more natural!) style.

So, the package net.contrapunctus.lzma provides implementations of
LzmaInputStream and LzmaOutputStream that interact with underlying
LZMA encoders and decoders running in separate threads.  This way I
could get the desired interface without having to restructure the LZMA
implementation, which would undoubtedly introduce bugs.

The Jar file available for download includes the compiled classes for
the (unmodified) LZMA SDK and for my interface, so that may be all you
need.  Try this, for a simple test:

  $ java -cp lzma-4.43-jio-0.7.jar net.contrapunctus.lzma.RoundTrip

It should show some compressed bytes and output a short text message
twice.

There is more work to be done.  Currently, many of the compression
parameters available for configuring the underlying Encoder are
hard-coded in LzmaOutputStream.  This may decrease compatibility
between streams written with and without my interface.  Please don't
hesitate to contact me with any bug reports, questions, or feature
requests.
