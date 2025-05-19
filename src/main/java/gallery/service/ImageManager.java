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
 * Gère les images de la galerie photo.
 * Responsable du chargement, de l'affichage et de la manipulation des images.
 */
public class ImageManager {
    // Stockage des images et filtres
    private Map<JLabel, Image> originalImages = new HashMap<>();
    private Map<JLabel, String> appliedFilters = new HashMap<>();

    // Références aux composants d'interface
    private JLabel fullImageLabel;
    private JComboBox<String> filterComboBox;

    /**
     * Enregistre les composants d'affichage plein écran
     */
    public void registerFullScreenComponents(JLabel fullImageLabel, JComboBox<String> filterComboBox) {
        this.fullImageLabel = fullImageLabel;
        this.filterComboBox = filterComboBox;
    }

    /**
     * Crée une étiquette contenant une image
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
     * Affiche une image en plein écran
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
     * Applique un filtre à une image
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
     * Applique un filtre niveaux de gris
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
     * Applique un filtre sépia
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
     * Applique un filtre d'inversion
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
     * Redimensionne une image
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
     * Récupère les chemins des images disponibles dans les ressources
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
     * Obtient les données de filtre pour la persistance
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
     * Définit les données de filtre depuis la persistance
     */
    public void setFilterData(Map<String, String> filterData) {
        this.appliedFilters.clear();
        // Les filtres seront appliqués lors du chargement des images
    }
}