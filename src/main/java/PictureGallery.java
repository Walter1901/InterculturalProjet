import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;
import java.util.List;

public class PictureGallery {
    private static final String SAVE_FILE = "gallery_data.json";

    private static JPanel galleryPanel;
    private static JPanel fullViewPanel;
    private static CardLayout viewLayout;
    private static JLabel fullImageLabel;
    private static JPanel pictureGalleryApp;
    private static Map<String, JPanel> albums = new HashMap<>();
    private static Map<String, List<String>> albumData = new HashMap<>();
    private static String currentAlbum = "default";

    public static JPanel createPictureGallery() {
        viewLayout = new CardLayout();
        pictureGalleryApp = new JPanel(viewLayout);

        JPanel mainView = new JPanel(new BorderLayout());
        mainView.setBackground(Color.WHITE);

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

        galleryPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        galleryPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(galleryPanel);
        mainView.add(scrollPane, BorderLayout.CENTER);

        fullViewPanel = new JPanel(new BorderLayout());
        fullViewPanel.setBackground(Color.WHITE);
        fullImageLabel = new JLabel("", SwingConstants.CENTER);
        fullViewPanel.add(fullImageLabel, BorderLayout.CENTER);

        JPanel fullTopBar = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Aligné à gauche
        fullTopBar.setBackground(Color.WHITE);

        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> viewLayout.show(pictureGalleryApp, "main")); // Revient à la galerie
        fullTopBar.add(backBtn);

        fullViewPanel.add(fullTopBar, BorderLayout.NORTH); // Ajoute la barre en haut

        JPanel fullControls = new JPanel();
        fullControls.setBackground(Color.WHITE);
        JButton resizeBtn = new JButton("Resize");
        JButton deleteBtn = new JButton("Delete");
        fullControls.add(resizeBtn);
        fullControls.add(deleteBtn);
        fullViewPanel.add(fullControls, BorderLayout.SOUTH);

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

                    // Remove from album data
                    for (Map.Entry<String, JPanel> entry : albums.entrySet()) {
                        if (entry.getValue() == albumPanel) {
                            List<String> paths = albumData.get(entry.getKey());
                            paths.remove(origin.getClientProperty("resourcePath"));
                            break;
                        }
                    }

                    saveGallery();
                }
                viewLayout.show(pictureGalleryApp, "main");
            }
        });
        resizeBtn.addActionListener(e -> showResizeDialog());
        albumBtn.addActionListener(e -> showAlbumChooser());

        pictureGalleryApp.add(mainView, "main");
        pictureGalleryApp.add(fullViewPanel, "full");

        albums.put("default", galleryPanel);
        albumData.put("default", new ArrayList<>());
        loadGallery();

        viewLayout.show(pictureGalleryApp, "main");
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
            albumData.get(currentAlbum).add(selected);
            saveGallery();
        }
    }

    private static JLabel createImageLabelFromResource(String resourcePath) {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        //label.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        URL imageUrl = PictureGallery.class.getResource(resourcePath);
        if (imageUrl != null) {
            ImageIcon icon = new ImageIcon(imageUrl);
            Image scaledImage = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaledImage));
        } else {
            label.setText("X");
        }

        label.putClientProperty("albumPanel", getCurrentAlbumPanel());
        label.putClientProperty("resourcePath", resourcePath);

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

            JLabel origin = (JLabel) fullImageLabel.getClientProperty("originLabel");

            if (origin != null && origin.getIcon() instanceof ImageIcon) {
                ImageIcon oldIcon = (ImageIcon) origin.getIcon();
                Image resizedImage = oldIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                ImageIcon resizedIcon = new ImageIcon(resizedImage);

                fullImageLabel.setIcon(resizedIcon);
                origin.setIcon(resizedIcon);

                JPanel albumPanel = (JPanel) origin.getClientProperty("albumPanel");
                if (albumPanel != null) {
                    albumPanel.revalidate();
                    albumPanel.repaint();
                }
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
                albumData.put(name, new ArrayList<>());
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
        String folder = "/imageGallery";
        URL dirURL = PictureGallery.class.getResource(folder);
        if (dirURL == null) return new String[0];
        File directory = new File(dirURL.getFile());
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));

        if (files == null) return new String[0];

        List<String> result = new ArrayList<>();
        for (File file : files) {
            result.add(folder + "/" + file.getName());
        }
        return result.toArray(new String[0]);
    }

    private static void saveGallery() {
        try (Writer writer = new FileWriter(SAVE_FILE)) {
            new Gson().toJson(albumData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadGallery() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, List<String>>>() {}.getType();
            albumData = new Gson().fromJson(reader, type);

            for (String albumName : albumData.keySet()) {
                currentAlbum = albumName;
                if (!albums.containsKey(albumName)) {
                    JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
                    panel.setBackground(Color.WHITE);
                    albums.put(albumName, panel);
                    galleryPanel.add(new JLabel("<html><b>" + albumName + "</b></html>"));
                    galleryPanel.add(panel);
                }
                for (String path : albumData.get(albumName)) {
                    JLabel label = createImageLabelFromResource(path);
                    albums.get(albumName).add(label);
                }
            }
            galleryPanel.revalidate();
            galleryPanel.repaint();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
