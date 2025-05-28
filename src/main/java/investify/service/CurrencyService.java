package investify.service; // Defines the package for this service class within the application

/**
 * Manages currency operations for the Investify application.
 * This service handles currency conversion, formatting, and storage of the current
 * selected currency. It supports conversion between USD and CHF currencies
 * using fixed exchange rates.
 */
public class CurrencyService { // Main class for handling currency operations and conversions
    private String currentCurrency = "USD"; // Stores the currently selected currency, with USD as default
    private final double USD_TO_CHF = 0.83; // Defines the fixed exchange rate from USD to Swiss Francs

    /**
     * Sets the current application currency.
     * Only accepts valid currency codes that are supported by the application
     * (currently USD and CHF).
     *
     * @param currency The currency code to set ("USD" or "CHF")
     */
    public void setCurrency(String currency) { // Method to change the active currency
        if ("USD".equals(currency) || "CHF".equals(currency)) { // Validates that the currency is supported
            this.currentCurrency = currency; // Updates the current currency if valid
        }
    }

    /**
     * Gets the currently selected currency code.
     *
     * @return The current currency code ("USD" or "CHF")
     */
    public String getCurrentCurrency() { // Method to retrieve the currently selected currency
        return currentCurrency; // Returns the current currency code
    }

    /**
     * Formats a monetary amount according to the current currency.
     * Converts the amount to the appropriate currency if needed and
     * applies the correct formatting including currency symbol.
     *
     * @param amount The monetary amount in USD to format
     * @return A formatted string representing the amount with currency symbol
     */
    public String formatCurrency(double amount) { // Method to format amounts according to the current currency
        if (currentCurrency.equals("CHF")) { // Checks if the current currency is Swiss Francs
            amount = amount * USD_TO_CHF; // Converts the amount from USD to CHF using the exchange rate
            return String.format("%.2f Fr.", amount); // Returns formatted string with CHF symbol and 2 decimal places
        } else {
            return String.format("%.2f $", amount); // Returns formatted string with USD symbol and 2 decimal places
        }
    }

    /**
     * Converts a monetary value from USD to the currently selected currency.
     * If the current currency is USD, returns the original amount.
     * If the current currency is CHF, converts the amount using the defined exchange rate.
     *
     * @param amountUSD The monetary amount in USD to convert
     * @return The equivalent amount in the currently selected currency
     */
    public double convertToCurrent(double amountUSD) { // Method to convert USD amount to the current currency
        if (currentCurrency.equals("CHF")) { // Checks if conversion to CHF is needed
            return amountUSD * USD_TO_CHF; // Converts and returns amount in CHF
        }
        return amountUSD; // Returns original amount if currency is already USD
    }
}