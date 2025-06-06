package gallery.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages associations between images and contacts.
 *
 * This class provides functionality to link images in the gallery with
 * contacts from the address book. It maintains a mapping of contact IDs
 * to the images associated with them, supporting bidirectional lookup.
 *
 * Key features:
 * - Linking images to contacts
 * - Finding all images for a specific contact
 * - Finding all contacts associated with an image
 * - Removing links between images and contacts
 */
public class ContactLinkManager {
    // Data structure to store image-contact relationships
    private Map<String, List<String>> contactImageLinks = new HashMap<>();  // Maps contact IDs to lists of image paths

    /**
     * Links an image to a contact.
     *
     * Creates an association between the specified contact and image.
     * If the contact already has links to other images, the new image
     * is added to that collection.
     *
     * @param contactId Identifier of the contact
     * @param imagePath Path to the image resource
     */
    public void linkImageToContact(String contactId, String imagePath) {
        // If this contact doesn't have a list yet, create one
        if (!contactImageLinks.containsKey(contactId)) {
            contactImageLinks.put(contactId, new ArrayList<>());
        }

        // Add the image to the contact's list if not already there
        if (!contactImageLinks.get(contactId).contains(imagePath)) {
            contactImageLinks.get(contactId).add(imagePath);
        }
    }

    /**
     * Retrieves all images linked to a specific contact.
     *
     * Returns a list of image paths associated with the given contact ID.
     * If no images are linked to the contact, returns an empty list.
     *
     * @param contactId Identifier of the contact
     * @return List of image paths linked to the contact
     */
    public List<String> getImagesForContact(String contactId) {
        // Return the list of images for this contact or an empty list if none exists
        return contactImageLinks.getOrDefault(contactId, new ArrayList<>());
    }

    /**
     * Removes the association between an image and a contact.
     *
     * @param contactId Identifier of the contact
     * @param imagePath Path to the image resource
     */
    public void unlinkImageFromContact(String contactId, String imagePath) {
        // If contact exists, remove the image from their list
        if (contactImageLinks.containsKey(contactId)) {
            contactImageLinks.get(contactId).remove(imagePath);
        }
    }

    /**
     * Retrieves all contacts associated with a specific image.
     *
     * @param imagePath Path to the image resource
     * @return List of contact IDs linked to the image
     */
    public List<String> getContactsForImage(String imagePath) {
        List<String> contacts = new ArrayList<>();

        // Search through all contacts to find those that have this image
        for (Map.Entry<String, List<String>> entry : contactImageLinks.entrySet()) {
            if (entry.getValue().contains(imagePath)) {
                contacts.add(entry.getKey());  // Add contact ID to results
            }
        }

        return contacts;  // Return list of contact IDs
    }
}