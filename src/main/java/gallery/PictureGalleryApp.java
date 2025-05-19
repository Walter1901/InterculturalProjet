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
    // Gestionnaires
    private GalleryUI ui;
    private AlbumManager albumManager;
    private ImageManager imageManager;
    private StorageManager storageManager;
    private ContactLinkManager contactLinkManager;

    // État de l'application
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
        // Initialiser les différents gestionnaires dans l'ordre des dépendances
        storageManager = new StorageManager();
        imageManager = new ImageManager();
        albumManager = new AlbumManager(imageManager, storageManager);
        contactLinkManager = new ContactLinkManager();
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
        // Créer l'interface
        JPanel mainPanel = ui.createMainInterface();

        // Initialiser l'album par défaut
        albumManager.initializeDefaultAlbum();

        // Charger les données existantes
        loadGallery();

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
        GalleryData data = storageManager.loadGalleryData();
        if (data != null) {
            albumManager.setAlbumData(data.getAlbumData());
            imageManager.setFilterData(data.getImageFilters());

            // Actualiser l'interface
            ui.refreshGalleryFromData();
        }
    }

    /**
     * Saves the current state of the gallery to persistent storage.
     *
     * Collects current album data and image filter information and
     * persists it to storage for future retrieval.
     */
    public void saveGallery() {
        GalleryData data = new GalleryData();
        data.setAlbumData(albumManager.getAlbumData());
        data.setImageFilters(imageManager.getFilterData());
        storageManager.saveGalleryData(data);
    }

    /**
     * Gets the name of the currently selected album.
     *
     * @return The name of the current album
     */
    public String getCurrentAlbum() {
        return currentAlbum;
    }

    /**
     * Sets the current album.
     *
     * @param albumName The name of the album to set as current
     */
    public void setCurrentAlbum(String albumName) {
        this.currentAlbum = albumName;
    }
}