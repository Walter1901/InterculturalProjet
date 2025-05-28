package gallery.ui;

import gallery.PictureGalleryApp;
import gallery.service.AlbumManager;
import gallery.service.ImageManager;

import javax.swing.*;
import java.awt.*;

/**
 * Manages the user interface for the gallery application.
 *
 * This class is responsible for creating and managing all UI components,
 * handling user interactions, and providing a consistent user experience.
 * The UI is structured with a CardLayout that allows switching between
 * different views:
 * - Main Gallery View - Shows all images in the current album
 * - Full Screen View - Shows a single image in detail with editing options
 * - Album Views - One view per album, dynamically created
 *
 * The class delegates business logic to the appropriate manager classes
 * and updates the UI based on the results.
 */
public class GalleryUI {
    // References to other components
    private PictureGalleryApp app;           // Main application
    private AlbumManager albumManager;       // Album management
    private ImageManager imageManager;       // Image processing

    // Main UI components
    private JPanel mainPanel;                // Contains all views
    private CardLayout cardLayout;           // For switching between views
    private JPanel galleryPanel;             // Shows images grid
    private JPanel fullViewPanel;            // Shows single image large
    private JLabel fullImageLabel;           // Displays full-size image
    private JPanel albumBar;                 // Shows album thumbnails
    private JPanel fullControls;             // Controls for full view
    private JComboBox<String> filterComboBox;// Filter selection dropdown


    public GalleryUI(PictureGalleryApp app, AlbumManager albumManager, ImageManager imageManager) {
        this.app = app;                  // Store reference to main app
        this.albumManager = albumManager;// Store reference to album manager
        this.imageManager = imageManager;// Store reference to image manager
    }

    /**
     * Creates the main interface of the gallery.
     *
     * Initializes all UI components and panels, sets up the card layout,
     * and prepares the basic structure for the gallery application.
     *
     * @return A JPanel containing the complete gallery UI
     */
    public JPanel createMainInterface() {
        // Initialize the layout and main panel
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Create the different views
        JPanel mainView = createMainView();         // Main gallery grid
        fullViewPanel = createFullScreenView();     // Full-screen image view

        // Add views to the main panel
        mainPanel.add(mainView, "main");            // "main" is the card name
        mainPanel.add(fullViewPanel, "full");       // "full" is the card name

        // Show the main view by default
        cardLayout.show(mainPanel, "main");

        return mainPanel;  // Return the complete interface
    }

    /**
     * Creates the main gallery view.
     *
     * This view contains:
     * - A top toolbar with Add and Albums buttons
     * - A central scrollable panel showing images in a grid
     * - A bottom scrollable bar showing album thumbnails
     *
     * @return The main gallery view panel
     */
    private JPanel createMainView() {
        // Create main container with BorderLayout
        JPanel mainView = new JPanel(new BorderLayout());
        mainView.setBackground(Color.WHITE);

        // Create top toolbar with buttons
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);

        // Create action buttons
        JButton addBtn = new JButton("Add");        // For adding images
        JButton albumBtn = new JButton("Albums");   // For managing albums

        // Arrange buttons in left panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(addBtn);

        // Add buttons to top bar
        topBar.add(buttonPanel, BorderLayout.WEST);
        topBar.add(albumBtn, BorderLayout.EAST);
        mainView.add(topBar, BorderLayout.NORTH);

        // Create center panel with image grid (2 columns, 10px spacing)
        galleryPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        galleryPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(galleryPanel);  // Make scrollable
        mainView.add(scrollPane, BorderLayout.CENTER);

        // Create album thumbnails bar at bottom
        albumBar = new JPanel();
        albumBar.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));
        albumBar.setBackground(Color.LIGHT_GRAY);

        // Make album bar scrollable horizontally
        JScrollPane albumScrollPane = new JScrollPane(
                albumBar,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,      // No vertical scrollbar
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED // Horizontal scrollbar when needed
        );
        albumScrollPane.setPreferredSize(new Dimension(0, 160));
        albumScrollPane.setBorder(null);  // Remove border
        mainView.add(albumScrollPane, BorderLayout.SOUTH);

        // Register panels with album manager
        albumManager.registerPanels(galleryPanel, albumBar);

        // Add event listeners
        addBtn.addActionListener(e -> showAddImageDialog());     // Show add image dialog
        albumBtn.addActionListener(e -> showAlbumChooser());     // Show album dialog

        return mainView;  // Return the complete main view
    }

    /**
     * Creates the full screen view for displaying and editing images.
     *
     * This view contains:
     * - A top bar with a Back button
     * - A central area showing the selected image
     * - A bottom control panel with editing options
     *
     * @return The full screen view panel
     */
    private JPanel createFullScreenView() {
        // Create main container with BorderLayout
        JPanel fullView = new JPanel(new BorderLayout());
        fullView.setBackground(Color.WHITE);

        // Create label for displaying the full-size image
        fullImageLabel = new JLabel("", SwingConstants.CENTER);
        fullView.add(fullImageLabel, BorderLayout.CENTER);

        // Create top bar with back button
        JPanel fullTopBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fullTopBar.setBackground(Color.WHITE);
        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "main"));  // Return to main view
        fullTopBar.add(backBtn);
        fullView.add(fullTopBar, BorderLayout.NORTH);

        // Create bottom controls panel
        fullControls = new JPanel();
        fullControls.setBackground(Color.WHITE);

        // Add control buttons
        JButton resizeBtn = new JButton("Resize");    // For resizing images
        JButton deleteBtn = new JButton("Delete");    // For deleting images

        // Create filter dropdown
        String[] filters = {"None", "Grayscale", "Sepia", "Invert"};
        filterComboBox = new JComboBox<>(filters);

        // Add controls to panel
        fullControls.add(resizeBtn);
        fullControls.add(deleteBtn);
        fullControls.add(filterComboBox);

        fullView.add(fullControls, BorderLayout.SOUTH);

        // Set up button actions
        deleteBtn.addActionListener(e -> deleteCurrentImage());    // Delete current image
        resizeBtn.addActionListener(e -> showResizeDialog());      // Show resize dialog
        filterComboBox.addActionListener(e -> applySelectedFilter()); // Apply selected filter

        // Register components with image manager
        imageManager.registerFullScreenComponents(fullImageLabel, filterComboBox);

        return fullView;  // Return the complete full-screen view
    }

    /**
     * Shows a dialog for selecting and adding an image to the gallery.
     *
     * The dialog presents a list of available images from the resources folder.
     * When an image is selected, it is compressed using TinyPNG and added to
     * the current album.
     */
    private void showAddImageDialog() {
        // Get list of available images
        String[] imagePaths = imageManager.getImageResourcePaths();

        // Check if any images are available
        if (imagePaths.length == 0) {
            JOptionPane.showMessageDialog(null,
                    "No images found in /resources/imageGallery",
                    "No Images Found",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Show image selection dialog
        String selected = (String) JOptionPane.showInputDialog(null,
                "Select an image:",
                "Add Image",
                JOptionPane.PLAIN_MESSAGE,
                null,
                imagePaths,
                imagePaths[0]);  // First image is default selection

        // Process selection if user didn't cancel
        if (selected != null) {
            // Compress and add image to current album
            albumManager.addImageToAlbum(app.getCurrentAlbum(), selected);

            // Return to main view and save gallery state
            cardLayout.show(mainPanel, "main");
            app.saveGallery();
        }
    }

    /**
     * Shows a dialog for creating or selecting an album.
     *
     * If the album name entered doesn't exist, a new album is created.
     * The gallery state is saved after creating a new album.
     */
    private void showAlbumChooser() {
        // Prompt for album name
        String name = JOptionPane.showInputDialog("Name of album to create/select:");

        // Process if user entered a name
        if (name != null && !name.trim().isEmpty()) {
            // Try to create album (returns false if already exists)
            boolean created = albumManager.createAlbumIfNotExists(name);
            if (created) {
                app.saveGallery();  // Save changes if new album created
            }
        }
    }

    /**
     * Deletes the currently displayed image after confirmation.
     *
     * Shows a confirmation dialog, and if confirmed, removes the image from
     * its album and updates the UI accordingly.
     */
    private void deleteCurrentImage() {
        // Get the original label for the image
        JLabel origin = (JLabel) fullImageLabel.getClientProperty("originLabel");
        if (origin != null) {
            // Ask for confirmation
            int confirm = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to delete this image?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // Delete the image from its album
                albumManager.deleteImage(origin);

                // Clear the full-screen view
                fullImageLabel.setIcon(null);
                fullImageLabel.putClientProperty("originLabel", null);

                // Save changes
                app.saveGallery();

                // Return to main view
                cardLayout.show(mainPanel, "main");
            }
        }
    }

    /**
     * Shows a dialog for resizing the currently displayed image.
     *
     * Accepts input in the format "widthxheight" (e.g., "150x150")
     * and resizes the image accordingly.
     */
    private void showResizeDialog() {
        // Prompt for new dimensions
        String input = JOptionPane.showInputDialog("New size (e.g. 150x150):");

        // Check if input matches expected format
        if (input != null && input.matches("\\d+x\\d+")) {
            // Parse width and height
            String[] parts = input.split("x");
            int width = Integer.parseInt(parts[0]);
            int height = Integer.parseInt(parts[1]);

            // Get original image label
            JLabel origin = (JLabel) fullImageLabel.getClientProperty("originLabel");
            if (origin != null) {
                // Resize the image
                imageManager.resizeImage(origin, fullImageLabel, width, height);
                app.saveGallery();  // Save changes
            }
        } else if (input != null) {
            // Show error for invalid format
            JOptionPane.showMessageDialog(null,
                    "Invalid format. Use widthxheight");
        }
    }

    /**
     * Applies the currently selected filter to the displayed image.
     *
     * Gets the filter name from the filterComboBox and applies it to
     * the image using the ImageManager.
     */
    private void applySelectedFilter() {
        // Get original image label
        JLabel origin = (JLabel) fullImageLabel.getClientProperty("originLabel");
        if (origin != null) {
            // Get selected filter from dropdown
            String filterName = (String) filterComboBox.getSelectedItem();

            // Apply the filter
            imageManager.applyFilter(origin, fullImageLabel, filterName);
            app.saveGallery();  // Save changes
        }
    }

    /**
     * Refreshes the gallery UI based on loaded data.
     *
     * Updates all albums, their contents, and thumbnails to reflect
     * the current state of the data model.
     */
    public void refreshGalleryFromData() {
        albumManager.refreshAllAlbums();  // Refresh all albums from data
    }

    /**
     * Switches to the specified view in the card layout.
     *
     * @param viewName The name of the view to display
     */
    public void showView(String viewName) {
        cardLayout.show(mainPanel, viewName);  // Show the specified card
    }
}