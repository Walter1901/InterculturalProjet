package gallery.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import gallery.model.GalleryData;

import java.io.*;
import java.lang.reflect.Type;

/**
 * Manages data persistence for the gallery application.
 *
 * This class handles saving and loading gallery data to/from disk storage.
 * It uses JSON format (via Gson) to serialize and deserialize the gallery
 * structure, including albums, image paths, and applied filters.
 *
 * All data is stored in a single JSON file in the application directory.
 */
public class StorageManager {
    private static final String SAVE_FILE = "gallery_data.json";  // File name for saved data

    /**
     * Saves gallery data to a JSON file.
     *
     * Serializes the provided GalleryData object to JSON format and
     * writes it to the gallery_data.json file. Uses pretty printing
     * for better readability of the saved file.
     *
     * @param data The GalleryData object to serialize and save
     */
    public void saveGalleryData(GalleryData data) {
        File file = new File(SAVE_FILE);

        // Only save if the file already exists
        if (file.exists()) {
            try (Writer writer = new FileWriter(SAVE_FILE)) {
                // Create Gson instance with pretty printing for readable JSON
                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                // Convert data object to JSON and write to file
                gson.toJson(data, writer);
            } catch (IOException e) {
                // Log error if save fails
                e.printStackTrace();
                System.err.println("Error saving gallery data: " + e.getMessage());
            }
        } else {
            System.out.println("gallery_data.json does not exist, skipping save operation");
        }
    }

    /**
     * Loads gallery data from the JSON file.
     *
     * Reads the gallery_data.json file, deserializes it into a GalleryData
     * object, and returns it. If the file doesn't exist or can't be read,
     * returns null to indicate that a new empty gallery should be created.
     *
     * @return The loaded GalleryData object, or null if not available
     */
    public GalleryData loadGalleryData() {
        File file = new File(SAVE_FILE);

        // Check if save file exists
        if (!file.exists()) {
            System.out.println("Using existing gallery data file.");
            return null;  // Return null to indicate no data available
        }

        try (Reader reader = new FileReader(file)) {
            // Define the type for deserialization
            Type type = new TypeToken<GalleryData>() {}.getType();

            // Parse JSON file into GalleryData object
            return new Gson().fromJson(reader, type);
        } catch (IOException e) {
            // Log error if load fails
            e.printStackTrace();
            System.err.println("Error loading gallery data: " + e.getMessage());
            return null;  // Return null to indicate loading failed
        }
    }
}