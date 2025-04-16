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

        // Zone de galerie au centre à changer par des images
        JPanel galleryPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        galleryPanel.setBackground(phoneUtils.backgroundColor);
        galleryPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        for (int i = 1; i <= 6; i++) {
            JLabel imgLabel = new JLabel("📷", SwingConstants.CENTER);
            imgLabel.setFont(new Font("Inter", Font.PLAIN, 40));
            imgLabel.setForeground(Color.LIGHT_GRAY);
            imgLabel.setOpaque(true);
            imgLabel.setBackground(new Color(44, 44, 46));
            imgLabel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1, true));
            galleryPanel.add(imgLabel);
        }

        pictureGalleryApp.add(galleryPanel, BorderLayout.CENTER);

        return pictureGalleryApp;
    }


}
