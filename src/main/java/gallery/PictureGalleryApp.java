package gallery;

import gallery.model.GalleryData;
import gallery.service.AlbumManager;
import gallery.service.ContactLinkManager;
import gallery.service.ImageManager;
import gallery.service.StorageManager;
import gallery.ui.GalleryUI;

import javax.swing.*;
import java.awt.*;

/**
 * Application principale de la galerie photo.
 * Coordonne les différents composants et gère le cycle de vie de l'application.
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
     * Constructeur - initialise tous les composants de l'application
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
     * Crée et initialise l'interface utilisateur de la galerie
     * @return Le panel principal de l'application
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
     * Charge les données de la galerie depuis le stockage
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
     * Sauvegarde l'état actuel de la galerie
     */
    public void saveGallery() {
        GalleryData data = new GalleryData();
        data.setAlbumData(albumManager.getAlbumData());
        data.setImageFilters(imageManager.getFilterData());
        storageManager.saveGalleryData(data);
    }

    // Getters et setters pour l'état de l'application

    public String getCurrentAlbum() {
        return currentAlbum;
    }

    public void setCurrentAlbum(String albumName) {
        this.currentAlbum = albumName;
    }
}