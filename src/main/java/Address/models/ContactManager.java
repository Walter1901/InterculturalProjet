package Address.models;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Singleton contact manager that handles contact persistence and operations
 */
public class ContactManager {
    private static ContactManager instance;
    private static List<Contact> contacts = new ArrayList<>();

    private ContactManager() {
        // Private constructor for singleton pattern
        loadContacts();
    }

    /**
     * Get singleton instance of ContactManager
     */
    public static ContactManager getInstance() {
        if (instance == null) {
            synchronized (ContactManager.class) {
                if (instance == null) {
                    instance = new ContactManager();
                }
            }
        }
        return instance;
    }

    /**
     * Load contacts from JSON file using ContactStorage
     */
    public static void loadContacts() {
        try {
            contacts = ContactStorage.loadContacts();
            System.out.println("Loaded " + contacts.size() + " contacts");
        } catch (Exception e) {
            String errorMsg = "Error loading contacts: " + e.getMessage();
            System.err.println(errorMsg);
            JOptionPane.showMessageDialog(null, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
            contacts = new ArrayList<>(); // Initialize with empty list on error
        }
    }

    /**
     * Save contacts to JSON file using ContactStorage
     */
    public static void saveContacts() {
        try {
            ContactStorage.saveContacts(contacts);
            System.out.println("Saved " + contacts.size() + " contacts");
        } catch (Exception e) {
            String errorMsg = "Error saving contacts: " + e.getMessage();
            System.err.println(errorMsg);
            JOptionPane.showMessageDialog(null, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Get a copy of all contacts
     */
    public static List<Contact> getContacts() {
        return new ArrayList<>(contacts);
    }

    /**
     * Add a new contact
     */
    public static boolean addContact(Contact contact) {
        if (contact == null || !contact.isValid()) {
            return false;
        }

        // Check for duplicates
        if (contacts.stream().anyMatch(c -> c.equals(contact))) {
            return false;
        }

        contacts.add(contact);
        saveContacts();
        return true;
    }

    /**
     * Update an existing contact
     */
    public static boolean updateContact(int index, Contact contact) {
        if (index >= 0 && index < contacts.size() && contact != null && contact.isValid()) {
            contacts.set(index, contact);
            saveContacts();
            return true;
        }
        return false;
    }

    /**
     * Delete a contact by index
     */
    public static boolean deleteContact(int index) {
        if (index >= 0 && index < contacts.size()) {
            contacts.remove(index);
            saveContacts();
            return true;
        }
        return false;
    }

    /**
     * Delete a contact by reference
     */
    public static boolean deleteContact(Contact contact) {
        if (contacts.remove(contact)) {
            saveContacts();
            return true;
        }
        return false;
    }

    /**
     * Get contact by index
     */
    public static Contact getContact(int index) {
        if (index >= 0 && index < contacts.size()) {
            return contacts.get(index);
        }
        return null;
    }

    /**
     * Get contact index
     */
    public static int getContactIndex(Contact contact) {
        return contacts.indexOf(contact);
    }

    /**
     * Search contacts by name
     */
    public static List<Contact> searchContacts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getContacts();
        }

        String lowerQuery = query.toLowerCase().trim();
        return contacts.stream()
                .filter(contact ->
                        contact.getName().toLowerCase().contains(lowerQuery) ||
                                contact.getFirstName().toLowerCase().contains(lowerQuery) ||
                                contact.getLastName().toLowerCase().contains(lowerQuery) ||
                                contact.getPhone().contains(lowerQuery) ||
                                contact.getEmail().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    /**
     * Get contacts count
     */
    public static int getContactsCount() {
        return contacts.size();
    }

    /**
     * Clear all contacts
     */
    public static void clearAllContacts() {
        contacts.clear();
        saveContacts();
    }
}