#!/bin/sh

IN_FILE=$1
OUT_DIR=$2

while read p; do
    cp $p $OUT_DIR
done < $IN_FILE;