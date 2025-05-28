package Address;

import Address.models.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Main AddressBook application class that integrates all functionalities
 * Provides a tabbed interface with contacts, map, and gallery features
 * Modified to work with phoneUtils infrastructure
 */
public class AddressBook {
    private static final Color backgroundColor = new Color(28, 28, 30);
    private static final Color textColor = new Color(255, 255, 255);
    private static final Color cardColor = new Color(44, 44, 46);

    // Main components
    public static JTabbedPane tabbedPane;
    private static ContactFrame contactFrame;
    private static MapPanel mapPanel;
    private static JPanel homePanel;

    // Contact management components
    private static JList<Contact> contactList;
    private static DefaultListModel<Contact> listModel;
    private static JPanel contactDetailsPanel;
    private static JPanel infoPanel;
    private static Contact selectedContact;

    /**
     * Creates the AddressBook panel for integration with phoneUtils
     * This is the main entry point called by phoneUtils
     */
    public static JPanel createAddressBook() {
        // Initialize the contact manager
        ContactManager.getInstance();

        // Create main panel with phone-compatible styling
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(backgroundColor);
        mainPanel.setPreferredSize(new Dimension(360, 580)); // Phone screen minus top/bottom bars

        // Create the tabbed interface
        createMainInterface();

        // Add the tabbed pane to main panel
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Load initial data
        loadInitialData();

        return mainPanel;
    }

    /**
     * Create the main tabbed interface adapted for phone screen
     */
    private static void createMainInterface() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(backgroundColor);
        tabbedPane.setForeground(textColor);
        tabbedPane.setFont(new Font("Inter", Font.PLAIN, 12));

        // Create individual panels
        createHomePanel();
        createMapPanel();

        // Add tabs with smaller font for phone
        tabbedPane.addTab("Home", createHomeIcon(), homePanel, "Application Home");
        tabbedPane.addTab("Contacts", createContactIcon(), createContactManagementPanel(), "Manage Contacts");
        tabbedPane.addTab("Map", createMapIcon(), mapPanel, "View Locations");
    }

    /**
     * Create the home panel adapted for phone screen
     */
    private static void createHomePanel() {
        homePanel = new JPanel(new BorderLayout());
        homePanel.setBackground(backgroundColor);

        // Title panel - smaller for phone
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(backgroundColor);
        JLabel titleLabel = new JLabel("Address Book");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 18));
        titleLabel.setForeground(textColor);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel);

        // Statistics panel - simplified for phone
        JPanel statsPanel = createStatsPanel();

        // Quick actions panel - simplified for phone
        JPanel actionsPanel = createQuickActionsPanel();

        homePanel.add(titlePanel, BorderLayout.NORTH);
        homePanel.add(statsPanel, BorderLayout.CENTER);
        homePanel.add(actionsPanel, BorderLayout.SOUTH);
    }

    /**
     * Create statistics panel adapted for phone screen
     */
    private static JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        statsPanel.setBackground(backgroundColor);

        // Total contacts
        JPanel totalContactsCard = createStatsCard("Contacts",
                String.valueOf(ContactManager.getContactsCount()), new Color(0, 122, 255));

        // Contacts with photos
        JPanel photosCard = createStatsCard("Photos", "0", new Color(52, 199, 89));

        // Contacts with addresses
        long addressCount = ContactManager.getContacts().stream()
                .mapToLong(c -> c.getAddress().isEmpty() ? 0 : 1).sum();
        JPanel addressCard = createStatsCard("Addresses",
                String.valueOf(addressCount), new Color(255, 149, 0));

        // Recent activity
        JPanel recentCard = createStatsCard("Recent", "Today", new Color(175, 82, 222));

        statsPanel.add(totalContactsCard);
        statsPanel.add(photosCard);
        statsPanel.add(addressCard);
        statsPanel.add(recentCard);

        return statsPanel;
    }

    /**
     * Create a statistics card adapted for phone screen
     */
    private static JPanel createStatsCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(cardColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(58, 58, 60), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Inter", Font.PLAIN, 10));
        titleLabel.setForeground(new Color(174, 174, 178));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Inter", Font.BOLD, 16));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    /**
     * Create quick actions panel adapted for phone screen
     */
    private static JPanel createQuickActionsPanel() {
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        actionsPanel.setBackground(backgroundColor);
        actionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton addContactBtn = createActionButton("Add", new Color(0, 122, 255));
        JButton viewMapBtn = createActionButton("Map", new Color(255, 149, 0));

        addContactBtn.addActionListener(e -> showAddContactDialog());
        viewMapBtn.addActionListener(e -> tabbedPane.setSelectedIndex(2));

        actionsPanel.add(addContactBtn);
        actionsPanel.add(viewMapBtn);

        return actionsPanel;
    }

    /**
     * Create action button adapted for phone screen
     */
    private static JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.PLAIN, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Create the contact management panel adapted for phone screen
     */
    private static JPanel createContactManagementPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.setBackground(backgroundColor);

        // Initialize contact list
        listModel = new DefaultListModel<>();
        contactList = new JList<>(listModel);
        contactList.setCellRenderer(new ContactCellRenderer());
        contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactList.setBackground(cardColor);
        contactList.setForeground(textColor);

        // Contact list panel - full width for phone
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(backgroundColor);

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBackground(backgroundColor);
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Inter", Font.PLAIN, 12));
        searchField.setBackground(cardColor);
        searchField.setForeground(textColor);
        searchField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(textColor);
        searchLabel.setFont(new Font("Inter", Font.PLAIN, 12));

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        listPanel.add(searchPanel, BorderLayout.NORTH);

        JScrollPane listScrollPane = new JScrollPane(contactList);
        listScrollPane.setPreferredSize(new Dimension(350, 200));
        listScrollPane.getViewport().setBackground(cardColor);
        listPanel.add(listScrollPane, BorderLayout.CENTER);

        // Contact details panel
        contactDetailsPanel = new JPanel();
        contactDetailsPanel.setLayout(new BoxLayout(contactDetailsPanel, BoxLayout.Y_AXIS));
        contactDetailsPanel.setBackground(backgroundColor);

        // Info panel
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(backgroundColor);

        JScrollPane detailsScrollPane = new JScrollPane(contactDetailsPanel);
        detailsScrollPane.setPreferredSize(new Dimension(350, 200));
        detailsScrollPane.getViewport().setBackground(backgroundColor);

        // Buttons panel - smaller buttons for phone
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        buttonsPanel.setBackground(backgroundColor);

        JButton saveBtn = createSmallButton("Save");
        JButton deleteBtn = createSmallButton("Delete");
        JButton addPhotoBtn = createSmallButton("Photo");
        JButton showMapBtn = createSmallButton("Map");

        buttonsPanel.add(saveBtn);
        buttonsPanel.add(deleteBtn);
        buttonsPanel.add(addPhotoBtn);
        buttonsPanel.add(showMapBtn);

        // Layout for phone - vertical stack
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(backgroundColor);
        topPanel.add(listPanel, BorderLayout.NORTH);
        topPanel.add(detailsScrollPane, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.CENTER);
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        // Setup contact list event handlers
        setupContactListHandlers(saveBtn, deleteBtn, addPhotoBtn, showMapBtn, searchField);

        return mainPanel;
    }

    /**
     * Create small button for phone interface
     */
    private static JButton createSmallButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.PLAIN, 10));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 122, 255));
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setFocusPainted(false);
        return button;
    }

    /**
     * Setup event handlers for contact management
     */
    private static void setupContactListHandlers(JButton saveBtn, JButton deleteBtn,
                                                 JButton addPhotoBtn, JButton showMapBtn,
                                                 JTextField searchField) {

        // Contact selection handler
        contactList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedContact = contactList.getSelectedValue();
                updateContactDetails(selectedContact);
                boolean hasSelection = selectedContact != null;
                saveBtn.setEnabled(hasSelection);
                deleteBtn.setEnabled(hasSelection);
                addPhotoBtn.setEnabled(hasSelection);
                showMapBtn.setEnabled(hasSelection && !selectedContact.getAddress().isEmpty());
            }
        });

        // Search functionality
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }

            private void search() {
                String query = searchField.getText();
                java.util.List<Contact> results = ContactManager.searchContacts(query);
                updateContactList(results);
            }
        });

        // Button handlers
        saveBtn.addActionListener(e -> saveCurrentContact());
        deleteBtn.addActionListener(e -> deleteCurrentContact());
        addPhotoBtn.addActionListener(e -> addPhotoToContact());
        showMapBtn.addActionListener(e -> showContactOnMap());
    }

    /**
     * Create the map panel
     */
    private static void createMapPanel() {
        mapPanel = new MapPanel();
        mapPanel.setBackground(backgroundColor);
    }

    /**
     * Load initial data
     */
    private static void loadInitialData() {
        updateContactList(ContactManager.getContacts());
    }

    /**
     * Update the contact list display
     */
    public static void updateContactList(java.util.List<Contact> contacts) {
        listModel.clear();
        contacts.forEach(listModel::addElement);
    }

    /**
     * Update contact details panel
     */
    private static void updateContactDetails(Contact contact) {
        if (contact == null) {
            contactDetailsPanel.removeAll();
            contactDetailsPanel.revalidate();
            contactDetailsPanel.repaint();
            return;
        }

        // Clear existing components
        contactDetailsPanel.removeAll();

        // Create photo panel
        JPanel photoPanel = new JPanel(new BorderLayout());
        photoPanel.setBackground(cardColor);

        if (contact != null && contact.getPhoto() != null && contact.getPhoto().getImage() != null) {
            try {
                ImageIcon icon = new ImageIcon(String.valueOf(contact.getPhoto()));
                Image image = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                JLabel photoLabel = new JLabel(new ImageIcon(image));
                photoLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));
                photoPanel.add(photoLabel, BorderLayout.WEST);
            } catch (Exception e) {
                System.err.println("Error loading profile photo: " + e.getMessage());
            }
        }

        // Add contact fields directly to contactDetailsPanel
        addContactField(contactDetailsPanel, "First Name:", contact.getFirstName());
        addContactField(contactDetailsPanel, "Last Name:", contact.getLastName());
        addContactField(contactDetailsPanel, "Phone:", contact.getPhone());
        addContactField(contactDetailsPanel, "Email:", contact.getEmail());
        addContactField(contactDetailsPanel, "Address:", contact.getAddress());
        addContactField(contactDetailsPanel, "Birth Date:", contact.getBirthDate());

        // Add everything to the main panel
        contactDetailsPanel.add(photoPanel, BorderLayout.WEST);

        contactDetailsPanel.revalidate();
        contactDetailsPanel.repaint();
    }

    /**
     * Add a contact field to the contact details panel
     */
    private static void addContactField(JPanel panel, String label, String value) {
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        fieldPanel.setBackground(backgroundColor);

        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setForeground(textColor);
        fieldLabel.setFont(new Font("Inter", Font.PLAIN, 12));

        JTextField textField = new JTextField(value);
        textField.setFont(new Font("Inter", Font.PLAIN, 12));
        textField.setBackground(cardColor);
        textField.setForeground(textColor);
        textField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        fieldPanel.add(fieldLabel);
        fieldPanel.add(textField);

        panel.add(fieldPanel);
    }

    /**
     * Save current contact
     */
    private static void saveCurrentContact() {
        if (selectedContact == null) return;

        try {
            Contact updatedContact = createUpdatedContactFromForm();
            int index = ContactManager.getContactIndex(selectedContact);

            if (ContactManager.updateContact(index, updatedContact)) {
                updateContactList(ContactManager.getContacts());
                JOptionPane.showMessageDialog(null, "Contact saved successfully!");
                refreshHomeStats();
            } else {
                JOptionPane.showMessageDialog(null, "Error saving contact!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static Contact createUpdatedContactFromForm() {
        try {
            // Récupérer les valeurs depuis les champs de texte dans contactDetailsPanel
            String firstName = "";
            String lastName = "";
            String phone = "";
            String email = "";
            String address = "";
            String birthDate = "";

            // Parcourir les composants pour extraire les valeurs
            Component[] components = contactDetailsPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    Component[] subComponents = panel.getComponents();
                    for (Component subComp : subComponents) {
                        if (subComp instanceof JPanel) {
                            // C'est probablement un fieldPanel
                            JPanel fieldPanel = (JPanel) subComp;
                            Component[] fieldComponents = fieldPanel.getComponents();
                            if (fieldComponents.length >= 2) {
                                JLabel label = (JLabel) fieldComponents[0];
                                JTextField textField = (JTextField) fieldComponents[1];

                                String labelText = label.getText();
                                String value = textField.getText();

                                switch (labelText) {
                                    case "First Name:":
                                        firstName = value;
                                        break;
                                    case "Last Name:":
                                        lastName = value;
                                        break;
                                    case "Phone:":
                                        phone = value;
                                        break;
                                    case "Email:":
                                        email = value;
                                        break;
                                    case "Address:":
                                        address = value;
                                        break;
                                    case "Birth Date:":
                                        birthDate = value;
                                        break;
                                }
                            }
                        }
                    }
                }
            }

            return new Contact(firstName + " " + lastName, phone, firstName, lastName,
                    birthDate, address, email, selectedContact.getPhoto());

        } catch (Exception e) {
            System.err.println("Error creating updated contact: " + e.getMessage());
            // Retourner le contact original en cas d'erreur
            return selectedContact;
        }
    }

    private static String getFieldValue(Component[] components, int fieldIndex) {
        if (fieldIndex < components.length) {
            JPanel fieldPanel = (JPanel) components[fieldIndex];
            JTextField textField = (JTextField) fieldPanel.getComponent(1);
            return textField.getText();
        }
        return "";
    }

    private static void deleteCurrentContact() {
        if (selectedContact == null) return;

        int result = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to delete this contact?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            if (ContactManager.deleteContact(selectedContact)) {
                updateContactList(ContactManager.getContacts());
                contactDetailsPanel.removeAll();
                JLabel deletedLabel = new JLabel("Contact deleted");
                deletedLabel.setForeground(textColor);
                contactDetailsPanel.add(deletedLabel);
                contactDetailsPanel.revalidate();
                contactDetailsPanel.repaint();
                refreshHomeStats();
                JOptionPane.showMessageDialog(null, "Contact deleted successfully!");
            }
        }
    }

    private static void addPhotoToContact() {
        if (selectedContact == null) return;

        GalleryFrame galleryFrame = new GalleryFrame(selectedContact);
        galleryFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                updateContactDetails(selectedContact);
                updateContactList(ContactManager.getContacts());
                refreshHomeStats();
            }
        });
    }

    private static void showContactOnMap() {
        if (selectedContact == null || selectedContact.getAddress().isEmpty()) return;

        mapPanel.showAddressOnMap(selectedContact.getAddress());
        tabbedPane.setSelectedIndex(2); // Switch to map tab
    }

    private static void showAddContactDialog() {
        // Créer un nouveau panel pour l'onglet d'ajout
        JPanel addContactPanel = new JPanel(new BorderLayout());
        addContactPanel.setBackground(backgroundColor);

        // Créer et configurer le dialogue d'ajout
        AddContactDialog dialog = new AddContactDialog(null);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        // Ajouter le dialogue au panel
        addContactPanel.add(dialog, BorderLayout.CENTER);

        // Ajouter l'onglet
        int index = tabbedPane.getTabCount();
        tabbedPane.addTab("Add Contact", createContactIcon(), addContactPanel, "Add a new contact");
        tabbedPane.setSelectedIndex(index);

        // Gérer la fermeture du dialogue
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                // Supprimer l'onglet lors de la fermeture
                tabbedPane.removeTabAt(index);

                // Si un contact a été créé, le sauvegarder
                if (dialog.getContact() != null) {
                    Contact newContact = dialog.getContact();
                    if (ContactManager.addContact(newContact)) {
                        updateContactList(ContactManager.getContacts());
                        JOptionPane.showMessageDialog(null, "Contact added successfully!");
                        refreshHomeStats();
                    } else {
                        JOptionPane.showMessageDialog(null, "Error adding contact!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    public static void refreshHomeStats() {
        createHomePanel();
        if (tabbedPane.getTabCount() > 0) {
            tabbedPane.setComponentAt(0, homePanel);
        }
    }

    private static Icon createHomeIcon() {
        return new ImageIcon(); // You can add actual icons here
    }

    private static Icon createContactIcon() {
        return new ImageIcon(); // You can add actual icons here
    }

    private static Icon createMapIcon() {
        return new ImageIcon(); // You can add actual icons here
    }
}