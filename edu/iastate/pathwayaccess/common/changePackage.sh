#!/bin/bash

OLDPKG=$1
NEWPKG=$2

FILES=*.java
for F in $FILES
do
	echo $F
	`sed s/package\ $OLDPKG/package\ $NEWPKG/ $F > tmp`
	mv tmp $F
done
