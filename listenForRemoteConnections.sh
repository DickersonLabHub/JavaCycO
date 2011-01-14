#!/bin/bash

PORT=4444

javac edu/iastate/javacyco/*.java

#can also use the following arguments:
#-verbose
#-log
java -Djava.library.path=. -cp . edu.iastate.javacyco.JavacycServer -port $PORT
