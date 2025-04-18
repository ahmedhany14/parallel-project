package imageprocessor;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class AbstractParallelFilter implements ImageFilter {
    
    protected final int numThreads;
    
    public AbstractParallelFilter() {
        this.numThreads = Runtime.getRuntime().availableProcessors();
    }
    
    public AbstractParallelFilter(int numThreads) {
        this.numThreads = numThreads;
    }
    
    @Override
    public BufferedImage applyFilter(BufferedImage inputImage, Consumer<Integer> progressCallback) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        
        BufferedImage outputImage = new BufferedImage(width, height, inputImage.getType());
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        int rowsPerThread = height / numThreads;
        
        for (int i = 0; i < numThreads; i++) {
            final int startY = i * rowsPerThread;
            final int endY = (i == numThreads - 1) ? height : (i + 1) * rowsPerThread;
            
            Runnable processor = new ImageChunkProcessor(
                inputImage, outputImage, startY, endY, height, progressCallback, this);
            
            executor.submit(processor);
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return outputImage;
    }
    
    protected abstract void processImageChunk(BufferedImage inputImage, BufferedImage outputImage, 
                                             int startY, int endY);
}