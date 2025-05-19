package gallery.service;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * Manages image processing and manipulation in the gallery.
 *
 * This class handles loading, displaying, filtering, and resizing images.
 * It maintains the original versions of images to allow non-destructive
 * editing and provides methods for various image transformations.
 *
 * Key responsibilities:
 * - Loading images from resources
 * - Creating image thumbnails
 * - Applying image filters (grayscale, sepia, invert)
 * - Resizing images
 * - Maintaining original image data
 */
public class ImageManager {
    // Data structures for state management
    private Map<JLabel, Image> originalImages = new HashMap<>();  // Store original versions of images
    private Map<JLabel, String> appliedFilters = new HashMap<>(); // Track which filter is applied to each image

    // References to UI components
    private JLabel fullImageLabel;      // Label that displays full-size image
    private JComboBox<String> filterComboBox; // Dropdown for selecting filters

    /**
     * Registers UI components for full-screen image display.
     *
     * @param fullImageLabel The label that displays the full-screen image
     * @param filterComboBox The combo box for selecting image filters
     */
    public void registerFullScreenComponents(JLabel fullImageLabel, JComboBox<String> filterComboBox) {
        this.fullImageLabel = fullImageLabel;       // Store reference to the full image display
        this.filterComboBox = filterComboBox;       // Store reference to the filter selector
    }

    /**
     * Creates a JLabel containing an image.
     *
     * Loads the image from the specified resource path, creates a thumbnail,
     * and configures the label for interaction and drag-and-drop. The original
     * image is stored for filter operations.
     *
     * @param resourcePath Path to the image resource
     * @param albumPanel Panel that will contain the image
     * @return A JLabel containing the image thumbnail
     */
    public JLabel createImageLabel(String resourcePath, JPanel albumPanel) {
        // Create a new label for the image
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);  // Center the image in the label

        try {
            // Try to load the image from the resources
            URL imageUrl = getClass().getResource(resourcePath);
            if (imageUrl != null) {
                // If found, read the image
                Image image = ImageIO.read(imageUrl);
                if (image != null) {
                    // Create a thumbnail (scaled to 100x100 pixels)
                    Image scaledImage = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    label.setIcon(new ImageIcon(scaledImage));  // Set as the label's icon

                    // Store the original image for later use (filters, resize)
                    originalImages.put(label, image);
                } else {
                    // If image couldn't be loaded, show error text
                    label.setText("Invalid image");
                }
            } else {
                // If resource doesn't exist, show error text
                label.setText("Not found");
            }
        } catch (Exception e) {
            // If any exception occurs, show error text
            e.printStackTrace();
            label.setText("Error: " + e.getMessage());
        }

        // Store metadata with the label
        label.putClientProperty("albumPanel", albumPanel);     // Remember which album panel this belongs to
        label.putClientProperty("resourcePath", resourcePath); // Remember the path to the image file

        // Add mouse listener to handle clicks (for full-screen view)
        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showFullScreenImage(label);  // Show this image in full-screen when clicked
            }
        });

        // Configure drag and drop support
        new java.awt.dnd.DragSource().createDefaultDragGestureRecognizer(
                label,                        // Component that can be dragged
                java.awt.dnd.DnDConstants.ACTION_COPY,  // Allow copy operations
                dge -> {
                    // Get the image path when drag starts
                    String path = (String) label.getClientProperty("resourcePath");
                    // Start the drag operation with the path as the transferable data
                    dge.startDrag(
                            java.awt.dnd.DragSource.DefaultCopyDrop,  // Show copy cursor
                            new java.awt.datatransfer.StringSelection(path)  // Use path as data
                    );
                }
        );

        return label;  // Return the configured image label
    }

    /**
     * Shows an image in full-screen view.
     *
     * @param label The label containing the image to display
     */
    private void showFullScreenImage(JLabel label) {
        // Make sure we have a place to show the full image
        if (fullImageLabel == null) return;

        // Check if the label has an icon (image)
        if (label.getIcon() instanceof ImageIcon) {
            ImageIcon currentIcon = (ImageIcon) label.getIcon();

            // Create a larger version for full-screen view (300x300 pixels)
            fullImageLabel.setIcon(new ImageIcon(
                    currentIcon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH)
            ));

            // Store a reference to the original label
            fullImageLabel.putClientProperty("originLabel", label);

            // Update the filter dropdown to show current filter
            if (filterComboBox != null) {
                // Get the current filter or "None" if none applied
                String currentFilter = appliedFilters.getOrDefault(label, "None");
                filterComboBox.setSelectedItem(currentFilter);  // Select it in the dropdown
            }

            // Show the full-screen view by switching CardLayout
            Container parent = fullImageLabel.getParent().getParent();
            if (parent instanceof JPanel) {
                CardLayout layout = (CardLayout) ((JPanel) parent).getLayout();
                layout.show((JPanel) parent, "full");  // Show the "full" card
            }
        }
    }

    /**
     * Applies a filter to an image.
     *
     * Processes the image pixel by pixel to apply the selected filter effect.
     * The original image is preserved to allow switching between different filters.
     *
     * @param originLabel The label containing the original image
     * @param fullScreenLabel The label displaying the full-screen image
     * @param filterName Name of the filter to apply ("None", "Grayscale", "Sepia", "Invert")
     */
    public void applyFilter(JLabel originLabel, JLabel fullScreenLabel, String filterName) {
        // Verify we have a valid label with an icon
        if (originLabel == null || !(originLabel.getIcon() instanceof ImageIcon)) return;

        // Ensure we have the original image stored
        if (!originalImages.containsKey(originLabel)) {
            originalImages.put(originLabel, ((ImageIcon) originLabel.getIcon()).getImage());
        }

        // Get the original unfiltered image
        Image original = originalImages.get(originLabel);
        if (original == null) return;

        // Get dimensions
        int width = original.getWidth(null);
        int height = original.getHeight(null);

        // Ensure dimensions are valid
        if (width <= 0 || height <= 0) return;

        // Create a buffer image to apply filters
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(original, 0, 0, null);  // Draw original into buffer
        g2d.dispose();

        // Apply the selected filter
        if (!"None".equals(filterName)) {
            switch (filterName) {
                case "Grayscale":
                    applyGrayscaleFilter(img);
                    break;
                case "Sepia":
                    applySepiaFilter(img);
                    break;
                case "Invert":
                    applyInvertFilter(img);
                    break;
            }
        }

        // Remember which filter was applied
        appliedFilters.put(originLabel, filterName);

        // Create scaled versions for thumbnail and full view
        Image thumbnailImage = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        Image fullScreenImage = img.getScaledInstance(300, 300, Image.SCALE_SMOOTH);

        // Update both displays
        originLabel.setIcon(new ImageIcon(thumbnailImage));
        fullScreenLabel.setIcon(new ImageIcon(fullScreenImage));

        // Refresh the album panel
        JPanel albumPanel = (JPanel) originLabel.getClientProperty("albumPanel");
        if (albumPanel != null) {
            albumPanel.revalidate();
            albumPanel.repaint();
        }
    }

    /**
     * Applies a grayscale filter to an image.
     *
     * Converts each pixel to grayscale by averaging the RGB components.
     *
     * @param img The BufferedImage to transform
     */
    private void applyGrayscaleFilter(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        // Process each pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);  // Get pixel color

                // Extract RGB components
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Convert to grayscale by averaging the components
                int gray = (r + g + b) / 3;

                // Create new RGB value with equal R, G, and B
                int newRGB = (gray << 16) | (gray << 8) | gray;

                // Set the pixel's new color
                img.setRGB(x, y, newRGB);
            }
        }
    }

    /**
     * Applies a sepia filter to an image.
     *
     * Transforms each pixel using a sepia tone formula for a vintage effect.
     *
     * @param img The BufferedImage to transform
     */
    private void applySepiaFilter(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        // Process each pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);  // Get pixel color

                // Extract RGB components
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Apply sepia formula
                int tr = Math.min(255, (int) (0.393 * r + 0.769 * g + 0.189 * b));
                int tg = Math.min(255, (int) (0.349 * r + 0.686 * g + 0.168 * b));
                int tb = Math.min(255, (int) (0.272 * r + 0.534 * g + 0.131 * b));

                // Create new RGB value
                int newRGB = (tr << 16) | (tg << 8) | tb;

                // Set the pixel's new color
                img.setRGB(x, y, newRGB);
            }
        }
    }

    /**
     * Applies an invert filter to an image.
     *
     * Inverts the color values of each pixel (255 - value).
     *
     * @param img The BufferedImage to transform
     */
    private void applyInvertFilter(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        // Process each pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);  // Get pixel color

                // Extract RGB components and invert them (255 - value)
                int r = 255 - ((rgb >> 16) & 0xFF);
                int g = 255 - ((rgb >> 8) & 0xFF);
                int b = 255 - (rgb & 0xFF);

                // Create new RGB value
                int newRGB = (r << 16) | (g << 8) | b;

                // Set the pixel's new color
                img.setRGB(x, y, newRGB);
            }
        }
    }

    /**
     * Resizes an image to the specified dimensions.
     *
     * @param originLabel The label containing the original image
     * @param fullScreenLabel The label displaying the full-screen image
     * @param width The new width for the image
     * @param height The new height for the image
     */
    public void resizeImage(JLabel originLabel, JLabel fullScreenLabel, int width, int height) {
        // Check if we have a valid label with an image
        if (originLabel != null && originLabel.getIcon() instanceof ImageIcon) {
            ImageIcon oldIcon = (ImageIcon) originLabel.getIcon();

            // Create resized version of the image
            Image resizedImage = oldIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(resizedImage);

            // Update both displays
            fullScreenLabel.setIcon(resizedIcon);
            originLabel.setIcon(resizedIcon);

            // Refresh the album panel
            JPanel albumPanel = (JPanel) originLabel.getClientProperty("albumPanel");
            if (albumPanel != null) {
                albumPanel.revalidate();
                albumPanel.repaint();
            }
        }
    }

    /**
     * Gets a list of available image paths from the resources.
     *
     * @return An array of image resource paths
     */
    public String[] getImageResourcePaths() {
        // Define the resource folder to look in
        String folder = "/imageGallery";
        URL dirURL = getClass().getResource(folder);

        // Check if directory exists
        if (dirURL == null) {
            System.out.println("Resource directory not found: " + folder);
            return new String[0];  // Return empty array if not found
        }

        // Get the directory as a File object
        File directory = new File(dirURL.getFile());

        // Get all image files using a filter for common image extensions
        File[] files = directory.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".png") ||
                        name.toLowerCase().endsWith(".jpeg") ||
                        name.toLowerCase().endsWith(".gif"));

        // Check if files were found
        if (files == null) {
            System.out.println("No files found in directory: " + directory.getAbsolutePath());
            return new String[0];  // Return empty array if no files
        }

        // Create a list of resource paths
        List<String> result = new ArrayList<>();
        for (File file : files) {
            // Convert to resource path format
            result.add(folder + "/" + file.getName());
        }

        // Convert list to array and return
        return result.toArray(new String[0]);
    }

    /**
     * Gets the filter data for persistence.
     *
     * @return A map of image paths to their applied filters
     */
    public Map<String, String> getFilterData() {
        Map<String, String> filterData = new HashMap<>();

        // For each image with a filter, store the filter by path
        for (Map.Entry<JLabel, String> entry : appliedFilters.entrySet()) {
            JLabel label = entry.getKey();
            String path = (String) label.getClientProperty("resourcePath");

            // Save the filter if path is available
            if (path != null) {
                filterData.put(path, entry.getValue());
            }
        }

        return filterData;  // Return map of paths to filter names
    }

    /**
     * Sets the filter data from persistent storage.
     *
     * @param filterData A map of image paths to their applied filters
     */
    public void setFilterData(Map<String, String> filterData) {
        // Clear existing filter data
        this.appliedFilters.clear();
        // Filters will be applied when images are loaded
    }
}