import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
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
    private static JPanel albumBar;
    private static Map<String, JPanel> albums = new HashMap<>();
    private static Map<String, List<String>> albumData = new HashMap<>();
    private static Map<String, JLabel> albumThumbnails = new HashMap<>();
    private static String currentAlbum = "default";

    public static JPanel createPictureGallery() {
        viewLayout = new CardLayout();
        pictureGalleryApp = new JPanel(viewLayout);

        JPanel mainView = new JPanel(new BorderLayout());
        mainView.setBackground(Color.WHITE);

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

        galleryPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        galleryPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(galleryPanel);
        mainView.add(scrollPane, BorderLayout.CENTER);

        albumBar = new JPanel();
        albumBar.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        albumBar.setBackground(Color.LIGHT_GRAY);
        mainView.add(albumBar, BorderLayout.SOUTH);

        fullViewPanel = new JPanel(new BorderLayout());
        fullViewPanel.setBackground(Color.WHITE);
        fullImageLabel = new JLabel("", SwingConstants.CENTER);
        fullViewPanel.add(fullImageLabel, BorderLayout.CENTER);

        JPanel fullTopBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fullTopBar.setBackground(Color.WHITE);
        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> viewLayout.show(pictureGalleryApp, "main"));
        fullTopBar.add(backBtn);
        fullViewPanel.add(fullTopBar, BorderLayout.NORTH);

        JPanel fullControls = new JPanel();
        fullControls.setBackground(Color.WHITE);
        JButton resizeBtn = new JButton("Resize");
        JButton deleteBtn = new JButton("Delete");
        fullControls.add(resizeBtn);
        fullControls.add(deleteBtn);
        String[] filters = {"None", "Grayscale", "Sepia", "Invert"};
        JComboBox<String> filterCombo = new JComboBox<>(filters);
        filterCombo.addActionListener(e -> applyFilter((String) filterCombo.getSelectedItem()));
        fullControls.add(filterCombo);
        fullViewPanel.add(fullControls, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> showAddImageDialog());
        deleteBtn.addActionListener(e -> deleteCurrentImage());
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

    private static void deleteCurrentImage() {
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
    }

    private static void showAddImageDialog() {
        String[] imagePaths = getImageResourcePaths();
        if (imagePaths.length == 0) {
            JOptionPane.showMessageDialog(null, "No images found in /resources/imageGallery");
            return;
        }

        String selected = (String) JOptionPane.showInputDialog(null, "Select an image :", "Add Image", JOptionPane.PLAIN_MESSAGE, null, imagePaths, imagePaths[0]);

        if (selected != null) {
            JLabel imgLabel = createImageLabelFromResource(selected);


            JPanel mainGalleryPanel = albums.get("default");
            mainGalleryPanel.add(imgLabel);
            mainGalleryPanel.revalidate();
            mainGalleryPanel.repaint();


            albumData.get("default").add(selected);

            saveGallery();
        }
    }


    private static JLabel createImageLabelFromResource(String resourcePath) {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        URL imageUrl = PictureGallery.class.getResource(resourcePath);
        ImageIcon icon = null;
        if (imageUrl != null) {
            icon = new ImageIcon(imageUrl);
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
                    fullImageLabel.setIcon(new ImageIcon(icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH)));
                    fullImageLabel.putClientProperty("originLabel", label);
                    viewLayout.show(pictureGalleryApp, "full");
                }
            }
        });

        if (icon != null) label.putClientProperty("originalImage", icon.getImage());

        new DragSource().createDefaultDragGestureRecognizer(label, DnDConstants.ACTION_COPY, dge -> {
            String path = (String) label.getClientProperty("resourcePath");
            dge.startDrag(DragSource.DefaultCopyDrop, new StringSelection(path));
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
                if (albumPanel != null) albumPanel.revalidate();
            }
        } else JOptionPane.showMessageDialog(null, "Invalid format. Use widthxheight");
    }

    private static void showAlbumChooser() {
        String name = JOptionPane.showInputDialog("Name of album to create/select :");
        if (name != null && !name.trim().isEmpty()) {
            if (!albums.containsKey(name)) {
                JPanel albumPanel = new JPanel(new GridLayout(0, 2, 10, 10));
                albums.put(name, albumPanel);
                albumData.put(name, new ArrayList<>());

                JLabel thumbnail = new JLabel(name, JLabel.CENTER);
                thumbnail.setVerticalTextPosition(JLabel.BOTTOM);
                thumbnail.setHorizontalTextPosition(JLabel.CENTER);
                thumbnail.setPreferredSize(new Dimension(100, 120));
                thumbnail.setTransferHandler(new TransferHandler("text") {
                    public boolean canImport(TransferHandler.TransferSupport support) {
                        return support.isDataFlavorSupported(DataFlavor.stringFlavor);
                    }

                    public boolean importData(TransferHandler.TransferSupport support) {
                        try {
                            String path = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                            JLabel newLabel = createImageLabelFromResource(path);
                            albumPanel.add(newLabel);
                            albumPanel.revalidate();
                            albumPanel.repaint();
                            albumData.get(name).add(path);
                            if (albumData.get(name).size() == 1) {
                                ImageIcon icon = (ImageIcon) newLabel.getIcon();
                                thumbnail.setIcon(icon);
                            }
                            saveGallery();
                            return true;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        return false;
                    }
                });

                thumbnail.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        JPanel albumView = new JPanel(new BorderLayout());
                        albumView.setBackground(Color.WHITE);

                        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        topBar.setBackground(Color.WHITE);
                        JButton backBtn = new JButton("Back");
                        backBtn.addActionListener(ev -> viewLayout.show(pictureGalleryApp, "currentAlbum"));
                        topBar.add(backBtn);
                        albumView.add(topBar, BorderLayout.NORTH);

                        JScrollPane scrollPane = new JScrollPane(albumPanel);
                        albumView.add(scrollPane, BorderLayout.CENTER);

                        pictureGalleryApp.add(albumView, name);
                        viewLayout.show(pictureGalleryApp, name);
                    }
                });


                albumThumbnails.put(name, thumbnail);
                albumBar.add(thumbnail);
                albumBar.revalidate();
            }
            currentAlbum = name;
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
        for (File file : files) result.add(folder + "/" + file.getName());
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
            Type type = new TypeToken<Map<String, List<String>>>() {
            }.getType();
            albumData = new Gson().fromJson(reader, type);

            for (String albumName : albumData.keySet()) {
                currentAlbum = albumName;
                if (!albums.containsKey(albumName)) {
                    JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
                    albums.put(albumName, panel);
                    JLabel thumbnail = new JLabel(albumName, JLabel.CENTER);
                    thumbnail.setVerticalTextPosition(JLabel.BOTTOM);
                    thumbnail.setHorizontalTextPosition(JLabel.CENTER);
                    thumbnail.setPreferredSize(new Dimension(100, 120));

                    for (String path : albumData.get(albumName)) {
                        JLabel label = createImageLabelFromResource(path);
                        panel.add(label);
                        if (!albumThumbnails.containsKey(albumName)) {
                            ImageIcon icon = (ImageIcon) label.getIcon();
                            thumbnail.setIcon(icon);
                            albumThumbnails.put(albumName, thumbnail);
                        }
                    }

                    thumbnail.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                            JPanel albumView = new JPanel(new BorderLayout());
                            albumView.setBackground(Color.WHITE);

                            JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
                            topBar.setBackground(Color.WHITE);
                            JButton backBtn = new JButton("Back");
                            backBtn.addActionListener(ev -> viewLayout.show(pictureGalleryApp, "main"));
                            topBar.add(backBtn);
                            albumView.add(topBar, BorderLayout.NORTH);

                            JScrollPane scrollPane = new JScrollPane(panel);
                            albumView.add(scrollPane, BorderLayout.CENTER);

                            pictureGalleryApp.add(albumView, albumName);
                            viewLayout.show(pictureGalleryApp, albumName);
                        }
                    });


                    thumbnail.setTransferHandler(new TransferHandler("text") {
                        public boolean canImport(TransferHandler.TransferSupport support) {
                            return support.isDataFlavorSupported(DataFlavor.stringFlavor);
                        }

                        public boolean importData(TransferHandler.TransferSupport support) {
                            try {
                                String path = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                                JLabel newLabel = createImageLabelFromResource(path);
                                panel.add(newLabel);
                                albumData.get(albumName).add(path);
                                panel.revalidate();
                                panel.repaint();
                                if (albumData.get(albumName).size() == 1) {
                                    ImageIcon icon = (ImageIcon) newLabel.getIcon();
                                    thumbnail.setIcon(icon);
                                }
                                saveGallery();
                                return true;
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            return false;
                        }
                    });

                    albumBar.add(thumbnail);
                }
            }
            albumBar.revalidate();
            galleryPanel.revalidate();
            galleryPanel.repaint();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void applyFilter(String filterName) {
        JLabel origin = (JLabel) fullImageLabel.getClientProperty("originLabel");
        if (origin == null || origin.getClientProperty("originalImage") == null) return;

        Image original = (Image) origin.getClientProperty("originalImage");
        int width = original.getWidth(null);
        int height = original.getHeight(null);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();

        switch (filterName) {
            case "Grayscale":
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int rgb = img.getRGB(x, y);
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;
                        int gray = (r + g + b) / 3;
                        int newRGB = (gray << 16) | (gray << 8) | gray;
                        img.setRGB(x, y, newRGB);
                    }
                }
                break;
            case "Sepia":
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int rgb = img.getRGB(x, y);
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;
                        int tr = (int)(0.393 * r + 0.769 * g + 0.189 * b);
                        int tg = (int)(0.349 * r + 0.686 * g + 0.168 * b);
                        int tb = (int)(0.272 * r + 0.534 * g + 0.131 * b);
                        tr = Math.min(255, tr);
                        tg = Math.min(255, tg);
                        tb = Math.min(255, tb);
                        int newRGB = (tr << 16) | (tg << 8) | tb;
                        img.setRGB(x, y, newRGB);
                    }
                }
                break;
            case "Invert":
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int rgb = img.getRGB(x, y);
                        int r = 255 - ((rgb >> 16) & 0xFF);
                        int g = 255 - ((rgb >> 8) & 0xFF);
                        int b = 255 - (rgb & 0xFF);
                        int newRGB = (r << 16) | (g << 8) | b;
                        img.setRGB(x, y, newRGB);
                    }
                }
                break;
            case "None":
            default:

                break;
        }

        ImageIcon filteredIcon = new ImageIcon(img.getScaledInstance(200, 200, Image.SCALE_SMOOTH));
        fullImageLabel.setIcon(filteredIcon);
    }

    private static void updateImagePathInAlbumData(JLabel label, String newPath) {
        JPanel albumPanel = (JPanel) label.getClientProperty("albumPanel");
        String oldPath = (String) label.getClientProperty("resourcePath");

        for (Map.Entry<String, JPanel> entry : albums.entrySet()) {
            if (entry.getValue() == albumPanel) {
                List<String> paths = albumData.get(entry.getKey());
                int index = paths.indexOf(oldPath);
                if (index != -1) {
                    paths.set(index, newPath);
                    label.putClientProperty("resourcePath", newPath);
                }
                break;
            }
        }
    }

}