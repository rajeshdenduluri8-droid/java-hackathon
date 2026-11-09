#!/bin/sh
echo "Compiling..."
mkdir -p out
javac -encoding UTF-8 -d out src/*.java || { echo "Compile failed. Install JDK 11+ (javac -version)."; exit 1; }
echo "Starting server at http://localhost:8080  (press Ctrl+C to stop)"
java -cp out Main
