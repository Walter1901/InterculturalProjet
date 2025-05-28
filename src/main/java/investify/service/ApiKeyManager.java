package investify.service; // Defines the package for this class in the service layer

// Imports for the AlphaVantage API library
import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.Config;
// Imports for building the GUI dialog
import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
 * Manages the AlphaVantage API key for the Investify application.
 * This service handles API key collection from users and initialization
 * of the AlphaVantage API client with valid credentials.
 * It provides UI dialogs for key input and performs validation to ensure
 * proper API connectivity.
 */
public class ApiKeyManager {
    /**
     * Shows a dialog for entering the AlphaVantage API key.
     * Creates and displays a user interface with a text field where users
     * can enter their API key. The dialog includes informational text
     * and OK/Cancel buttons.
     *
     * @return The entered API key if the user confirms, or null if the user
     *         cancels the dialog or closes it without confirming
     */
    public String showApiKeyDialog() {
        // Creates a panel with GridBagLayout for flexible component positioning
        JPanel panel = new JPanel(new GridBagLayout());
        // Sets the preferred dimensions for the dialog panel
        panel.setPreferredSize(new Dimension(350, 150));

        // Creates constraints object to control component placement and behavior
        GridBagConstraints gbc = new GridBagConstraints();
        // Sets initial x coordinate in the grid
        gbc.gridx = 0;
        // Sets initial y coordinate in the grid
        gbc.gridy = 0;
        // Aligns components to the left side of their cell
        gbc.anchor = GridBagConstraints.WEST;
        // Adds padding around components (top, left, bottom, right)
        gbc.insets = new Insets(10, 10, 5, 10);

        // Creates the instructional label text
        JLabel label = new JLabel("Enter your AlphaVantage API key:");
        // Sets font styling for the label
        label.setFont(new Font("Inter", Font.BOLD, 14));
        // Adds the label to the panel with the specified constraints
        panel.add(label, gbc);

        // Moves to the second row
        gbc.gridy = 1;
        // Makes the component fill available horizontal space
        gbc.fill = GridBagConstraints.HORIZONTAL;
        // Gives horizontal weight to this component
        gbc.weightx = 1.0;
        // Creates a text field for the API key input
        JTextField apiKeyField = new JTextField(20);
        // Sets font for the input field
        apiKeyField.setFont(new Font("Inter", Font.PLAIN, 14));
        // Sets size for the input field
        apiKeyField.setPreferredSize(new Dimension(300, 30));
        // Adds the text field to the panel
        panel.add(apiKeyField, gbc);

        // Moves to the third row
        gbc.gridy = 2;
        // Adds helper text below the input field
        JLabel infoLabel = new JLabel("Enter the provided API key");
        // Sets italicized font for the helper text
        infoLabel.setFont(new Font("Inter", Font.ITALIC, 12));
        // Adds the helper label to the panel
        panel.add(infoLabel, gbc);

        // Displays a dialog with OK and Cancel buttons, storing user's response
        int result = JOptionPane.showConfirmDialog(
                null, // No parent component specified
                panel, // Our configured panel
                "Investify API Configuration", // Dialog title
                JOptionPane.OK_CANCEL_OPTION, // Shows OK and Cancel buttons
                JOptionPane.PLAIN_MESSAGE); // Uses plain style (no icon)

        // Checks if user clicked OK
        if (result == JOptionPane.OK_OPTION) {
            // Returns the entered API key with whitespace removed
            return apiKeyField.getText().trim();
        }

        // Returns null if user canceled or closed the dialog
        return null;
    }

    /**
     * Initializes the AlphaVantage API with the provided key.
     * Configures the AlphaVantage client library with the specified API key
     * and default timeout settings. This method must be called before making
     * any API requests to AlphaVantage.
     *
     * @param apiKey The AlphaVantage API key to use for initialization
     * @return true if initialization was successful, false if the key was invalid
     *         or an error occurred during initialization
     */
    public boolean initializeApi(String apiKey) {
        // Validates the API key is not null or empty
        if (apiKey == null || apiKey.isEmpty()) {
            // Returns false if key validation fails
            return false;
        }

        try {
            // Creates an API configuration using builder pattern
            Config cfg = Config.builder()
                    .key(apiKey) // Sets the API key
                    .timeOut(30) // Sets request timeout to 30 seconds
                    .build(); // Builds the final configuration object

            // Initializes the AlphaVantage API with our configuration
            AlphaVantage.api().init(cfg);
            // Returns true to indicate successful initialization
            return true;
        } catch (Exception e) {
            // Prints stack trace if any error occurs during initialization
            e.printStackTrace();
            // Returns false to indicate initialization failed
            return false;
        }
    }

    /**
     * Gère la clé API en la sauvegardant localement après la première saisie.
     * Cherche d'abord une clé existante, et ne demande à l'utilisateur que si nécessaire.
     *
     * @return true si l'API est correctement initialisée, false sinon
     */
    public boolean setupApiKey() {
        // Vérifie si une clé API est déjà enregistrée
        String savedKey = loadSavedApiKey();

        if (savedKey != null && !savedKey.isEmpty()) {
            // Si une clé existe, l'utiliser sans demander à l'utilisateur
            boolean success = initializeApi(savedKey);
            if (success) {
                return true;
            }
            // Si la clé sauvegardée n'est plus valide, demander une nouvelle clé
        }

        // Aucune clé valide trouvée, demander à l'utilisateur
        String newKey = showApiKeyDialog();
        if (newKey != null && !newKey.isEmpty()) {
            boolean success = initializeApi(newKey);
            if (success) {
                // Sauvegarder la clé pour les utilisations futures
                saveApiKey(newKey);
                return true;
            }
        }

        return false;
    }

    /**
     * Charge la clé API depuis le fichier local.
     *
     * @return La clé API sauvegardée, ou null si aucune n'est trouvée
     */
    private String loadSavedApiKey() {
        File keyFile = new File(System.getProperty("user.home"), ".investify_api_key");
        if (!keyFile.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(keyFile))) {
            return reader.readLine();
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture de la clé API: " + e.getMessage());
            return null;
        }
    }

    /**
     * Sauvegarde la clé API dans un fichier local.
     *
     * @param apiKey La clé API à sauvegarder
     */
    private void saveApiKey(String apiKey) {
        File keyFile = new File(System.getProperty("user.home"), ".investify_api_key");

        try (FileWriter writer = new FileWriter(keyFile)) {
            writer.write(apiKey);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'enregistrement de la clé API: " + e.getMessage());
        }
    }

}