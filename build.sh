#!/bin/sh
# Build script for Lab 5 - compiles without Maven

set -e

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$BASE_DIR/srs/main/java/app"
RES_DIR="$BASE_DIR/srs/main/resources"
BUILD_DIR="$BASE_DIR/build/classes"
DIST_DIR="$BASE_DIR/dist"
LIB_DIR="$BASE_DIR/lib"

echo "=== Lab 5 Build Script ==="
echo "Base dir: $BASE_DIR"

# Clean and create directories
rm -rf "$BUILD_DIR" "$DIST_DIR"
mkdir -p "$BUILD_DIR" "$DIST_DIR" "$LIB_DIR"

# Copy resources
echo "Copying resources..."
cp "$RES_DIR/log4j2.xml" "$BUILD_DIR/" 2>/dev/null || echo "  (log4j2.xml not found, will work without Log4J)"

# Find all Java files
JAVA_FILES=$(find "$SRC_DIR" -name "*.java" | tr '\n' ' ')
echo "Found Java files: $(echo $JAVA_FILES | wc -w)"

# Compile all sources
echo "Compiling Java sources..."
javac -d "$BUILD_DIR" -encoding UTF-8 --release 17 $JAVA_FILES

echo "Compilation successful!"

# Create manifest for server
echo "Creating server.jar..."
cat > "$BUILD_DIR/MANIFEST-SERVER.MF" << EOF
Manifest-Version: 1.0
Main-Class: server.ServerMain
Class-Path: ./lib/postgresql-42.7.0.jar
Created-By: Lab5 Build Script

EOF

# Create manifest for client
echo "Creating client.jar..."
cat > "$BUILD_DIR/MANIFEST-CLIENT.MF" << EOF
Manifest-Version: 1.0
Main-Class: client.ClientMain
Class-Path: ./lib/postgresql-42.7.0.jar
Created-By: Lab5 Build Script

EOF

# Create JAR files with full classpath
(cd "$BUILD_DIR" && jar cfm "$DIST_DIR/server.jar" MANIFEST-SERVER.MF . && jar cfm "$DIST_DIR/client.jar" MANIFEST-CLIENT.MF .)
echo ""
echo "=== Build Complete ==="
echo "Server JAR: $DIST_DIR/server.jar"
echo "Client JAR: $DIST_DIR/client.jar"
echo ""
echo "To run server: java -cp dist/server.jar:lib/postgresql-42.7.0.jar server.ServerMain <db_login> <db_password> [port]"
echo "To run client: java -cp dist/client.jar:lib/postgresql-42.7.0.jar client.ClientMain localhost 5555"
