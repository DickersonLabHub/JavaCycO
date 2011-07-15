#!/bin/bash

#You might need to adjust where your PathwayAccess source code is.  Get it from https://subversion.vrac.iastate.edu/Subversion/PathwayAccess/svn/PathwayAccess/trunk/

JAVACYCOSRC=/usr/local/ptools-local/PathwayAccess
STARTDIR=`pwd`

cd $JAVACYCOSRC
javac edu/iastate/javacyco/*.java
jar -cfe NetworkExporter.jar edu.iastate.javacyco.NetworkExporter edu/iastate/javacyco/*.class
mv NetworkExporter.jar $STARTDIR
