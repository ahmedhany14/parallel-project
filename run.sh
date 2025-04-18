#./!/bin/bash

echo "Removing old build..."
rm -rf target

echo "Creating target/classes directory..."
mkdir -p target/classes

echo "Compiling Java files..."
javac -d target/classes src/main/java/imageprocessor/*.java

echo "Running the app..."
java -cp target/classes imageprocessor.ImageProcessorApp
