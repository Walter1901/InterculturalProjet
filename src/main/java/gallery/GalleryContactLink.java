package gallery;

import Address.models.Contact;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * Classe pour gérer la liaison entre les images de la galerie et les contacts.
 * Permet de sélectionner des images de la galerie pour les associer aux contacts.
 */
public class GalleryContactLink {

    /**
     * Interface pour écouter la sélection d'images
     */
    public interface ImageSelectionListener {
        void onImageSelected(ImageIcon selectedImage, String imagePath);
    }

    /**
     * Ouvre un dialogue pour sélectionner une image de la galerie
     * @param parent Le composant parent pour centrer le dialogue
     * @param listener Le listener à appeler quand une image est sélectionnée
     */
    public static void openImageSelector(Component parent, ImageSelectionListener listener) {
        // Créer le dialogue de sélection
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(parent),
                "Sélectionner une photo", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(parent);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Panel principal avec layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Titre
        JLabel titleLabel = new JLabel("Choisissez une photo de la galerie", JLabel.CENTER);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Panel pour les images avec scroll
        JPanel imagesPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        imagesPanel.setBackground(Color.WHITE);
        imagesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Charger les images disponibles
        List<ImageInfo> availableImages = loadAvailableImages();

        if (availableImages.isEmpty()) {
            JLabel noImagesLabel = new JLabel("Aucune image trouvée dans la galerie", JLabel.CENTER);
            noImagesLabel.setFont(new Font("Inter", Font.PLAIN, 14));
            noImagesLabel.setForeground(Color.GRAY);
            imagesPanel.add(noImagesLabel);
        } else {
            // Ajouter chaque image comme bouton cliquable
            for (ImageInfo imageInfo : availableImages) {
                JButton imageButton = createImageButton(imageInfo, dialog, listener);
                imagesPanel.add(imageButton);
            }
        }

        JScrollPane scrollPane = new JScrollPane(imagesPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);

        JButton cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    /**
     * Crée un bouton pour une image
     */
    private static JButton createImageButton(ImageInfo imageInfo, JDialog dialog,
                                             ImageSelectionListener listener) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(150, 150));
        button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        button.setBackground(Color.WHITE);

        // Charger et redimensionner l'image
        try {
            ImageIcon icon = new ImageIcon(imageInfo.imagePath);
            Image scaledImage = icon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaledImage));

            // Ajouter le nom de l'image comme tooltip avec info sur l'album
            button.setToolTipText(imageInfo.getDisplayText());

            // Action lors du clic
            button.addActionListener(e -> {
                if (listener != null) {
                    listener.onImageSelected(icon, imageInfo.imagePath);
                }
                dialog.dispose();
            });

            // Effet de survol
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBorder(BorderFactory.createLineBorder(new Color(0, 122, 255), 3));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
                }
            });

        } catch (Exception e) {
            button.setText("Erreur de chargement");
            button.setEnabled(false);
        }

        return button;
    }

    /**
     * Charge la liste des images disponibles depuis la galerie en utilisant le pont de données
     */
    private static List<ImageInfo> loadAvailableImages() {
        List<ImageInfo> images = new ArrayList<>();

        try {
            // Utiliser le pont de données pour récupérer les images de la galerie
            List<GalleryDataBridge.ImageData> galleryImages = GalleryDataBridge.getAllAvailableImages();

            for (GalleryDataBridge.ImageData imageData : galleryImages) {
                String path = imageData.getPath();
                String fileName = imageData.getDisplayName();

                // Convertir le chemin de ressource en chemin absolu si nécessaire
                String absolutePath = convertToAbsolutePath(path);
                images.add(new ImageInfo(fileName, absolutePath, imageData.getAlbum()));
            }

            System.out.println("Chargé " + images.size() + " images via GalleryDataBridge");

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement via GalleryDataBridge: " + e.getMessage());

            // Fallback: charger depuis les ressources directement
            loadFromResources(images);
        }

        return images;
    }

    /**
     * Convertit un chemin de ressource en chemin absolu
     */
    private static String convertToAbsolutePath(String resourcePath) {
        if (resourcePath.startsWith("/")) {
            // C'est un chemin de ressource, essayer de le convertir
            try {
                URL resourceUrl = GalleryContactLink.class.getResource(resourcePath);
                if (resourceUrl != null) {
                    return resourceUrl.getPath();
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la conversion du chemin: " + resourcePath);
            }
        }
        return resourcePath; // Retourner le chemin original si la conversion échoue
    }

    /**
     * Méthode de fallback pour charger depuis les ressources
     */
    private static void loadFromResources(List<ImageInfo> images) {
        try {
            URL resourceUrl = GalleryContactLink.class.getResource("/imageGallery");
            if (resourceUrl != null) {
                File resourceDir = new File(resourceUrl.getFile());
                if (resourceDir.exists() && resourceDir.isDirectory()) {
                    File[] imageFiles = resourceDir.listFiles((dir, name) ->
                            name.toLowerCase().endsWith(".jpg") ||
                                    name.toLowerCase().endsWith(".png") ||
                                    name.toLowerCase().endsWith(".jpeg") ||
                                    name.toLowerCase().endsWith(".gif"));

                    if (imageFiles != null) {
                        for (File imageFile : imageFiles) {
                            images.add(new ImageInfo(imageFile.getName(), imageFile.getAbsolutePath(), "default"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement fallback: " + e.getMessage());
        }
    }

    /**
     * Classe pour stocker les informations d'une image
     */
    private static class ImageInfo {
        String fileName;
        String imagePath;
        String album;

        ImageInfo(String fileName, String imagePath, String album) {
            this.fileName = fileName;
            this.imagePath = imagePath;
            this.album = album != null ? album : "default";
        }

        // Constructeur de compatibilité
        ImageInfo(String fileName, String imagePath) {
            this(fileName, imagePath, "default");
        }

        public String getDisplayText() {
            if ("default".equals(album)) {
                return fileName;
            } else {
                return fileName + " (" + album + ")";
            }
        }
    }

    /**
     * Sauvegarde la liaison entre un contact et une image
     */
    public static void saveContactImageLink(Contact contact, String imagePath) {
        // Ici vous pouvez implémenter la sauvegarde de la liaison
        // Par exemple, dans un fichier JSON ou dans les propriétés du contact
        System.out.println("Liaison sauvegardée: Contact " + contact.getName() + " -> Image " + imagePath);
    }

    /**
     * Récupère l'image associée à un contact
     */
    public static String getContactImagePath(Contact contact) {
        // Implémentation pour récupérer le chemin de l'image du contact
        // Pour l'instant, retourne null
        return null;
    }
}