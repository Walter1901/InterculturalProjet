package Address.models;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Dialog for adding new contacts to the address book
 */
public class AddContactDialog extends JDialog {
    private Contact newContact;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField addressField;
    private JTextField birthDateField;
    private JLabel photoLabel;
    private ImageIcon selectedPhoto;

    public AddContactDialog(Frame parent) {
        super(parent, "Add New Contact", true);
        initializeDialog();
        createComponents();
        layoutComponents();
        setupEventHandlers();
    }

    private void initializeDialog() {
        setSize(400, 500);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
    }

    private void createComponents() {
        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        phoneField = new JTextField(20);
        emailField = new JTextField(20);
        addressField = new JTextField(20);
        birthDateField = new JTextField(20);

        photoLabel = new JLabel("No photo selected");
        photoLabel.setPreferredSize(new Dimension(80, 80));
        photoLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        photoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        photoLabel.setVerticalAlignment(SwingConstants.CENTER);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));

        // Main form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        // Photo panel
        JPanel photoPanel = new JPanel(new FlowLayout());
        JButton selectPhotoBtn = new JButton("Select Photo");
        photoPanel.add(photoLabel);
        photoPanel.add(selectPhotoBtn);

        // Form fields
        formPanel.add(createFieldPanel("First Name *:", firstNameField));
        formPanel.add(createFieldPanel("Last Name *:", lastNameField));
        formPanel.add(createFieldPanel("Phone:", phoneField));
        formPanel.add(createFieldPanel("Email:", emailField));
        formPanel.add(createFieldPanel("Address:", addressField));
        formPanel.add(createFieldPanel("Birth Date:", birthDateField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(photoPanel);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save Contact");
        JButton cancelButton = new JButton("Cancel");

        saveButton.setPreferredSize(new Dimension(120, 35));
        cancelButton.setPreferredSize(new Dimension(120, 35));

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Setup button actions
        selectPhotoBtn.addActionListener(e -> selectPhoto());
        saveButton.addActionListener(e -> saveContact());
        cancelButton.addActionListener(e -> dispose());
    }

    private JPanel createFieldPanel(String labelText, JTextField textField) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(100, 25));
        label.setFont(new Font("SF Pro Text", Font.PLAIN, 14));

        textField.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        panel.add(label, BorderLayout.WEST);
        panel.add(textField, BorderLayout.CENTER);

        return panel;
    }

    private void setupEventHandlers() {
        // Add input validation or formatting if needed
        phoneField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c) && c != '-' && c != '(' && c != ')' && c != ' ' && c != KeyEvent.VK_BACK_SPACE) {
                    evt.consume();
                }
            }
        });
    }

    private void selectPhoto() {
        GalleryFrame gallery = new GalleryFrame();
        gallery.addImageSelectedListener(image -> {
            selectedPhoto = image;
            photoLabel.setIcon(new ImageIcon(image.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH)));
            photoLabel.setText("");
        });
        gallery.setVisible(true);
    }

    private void saveContact() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String birthDate = birthDateField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "First Name and Last Name are required fields!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate email format if provided
        if (!email.isEmpty() && !isValidEmail(email)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid email address!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create new contact
        try {
            newContact = new Contact(
                    firstName + " " + lastName,
                    phone,
                    firstName,
                    lastName,
                    birthDate,
                    address,
                    email,
                    selectedPhoto
            );

            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error creating contact: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }

    public Contact getContact() {
        return newContact;
    }
}