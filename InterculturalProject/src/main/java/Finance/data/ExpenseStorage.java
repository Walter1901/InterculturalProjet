package Finance.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class ExpenseStorage {
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    // Im Home-Verzeichnis unter FinanceTracker/expenses speichern
    private static final String BASE_DIR = System.getProperty("user.home")
            + File.separator + "FinanceTracker" + File.separator + "expenses";
    private static final Path BASE_PATH = Paths.get(BASE_DIR);
    private static final Path EXPENSES_FILE = BASE_PATH.resolve("expenses.json");

    public ExpenseStorage() {
        try {
            Files.createDirectories(BASE_PATH);
        } catch (IOException e) {
            System.err.println("Could not create expenses directory: " + e.getMessage());
        }
    }

    /** Speichert die Map<String, List<String>> als JSON */
    public void saveExpenses(Map<String, List<String>> data) {
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(EXPENSES_FILE.toFile()), StandardCharsets.UTF_8)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("Error saving expenses: " + e.getMessage());
        }
    }

    /** Lädt die JSON und gibt eine Map zurück (oder leere Map) */
    public Map<String, List<String>> loadExpenses() {
        if (!Files.exists(EXPENSES_FILE)) {
            return new HashMap<>();
        }
        try (Reader reader = new InputStreamReader(
                new FileInputStream(EXPENSES_FILE.toFile()), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<String, List<String>>>(){}.getType();
            Map<String, List<String>> map = gson.fromJson(reader, type);
            return map != null ? map : new HashMap<>();
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Error loading expenses: " + e.getMessage());
            return new HashMap<>();
        }
    }
}