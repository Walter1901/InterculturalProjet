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
import gallery.GalleryContactLink;

/**
 * Enhanced GalleryFrame that can work with contacts
 * Replace your existing GalleryFrame.java with this code
 */
public class GalleryFrame extends JFrame {
    private static final int THUMBNAIL_SIZE = 100;
    private JPanel galleryPanel;
    private EventListenerList listenerList = new EventListenerList();
    private List<ImageIcon> images = new ArrayList<>();
    private Contact forContact = null; // If opened for a specific contact

    /**
     * Default constructor
     */
    public GalleryFrame() {
        this(null);
    }

    /**
     * Constructor for contact mode
     */
    public GalleryFrame(Contact contact) {
        this.forContact = contact;

        String title = (contact != null) ?
                "Select Image for " + contact.getDisplayName() :
                "Gallery";

        setTitle(title);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeComponents();
        loadImages();
    }

    private void initializeComponents() {
        galleryPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        galleryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String borderTitle = (forContact != null) ?
                "Click an image to link it to " + forContact.getDisplayName() :
                "Select an image";

        JScrollPane scrollPane = new JScrollPane(galleryPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder(borderTitle));

        add(scrollPane, BorderLayout.CENTER);

        // Add buttons at bottom
        JPanel buttonPanel = new JPanel(new FlowLayout());

        if (forContact != null) {
            JButton viewLinkedBtn = new JButton("View Linked Images");
            viewLinkedBtn.addActionListener(e -> viewLinkedImages());
            buttonPanel.add(viewLinkedBtn);
        }

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(cancelBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void viewLinkedImages() {
        if (forContact != null) {
            GalleryContactLink.showImagesForContact(forContact, this);
        }
    }

    private void loadImages() {
        try {
            File jsonFile = new File("gallery_data.json");
            if (jsonFile.exists()) {
                String content = new String(java.nio.file.Files.readAllBytes(jsonFile.toPath()));
                JSONObject json = new JSONObject(content);

                JSONObject albumData = json.getJSONObject("albumData");
                for (String user : albumData.keySet()) {
                    org.json.JSONArray imagesArray = albumData.getJSONArray(user);

                    for (int i = 0; i < imagesArray.length(); i++) {
                        String imagePath = imagesArray.getString(i);
                        imagePath = imagePath.replace("/imageGallery/", "");
                        File imgFile = new File(imagePath);
                        if (imgFile.exists()) {
                            BufferedImage image = ImageIO.read(imgFile);
                            if (image != null) {
                                ImageIcon thumbnail = new ImageIcon(image.getScaledInstance(
                                        THUMBNAIL_SIZE, THUMBNAIL_SIZE, Image.SCALE_SMOOTH));
                                addImage(thumbnail, imagePath);
                            }
                        }
                    }
                }
            } else {
                JLabel noImages = new JLabel("No images found", SwingConstants.CENTER);
                galleryPanel.add(noImages);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading images: " + e.getMessage());
        }
    }

    private void addImage(ImageIcon image, String imagePath) {
        images.add(image);

        JButton imageButton = new JButton();
        imageButton.setIcon(image);
        imageButton.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        imageButton.setContentAreaFilled(false);

        // Show if already linked to this contact
        if (forContact != null) {
            String fullPath = "/imageGallery/" + imagePath;
            boolean isLinked = GalleryContactLink.getImagesForContact(forContact.getDisplayName())
                    .contains(fullPath);
            if (isLinked) {
                imageButton.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
            }
        }

        imageButton.addActionListener(e -> {
            if (forContact != null) {
                // Link mode - link image to contact
                String fullPath = "/imageGallery/" + imagePath;
                GalleryContactLink.linkImageToContact(forContact.getDisplayName(), fullPath);

                JOptionPane.showMessageDialog(this,
                        "Image linked to " + forContact.getDisplayName() +
                                "\n(Image stays in gallery)");
                dispose();
            } else {
                // Normal mode - fire selection event
                fireImageSelected(image);
            }
        });

        galleryPanel.add(imageButton);
        galleryPanel.revalidate();
        galleryPanel.repaint();
    }

    /**
     * Static method to open gallery for contact linking
     */
    public static void openForContact(Contact contact, Component parent) {
        GalleryFrame frame = new GalleryFrame(contact);
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
    }

    // Keep existing interface for backward compatibility
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
        dispose();
    }

    public interface ImageSelectedListener extends java.util.EventListener {
        void imageSelected(ImageIcon image);
    }
}