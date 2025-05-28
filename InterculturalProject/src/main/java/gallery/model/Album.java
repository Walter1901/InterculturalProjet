package gallery.model;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an album in the gallery.
 *
 * An album is a collection of images with a name, a panel for displaying
 * those images, and a thumbnail for representing the album in the UI.
 * It manages the list of image paths contained in the album and provides
 * methods for adding, removing, and checking for images.
 */
public class Album {
    private String name;          // Name of the album
    private JPanel panel;         // Panel where images are displayed
    private JLabel thumbnail;     // Thumbnail representation in UI
    private List<String> imagePaths; // Paths to images in the album

    /**
     * Creates a new album with the specified name and panel.
     *
     * @param name The name of the album
     * @param panel The panel that will display the album's images
     */
    public Album(String name, JPanel panel) {
        this.name = name;                      // Store album name
        this.panel = panel;                    // Store display panel
        this.imagePaths = new ArrayList<>();   // Initialize empty path list
    }

    /**
     * Adds an image to the album.
     *
     * If the image is not already in the album, adds its path to the
     * collection of images associated with this album.
     *
     * @param path Path to the image resource
     */
    public void addImage(String path) {
        // Add image path if not already in album
        if (!imagePaths.contains(path)) {
            imagePaths.add(path);
        }
    }

    /**
     * Removes an image from the album.
     *
     * Removes the specified image path from the album's collection.
     *
     * @param path Path to the image resource to remove
     * @return true if the image was found and removed, false otherwise
     */
    public boolean removeImage(String path) {
        // Remove the path and return true if found
        return imagePaths.remove(path);
    }

    /**
     * Checks if the album contains a specific image.
     *
     * @param path Path to the image resource to check
     * @return true if the album contains the image, false otherwise
     */
    public boolean containsImage(String path) {
        // Return true if path is in the list
        return imagePaths.contains(path);
    }

    /**
     * Gets the name of the album.
     *
     * @return The album name
     */
    public String getName() {
        return name;  // Return album name
    }

    /**
     * Gets the panel used to display the album's images.
     *
     * @return The album's JPanel
     */
    public JPanel getPanel() {
        return panel;  // Return display panel
    }

    /**
     * Gets a copy of the image paths in this album.
     *
     * @return A list of image paths
     */
    public List<String> getImagePaths() {
        return new ArrayList<>(imagePaths);  // Return defensive copy
    }

    /**
     * Gets the thumbnail label for this album.
     *
     * @return The JLabel used as the album's thumbnail
     */
    public JLabel getThumbnail() {
        return thumbnail;  // Return thumbnail label
    }

    /**
     * Sets the thumbnail label for this album.
     *
     * @param thumbnail The JLabel to use as the album's thumbnail
     */
    public void setThumbnail(JLabel thumbnail) {
        this.thumbnail = thumbnail;  // Store thumbnail label
    }
}