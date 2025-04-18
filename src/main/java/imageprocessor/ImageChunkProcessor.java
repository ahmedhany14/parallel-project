package imageprocessor;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * Runnable that processes a chunk of an image for parallel filtering
 */
public class ImageChunkProcessor implements Runnable {
    private final BufferedImage inputImage;
    private final BufferedImage outputImage;
    private final int startY;
    private final int endY;
    private final int height;
    private final Consumer<Integer> progressCallback;
    private final AbstractParallelFilter filter;
    
    /**
     * Create a new ImageChunkProcessor
     * 
     * @param inputImage Original image
     * @param outputImage Output image to write to
     * @param startY Starting Y coordinate (row)
     * @param endY Ending Y coordinate (row)
     * @param height Total height of the image
     * @param progressCallback Callback to report progress
     * @param filter The filter to apply to the chunk
     */
    public ImageChunkProcessor(
            BufferedImage inputImage, 
            BufferedImage outputImage, 
            int startY, 
            int endY, 
            int height,
            Consumer<Integer> progressCallback,
            AbstractParallelFilter filter) {
        this.inputImage = inputImage;
        this.outputImage = outputImage;
        this.startY = startY;
        this.endY = endY;
        this.height = height;
        this.progressCallback = progressCallback;
        this.filter = filter;
    }
    
    /**
     * Process the image chunk when the thread is executed
     */
    @Override
    public void run() {
        // Process the image chunk
        filter.processImageChunk(inputImage, outputImage, startY, endY);
        
        // Calculate and report progress
        synchronized (progressCallback) {
            int progress = (int) (((double) endY / height) * 100);
            progressCallback.accept(progress);
        }
    }
}