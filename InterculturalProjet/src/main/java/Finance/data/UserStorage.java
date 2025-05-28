package Finance.data;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class UserStorage {
    private static final String FILE_PATH = "users.json";
    private Map<String, String> users = new HashMap<>();

    public UserStorage() {
        loadUsers();
    }

    private void loadUsers() {
        try {
            if (!Files.exists(Paths.get(FILE_PATH))) {
                saveUsers();  // Datei erstellen, falls nicht vorhanden
                return;
            }
            String content = new String(Files.readAllBytes(Paths.get(FILE_PATH)));
            JSONArray array = new JSONArray(content);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                users.put(obj.getString("username"), obj.getString("password"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean userExists(String username) {
        return users.containsKey(username);
    }
    public boolean checkPassword(String username, String password) {
        return password.equals(users.get(username));
    }
    public void addUser(String username, String password) {
        users.put(username, password);
        saveUsers();
    }

    private void saveUsers() {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            JSONArray array = new JSONArray();
            for (Map.Entry<String, String> entry : users.entrySet()) {
                JSONObject obj = new JSONObject();
                obj.put("username", entry.getKey());
                obj.put("password", entry.getValue());
                array.put(obj);
            }
            writer.write(array.toString(2)); // schön formatiert speichern
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}