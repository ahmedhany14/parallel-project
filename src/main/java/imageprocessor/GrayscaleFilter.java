package imageprocessor;

import java.awt.image.BufferedImage;

public class GrayscaleFilter extends AbstractParallelFilter {
    
    public GrayscaleFilter() {
        super();
    }
    
    public GrayscaleFilter(int numThreads) {
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
                
                int gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
                
                int newPixel = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
                
                outputImage.setRGB(x, y, newPixel);
            }
        }
    }
}