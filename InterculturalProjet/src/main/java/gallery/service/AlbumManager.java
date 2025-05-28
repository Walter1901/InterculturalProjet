package gallery.service;

import gallery.model.Album;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the albums in the gallery application.
 */
public class AlbumManager {
    // Dependencies
    private ImageManager imageManager;       // Used for image operations
    private StorageManager storageManager;   // Used for saving data

    // Data structures
    private Map<String, Album> albums = new HashMap<>();  // Store all albums by name

    // UI references
    private JPanel galleryPanel;  // Panel where images are displayed
    private JPanel albumBar;      // Panel where album thumbnails are shown

    /**
     * Manages the albums in the gallery application.
     * <p>
     * This class is responsible for creating, updating, and deleting albums,
     * as well as managing the images within each album. It handles the organization
     * of images into logical collections and provides methods for album manipulation.
     * <p>
     * Key responsibilities:
     * - Creating and managing album data structures
     * - Adding and removing images from albums
     * - Creating visual representations of albums
     * - Handling drag-and-drop functionality between albums
     */
    public AlbumManager(ImageManager imageManager, StorageManager storageManager) {
        this.imageManager = imageManager;        // Store reference to image manager
        this.storageManager = storageManager;    // Store reference to storage manager
    }

    /**
     * Registers the UI panels used by the album manager.
     *
     * @param galleryPanel The main panel that displays images
     * @param albumBar     The panel that displays album thumbnails
     */
    public void registerPanels(JPanel galleryPanel, JPanel albumBar) {
        this.galleryPanel = galleryPanel;  // Remember the main display panel
        this.albumBar = albumBar;          // Remember the album bar panel
    }

    /**
     * Initializes the default album if it doesn't exist yet.
     * The default album is used when no specific album is selected.
     */
    public void initializeDefaultAlbum() {
        // Check if default album already exists
        if (!albums.containsKey("default")) {
            // Create new default album using the main gallery panel
            Album defaultAlbum = new Album("default", galleryPanel);
            albums.put("default", defaultAlbum);  // Store in the albums collection
        }
    }

    /**
     * Creates a new album if it doesn't already exist.
     *
     * @param name The name for the new album
     * @return true if the album was created, false if it already existed
     */
    public boolean createAlbumIfNotExists(String name) {
        // Check if the album already exists
        if (!albums.containsKey(name)) {
            // Create a panel for the new album with grid layout (2 columns with spacing)
            JPanel albumPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            albumPanel.putClientProperty("albumName", name);  // Use the 'name' parameter here
            albumPanel.setBackground(Color.WHITE);  // Set white background

            // Create the album object
            Album album = new Album(name, albumPanel);
            albums.put(name, album);  // Add to our collection of albums

            // Create a thumbnail for the album that will appear in the album bar
            JLabel thumbnail = createAlbumThumbnail(name, albumPanel);
            album.setThumbnail(thumbnail);  // Link thumbnail to album

            // Add the thumbnail to the album bar at the bottom of the screen
            albumBar.add(thumbnail);
            albumBar.revalidate();  // Update layout
            albumBar.repaint();     // Redraw component

            // Show confirmation message
            JOptionPane.showMessageDialog(null,
                    "Album '" + name + "' created successfully!",
                    "Album Created",
                    JOptionPane.INFORMATION_MESSAGE);

            return true;  // Album was created
        }
        return false;  // Album already existed
    }

    /**
     * Creates a thumbnail label for an album.
     * <p>
     * The thumbnail includes the album name and is configured to support
     * drag-and-drop operations. Click events on the thumbnail will open
     * the corresponding album view.
     *
     * @param albumName  Name of the album
     * @param albumPanel The panel containing the album's images
     * @return A JLabel serving as the album thumbnail
     */
    private JLabel createAlbumThumbnail(String albumName, JPanel albumPanel) {
        // Create a label with the album name, centered
        JLabel thumbnail = new JLabel(albumName, JLabel.CENTER);

        // Configure the appearance of the label
        thumbnail.setVerticalTextPosition(JLabel.BOTTOM);      // Text below icon
        thumbnail.setHorizontalTextPosition(JLabel.CENTER);    // Text centered
        thumbnail.setPreferredSize(new Dimension(100, 120));   // Set a fixed size
        thumbnail.setFont(new Font("Arial", Font.BOLD, 14));   // Bold text
        thumbnail.setOpaque(true);                             // Fill background
        thumbnail.setBackground(new Color(240, 240, 240));     // Light gray background
        thumbnail.setBorder(BorderFactory.createLineBorder(Color.GRAY)); // Gray border

        // Set up drag and drop handling
        thumbnail.setTransferHandler(createAlbumTransferHandler(albumName, albumPanel, thumbnail));

        // Add mouse listener to handle clicks
        thumbnail.addMouseListener(createAlbumMouseListener(albumName, albumPanel, thumbnail));

        // Set up drag source capabilities
        new DragSource().createDefaultDragGestureRecognizer(thumbnail,
                DnDConstants.ACTION_COPY, dge -> {
                    // Allow images to be dragged from this album
                    // This is a placeholder gesture recognizer
                });

        return thumbnail;  // Return the configured thumbnail
    }

    /**
     * Creates a TransferHandler for album thumbnails to support drag-and-drop.
     * <p>
     * This handler allows images to be dragged from other parts of the application
     * and dropped onto an album thumbnail, adding them to that album.
     *
     * @param albumName  Name of the target album
     * @param albumPanel Panel containing the album's images
     * @param thumbnail  The album's thumbnail label
     * @return A TransferHandler configured for the album
     */
    private TransferHandler createAlbumTransferHandler(String albumName, JPanel albumPanel, JLabel thumbnail) {
        return new TransferHandler("text") {
            public boolean canImport(TransferSupport support) {
                // Only accept string data (image paths)
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            public boolean importData(TransferSupport support) {
                try {
                    // Get the dropped image path from the transferable
                    String path = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);

                    // Use the ImageManager to create a label for the image
                    JLabel newLabel = imageManager.createImageLabel(path, albumPanel);

                    // Verify the image was loaded successfully
                    if (newLabel.getIcon() == null) {
                        System.err.println("Failed to create image label for: " + path);
                        return false;  // Image couldn't be loaded
                    }

                    // Get the album object and add the image to it
                    Album album = albums.get(albumName);
                    album.addImage(path);  // Add image path to album data

                    // Add the image to the album's panel
                    albumPanel.add(newLabel);
                    albumPanel.revalidate();  // Update layout
                    albumPanel.repaint();     // Redraw component

                    // Use the first image as album thumbnail if this is the first image
                    if (album.getImagePaths().size() == 1 && newLabel.getIcon() instanceof ImageIcon) {
                        thumbnail.setIcon((ImageIcon) newLabel.getIcon());
                    }

                    // Save the changes to persistent storage
                    storageManager.saveGalleryData(
                            new gallery.model.GalleryData()
                    );
                    return true;  // Drop was successful
                } catch (Exception ex) {
                    // Log and show any errors
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Error during drag & drop: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                return false;  // Drop failed
            }
        };
    }

    /**
     * Creates a MouseListener for album thumbnails.
     * <p>
     * When an album thumbnail is clicked, this listener either shows an existing
     * album view or creates a new one if it doesn't exist yet. The album view
     * includes navigation controls and displays all images in the album.
     *
     * @param albumName  Name of the album
     * @param albumPanel Panel containing the album's images
     * @param thumbnail  The album's thumbnail label
     * @return A MouseAdapter for handling click events
     */
    private MouseAdapter createAlbumMouseListener(String albumName, JPanel albumPanel, JLabel thumbnail) {
        return new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Find the main panel containing the CardLayout
                Component component = thumbnail;
                Container mainPanel = null;

                // Walk up the component hierarchy to find the CardLayout container
                while (component != null) {
                    Container parent = component.getParent();
                    if (parent != null && parent.getLayout() instanceof CardLayout) {
                        mainPanel = parent;
                        break;
                    }
                    component = parent;
                }

                // If we didn't find a CardLayout, we can't show the album
                if (mainPanel == null) {
                    System.err.println("Could not find main panel with CardLayout");
                    return;
                }

                // Get the CardLayout from the main panel
                CardLayout cardLayout = (CardLayout) mainPanel.getLayout();

                // Check if we already have a view for this album
                boolean viewExists = false;
                String viewName = "albumView_" + albumName;  // Naming convention for album views

                // Look through all components to find an existing view
                for (Component comp : mainPanel.getComponents()) {
                    if (comp instanceof JPanel && comp.getName() != null &&
                            comp.getName().equals(viewName)) {
                        viewExists = true;
                        cardLayout.show(mainPanel, viewName);  // Show existing view
                        break;
                    }
                }

                // If no view exists, create a new one
                if (!viewExists) {
                    // Create a panel for the album view
                    JPanel albumView = new JPanel(new BorderLayout());
                    albumView.setName(viewName);  // Set name to match our convention
                    albumView.setBackground(Color.WHITE);

                    // Create top navigation bar
                    JPanel topBar = new JPanel(new BorderLayout());
                    topBar.setBackground(Color.WHITE);

                    // Add back button on the left
                    JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    leftButtons.setBackground(Color.WHITE);
                    JButton backBtn = new JButton("Back");

                    // Store reference to mainPanel for the action listener
                    Container finalMainPanel = mainPanel;
                    backBtn.addActionListener(ev -> cardLayout.show(finalMainPanel, "main"));
                    leftButtons.add(backBtn);

                    // Add delete album button on the right
                    JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    rightButtons.setBackground(Color.WHITE);
                    JButton deleteAlbumBtn = new JButton("Delete Album");
                    deleteAlbumBtn.setBackground(new Color(255, 100, 100));  // Red background
                    deleteAlbumBtn.addActionListener(ev -> deleteAlbum(albumName, thumbnail));
                    rightButtons.add(deleteAlbumBtn);

                    // Add button panels to the top bar
                    topBar.add(leftButtons, BorderLayout.WEST);
                    topBar.add(rightButtons, BorderLayout.EAST);

                    // Add album title in the center
                    JLabel titleLabel = new JLabel("Album: " + albumName, JLabel.CENTER);
                    titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
                    topBar.add(titleLabel, BorderLayout.CENTER);

                    // Assemble the view with top bar and album panel
                    albumView.add(topBar, BorderLayout.NORTH);
                    albumView.add(albumPanel, BorderLayout.CENTER);

                    // Add the new view to the main panel and show it
                    mainPanel.add(albumView, viewName);
                    cardLayout.show(mainPanel, viewName);
                }
            }
        };
    }

    /**
     * Adds an image to the specified album.
     * <p>
     * The image is first compressed using TinyPNG, then added to the specified
     * album. If the album doesn't exist, the default album is used.
     *
     * @param albumName Name of the album to add the image to
     * @param imagePath Path to the image resource
     */
    public void addImageToAlbum(String albumName, String imagePath) {
        // Compress the image before adding it
        TinyPNGService tinyPNGService = new TinyPNGService();
        File originalFile = new File("src/main/resources" + imagePath);
        File compressedFile = tinyPNGService.compressImage(originalFile);

        // Convert absolute path to relative path
        String relativePath = "/imageGallery/" + compressedFile.getName();

        // Determine which album to use
        String targetAlbum = albumName;
        if (targetAlbum == null || targetAlbum.equals("main")) {
            targetAlbum = "default";  // Use default album if none specified
        }

        // Get or create the target album
        Album album = albums.get(targetAlbum);
        if (album == null) {
            // If album doesn't exist, use the default album
            album = albums.get("default");
            if (album == null) {
                // Create default album if needed
                album = new Album("default", galleryPanel);
                albums.put("default", album);
            }
        }

        // Create an image label and add it to the panel
        JPanel targetPanel = album.getPanel();
        JLabel imgLabel = imageManager.createImageLabel(relativePath, targetPanel);

        // If image loaded successfully, add it to the album
        if (imgLabel.getIcon() != null) {
            targetPanel.add(imgLabel);
            targetPanel.revalidate();  // Update layout
            targetPanel.repaint();     // Redraw component

            // Add the path to the album's data structure
            album.addImage(relativePath);

            // If this is the first image, use it as album thumbnail
            if (album.getThumbnail() != null && album.getImagePaths().size() == 1) {
                album.getThumbnail().setIcon((ImageIcon) imgLabel.getIcon());
            }
        }
    }

    /**
     * Deletes an image from its album.
     * <p>
     * Removes the image from the UI and from the album's data structure.
     *
     * @param imageLabel The JLabel containing the image to delete
     */
    public void deleteImage(JLabel imageLabel) {
        // Get the album panel this image belongs to
        JPanel albumPanel = (JPanel) imageLabel.getClientProperty("albumPanel");
        String resourcePath = (String) imageLabel.getClientProperty("resourcePath");

        // If no album panel specified, use the gallery panel
        if (albumPanel == null) {
            albumPanel = galleryPanel;
        }

        // Find which album contains this panel
        Album album = null;
        for (Album a : albums.values()) {
            if (a.getPanel() == albumPanel) {
                album = a;
                break;
            }
        }

        // If album not found, show error and exit
        if (album == null) {
            System.err.println("Could not find album for the image to delete");
            return;
        }

        // Remove the image from the UI
        albumPanel.remove(imageLabel);
        albumPanel.revalidate();  // Update layout
        albumPanel.repaint();     // Redraw component

        // Remove the image path from the album data
        album.removeImage(resourcePath);
    }

    /**
     * Deletes an album and all its images.
     *
     * @param albumName The name of the album to delete
     * @param thumbnail The thumbnail label for the album
     */
    public void deleteAlbum(String albumName, JLabel thumbnail) {
        // Prevent deletion of the default album
        if (albumName.equals("default")) {
            JOptionPane.showMessageDialog(null,
                    "The main album cannot be deleted.",
                    "Action impossible",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ask for confirmation before deleting
        int confirm = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to delete the album '" + albumName + "' and all its images?",
                "Confirm deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Retrieve the album
            Album album = albums.get(albumName);
            if (album == null) return;  // Album not found

            // Find and remove the album view from the UI
            Container parentContainer = thumbnail.getParent().getParent().getParent();
            if (parentContainer instanceof JPanel) {
                JPanel mainPanel = (JPanel) parentContainer;
                Component[] components = mainPanel.getComponents();

                // Look for the album view component
                for (Component comp : components) {
                    if (comp instanceof JPanel && comp.getName() != null
                            && comp.getName().equals("albumView_" + albumName)) {
                        mainPanel.remove(comp);  // Remove it
                        break;
                    }
                }

                // Return to main view
                CardLayout cardLayout = (CardLayout) mainPanel.getLayout();
                cardLayout.show(mainPanel, "main");
            }

            // Remove album from data structures
            albums.remove(albumName);

            // Remove thumbnail from album bar
            albumBar.remove(thumbnail);
            albumBar.revalidate();  // Update layout
            albumBar.repaint();     // Redraw component

            // Save changes to persist the album deletion to the JSON file
            gallery.model.GalleryData data = new gallery.model.GalleryData();
            data.setAlbumData(getAlbumData());
            data.setImageFilters(imageManager.getFilterData());
            storageManager.saveGalleryData(data);


            // Show confirmation message
            JOptionPane.showMessageDialog(null,
                    "Album '" + albumName + "' deleted successfully!",
                    "Album Deleted",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Refreshes all albums with current data.
     * <p>
     * Clears and rebuilds all album panels and thumbnails based on the
     * current album data. This ensures the UI reflects the current state
     * of the data model.
     */
    public void refreshAllAlbums() {
            // Clear existing panels
            for (Album album : albums.values()) {
                album.getPanel().removeAll();
            }

            // Clear the album bar
            albumBar.removeAll();

            // Keep track of all labels by path for filter application
            Map<String, JLabel> allLabels = new HashMap<>();

            // Recreate thumbnails and reload images
            for (Map.Entry<String, Album> entry : albums.entrySet()) {
                String albumName = entry.getKey();  // This defines albumName in this scope
                Album album = entry.getValue();

                // Set album name on panel for filter detection
                album.getPanel().putClientProperty("albumName", albumName);  // Now albumName is defined

                // Recreate thumbnail if needed
                if (album.getThumbnail() == null && !albumName.equals("default")) {
                    JLabel thumbnail = createAlbumThumbnail(albumName, album.getPanel());
                    album.setThumbnail(thumbnail);
                    albumBar.add(thumbnail);
                }

                // Load images into panel
                for (String path : album.getImagePaths()) {
                    JLabel imageLabel = imageManager.createImageLabel(path, album.getPanel());
                    if (imageLabel.getIcon() != null) {
                        album.getPanel().add(imageLabel);

                        // Store label for filter application
                        allLabels.put(path, imageLabel);

                        // Set album thumbnail if needed
                        if (album.getThumbnail() != null && album.getThumbnail().getIcon() == null
                                && imageLabel.getIcon() instanceof ImageIcon) {
                            album.getThumbnail().setIcon((ImageIcon) imageLabel.getIcon());
                        }
                    }
                }

                // Update the UI
                album.getPanel().revalidate();
                album.getPanel().repaint();
            }

            // Update the album bar
            albumBar.revalidate();
            albumBar.repaint();

            // Apply saved filters after all images are loaded
            imageManager.applyPersistedFilters(allLabels);
        }

    /**
     * Gets the album data for persistence.
     *
     * @return A map of album names to lists of image paths
     */
    public Map<String, List<String>> getAlbumData() {
        Map<String, List<String>> result = new HashMap<>();

        // For each album, collect its image paths
        for (Map.Entry<String, Album> entry : albums.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getImagePaths());
        }

        return result;  // Return map of album names to image path lists
    }

    /**
     * Sets the album data from persistent storage.
     *
     * @param albumData A map of album names to lists of image paths
     */
    public void setAlbumData(Map<String, List<String>> albumData) {
        // Create or update albums from the loaded data
        for (Map.Entry<String, List<String>> entry : albumData.entrySet()) {
            String albumName = entry.getKey();
            List<String> paths = entry.getValue();

            // Get existing album or create new one
            Album album = albums.get(albumName);

            if (album == null) {
                // Create a new panel for the album
                JPanel newPanel = new JPanel(new GridLayout(0, 2, 10, 10));
                newPanel.setBackground(Color.WHITE);

                // Create the album
                album = new Album(albumName, newPanel);
                albums.put(albumName, album);

                // Thumbnail will be created during refresh
            }

            // Add image paths to the album
            for (String path : paths) {
                album.addImage(path);
            }
        }
    }
}