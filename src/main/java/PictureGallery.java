import javax.swing.*;
import java.awt.*;

public class PictureGallery {

    public static JPanel createPictureGallery() {

        JPanel pictureGalleryApp = new JPanel();
        pictureGalleryApp.setBackground(phoneUtils.backgroundColor);
        JLabel label = new JLabel("Picture Gallery App", SwingConstants.CENTER);
        label.setFont(new Font("Inter", Font.BOLD, 20));
        label.setForeground(phoneUtils.textColor);
        pictureGalleryApp.add(label, BorderLayout.CENTER);

        return pictureGalleryApp;
    }


}
