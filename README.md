## Project Overview

This project demonstrates parallel image processing using Java's multithreading capabilities. The program converts a colored image into grayscale by dividing the workload among multiple threads, utilizing Java's Thread class.

## Features

- Loads an image from a specified file path.
- Uses multiple threads to process different parts of the image simultaneously.
- Converts the image into grayscale using a weighted formula.
- Saves the processed image to a new file.

## How It Works

1. The program loads an image from disk.
2. It determines the number of available processor cores and creates an equivalent number of worker threads.
3. The image is divided into horizontal chunks, with each thread assigned to process a portion.
4. Each thread converts its portion of the image into grayscale.
5. Once all threads finish processing, the final image is saved as output.jpg.

## Prerequisites

- Java Development Kit (JDK) installed.
- An image file (image.jpg) placed in the specified directory.

## Image Processing Details

- The program extracts the red, green, and blue (RGB) components of each pixel.
- A grayscale value is computed using the formula:


    gray = 0.299 * red + 0.587 * green + 0.114 * blue


- The new grayscale value is applied uniformly across all RGB channels.

## Example Output

Before: A colored image (image.jpg) After: A grayscale image (output.jpg)

## Performance Benefits

- Parallel processing significantly speeds up the grayscale conversion by utilizing multiple CPU cores.
- Dividing the image into chunks ensures efficient workload distribution.