# Parallel Image Processing Project

## Overview
This project demonstrates parallel image processing using Java multithreading. It converts a color image to grayscale by distributing the workload across multiple threads, with each thread processing a separate portion of the image. The implementation leverages Java's native threading capabilities to achieve significant performance improvements over sequential processing.

## Project Description
The application takes an input image and divides it into horizontal chunks based on the number of available processor cores. Each chunk is then processed independently by a dedicated worker thread. The worker threads simultaneously convert their assigned image portions from color to grayscale using the standard luminance formula (0.299R + 0.587G + 0.114B). Once all threads complete their work, the resulting grayscale image is saved to disk.

## Key Features
- **Dynamic Thread Allocation**: Automatically detects the number of available processor cores and creates an optimal number of worker threads.
- **Efficient Workload Distribution**: Divides the image into equal chunks for balanced processing across all threads.
- **Parallel Processing**: Performs pixel-by-pixel image conversion simultaneously across multiple threads.
- **Minimal Synchronization Overhead**: Design minimizes thread communication and synchronization points.

## Advantages of Parallel Processing
- **Improved Performance**: Significantly faster processing times compared to sequential approaches, especially for large images.
- **Scalability**: Automatically scales with the number of available CPU cores.
- **Resource Utilization**: Maximizes CPU utilization by keeping all cores busy during processing.
- **Real-world Application**: Demonstrates practical application of parallel programming concepts in image processing.

## Implementation Details
The project is implemented using Java's built-in threading capabilities:
- The main thread divides the image into chunks and creates worker threads
- Each worker thread processes its assigned image portion independently
- The BufferedImage object is shared among threads but accessed in a way that prevents race conditions
- Thread synchronization is achieved using join() to ensure all processing completes before saving

## Performance Considerations
- The performance gain is most noticeable for large image files
- The optimal number of threads is typically equal to the number of available processor cores
- For very small images, the overhead of creating and managing threads may outweigh the benefits

## Future Enhancements
- Implement additional image processing filters
- Add benchmarking to measure performance improvement
- Explore alternative parallelization strategies like thread pools or fork/join framework
- Implement a user interface for selecting images and processing options

## Requirements
- Java Development Kit (JDK) 8 or higher
- An input image file (JPG, PNG, etc.)