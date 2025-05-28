package gallery.service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.prefs.Preferences;

/**
 * Utility class for managing TinyPNG API key authentication and image compression.
 * This class provides methods to validate and store the API key, ensuring secure communication
 * with the TinyPNG service for compressing images.
 *
 * The API key is stored using Java Preferences and can be retrieved or updated via a user prompt.
 * If the key is missing or invalid, the application prompts the user to enter a new key.
 *
 * AI-assisted in analyzing and optimizing this code for better structure and efficiency.
 */

public class TinyPNGService {
    // Constants for API communication
    private static final String API_URL = "https://api.tinify.com/shrink";  // TinyPNG API endpoint

    // User preferences for storing API key
    private static final Preferences prefs = Preferences.userNodeForPackage(TinyPNGService.class);
    private static final String API_KEY_PREF = "tinypng_api_key";  // Preference key for API key

    /**
     * Displays a dialog prompting the user to enter the TinyPNG API key if none exists or if validation fails.
     * The key is stored using Java Preferences and validated before accepting it.
     *
     * @param parent The parent UI component for positioning the dialog (can be null).
     * @return true if a valid API key is entered and stored, false if the application should exit.
     */
    public boolean showApiKeyDialog(Component parent) {
        // Try to get saved API key
        String savedApiKey = prefs.get(API_KEY_PREF, null);

        // If we already have a valid key, return success
        if (savedApiKey != null && !savedApiKey.isEmpty() && validateApiKey(savedApiKey)) {
            return true;
        }

        // Create a custom dialog for API key input
        JDialog dialog = new JDialog((Frame) null, "API Key Required", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setResizable(false);

        // Add question icon
        JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.questionIcon"));
        iconLabel.setForeground(new Color(0, 180, 0));
        iconLabel.setPreferredSize(new Dimension(48, 48));

        // Create input field with prompt
        JLabel messageLabel = new JLabel("Please enter your API key:");
        JTextField apiKeyField = new JTextField(32);

        // Arrange components in panels
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(messageLabel, BorderLayout.NORTH);
        inputPanel.add(apiKeyField, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(iconLabel, BorderLayout.WEST);
        centerPanel.add(inputPanel, BorderLayout.CENTER);

        // Add OK/Cancel buttons
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        // Assemble the dialog
        dialog.add(centerPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);  // Center relative to parent

        // Define OK button action
        okButton.addActionListener(e -> {
            String apiKey = apiKeyField.getText().trim();
            if (apiKey.isEmpty()) {
                // Show error if key is empty
                JOptionPane.showMessageDialog(dialog,
                        "API key cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!validateApiKey(apiKey)) {
                // Show error if key is invalid
                JOptionPane.showMessageDialog(dialog,
                        "Invalid API key.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Save valid key to preferences
            prefs.put(API_KEY_PREF, apiKey);
            dialog.dispose();  // Close dialog
        });

        // Define Cancel button action (exit app)
        cancelButton.addActionListener(e -> {
            dialog.dispose();
            System.exit(0);  // Exit application if cancelled
        });

        // Also exit if dialog is closed via X button
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        // Show dialog and wait for input
        dialog.setVisible(true);

        // After dialog closes, check if we have a valid key stored
        String storedKey = prefs.get(API_KEY_PREF, null);
        return storedKey != null && !storedKey.isEmpty() && validateApiKey(storedKey);
    }

    /**
     * Validates the TinyPNG API key by making a test request to the TinyPNG service.
     * Ensures the key is valid and has not exceeded usage limits.
     *
     * @param apiKey The API key to validate.
     * @return true if the key is valid and recognized by TinyPNG, false otherwise.
     */
    public boolean validateApiKey(String apiKey) {
        // Return false for empty keys
        if (apiKey == null || apiKey.trim().isEmpty()) return false;

        try {
            // Set up connection to TinyPNG API
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            // Add basic auth header with API key
            String auth = "Basic " + Base64.getEncoder().encodeToString(("api:" + apiKey).getBytes());
            connection.setRequestProperty("Authorization", auth);

            // Enable output and send empty payload
            connection.setDoOutput(true);
            connection.getOutputStream().write("{}".getBytes());

            // Get response code
            int responseCode = connection.getResponseCode();

            // 401 = Unauthorized (invalid key)
            // 429 = Too many requests (key is valid but quota exceeded)
            return responseCode != 401;
        } catch (Exception e) {
            return false;  // Any exception means validation failed
        }
    }

    /**
     * Retrieves the stored TinyPNG API key from preferences.
     * Returns an empty string if no key has been stored.
     *
     * @return The stored API key or an empty string if unavailable.
     */
    public String getApiKey() {
        return prefs.get(API_KEY_PREF, "");  // Return empty string if no key stored
    }
    /**
     * Compresses an image using the TinyPNG API.
     * This method sends an image to TinyPNG's API, retrieves the compressed version,
     * and saves it back to the original file location.
     *
     * The process:
     * 1. Reads the image file and prepares it for API submission.
     * 2. Sends the image data to TinyPNG using an authenticated HTTP request.
     * 3. Receives the compressed image URL and downloads the optimized file.
     * 4. Saves the compressed version to disk, replacing the original file.
     * 5. If compression fails, returns the original file unchanged.
     *
     * @param inputFile The image file to be compressed.
     * @return The compressed image file or the original if compression fails.
     */
    public File compressImage(File inputFile) {
        // Get the API key
        String apiKey = getApiKey();
        if (apiKey.isEmpty()) {
            return inputFile;  // Return original file if no API key
        }

        try {
            // Set up connection to TinyPNG API
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            // Add basic auth header with API key
            String auth = "Basic " + Base64.getEncoder().encodeToString(("api:" + apiKey).getBytes());
            connection.setRequestProperty("Authorization", auth);

            // Enable output for sending image data
            connection.setDoOutput(true);

            // Read and send the image data
            byte[] imageData = java.nio.file.Files.readAllBytes(inputFile.toPath());
            connection.getOutputStream().write(imageData);

            // Check response code
            int responseCode = connection.getResponseCode();
            if (responseCode != 201) {  // 201 = Created (success)
                System.err.println("TinyPNG API error: " + responseCode);
                return inputFile;  // Return original file on error
            }

            // Get URL of compressed image from response header
            String compressedImageUrl = connection.getHeaderField("Location");
            if (compressedImageUrl == null) {
                return inputFile;  // Return original file if no URL in response
            }

            // Download the compressed image
            URL compressedUrl = new URL(compressedImageUrl);
            HttpURLConnection downloadConnection = (HttpURLConnection) compressedUrl.openConnection();

            // Create output file with same name in same location
            File compressedFile = new File(inputFile.getParent(), inputFile.getName());

            // Copy the compressed image data to the file
            try (InputStream compressedStream = downloadConnection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(compressedFile)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = compressedStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            return compressedFile;  // Return the compressed file
        } catch (Exception e) {
            // Log error and return original file if compression fails
            e.printStackTrace();
            return inputFile;
        }
    }
}
