package investify.app;

import investify.service.*;
import investify.ui.InvestifyUI;
import javax.swing.*;
import java.awt.*;

/**
 * Main class for the Investify application.
 * This class serves as the entry point for the application, initializing
 * all core services and creating the main user interface.
 * <p>
 * The application was originally implemented in a single file. Following the code review on May 21st, the instructors
 * recommended applying a design pattern such as MVC. The code was refactored by IA into multiple packages and classes to
 * align with this architecture. I took the time to understand the code that was added and modified by the AI. The comments
 * and javadoc documentation were also added by the AI to clarify the purpose and functionality of each component.
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
     *
     * @return A configured JPanel containing the complete Investify interface
     */
    public JPanel createInvestify() {
        return ui.createMainPanel(); // Delegates UI creation to UI service
    }

    /**
     * Ensures that the API is properly initialized with a valid key.
     * If the API has not been initialized yet, this method prompts the user
     * for an API key and attempts to initialize the API with the provided key.
     * If already initialized, it returns true without further action.
     *
     * @return true if the API is successfully initialized or was already initialized,
     * false if initialization fails or the user cancels the operation
     */
    public boolean ensureApiInitialized() {
        if (apiKeyInitialized) {
            return true;
        }

        if (apiKeyManager.setupApiKey()) {
            apiKeyInitialized = true;
            return true;
        }

        return false;
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