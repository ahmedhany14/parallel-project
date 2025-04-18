package imageprocessor;

import java.awt.image.BufferedImage;

/**
 * Filter that applies a Gaussian blur to an image using sequential processing
 */
public class SequentialBlurFilter extends AbstractSequentialFilter {
    
    private final int radius;
    private float[] kernel;
    
    /**
     * Constructor with default radius
     */
    public SequentialBlurFilter() {
        this(3);
    }
    
    /**
     * Constructor with custom radius
     * 
     * @param radius Blur radius
     */
    public SequentialBlurFilter(int radius) {
        this.radius = radius;
        this.kernel = createGaussianKernel(radius);
    }
    
    @Override
    protected void processRow(BufferedImage inputImage, BufferedImage outputImage, int y) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        
        // Apply kernel to each pixel in the row
        for (int x = 0; x < width; x++) {
            float sumR = 0, sumG = 0, sumB = 0;
            float sumWeight = 0;
            
            // Apply convolution with kernel
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
            
            // Normalize and set the new pixel value
            int r = Math.min(Math.max((int) (sumR / sumWeight), 0), 255);
            int g = Math.min(Math.max((int) (sumG / sumWeight), 0), 255);
            int b = Math.min(Math.max((int) (sumB / sumWeight), 0), 255);
            
            int alpha = (inputImage.getRGB(x, y) >> 24) & 0xff;
            int newPixel = (alpha << 24) | (r << 16) | (g << 8) | b;
            
            outputImage.setRGB(x, y, newPixel);
        }
    }
    
    /**
     * Creates a 1D Gaussian kernel with specified radius
     */
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
        
        // Normalize the kernel
        for (int i = 0; i < kernel.length; i++) {
            kernel[i] /= total;
        }
        
        return kernel;
    }
}