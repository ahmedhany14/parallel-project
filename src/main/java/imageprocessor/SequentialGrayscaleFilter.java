package imageprocessor;

import java.awt.image.BufferedImage;

/**
 * Filter that converts an image to grayscale using sequential processing
 */
public class SequentialGrayscaleFilter extends AbstractSequentialFilter {
    
    @Override
    protected void processRow(BufferedImage inputImage, BufferedImage outputImage, int y) {
        int width = inputImage.getWidth();
        
        for (int x = 0; x < width; x++) {
            int rgb = inputImage.getRGB(x, y);
            
            int alpha = (rgb >> 24) & 0xff;
            int red = (rgb >> 16) & 0xff;
            int green = (rgb >> 8) & 0xff;
            int blue = rgb & 0xff;
            
            // Convert to grayscale using weighted method (luminance)
            int gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
            
            // Set the same value to all RGB components
            int newPixel = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
            
            outputImage.setRGB(x, y, newPixel);
        }
    }
}