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

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loadButton = new JButton("Load Image");
        filterComboBox = new JComboBox<>(new String[]{"Grayscale", "Blur", "Sharpen", "Edge Detection", "Sepia"});
        topPanel.add(loadButton);
        topPanel.add(new JLabel("Filter:"));
        topPanel.add(filterComboBox);
        
        JPanel threadPanel = new JPanel();
        threadPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Parallelism Control", 
            TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("Dialog", Font.BOLD, 12), Color.BLUE));
        
        JLabel threadLabel = new JLabel("Number of Threads:");
        int defaultThreads = Runtime.getRuntime().availableProcessors();
        SpinnerNumberModel threadModel = new SpinnerNumberModel(
            defaultThreads, 1, Math.max(32, defaultThreads * 2), 1);
        threadCountSpinner = new JSpinner(threadModel);
        JComponent editor = threadCountSpinner.getEditor();
        JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
        tf.setColumns(3);
        
        JLabel cpuInfoLabel = new JLabel("(System has " + defaultThreads + " processors)");
        cpuInfoLabel.setFont(new Font("Dialog", Font.ITALIC, 10));
        
        compareSequentialCheckbox = new JCheckBox("Compare with Sequential Processing");
        compareSequentialCheckbox.setSelected(true);
        compareSequentialCheckbox.setToolTipText("Compare multi-threaded with single-threaded processing");
        
        processButton = new JButton("Process Image");
        processButton.setEnabled(false);
        processButton.setFont(new Font("Dialog", Font.BOLD, 12));
        
        threadPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        threadPanel.add(threadLabel);
        threadPanel.add(threadCountSpinner);
        threadPanel.add(cpuInfoLabel);
        threadPanel.add(compareSequentialCheckbox);
        threadPanel.add(processButton);
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(topPanel, BorderLayout.NORTH);
        controlPanel.add(threadPanel, BorderLayout.CENTER);
        controlPanel.add(progressBar, BorderLayout.SOUTH);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        
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

        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);

        loadButton.addActionListener(this::loadImage);
        processButton.addActionListener(this::processImage);

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
        
        processButton.setEnabled(false);
        threadCountSpinner.setEnabled(false);
        compareSequentialCheckbox.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setVisible(true);
        
        parallelInfoArea.setText("Processing...");
        
        SwingWorker<ProcessingResult, Integer> worker = new SwingWorker<ProcessingResult, Integer>() {
            @Override
            protected ProcessingResult doInBackground() {
                // Process with multiple threads
                ImageFilter parallelFilter = createParallelFilter(filterName, threadCount);
                
                publish(0);
                long parallelStartTime = System.currentTimeMillis();
                BufferedImage parallelResult = parallelFilter.applyFilter(originalImage, progress -> publish(progress));
                long parallelEndTime = System.currentTimeMillis();
                double parallelTime = (parallelEndTime - parallelStartTime) / 1000.0;
                
                double sequentialTime = 0.0;
                if (compareWithSequential) {
                    // Process with a single thread for "sequential" comparison
                    ImageFilter sequentialFilter = createParallelFilter(filterName, 1);
                    
                    publish(-1);
                    long sequentialStartTime = System.currentTimeMillis();
                    sequentialFilter.applyFilter(originalImage, progress -> publish(-progress - 1));
                    long sequentialEndTime = System.currentTimeMillis();
                    sequentialTime = (sequentialEndTime - sequentialStartTime) / 1000.0;
                }
                
                return new ProcessingResult(parallelResult, parallelTime, sequentialTime, compareWithSequential);
            }
            
            @Override
            protected void process(java.util.List<Integer> chunks) {
                if (!chunks.isEmpty()) {
                    int latestProgress = chunks.get(chunks.size() - 1);
                    if (latestProgress >= 0) {
                        progressBar.setValue(latestProgress);
                        progressBar.setString("Parallel: " + latestProgress + "%");
                    } else if (latestProgress == -1) {
                        progressBar.setValue(0);
                        progressBar.setString("Sequential: 0%");
                    } else {
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
                        
                        info.append("\nSEQUENTIAL PROCESSING (1 THREAD):\n");
                        info.append("- Processing Time: ").append(df.format(result.sequentialTime)).append(" seconds\n");
                        info.append("- Performance: ").append(String.format("%,.0f", sequentialPixelsPerSecond))
                            .append(" pixels/second\n");
                        
                        info.append("\nCOMPARISON:\n");
                        info.append("- Speedup: ").append(df.format(speedup)).append("x\n");
                        info.append("- Parallelism Efficiency: ").append(String.format("%.1f", efficiency)).append("%\n");
                        info.append("  (Ideal efficiency would be 100%)\n");
                        
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
                    
                    String message = "Image processing completed in " + df.format(result.parallelTime) + " seconds!";
                    if (result.compareWithSequential) {
                        message += "\nSingle-threaded processing took " + df.format(result.sequentialTime) + " seconds.";
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
    
    private void displayImage(BufferedImage img) {
        if (img != null) {
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
        
        double widthRatio = (double) maxWidth / width;
        double heightRatio = (double) maxHeight / height;
        double ratio = Math.min(widthRatio, heightRatio);
        
        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);
        
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, original.getType());
        Graphics2D g = scaledImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();
        
        return scaledImage;
    }
    
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
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new ImageProcessorApp());
    }
}