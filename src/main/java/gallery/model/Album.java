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
    private String name;
    private JPanel panel;
    private JLabel thumbnail;
    private List<String> imagePaths;

    /**
     * Creates a new album with the specified name and panel.
     *
     * @param name The name of the album
     * @param panel The panel that will display the album's images
     */
    public Album(String name, JPanel panel) {
        this.name = name;
        this.panel = panel;
        this.imagePaths = new ArrayList<>();
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
        return imagePaths.remove(path);
    }

    /**
     * Checks if the album contains a specific image.
     *
     * @param path Path to the image resource to check
     * @return true if the album contains the image, false otherwise
     */
    public boolean containsImage(String path) {
        return imagePaths.contains(path);
    }

    /**
     * Gets the name of the album.
     *
     * @return The album name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the panel used to display the album's images.
     *
     * @return The album's JPanel
     */
    public JPanel getPanel() {
        return panel;
    }

    /**
     * Gets a copy of the image paths in this album.
     *
     * @return A list of image paths
     */
    public List<String> getImagePaths() {
        return new ArrayList<>(imagePaths); // Copie défensive
    }

    /**
     * Gets the thumbnail label for this album.
     *
     * @return The JLabel used as the album's thumbnail
     */
    public JLabel getThumbnail() {
        return thumbnail;
    }

    /**
     * Sets the thumbnail label for this album.
     *
     * @param thumbnail The JLabel to use as the album's thumbnail
     */
    public void setThumbnail(JLabel thumbnail) {
        this.thumbnail = thumbnail;
    }
}