package Address.models;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ContactStorage {
    private static final String FILE_PATH = "contacts.json";

    public static void saveContacts(List<Contact> contacts) {
        JSONArray array = new JSONArray();
        for (Contact c : contacts) {
            JSONObject obj = new JSONObject();
            obj.put("name", c.getName());
            obj.put("firstName", c.getFirstName());
            obj.put("lastName", c.getLastName());
            obj.put("phone", c.getPhone());
            obj.put("birthDate", c.getBirthDate());
            obj.put("address", c.getAddress());
            obj.put("email", c.getEmail());
            // L'image n'est pas sauvegardée ici (champ transient)
            array.put(obj);
        }
        try (FileWriter file = new FileWriter(FILE_PATH)) {
            file.write(array.toString(2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Contact> loadContacts() {
        List<Contact> contacts = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return contacts;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            JSONArray array = new JSONArray(sb.toString());
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Contact c = new Contact(
                        obj.optString("name"),
                        obj.optString("phone"),
                        obj.optString("firstName"),
                        obj.optString("lastName"),
                        obj.optString("birthDate"),
                        obj.optString("address"),
                        obj.optString("email"),
                        null // pas de photo
                );
                contacts.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contacts;
    }
}