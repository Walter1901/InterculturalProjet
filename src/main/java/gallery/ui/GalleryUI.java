package gallery.ui;

import gallery.PictureGalleryApp;
import gallery.service.AlbumManager;
import gallery.service.ImageManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Manages the user interface for the gallery application.
 *
 * This class is responsible for creating and managing all UI components,
 * handling user interactions, and providing a consistent user experience.
 * The UI is structured with a CardLayout that allows switching between
 * different views:
 * - Main Gallery View - Shows all images in the current album
 * - Full Screen View - Shows a single image in detail with editing options
 * - Album Views - One view per album, dynamically created
 *
 * The class delegates business logic to the appropriate manager classes
 * and updates the UI based on the results.
 */
public class GalleryUI {
    private PictureGalleryApp app;
    private AlbumManager albumManager;
    private ImageManager imageManager;

    // Composants UI principaux
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel galleryPanel;
    private JPanel fullViewPanel;
    private JLabel fullImageLabel;
    private JPanel albumBar;
    private JPanel fullControls;
    private JComboBox<String> filterComboBox;


    public GalleryUI(PictureGalleryApp app, AlbumManager albumManager, ImageManager imageManager) {
        this.app = app;
        this.albumManager = albumManager;
        this.imageManager = imageManager;
    }

    /**
     * Creates the main interface of the gallery.
     *
     * Initializes all UI components and panels, sets up the card layout,
     * and prepares the basic structure for the gallery application.
     *
     * @return A JPanel containing the complete gallery UI
     */
    public JPanel createMainInterface() {
        // Initialiser le layout et le panel principal
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Créer les différentes vues
        JPanel mainView = createMainView();
        fullViewPanel = createFullScreenView();

        // Ajouter les vues au panel principal
        mainPanel.add(mainView, "main");
        mainPanel.add(fullViewPanel, "full");

        // Afficher la vue principale par défaut
        cardLayout.show(mainPanel, "main");

        return mainPanel;
    }

    /**
     * Creates the main gallery view.
     *
     * This view contains:
     * - A top toolbar with Add and Albums buttons
     * - A central scrollable panel showing images in a grid
     * - A bottom scrollable bar showing album thumbnails
     *
     * @return The main gallery view panel
     */
    private JPanel createMainView() {
        JPanel mainView = new JPanel(new BorderLayout());
        mainView.setBackground(Color.WHITE);

        // Barre d'outils supérieure avec boutons d'action
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);

        JButton addBtn = new JButton("Add");
        JButton albumBtn = new JButton("Albums");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(addBtn);

        topBar.add(buttonPanel, BorderLayout.WEST);
        topBar.add(albumBtn, BorderLayout.EAST);
        mainView.add(topBar, BorderLayout.NORTH);

        // Panel central avec la grille d'images
        galleryPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        galleryPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(galleryPanel);
        mainView.add(scrollPane, BorderLayout.CENTER);

        // Barre d'albums en bas
        albumBar = new JPanel();
        albumBar.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));
        albumBar.setBackground(Color.LIGHT_GRAY);

        JScrollPane albumScrollPane = new JScrollPane(
                albumBar,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        albumScrollPane.setPreferredSize(new Dimension(0, 160));
        albumScrollPane.setBorder(null);
        mainView.add(albumScrollPane, BorderLayout.SOUTH);

        // Enregistrer les panneaux dans le gestionnaire d'albums
        albumManager.registerPanels(galleryPanel, albumBar);

        // Ajouter les écouteurs d'événement
        addBtn.addActionListener(e -> showAddImageDialog());
        albumBtn.addActionListener(e -> showAlbumChooser());

        return mainView;
    }

    /**
     * Creates the full screen view for displaying and editing images.
     *
     * This view contains:
     * - A top bar with a Back button
     * - A central area showing the selected image
     * - A bottom control panel with editing options
     *
     * @return The full screen view panel
     */
    private JPanel createFullScreenView() {
        JPanel fullView = new JPanel(new BorderLayout());
        fullView.setBackground(Color.WHITE);

        // Étiquette pour l'image en plein écran
        fullImageLabel = new JLabel("", SwingConstants.CENTER);
        fullView.add(fullImageLabel, BorderLayout.CENTER);

        // Barre supérieure avec bouton de retour
        JPanel fullTopBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fullTopBar.setBackground(Color.WHITE);
        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "main"));
        fullTopBar.add(backBtn);
        fullView.add(fullTopBar, BorderLayout.NORTH);

        // Contrôles en bas pour manipuler l'image
        fullControls = new JPanel();
        fullControls.setBackground(Color.WHITE);

        JButton resizeBtn = new JButton("Resize");
        JButton deleteBtn = new JButton("Delete");

        String[] filters = {"None", "Grayscale", "Sepia", "Invert"};
        filterComboBox = new JComboBox<>(filters);

        fullControls.add(resizeBtn);
        fullControls.add(deleteBtn);
        fullControls.add(filterComboBox);

        fullView.add(fullControls, BorderLayout.SOUTH);

        // Actions des boutons
        deleteBtn.addActionListener(e -> deleteCurrentImage());
        resizeBtn.addActionListener(e -> showResizeDialog());
        filterComboBox.addActionListener(e -> applySelectedFilter());

        // Enregistrer les composants dans le gestionnaire d'images
        imageManager.registerFullScreenComponents(fullImageLabel, filterComboBox);

        return fullView;
    }

    /**
     * Shows a dialog for selecting and adding an image to the gallery.
     *
     * The dialog presents a list of available images from the resources folder.
     * When an image is selected, it is compressed using TinyPNG and added to
     * the current album.
     */
    private void showAddImageDialog() {
        String[] imagePaths = imageManager.getImageResourcePaths();

        if (imagePaths.length == 0) {
            JOptionPane.showMessageDialog(null,
                    "No images found in /resources/imageGallery",
                    "No Images Found",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selected = (String) JOptionPane.showInputDialog(null,
                "Select an image:",
                "Add Image",
                JOptionPane.PLAIN_MESSAGE,
                null,
                imagePaths,
                imagePaths[0]);

        if (selected != null) {
            // Compresser et ajouter l'image à l'album actuel
            albumManager.addImageToAlbum(app.getCurrentAlbum(), selected);

            // Retourner à la vue principale et sauvegarder
            cardLayout.show(mainPanel, "main");
            app.saveGallery();
        }
    }

    /**
     * Shows a dialog for creating or selecting an album.
     *
     * If the album name entered doesn't exist, a new album is created.
     * The gallery state is saved after creating a new album.
     */
    private void showAlbumChooser() {
        String name = JOptionPane.showInputDialog("Name of album to create/select:");

        if (name != null && !name.trim().isEmpty()) {
            boolean created = albumManager.createAlbumIfNotExists(name);
            if (created) {
                app.saveGallery();
            }
        }
    }

    /**
     * Deletes the currently displayed image after confirmation.
     *
     * Shows a confirmation dialog, and if confirmed, removes the image from
     * its album and updates the UI accordingly.
     */
    private void deleteCurrentImage() {
        JLabel origin = (JLabel) fullImageLabel.getClientProperty("originLabel");
        if (origin != null) {
            int confirm = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to delete this image?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                albumManager.deleteImage(origin);

                // Nettoyer la vue en plein écran
                fullImageLabel.setIcon(null);
                fullImageLabel.putClientProperty("originLabel", null);

                // Sauvegarder les changements
                app.saveGallery();

                // Retourner à la vue principale
                cardLayout.show(mainPanel, "main");
            }
        }
    }

    /**
     * Shows a dialog for resizing the currently displayed image.
     *
     * Accepts input in the format "widthxheight" (e.g., "150x150")
     * and resizes the image accordingly.
     */
    private void showResizeDialog() {
        String input = JOptionPane.showInputDialog("New size (e.g. 150x150):");

        if (input != null && input.matches("\\d+x\\d+")) {
            String[] parts = input.split("x");
            int width = Integer.parseInt(parts[0]);
            int height = Integer.parseInt(parts[1]);

            JLabel origin = (JLabel) fullImageLabel.getClientProperty("originLabel");
            if (origin != null) {
                imageManager.resizeImage(origin, fullImageLabel, width, height);
                app.saveGallery();
            }
        } else if (input != null) {
            JOptionPane.showMessageDialog(null,
                    "Invalid format. Use widthxheight");
        }
    }

    /**
     * Applies the currently selected filter to the displayed image.
     *
     * Gets the filter name from the filterComboBox and applies it to
     * the image using the ImageManager.
     */
    private void applySelectedFilter() {
        JLabel origin = (JLabel) fullImageLabel.getClientProperty("originLabel");
        if (origin != null) {
            String filterName = (String) filterComboBox.getSelectedItem();
            imageManager.applyFilter(origin, fullImageLabel, filterName);
            app.saveGallery();
        }
    }

    /**
     * Refreshes the gallery UI based on loaded data.
     *
     * Updates all albums, their contents, and thumbnails to reflect
     * the current state of the data model.
     */
    public void refreshGalleryFromData() {
        albumManager.refreshAllAlbums();
    }

    /**
     * Switches to the specified view in the card layout.
     *
     * @param viewName The name of the view to display
     */
    public void showView(String viewName) {
        cardLayout.show(mainPanel, viewName);
    }
}