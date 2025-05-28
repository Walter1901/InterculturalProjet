package Address;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;

import Address.models.Contact;
import Address.models.ContactManager;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;

public class AddressBook {

    private static DefaultListModel<Contact> contactListModel;
    private static ImageIcon contactIcon;
    private static JPanel mainPanel;

    public static JPanel createAddressBook() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(phoneUtils.backgroundColor);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(phoneUtils.backgroundColor);

        // Header Panel with Title and Add Button (iOS style)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(phoneUtils.backgroundColor);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Titre
        JLabel titleLabel = new JLabel("Address Book", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SF Pro Display", Font.BOLD, 20));
        titleLabel.setForeground(phoneUtils.textColor);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Bouton Add (+) style iOS
        JButton addButton = new JButton("+");
        addButton.setFont(new Font("SF Pro Display", Font.BOLD, 20));
        addButton.setForeground(new Color(0, 122, 255));  // Couleur bleue iOS
        addButton.setBackground(phoneUtils.backgroundColor);
        addButton.setBorderPainted(false);
        addButton.setFocusPainted(false);
        addButton.setPreferredSize(new Dimension(40, 30));
        headerPanel.add(addButton, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Chargement de l'icône générique
        ImageIcon contactIcon = new ImageIcon("src/main/resources/homescreenIcons/addressBookIcon.png");
        Image img = contactIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        AddressBook.contactIcon = new ImageIcon(img);

        // Liste des contacts avec style iOS
        contactListModel = new DefaultListModel<>();
        JList<Contact> contactList = new JList<>(contactListModel);
        contactList.setCellRenderer(new ContactCellRenderer(contactIcon));
        contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactList.setFixedCellHeight(65);  // Cellules plus hautes style iOS

        // Style iOS de la liste
        JScrollPane scrollPane = new JScrollPane(contactList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(phoneUtils.backgroundColor);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel pour la recherche style iOS
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(phoneUtils.backgroundColor);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));

        JTextField searchField = new JTextField("Search");
        searchField.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        searchField.setForeground(Color.GRAY);
        searchField.setBackground(new Color(230, 230, 230));  // Gris clair iOS
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        // Focus listener pour effacer "Search" quand le champ est sélectionné
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });

        searchPanel.add(searchField, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.SOUTH);

        // Actions pour la liste de contacts

        // Double-clic pour modifier un contact (style iOS)
        contactList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = contactList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        Contact selectedContact = contactListModel.getElementAt(index);
                        openEditContactWindow(selectedContact, contactList);
                    }
                }
            }
        });

        // Synchroniser la liste avec ContactManager
        contactList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = contactList.getSelectedIndex();
                if (index != -1) {
                    Contact selectedContact = contactListModel.getElementAt(index);
                    ContactManager.updateContact(index, selectedContact);
                }
            }
        });

        // Menu contextuel style iOS avec swipe et longpress
        contactList.addMouseListener(new MouseAdapter() {
            private Point pressPoint;
            private Timer longPressTimer;

            @Override
            public void mousePressed(MouseEvent e) {
                pressPoint = e.getPoint();

                // Configurer timer pour longpress
                longPressTimer = new Timer(600, _ -> {
                    int index = contactList.locationToIndex(pressPoint);
                    if (index != -1) {
                        Contact selectedContact = contactListModel.getElementAt(index);
                        showContextMenu(selectedContact, contactList, e.getPoint());
                    }
                });
                longPressTimer.setRepeats(false);
                longPressTimer.start();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (longPressTimer != null) {
                    longPressTimer.stop();
                }
            }
        });

        // Action pour le bouton d'ajout
        addButton.addActionListener(e -> {
            openAddContactWindow();
            // Sauvegarder après l'ajout
            ContactManager.saveContacts();
        });

        // Charger les contacts depuis le fichier JSON
        ContactManager.loadContacts();

        for (Contact c: ContactManager.getContacts()) {
            Contact newContact = new Contact(
                    c.getName(),
                    c.getPhone(),
                    c.getFirstName(),
                    c.getLastName(),
                    c.getBirthDate(),
                    c.getAddress(),
                    c.getEmail(),
                    c.getPhoto()
            );
            contactListModel.addElement(newContact);
        }
        return panel;
    }

    // Méthodes d'interface utilisateur style iOS

    // Ouvrir la fenêtre d'ajout de contact
    private static void openAddContactWindow() {
        // Créer une nouvelle fenêtre pour l'ajout de contact
        JFrame addContactFrame = new JFrame("New Contact");
        addContactFrame.setSize(400, 700);
        addContactFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addContactFrame.setLocationRelativeTo(mainPanel);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(phoneUtils.backgroundColor);

        // En-tête avec boutons Annuler et Créer
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(phoneUtils.backgroundColor);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("SF Pro Text", Font.PLAIN, 16));
        cancelButton.setForeground(new Color(0, 122, 255));
        cancelButton.setBackground(phoneUtils.backgroundColor);
        cancelButton.setBorderPainted(false);
        cancelButton.setFocusPainted(false);
        headerPanel.add(cancelButton, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("New Contact", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SF Pro Display", Font.BOLD, 18));
        titleLabel.setForeground(phoneUtils.textColor);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JButton createButton = new JButton("Done");
        createButton.setFont(new Font("SF Pro Text", Font.BOLD, 16));
        createButton.setForeground(new Color(0, 122, 255));
        createButton.setBackground(phoneUtils.backgroundColor);
        createButton.setBorderPainted(false);
        createButton.setFocusPainted(false);
        headerPanel.add(createButton, BorderLayout.EAST);

        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // Champs de formulaire
        JPanel formPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, 700);
            }
        };
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(phoneUtils.backgroundColor);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel de défilement pour les champs
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Photo/image
        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        photoPanel.setBackground(phoneUtils.backgroundColor);

        // Image par défaut avec taille plus grande (style iOS)
        ImageIcon defaultIcon = new ImageIcon("src/main/resources/homescreenIcons/contactDefault.png");
        Image scaledImg = defaultIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImg);

        JLabel photoLabel = new JLabel(scaledIcon);
        photoLabel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        photoPanel.add(photoLabel);

        // Panel séparé pour le bouton "add photo"
        JPanel addPhotoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addPhotoPanel.setBackground(phoneUtils.backgroundColor);
        JButton addPhotoButton = new JButton("add photo");
        addPhotoButton.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        addPhotoButton.setForeground(new Color(0, 122, 255));
        addPhotoButton.setBackground(phoneUtils.backgroundColor);
        addPhotoButton.setBorderPainted(false);
        addPhotoButton.setFocusPainted(false);
        addPhotoPanel.add(addPhotoButton);

        formPanel.add(photoPanel);
        formPanel.add(addPhotoPanel);
        formPanel.add(Box.createVerticalStrut(20));

        // Champ prénom
        JPanel firstNamePanel = createFormField("First Name");
        JTextField firstNameField = (JTextField) firstNamePanel.getComponent(1);
        formPanel.add(firstNamePanel);
        formPanel.add(Box.createVerticalStrut(1));

        // Champ nom
        JPanel lastNamePanel = createFormField("Last Name");
        JTextField lastNameField = (JTextField) lastNamePanel.getComponent(1);
        formPanel.add(lastNamePanel);
        formPanel.add(Box.createVerticalStrut(1));

        // Champ téléphone
        JPanel phonePanel = createFormField("Phone");
        JTextField phoneField = (JTextField) phonePanel.getComponent(1);
        formPanel.add(phonePanel);
        formPanel.add(Box.createVerticalStrut(1));

        // Champ email
        JPanel emailPanel = createFormField("Email");
        JTextField emailField = (JTextField) emailPanel.getComponent(1);
        formPanel.add(emailPanel);
        formPanel.add(Box.createVerticalStrut(1));

        // Champ date de naissance
        JPanel birthDatePanel = createFormField("Birth Date (DD/MM/YYYY)");
        JTextField birthDateField = (JTextField) birthDatePanel.getComponent(1);
        try {
            MaskFormatter dateMask = new MaskFormatter("##/##/####");
            dateMask.setPlaceholderCharacter('_');
            JFormattedTextField formattedBirthDateField = new JFormattedTextField(dateMask);
            formattedBirthDateField.setFont(new Font("SF Pro Text", Font.PLAIN, 16));
            formattedBirthDateField.setBorder(null);
            birthDatePanel.remove(birthDateField);
            birthDatePanel.add(formattedBirthDateField, BorderLayout.CENTER);
            birthDateField = formattedBirthDateField;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erreur lors de la saisie de la date de naissance.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        formPanel.add(birthDatePanel);
        formPanel.add(Box.createVerticalStrut(1));

        // Champ adresse
        JPanel addressPanel = createFormField("Address");
        JTextField addressField = (JTextField) addressPanel.getComponent(1);
        formPanel.add(addressPanel);

        // Actions des boutons
        cancelButton.addActionListener(_ -> addContactFrame.dispose());

        // Action pour le bouton "add photo" (simulation)
        addPhotoButton.addActionListener(_ -> JOptionPane.showMessageDialog(addContactFrame, "Photo selection dialog would appear here.",
                "Select Photo", JOptionPane.INFORMATION_MESSAGE));

        JTextField finalBirthDateField = birthDateField;
        createButton.addActionListener(_ -> {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String fullName = firstName + " " + lastName;
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String birthDate = finalBirthDateField.getText().trim();
            String address = addressField.getText().trim();

            if (!firstName.isEmpty() && !lastName.isEmpty()) {
                Contact newContact = new Contact(fullName, phone, firstName, lastName, birthDate, address, email, null);
                ContactManager.addContact(newContact);
                contactListModel.addElement(newContact);
                addContactFrame.dispose();
            } else {
                JPanel errorPanel = firstName.isEmpty() ? firstNamePanel : lastNamePanel;
                shakeComponent(errorPanel);
                JOptionPane.showMessageDialog(addContactFrame, "First name and last name are required.",
                        "Required Fields", JOptionPane.WARNING_MESSAGE);
            }
        });

        addContactFrame.setContentPane(contentPanel);
        addContactFrame.setVisible(true);
    }

    // Ouvrir la fenêtre de modification d'un contact
    private static void openEditContactWindow(Contact contact, JList<Contact> contactList) {

        JFrame editContactFrame = new JFrame("Edit Contact");
        editContactFrame.setSize(400, 700);
        editContactFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editContactFrame.setLocationRelativeTo(mainPanel);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(phoneUtils.backgroundColor);

        // En-tête avec boutons Annuler et Sauvegarder
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(phoneUtils.backgroundColor);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("SF Pro Text", Font.PLAIN, 16));
        cancelButton.setForeground(new Color(0, 122, 255));
        cancelButton.setBackground(phoneUtils.backgroundColor);
        cancelButton.setBorderPainted(false);
        cancelButton.setFocusPainted(false);
        headerPanel.add(cancelButton, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("Edit Contact", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SF Pro Display", Font.BOLD, 18));
        titleLabel.setForeground(phoneUtils.textColor);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Done");
        saveButton.setFont(new Font("SF Pro Text", Font.BOLD, 16));
        saveButton.setForeground(new Color(0, 122, 255));
        saveButton.setBackground(phoneUtils.backgroundColor);
        saveButton.setBorderPainted(false);
        saveButton.setFocusPainted(false);
        headerPanel.add(saveButton, BorderLayout.EAST);

        // Champs de formulaire
        JPanel formPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, 700); // Taille pour accommoder tous les champs
            }
        };
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(phoneUtils.backgroundColor);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel de défilement pour les champs
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        editContactFrame.add(scrollPane, BorderLayout.CENTER);
        JButton Loc = new JButton("Localisation");
        Loc.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        Loc.setForeground(new Color(0, 122, 255));
        Loc.setBackground(phoneUtils.backgroundColor);
        Loc.setBorderPainted(false);
        Loc.setFocusPainted(false);
        Loc.setCursor(new Cursor(Cursor.HAND_CURSOR));
        formPanel.add(Loc);

        Loc.addActionListener(_ -> {
            JDialog mapDialog = new JDialog((Frame) null, "Localisation", true);
            mapDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            mapDialog.setSize(600, 400); // Taille de la fenêtre
            mapDialog.setLocationRelativeTo(null);

            MapPanel mapPanel = new MapPanel();
            mapPanel.showAddressOnMap(contact.getAddress());

            // Rendre la carte navigable
            PanMouseInputListener pan = new PanMouseInputListener(mapPanel.getMapViewer());
            mapPanel.getMapViewer().addMouseListener(pan);
            mapPanel.getMapViewer().addMouseMotionListener(pan);
            mapPanel.getMapViewer().addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapPanel.getMapViewer()));

            mapDialog.add(mapPanel);
            mapDialog.setVisible(true);
        });

        // Photo/image
        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        photoPanel.setBackground(phoneUtils.backgroundColor);

        // Image par défaut avec taille plus grande (style iOS)
        ImageIcon defaultIcon = new ImageIcon("src/main/resources/homescreenIcons/contactDefault.png");
        Image scaledImg = defaultIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImg);

        JLabel photoLabel = new JLabel(scaledIcon);
        photoLabel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        photoPanel.add(photoLabel);

        // Panel séparé pour le bouton "add photo"
        JPanel addPhotoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addPhotoPanel.setBackground(phoneUtils.backgroundColor);
        JButton changePhotoButton = new JButton("change photo");
        changePhotoButton.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        changePhotoButton.setForeground(new Color(0, 122, 255));
        changePhotoButton.setBackground(phoneUtils.backgroundColor);
        changePhotoButton.setBorderPainted(false);
        changePhotoButton.setFocusPainted(false);
        changePhotoButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addPhotoPanel.add(changePhotoButton);

        formPanel.add(photoPanel);
        formPanel.add(addPhotoPanel);
        formPanel.add(Box.createVerticalStrut(20));

        // Extraire le prénom et le nom de la personne
        String firstName = contact.getFirstName();
        String lastName = contact.getLastName();

// Correction : gérer le cas où firstName ou lastName est null
        if ((firstName == null || firstName.isEmpty()) && (lastName == null || lastName.isEmpty()) && contact.getName() != null && contact.getName().contains(" ")) {
            String[] nameParts = contact.getName().split(" ", 2);
            firstName = nameParts[0];
            lastName = nameParts.length > 1 ? nameParts[1] : "";
        }
        if (firstName == null) firstName = "";
        if (lastName == null) lastName = "";
        if (contact.getName() == null) contact.setName("");
        if (contact.getPhone() == null) contact.setPhone("");
        if (contact.getEmail() == null) contact.setEmail("");
        if (contact.getBirthDate() == null) contact.setBirthDate("");
        if (contact.getAddress() == null) contact.setAddress("");

        // Si les champs sont vides (pour la compatibilité avec les anciens contacts)
        if (firstName.isEmpty() && lastName.isEmpty() && contact.getName().contains(" ")) {
            String[] nameParts = contact.getName().split(" ", 2);
            firstName = nameParts[0];
            lastName = nameParts.length > 1 ? nameParts[1] : "";
        }

        // Champ prénom
        JPanel firstNamePanel = createFormField("First Name");
        JTextField firstNameField = (JTextField) firstNamePanel.getComponent(1);
        firstNameField.setText(firstName);
        formPanel.add(firstNamePanel);
        formPanel.add(Box.createVerticalStrut(1));

        // Champ nom
        JPanel lastNamePanel = createFormField("Last Name");
        JTextField lastNameField = (JTextField) lastNamePanel.getComponent(1);
        lastNameField.setText(lastName);
        formPanel.add(lastNamePanel);
        formPanel.add(Box.createVerticalStrut(1));

        // Champ téléphone
        JPanel phonePanel = createFormField("Phone");
        JTextField phoneField = (JTextField) phonePanel.getComponent(1);
        phoneField.setText(contact.getPhone());
        formPanel.add(phonePanel);
        formPanel.add(Box.createVerticalStrut(1));

        // Champ email
        JPanel emailPanel = createFormField("Email");
        JTextField emailField = (JTextField) emailPanel.getComponent(1);
        emailField.setText(contact.getEmail());
        formPanel.add(emailPanel);
        formPanel.add(Box.createVerticalStrut(1));

        // Champ date de naissance
        JPanel birthDatePanel = createFormField("Birth Date (DD/MM/YYYY)");
        JTextField birthDateField = (JTextField) birthDatePanel.getComponent(1);
        // Formatter pour la date
        try {
            MaskFormatter dateMask = new MaskFormatter("##/##/####");
            dateMask.setPlaceholderCharacter('_');
            JFormattedTextField formattedBirthDateField = new JFormattedTextField(dateMask);
            formattedBirthDateField.setFont(new Font("SF Pro Text", Font.PLAIN, 16));
            formattedBirthDateField.setBorder(null);
            formattedBirthDateField.setValue(contact.getBirthDate().isEmpty() ? "  /  /    " : contact.getBirthDate());
            birthDatePanel.remove(birthDateField);
            birthDatePanel.add(formattedBirthDateField, BorderLayout.CENTER);
            birthDateField = formattedBirthDateField;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erreur lors de la saisie de la date de naissance.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        formPanel.add(birthDatePanel);
        formPanel.add(Box.createVerticalStrut(1));

        // Champ adresse
        JPanel addressPanel = createFormField("Address");
        JTextField addressField = (JTextField) addressPanel.getComponent(1);
        addressField.setText(contact.getAddress());
        formPanel.add(addressPanel);

        // Bouton Supprimer en bas (rouge)
        JPanel deletePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        deletePanel.setBackground(phoneUtils.backgroundColor);
        deletePanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        JButton deleteButton = new JButton("Delete Contact");
        deleteButton.setFont(new Font("SF Pro Text", Font.BOLD, 16));
        deleteButton.setForeground(Color.RED);
        deleteButton.setBackground(new Color(240, 240, 240));
        deleteButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        deletePanel.add(deleteButton);
        formPanel.add(deletePanel);

        // Actions des boutons
        cancelButton.addActionListener(_ -> editContactFrame.dispose());

        // Action pour le bouton "change photo" (simulation)
        changePhotoButton.addActionListener(_ -> JOptionPane.showMessageDialog(editContactFrame, "Photo selection dialog would appear here.",
                "Change Photo", JOptionPane.INFORMATION_MESSAGE));

        JTextField finalBirthDateField = birthDateField;
        saveButton.addActionListener(_ -> {
            String newFirstName = firstNameField.getText().trim();
            String newLastName = lastNameField.getText().trim();
            String newFullName = newFirstName + " " + newLastName;
            String newPhone = phoneField.getText().trim();
            String newEmail = emailField.getText().trim();
            String newBirthDate = finalBirthDateField.getText().trim();
            String newAddress = addressField.getText().trim();

            if (!newFirstName.isEmpty() && !newLastName.isEmpty()) {
                contact.setName(newFullName);
                contact.setFirstName(newFirstName);
                contact.setLastName(newLastName);
                contact.setPhone(newPhone);
                contact.setEmail(newEmail);
                contact.setBirthDate(newBirthDate);
                contact.setAddress(newAddress);
                contactList.repaint();

            } else {
                // Animation de secousse pour indiquer l'erreur (style iOS)
                JPanel errorPanel = newFirstName.isEmpty() ? firstNamePanel : lastNamePanel;
                shakeComponent(errorPanel);
                shakeComponent(errorPanel);
                JOptionPane.showMessageDialog(editContactFrame, "First name and last name are required.",
                        "Required Fields", JOptionPane.WARNING_MESSAGE);
            }
        });

        deleteButton.addActionListener(_ -> {
            // Animation de confirmation style iOS
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setBackground(Color.RED);

            Timer timer = new Timer(300, _ -> {
                contactListModel.removeElement(contact);
            });
            timer.setRepeats(false);
            timer.start();
        });

        editContactFrame.setSize(350, 600);
        editContactFrame.setLocationRelativeTo(null);
        editContactFrame.setVisible(true);
    }

    // Afficher le menu contextuel pour un contact
    private static void showContextMenu(Contact contact, JList<Contact> contactList, Point location) {
        JPopupMenu contextMenu = new JPopupMenu();
        contextMenu.setBackground(new Color(250, 250, 250));
        contextMenu.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));

        JMenuItem editItem = new JMenuItem("Edit");
        editItem.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        editItem.setForeground(Color.BLACK);
        editItem.addActionListener(_ -> openEditContactWindow(contact, contactList));

        JMenuItem callItem = new JMenuItem("Call");
        callItem.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        callItem.setForeground(new Color(0, 122, 255));
        callItem.addActionListener(_ -> JOptionPane.showMessageDialog(null, "Calling " + contact.getName() + "..."));

        JMenuItem messageItem = new JMenuItem("Message");
        messageItem.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        messageItem.setForeground(new Color(0, 122, 255));
        messageItem.addActionListener(_ -> JOptionPane.showMessageDialog(null, "Messaging " + contact.getName() + "..."));

        JMenuItem emailItem = new JMenuItem("Email");
        emailItem.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        emailItem.setForeground(new Color(0, 122, 255));
        emailItem.addActionListener(_ -> {
            if (contact.getEmail() != null && !contact.getEmail().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Sending email to " + contact.getEmail() + "...");
            } else {
                JOptionPane.showMessageDialog(null, "No email address available for " + contact.getName());
            }
        });

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        deleteItem.setForeground(Color.RED);
        deleteItem.addActionListener(_ -> {
            contactListModel.removeElement(contact);
            ContactManager.saveContacts();
        });

        contextMenu.add(callItem);
        contextMenu.add(messageItem);
        contextMenu.add(emailItem);
        contextMenu.addSeparator();
        contextMenu.add(editItem);
        contextMenu.addSeparator();
        contextMenu.add(deleteItem);

        contextMenu.show(contactList, location.x, location.y);
    }

    // Créer un champ de formulaire style iOS
    private static JPanel createFormField(String labelText) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        label.setForeground(Color.GRAY);
        label.setPreferredSize(new Dimension(120, 25));

        JTextField textField = new JTextField();
        textField.setFont(new Font("SF Pro Text", Font.PLAIN, 16));
        textField.setBorder(null);

        panel.add(label, BorderLayout.WEST);
        panel.add(textField, BorderLayout.CENTER);

        return panel;
    }

    // Animation de secousse pour indiquer une erreur (style iOS)
    private static void shakeComponent(Component component) {
        final int[] moves = {-5, 5, -5, 5, -3, 3, -2, 2, -1, 1, 0};
        final int delay = 50;
        final Point originalLocation = component.getLocation();

        Timer timer = new Timer(delay, null);
        timer.addActionListener(new ActionListener() {
            int index = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (index < moves.length) {
                    int offset = moves[index++];
                    component.setLocation(new Point(originalLocation.x + offset, originalLocation.y));
                } else {
                    component.setLocation(originalLocation);
                    timer.stop();
                }
            }
        });
        timer.start();
    }

    public static ImageIcon getContactIcon() {
        return contactIcon;
    }


    // Custom renderer style iOS
    static class ContactCellRenderer extends JPanel implements ListCellRenderer<Contact> {
        private final JLabel iconLabel = new JLabel();
        private final JLabel nameLabel = new JLabel();
        private final JLabel phoneLabel = new JLabel();
        private final ImageIcon contactIcon;

        public ContactCellRenderer(ImageIcon icon) {
            this.contactIcon = icon;
            setLayout(new BorderLayout(10, 5));

            // Panel pour l'icône
            JPanel iconPanel = new JPanel(new BorderLayout());
            iconPanel.setOpaque(false);
            iconPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
            iconLabel.setPreferredSize(new Dimension(40, 40));
            iconPanel.add(iconLabel, BorderLayout.CENTER);

            // Panel pour le texte
            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);
            textPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

            nameLabel.setFont(new Font("SF Pro Text", Font.PLAIN, 16));
            phoneLabel.setFont(new Font("SF Pro Text", Font.PLAIN, 12));
            phoneLabel.setForeground(Color.GRAY);

            textPanel.add(nameLabel);
            textPanel.add(phoneLabel);

            // Chevron iOS
            JLabel arrowLabel = new JLabel("›");
            arrowLabel.setFont(new Font("SF Pro Text", Font.PLAIN, 24));
            arrowLabel.setForeground(new Color(180, 180, 180));
            arrowLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            arrowLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 15));

            add(iconPanel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
            add(arrowLabel, BorderLayout.EAST);

            // Ligne de séparation style iOS
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Contact> list, Contact contact, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            iconLabel.setIcon(contactIcon);
            nameLabel.setText(contact.getName());
            phoneLabel.setText(contact.getPhone());

            if (isSelected) {
                setBackground(new Color(217, 217, 217));  // Gris sélection iOS
                nameLabel.setForeground(list.getForeground());
                phoneLabel.setForeground(new Color(100, 100, 100));
            } else {
                setBackground(list.getBackground());
                nameLabel.setForeground(list.getForeground());
                phoneLabel.setForeground(Color.GRAY);
            }

            return this;
        }
    }
}