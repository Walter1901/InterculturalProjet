package Address.models;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

/**
 * Custom cell renderer for contact list with iOS-style appearance
 */
public class ContactCellRenderer extends JPanel implements ListCellRenderer<Contact> {
    private static final Color SELECTED_BACKGROUND = new Color(217, 217, 217);
    private static final Color DEFAULT_BACKGROUND = Color.WHITE;
    private static final Color SEPARATOR_COLOR = new Color(230, 230, 230);
    private static final Color SECONDARY_TEXT_COLOR = new Color(142, 142, 147);
    private static final Color CHEVRON_COLOR = new Color(199, 199, 204);

    private JLabel photoLabel;
    private JLabel nameLabel;
    private JLabel phoneLabel;
    private JLabel chevronLabel;
    private ImageIcon defaultIcon;


    public ContactCellRenderer() {
        initializeComponents();
        setupLayout();
    }

    private void initializeComponents() {
        // Photo label
        photoLabel = new JLabel();
        photoLabel.setPreferredSize(new Dimension(40, 40));
        photoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        photoLabel.setVerticalAlignment(SwingConstants.CENTER);

        // Initialize default icon
        defaultIcon = createDefaultAvatar(new Contact("A", "", "", "", "", "", "", null));
        photoLabel.setIcon(defaultIcon);

        // Name label
        nameLabel = new JLabel();
        nameLabel.setFont(new Font("SF Pro Text", Font.PLAIN, 17));
        nameLabel.setOpaque(false);

        // Phone label
        phoneLabel = new JLabel();
        phoneLabel.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        phoneLabel.setForeground(SECONDARY_TEXT_COLOR);
        phoneLabel.setOpaque(false);

        // Chevron label (iOS-style arrow)
        chevronLabel = new JLabel("›");
        chevronLabel.setFont(new Font("SF Pro Text", Font.PLAIN, 20));
        chevronLabel.setForeground(CHEVRON_COLOR);
        chevronLabel.setHorizontalAlignment(SwingConstants.CENTER);
        chevronLabel.setVerticalAlignment(SwingConstants.CENTER);
        chevronLabel.setPreferredSize(new Dimension(20, 20));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(12, 0));
        setOpaque(true);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, SEPARATOR_COLOR),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        // Left panel for photo
        JPanel photoPanel = new JPanel(new BorderLayout());
        photoPanel.setOpaque(false);
        photoPanel.add(photoLabel, BorderLayout.CENTER);

        // Center panel for text content
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(phoneLabel);

        // Right panel for chevron
        JPanel chevronPanel = new JPanel(new BorderLayout());
        chevronPanel.setOpaque(false);
        chevronPanel.add(chevronLabel, BorderLayout.CENTER);

        add(photoPanel, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);
        add(chevronPanel, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends Contact> list,
            Contact contact,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        // Set background color
        setBackground(isSelected ? SELECTED_BACKGROUND : DEFAULT_BACKGROUND);

        // Update photo - CORRECTION ICI
        ImageIcon contactPhoto = contact.getPhoto();
        if (contactPhoto != null) {
            // Scale the existing ImageIcon
            Image photo = contactPhoto.getImage();
            if (photo != null) {
                photo = photo.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                photoLabel.setIcon(new ImageIcon(photo));
            } else {
                photoLabel.setIcon(createDefaultAvatar(contact));
            }
        } else {
            photoLabel.setIcon(createDefaultAvatar(contact));
        }

        // Update text content
        updateTextContent(contact, isSelected);

        return this;
    }

    private void updatePhoto(Contact contact) {
        ImageIcon photo = contact.getPhoto();
        if (photo == null) {
            photo = defaultIcon;
        }

        if (photo != null) {
            // Scale image to proper size
            Image scaledImage = photo.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            photoLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            // Use a default colored circle if no icon available
            photoLabel.setIcon(createDefaultAvatar(contact));
        }
    }

    private void updateTextContent(Contact contact, boolean isSelected) {
        // Set name
        String displayName = contact.getDisplayName();
        nameLabel.setText(displayName);
        nameLabel.setForeground(Color.BLACK);

        // Set phone with formatting
        String phoneText = contact.getFormattedPhone();
        if (phoneText.isEmpty()) {
            phoneText = "No phone number";
        }
        phoneLabel.setText(phoneText);
        phoneLabel.setForeground(isSelected ? SECONDARY_TEXT_COLOR.darker() : SECONDARY_TEXT_COLOR);
    }

    /**
     * Creates a default avatar with initials if no photo is available
     */
    private ImageIcon createDefaultAvatar(Contact contact) {
        // Create a simple colored circle with initials
        int size = 40;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set background color (based on name hash for consistency)
        Color bgColor = generateColorFromName(contact.getDisplayName());
        g2d.setColor(bgColor);
        g2d.fillOval(0, 0, size, size);

        // Set text color and font
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SF Pro Text", Font.BOLD, 14));

        // Draw initials
        String initials = getInitials(contact);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(initials);
        int textHeight = fm.getAscent();
        int x = (size - textWidth) / 2;
        int y = (size + textHeight) / 2 - 2;

        g2d.drawString(initials, x, y);
        g2d.dispose();

        return new ImageIcon(String.valueOf(image));
    }

    private Color generateColorFromName(String name) {
        // Generate a consistent color based on the name hash
        int hash = name.hashCode();
        Color[] colors = {
                new Color(74, 144, 226),   // Blue
                new Color(52, 199, 89),    // Green
                new Color(255, 149, 0),    // Orange
                new Color(255, 59, 48),    // Red
                new Color(175, 82, 222),   // Purple
                new Color(255, 204, 0),    // Yellow
                new Color(90, 200, 250),   // Light Blue
                new Color(255, 45, 85)     // Pink
        };
        return colors[Math.abs(hash) % colors.length];
    }

    private String getInitials(Contact contact) {
        String firstName = contact.getFirstName();
        String lastName = contact.getLastName();

        StringBuilder initials = new StringBuilder();
        if (!firstName.isEmpty()) {
            initials.append(firstName.charAt(0));
        }
        if (!lastName.isEmpty()) {
            initials.append(lastName.charAt(0));
        }

        if (initials.isEmpty()) {
            initials.append("?");
        }

        return initials.toString().toUpperCase();
    }
}