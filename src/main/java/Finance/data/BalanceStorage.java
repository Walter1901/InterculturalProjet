package Finance.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

// Class for storing and loading balance data (net savings and debts)
public class BalanceStorage {
    // Gson instance for JSON serialization/deserialization
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()  // Makes JSON output more readable
            .create();

    // Base directory for storing balance data files
    private final String BASE_DIR = System.getProperty("user.home")
            + File.separator + "FinanceTracker" + File.separator + "balance";
    private final Path BASE_PATH = Paths.get(BASE_DIR);

    // File paths for different types of balance data
    private final Path NET_SAVINGS_FILE = BASE_PATH.resolve("net_savings.json");
    private final Path DEBTS_FILE = BASE_PATH.resolve("debts.json");

    // Constructor creates the storage directory if it doesn't exist
    public BalanceStorage() {
        new File(BASE_DIR).mkdirs();
    }

    // Saves net savings data to JSON file
    public void saveNetSavings(Map<String, Map<String, String>> data) {
        saveData(NET_SAVINGS_FILE, data);
    }

    // Saves debts data to JSON file
    public void saveDebts(Map<String, Map<String, String>> data) {
        saveData(DEBTS_FILE, data);
    }

    // Loads net savings data from JSON file
    public Map<String, Map<String, String>> loadNetSavings() {
        return loadData(NET_SAVINGS_FILE);
    }

    // Loads debts data from JSON file
    public Map<String, Map<String, String>> loadDebts() {
        return loadData(DEBTS_FILE);
    }

    // Generic method to save data to a JSON file
    private void saveData(Path path, Map<String, Map<String, String>> data) {
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(path.toFile()), StandardCharsets.UTF_8)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("Error saving to " + path + ": " + e.getMessage());
        }
    }

    // Generic method to load data from a JSON file
    private Map<String, Map<String, String>> loadData(Path path) {
        File file = path.toFile();
        // Return empty map if file doesn't exist
        if (!file.exists()) return new HashMap<>();

        try (Reader reader = new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8)) {
            // Define the complex type for Gson deserialization
            Type type = new TypeToken<Map<String, Map<String, String>>>(){}.getType();
            Map<String, Map<String, String>> map = gson.fromJson(reader, type);
            // Return empty map if file is empty or invalid
            return map != null ? map : new HashMap<>();
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Error loading from " + path + ": " + e.getMessage());
            return new HashMap<>();
        }
    }
}