#!/bin/bash
BINJAR=target/scala-2.11/degrel.jar
if [[ ! -f $BINJAR ]]; then
    . sbt assembly
fi
java -jar $BINJAR $*
