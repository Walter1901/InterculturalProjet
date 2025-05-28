package Address;

import Address.models.Contact;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GalleryFrame extends JFrame {
    private static final Color backgroundColor = new Color(28, 28, 30);
    private static final Color textColor = new Color(255, 255, 255);
    private static final Color cardColor = new Color(44, 44, 46);
    private static final String GALLERY_DATA_PATH = "gallery_data.json";

    private JList<String> photoList;
    private DefaultListModel<String> listModel;
    private JPanel previewPanel;
    private JLabel previewLabel;
    private JButton selectButton;
    private Contact targetContact;

    public GalleryFrame(Contact contact) {
        super("Select Profile Photo");
        this.targetContact = contact;
        initializeUI();
        loadPhotos();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));
        setBackground(backgroundColor);

        // Photo list panel
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(backgroundColor);

        listModel = new DefaultListModel<>();
        photoList = new JList<>(listModel);
        photoList.setCellRenderer(new PhotoCellRenderer());
        photoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        photoList.setBackground(cardColor);
        photoList.setForeground(textColor);

        JScrollPane listScrollPane = new JScrollPane(photoList);
        listScrollPane.setPreferredSize(new Dimension(350, 300));
        listScrollPane.getViewport().setBackground(cardColor);
        listPanel.add(listScrollPane, BorderLayout.CENTER);

        // Preview panel
        previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBackground(backgroundColor);
        previewLabel = new JLabel();
        previewPanel.add(previewLabel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonsPanel.setBackground(backgroundColor);

        selectButton = new JButton("Select");
        selectButton.setFont(new Font("Inter", Font.PLAIN, 12));
        selectButton.setForeground(Color.WHITE);
        selectButton.setBackground(new Color(0, 122, 255));
        selectButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        selectButton.setFocusPainted(false);
        selectButton.setEnabled(false);

        buttonsPanel.add(selectButton);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBackground(backgroundColor);
        mainPanel.add(listPanel, BorderLayout.CENTER);
        mainPanel.add(previewPanel, BorderLayout.NORTH);
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Setup event handlers
        photoList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedPhoto = photoList.getSelectedValue();
                if (selectedPhoto != null) {
                    updatePreview(selectedPhoto);
                    selectButton.setEnabled(true);
                } else {
                    previewLabel.setIcon(null);
                    selectButton.setEnabled(false);
                }
            }
        });

        selectButton.addActionListener(e -> {
            String selectedPhoto = photoList.getSelectedValue();
            if (selectedPhoto != null) {
                targetContact.setPhoto(selectedPhoto);
                dispose();
            }
        });
    }

    private void loadPhotos() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(GALLERY_DATA_PATH)));
            // Here we would parse the JSON and add photos to listModel
            // For now, we'll add a simple list of photos
            List<String> photos = new ArrayList<>();
            photos.add("/imageGallery/Walter1.jpeg");
            photos.add("/imageGallery/Walter2.jpeg");
            photos.add("/imageGallery/SEAT_LEON.jpg");
            photos.add("/imageGallery/SEAT_LEON_2.jpg");
            photos.add("/imageGallery/SEAT_LEON_3.jpg");

            photos.forEach(listModel::addElement);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading photos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePreview(String photoPath) {
        ImageIcon icon = new ImageIcon(photoPath);
        Image image = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        previewLabel.setIcon(new ImageIcon(image));
    }

    private class PhotoCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setFont(new Font("Inter", Font.PLAIN, 12));
            label.setForeground(isSelected ? backgroundColor : textColor);
            label.setBackground(isSelected ? textColor : cardColor);
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            return label;
        }
    }
}
