package gallery.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Structure de données pour stocker et sérialiser les données de la galerie.
 * Utilisée pour sauvegarder et charger l'état de l'application.
 */
public class GalleryData {
    // Stocke les chemins d'images par album
    private Map<String, List<String>> albumData = new HashMap<>();

    // Stocke les filtres appliqués à chaque image
    private Map<String, String> imageFilters = new HashMap<>();

    /**
     * Constructeur par défaut
     */
    public GalleryData() {
    }

    /**
     * Récupère les données d'album
     */
    public Map<String, List<String>> getAlbumData() {
        return albumData;
    }

    /**
     * Définit les données d'album
     */
    public void setAlbumData(Map<String, List<String>> albumData) {
        this.albumData = albumData;
    }

    /**
     * Récupère les filtres appliqués aux images
     */
    public Map<String, String> getImageFilters() {
        return imageFilters;
    }

    /**
     * Définit les filtres appliqués aux images
     */
    public void setImageFilters(Map<String, String> imageFilters) {
        this.imageFilters = imageFilters;
    }
}