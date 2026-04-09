#!/bin/bash
# Javadoc Generation Script for Lab 5
# Generates HTML documentation from Java source code comments

set -e

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$BASE_DIR/srs/main/java/app"
JAVADOC_DIR="$BASE_DIR/javadoc"
LIB_DIR="$BASE_DIR/lib"

echo "=== Lab 5 Javadoc Generation ==="
echo "Source directory: $SRC_DIR"
echo "Output directory: $JAVADOC_DIR"

# Clean and create output directory
rm -rf "$JAVADOC_DIR"
mkdir -p "$JAVADOC_DIR"

# Find all Java source files
JAVA_FILES=$(find "$SRC_DIR" -name "*.java" | tr '\n' ' ')

echo "Found $(echo $JAVA_FILES | wc -w) Java files"

# Generate Javadoc
echo "Generating Javadoc..."
javadoc \
    -d "$JAVADOC_DIR" \
    -encoding UTF-8 \
    -docencoding UTF-8 \
    -charset UTF-8 \
    -windowtitle "Lab 5 - Collection Management System" \
    -doctitle "<h1>Lab 5: Collection Management Client-Server Application</h1>" \
    -header "<b>Lab 5 v1.0</b>" \
    -author \
    -version \
    -use \
    $JAVA_FILES \
    2>&1 | head -100

echo ""
echo "=== Javadoc Generation Complete ==="
echo "Open: file://$JAVADOC_DIR/index.html"
