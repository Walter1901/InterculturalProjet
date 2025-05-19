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
    private static final String SAVE_FILE = "gallery_data.json";

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
        try (Writer writer = new FileWriter(SAVE_FILE)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error saving gallery data: " + e.getMessage());
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
        if (!file.exists()) {
            System.out.println("No gallery data file found. Starting with empty gallery.");
            return null;
        }

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<GalleryData>() {}.getType();
            return new Gson().fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading gallery data: " + e.getMessage());
            return null;
        }
    }
}