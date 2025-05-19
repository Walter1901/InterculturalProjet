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
    // Stockage des images et filtres
    private Map<JLabel, Image> originalImages = new HashMap<>();
    private Map<JLabel, String> appliedFilters = new HashMap<>();

    // Références aux composants d'interface
    private JLabel fullImageLabel;
    private JComboBox<String> filterComboBox;

    /**
     * Registers UI components for full-screen image display.
     *
     * @param fullImageLabel The label that displays the full-screen image
     * @param filterComboBox The combo box for selecting image filters
     */
    public void registerFullScreenComponents(JLabel fullImageLabel, JComboBox<String> filterComboBox) {
        this.fullImageLabel = fullImageLabel;
        this.filterComboBox = filterComboBox;
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
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);

        try {
            URL imageUrl = getClass().getResource(resourcePath);
            if (imageUrl != null) {
                Image image = ImageIO.read(imageUrl);
                if (image != null) {
                    // Créer la miniature
                    Image scaledImage = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    label.setIcon(new ImageIcon(scaledImage));

                    // Stocker l'image originale
                    originalImages.put(label, image);
                } else {
                    label.setText("Invalid image");
                }
            } else {
                label.setText("Not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            label.setText("Error: " + e.getMessage());
        }

        // Stocker des métadonnées sur l'étiquette
        label.putClientProperty("albumPanel", albumPanel);
        label.putClientProperty("resourcePath", resourcePath);

        // Ajouter un écouteur pour l'affichage plein écran
        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showFullScreenImage(label);
            }
        });

        // Configurer le support de drag-and-drop
        new java.awt.dnd.DragSource().createDefaultDragGestureRecognizer(
                label,
                java.awt.dnd.DnDConstants.ACTION_COPY,
                dge -> {
                    String path = (String) label.getClientProperty("resourcePath");
                    dge.startDrag(
                            java.awt.dnd.DragSource.DefaultCopyDrop,
                            new java.awt.datatransfer.StringSelection(path)
                    );
                }
        );

        return label;
    }

    /**
     * Shows an image in full-screen view.
     *
     * @param label The label containing the image to display
     */
    private void showFullScreenImage(JLabel label) {
        if (fullImageLabel == null) return;

        if (label.getIcon() instanceof ImageIcon) {
            ImageIcon currentIcon = (ImageIcon) label.getIcon();

            // Afficher l'image en plein écran
            fullImageLabel.setIcon(new ImageIcon(
                    currentIcon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH)
            ));

            // Stocker l'étiquette d'origine pour référence
            fullImageLabel.putClientProperty("originLabel", label);

            // Mettre à jour le filtre sélectionné
            if (filterComboBox != null) {
                String currentFilter = appliedFilters.getOrDefault(label, "None");
                filterComboBox.setSelectedItem(currentFilter);
            }

            // Afficher la vue plein écran (via le composant parent)
            Container parent = fullImageLabel.getParent().getParent();
            if (parent instanceof JPanel) {
                CardLayout layout = (CardLayout) ((JPanel) parent).getLayout();
                layout.show((JPanel) parent, "full");
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
        if (originLabel == null || !(originLabel.getIcon() instanceof ImageIcon)) return;

        // Récupérer ou stocker l'image originale
        if (!originalImages.containsKey(originLabel)) {
            originalImages.put(originLabel, ((ImageIcon) originLabel.getIcon()).getImage());
        }

        Image original = originalImages.get(originLabel);
        if (original == null) return;

        int width = original.getWidth(null);
        int height = original.getHeight(null);

        if (width <= 0 || height <= 0) return;

        // Créer une image buffer pour manipuler les pixels
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();

        // Appliquer le filtre sélectionné
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

        // Enregistrer le filtre appliqué
        appliedFilters.put(originLabel, filterName);

        // Mettre à jour les affichages
        Image thumbnailImage = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        Image fullScreenImage = img.getScaledInstance(300, 300, Image.SCALE_SMOOTH);

        originLabel.setIcon(new ImageIcon(thumbnailImage));
        fullScreenLabel.setIcon(new ImageIcon(fullScreenImage));

        // Rafraîchir le panel d'album
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

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (r + g + b) / 3;
                int newRGB = (gray << 16) | (gray << 8) | gray;
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

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int tr = Math.min(255, (int) (0.393 * r + 0.769 * g + 0.189 * b));
                int tg = Math.min(255, (int) (0.349 * r + 0.686 * g + 0.168 * b));
                int tb = Math.min(255, (int) (0.272 * r + 0.534 * g + 0.131 * b));
                int newRGB = (tr << 16) | (tg << 8) | tb;
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

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int r = 255 - ((rgb >> 16) & 0xFF);
                int g = 255 - ((rgb >> 8) & 0xFF);
                int b = 255 - (rgb & 0xFF);
                int newRGB = (r << 16) | (g << 8) | b;
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
        if (originLabel != null && originLabel.getIcon() instanceof ImageIcon) {
            ImageIcon oldIcon = (ImageIcon) originLabel.getIcon();

            // Redimensionner l'image
            Image resizedImage = oldIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(resizedImage);

            // Mettre à jour les affichages
            fullScreenLabel.setIcon(resizedIcon);
            originLabel.setIcon(resizedIcon);

            // Rafraîchir le panel d'album
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
        String folder = "/imageGallery";
        URL dirURL = getClass().getResource(folder);

        if (dirURL == null) {
            System.out.println("Resource directory not found: " + folder);
            return new String[0];
        }

        File directory = new File(dirURL.getFile());
        File[] files = directory.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".png") ||
                        name.toLowerCase().endsWith(".jpeg") ||
                        name.toLowerCase().endsWith(".gif"));

        if (files == null) {
            System.out.println("No files found in directory: " + directory.getAbsolutePath());
            return new String[0];
        }

        List<String> result = new ArrayList<>();
        for (File file : files) {
            result.add(folder + "/" + file.getName());
        }

        return result.toArray(new String[0]);
    }

    /**
     * Gets the filter data for persistence.
     *
     * @return A map of image paths to their applied filters
     */
    public Map<String, String> getFilterData() {
        Map<String, String> filterData = new HashMap<>();

        for (Map.Entry<JLabel, String> entry : appliedFilters.entrySet()) {
            JLabel label = entry.getKey();
            String path = (String) label.getClientProperty("resourcePath");

            if (path != null) {
                filterData.put(path, entry.getValue());
            }
        }

        return filterData;
    }

    /**
     * Sets the filter data from persistent storage.
     *
     * @param filterData A map of image paths to their applied filters
     */
    public void setFilterData(Map<String, String> filterData) {
        this.appliedFilters.clear();
        // Les filtres seront appliqués lors du chargement des images
    }
}