import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PictureGallery {
    private static JPanel galleryPanel;
    private static JPanel fullViewPanel;
    private static CardLayout viewLayout;
    private static JLabel fullImageLabel;
    private static JPanel pictureGalleryApp;
    private static Map<String, JPanel> albums = new HashMap<>();
    private static String currentAlbum = "default";

    public static JPanel createPictureGallery() {
        viewLayout = new CardLayout();
        pictureGalleryApp = new JPanel(viewLayout);

        // === Main View ===
        JPanel mainView = new JPanel(new BorderLayout());
        mainView.setBackground(Color.WHITE);

        // === Top Bar ===
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);

        JButton addBtn = new JButton("Add");
        JButton filterBtn = new JButton("Filter");
        JButton albumBtn = new JButton("Albums");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(addBtn);
        buttonPanel.add(filterBtn);

        topBar.add(buttonPanel, BorderLayout.WEST);
        topBar.add(albumBtn, BorderLayout.EAST);
        mainView.add(topBar, BorderLayout.NORTH);

        // === Gallery Panel ===
        galleryPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        galleryPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(galleryPanel);
        mainView.add(scrollPane, BorderLayout.CENTER);

        // === Full Image View ===
        fullViewPanel = new JPanel(new BorderLayout());
        fullViewPanel.setBackground(Color.WHITE);
        fullImageLabel = new JLabel("", SwingConstants.CENTER);
        fullViewPanel.add(fullImageLabel, BorderLayout.CENTER);

        JPanel fullControls = new JPanel();
        fullControls.setBackground(Color.WHITE);
        JButton resizeBtn = new JButton("Resize");
        JButton deleteBtn = new JButton("Delete");
        fullControls.add(resizeBtn);
        fullControls.add(deleteBtn);
        fullViewPanel.add(fullControls, BorderLayout.SOUTH);

        // === Actions ===
        addBtn.addActionListener(e -> showAddImageDialog());
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this image?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                JLabel origin = (JLabel) fullImageLabel.getClientProperty("originLabel");
                if (origin != null) {
                    JPanel albumPanel = (JPanel) origin.getClientProperty("albumPanel");
                    if (albumPanel != null) {
                        albumPanel.remove(origin);
                        albumPanel.revalidate();
                        albumPanel.repaint();
                    }
                }
                viewLayout.show(pictureGalleryApp, "main");
            }
        });
        resizeBtn.addActionListener(e -> showResizeDialog());
        albumBtn.addActionListener(e -> showAlbumChooser());

        // === Views ===
        pictureGalleryApp.add(mainView, "main");
        pictureGalleryApp.add(fullViewPanel, "full");

        viewLayout.show(pictureGalleryApp, "main");
        albums.put("default", galleryPanel);
        return pictureGalleryApp;
    }

    private static void showAddImageDialog() {
        String[] imagePaths = getImageResourcePaths();
        if (imagePaths.length == 0) {
            JOptionPane.showMessageDialog(null, "No images found in /resources/imageGallery");
            return;
        }

        String selected = (String) JOptionPane.showInputDialog(
                null,
                "Select an image :",
                "Add Image",
                JOptionPane.PLAIN_MESSAGE,
                null,
                imagePaths,
                imagePaths[0]
        );

        if (selected != null) {
            JLabel imgLabel = createImageLabelFromResource(selected);
            getCurrentAlbumPanel().add(imgLabel);
            getCurrentAlbumPanel().revalidate();
            getCurrentAlbumPanel().repaint();
        }
    }

    private static JLabel createImageLabelFromResource(String resourcePath) {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        URL imageUrl = PictureGallery.class.getResource(resourcePath);
        if (imageUrl != null) {
            ImageIcon icon = new ImageIcon(imageUrl);
            Image scaledImage = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaledImage));
        } else {
            label.setText("X");
        }

        // Store the album panel reference so we can update it later
        label.putClientProperty("albumPanel", getCurrentAlbumPanel());

        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                ImageIcon icon = (ImageIcon) label.getIcon();
                if (icon != null) {
                    Image scaled = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                    fullImageLabel.setIcon(new ImageIcon(scaled));
                    fullImageLabel.putClientProperty("originLabel", label);
                    viewLayout.show(pictureGalleryApp, "full");
                }
            }
        });

        return label;
    }

    private static void showResizeDialog() {
        String input = JOptionPane.showInputDialog("New size (e.g. 150x150) :");
        if (input != null && input.matches("\\d+x\\d+")) {
            String[] parts = input.split("x");
            int width = Integer.parseInt(parts[0]);
            int height = Integer.parseInt(parts[1]);

            // Get the label of the selected image
            JLabel origin = (JLabel) fullImageLabel.getClientProperty("originLabel");

            if (origin != null && origin.getIcon() instanceof ImageIcon) {
                ImageIcon oldIcon = (ImageIcon) origin.getIcon();

                // Resize the image to the new size
                Image resizedImage = oldIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                ImageIcon resizedIcon = new ImageIcon(resizedImage);

                // Set the resized image to the full view
                fullImageLabel.setIcon(resizedIcon);

                // Update the image in the gallery
                JPanel albumPanel = (JPanel) origin.getClientProperty("albumPanel");
                if (albumPanel != null) {
                    // Ensure that the resized image is updated in the gallery as well
                    origin.setIcon(resizedIcon);

                    // Revalidate and repaint the panel containing the image
                    albumPanel.revalidate();
                    albumPanel.repaint();
                }

                // Also revalidate and repaint the full view panel
                fullImageLabel.revalidate();
                fullImageLabel.repaint();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Invalid format. Use widthxheight");
        }
    }

    private static void showAlbumChooser() {
        String name = JOptionPane.showInputDialog("Name of album to create/select :");
        if (name != null && !name.trim().isEmpty()) {
            currentAlbum = name;
            if (!albums.containsKey(name)) {
                JPanel newAlbum = new JPanel(new GridLayout(0, 2, 10, 10));
                newAlbum.setBackground(Color.WHITE);
                albums.put(name, newAlbum);
                galleryPanel.add(new JLabel("<html><b>" + name + "</b></html>"));
                galleryPanel.add(newAlbum);
                galleryPanel.revalidate();
                galleryPanel.repaint();
            }
        }
    }

    private static JPanel getCurrentAlbumPanel() {
        return albums.getOrDefault(currentAlbum, galleryPanel);
    }

    private static String[] getImageResourcePaths() {
        List<String> paths = new ArrayList<>();
        try {
            String folder = "imageGallery";
            URL dirURL = PictureGallery.class.getClassLoader().getResource(folder);
            if (dirURL != null && dirURL.getProtocol().equals("file")) {
                File[] files = new File(dirURL.toURI()).listFiles();
                for (File file : files) {
                    if (file.getName().endsWith(".jpg") || file.getName().endsWith(".png")) {
                        paths.add("/" + folder + "/" + file.getName());
                    }
                }
            } else if (dirURL != null && dirURL.getProtocol().equals("jar")) {
                String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
                try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        String name = entries.nextElement().getName();
                        if (name.startsWith(folder + "/") && (name.endsWith(".jpg") || name.endsWith(".png"))) {
                            paths.add("/" + name);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return paths.toArray(new String[0]);
    }
}
