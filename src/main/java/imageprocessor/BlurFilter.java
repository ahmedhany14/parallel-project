package imageprocessor;

import java.awt.image.BufferedImage;

public class BlurFilter extends AbstractParallelFilter {
    
    private final int radius;
    
    public BlurFilter() {
        this(3);
    }
    
    public BlurFilter(int radius) {
        super();
        this.radius = radius;
    }
    
    public BlurFilter(int numThreads, boolean useThreadCount) {
        super(numThreads);
        this.radius = 3;
    }
    
    public BlurFilter(int radius, int numThreads) {
        super(numThreads);
        this.radius = radius;
    }
    
    @Override
    protected void processImageChunk(BufferedImage inputImage, BufferedImage outputImage, 
                                    int startY, int endY) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        
        float[] kernel = createGaussianKernel(radius);
        
        for (int y = startY; y < endY; y++) {
            for (int x = 0; x < width; x++) {
                float sumR = 0, sumG = 0, sumB = 0;
                float sumWeight = 0;
                
                for (int ky = -radius; ky <= radius; ky++) {
                    for (int kx = -radius; kx <= radius; kx++) {
                        int px = Math.min(Math.max(x + kx, 0), width - 1);
                        int py = Math.min(Math.max(y + ky, 0), height - 1);
                        
                        int rgb = inputImage.getRGB(px, py);
                        float weight = kernel[kx + radius] * kernel[ky + radius];
                        
                        sumR += weight * ((rgb >> 16) & 0xff);
                        sumG += weight * ((rgb >> 8) & 0xff);
                        sumB += weight * (rgb & 0xff);
                        sumWeight += weight;
                    }
                }
                
                int r = Math.min(Math.max((int) (sumR / sumWeight), 0), 255);
                int g = Math.min(Math.max((int) (sumG / sumWeight), 0), 255);
                int b = Math.min(Math.max((int) (sumB / sumWeight), 0), 255);
                
                int alpha = (inputImage.getRGB(x, y) >> 24) & 0xff;
                int newPixel = (alpha << 24) | (r << 16) | (g << 8) | b;
                
                outputImage.setRGB(x, y, newPixel);
            }
        }
    }
    
    private float[] createGaussianKernel(int radius) {
        float[] kernel = new float[2 * radius + 1];
        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(2.0f * Math.PI * sigma * sigma);
        float total = 0.0f;
        
        for (int i = -radius; i <= radius; i++) {
            float distance = i * i;
            int index = i + radius;
            kernel[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
            total += kernel[index];
        }
        
        for (int i = 0; i < kernel.length; i++) {
            kernel[i] /= total;
        }
        
        return kernel;
    }
}