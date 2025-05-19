package gallery.service;

import gallery.model.Album;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;


public class AlbumManager {
    private ImageManager imageManager;
    private StorageManager storageManager;

    // Stockage des albums
    private Map<String, Album> albums = new HashMap<>();

    // Références aux panneaux d'interface
    private JPanel galleryPanel;
    private JPanel albumBar;

    /**
     * Manages the albums in the gallery application.
     *
     * This class is responsible for creating, updating, and deleting albums,
     * as well as managing the images within each album. It handles the organization
     * of images into logical collections and provides methods for album manipulation.
     *
     * Key responsibilities:
     * - Creating and managing album data structures
     * - Adding and removing images from albums
     * - Creating visual representations of albums
     * - Handling drag-and-drop functionality between albums
     */
    public AlbumManager(ImageManager imageManager, StorageManager storageManager) {
        this.imageManager = imageManager;
        this.storageManager = storageManager;
    }

    /**
     * Registers the UI panels used by the album manager.
     *
     * @param galleryPanel The main panel that displays images
     * @param albumBar The panel that displays album thumbnails
     */
    public void registerPanels(JPanel galleryPanel, JPanel albumBar) {
        this.galleryPanel = galleryPanel;
        this.albumBar = albumBar;
    }

    /**
     * Initializes the default album if it doesn't exist yet.
     * The default album is used when no specific album is selected.
     */
    public void initializeDefaultAlbum() {
        if (!albums.containsKey("default")) {
            Album defaultAlbum = new Album("default", galleryPanel);
            albums.put("default", defaultAlbum);
        }
    }

    /**
     * Creates a new album if it doesn't already exist.
     *
     * @param name The name for the new album
     * @return true if the album was created, false if it already existed
     */
    public boolean createAlbumIfNotExists(String name) {
        if (!albums.containsKey(name)) {
            // Créer le panel pour l'album
            JPanel albumPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            albumPanel.setBackground(Color.WHITE);

            // Créer l'album
            Album album = new Album(name, albumPanel);
            albums.put(name, album);

            // Créer la vignette de l'album
            JLabel thumbnail = createAlbumThumbnail(name, albumPanel);
            album.setThumbnail(thumbnail);

            // Ajouter la vignette à la barre d'albums
            albumBar.add(thumbnail);
            albumBar.revalidate();
            albumBar.repaint();

            JOptionPane.showMessageDialog(null,
                    "Album '" + name + "' created successfully!",
                    "Album Created",
                    JOptionPane.INFORMATION_MESSAGE);

            return true;
        }
        return false;
    }

    /**
     * Creates a thumbnail label for an album.
     *
     * The thumbnail includes the album name and is configured to support
     * drag-and-drop operations. Click events on the thumbnail will open
     * the corresponding album view.
     *
     * @param albumName Name of the album
     * @param albumPanel The panel containing the album's images
     * @return A JLabel serving as the album thumbnail
     */
    private JLabel createAlbumThumbnail(String albumName, JPanel albumPanel) {
        // Créer la vignette avec le nom de l'album
        JLabel thumbnail = new JLabel(albumName, JLabel.CENTER);
        thumbnail.setVerticalTextPosition(JLabel.BOTTOM);
        thumbnail.setHorizontalTextPosition(JLabel.CENTER);
        thumbnail.setPreferredSize(new Dimension(100, 120));
        thumbnail.setFont(new Font("Arial", Font.BOLD, 14));
        thumbnail.setOpaque(true);
        thumbnail.setBackground(new Color(240, 240, 240));
        thumbnail.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Configurer le gestionnaire de glisser-déposer
        thumbnail.setTransferHandler(createAlbumTransferHandler(albumName, albumPanel, thumbnail));

        // Ajouter un écouteur de clic pour ouvrir l'album
        thumbnail.addMouseListener(createAlbumMouseListener(albumName, albumPanel, thumbnail));

        // Configurer le support de drag-and-drop
        new DragSource().createDefaultDragGestureRecognizer(thumbnail,
                DnDConstants.ACTION_COPY, dge -> {
                    // Permettre aux images d'être glissées depuis cet album
                });

        return thumbnail;
    }

    /**
     * Creates a TransferHandler for album thumbnails to support drag-and-drop.
     *
     * This handler allows images to be dragged from other parts of the application
     * and dropped onto an album thumbnail, adding them to that album.
     *
     * @param albumName Name of the target album
     * @param albumPanel Panel containing the album's images
     * @param thumbnail The album's thumbnail label
     * @return A TransferHandler configured for the album
     */
    private TransferHandler createAlbumTransferHandler(String albumName, JPanel albumPanel, JLabel thumbnail) {
        return new TransferHandler("text") {
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            public boolean importData(TransferSupport support) {
                try {
                    // Récupérer le chemin de l'image glissée
                    String path = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);

                    // Créer une étiquette pour l'image
                    JLabel newLabel = imageManager.createImageLabel(path, albumPanel);

                    // Vérifier que l'étiquette a été créée avec succès
                    if (newLabel.getIcon() == null) {
                        System.err.println("Failed to create image label for: " + path);
                        return false;
                    }

                    // Ajouter l'image à l'album
                    Album album = albums.get(albumName);
                    album.addImage(path);
                    albumPanel.add(newLabel);
                    albumPanel.revalidate();
                    albumPanel.repaint();

                    // Utiliser la première image comme vignette d'album si nécessaire
                    if (album.getImagePaths().size() == 1 && newLabel.getIcon() instanceof ImageIcon) {
                        thumbnail.setIcon((ImageIcon) newLabel.getIcon());
                    }

                    // Sauvegarder les modifications
                    storageManager.saveGalleryData(
                            new gallery.model.GalleryData()
                    );
                    return true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Error during drag & drop: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                return false;
            }
        };
    }

    /**
     * Creates a MouseListener for album thumbnails.
     *
     * When an album thumbnail is clicked, this listener either shows an existing
     * album view or creates a new one if it doesn't exist yet. The album view
     * includes navigation controls and displays all images in the album.
     *
     * @param albumName Name of the album
     * @param albumPanel Panel containing the album's images
     * @param thumbnail The album's thumbnail label
     * @return A MouseAdapter for handling click events
     */
    private MouseAdapter createAlbumMouseListener(String albumName, JPanel albumPanel, JLabel thumbnail) {
        return new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Chercher le panel principal contenant le CardLayout
                Component component = thumbnail;
                Container mainPanel = null;

                // Rechercher le conteneur principal dans la hiérarchie des composants
                while (component != null) {
                    Container parent = component.getParent();
                    if (parent != null && parent.getLayout() instanceof CardLayout) {
                        mainPanel = parent;
                        break;
                    }
                    component = parent;
                }

                if (mainPanel == null) {
                    System.err.println("Could not find main panel with CardLayout");
                    return;
                }

                CardLayout cardLayout = (CardLayout) mainPanel.getLayout();

                // Chercher une vue d'album existante ou en créer une nouvelle
                boolean viewExists = false;
                String viewName = "albumView_" + albumName;

                for (Component comp : mainPanel.getComponents()) {
                    if (comp instanceof JPanel && comp.getName() != null &&
                            comp.getName().equals(viewName)) {
                        viewExists = true;
                        cardLayout.show(mainPanel, viewName); // Utiliser le nom de la vue comme identifiant
                        break;
                    }
                }

                // Si la vue n'existe pas, la créer
                if (!viewExists) {
                    JPanel albumView = new JPanel(new BorderLayout());
                    albumView.setName(viewName);
                    albumView.setBackground(Color.WHITE);

                    // Barre de navigation supérieure
                    JPanel topBar = new JPanel(new BorderLayout());
                    topBar.setBackground(Color.WHITE);

                    // Bouton de retour
                    JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    leftButtons.setBackground(Color.WHITE);
                    JButton backBtn = new JButton("Back");
                    Container finalMainPanel = mainPanel;
                    backBtn.addActionListener(ev -> cardLayout.show(finalMainPanel, "main"));
                    leftButtons.add(backBtn);

                    // Bouton de suppression d'album
                    JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    rightButtons.setBackground(Color.WHITE);
                    JButton deleteAlbumBtn = new JButton("Delete Album");
                    deleteAlbumBtn.setBackground(new Color(255, 100, 100));
                    deleteAlbumBtn.addActionListener(ev -> deleteAlbum(albumName, thumbnail));
                    rightButtons.add(deleteAlbumBtn);

                    // Ajouter les boutons à la barre
                    topBar.add(leftButtons, BorderLayout.WEST);
                    topBar.add(rightButtons, BorderLayout.EAST);

                    // Titre de l'album
                    JLabel titleLabel = new JLabel("Album: " + albumName, JLabel.CENTER);
                    titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
                    topBar.add(titleLabel, BorderLayout.CENTER);

                    // Ajouter les composants à la vue
                    albumView.add(topBar, BorderLayout.NORTH);
                    albumView.add(albumPanel, BorderLayout.CENTER);

                    // Ajouter la vue au panel principal
                    mainPanel.add(albumView, viewName); // Attention: utiliser viewName comme identifiant
                    cardLayout.show(mainPanel, viewName);
                }
            }
        };
    }

    /**
     * Adds an image to the specified album.
     *
     * The image is first compressed using TinyPNG, then added to the specified
     * album. If the album doesn't exist, the default album is used.
     *
     * @param albumName Name of the album to add the image to
     * @param imagePath Path to the image resource
     */
    public void addImageToAlbum(String albumName, String imagePath) {
        // Compresser l'image avant de l'ajouter
        TinyPNGService tinyPNGService = new TinyPNGService();
        File originalFile = new File("src/main/resources" + imagePath);
        File compressedFile = tinyPNGService.compressImage(originalFile);

        // Convertir le chemin absolu en chemin relatif
        String relativePath = "/imageGallery/" + compressedFile.getName();

        // Récupérer ou créer l'album cible
        String targetAlbum = albumName;
        if (targetAlbum == null || targetAlbum.equals("main")) {
            targetAlbum = "default";
        }

        Album album = albums.get(targetAlbum);
        if (album == null) {
            // Si l'album n'existe pas, utiliser l'album par défaut
            album = albums.get("default");
            if (album == null) {
                // Créer l'album par défaut si nécessaire
                album = new Album("default", galleryPanel);
                albums.put("default", album);
            }
        }

        // Créer l'étiquette pour l'image et l'ajouter au panel
        JPanel targetPanel = album.getPanel();
        JLabel imgLabel = imageManager.createImageLabel(relativePath, targetPanel);

        if (imgLabel.getIcon() != null) {
            targetPanel.add(imgLabel);
            targetPanel.revalidate();
            targetPanel.repaint();

            // Ajouter le chemin à l'album
            album.addImage(relativePath);

            // Si c'est la première image, l'utiliser comme vignette
            if (album.getThumbnail() != null && album.getImagePaths().size() == 1) {
                album.getThumbnail().setIcon((ImageIcon) imgLabel.getIcon());
            }
        }
    }

    /**
     * Deletes an image from its album.
     *
     * Removes the image from the UI and from the album's data structure.
     *
     * @param imageLabel The JLabel containing the image to delete
     */
    public void deleteImage(JLabel imageLabel) {
        JPanel albumPanel = (JPanel) imageLabel.getClientProperty("albumPanel");
        String resourcePath = (String) imageLabel.getClientProperty("resourcePath");

        // Si le panel d'album n'est pas spécifié, utiliser le panel de la galerie
        if (albumPanel == null) {
            albumPanel = galleryPanel;
        }

        // Trouver l'album correspondant
        Album album = null;
        for (Album a : albums.values()) {
            if (a.getPanel() == albumPanel) {
                album = a;
                break;
            }
        }

        if (album == null) {
            System.err.println("Could not find album for the image to delete");
            return;
        }

        // Supprimer l'image du panel
        albumPanel.remove(imageLabel);
        albumPanel.revalidate();
        albumPanel.repaint();

        // Supprimer le chemin de l'image dans l'album
        album.removeImage(resourcePath);
    }

    /**
     * Deletes an album and all its images.
     *
     * @param albumName The name of the album to delete
     * @param thumbnail The thumbnail label for the album
     */
    public void deleteAlbum(String albumName, JLabel thumbnail) {
        // Empêcher la suppression de l'album par défaut
        if (albumName.equals("default")) {
            JOptionPane.showMessageDialog(null,
                    "The main album cannot be deleted.",
                    "Action impossible",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Demander confirmation
        int confirm = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to delete the album '" + albumName + "' and all its images?",
                "Confirm deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Récupérer l'album
            Album album = albums.get(albumName);
            if (album == null) return;

            // Trouver et supprimer la vue d'album
            Container parentContainer = thumbnail.getParent().getParent().getParent();
            if (parentContainer instanceof JPanel) {
                JPanel mainPanel = (JPanel) parentContainer;
                Component[] components = mainPanel.getComponents();

                for (Component comp : components) {
                    if (comp instanceof JPanel && comp.getName() != null
                            && comp.getName().equals("albumView_" + albumName)) {
                        mainPanel.remove(comp);
                        break;
                    }
                }

                // Retourner à la vue principale
                CardLayout cardLayout = (CardLayout) mainPanel.getLayout();
                cardLayout.show(mainPanel, "main");
            }

            // Supprimer l'album des structures de données
            albums.remove(albumName);

            // Supprimer la vignette de la barre d'albums
            albumBar.remove(thumbnail);
            albumBar.revalidate();
            albumBar.repaint();

            JOptionPane.showMessageDialog(null,
                    "Album '" + albumName + "' deleted successfully!",
                    "Album Deleted",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Refreshes all albums with current data.
     *
     * Clears and rebuilds all album panels and thumbnails based on the
     * current album data. This ensures the UI reflects the current state
     * of the data model.
     */
    public void refreshAllAlbums() {
        // Nettoyer les panels existants
        for (Album album : albums.values()) {
            album.getPanel().removeAll();
        }

        // Rafraîchir l'album bar
        albumBar.removeAll();

        // Recréer les vignettes et recharger les images
        for (Map.Entry<String, Album> entry : albums.entrySet()) {
            String albumName = entry.getKey();
            Album album = entry.getValue();

            // Remettre en place la vignette si nécessaire
            if (album.getThumbnail() == null && !albumName.equals("default")) {
                JLabel thumbnail = createAlbumThumbnail(albumName, album.getPanel());
                album.setThumbnail(thumbnail);
                albumBar.add(thumbnail);
            }

            // Charger les images dans le panel
            for (String path : album.getImagePaths()) {
                JLabel imageLabel = imageManager.createImageLabel(path, album.getPanel());
                if (imageLabel.getIcon() != null) {
                    album.getPanel().add(imageLabel);

                    // Définir la vignette d'album si nécessaire
                    if (album.getThumbnail() != null && album.getThumbnail().getIcon() == null
                            && imageLabel.getIcon() instanceof ImageIcon) {
                        album.getThumbnail().setIcon((ImageIcon) imageLabel.getIcon());
                    }
                }
            }

            album.getPanel().revalidate();
            album.getPanel().repaint();
        }

        albumBar.revalidate();
        albumBar.repaint();
    }

    /**
     * Gets the album data for persistence.
     *
     * @return A map of album names to lists of image paths
     */
    public Map<String, List<String>> getAlbumData() {
        Map<String, List<String>> result = new HashMap<>();

        for (Map.Entry<String, Album> entry : albums.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getImagePaths());
        }

        return result;
    }

    /**
     * Sets the album data from persistent storage.
     *
     * @param albumData A map of album names to lists of image paths
     */
    public void setAlbumData(Map<String, List<String>> albumData) {
        // Créer ou mettre à jour les albums
        for (Map.Entry<String, List<String>> entry : albumData.entrySet()) {
            String albumName = entry.getKey();
            List<String> paths = entry.getValue();

            Album album = albums.get(albumName);

            if (album == null) {
                // Créer un nouveau panel pour l'album
                JPanel newPanel = new JPanel(new GridLayout(0, 2, 10, 10));
                newPanel.setBackground(Color.WHITE);

                // Créer l'album
                album = new Album(albumName, newPanel);
                albums.put(albumName, album);

                // Ne pas créer de vignette ici, ce sera fait pendant le rafraîchissement
            }

            // Ajouter les chemins d'image à l'album
            for (String path : paths) {
                album.addImage(path);
            }
        }
    }
}