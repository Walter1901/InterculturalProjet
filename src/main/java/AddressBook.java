import javax.swing.*;
import java.awt.*;


public class AddressBook {
    private static DefaultListModel<Contact> contactListModel;
    private static ImageIcon contactIcon;

    public static JPanel createAddressBook() {
        JPanel addressBookApp = new JPanel(new BorderLayout());
        addressBookApp.setBackground(phoneUtils.backgroundColor);

        // Titre
        JLabel titleLabel = new JLabel("Address Book", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 20));
        titleLabel.setForeground(phoneUtils.textColor);
        addressBookApp.add(titleLabel, BorderLayout.NORTH);

        // Chargement de l'icône générique
        contactIcon = new ImageIcon("src/main/resources/homescreenIcons/addressBookIcon.png");
        Image img = contactIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        contactIcon = new ImageIcon(img);

        // Liste des contacts
        contactListModel = new DefaultListModel<>();
        JList<Contact> contactList = new JList<>(contactListModel);
        contactList.setCellRenderer(new ContactCellRenderer());
        JScrollPane scrollPane = new JScrollPane(contactList);
        addressBookApp.add(scrollPane, BorderLayout.CENTER);

        // Bouton "Add Contact"
        JButton addButton = new JButton("Add Contact");
        addButton.setFont(new Font("Inter", Font.PLAIN, 16));
        addButton.addActionListener(e -> showAddContactDialog());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(phoneUtils.backgroundColor);
        buttonPanel.add(addButton);
        addressBookApp.add(buttonPanel, BorderLayout.SOUTH);

        // Ajouter les contacts par défaut
        addDefaultContacts();

        return addressBookApp;
    }

    private static void addDefaultContacts() {
        String[] defaultContacts = {
                "Timothy Fayulu", "Numa Lavanchy", "Marquinhos Cipriano",
                "Nias Hefti", "Kreshnik Hajrizi", "Anton Miranchuk",
                "Théo Bouchlarhem", "Théo Berdayes", "Ilyas Chouaref", "Dejan Sorgić"
        };
        for (String name : defaultContacts) {
            contactListModel.addElement(new Contact(name, "+41 79 123 45 67"));
        }
    }

    private static void showAddContactDialog() {
        JTextField nameField = new JTextField(15);
        JTextField phoneField = new JTextField(15);

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Phone:"));
        panel.add(phoneField);

        int result = JOptionPane.showConfirmDialog(null, panel,
                "Add New Contact", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            if (!name.isEmpty() && !phone.isEmpty()) {
                contactListModel.addElement(new Contact(name, phone));
            } else {
                JOptionPane.showMessageDialog(null, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Classe Contact
    static class Contact {
        String name;
        String phone;

        public Contact(String name, String phone) {
            this.name = name;
            this.phone = phone;
        }
    }

    // Custom Cell Renderer pour afficher l'icône + nom + téléphone
    static class ContactCellRenderer extends JPanel implements ListCellRenderer<Contact> {
        private final JLabel iconLabel;
        private final JLabel nameLabel;
        private final JLabel phoneLabel;

        public ContactCellRenderer() {
            setLayout(new BorderLayout(10, 5));
            iconLabel = new JLabel();
            nameLabel = new JLabel();
            phoneLabel = new JLabel();

            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.add(nameLabel);
            textPanel.add(phoneLabel);

            add(iconLabel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);

            setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Contact> list, Contact contact, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            iconLabel.setIcon(contactIcon);
            nameLabel.setText(contact.name);
            phoneLabel.setText(contact.phone);

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                nameLabel.setForeground(list.getSelectionForeground());
                phoneLabel.setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
                nameLabel.setForeground(list.getForeground());
                phoneLabel.setForeground(list.getForeground());
            }

            return this;
        }
    }
}