package imageprocessor;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import javax.imageio.ImageIO;

public class ImageProcessorApp extends JFrame {
    private BufferedImage originalImage;
    private BufferedImage processedImage;
    private JLabel imageLabel;
    private JComboBox<String> filterComboBox;
    private JButton processButton;
    private JProgressBar progressBar;
    private JSpinner threadCountSpinner;
    private JTextArea parallelInfoArea;
    private final int MAX_IMAGE_WIDTH = 800;
    private final int MAX_IMAGE_HEIGHT = 600;
    private JCheckBox compareSequentialCheckbox;

    public ImageProcessorApp() {
        setTitle("Parallel Image Processor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create top panel with file and filter selection
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loadButton = new JButton("Load Image");
        filterComboBox = new JComboBox<>(new String[]{"Grayscale", "Blur", "Sharpen", "Edge Detection", "Sepia"});
        topPanel.add(loadButton);
        topPanel.add(new JLabel("Filter:"));
        topPanel.add(filterComboBox);
        
        // Create a dedicated panel for thread control with a border
        JPanel threadPanel = new JPanel();
        threadPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Parallelism Control", 
            TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("Dialog", Font.BOLD, 12), Color.BLUE));
        
        // Thread count spinner with better visibility
        JLabel threadLabel = new JLabel("Number of Threads:");
        int defaultThreads = Runtime.getRuntime().availableProcessors();
        SpinnerNumberModel threadModel = new SpinnerNumberModel(
            defaultThreads, 1, Math.max(32, defaultThreads * 2), 1);
        threadCountSpinner = new JSpinner(threadModel);
        JComponent editor = threadCountSpinner.getEditor();
        JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
        tf.setColumns(3);
        
        // Add information about system processors
        JLabel cpuInfoLabel = new JLabel("(System has " + defaultThreads + " processors)");
        cpuInfoLabel.setFont(new Font("Dialog", Font.ITALIC, 10));
        
        // Add checkbox for comparing with sequential processing
        compareSequentialCheckbox = new JCheckBox("Compare with Sequential Processing");
        compareSequentialCheckbox.setSelected(true);
        compareSequentialCheckbox.setToolTipText("Run both parallel and sequential processing and compare performance");
        
        // Process button
        processButton = new JButton("Process Image");
        processButton.setEnabled(false);
        processButton.setFont(new Font("Dialog", Font.BOLD, 12));
        
        // Organize thread panel components
        threadPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        threadPanel.add(threadLabel);
        threadPanel.add(threadCountSpinner);
        threadPanel.add(cpuInfoLabel);
        threadPanel.add(compareSequentialCheckbox);
        threadPanel.add(processButton);
        
        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        
        // Add panels to top section
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(topPanel, BorderLayout.NORTH);
        controlPanel.add(threadPanel, BorderLayout.CENTER);
        controlPanel.add(progressBar, BorderLayout.SOUTH);

        // Image display area
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        
        // Parallelism info panel with better styling
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Processing Performance Information", 
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Dialog", Font.BOLD, 12), Color.BLUE));
        
        parallelInfoArea = new JTextArea(7, 50);
        parallelInfoArea.setEditable(false);
        parallelInfoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        parallelInfoArea.setText("Performance metrics will appear here after processing");
        parallelInfoArea.setBackground(new Color(240, 240, 240));
        parallelInfoArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane infoScrollPane = new JScrollPane(parallelInfoArea);
        infoPanel.add(infoScrollPane, BorderLayout.CENTER);

        // Add components to frame
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);

        // Event listeners
        loadButton.addActionListener(this::loadImage);
        processButton.addActionListener(this::processImage);

        // Set frame properties
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadImage(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "bmp", "gif");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                originalImage = ImageIO.read(selectedFile);
                displayImage(originalImage);
                processButton.setEnabled(true);
                
                // Reset info area
                parallelInfoArea.setText("Ready to process. Image size: " + 
                                        originalImage.getWidth() + "x" + originalImage.getHeight() + " pixels");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, 
                        "Error loading image: " + ex.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void processImage(ActionEvent e) {
        if (originalImage == null) return;
        
        String filterName = (String) filterComboBox.getSelectedItem();
        int threadCount = (Integer) threadCountSpinner.getValue();
        boolean compareWithSequential = compareSequentialCheckbox.isSelected();
        
        // Disable UI during processing
        processButton.setEnabled(false);
        threadCountSpinner.setEnabled(false);
        compareSequentialCheckbox.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setVisible(true);
        
        parallelInfoArea.setText("Processing...");
        
        // Process the image in a background thread to keep UI responsive
        SwingWorker<ProcessingResult, Integer> worker = new SwingWorker<ProcessingResult, Integer>() {
            @Override
            protected ProcessingResult doInBackground() {
                ImageFilter parallelFilter = createParallelFilter(filterName, threadCount);
                
                // Run parallel processing and measure time
                publish(0);
                long parallelStartTime = System.currentTimeMillis();
                BufferedImage parallelResult = parallelFilter.applyFilter(originalImage, progress -> publish(progress));
                long parallelEndTime = System.currentTimeMillis();
                double parallelTime = (parallelEndTime - parallelStartTime) / 1000.0;
                
                // Run sequential processing if requested
                double sequentialTime = 0.0;
                if (compareWithSequential) {
                    publish(0); // Reset progress
                    SequentialImageFilter sequentialFilter = createSequentialFilter(filterName);
                    
                    publish(-1); // Special flag to indicate sequential processing
                    long sequentialStartTime = System.currentTimeMillis();
                    sequentialFilter.applyFilter(originalImage, progress -> publish(-progress - 1)); // Negative to distinguish from parallel
                    long sequentialEndTime = System.currentTimeMillis();
                    sequentialTime = (sequentialEndTime - sequentialStartTime) / 1000.0;
                }
                
                return new ProcessingResult(parallelResult, parallelTime, sequentialTime, compareWithSequential);
            }
            
            @Override
            protected void process(java.util.List<Integer> chunks) {
                // Update progress bar
                if (!chunks.isEmpty()) {
                    int latestProgress = chunks.get(chunks.size() - 1);
                    if (latestProgress >= 0) {
                        progressBar.setValue(latestProgress);
                        progressBar.setString("Parallel: " + latestProgress + "%");
                    } else if (latestProgress == -1) {
                        // Starting sequential processing
                        progressBar.setValue(0);
                        progressBar.setString("Sequential: 0%");
                    } else {
                        // Sequential progress (convert back to positive)
                        int sequentialProgress = -latestProgress - 1;
                        progressBar.setValue(sequentialProgress);
                        progressBar.setString("Sequential: " + sequentialProgress + "%");
                    }
                }
            }
            
            @Override
            protected void done() {
                try {
                    ProcessingResult result = get();
                    processedImage = result.processedImage;
                    displayImage(processedImage);
                    
                    DecimalFormat df = new DecimalFormat("#.###");
                    
                    // Calculate and display performance metrics
                    int imagePixels = originalImage.getWidth() * originalImage.getHeight();
                    double pixelsPerSecond = imagePixels / result.parallelTime;
                    double pixelsPerThread = pixelsPerSecond / threadCount;
                    
                    StringBuilder info = new StringBuilder();
                    info.append("Performance Information:\n");
                    info.append("- Filter: ").append(filterName).append("\n");
                    info.append("- Image Size: ").append(originalImage.getWidth()).append("x")
                        .append(originalImage.getHeight()).append(" (").append(imagePixels).append(" pixels)\n\n");
                    
                    info.append("PARALLEL PROCESSING:\n");
                    info.append("- Thread Count: ").append(threadCount).append("\n");
                    info.append("- Processing Time: ").append(df.format(result.parallelTime)).append(" seconds\n");
                    info.append("- Performance: ").append(String.format("%,.0f", pixelsPerSecond))
                        .append(" pixels/second (").append(String.format("%,.0f", pixelsPerThread))
                        .append(" pixels/second/thread)\n");
                    
                    if (result.compareWithSequential) {
                        double sequentialPixelsPerSecond = imagePixels / result.sequentialTime;
                        double speedup = result.sequentialTime / result.parallelTime;
                        double efficiency = (speedup / threadCount) * 100;
                        
                        info.append("\nSEQUENTIAL PROCESSING:\n");
                        info.append("- Processing Time: ").append(df.format(result.sequentialTime)).append(" seconds\n");
                        info.append("- Performance: ").append(String.format("%,.0f", sequentialPixelsPerSecond))
                            .append(" pixels/second\n");
                        
                        info.append("\nCOMPARISON:\n");
                        info.append("- Speedup: ").append(df.format(speedup)).append("x\n");
                        info.append("- Parallelism Efficiency: ").append(String.format("%.1f", efficiency)).append("%\n");
                        info.append("  (Ideal efficiency would be 100%)\n");
                        
                        // Add interpretation of results
                        if (efficiency > 90) {
                            info.append("- Excellent parallelism: This filter benefits greatly from multi-threading.");
                        } else if (efficiency > 70) {
                            info.append("- Good parallelism: This filter works well with multi-threading.");
                        } else if (efficiency > 50) {
                            info.append("- Moderate parallelism: Some benefit from multi-threading.");
                        } else {
                            info.append("- Limited parallelism: Diminishing returns from adding more threads.");
                        }
                    }
                    
                    parallelInfoArea.setText(info.toString());
                    
                    // Show completion message
                    String message = "Image processing completed in " + df.format(result.parallelTime) + " seconds!";
                    if (result.compareWithSequential) {
                        message += "\nSequential processing took " + df.format(result.sequentialTime) + " seconds.";
                        message += "\nSpeedup: " + df.format(result.sequentialTime / result.parallelTime) + "x";
                    }
                    JOptionPane.showMessageDialog(ImageProcessorApp.this, 
                            message, "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ImageProcessorApp.this, 
                            "Error processing image: " + ex.getMessage(), 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    parallelInfoArea.setText("Error during processing: " + ex.getMessage());
                } finally {
                    processButton.setEnabled(true);
                    threadCountSpinner.setEnabled(true);
                    compareSequentialCheckbox.setEnabled(true);
                    progressBar.setVisible(false);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Creates a parallel filter based on the selected filter type and thread count
     */
    private ImageFilter createParallelFilter(String filterName, int threadCount) {
        switch (filterName) {
            case "Grayscale":
                return new GrayscaleFilter(threadCount);
            case "Blur":
                return new BlurFilter(threadCount, true);
            case "Sharpen":
                return new SharpenFilter(threadCount);
            case "Edge Detection":
                return new EdgeDetectionFilter(threadCount);
            case "Sepia":
                return new SepiaFilter(threadCount);
            default:
                return new GrayscaleFilter(threadCount);
        }
    }
    
    /**
     * Creates a sequential filter based on the selected filter type
     */
    private SequentialImageFilter createSequentialFilter(String filterName) {
        switch (filterName) {
            case "Grayscale":
                return new SequentialGrayscaleFilter();
            case "Blur":
                return new SequentialBlurFilter();
            case "Sharpen":
                return new SequentialSharpenFilter();
            case "Edge Detection":
                return new SequentialEdgeDetectionFilter();
            case "Sepia":
                return new SequentialSepiaFilter();
            default:
                return new SequentialGrayscaleFilter();
        }
    }
    
    private void displayImage(BufferedImage img) {
        if (img != null) {
            // Scale image for display if needed
            BufferedImage displayImg = img;
            if (img.getWidth() > MAX_IMAGE_WIDTH || img.getHeight() > MAX_IMAGE_HEIGHT) {
                displayImg = scaleImage(img, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
            }
            imageLabel.setIcon(new ImageIcon(displayImg));
            pack();
        }
    }
    
    private BufferedImage scaleImage(BufferedImage original, int maxWidth, int maxHeight) {
        int width = original.getWidth();
        int height = original.getHeight();
        
        // Calculate scaling factor
        double widthRatio = (double) maxWidth / width;
        double heightRatio = (double) maxHeight / height;
        double ratio = Math.min(widthRatio, heightRatio);
        
        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);
        
        // Create scaled image
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, original.getType());
        Graphics2D g = scaledImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();
        
        return scaledImage;
    }
    
    /**
     * Class to store the results of image processing
     */
    private static class ProcessingResult {
        final BufferedImage processedImage;
        final double parallelTime;
        final double sequentialTime;
        final boolean compareWithSequential;
        
        ProcessingResult(BufferedImage processedImage, double parallelTime, double sequentialTime, boolean compareWithSequential) {
            this.processedImage = processedImage;
            this.parallelTime = parallelTime;
            this.sequentialTime = sequentialTime;
            this.compareWithSequential = compareWithSequential;
        }
    }

    public static void main(String[] args) {
        // Set the look and feel to the system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Start the application
        SwingUtilities.invokeLater(() -> new ImageProcessorApp());
    }
}