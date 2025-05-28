package Finance.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import Finance.model.Goal;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles storage and retrieval of saving goals to/from JSON file
 * Stores all goals in a single file in the user's home directory
 */
public class GoalStorage {
    // Configured Gson instance with custom date handling and pretty printing
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();

    // Path to the goals storage file in user's home directory
    private static final String STORAGE_FILE = System.getProperty("user.home")
            + File.separator + "finance_goals.json";

    /**
     * Saves all goals to persistent storage
     * @param goals List of Goal objects to save
     */
    public void saveGoals(List<Goal> goals) {
        // Create parent directory if it doesn't exist
        new File(System.getProperty("user.home")).mkdirs();

        try (Writer writer = new FileWriter(STORAGE_FILE)) {
            gson.toJson(goals, writer);
        } catch (IOException e) {
            System.err.println("Error saving goals: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads all goals from persistent storage
     * @return List of Goal objects, empty list if none exist or error occurs
     */
    public List<Goal> loadGoals() {
        File file = new File(STORAGE_FILE);

        // Return empty list if file doesn't exist yet
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            // Define the type for proper deserialization
            Type goalListType = new TypeToken<List<Goal>>() {}.getType();

            // Deserialize the JSON
            List<Goal> goals = gson.fromJson(reader, goalListType);

            // Return empty list if file was empty
            return goals != null ? goals : new ArrayList<>();

        } catch (IOException e) {
            System.err.println("Error reading goals file: " + e.getMessage());
            return new ArrayList<>();
        } catch (JsonSyntaxException e) {
            System.err.println("Invalid JSON format in goals file: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}