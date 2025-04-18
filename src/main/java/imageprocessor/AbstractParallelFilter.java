package imageprocessor;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Abstract base class for parallel image filters
 */
public abstract class AbstractParallelFilter implements ImageFilter {
    
    // Number of threads to use for parallel processing
    protected final int numThreads;
    
    /**
     * Constructor with default number of threads (number of available processors)
     */
    public AbstractParallelFilter() {
        this.numThreads = Runtime.getRuntime().availableProcessors();
    }
    
    /**
     * Constructor with custom number of threads
     * 
     * @param numThreads Number of threads to use
     */
    public AbstractParallelFilter(int numThreads) {
        this.numThreads = numThreads;
    }
    
    @Override
    public BufferedImage applyFilter(BufferedImage inputImage, Consumer<Integer> progressCallback) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        
        // Create a new image for the result
        BufferedImage outputImage = new BufferedImage(width, height, inputImage.getType());
        
        // Create thread pool
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        // Calculate rows per thread
        int rowsPerThread = height / numThreads;
        
        // Submit tasks to thread pool
        for (int i = 0; i < numThreads; i++) {
            final int startY = i * rowsPerThread;
            final int endY = (i == numThreads - 1) ? height : (i + 1) * rowsPerThread;
            
            // Create explicit Runnable task
            Runnable processor = new ImageChunkProcessor(
                inputImage, outputImage, startY, endY, height, progressCallback, this);
            
            // Submit task to executor
            executor.submit(processor);
        }
        
        // Shutdown the executor and wait for all tasks to complete
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return outputImage;
    }
    
    /**
     * Process a chunk of the image (to be implemented by subclasses)
     * 
     * @param inputImage Original image
     * @param outputImage Output image to write to
     * @param startY Starting Y coordinate (row)
     * @param endY Ending Y coordinate (row)
     */
    protected abstract void processImageChunk(BufferedImage inputImage, BufferedImage outputImage, 
                                             int startY, int endY);
}