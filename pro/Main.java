import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args) {
        try {
            File inputFile = new File("/home/ahmed_hany/parallel-project/src/image.jpg");
            BufferedImage image = ImageIO.read(inputFile);
            if (image == null) {
                System.err.println("Could not load image. Make sure 'input.jpg' exists and is a valid image file.");
                return;
            }
            System.out.println("Image loaded successfully! Starting processing...");

            int numThreads = Runtime.getRuntime().availableProcessors();
            System.out.println("Using " + numThreads + " threads for parallel processing.");

            int imageHeight = image.getHeight();
            int chunkSize = imageHeight / numThreads;
            Worker[] workers = new Worker[numThreads];

            for (int i = 0; i < numThreads; i++) {
                int startRow = i * chunkSize;
                int endRow = (i == numThreads - 1) ? imageHeight : startRow + chunkSize;
                workers[i] = new Worker(image, startRow, endRow);
                System.out.println("Starting thread " + (i + 1) + " for rows " + startRow + " to " + (endRow - 1));
                workers[i].start();
            }

            for (Worker worker : workers) {
                worker.join();
            }

            File outputFile = new File("output.jpg");
            ImageIO.write(image, "jpg", outputFile);
            System.out.println("Image processing complete! Check out output.jpg for your new grayscale masterpiece.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
