package imageprocessor;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * Abstract base class for sequential image filters
 */
public abstract class AbstractSequentialFilter implements SequentialImageFilter {
    
    @Override
    public BufferedImage applyFilter(BufferedImage inputImage, Consumer<Integer> progressCallback) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        
        // Create a new image for the result
        BufferedImage outputImage = new BufferedImage(width, height, inputImage.getType());
        
        // Process the entire image sequentially
        processImageSequentially(inputImage, outputImage, 0, height, progressCallback);
        
        return outputImage;
    }
    
    /**
     * Process the entire image sequentially
     * 
     * @param inputImage Original image
     * @param outputImage Output image to write to
     * @param startY Starting Y coordinate (row)
     * @param endY Ending Y coordinate (row)
     * @param progressCallback Callback to report progress
     */
    protected void processImageSequentially(BufferedImage inputImage, BufferedImage outputImage, 
                                   int startY, int endY, Consumer<Integer> progressCallback) {
        int height = inputImage.getHeight();
        
        // Process the image in small chunks to report progress
        int progressUpdateInterval = Math.max(1, height / 20); // Update progress ~20 times
        
        for (int y = startY; y < endY; y++) {
            // Process this row of the image
            processRow(inputImage, outputImage, y);
            
            // Report progress occasionally
            if (y % progressUpdateInterval == 0 || y == endY - 1) {
                int progress = (int) (((double) y / height) * 100);
                progressCallback.accept(progress);
            }
        }
        
        // Ensure 100% progress is reported
        progressCallback.accept(100);
    }
    
    /**
     * Process a single row of the image (to be implemented by subclasses)
     * 
     * @param inputImage Original image
     * @param outputImage Output image to write to
     * @param y Row to process
     */
    protected abstract void processRow(BufferedImage inputImage, BufferedImage outputImage, int y);
}