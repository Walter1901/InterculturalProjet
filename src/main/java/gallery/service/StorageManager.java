package gallery.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import gallery.model.GalleryData;

import java.io.*;
import java.lang.reflect.Type;

/**
 * Gère la persistance des données de la galerie.
 * Responsable de la sauvegarde et du chargement des données.
 */
public class StorageManager {
    private static final String SAVE_FILE = "gallery_data.json";

    /**
     * Sauvegarde les données de la galerie dans un fichier JSON
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
     * Charge les données de la galerie depuis un fichier JSON
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