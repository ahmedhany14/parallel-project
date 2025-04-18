package imageprocessor;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class ImageChunkProcessor implements Runnable {
    private final BufferedImage inputImage;
    private final BufferedImage outputImage;
    private final int startY;
    private final int endY;
    private final int height;
    private final Consumer<Integer> progressCallback;
    private final AbstractParallelFilter filter;
    
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
    
    @Override
    public void run() {
        filter.processImageChunk(inputImage, outputImage, startY, endY);
        
        synchronized (progressCallback) {
            int progress = (int) (((double) endY / height) * 100);
            progressCallback.accept(progress);
        }
    }
}