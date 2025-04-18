package imageprocessor;

import java.awt.image.BufferedImage;

public class EdgeDetectionFilter extends AbstractParallelFilter {
    
    public EdgeDetectionFilter() {
        super();
    }
    
    public EdgeDetectionFilter(int numThreads) {
        super(numThreads);
    }
    
    @Override
    protected void processImageChunk(BufferedImage inputImage, BufferedImage outputImage, 
                                    int startY, int endY) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        
        int[][] sobelX = {
            {-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}
        };
        
        int[][] sobelY = {
            {-1, -2, -1},
            { 0,  0,  0},
            { 1,  2,  1}
        };
        
        int kernelSize = 3;
        int kernelRadius = kernelSize / 2;
        
        for (int y = startY; y < endY; y++) {
            for (int x = 0; x < width; x++) {
                int gx = 0, gy = 0;
                
                for (int ky = -kernelRadius; ky <= kernelRadius; ky++) {
                    for (int kx = -kernelRadius; kx <= kernelRadius; kx++) {
                        int px = Math.min(Math.max(x + kx, 0), width - 1);
                        int py = Math.min(Math.max(y + ky, 0), height - 1);
                        
                        int rgb = inputImage.getRGB(px, py);
                        
                        int r = (rgb >> 16) & 0xff;
                        int g = (rgb >> 8) & 0xff;
                        int b = rgb & 0xff;
                        int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                        
                        gx += gray * sobelX[ky + kernelRadius][kx + kernelRadius];
                        gy += gray * sobelY[ky + kernelRadius][kx + kernelRadius];
                    }
                }
                
                int magnitude = (int) Math.sqrt(gx * gx + gy * gy);
                
                magnitude = Math.min(Math.max(magnitude, 0), 255);
                
                int alpha = (inputImage.getRGB(x, y) >> 24) & 0xff;
                int newPixel = (alpha << 24) | (magnitude << 16) | (magnitude << 8) | magnitude;
                
                outputImage.setRGB(x, y, newPixel);
            }
        }
    }
}