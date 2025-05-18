
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
 * PictureGallery is a user-friendly Java Swing application designed for intuitive image album management.
 * It allows users to create and organize albums, add or remove images, view pictures in fullscreen mode,
 * apply various filters, and resize images effortlessly.
 *
 * The application also features drag-and-drop functionality for seamless image integration into albums.
 * Additionally, it handles essential configuration settings, verifies required API keys, and provides
 * prompts when credentials are missing, ensuring smooth and uninterrupted operation.
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
    private JComboBox<String> filterComboBox;

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


    /**
     * The constructor for PictureGallery.
     * Initializes the gallery and loads existing data.
     * Key features include:
     * 1. Checking for existing API keys.
     *
     *
     */

    /**
     * GalleryData is a helper class that stores album-related data and image filters.
     * It maintains a mapping between album names and the list of image file paths they contain.
     * Additionally, it keeps track of applied filters for each image.
     */
    private static class GalleryData {
        // Stores album names with corresponding lists of image file paths.
        Map<String, List<String>> albumData = new HashMap<>();

        // Stores image file paths with their corresponding applied filters.
        Map<String, String> imageFilters = new HashMap<>();
    }

    /**
     * Creates the main interface for the picture gallery application.
     * This method initializes various UI components, organizes views using CardLayout,
     * and sets up the main gallery view along with additional screens such as the fullscreen image view.
     *
     * @return The JPanel containing the complete application UI.
     */
    public JPanel createPictureGallery() {
        // Setting up the main layout system using CardLayout for switching views dynamically.
        viewLayout = new CardLayout();
        pictureGalleryApp = new JPanel(viewLayout);

        // Main gallery screen initialization.
        JPanel mainView = new JPanel(new BorderLayout());
        mainView.setBackground(Color.WHITE); // Set background color for visual consistency.

        // Top bar panel that holds navigation buttons.
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);

        // Buttons for adding new images and navigating albums.
        JButton addBtn = new JButton("Add"); // Button to add new images.
        JButton albumBtn = new JButton("Albums"); // Button to open the album selection screen.

        // Creating a button panel for better alignment.
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(addBtn); // Adding the "Add" button to the panel.

        // Adding button panels to the top bar.
        topBar.add(buttonPanel, BorderLayout.WEST);
        topBar.add(albumBtn, BorderLayout.EAST);
        mainView.add(topBar, BorderLayout.NORTH); // Placing the navigation bar at the top.

        // Gallery display panel with a grid layout.
        galleryPanel = new JPanel(new GridLayout(0, 2, 10, 10)); // Dynamic row count with two columns.
        galleryPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(galleryPanel); // Enable scrolling for large galleries.
        mainView.add(scrollPane, BorderLayout.CENTER);

        // Album selection bar for browsing different albums.
        albumBar = new JPanel();
        albumBar.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));
        albumBar.setBackground(Color.LIGHT_GRAY);

        // Wrapping albumBar inside a scroll pane to enable horizontal scrolling.
        JScrollPane albumScrollPane = new JScrollPane(
                albumBar,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, // Disable vertical scrolling.
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED // Enable horizontal scrolling when needed.
        );
        albumScrollPane.setPreferredSize(new Dimension(0, 160)); // Adjusting height of the album bar.
        albumScrollPane.setBorder(null); // Removing default borders for a cleaner look.

        // Adding the album selection bar at the bottom of the main view.
        mainView.add(albumScrollPane, BorderLayout.SOUTH);

        // Fullscreen image view setup.
        fullViewPanel = new JPanel(new BorderLayout());
        fullViewPanel.setBackground(Color.WHITE);
        fullImageLabel = new JLabel("", SwingConstants.CENTER); // Image label for displaying fullscreen images.
        fullViewPanel.add(fullImageLabel, BorderLayout.CENTER);

        // Navigation bar for the fullscreen view.
        JPanel fullTopBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fullTopBar.setBackground(Color.WHITE);
        JButton backBtn = new JButton("Back"); // Button to return to the main gallery view.
        backBtn.addActionListener(e -> viewLayout.show(pictureGalleryApp, "main"));
        fullTopBar.add(backBtn);
        fullViewPanel.add(fullTopBar, BorderLayout.NORTH);

        // Control panel for editing images in fullscreen mode.
        fullControls = new JPanel();
        fullControls.setBackground(Color.WHITE);
        JButton resizeBtn = new JButton("Resize"); // Button to open the resizing tool.
        JButton deleteBtn = new JButton("Delete"); // Button to delete the currently displayed image.
        fullControls.add(resizeBtn);
        fullControls.add(deleteBtn);

        // Dropdown list for selecting image filters.
        String[] filters = {"None", "Grayscale", "Sepia", "Invert"};
        filterComboBox = new JComboBox<>(filters);
        filterComboBox.addActionListener(e -> applyFilter((String) filterComboBox.getSelectedItem())); // Apply selected filter.
        fullControls.add(filterComboBox);

        // Adding the controls panel to the fullscreen view.
        fullViewPanel.add(fullControls, BorderLayout.SOUTH);

        // Setting up event listeners for interactive elements.
        addBtn.addActionListener(this::actionPerformed); // Triggers image addition process.
        deleteBtn.addActionListener(e -> deleteCurrentImage()); // Deletes the current image.
        resizeBtn.addActionListener(e -> showResizeDialog()); // Opens the resize tool.
        albumBtn.addActionListener(e -> showAlbumChooser()); // Opens the album selection screen.

        // Adding different screens to the main application container.
        pictureGalleryApp.add(mainView, "main"); // The main gallery view.
        pictureGalleryApp.add(fullViewPanel, "full"); // The fullscreen image view.

        // Creating a default album to store images.
        albums.put("default", galleryPanel);
        albumData.put("default", new ArrayList<>());

        // Load existing gallery data from storage.
        loadGallery();

        // Display the main gallery view by default when the application starts.
        viewLayout.show(pictureGalleryApp, "main");

        return pictureGalleryApp;
    }


    /**
     * Deletes the currently displayed image.
     * Confirms with the user before deleting.
     */
    /**
     * Deletes the currently displayed image from the gallery and the associated album.
     * The deletion process includes both the visual removal and the data structure cleanup.
     * <p>
     * Steps:
     * 1. Ask the user for confirmation before deleting.
     * 2. Identify the associated album panel and resource path of the selected image.
     * 3. Ensure the correct album panel is used, even if it is null initially.
     * 4. Locate and remove the image visually from the panel.
     * 5. Remove the image from stored album data, ensuring consistency with different path formats.
     * 6. Clean up any additional references related to filters and original images.
     * 7. Persist changes to storage and update the UI accordingly.
     */
    private void deleteCurrentImage() {
        int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this image?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            JLabel origin = (JLabel) fullImageLabel.getClientProperty("originLabel");

            if (origin != null) {
                JPanel albumPanel = (JPanel) origin.getClientProperty("albumPanel");
                String resourcePath = (String) origin.getClientProperty("resourcePath");

                // If albumPanel is null, use the current active album panel.
                if (albumPanel == null) {
                    albumPanel = getCurrentAlbumPanel();
                }

                // Locate the album that contains the image
                String albumName = null;
                for (Map.Entry<String, JPanel> entry : albums.entrySet()) {
                    if (entry.getValue() == albumPanel) {
                        albumName = entry.getKey();
                        break;
                    }
                }

                // Ensure an album was found, otherwise exit
                if (albumName == null) {
                    System.err.println("Could not find album for the image to delete");
                    return;
                }

                // Remove the image visually, checking if it belongs to the panel
                if (albumPanel.isAncestorOf(origin)) {
                    albumPanel.remove(origin);
                } else {
                    // Search the panel manually for the correct image label
                    Component[] components = albumPanel.getComponents();
                    for (Component comp : components) {
                        if (comp instanceof JLabel) {
                            JLabel label = (JLabel) comp;
                            String path = (String) label.getClientProperty("resourcePath");
                            if (resourcePath != null && resourcePath.equals(path)) {
                                albumPanel.remove(label);
                                break;
                            }
                        }
                    }
                }

                // Refresh the UI to reflect the removal
                albumPanel.revalidate();
                albumPanel.repaint();

                // Remove the image from stored album data, handling relative vs absolute paths
                List<String> paths = albumData.get(albumName);
                if (paths != null) {
                    boolean removed = paths.remove(resourcePath);

                    // If direct removal fails, try matching based on filename similarity
                    if (!removed) {
                        for (Iterator<String> it = paths.iterator(); it.hasNext(); ) {
                            String path = it.next();
                            if (path.endsWith(new File(resourcePath).getName())) {
                                it.remove();
                                break;
                            }
                        }
                    }
                }

                // Remove additional stored references
                appliedFilters.remove(origin);
                originalImages.remove(origin);

                // Save the updated gallery data
                saveGallery();

                // Clear the fullscreen view
                fullImageLabel.setIcon(null);
                fullImageLabel.putClientProperty("originLabel", null);

                // Maintain the user's current view if they were inside an album
                if (albumPanel != null && albumPanel != galleryPanel) {
                    String currentAlbum = null;
                    for (Map.Entry<String, JPanel> entry : albums.entrySet()) {
                        if (entry.getValue() == albumPanel) {
                            currentAlbum = entry.getKey();
                            break;
                        }
                    }

                    if (currentAlbum != null) {
                        viewLayout.show(pictureGalleryApp, currentAlbum);
                        return;
                    }
                }

            } else {
                System.err.println("No origin label found for the image to delete");
            }
        }
    }

    /**
     * Displays a dialog for selecting an image from the resources folder and adding it to an album.
     * The method:
     * 1. Retrieves available image paths from the resources directory.
     * 2. Presents a selection dialog for the user to choose an image.
     * 3. Compresses the selected image using TinyPNGService.
     * 4. Creates a JLabel to visually represent the image.
     * 5. Determines the correct target album and ensures a valid album panel exists.
     * 6. Adds the image to the album panel and refreshes the UI.
     * 7. Saves the updated gallery structure for future persistence.
     */
    private void showAddImageDialog() {
        String[] imagePaths = getImageResourcePaths();

        // If no images are found, inform the user and stop execution
        if (imagePaths.length == 0) {
            JOptionPane.showMessageDialog(null, "No images found in /resources/imageGallery",
                    "No Images Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Show selection dialog to choose an image
        String selected = (String) JOptionPane.showInputDialog(null,
                "Select an image:",
                "Add Image",
                JOptionPane.PLAIN_MESSAGE,
                null,
                imagePaths,
                imagePaths[0]);

        if (selected != null) {
            // Compress image using TinyPNG API before adding it
            TinyPNGService tinyPNGService = new TinyPNGService();
            File originalFile = new File("src/main/resources" + selected);
            File compressedFile = tinyPNGService.compressImage(originalFile);

            // Convert absolute path to relative path
            String relativePath = "/imageGallery/" + compressedFile.getName();
            JLabel imgLabel = createImageLabelFromResource(relativePath);

            // If the label creation failed, stop execution
            if (imgLabel.getIcon() == null) {
                JOptionPane.showMessageDialog(null,
                        "Failed to load image: " + relativePath,
                        "Image Loading Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Determine the correct target album
            String targetAlbum = currentAlbum;
            if (targetAlbum == null || targetAlbum.equals("main")) {
                targetAlbum = "default"; // Use "default" for the main album
            }

            // Retrieve or create the album panel
            JPanel targetPanel = albums.get(targetAlbum);
            if (targetPanel == null) {
                targetPanel = galleryPanel;
                albums.put(targetAlbum, targetPanel);
                if (!albumData.containsKey(targetAlbum)) {
                    albumData.put(targetAlbum, new ArrayList<>());
                }
            }

            // Store relevant metadata in the image label
            imgLabel.putClientProperty("albumPanel", targetPanel);
            imgLabel.putClientProperty("resourcePath", relativePath);

            // Add the image to the album panel
            targetPanel.add(imgLabel);
            targetPanel.revalidate();
            targetPanel.repaint();
            albumData.get(targetAlbum).add(relativePath);

            // Ensure the gallery view remains the default view after addition
            viewLayout.show(pictureGalleryApp, "main");

            // Save gallery data updates
            saveGallery();
        }
    }


    /**
     * Creates a JLabel containing an image from the given resource path.
     * The label represents a thumbnail view of the image and includes event listeners
     * for user interactions such as opening the image in fullscreen mode and supporting drag-and-drop.
     *
     * @param resourcePath The relative path to the image resource.
     * @return A JLabel displaying the image thumbnail with interactive capabilities.
     */
    private JLabel createImageLabelFromResource(String resourcePath) {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER); // Centers the image in the label.

        try {
            URL imageUrl = getClass().getResource(resourcePath);
            if (imageUrl != null) {
                Image image = ImageIO.read(imageUrl);
                if (image != null) {
                    // Resize the image to create a thumbnail view.
                    Image scaledImage = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    label.setIcon(new ImageIcon(scaledImage));
                    originalImages.put(label, image); // Store original image for reference.
                } else {
                    label.setText("Invalid image"); // Display error message if the image is null.
                }
            } else {
                label.setText("Not found"); // Handle missing resource case.
            }
        } catch (Exception e) {
            e.printStackTrace();
            label.setText("Error: " + e.getMessage()); // Display error message on exception.
        }

        // Associate the label with the corresponding album panel and image path.
        JPanel albumPanel = getCurrentAlbumPanel();
        label.putClientProperty("albumPanel", albumPanel);
        label.putClientProperty("resourcePath", resourcePath);

        // Add a mouse listener for fullscreen display functionality.
        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (label.getIcon() instanceof ImageIcon) {
                    ImageIcon currentIcon = (ImageIcon) label.getIcon();
                    // Display the image in fullscreen mode with a larger size.
                    fullImageLabel.setIcon(new ImageIcon(currentIcon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH)));
                    fullImageLabel.putClientProperty("originLabel", label);
                    // Retrieve and update the selected filter for this image.
                    String currentFilter = appliedFilters.getOrDefault(label, "None");
                    updateFilterComboBox(currentFilter);
                    viewLayout.show(pictureGalleryApp, "full");
                }
            }
        });

        // Enable drag-and-drop functionality for image movement.
        new DragSource().createDefaultDragGestureRecognizer(label, DnDConstants.ACTION_COPY, dge -> {
            String path = (String) label.getClientProperty("resourcePath");
            dge.startDrag(DragSource.DefaultCopyDrop, new StringSelection(path));
        });

        return label;
    }

    /**
     * Displays a dialog allowing the user to resize the currently displayed image.
     * Users enter width and height in a formatted string (e.g., "150x150").
     * <p>
     * The method:
     * 1. Prompts the user for dimensions using an input dialog.
     * 2. Validates the input format and extracts width and height values.
     * 3. Resizes the image displayed in fullscreen mode using smooth scaling.
     * 4. Updates the image thumbnail and revalidates the album panel for consistency.
     */
    private void showResizeDialog() {
        String input = JOptionPane.showInputDialog("New size (e.g. 150x150) :");

        // Validate input format before proceeding.
        if (input != null && input.matches("\\d+x\\d+")) {
            String[] parts = input.split("x");
            int width = Integer.parseInt(parts[0]);
            int height = Integer.parseInt(parts[1]);

            JLabel origin = (JLabel) fullImageLabel.getClientProperty("originLabel");
            if (origin != null && origin.getIcon() instanceof ImageIcon) {
                ImageIcon oldIcon = (ImageIcon) origin.getIcon();
                // Resize the image smoothly using the specified dimensions.
                Image resizedImage = oldIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                ImageIcon resizedIcon = new ImageIcon(resizedImage);
                fullImageLabel.setIcon(resizedIcon); // Update fullscreen view.
                origin.setIcon(resizedIcon); // Update thumbnail view.

                // Refresh the album panel to ensure updated visuals.
                JPanel albumPanel = (JPanel) origin.getClientProperty("albumPanel");
                if (albumPanel != null) albumPanel.revalidate();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Invalid format. Use widthxheight"); // Handle invalid input.
        }
    }

    /**
     * Displays a dialog for selecting or creating an album.
     * If an album with the given name does not exist, it is created and registered.
     * <p>
     * The method:
     * 1. Prompts the user to enter the name of an album.
     * 2. Checks if the album already exists; if not, it creates a new album panel.
     * 3. Initializes the album panel with a grid layout for storing images.
     * 4. Generates a styled album thumbnail for navigation.
     * 5. Updates the UI to reflect album creation and informs the user.
     */
    private void showAlbumChooser() {
        String name = JOptionPane.showInputDialog("Name of album to create/select :");

        // Ensure valid input before proceeding.
        if (name != null && !name.trim().isEmpty()) {
            if (!albums.containsKey(name)) {
                // Create a new album panel with grid layout.
                JPanel albumPanel = new JPanel(new GridLayout(0, 2, 10, 10));
                albumPanel.setBackground(Color.WHITE);

                // Register the album panel in the mapping structures.
                albums.put(name, albumPanel);
                albumData.put(name, new ArrayList<>());

                // Create a thumbnail representation for the album.
                JLabel thumbnail = new JLabel(name, JLabel.CENTER);
                thumbnail.setVerticalTextPosition(JLabel.BOTTOM);
                thumbnail.setHorizontalTextPosition(JLabel.CENTER);
                thumbnail.setPreferredSize(new Dimension(100, 120));
                thumbnail.setFont(new Font("Arial", Font.BOLD, 14));
                thumbnail.setOpaque(true);
                thumbnail.setBackground(new Color(240, 240, 240));
                thumbnail.setBorder(BorderFactory.createLineBorder(Color.GRAY));

                // Assign event listeners for drag-and-drop and album navigation.
                thumbnail.setTransferHandler(createAlbumTransferHandler(name, albumPanel, thumbnail));
                thumbnail.addMouseListener(createAlbumMouseListener(name, albumPanel, thumbnail));

                // Store the album thumbnail reference and update the album navigation bar.
                albumThumbnails.put(name, thumbnail);
                albumBar.add(thumbnail);
                albumBar.revalidate();

                // Notify the user of successful album creation.
                JOptionPane.showMessageDialog(null,
                        "Album '" + name + "' created successfully!",
                        "Album Created",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }


    /**
     * Creates a TransferHandler for an album thumbnail, allowing drag-and-drop functionality.
     * This handler enables users to drag images into an album, automatically registering
     * them within the album's data structure.
     * <p>
     * The method:
     * 1. Determines whether a dragged item is a valid image path.
     * 2. Creates a new JLabel for the image and associates it with the album.
     * 3. Updates the UI, ensuring the image appears within the album.
     * 4. If the album was previously empty, uses the first image as its thumbnail.
     * 5. Persists album modifications to the gallery storage.
     *
     * @param albumName  The name of the album receiving the dragged image.
     * @param albumPanel The panel representing the album's image collection.
     * @param thumbnail  The album's thumbnail label, updated if necessary.
     * @return A TransferHandler object to manage drag-and-drop operations.
     */
    private TransferHandler createAlbumTransferHandler(String albumName, JPanel albumPanel, JLabel thumbnail) {
        return new TransferHandler("text") {
            public boolean canImport(TransferHandler.TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            public boolean importData(TransferHandler.TransferSupport support) {
                try {
                    // Retrieve image path from drag-and-drop event.
                    String path = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                    JLabel newLabel = createImageLabelFromResource(path);
                    newLabel.putClientProperty("albumPanel", albumPanel);

                    // Ensure image label creation succeeded.
                    if (newLabel.getIcon() == null) {
                        System.out.println("Failed to create image label for: " + path);
                        return false;
                    }

                    // Add the image to the album panel and update the UI.
                    albumPanel.add(newLabel);
                    albumPanel.revalidate();
                    albumPanel.repaint();
                    albumData.get(albumName).add(path);

                    // If this is the first image in the album, use it as the thumbnail.
                    if (albumData.get(albumName).size() == 1 && newLabel.getIcon() instanceof ImageIcon) {
                        ImageIcon icon = (ImageIcon) newLabel.getIcon();
                        thumbnail.setIcon(icon);
                    }

                    // Save the updated gallery structure.
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
     * This listener allows users to open an album when clicking its thumbnail.
     * <p>
     * The method:
     * 1. Checks if the album view already exists in the UI.
     * 2. If the view exists, it simply switches to the album screen.
     * 3. If the view does not exist, it creates an album view with a navigation bar.
     * 4. The navigation bar includes a back button and a delete button.
     * 5. Adds the album view to the application and switches to it.
     *
     * @param albumName  The name of the album being opened.
     * @param albumPanel The panel containing the album's images.
     * @param thumbnail  The thumbnail label representing the album.
     * @return A MouseAdapter that handles album selection when clicked.
     */
    private MouseAdapter createAlbumMouseListener(String albumName, JPanel albumPanel, JLabel thumbnail) {
        return new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Check if the album view already exists.
                for (Component comp : pictureGalleryApp.getComponents()) {
                    if (comp.getName() != null && comp.getName().equals("albumView_" + albumName)) {
                        viewLayout.show(pictureGalleryApp, albumName);
                        currentAlbum = albumName;
                        return;
                    }
                }

                // Create a new album view panel.
                JPanel albumView = new JPanel(new BorderLayout());
                albumView.setName("albumView_" + albumName);
                albumView.setBackground(Color.WHITE);

                // Top navigation bar for the album.
                JPanel topBar = new JPanel(new BorderLayout());
                topBar.setBackground(Color.WHITE);

                // Back button to return to the main gallery.
                JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
                leftButtons.setBackground(Color.WHITE);
                JButton backBtn = new JButton("Back");
                backBtn.addActionListener(ev -> viewLayout.show(pictureGalleryApp, "main"));
                leftButtons.add(backBtn);

                // Delete album button for removing albums.
                JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                rightButtons.setBackground(Color.WHITE);
                JButton deleteAlbumBtn = new JButton("Delete Album");
                deleteAlbumBtn.setBackground(new Color(255, 100, 100));
                deleteAlbumBtn.addActionListener(ev -> deleteAlbum(albumName, thumbnail));
                rightButtons.add(deleteAlbumBtn);

                // Add buttons to the top navigation bar.
                topBar.add(leftButtons, BorderLayout.WEST);
                topBar.add(rightButtons, BorderLayout.EAST);

                // Display album title in the navigation bar.
                JLabel titleLabel = new JLabel("Album: " + albumName, JLabel.CENTER);
                titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
                topBar.add(titleLabel, BorderLayout.CENTER);

                // Add components to the album view.
                albumView.add(topBar, BorderLayout.NORTH);
                albumView.add(albumPanel, BorderLayout.CENTER);

                // Register album view in the application.
                pictureGalleryApp.add(albumView, albumName);
                viewLayout.show(pictureGalleryApp, albumName);

                currentAlbum = albumName;
            }
        };
    }

    /**
     * Retrieves the panel associated with the currently selected album.
     * If no album is selected, defaults to the main gallery panel.
     *
     * @return The JPanel representing the current album.
     */
    private JPanel getCurrentAlbumPanel() {
        return albums.getOrDefault(currentAlbum, galleryPanel);
    }

    /**
     * Scans the resources folder to retrieve available image paths.
     * Searches for images in common formats such as JPEG, PNG, and GIF.
     *
     * @return An array of image file paths located in the resources folder.
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
        }

        return result.toArray(new String[0]);
    }

    /**
     * Saves the current gallery data to a file for persistence.
     * Includes album structures and applied image filters.
     */
    private void saveGallery() {
        try (Writer writer = new FileWriter(SAVE_FILE)) {
            GalleryData data = new GalleryData();
            data.albumData = this.albumData;

            // Store filter information for images.
            for (Map.Entry<JLabel, String> entry : appliedFilters.entrySet()) {
                JLabel label = entry.getKey();
                String path = (String) label.getClientProperty("resourcePath");
                if (path != null) {
                    data.imageFilters.put(path, entry.getValue());
                }
            }

            // Use Gson for JSON formatting.
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the gallery data from a previously saved file, restoring album structures and image filters.
     * <p>
     * The method:
     * 1. Checks if a saved gallery data file exists; if not, it starts with an empty gallery.
     * 2. Reads the stored JSON file using Gson and extracts album image paths.
     * 3. Restores images to albums, re-adding their visual representation.
     * 4. Applies previously stored filters to images if they exist.
     * 5. Updates the UI by refreshing album panels and gallery navigation.
     */
    private void loadGallery() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            System.out.println("No gallery data file found. Starting with empty gallery.");
            return;
        }

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<GalleryData>() {
            }.getType();
            GalleryData data = new Gson().fromJson(reader, type);

            // Load album data into memory
            this.albumData = data.albumData;

            for (String albumName : albumData.keySet()) {
                JPanel panel = albums.getOrDefault(albumName, null);

                if (albumName.equals("default")) {
                    // Use existing gallery panel for default album
                    panel = galleryPanel;

                    // Load images into default gallery panel
                    for (String path : albumData.get(albumName)) {
                        JLabel label = createImageLabelFromResource(path);
                        if (label.getIcon() != null) {
                            panel.add(label);

                            // Restore filters if applicable
                            if (data.imageFilters.containsKey(path)) {
                                String filterName = data.imageFilters.get(path);
                                if (!"None".equals(filterName)) {
                                    appliedFilters.put(label, filterName);
                                    applyFilterToLabel(label, filterName);
                                }
                            }
                        }
                    }
                    panel.revalidate();
                    panel.repaint();
                    continue;
                }

                // Handle custom album panels
                if (panel == null) {
                    final JPanel albumPanel = new JPanel(new GridLayout(0, 2, 10, 10));
                    albumPanel.setBackground(Color.WHITE);
                    panel = albumPanel;
                    albums.put(albumName, albumPanel);

                    // Create album thumbnail
                    JLabel thumbnail = new JLabel(albumName, JLabel.CENTER);
                    thumbnail.setVerticalTextPosition(JLabel.BOTTOM);
                    thumbnail.setHorizontalTextPosition(JLabel.CENTER);
                    thumbnail.setPreferredSize(new Dimension(100, 120));
                    thumbnail.setOpaque(true);
                    thumbnail.setBackground(new Color(240, 240, 240));
                    thumbnail.setBorder(BorderFactory.createLineBorder(Color.GRAY));

                    // Load images into album panel
                    boolean thumbnailSet = false;
                    for (String path : albumData.get(albumName)) {
                        JLabel label = createImageLabelFromResource(path);
                        if (label.getIcon() != null) {
                            albumPanel.add(label);

                            // Apply saved filters if necessary
                            if (data.imageFilters.containsKey(path)) {
                                String filterName = data.imageFilters.get(path);
                                if (!"None".equals(filterName)) {
                                    appliedFilters.put(label, filterName);
                                    applyFilterToLabel(label, filterName);
                                }
                            }

                            // Set album thumbnail using the first valid image
                            if (!thumbnailSet && label.getIcon() instanceof ImageIcon) {
                                ImageIcon icon = (ImageIcon) label.getIcon();
                                thumbnail.setIcon(icon);
                                thumbnailSet = true;
                            }
                        }
                    }

                    // Register album thumbnail and event listeners
                    albumThumbnails.put(albumName, thumbnail);
                    thumbnail.addMouseListener(createAlbumMouseListener(albumName, albumPanel, thumbnail));
                    thumbnail.setTransferHandler(createAlbumTransferHandler(albumName, albumPanel, thumbnail));

                    albumBar.add(thumbnail);
                }
            }
            // Refresh UI components
            albumBar.revalidate();
            albumBar.repaint();
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
     * Applies a filter effect to a specific image label.
     * Filters modify the appearance of images by changing their color properties.
     * <p>
     * Supported filters:
     * - Grayscale: Converts the image to grayscale tones.
     * - Sepia: Applies a warm brown tone to simulate old photographs.
     * - Invert: Inverts the colors of the image.
     * <p>
     * The method:
     * 1. Ensures an original copy of the image exists before modifying it.
     * 2. Creates a BufferedImage to allow pixel-by-pixel processing.
     * 3. Iterates over each pixel to apply the selected filter transformation.
     * 4. Updates the JLabel with the newly processed image.
     *
     * @param label      The JLabel displaying the image to be filtered.
     * @param filterName The name of the filter to apply.
     */
    private void applyFilterToLabel(JLabel label, String filterName) {
        if (label == null || !(label.getIcon() instanceof ImageIcon)) return;

        // Store original image reference if not already cached
        if (!originalImages.containsKey(label)) {
            originalImages.put(label, ((ImageIcon) label.getIcon()).getImage());
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

        // Apply selected filter transformation
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
                        int tr = Math.min(255, (int) (0.393 * r + 0.769 * g + 0.189 * b));
                        int tg = Math.min(255, (int) (0.349 * r + 0.686 * g + 0.168 * b));
                        int tb = Math.min(255, (int) (0.272 * r + 0.534 * g + 0.131 * b));
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

        // Update image label with modified version
        Image thumbnailImage = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        label.setIcon(new ImageIcon(thumbnailImage));
    }


    /**
     * Applies a selected filter to the current image and stores filter information.
     * This method modifies the visual appearance of an image based on the chosen filter.
     * <p>
     * The process:
     * 1. Retrieves the original image from stored references or saves it if applying a filter for the first time.
     * 2. Creates a BufferedImage for pixel manipulation.
     * 3. Applies the selected filter, modifying the pixel color values accordingly.
     * 4. Updates the image display in both the fullscreen view and the gallery thumbnail.
     * 5. Refreshes the album panel to reflect changes and saves the gallery state.
     * <p>
     * Supported filters:
     * - Grayscale: Converts the image to black-and-white shades.
     * - Sepia: Applies a brownish tone to simulate old photographs.
     * - Invert: Inverts all color values for a negative effect.
     * - None: Retains the original image without modification.
     *
     * @param filterName The name of the filter to apply.
     */
    private void applyFilter(String filterName) {
        JLabel origin = (JLabel) fullImageLabel.getClientProperty("originLabel");
        if (origin == null) return;

        // Retrieve or store the original image reference
        Image original;
        if (!originalImages.containsKey(origin)) {
            original = ((ImageIcon) origin.getIcon()).getImage();
            originalImages.put(origin, original); // Save original image for future use
        } else {
            original = originalImages.get(origin);
        }

        int width = original.getWidth(null);
        int height = original.getHeight(null);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();

        // Skip processing if the filter is "None"
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
                            int tr = Math.min(255, (int) (0.393 * r + 0.769 * g + 0.189 * b));
                            int tg = Math.min(255, (int) (0.349 * r + 0.686 * g + 0.168 * b));
                            int tb = Math.min(255, (int) (0.272 * r + 0.534 * g + 0.131 * b));
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

        // Store the applied filter for future reference
        appliedFilters.put(origin, filterName);

        // Update both fullscreen and thumbnail views
        fullImageLabel.setIcon(new ImageIcon(img.getScaledInstance(200, 200, Image.SCALE_SMOOTH)));
        origin.setIcon(new ImageIcon(img.getScaledInstance(100, 100, Image.SCALE_SMOOTH)));

        // Refresh album panel to reflect changes
        JPanel albumPanel = (JPanel) origin.getClientProperty("albumPanel");
        if (albumPanel != null) {
            albumPanel.revalidate();
            albumPanel.repaint();
        }

        // Persist filter changes to the gallery file
        saveGallery();
    }

    /**
     * Updates the filter selection in the ComboBox UI element.
     * Ensures the dropdown menu reflects the currently applied filter on the image.
     *
     * @param filterName The name of the filter to be selected in the ComboBox.
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
     * Updates an image's file path within the album data.
     * This method ensures album records stay consistent when an image is moved or modified.
     * <p>
     * The process:
     * 1. Retrieves the album panel associated with the image.
     * 2. Finds the image's existing path within album storage.
     * 3. Updates the stored path to match the new location.
     *
     * @param label   The JLabel containing the image whose path needs updating.
     * @param newPath The new file path for the image.
     */
    private void updateImagePathInAlbumData(JLabel label, String newPath) {
        JPanel albumPanel = (JPanel) label.getClientProperty("albumPanel");
        String oldPath = (String) label.getClientProperty("resourcePath");

        for (Map.Entry<String, JPanel> entry : albums.entrySet()) {
            if (entry.getValue() == albumPanel) {
                List<String> paths = albumData.get(entry.getKey());
                int index = paths.indexOf(oldPath);
                if (index != -1) {
                    paths.set(index, newPath); // Update the stored path reference
                    label.putClientProperty("resourcePath", newPath);
                }
                break;
            }
        }
    }


    /**
     * Deletes an album and all its associated images from the application.
     * This method ensures proper cleanup of UI components and data structures
     * when an album is removed.
     * <p>
     * The process:
     * 1. Prevents deletion of the default album to avoid breaking the application structure.
     * 2. Prompts the user for confirmation before proceeding with deletion.
     * 3. Removes the album's panel from the main application layout.
     * 4. Updates internal data structures to erase the album and its references.
     * 5. Refreshes the UI by removing the album thumbnail from the album bar.
     * 6. Switches back to the main gallery view after deletion.
     * 7. Persists changes by updating the saved gallery file.
     *
     * @param albumName The name of the album to delete.
     * @param thumbnail The thumbnail representing the album in the album bar.
     */
    private void deleteAlbum(String albumName, JLabel thumbnail) {
        // Prevent deletion of the default album
        if (albumName.equals("default")) {
            JOptionPane.showMessageDialog(null,
                    "The main album cannot be deleted.",
                    "Action impossible",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirm album deletion with the user
        int confirm = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to delete the album '" + albumName + "' and all its images?",
                "Confirm deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Remove the album panel from the application
            pictureGalleryApp.remove(pictureGalleryApp.getComponentCount() - 1);

            // Clear album-related data from stored structures
            albums.remove(albumName);
            albumData.remove(albumName);
            albumThumbnails.remove(albumName);

            // Remove the album thumbnail from the album bar
            albumBar.remove(thumbnail);
            albumBar.revalidate();
            albumBar.repaint();

            // Redirect user back to the main gallery view
            viewLayout.show(pictureGalleryApp, "main");

            // Persist changes by saving the updated gallery state
            saveGallery();

            // Notify user of successful deletion
            JOptionPane.showMessageDialog(null,
                    "The album '" + albumName + "' has been successfully deleted.",
                    "Album deleted",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}