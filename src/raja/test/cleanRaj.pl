#!/usr/bin/perl

#
# Removes unused references in a .raj file
#
# Usage : cleanRaj.pl .raj files
#


foreach (@ARGV) {
    chomp ;
    cleanFile($_) ;
}

sub cleanFile {
    my($file) = @_ ;
    my $usedRef ;
    $newFile = $file ;
    $newFile =~ s/\.raj$/-cleaned\.raj/ ;
    print "Processing file $file\n" ;
    print "Writing file $newFile\n" ;

    open(DATAFILE, "<$file") ;
    while(<DATAFILE>) {
        # Split on whitespaces
        @splitted = split ;
        foreach (@splitted) {
            if (/^@([a-zA-Z0-9_\.]*)/) {
                $thisRef = $1 ;
                $usedRef{$thisRef} = "1" ;
            }
        }
    }
    close(DATAFILE) ;

    open(DATAFILE, "<$file") ;
    open(NEWDATAFILE, ">$newFile") ;
    while(<DATAFILE>) {
        if (/\(@(.*)\) \{/) {
            $thisRef = $1 ;
            if ($usedRef{$thisRef}) {
                print NEWDATAFILE ;
            }
            else {
                s/\(@(.*)\) \{/\{/ ;
                print NEWDATAFILE ;
            }
        }
        else {
            print NEWDATAFILE ;
        }
    }

    close(DATAFILE) ;
    close(NEWDATAFILE) ;
}
