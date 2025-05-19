package gallery.service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
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
    private static final String API_URL = "https://api.tinify.com/shrink";
    private static final Preferences prefs = Preferences.userNodeForPackage(TinyPNGService.class);
    private static final String API_KEY_PREF = "tinypng_api_key";

    /**
     * Displays a dialog prompting the user to enter the TinyPNG API key if none exists or if validation fails.
     * The key is stored using Java Preferences and validated before accepting it.
     *
     * @param parent The parent UI component for positioning the dialog (can be null).
     * @return true if a valid API key is entered and stored, false if the application should exit.
     */
    public boolean showApiKeyDialog(Component parent) {
        String savedApiKey = prefs.get(API_KEY_PREF, null);

        // If a valid API key is already stored, return immediately.
        if (savedApiKey != null && !savedApiKey.isEmpty() && validateApiKey(savedApiKey)) {
            return true;
        }

        // Create a custom modal dialog for API key input.
        JDialog dialog = new JDialog((Frame) null, "API Key Required", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setResizable(false);

        // Display an icon for user guidance.
        JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.questionIcon"));
        iconLabel.setForeground(new Color(0, 180, 0));
        iconLabel.setPreferredSize(new Dimension(48, 48));

        // Create input field for entering the API key.
        JLabel messageLabel = new JLabel("Please enter your API key:");
        JTextField apiKeyField = new JTextField(32);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(messageLabel, BorderLayout.NORTH);
        inputPanel.add(apiKeyField, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(iconLabel, BorderLayout.WEST);
        centerPanel.add(inputPanel, BorderLayout.CENTER);

        // OK and Cancel buttons for user interaction.
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dialog.add(centerPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);

        // Handle OK button action.
        okButton.addActionListener(e -> {
            String apiKey = apiKeyField.getText().trim();
            if (apiKey.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "API key cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!validateApiKey(apiKey)) {
                JOptionPane.showMessageDialog(dialog, "Invalid API key.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Save the validated API key.
            prefs.put(API_KEY_PREF, apiKey);
            dialog.dispose();
        });

        // Handle Cancel button action and exit the application if required.
        cancelButton.addActionListener(e -> {
            dialog.dispose();
            System.exit(0);
        });
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        dialog.setVisible(true);

        // Verify the stored API key after dialog completion.
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
        if (apiKey == null || apiKey.trim().isEmpty()) return false;
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            String auth = "Basic " + Base64.getEncoder().encodeToString(("api:" + apiKey).getBytes());
            connection.setRequestProperty("Authorization", auth);
            connection.setDoOutput(true);
            connection.getOutputStream().write("{}".getBytes());
            int responseCode = connection.getResponseCode();
            // 401 = Unauthorized, 429 = quota exceeded (but key exists)
            return responseCode != 401;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Retrieves the stored TinyPNG API key from preferences.
     * Returns an empty string if no key has been stored.
     *
     * @return The stored API key or an empty string if unavailable.
     */
    public String getApiKey() {
        return prefs.get(API_KEY_PREF, "");
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
        String apiKey = getApiKey();
        if (apiKey.isEmpty()) {
            return inputFile;
        }
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            String auth = "Basic " + Base64.getEncoder().encodeToString(("api:" + apiKey).getBytes());
            connection.setRequestProperty("Authorization", auth);
            connection.setDoOutput(true);

            // Read and send image data to TinyPNG.
            byte[] imageData = java.nio.file.Files.readAllBytes(inputFile.toPath());
            connection.getOutputStream().write(imageData);

            int responseCode = connection.getResponseCode();
            if (responseCode != 201) {
                System.err.println("TinyPNG API error: " + responseCode);
                return inputFile;
            }

            // Retrieve compressed image URL from API response.
            String compressedImageUrl = connection.getHeaderField("Location");
            if (compressedImageUrl == null) {
                return inputFile;
            }

            // Download compressed image from TinyPNG.
            URL compressedUrl = new URL(compressedImageUrl);
            HttpURLConnection downloadConnection = (HttpURLConnection) compressedUrl.openConnection();
            File compressedFile = new File(inputFile.getParent(), inputFile.getName());

            try (InputStream compressedStream = downloadConnection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(compressedFile)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = compressedStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            return compressedFile;
        } catch (Exception e) {
            e.printStackTrace();
            return inputFile;
        }
    }
}
