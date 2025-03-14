import java.awt.image.BufferedImage;

public class Worker extends Thread {
    private final BufferedImage image;
    private final int startRow;
    private final int endRow;

    public Worker(BufferedImage image, int startRow, int endRow) {
        this.image = image;
        this.startRow = startRow;
        this.endRow = endRow;
    }

    @Override
    public void run() {
        for (int y = startRow; y < endRow; y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xff;
                int red   = (rgb >> 16) & 0xff;
                int green = (rgb >> 8) & 0xff;
                int blue  = rgb & 0xff;
                int gray = (int)(0.299 * red + 0.587 * green + 0.114 * blue);
                int newPixel = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
                image.setRGB(x, y, newPixel);
            }
        }
        System.out.println("Thread processing rows " + startRow + " to " + (endRow - 1) + " has finished.");
    }
}
