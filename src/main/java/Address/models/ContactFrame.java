package Address.models;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ContactFrame extends JFrame {
    private JList<Contact> contactList;
    private DefaultListModel<Contact> listModel;
    private JPanel contactPanel;
    private JButton saveButton;
    private Contact selectedContact;

    public ContactFrame() {
        setTitle("Contacts");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeComponents();
        setupLayout();
        addListeners();
    }

    private void initializeComponents() {
        listModel = new DefaultListModel<>();
        contactList = new JList<>(listModel);
        contactList.setCellRenderer(new ContactCellRenderer());

        contactPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        contactPanel.add(new JLabel("Select a contact to modify"));
        saveButton = new JButton("Save");
        saveButton.setEnabled(false);
        saveButton.setVisible(true); // Assurer que le bouton est visible

        // Initialize list with contacts
        ContactManager.getContacts().forEach(listModel::addElement);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // Left panel with contact list
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Contacts"));
        leftPanel.add(new JScrollPane(contactList), BorderLayout.CENTER);

        // Right panel with contact details
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Contact Details"));
        
        // Créer un panel pour le bouton de sauvegarde
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        
        // Ajouter le panel des détails et le panel du bouton
        rightPanel.add(contactPanel, BorderLayout.CENTER);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    private void addListeners() {
        contactList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = contactList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        selectedContact = contactList.getModel().getElementAt(index);
                        updateContactDetails(selectedContact);
                        saveButton.setEnabled(true); // Activer le bouton Save
                    }
                }
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedContact != null) {
                    try {
                        // Récupérer les données modifiées
                        Contact updatedContact = createUpdatedContact();

                        // Mettre à jour le contact dans le modèle
                        int index = listModel.indexOf(selectedContact);
                        if (ContactManager.updateContact(index, updatedContact)) {
                            listModel.setElementAt(updatedContact, index);
                            selectedContact = updatedContact;

                            // Afficher un message de confirmation
                            JOptionPane.showMessageDialog(ContactFrame.this, "Contact sauvegardé avec succès !");
                        } else {
                            JOptionPane.showMessageDialog(ContactFrame.this, "Erreur lors de la sauvegarde du contact.", "Erreur", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ContactFrame.this, "Une erreur est survenue : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    private void updateContactDetails(Contact contact) {
        contactPanel.removeAll();

        JTextField nameField = new JTextField(contact.getName());
        JTextField phoneField = new JTextField(contact.getPhone());
        JTextField firstNameField = new JTextField(contact.getFirstName());
        JTextField lastNameField = new JTextField(contact.getLastName());
        JTextField birthDateField = new JTextField(contact.getBirthDate());
        JTextField addressField = new JTextField(contact.getAddress());
        JTextField emailField = new JTextField(contact.getEmail());

        contactPanel.add(new JLabel("Name:"));
        contactPanel.add(nameField);
        contactPanel.add(new JLabel("Phone:"));
        contactPanel.add(phoneField);
        contactPanel.add(new JLabel("First Name:"));
        contactPanel.add(firstNameField);
        contactPanel.add(new JLabel("Last Name:"));
        contactPanel.add(lastNameField);
        contactPanel.add(new JLabel("Birth Date:"));
        contactPanel.add(birthDateField);
        contactPanel.add(new JLabel("Address:"));
        contactPanel.add(addressField);
        contactPanel.add(new JLabel("Email:"));
        contactPanel.add(emailField);

        contactPanel.revalidate();
        contactPanel.repaint();
    }

    private Contact createUpdatedContact() {
        Component[] components = contactPanel.getComponents();
        // Récupère les champs dans l'ordre où tu les ajoutes dans contactPanel
        JTextField nameField = (JTextField) components[1];
        JTextField phoneField = (JTextField) components[3];
        JTextField firstNameField = (JTextField) components[5];
        JTextField lastNameField = (JTextField) components[7];
        JTextField birthDateField = (JTextField) components[9];
        JTextField addressField = (JTextField) components[11];
        JTextField emailField = (JTextField) components[13];

        return new Contact(
                nameField.getText(),
                phoneField.getText(),
                firstNameField.getText(),
                lastNameField.getText(),
                birthDateField.getText(),
                addressField.getText(),
                emailField.getText(),
                null // ou la gestion de la photo si tu l'ajoutes
        );

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ContactManager.getInstance(); // Initialize singleton
            ContactFrame frame = new ContactFrame();
            frame.setVisible(true);
        });
    }
}
