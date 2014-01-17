#!/bin/bash
BINJAR=target/scala-2.10/degrel.jar
if [[ ! -f $BINJAR ]]; then
    . sbt assembly
fi
java -jar $BINJAR $*
