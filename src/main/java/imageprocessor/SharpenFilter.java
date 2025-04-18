package imageprocessor;

import java.awt.image.BufferedImage;

public class SharpenFilter extends AbstractParallelFilter {
    
    public SharpenFilter() {
        super();
    }
    
    public SharpenFilter(int numThreads) {
        super(numThreads);
    }
    
    @Override
    protected void processImageChunk(BufferedImage inputImage, BufferedImage outputImage, 
                                    int startY, int endY) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        
        float[][] kernel = {
            {-1, -1, -1},
            {-1,  9, -1},
            {-1, -1, -1}
        };
        
        int kernelSize = 3;
        int kernelRadius = kernelSize / 2;
        
        for (int y = startY; y < endY; y++) {
            for (int x = 0; x < width; x++) {
                float sumR = 0, sumG = 0, sumB = 0;
                
                for (int ky = -kernelRadius; ky <= kernelRadius; ky++) {
                    for (int kx = -kernelRadius; kx <= kernelRadius; kx++) {
                        int px = Math.min(Math.max(x + kx, 0), width - 1);
                        int py = Math.min(Math.max(y + ky, 0), height - 1);
                        
                        int rgb = inputImage.getRGB(px, py);
                        float weight = kernel[ky + kernelRadius][kx + kernelRadius];
                        
                        sumR += weight * ((rgb >> 16) & 0xff);
                        sumG += weight * ((rgb >> 8) & 0xff);
                        sumB += weight * (rgb & 0xff);
                    }
                }
                
                int r = Math.min(Math.max((int) sumR, 0), 255);
                int g = Math.min(Math.max((int) sumG, 0), 255);
                int b = Math.min(Math.max((int) sumB, 0), 255);
                
                int alpha = (inputImage.getRGB(x, y) >> 24) & 0xff;
                int newPixel = (alpha << 24) | (r << 16) | (g << 8) | b;
                
                outputImage.setRGB(x, y, newPixel);
            }
        }
    }
}
