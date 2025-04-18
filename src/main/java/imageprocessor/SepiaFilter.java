package imageprocessor;

import java.awt.image.BufferedImage;

/**
 * Filter that applies a sepia tone effect to an image using parallel processing
 */
public class SepiaFilter extends AbstractParallelFilter {
    
    /**
     * Constructor with default number of threads
     */
    public SepiaFilter() {
        super();
    }
    
    /**
     * Constructor with custom number of threads
     * 
     * @param numThreads Number of threads to use
     */
    public SepiaFilter(int numThreads) {
        super(numThreads);
    }
    
    @Override
    protected void processImageChunk(BufferedImage inputImage, BufferedImage outputImage, 
                                    int startY, int endY) {
        int width = inputImage.getWidth();
        
        for (int y = startY; y < endY; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = inputImage.getRGB(x, y);
                
                int alpha = (rgb >> 24) & 0xff;
                int red = (rgb >> 16) & 0xff;
                int green = (rgb >> 8) & 0xff;
                int blue = rgb & 0xff;
                
                // Sepia formula
                int newRed = (int) Math.min(255, (red * 0.393 + green * 0.769 + blue * 0.189));
                int newGreen = (int) Math.min(255, (red * 0.349 + green * 0.686 + blue * 0.168));
                int newBlue = (int) Math.min(255, (red * 0.272 + green * 0.534 + blue * 0.131));
                
                int newPixel = (alpha << 24) | (newRed << 16) | (newGreen << 8) | newBlue;
                
                outputImage.setRGB(x, y, newPixel);
            }
        }
    }
}