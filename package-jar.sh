#!/usr/bin/env bash
set -e

echo "============================================================"
echo "  IntelliBranch-JAVA: Production Packaging Script"
echo "============================================================"

if ! command -v jar &> /dev/null; then
    if [ -n "$JAVA_HOME" ] && [ -f "$JAVA_HOME/bin/jar" ]; then
        JAR_CMD="$JAVA_HOME/bin/jar"
    else
        echo "[ERROR] 'jar' command not found in PATH or JAVA_HOME."
        exit 1
    fi
else
    JAR_CMD="jar"
fi

mkdir -p target/classes
echo "[INFO] Compiling classes..."
find src/main/java -name "*.java" > target/sources.txt
javac -d target/classes @target/sources.txt

echo "[INFO] Packaging target/intellibranch-3.0.0.jar..."
"$JAR_CMD" --create --file target/intellibranch-3.0.0.jar --main-class com.intellibranch.Main -C target/classes com

echo "[SUCCESS] Distribution JAR generated: target/intellibranch-3.0.0.jar"
