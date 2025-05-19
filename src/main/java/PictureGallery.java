import gallery.PictureGalleryApp;
import javax.swing.*;

/**
 * Point d'entrée pour l'application PictureGallery.
 * Cette classe sert d'interface entre l'application smartphone et le module de galerie.
 */
public class PictureGallery {
    private PictureGalleryApp app;

    /**
     * Constructeur par défaut.
     * Initialise l'application principale.
     */
    public PictureGallery() {
        app = new PictureGalleryApp();
    }

    /**
     * Crée et retourne le panel de la galerie photo.
     * Cette méthode est appelée par phoneUtils.
     * @return Le panel principal de l'application
     */
    public JPanel createPictureGallery() {
        return app.createPictureGallery();
    }
}