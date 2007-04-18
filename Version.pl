#! /usr/bin/perl
# Version.pl -- create a class with detailed version information
# Copyright (c)2007 Christopher League <league@contrapunctus.net>
#
# This is free software, but it comes with ABSOLUTELY NO WARRANTY.
# GNU Lesser General Public License 2.1 or Common Public License 1.0

use strict;

my $MAJOR = shift;
my $MINOR = shift;

print <<EOF;
package net.contrapunctus.lzma;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
public class Version {
public static final int major = $MAJOR;
public static final int minor = $MINOR;
public static final String context = 
EOF

print '"';
while(<>) 
{
    chop;
    s/[\"\\]/\\\0/g;
    print;
    print "\\n";
}
print '";';

print <<EOF;
public static void main( String[] args ) {
  if( args.length > 0 ) System.out.println(context);
  else System.out.printf("lzmajio-%d.%d%n", major, minor);
  }
}
EOF
