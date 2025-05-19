package gallery.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gère les liens entre les images et les contacts.
 * Permet d'associer des images à des contacts de l'application AddressBook.
 */
public class ContactLinkManager {
    // Structure de données pour stocker les liens
    private Map<String, List<String>> contactImageLinks = new HashMap<>();

    /**
     * Associe une image à un contact
     * @param contactId Identifiant du contact
     * @param imagePath Chemin de l'image
     */
    public void linkImageToContact(String contactId, String imagePath) {
        if (!contactImageLinks.containsKey(contactId)) {
            contactImageLinks.put(contactId, new ArrayList<>());
        }

        if (!contactImageLinks.get(contactId).contains(imagePath)) {
            contactImageLinks.get(contactId).add(imagePath);
        }
    }

    /**
     * Récupère les images associées à un contact
     * @param contactId Identifiant du contact
     * @return Liste des chemins d'images associées
     */
    public List<String> getImagesForContact(String contactId) {
        return contactImageLinks.getOrDefault(contactId, new ArrayList<>());
    }

    /**
     * Supprime l'association entre une image et un contact
     * @param contactId Identifiant du contact
     * @param imagePath Chemin de l'image
     */
    public void unlinkImageFromContact(String contactId, String imagePath) {
        if (contactImageLinks.containsKey(contactId)) {
            contactImageLinks.get(contactId).remove(imagePath);
        }
    }

    /**
     * Récupère tous les contacts associés à une image
     * @param imagePath Chemin de l'image
     * @return Liste des identifiants de contacts associés
     */
    public List<String> getContactsForImage(String imagePath) {
        List<String> contacts = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : contactImageLinks.entrySet()) {
            if (entry.getValue().contains(imagePath)) {
                contacts.add(entry.getKey());
            }
        }

        return contacts;
    }
}