package gallery.model;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un album de photos dans la galerie.
 * Contient les informations sur l'album et ses images.
 */
public class Album {
    private String name;
    private JPanel panel;
    private JLabel thumbnail;
    private List<String> imagePaths;

    /**
     * Constructeur pour un nouvel album
     * @param name Nom de l'album
     * @param panel Panel qui contiendra les images de l'album
     */
    public Album(String name, JPanel panel) {
        this.name = name;
        this.panel = panel;
        this.imagePaths = new ArrayList<>();
    }

    /**
     * Ajoute une image à l'album
     * @param path Chemin de l'image à ajouter
     */
    public void addImage(String path) {
        if (!imagePaths.contains(path)) {
            imagePaths.add(path);
        }
    }

    /**
     * Supprime une image de l'album
     * @param path Chemin de l'image à supprimer
     * @return true si l'image a été supprimée, false sinon
     */
    public boolean removeImage(String path) {
        return imagePaths.remove(path);
    }

    /**
     * Vérifie si l'album contient une image
     * @param path Chemin de l'image à vérifier
     * @return true si l'album contient l'image, false sinon
     */
    public boolean containsImage(String path) {
        return imagePaths.contains(path);
    }

    // Getters et setters

    public String getName() {
        return name;
    }

    public JPanel getPanel() {
        return panel;
    }

    public List<String> getImagePaths() {
        return new ArrayList<>(imagePaths); // Copie défensive
    }

    public JLabel getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(JLabel thumbnail) {
        this.thumbnail = thumbnail;
    }
}