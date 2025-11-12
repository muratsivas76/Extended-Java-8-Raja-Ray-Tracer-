#!/usr/bin/perl

#
# Replaces a HorizontalCamera by a StereoCamera in a .raj file
#
# Usage : toStereo.pl .raj files
#


foreach (@ARGV) {
    chomp ;
    toStereo($_) ;
}

sub toStereo {
    my($file) = @_ ;
    $newFile = $file ;
    $newFile =~ s/\.raj$/-stereo\.raj/ ;
    print "Processing file $file\n" ;
    print "Writing file $newFile\n" ;

    open(DATAFILE, "<$file") ;
    open(NEWDATAFILE, ">$newFile") ;

    while(<DATAFILE>) {
        if (s/HorizontalCamera/StereoCamera/g) {
            print NEWDATAFILE ;
            last ;
        }
        else {
            print NEWDATAFILE ;
        }
    }

    while(<DATAFILE>) {
        if (/^([ \t]*)}[ \t]*$/) {
            print NEWDATAFILE "$1        step = 2", "\n", $_ ;
            last ;
        }
        else {
            print NEWDATAFILE ;
        }
    }

    while(<DATAFILE>) {
        print NEWDATAFILE ;
    }

    close(DATAFILE) ;
    close(NEWDATAFILE) ;
}
