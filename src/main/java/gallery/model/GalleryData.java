package gallery.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data structure for serialization of gallery state.
 *
 * This class serves as a container for all persistent gallery data,
 * including the structure of albums and their contents, as well as
 * the filters applied to individual images. It is designed to be
 * easily serialized to and deserialized from JSON format.
 */
public class GalleryData {
    // Stocke les chemins d'images par album
    private Map<String, List<String>> albumData = new HashMap<>();

    // Stocke les filtres appliqués à chaque image
    private Map<String, String> imageFilters = new HashMap<>();

    /**
     * Creates a new empty GalleryData instance.
     */
    public GalleryData() {
    }

    /**
     * Gets the album data for all albums.
     *
     * Returns a map where each key is an album name and the corresponding
     * value is a list of image paths contained in that album.
     *
     * @return Map of album names to lists of image paths
     */
    public Map<String, List<String>> getAlbumData() {
        return albumData;
    }

    /**
     * Sets the album data for all albums.
     *
     * Updates the album structure with the provided mapping of album names
     * to lists of image paths.
     *
     * @param albumData Map of album names to lists of image paths
     */
    public void setAlbumData(Map<String, List<String>> albumData) {
        this.albumData = albumData;
    }

    /**
     * Gets the image filter data.
     *
     * Returns a map where each key is an image path and the corresponding
     * value is the name of the filter applied to that image.
     *
     * @return Map of image paths to filter names
     */
    public Map<String, String> getImageFilters() {
        return imageFilters;
    }

    /**
     * Sets the image filter data.
     *
     * Updates the filter information with the provided mapping of image paths
     * to filter names.
     *
     * @param imageFilters Map of image paths to filter names
     */
    public void setImageFilters(Map<String, String> imageFilters) {
        this.imageFilters = imageFilters;
    }
}