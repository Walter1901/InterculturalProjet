package Address.models;

import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryFrame extends JFrame {
    private static final int THUMBNAIL_SIZE = 100;
    private JPanel galleryPanel;
    private EventListenerList listenerList = new EventListenerList();
    private List<ImageIcon> images = new ArrayList<>();

    public GalleryFrame() {
        setTitle("Gallery");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeComponents();
        loadImages();
    }

    private void initializeComponents() {
        galleryPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        galleryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(galleryPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Select an image"));

        add(scrollPane);
    }

    private void loadImages() {
        try {
            // 1. Lire le fichier JSON
            File jsonFile = new File("gallery_data.json");
            if (jsonFile.exists()) {
                String content = new String(java.nio.file.Files.readAllBytes(jsonFile.toPath()));
                JSONObject json = new JSONObject(content);
                
                // 2. Parcourir les albums
                JSONObject albumData = json.getJSONObject("albumData");
                for (String user : albumData.keySet()) {
                    org.json.JSONArray imagesArray = albumData.getJSONArray(user);
                    
                    // 3. Parcourir les images de chaque album
                    for (int i = 0; i < imagesArray.length(); i++) {
                        String imagePath = imagesArray.getString(i);
                        // Supprimer le préfixe /imageGallery/
                        imagePath = imagePath.replace("/imageGallery/", "");
                        File imgFile = new File(imagePath);
                        if (imgFile.exists()) {
                            // Charger l'image et l'ajouter
                            BufferedImage image = ImageIO.read(imgFile);
                            if (image != null) {
                                // Créer une version réduite pour l'aperçu
                                ImageIcon thumbnail = new ImageIcon(image.getScaledInstance(
                                    THUMBNAIL_SIZE, THUMBNAIL_SIZE, Image.SCALE_SMOOTH));
                                addImage(thumbnail);
                            }
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Fichier gallery_data.json introuvable.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des images: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addImage(ImageIcon image) {
        images.add(image);
        
        // Créer un bouton pour l'image
        JButton imageButton = new JButton();
        imageButton.setIcon(image);
        imageButton.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        imageButton.setContentAreaFilled(false);
        imageButton.addActionListener(e -> fireImageSelected(image));
        
        galleryPanel.add(imageButton);
        galleryPanel.revalidate();
        galleryPanel.repaint();
    }

    public void addImageSelectedListener(ImageSelectedListener listener) {
        listenerList.add(ImageSelectedListener.class, listener);
    }

    protected void fireImageSelected(ImageIcon image) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ImageSelectedListener.class) {
                ((ImageSelectedListener) listeners[i + 1]).imageSelected(image);
            }
        }
        dispose(); // Fermer la fenêtre après sélection
    }

    public interface ImageSelectedListener extends java.util.EventListener {
        void imageSelected(ImageIcon image);
    }
}
