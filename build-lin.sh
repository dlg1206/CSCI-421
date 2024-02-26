#!/bin/bash

OUT_DIR="out"
# Compile source code
find ./src -name "*.java" > sources.txt || { echo "Failed to compile"; rm sources.txt; exit 1; }
javac -d "$OUT_DIR" @sources.txt
rm sources.txt

echo "Compiled files are available at $OUT_DIR"