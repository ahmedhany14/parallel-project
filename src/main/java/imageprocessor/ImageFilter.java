package imageprocessor;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public interface ImageFilter {
    BufferedImage applyFilter(BufferedImage inputImage, Consumer<Integer> progressCallback);
}