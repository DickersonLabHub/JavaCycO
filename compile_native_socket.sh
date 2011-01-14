#!/bin/bash

#To compile the socket listener.
#You might need to adjust where your includes are.  Just look for the jni.h file and use that directory, along with its /linux/ subdirectory.

javac edu/iastate/javacyco/UnixDomainSocket.java
javah -jni -classpath . edu.iastate.javacyco.UnixDomainSocket
gcc -I/usr/lib/jvm/java-6-openjdk/include -I/usr/lib/jvm/java-6-openjdk/include/linux/ -I. edu/iastate/javacyco/edu_iastate_javacyco_UnixDomainSocket.c -o libunixdomainsocket.so -shared -fPIC
