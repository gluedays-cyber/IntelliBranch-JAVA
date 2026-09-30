#!/usr/bin/env bash
set -e

echo "================================================================================"
echo "  Compiling and Running IntelliBranch-JAVA Enterprise Example Gallery"
echo "================================================================================"

mkdir -p target/classes

find src/main/java -name "*.java" > target/sources.txt
javac -d target/classes -cp target/classes @target/sources.txt

java -cp target/classes com.intellibranch.examples.ExampleGallery "$@"
