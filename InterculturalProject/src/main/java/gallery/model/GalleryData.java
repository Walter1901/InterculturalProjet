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
    // Stores image paths by album name
    private Map<String, List<String>> albumData = new HashMap<>();

    // Stores applied filters by image path or by album
    // The values can be either a String (filter name) or a Map<String, String> (image path to filter)
    private Map<String, Object> imageFilters = new HashMap<>();

    /**
     * Creates a new empty GalleryData instance.
     */
    public GalleryData() {
        // Empty constructor, fields initialized with default values
    }

    /**
     * Gets the album data for all albums.
     *
     * @return Map of album names to lists of image paths
     */
    public Map<String, List<String>> getAlbumData() {
        return albumData;
    }

    /**
     * Sets the album data for all albums.
     *
     * @param albumData Map of album names to lists of image paths
     */
    public void setAlbumData(Map<String, List<String>> albumData) {
        this.albumData = albumData;
    }

    /**
     * Gets the image filter data.
     *
     * @return Map of image paths or album names to filter information
     */
    public Map<String, Object> getImageFilters() {
        return imageFilters;
    }

    /**
     * Sets the image filter data.
     *
     * @param imageFilters Map of image paths or album names to filter information
     */
    public void setImageFilters(Map<String, Object> imageFilters) {
        this.imageFilters = imageFilters;
    }

    /**
     * Adds a filter for an image in a specific album.
     *
     * @param albumName The name of the album
     * @param imagePath The path of the image
     * @param filterName The name of the filter to apply
     */
    @SuppressWarnings("unchecked")
    public void addFilterToAlbum(String albumName, String imagePath, String filterName) {
        if (albumName == null || imagePath == null || filterName == null || "None".equals(filterName)) {
            return;
        }

        // Create album entry if it doesn't exist
        if (!imageFilters.containsKey(albumName)) {
            imageFilters.put(albumName, new HashMap<String, String>());
        }

        // Get the album's filter map and add the filter
        Object filterObj = imageFilters.get(albumName);
        if (filterObj instanceof Map) {
            Map<String, String> albumFilters = (Map<String, String>) filterObj;
            albumFilters.put(imagePath, filterName);
        }
    }

    /**
     * Adds a filter for an image not in a specific album.
     *
     * @param imagePath The path of the image
     * @param filterName The name of the filter to apply
     */
    public void addFilter(String imagePath, String filterName) {
        if (imagePath == null || filterName == null || "None".equals(filterName)) {
            return;
        }

        imageFilters.put(imagePath, filterName);
    }

    /**
     * Removes a filter from an image in a specific album.
     *
     * @param albumName The name of the album
     * @param imagePath The path of the image
     */
    @SuppressWarnings("unchecked")
    public void removeFilterFromAlbum(String albumName, String imagePath) {
        if (albumName == null || imagePath == null) {
            return;
        }

        Object filterObj = imageFilters.get(albumName);
        if (filterObj instanceof Map) {
            Map<String, String> albumFilters = (Map<String, String>) filterObj;
            albumFilters.remove(imagePath);

            // If the album has no more filters, remove it
            if (albumFilters.isEmpty()) {
                imageFilters.remove(albumName);
            }
        }
    }

    /**
     * Removes a filter from an image not in a specific album.
     *
     * @param imagePath The path of the image
     */
    public void removeFilter(String imagePath) {
        if (imagePath == null) {
            return;
        }

        imageFilters.remove(imagePath);
    }

    /**
     * Gets the filter for an image in a specific album.
     *
     * @param albumName The name of the album
     * @param imagePath The path of the image
     * @return The filter name, or null if no filter is applied
     */
    @SuppressWarnings("unchecked")
    public String getFilterFromAlbum(String albumName, String imagePath) {
        if (albumName == null || imagePath == null) {
            return null;
        }

        Object filterObj = imageFilters.get(albumName);
        if (filterObj instanceof Map) {
            Map<String, String> albumFilters = (Map<String, String>) filterObj;
            return albumFilters.get(imagePath);
        }

        return null;
    }

    /**
     * Gets the filter for an image not in a specific album.
     *
     * @param imagePath The path of the image
     * @return The filter name, or null if no filter is applied
     */
    public String getFilter(String imagePath) {
        if (imagePath == null) {
            return null;
        }

        Object filterObj = imageFilters.get(imagePath);
        if (filterObj instanceof String) {
            return (String) filterObj;
        }

        return null;
    }
}