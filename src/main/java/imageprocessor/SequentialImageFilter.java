package imageprocessor;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * Interface for image filters that are applied sequentially
 */
public interface SequentialImageFilter {
    /**
     * Applies the filter to the input image sequentially
     * 
     * @param inputImage The image to apply the filter to
     * @param progressCallback Callback to report progress (0-100)
     * @return The processed image
     */
    BufferedImage applyFilter(BufferedImage inputImage, Consumer<Integer> progressCallback);
}