package investify.app; // Package declaration for the application

// Imports for application services and UI components
import investify.service.*;
import investify.ui.InvestifyUI;

// Swing and AWT imports for GUI components
import javax.swing.*;
import java.awt.*;

/**
 * Main class for the Investify application.
 */
public class Investify {

    // Application-wide color scheme constants
    public static final Color backgroundColor = new Color(28, 28, 30); // Dark background color
    public static final Color textColor = new Color(255, 255, 255); // White text for better contrast

    // Core services of the application
    private final InvestifyUI ui; // UI component manager
    private final PortfolioManager portfolioManager; // Manages investment portfolios
    private final TransactionService transactionService; // Handles investment transactions
    private final ChartService chartService; // Creates visual representations of portfolio data
    private final CurrencyService currencyService; // Handles currency conversions
    private final ApiKeyManager apiKeyManager; // Manages API authentication

    // Configuration state variables
    private String apiKey; // Stores API key for external services
    private boolean apiKeyInitialized = false; // Tracks if API has been initialized

    /**
     * Constructor - initializes all services in the correct dependency order.
     * Sets up the entire application structure including:
     * - Currency service for handling currency conversions
     * - API key management for external service authentication
     * - Portfolio management for tracking investments
     * - Chart service for data visualization
     * - Transaction service for handling investment operations
     * - User interface components
     */
    public Investify() {
        // Initialize services in order of dependency
        this.currencyService = new CurrencyService(); // First as other services depend on it
        this.apiKeyManager = new ApiKeyManager(); // Handles API authentication
        this.portfolioManager = new PortfolioManager(currencyService); // Needs currency service
        this.chartService = new ChartService(portfolioManager, currencyService); // Depends on portfolio and currency
        this.transactionService = new TransactionService(portfolioManager); // Depends on portfolio
        this.ui = new InvestifyUI(this); // UI needs access to all services via app instance
    }

    /**
     * Creates the main Investify user interface.
     * This method initializes and configures the primary application UI
     * without requesting an API key on startup. API keys will only be
     * requested when needed for specific features.
     *
     * @return A configured JPanel containing the complete Investify interface
     */
    public JPanel createInvestify() {
        // Doesn't request API key on startup
        // API key will only be requested when needed for specific features
        return ui.createMainPanel(); // Delegates UI creation to UI service
    }

    /**
     * Ensures that the API is properly initialized with a valid key.
     * If the API has not been initialized yet, this method prompts the user
     * for an API key and attempts to initialize the API with the provided key.
     * If already initialized, it returns true without further action.
     *
     * @return true if the API is successfully initialized or was already initialized,
     *         false if initialization fails or the user cancels the operation
     */
    public boolean ensureApiInitialized() {
        if (apiKeyInitialized) {
            return true;
        }

        // Utilise la nouvelle méthode pour configurer la clé API
        if (apiKeyManager.setupApiKey()) {
            apiKeyInitialized = true;
            return true;
        }

        return false;
    }

    /**
     * Creates an error panel with a specified message.
     * The panel uses the application's standard color scheme and font styling
     * to display an error message to the user.
     *
     * @param message The error message to display
     * @return A configured JPanel displaying the error message
     */
    private JPanel createErrorPanel(String message) {
        JPanel errorPanel = new JPanel(new BorderLayout()); // Create panel with border layout
        errorPanel.setBackground(backgroundColor); // Apply app background color

        JLabel errorLabel = new JLabel(message, SwingConstants.CENTER); // Create centered error message
        errorLabel.setForeground(textColor); // Apply app text color
        errorLabel.setFont(new Font("Inter", Font.BOLD, 18)); // Set bold font for emphasis

        errorPanel.add(errorLabel, BorderLayout.CENTER); // Add message to center of panel
        return errorPanel; // Return the configured error panel
    }

    /**
     * Provides access to the application's portfolio management service.
     *
     * @return The PortfolioManager instance for this application
     */
    public PortfolioManager getPortfolioManager() {
        return portfolioManager;
    }

    /**
     * Provides access to the application's transaction handling service.
     *
     * @return The TransactionService instance for this application
     */
    public TransactionService getTransactionService() {
        return transactionService;
    }

    /**
     * Provides access to the application's chart and data visualization service.
     *
     * @return The ChartService instance for this application
     */
    public ChartService getChartService() {
        return chartService;
    }

    /**
     * Provides access to the application's currency conversion service.
     *
     * @return The CurrencyService instance for this application
     */
    public CurrencyService getCurrencyService() {
        return currencyService;
    }

    /**
     * Provides access to the application's API key management service.
     *
     * @return The ApiKeyManager instance for this application
     */
    public ApiKeyManager getApiKeyManager() {
        return apiKeyManager;
    }
}