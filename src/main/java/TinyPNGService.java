import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * A service class to handle image compression using the TinyPNG API.
 */
public class TinyPNGService {
    private static final String API_URL = "https://api.tinify.com/shrink";
    private static final Preferences prefs = Preferences.userNodeForPackage(TinyPNGService.class);
    private static final String API_KEY_PREF = "tinypng_api_key";

    /**
     * Shows a dialog to prompt the user for their TinyPNG API key.
     * Returns true if a valid key was provided, false otherwise.
     */
    public static boolean promptForApiKey() {
        // Check if we already have a stored API key
        String savedApiKey = prefs.get(API_KEY_PREF, null);

        if (savedApiKey != null && !savedApiKey.isEmpty() && validateApiKey(savedApiKey)) {
            return true;
        }

        // Show instructions for getting an API key
        JOptionPane.showMessageDialog(null,
                "To access the Gallery application, you will need a TinyPNG API key.\n\n" +
                        "1. Visit TinyPNG.com, enter your name and email address\n" +
                        "2. Click on \"Get your API key\"\n" +
                        "3. Check your email inbox and click the link\n" +
                        "4. Copy your API key from the dashboard",
                "API Key Required",
                JOptionPane.INFORMATION_MESSAGE);

        // Prompt for API key
        String apiKey = JOptionPane.showInputDialog(null,
                "Please enter your TinyPNG API Key:",
                "API Key Required",
                JOptionPane.QUESTION_MESSAGE);

        if (apiKey == null || apiKey.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "No API key provided. The application will now exit.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate the API key
        if (validateApiKey(apiKey)) {
            // Save the API key
            prefs.put(API_KEY_PREF, apiKey);
            return true;
        } else {
            JOptionPane.showMessageDialog(null,
                    "Invalid API key. The application will now exit.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Validates the API key by making a test request to the TinyPNG API.
     */
    private static boolean validateApiKey(String apiKey) {
        try {
            URL url = new URL("https://api.tinify.com/shrink");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            // Set Authorization header
            String auth = "Basic " + Base64.getEncoder().encodeToString(("api:" + apiKey).getBytes());
            connection.setRequestProperty("Authorization", auth);

            // Just check if the API key is valid (expecting 400 as we're not sending an image)
            int responseCode = connection.getResponseCode();
            return responseCode != 401; // 401 means unauthorized (invalid API key)
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets the stored API key
     */
    private static String getApiKey() {
        return prefs.get(API_KEY_PREF, "");
    }

    /**
     * Compresses an image file using TinyPNG and returns the compressed file.
     * If the compression fails, returns the original file.
     */
    public static File compressImageFile(File inputFile) {
        String apiKey = getApiKey();
        if (apiKey.isEmpty()) {
            return inputFile; // Return original if no API key
        }

        try {
            // Create a temporary file for the compressed image
            String extension = getFileExtension(inputFile.getName());
            File outputFile = File.createTempFile("compressed_", "." + extension);

            // Open connection to TinyPNG API
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            // Set Authorization header
            String auth = "Basic " + Base64.getEncoder().encodeToString(("api:" + apiKey).getBytes());
            connection.setRequestProperty("Authorization", auth);
            connection.setDoOutput(true);

            // Send image data
            byte[] imageData = Files.readAllBytes(inputFile.toPath());
            connection.getOutputStream().write(imageData);

            // Check response
            int responseCode = connection.getResponseCode();
            if (responseCode != 201) {
                System.err.println("TinyPNG API error: " + responseCode);
                return inputFile; // Return original on error
            }

            // Get compressed image URL from Location header
            String compressedImageUrl = connection.getHeaderField("Location");
            if (compressedImageUrl == null) {
                return inputFile;
            }

            // Download the compressed image
            URL compressedUrl = new URL(compressedImageUrl);
            HttpURLConnection downloadConnection = (HttpURLConnection) compressedUrl.openConnection();

            try (InputStream compressedStream = downloadConnection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(outputFile)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = compressedStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            return outputFile;
        } catch (Exception e) {
            e.printStackTrace();
            return inputFile; // Return original on error
        }
    }

    /**
     * Compresses an image from a resource path and saves it to the resources directory.
     * Returns the path to the compressed image.
     */
    public static String compressResourceImage(String resourcePath) {
        try {
            // Get the file from the resource path
            URL resourceUrl = TinyPNGService.class.getResource(resourcePath);
            if (resourceUrl == null) {
                return resourcePath;
            }

            // Create a temporary file from the resource
            File tempFile = File.createTempFile("temp_", getFileExtension(resourcePath));
            try (InputStream is = resourceUrl.openStream();
                 FileOutputStream os = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }

            // Compress the image
            File compressedFile = compressImageFile(tempFile);

            // Generate a new filename for the compressed image
            String baseName = getBaseName(resourcePath);
            String extension = getFileExtension(resourcePath);
            String compressedName = baseName + "_compressed." + extension;
            String compressedResourcePath = "/imageGallery/" + compressedName;

            // Copy the compressed file to the resources directory
            Path resourcesPath = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "imageGallery");
            if (!Files.exists(resourcesPath)) {
                Files.createDirectories(resourcesPath);
            }

            Path targetPath = Paths.get(resourcesPath.toString(), compressedName);
            Files.copy(compressedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Clean up temporary files
            tempFile.delete();
            if (!compressedFile.equals(tempFile)) {
                compressedFile.delete();
            }

            return compressedResourcePath;
        } catch (Exception e) {
            e.printStackTrace();
            return resourcePath; // Return original path on error
        }
    }

    private static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1).toLowerCase();
    }

    private static String getBaseName(String path) {
        String filename = path.substring(path.lastIndexOf('/') + 1);
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? filename : filename.substring(0, dotIndex);
    }
}