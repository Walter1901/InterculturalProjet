
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * A picture gallery application that allows users to create and manage albums of images.
 * The application supports image organization, viewing, filtering, and resizing.
 */
public class PictureGallery {
    // Constants
    private static final String SAVE_FILE = "gallery_data.json";

    // UI Components
    private JPanel galleryPanel;
    private JPanel fullViewPanel;
    private CardLayout viewLayout;
    private JLabel fullImageLabel;
    private JPanel pictureGalleryApp;
    private JPanel albumBar;
    private JPanel fullControls;
    private JComboBox<String> filterCombo;

    // Data structures
    private Map<String, JPanel> albums = new HashMap<>();
    private Map<String, List<String>> albumData = new HashMap<>();
    private Map<String, JLabel> albumThumbnails = new HashMap<>();
    private String currentAlbum = "default";
    // Pour gérer la persistance des filtres
    private Map<JLabel, String> appliedFilters = new HashMap<>(); // Pour stocker quel filtre est appliqué à quelle image
    private Map<JLabel, Image> originalImages = new HashMap<>(); // Pour stocker les images originales

    // Gestionnaire d'actions pour le bouton d'ajout d'image
    private void actionPerformed(ActionEvent e) {
        showAddImageDialog();
    }

    private static class GalleryData {
        Map<String, List<String>> albumData = new HashMap<>();
        Map<String, String> imageFilters = new HashMap<>();
    }

    /**
     * Creates the picture gallery application.
     *
     * @return The main JPanel containing the application
     */
    public JPanel createPictureGallery() {
        viewLayout = new CardLayout();
        pictureGalleryApp = new JPanel(viewLayout);

        JPanel mainView = new JPanel(new BorderLayout());
        mainView.setBackground(Color.WHITE);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);

        JButton addBtn = new JButton("Add");
        JButton albumBtn = new JButton("Albums");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(addBtn);

        topBar.add(buttonPanel, BorderLayout.WEST);
        topBar.add(albumBtn, BorderLayout.EAST);
        mainView.add(topBar, BorderLayout.NORTH);

        galleryPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        galleryPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(galleryPanel);
        mainView.add(scrollPane, BorderLayout.CENTER);

        albumBar = new JPanel();
        albumBar.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        albumBar.setBackground(Color.LIGHT_GRAY);
        mainView.add(albumBar, BorderLayout.SOUTH);

        fullViewPanel = new JPanel(new BorderLayout());
        fullViewPanel.setBackground(Color.WHITE);
        fullImageLabel = new JLabel("", SwingConstants.CENTER);
        fullViewPanel.add(fullImageLabel, BorderLayout.CENTER);

        JPanel fullTopBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fullTopBar.setBackground(Color.WHITE);
        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> viewLayout.show(pictureGalleryApp, "main"));
        fullTopBar.add(backBtn);
        fullViewPanel.add(fullTopBar, BorderLayout.NORTH);

        fullControls = new JPanel();
        fullControls.setBackground(Color.WHITE);
        JButton resizeBtn = new JButton("Resize");
        JButton deleteBtn = new JButton("Delete");
        fullControls.add(resizeBtn);
        fullControls.add(deleteBtn);
        String[] filters = {"None", "Grayscale", "Sepia", "Invert"};
        filterCombo = new JComboBox<>(filters);
        filterCombo.addActionListener(e -> applyFilter((String) filterCombo.getSelectedItem()));
        fullControls.add(filterCombo);
        fullViewPanel.add(fullControls, BorderLayout.SOUTH);

        addBtn.addActionListener(this::actionPerformed);
        deleteBtn.addActionListener(e -> deleteCurrentImage());
        resizeBtn.addActionListener(e -> showResizeDialog());
        albumBtn.addActionListener(e -> showAlbumChooser());

        pictureGalleryApp.add(mainView, "main");
        pictureGalleryApp.add(fullViewPanel, "full");

        albums.put("default", galleryPanel);
        albumData.put("default", new ArrayList<>());
        loadGallery();

        viewLayout.show(pictureGalleryApp, "main");
        return pictureGalleryApp;
    }

    /**
     * Deletes the currently displayed image.
     * Confirms with the user before deleting.
     */
    private void deleteCurrentImage() {
        int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this image?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            JLabel origin = (JLabel) fullImageLabel.getClientProperty("originLabel");
            if (origin != null) {
                JPanel albumPanel = (JPanel) origin.getClientProperty("albumPanel");
                if (albumPanel != null) {
                    albumPanel.remove(origin);
                    albumPanel.revalidate();
                    albumPanel.repaint();
                }
                for (Map.Entry<String, JPanel> entry : albums.entrySet()) {
                    if (entry.getValue() == albumPanel) {
                        List<String> paths = albumData.get(entry.getKey());
                        paths.remove(origin.getClientProperty("resourcePath"));
                        break;
                    }
                }
                saveGallery();
            }
            viewLayout.show(pictureGalleryApp, "main");
        }
    }

    /**
     * Shows a dialog to add images to the gallery.
     * Images are selected from the resources folder.
     */
    private void showAddImageDialog() {
        String[] imagePaths = getImageResourcePaths();
        if (imagePaths.length == 0) {
            JOptionPane.showMessageDialog(null, "No images found in /resources/imageGallery",
                    "No Images Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selected = (String) JOptionPane.showInputDialog(null,
                "Select an image:",
                "Add Image",
                JOptionPane.PLAIN_MESSAGE,
                null,
                imagePaths,
                imagePaths[0]);

        if (selected != null) {
            // Utiliser directement l'image sans compression
            String imagePath = selected;

            // Créer un label avec l'image
            JLabel imgLabel = createImageLabelFromResource(imagePath);

            // Si la création du label a échoué, arrêter le processus
            if (imgLabel.getIcon() == null) {
                JOptionPane.showMessageDialog(null,
                        "Failed to load image: " + imagePath,
                        "Image Loading Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Ajouter l'image à l'album courant
            JPanel mainGalleryPanel = albums.get(currentAlbum);
            mainGalleryPanel.add(imgLabel);
            mainGalleryPanel.revalidate();
            mainGalleryPanel.repaint();

            // Mettre à jour les données de l'album
            if (!albumData.containsKey(currentAlbum)) {
                albumData.put(currentAlbum, new ArrayList<>());
            }
            albumData.get(currentAlbum).add(imagePath);

            // Enregistrer les modifications
            saveGallery();

        }
    }

    /**
     * Creates a JLabel containing an image from the given resource path.
     * Sets up event listeners for the label.
     *
     * @param resourcePath Path to the image resource
     * @return JLabel containing the image
     */
    private JLabel createImageLabelFromResource(String resourcePath) {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);

        try {
            // Log pour débogage
            System.out.println("Loading image: " + resourcePath);

            URL imageUrl = getClass().getResource(resourcePath);
            if (imageUrl != null) {
                System.out.println("Image URL found: " + imageUrl);

                // Chargement de l'image
                Image image = ImageIO.read(imageUrl);

                if (image != null) {
                    // Redimensionner l'image pour l'affichage en vignette
                    Image scaledImage = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    label.setIcon(new ImageIcon(scaledImage));

                    // Conserver l'image originale pour les opérations de filtrage
                    originalImages.put(label, image);

                    System.out.println("Image loaded successfully");
                } else {
                    System.out.println("Failed to read image data");
                    label.setText("Invalid image");
                }
            } else {
                System.out.println("Image URL not found for: " + resourcePath);
                label.setText("Not found");
            }
        } catch (Exception e) {
            System.out.println("Exception while loading image: " + e.getMessage());
            e.printStackTrace();
            label.setText("Error: " + e.getMessage());
        }

        // Stocker les références pour l'album
        label.putClientProperty("albumPanel", getCurrentAlbumPanel());
        label.putClientProperty("resourcePath", resourcePath);

        // Ajouter les comportements d'interaction
        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (label.getIcon() instanceof ImageIcon) {
                    ImageIcon currentIcon = (ImageIcon) label.getIcon();

                    // Afficher l'image en vue complète
                    fullImageLabel.setIcon(new ImageIcon(currentIcon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH)));
                    fullImageLabel.putClientProperty("originLabel", label);

                    // Mettre à jour le menu déroulant pour refléter le filtre actuel
                    String currentFilter = appliedFilters.getOrDefault(label, "None");
                    updateFilterComboBox(currentFilter);

                    viewLayout.show(pictureGalleryApp, "full");
                }
            }
        });

        // Support pour glisser-déposer
        new DragSource().createDefaultDragGestureRecognizer(label, DnDConstants.ACTION_COPY, dge -> {
            String path = (String) label.getClientProperty("resourcePath");
            dge.startDrag(DragSource.DefaultCopyDrop, new StringSelection(path));
        });

        return label;
    }

    /**
     * Shows a dialog to resize the current image.
     * User can specify width and height.
     */
    private void showResizeDialog() {
        String input = JOptionPane.showInputDialog("New size (e.g. 150x150) :");
        if (input != null && input.matches("\\d+x\\d+")) {
            String[] parts = input.split("x");
            int width = Integer.parseInt(parts[0]);
            int height = Integer.parseInt(parts[1]);
            JLabel origin = (JLabel) fullImageLabel.getClientProperty("originLabel");
            if (origin != null && origin.getIcon() instanceof ImageIcon) {
                ImageIcon oldIcon = (ImageIcon) origin.getIcon();
                Image resizedImage = oldIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                ImageIcon resizedIcon = new ImageIcon(resizedImage);
                fullImageLabel.setIcon(resizedIcon);
                origin.setIcon(resizedIcon);
                JPanel albumPanel = (JPanel) origin.getClientProperty("albumPanel");
                if (albumPanel != null) albumPanel.revalidate();
            }
        } else JOptionPane.showMessageDialog(null, "Invalid format. Use widthxheight");
    }

    /**
     * Shows a dialog to create or select an album.
     * Creates the album if it doesn't exist.
     */
    private void showAlbumChooser() {
        String name = JOptionPane.showInputDialog("Name of album to create/select :");
        if (name != null && !name.trim().isEmpty()) {
            if (!albums.containsKey(name)) {
                JPanel albumPanel = new JPanel(new GridLayout(0, 2, 10, 10));
                albumPanel.setBackground(Color.WHITE);
                albums.put(name, albumPanel);
                albumData.put(name, new ArrayList<>());

                JLabel thumbnail = new JLabel(name, JLabel.CENTER);
                thumbnail.setVerticalTextPosition(JLabel.BOTTOM);
                thumbnail.setHorizontalTextPosition(JLabel.CENTER);
                thumbnail.setPreferredSize(new Dimension(100, 120));
                thumbnail.setOpaque(true);
                thumbnail.setBackground(new Color(240, 240, 240));
                thumbnail.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                thumbnail.setTransferHandler(createAlbumTransferHandler(name, albumPanel, thumbnail));

                thumbnail.addMouseListener(createAlbumMouseListener(name, albumPanel, thumbnail));

                albumThumbnails.put(name, thumbnail);
                albumBar.add(thumbnail);
                albumBar.revalidate();

                JOptionPane.showMessageDialog(null,
                        "Album '" + name + "' created successfully!",
                        "Album Created",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            currentAlbum = name;
            JOptionPane.showMessageDialog(null,
                    "Current album set to: " + name,
                    "Album Selected",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Creates a TransferHandler for an album thumbnail.
     *
     * @param albumName The name of the album
     * @param albumPanel The panel containing album images
     * @param thumbnail The thumbnail label for the album
     * @return TransferHandler for drag and drop operations
     */
    private TransferHandler createAlbumTransferHandler(String albumName, JPanel albumPanel, JLabel thumbnail) {
        return new TransferHandler("text") {
            public boolean canImport(TransferHandler.TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            public boolean importData(TransferHandler.TransferSupport support) {
                try {
                    String path = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                    JLabel newLabel = createImageLabelFromResource(path);

                    // Vérifier si l'étiquette a été créée avec succès
                    if (newLabel.getIcon() == null) {
                        System.out.println("Failed to create image label for: " + path);
                        return false;
                    }

                    albumPanel.add(newLabel);
                    albumPanel.revalidate();
                    albumPanel.repaint();
                    albumData.get(albumName).add(path);

                    // Si c'est la première image, utiliser comme vignette d'album
                    if (albumData.get(albumName).size() == 1 && newLabel.getIcon() instanceof ImageIcon) {
                        ImageIcon icon = (ImageIcon) newLabel.getIcon();
                        thumbnail.setIcon(icon);
                    }

                    saveGallery();
                    return true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Error during drag & drop: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                return false;
            }
        };
    }

    /**
     * Creates a MouseListener for an album thumbnail.
     *
     * @param albumName The name of the album
     * @param albumPanel The panel containing album images
     * @param thumbnail The thumbnail label for the album
     * @return MouseAdapter that handles click events
     */
    private MouseAdapter createAlbumMouseListener(String albumName, JPanel albumPanel, JLabel thumbnail) {
        return new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JPanel albumView = new JPanel(new BorderLayout());
                albumView.setBackground(Color.WHITE);

                JPanel topBar = new JPanel(new BorderLayout());
                topBar.setBackground(Color.WHITE);

                JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
                leftButtons.setBackground(Color.WHITE);
                JButton backBtn = new JButton("Back");
                backBtn.addActionListener(ev -> viewLayout.show(pictureGalleryApp, "main"));
                leftButtons.add(backBtn);

                JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                rightButtons.setBackground(Color.WHITE);
                JButton deleteAlbumBtn = new JButton("Delete Album");
                deleteAlbumBtn.setBackground(new Color(255, 100, 100));
                deleteAlbumBtn.addActionListener(ev -> deleteAlbum(albumName, thumbnail));
                rightButtons.add(deleteAlbumBtn);

                topBar.add(leftButtons, BorderLayout.WEST);
                topBar.add(rightButtons, BorderLayout.EAST);
                albumView.add(topBar, BorderLayout.NORTH);

                // Ajouter un titre pour l'album
                JLabel titleLabel = new JLabel("Album: " + albumName, JLabel.CENTER);
                titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
                topBar.add(titleLabel, BorderLayout.CENTER);

                JScrollPane scrollPane = new JScrollPane(albumPanel);
                albumView.add(scrollPane, BorderLayout.CENTER);

                pictureGalleryApp.add(albumView, albumName);
                viewLayout.show(pictureGalleryApp, albumName);

                // Définir cet album comme l'album courant
                currentAlbum = albumName;
            }
        };
    }

    /**
     * Returns the current album panel.
     *
     * @return The current album panel
     */
    private JPanel getCurrentAlbumPanel() {
        return albums.getOrDefault(currentAlbum, galleryPanel);
    }

    /**
     * Returns an array of image resource paths from the resources folder.
     *
     * @return Array of image paths
     */
    private String[] getImageResourcePaths() {
        String folder = "/imageGallery";
        URL dirURL = PictureGallery.class.getResource(folder);

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
            System.out.println("Found image: " + file.getName());
        }

        return result.toArray(new String[0]);
    }

    /**
     * Saves the gallery data to a file, including filter information.
     */
    private void saveGallery() {
        try (Writer writer = new FileWriter(SAVE_FILE)) {
            GalleryData data = new GalleryData();
            data.albumData = this.albumData;

            // Convert JLabel -> String filter map to String -> String map for serialization
            for (Map.Entry<JLabel, String> entry : appliedFilters.entrySet()) {
                JLabel label = entry.getKey();
                String path = (String) label.getClientProperty("resourcePath");
                if (path != null) {
                    data.imageFilters.put(path, entry.getValue());
                }
            }

            new Gson().toJson(data, writer);
            System.out.println("Gallery data saved successfully");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error saving gallery data: " + e.getMessage());
        }
    }

    /**
     * Loads the gallery data from a file, including filter information.
     */
    private void loadGallery() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            System.out.println("No gallery data file found. Starting with empty gallery.");
            return;
        }

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<GalleryData>() {}.getType();
            GalleryData data = new Gson().fromJson(reader, type);

            // Load album data
            this.albumData = data.albumData;

            for (String albumName : albumData.keySet()) {
                JPanel panel = albums.getOrDefault(albumName, null);

                if (albumName.equals("default")) {
                    // For the default album, use the existing galleryPanel
                    panel = galleryPanel;

                    // Load images for the default gallery
                    for (String path : albumData.get(albumName)) {
                        JLabel label = createImageLabelFromResource(path);
                        if (label.getIcon() != null) {
                            panel.add(label);

                            // Apply saved filter if one exists
                            if (data.imageFilters.containsKey(path)) {
                                String filterName = data.imageFilters.get(path);
                                if (!"None".equals(filterName)) {
                                    // Store reference to apply filter later
                                    appliedFilters.put(label, filterName);
                                    // Apply the filter
                                    applyFilterToLabel(label, filterName);
                                }
                            }
                        }
                    }

                    panel.revalidate();
                    panel.repaint();
                    continue;
                }

                // For custom albums
                if (panel == null) {
                    // Create a new panel for this album
                    final JPanel albumPanel = new JPanel(new GridLayout(0, 2, 10, 10));
                    albumPanel.setBackground(Color.WHITE);
                    panel = albumPanel;
                    albums.put(albumName, albumPanel);

                    JLabel thumbnail = new JLabel(albumName, JLabel.CENTER);
                    thumbnail.setVerticalTextPosition(JLabel.BOTTOM);
                    thumbnail.setHorizontalTextPosition(JLabel.CENTER);
                    thumbnail.setPreferredSize(new Dimension(100, 120));
                    thumbnail.setOpaque(true);
                    thumbnail.setBackground(new Color(240, 240, 240));
                    thumbnail.setBorder(BorderFactory.createLineBorder(Color.GRAY));

                    // Load existing images
                    boolean thumbnailSet = false;
                    for (String path : albumData.get(albumName)) {
                        JLabel label = createImageLabelFromResource(path);
                        if (label.getIcon() != null) {
                            albumPanel.add(label);

                            // Apply saved filter if one exists
                            if (data.imageFilters.containsKey(path)) {
                                String filterName = data.imageFilters.get(path);
                                if (!"None".equals(filterName)) {
                                    appliedFilters.put(label, filterName);
                                    applyFilterToLabel(label, filterName);
                                }
                            }

                            // Set the first valid image as album thumbnail
                            if (!thumbnailSet && label.getIcon() instanceof ImageIcon) {
                                ImageIcon icon = (ImageIcon) label.getIcon();
                                thumbnail.setIcon(icon);
                                thumbnailSet = true;
                            }
                        }
                    }

                    albumThumbnails.put(albumName, thumbnail);
                    thumbnail.addMouseListener(createAlbumMouseListener(albumName, albumPanel, thumbnail));
                    thumbnail.setTransferHandler(createAlbumTransferHandler(albumName, albumPanel, thumbnail));

                    albumBar.add(thumbnail);
                }
            }
            albumBar.revalidate();
            galleryPanel.revalidate();
            galleryPanel.repaint();

            System.out.println("Gallery data loaded successfully");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading gallery data: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unexpected error loading gallery: " + e.getMessage());
        }
    }

    /**
     * Applies a filter to a specific image label
     */
    private void applyFilterToLabel(JLabel label, String filterName) {
        if (label == null || !(label.getIcon() instanceof ImageIcon)) return;

        // First ensure we have the original image
        if (!originalImages.containsKey(label)) {
            originalImages.put(label, ((ImageIcon)label.getIcon()).getImage());
        }

        Image original = originalImages.get(label);
        if (original == null) {
            System.out.println("Original image not found for filter application");
            return;
        }

        int width = original.getWidth(null);
        int height = original.getHeight(null);

        if (width <= 0 || height <= 0) {
            System.out.println("Invalid image dimensions: " + width + "x" + height);
            return;
        }

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();

        // Apply the filter
        switch (filterName) {
            case "Grayscale":
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
                break;
            case "Sepia":
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int rgb = img.getRGB(x, y);
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;
                        int tr = (int)(0.393 * r + 0.769 * g + 0.189 * b);
                        int tg = (int)(0.349 * r + 0.686 * g + 0.168 * b);
                        int tb = (int)(0.272 * r + 0.534 * g + 0.131 * b);
                        tr = Math.min(255, tr);
                        tg = Math.min(255, tg);
                        tb = Math.min(255, tb);
                        int newRGB = (tr << 16) | (tg << 8) | tb;
                        img.setRGB(x, y, newRGB);
                    }
                }
                break;
            case "Invert":
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
                break;
            case "None":
            default:
                // No processing needed for "None"
                break;
        }

        // Update the label with filtered image
        Image thumbnailImage = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        label.setIcon(new ImageIcon(thumbnailImage));
    }

    /**
     * Applies a filter to the current image and stores the filter information.
     *
     * @param filterName The name of the filter to apply
     */
    private void applyFilter(String filterName) {
        JLabel origin = (JLabel) fullImageLabel.getClientProperty("originLabel");
        if (origin == null) return;

        // Get or create the original image reference
        Image original;
        if (!originalImages.containsKey(origin)) {
            // First time applying a filter, save the original
            original = ((ImageIcon) origin.getIcon()).getImage();
            originalImages.put(origin, original);
        } else {
            // Use the saved original
            original = originalImages.get(origin);
        }

        int width = original.getWidth(null);
        int height = original.getHeight(null);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();

        // Skip processing if "None" selected - just use original
        if (!"None".equals(filterName)) {
            switch (filterName) {
                case "Grayscale":
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
                    break;
                case "Sepia":
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int rgb = img.getRGB(x, y);
                            int r = (rgb >> 16) & 0xFF;
                            int g = (rgb >> 8) & 0xFF;
                            int b = rgb & 0xFF;
                            int tr = (int)(0.393 * r + 0.769 * g + 0.189 * b);
                            int tg = (int)(0.349 * r + 0.686 * g + 0.168 * b);
                            int tb = (int)(0.272 * r + 0.534 * g + 0.131 * b);
                            tr = Math.min(255, tr);
                            tg = Math.min(255, tg);
                            tb = Math.min(255, tb);
                            int newRGB = (tr << 16) | (tg << 8) | tb;
                            img.setRGB(x, y, newRGB);
                        }
                    }
                    break;
                case "Invert":
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
                    break;
            }
        }

        // Store the current filter for this image
        appliedFilters.put(origin, filterName);

        // Update images in both views
        // 1. Full view (keep the larger size)
        Image fullSizeImage = img.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        fullImageLabel.setIcon(new ImageIcon(fullSizeImage));

        // 2. Gallery thumbnail
        Image thumbnailImage = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        origin.setIcon(new ImageIcon(thumbnailImage));

        // Update the album panel
        JPanel albumPanel = (JPanel) origin.getClientProperty("albumPanel");
        if (albumPanel != null) {
            albumPanel.revalidate();
            albumPanel.repaint();
        }

        // Save the gallery state to persist the filter
        saveGallery();
    }

    /**
     * Updates the filter ComboBox to reflect the currently applied filter
     * @param filterName The name of the filter to select
     */
    private void updateFilterComboBox(String filterName) {
        for (Component comp : fullControls.getComponents()) {
            if (comp instanceof JComboBox) {
                @SuppressWarnings("unchecked")
                JComboBox<String> filterCombo = (JComboBox<String>) comp;
                filterCombo.setSelectedItem(filterName);
                break;
            }
        }
    }

    /**
     * Updates the image path in the album data.
     *
     * @param label The label containing the image
     * @param newPath The new path for the image
     */
    private void updateImagePathInAlbumData(JLabel label, String newPath) {
        JPanel albumPanel = (JPanel) label.getClientProperty("albumPanel");
        String oldPath = (String) label.getClientProperty("resourcePath");

        for (Map.Entry<String, JPanel> entry : albums.entrySet()) {
            if (entry.getValue() == albumPanel) {
                List<String> paths = albumData.get(entry.getKey());
                int index = paths.indexOf(oldPath);
                if (index != -1) {
                    paths.set(index, newPath);
                    label.putClientProperty("resourcePath", newPath);
                }
                break;
            }
        }
    }

    /**
     * Deletes an album and all its images.
     *
     * @param albumName The name of the album to delete
     * @param thumbnail The thumbnail of the album to remove from the album bar
     */
    private void deleteAlbum(String albumName, JLabel thumbnail) {
        // Don't allow deleting the default album
        if (albumName.equals("default")) {
            JOptionPane.showMessageDialog(null,
                    "The main album cannot be deleted.",
                    "Action impossible",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ask for confirmation before deletion
        int confirm = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to delete the album '" + albumName + "' and all its images?",
                "Confirm deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Remove the album panel from pictureGalleryApp
            pictureGalleryApp.remove(pictureGalleryApp.getComponentCount() - 1);

            // Remove the album from data structures
            albums.remove(albumName);
            albumData.remove(albumName);
            albumThumbnails.remove(albumName);

            // Remove the thumbnail from the album bar
            albumBar.remove(thumbnail);
            albumBar.revalidate();
            albumBar.repaint();

            // Go back to the main view
            viewLayout.show(pictureGalleryApp, "main");

            // Update the save file
            saveGallery();

            JOptionPane.showMessageDialog(null,
                    "The album '" + albumName + "' has been successfully deleted.",
                    "Album deleted",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}