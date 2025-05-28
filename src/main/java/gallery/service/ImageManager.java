package gallery.service;

import gallery.model.GalleryData;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    // Add StorageManager for persistence
    private StorageManager storageManager;

    // Data structures for state management
    private Map<JLabel, Image> originalImages = new HashMap<>();  // Store original versions of images
    private Map<JLabel, String> appliedFilters = new HashMap<>(); // Track which filter is applied to each image

    // References to UI components
    private JLabel fullImageLabel;      // Label that displays full-size image
    private JComboBox<String> filterComboBox; // Dropdown for selecting filters

    /**
     * Default constructor.
     * StorageManager must be set using setStorageManager() before applying filters.
     */
    public ImageManager() {
        // Default constructor
    }

    /**
     * Constructor with StorageManager for persistence
     *
     * @param storageManager The storage manager to use for persistence
     */
    public ImageManager(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    /**
     * Sets the storage manager for persistence operations.
     *
     * @param storageManager The storage manager to use
     */
    public void setStorageManager(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

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
     * Creates a JLabel containing an image from either resources or external path.
     * Handles both embedded resource paths and absolute file paths for external images.
     *
     * @param resourcePath Path to the image resource or absolute file path
     * @param albumPanel Panel that will contain the image
     * @return A JLabel containing the image thumbnail
     */
    public JLabel createImageLabel(String resourcePath, JPanel albumPanel) {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);

        try {
            Image image = null;

            // Check if it's an absolute path (external image)
            if (new File(resourcePath).isAbsolute()) {
                image = ImageIO.read(new File(resourcePath));
            } else {
                // Normal resource path
                URL imageUrl = getClass().getResource(resourcePath);
                if (imageUrl != null) {
                    image = ImageIO.read(imageUrl);
                }
            }

            if (image != null) {
                Image scaledImage = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaledImage));
                originalImages.put(label, image);
            } else {
                label.setText("Invalid image");
            }
        } catch (Exception e) {
            e.printStackTrace();
            label.setText("Error: " + e.getMessage());
        }

        label.putClientProperty("albumPanel", albumPanel);
        label.putClientProperty("resourcePath", resourcePath);

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
        if ("None".equals(filterName)) {
            appliedFilters.remove(originLabel);
        } else {
            appliedFilters.put(originLabel, filterName);
        }

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

        // Update storage with filter change
        updateStorageWithFilterChange(originLabel, filterName);
    }

    /**
     * Updates the storage with filter changes
     */
    private void updateStorageWithFilterChange(JLabel originLabel, String filterName) {
        // Check if StorageManager is available
        if (storageManager == null) return;

        // Get necessary information
        String path = (String) originLabel.getClientProperty("resourcePath");
        String albumName = getAlbumForImage(originLabel);

        if (path == null) return;

        // Load current data
        GalleryData data = storageManager.loadGalleryData();

        // Update the data
        if ("None".equals(filterName)) {
            // Remove the filter
            if (albumName != null && !albumName.equals("default")) {
                data.removeFilterFromAlbum(albumName, path);
            } else {
                data.removeFilter(path);
            }
        } else {
            // Add or update the filter
            if (albumName != null && !albumName.equals("default")) {
                data.addFilterToAlbum(albumName, path, filterName);
            } else {
                data.addFilter(path, filterName);
            }
        }

        // Save the changes
        storageManager.saveGalleryData(data);
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
     * Gets a list of available image paths from resources or external directory.
     * First tries to load images from embedded resources, then falls back to
     * an external directory in the user's home folder.
     *
     * @return An array of image resource paths or absolute paths for external images
     */
    public String[] getImageResourcePaths() {
        // Try embedded resources first
        String folder = "/imageGallery";
        URL dirURL = getClass().getResource(folder);

        if (dirURL != null) {
            // Existing code for embedded resources
            File directory = new File(dirURL.getFile());
            File[] files = directory.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".jpg") ||
                            name.toLowerCase().endsWith(".png") ||
                            name.toLowerCase().endsWith(".jpeg") ||
                            name.toLowerCase().endsWith(".gif"));

            if (files != null && files.length > 0) {
                List<String> result = new ArrayList<>();
                for (File file : files) {
                    result.add(folder + "/" + file.getName());
                }
                return result.toArray(new String[0]);
            }
        }

        // If no images in resources, look for external folder
        String externalFolder = System.getProperty("user.home") + "/GalleryImages";
        File externalDir = new File(externalFolder);

        if (!externalDir.exists()) {
            externalDir.mkdirs(); // Create folder if it doesn't exist
            System.out.println("Folder created: " + externalFolder);
            System.out.println("Add your images to this folder and restart the application");
            return new String[0];
        }

        File[] externalFiles = externalDir.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".png") ||
                        name.toLowerCase().endsWith(".jpeg") ||
                        name.toLowerCase().endsWith(".gif"));

        if (externalFiles == null || externalFiles.length == 0) {
            System.out.println("No images found in: " + externalFolder);
            return new String[0];
        }

        List<String> result = new ArrayList<>();
        for (File file : externalFiles) {
            result.add(file.getAbsolutePath()); // Absolute path for external images
        }

        return result.toArray(new String[0]);
    }

    /**
     * Gets the album name for an image label.
     *
     * @param label The image label
     * @return The album name, or null if not in a specific album
     */
    private String getAlbumForImage(JLabel label) {
        JPanel albumPanel = (JPanel) label.getClientProperty("albumPanel");
        if (albumPanel == null) return null;

        // Check if the panel or a parent has an albumName property
        if (albumPanel.getClientProperty("albumName") != null) {
            return (String) albumPanel.getClientProperty("albumName");
        }

        // Walk up the hierarchy to find albumView_XX panels
        Container parent = albumPanel;
        while (parent != null) {
            if (parent instanceof JPanel && parent.getName() != null &&
                    parent.getName().startsWith("albumView_")) {
                return parent.getName().substring("albumView_".length());
            }
            parent = parent.getParent();
        }

        return null;
    }

    /**
     * Gets the filter data for persistence, organized by albums.
     *
     * @return A map of image paths and album-specific filters
     */
    public Map<String, Object> getFilterData() {
        Map<String, Object> filterData = new HashMap<>();
        Map<String, Map<String, String>> albumFiltersMap = new HashMap<>();

        // Process all applied filters
        for (Map.Entry<JLabel, String> entry : appliedFilters.entrySet()) {
            JLabel label = entry.getKey();
            String filterName = entry.getValue();
            if ("None".equals(filterName)) continue;

            String path = (String) label.getClientProperty("resourcePath");
            String albumName = getAlbumForImage(label);

            if (path != null) {
                if (albumName != null && !albumName.equals("default")) {
                    // Album-specific filter
                    if (!albumFiltersMap.containsKey(albumName)) {
                        albumFiltersMap.put(albumName, new HashMap<>());
                    }
                    albumFiltersMap.get(albumName).put(path, filterName);
                } else {
                    // Regular filter
                    filterData.put(path, filterName);
                }
            }
        }

        // Add album filters to main data
        for (Map.Entry<String, Map<String, String>> entry : albumFiltersMap.entrySet()) {
            filterData.put(entry.getKey(), entry.getValue());
        }

        return filterData;
    }

    /**
     * Sets the filter data from persistent storage.
     *
     * @param filterData A map of image paths and albums to their applied filters
     */
    @SuppressWarnings("unchecked")
    public void setFilterData(Map<String, Object> filterData) {
        // Clear existing filter data
        this.appliedFilters.clear();

        // The actual filter application will happen in applyPersistedFilters
    }

    /**
     * Applies filters from persistent storage to loaded images.
     *
     * @param allLabels A map of resource paths to their corresponding labels
     */
    @SuppressWarnings("unchecked")
    public void applyPersistedFilters(Map<String, JLabel> allLabels) {
        // Check if StorageManager is available
        if (storageManager == null) return;

        // Load data from storage, don't use albumData directly
        GalleryData data = storageManager.loadGalleryData();
        Map<String, Object> filterData = data.getImageFilters();

        // Apply filters without updating storage
        for (Map.Entry<String, Object> entry : filterData.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                // Direct filter
                String path = key;
                String filterName = (String) value;

                JLabel label = allLabels.get(path);
                if (label != null && fullImageLabel != null) {
                    applyFilterWithoutStorage(label, fullImageLabel, filterName);
                }
            } else if (value instanceof Map) {
                // Album filters
                String albumName = key;
                Map<String, String> albumFilters = (Map<String, String>) value;

                for (Map.Entry<String, String> albumEntry : albumFilters.entrySet()) {
                    String path = albumEntry.getKey();
                    String filterName = albumEntry.getValue();

                    JLabel label = allLabels.get(path);
                    if (label != null && fullImageLabel != null) {
                        applyFilterWithoutStorage(label, fullImageLabel, filterName);
                    }
                }
            }
        }
    }

    /**
     * Applies a filter visually without updating storage
     */
    private void applyFilterWithoutStorage(JLabel originLabel, JLabel fullScreenLabel, String filterName) {
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

        // Remember which filter was applied (in memory only)
        if ("None".equals(filterName)) {
            appliedFilters.remove(originLabel);
        } else {
            appliedFilters.put(originLabel, filterName);
        }

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
}