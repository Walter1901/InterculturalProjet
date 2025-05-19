import gallery.PictureGalleryApp;
import javax.swing.*;


public class PictureGallery {
    private PictureGalleryApp app;


    public PictureGallery() {
        app = new PictureGalleryApp();
    }


    public JPanel createPictureGallery() {
        return app.createPictureGallery();
    }
}