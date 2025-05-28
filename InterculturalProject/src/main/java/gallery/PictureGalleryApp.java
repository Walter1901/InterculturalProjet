package gallery;

import gallery.model.GalleryData;
import gallery.service.AlbumManager;
import gallery.service.ContactLinkManager;
import gallery.service.ImageManager;
import gallery.service.StorageManager;
import gallery.ui.GalleryUI;

import javax.swing.*;

/**
 * Main application class for the photo gallery.
 *
 * This class acts as the central coordinator for the gallery application,
 * initializing and connecting all the different components. It manages the
 * application lifecycle, handles data loading and saving, and maintains
 * the current application state.
 *
 * The application follows a component-based architecture where each component
 * has a specific responsibility:
 * - StorageManager - Handles data persistence
 * - ImageManager - Manages image processing
 * - AlbumManager - Organizes images into albums
 * - ContactLinkManager - Links images to contacts
 * - GalleryUI - Provides the user interface
 */
public class PictureGalleryApp {
    // Managers for different aspects of the application
    private GalleryUI ui;                        // Handles all user interface components and interactions
    private AlbumManager albumManager;           // Manages album creation, deletion, and organization
    private ImageManager imageManager;           // Handles image operations like loading, filtering, and resizing
    private StorageManager storageManager;       // Manages saving and loading data from disk
    private ContactLinkManager contactLinkManager;// Links images to contacts in the address book

    // Application state variables
    private String currentAlbum = "default";

    /**
     * Constructs a new PictureGalleryApp.
     *
     * Initializes all components of the application in the correct order,
     * ensuring dependencies are properly set up. Components are initialized
     * in the following order:
     * 1. StorageManager - No dependencies
     * 2. ImageManager - No dependencies
     * 3. AlbumManager - Depends on ImageManager and StorageManager
     * 4. ContactLinkManager - No dependencies
     * 5. GalleryUI - Depends on all other components
     */
    public PictureGalleryApp() {
        // Initialize the components in the correct dependency order
        storageManager = new StorageManager();    // Create storage manager first (no dependencies)
        imageManager = new ImageManager();
        imageManager.setStorageManager(storageManager);  // Connect them

        // Create album manager with its dependencies
        albumManager = new AlbumManager(imageManager, storageManager);

        // Create contact link manager (no dependencies)
        contactLinkManager = new ContactLinkManager();

        // Finally create UI with all its dependencies
        ui = new GalleryUI(this, albumManager, imageManager);
    }

    /**
     * Creates and initializes the gallery user interface.
     *
     * This method:
     * 1. Creates the main user interface
     * 2. Initializes the default album
     * 3. Loads any existing gallery data from storage
     *
     * @return A JPanel containing the complete gallery interface
     */
    public JPanel createPictureGallery() {
        // Create the main user interface panel
        JPanel mainPanel = ui.createMainInterface();

        // Set up the default album for when the app starts
        albumManager.initializeDefaultAlbum();

        // Load any previously saved gallery data
        loadGallery();

        // Return the fully configured interface
        return mainPanel;
    }

    /**
     * Loads gallery data from persistent storage.
     *
     * Retrieves saved album structure and image filter information,
     * then updates the UI to reflect the loaded data. If no data
     * is found, the application starts with an empty gallery.
     */
    private void loadGallery() {
        // Attempt to load saved data from disk
        GalleryData data = storageManager.loadGalleryData();

        // If data was found, populate the gallery with it
        if (data != null) {
            // Set album structure from loaded data
            albumManager.setAlbumData(data.getAlbumData());

            // Apply saved image filters
            imageManager.setFilterData(data.getImageFilters());

            // Update the UI to reflect loaded data
            ui.refreshGalleryFromData();
        }
        // If no data was found, the gallery starts empty
    }

    /**
     * Saves the current state of the gallery to persistent storage.
     *
     * Collects current album data and image filter information and
     * persists it to storage for future retrieval.
     */
    public void saveGallery() {
        // Create a container for all gallery data
        GalleryData data = new GalleryData();

        // Collect album structure
        data.setAlbumData(albumManager.getAlbumData());

        // Collect image filter information
        data.setImageFilters(imageManager.getFilterData());

        // Save everything to disk
        storageManager.saveGalleryData(data);
    }

    /**
     * Gets the name of the currently selected album.
     *
     * @return The name of the current album
     */
    public String getCurrentAlbum() {
        return currentAlbum;  // Return the name of the album currently being viewed
    }

    /**
     * Sets the current album.
     *
     * @param albumName The name of the album to set as current
     */
    public void setCurrentAlbum(String albumName) {
        this.currentAlbum = albumName;  // Update which album is currently being viewed
    }
}