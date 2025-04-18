package imageprocessor;

import java.awt.image.BufferedImage;

/**
 * Filter that performs edge detection on an image using sequential processing
 */
public class SequentialEdgeDetectionFilter extends AbstractSequentialFilter {
    
    // Sobel operator kernels
    private final int[][] sobelX = {
        {-1, 0, 1},
        {-2, 0, 2},
        {-1, 0, 1}
    };
    
    private final int[][] sobelY = {
        {-1, -2, -1},
        { 0,  0,  0},
        { 1,  2,  1}
    };
    
    private final int kernelSize = 3;
    private final int kernelRadius = kernelSize / 2;
    
    @Override
    protected void processRow(BufferedImage inputImage, BufferedImage outputImage, int y) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        
        // Apply sobel operator to each pixel in the row
        for (int x = 0; x < width; x++) {
            int gx = 0, gy = 0;
            
            // Apply grayscale first for edge detection
            for (int ky = -kernelRadius; ky <= kernelRadius; ky++) {
                for (int kx = -kernelRadius; kx <= kernelRadius; kx++) {
                    int px = Math.min(Math.max(x + kx, 0), width - 1);
                    int py = Math.min(Math.max(y + ky, 0), height - 1);
                    
                    int rgb = inputImage.getRGB(px, py);
                    
                    // Get grayscale value
                    int r = (rgb >> 16) & 0xff;
                    int g = (rgb >> 8) & 0xff;
                    int b = rgb & 0xff;
                    int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                    
                    // Apply sobel weights
                    gx += gray * sobelX[ky + kernelRadius][kx + kernelRadius];
                    gy += gray * sobelY[ky + kernelRadius][kx + kernelRadius];
                }
            }
            
            // Calculate gradient magnitude
            int magnitude = (int) Math.sqrt(gx * gx + gy * gy);
            
            // Normalize to 0-255 range
            magnitude = Math.min(Math.max(magnitude, 0), 255);
            
            // Create the edge detection effect (white edges on black background)
            int alpha = (inputImage.getRGB(x, y) >> 24) & 0xff;
            int newPixel = (alpha << 24) | (magnitude << 16) | (magnitude << 8) | magnitude;
            
            outputImage.setRGB(x, y, newPixel);
        }
    }
}