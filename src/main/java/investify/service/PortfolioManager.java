package investify.service;

import investify.model.Transaction;

import javax.swing.*;
import java.util.*;
import java.io.*;


public class PortfolioManager { // Main class responsible for managing investment portfolio data
    private final CurrencyService currencyService; // Reference to handle currency formatting and conversion
    private Map<String, Integer> holdings = new HashMap<>(); // Stores stock quantities by symbol
    private Map<String, Double> totalValues = new HashMap<>(); // Stores monetary values by symbol
    private double portfolioTotalValue = 0.0; // Tracks the combined value of all holdings

    /**
     * Constructs a new PortfolioManager with the required dependencies.
     *
     * @param currencyService The currency service used for formatting and conversion
     */
    public PortfolioManager(CurrencyService currencyService) {
        this.currencyService = currencyService; // Initializes the currency service reference
    }

    /**
     * Gets the formatted total portfolio value as a string.
     * The value is updated before formatting to ensure accuracy.
     *
     * @return The formatted portfolio value string
     */
    public String getPortfolioValueFormatted() { // Method to retrieve formatted portfolio value
        calculatePortfolioValue(); // Updates the portfolio value before formatting
        return currencyService.formatCurrency(portfolioTotalValue); // Returns the value with proper currency format
    }

    /**
     * Calculates the total value of the portfolio.
     * This method processes all transactions and computes the combined value of holdings.
     */
    public void calculatePortfolioValue() { // Method to compute current portfolio value
        loadTransactions(); // Loads and processes all transaction data first

        // Calculates the sum of all holding values using Stream API
        portfolioTotalValue = totalValues.values().stream()
                .mapToDouble(Double::doubleValue) // Converts each value to primitive double
                .sum(); // Aggregates all values into a total
    }

    /**
     * Loads and processes transactions from the storage file.
     * This method reads transaction data, clears existing holdings, and updates the portfolio state. Made by AI.
     */
    public void loadTransactions() { // Method to retrieve transaction data from file
        // Clears existing data to prevent duplication
        holdings.clear();
        totalValues.clear();

        // Constructs the path to the transaction data file
        String filePath = System.getProperty("user.home") + "/investifyData.json";
        File file = new File(filePath);

        if (file.exists()) { // Checks if the data file exists before proceeding
            try (FileReader reader = new FileReader(file)) { // Opens the file with auto-closing
                // Uses the transaction service to parse the JSON data
                Transaction[] transactions = TransactionService.readTransactionsFromFile(file);

                if (transactions != null) { // Ensures data was successfully loaded
                    processTransactions(transactions); // Processes the transaction array
                }
            } catch (Exception e) { // Handles any exceptions during file operations
                // Shows an error dialog to the user if transaction loading fails
                JOptionPane.showMessageDialog(
                        null,
                        "An error occurred while loading transactions:\n" + e.getMessage(),
                        "Load Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    /**
     * Processes an array of transactions to update holdings and total values.
     * This method calculates the net quantity and value for each stock in the portfolio. Made by AI.
     *
     * @param transactions The array of transactions to process
     */
    private void processTransactions(Transaction[] transactions) { // Method to analyze transactions and update portfolio state
        for (Transaction transaction : transactions) { // Iterates through each transaction
            String symbol = transaction.getSymbol(); // Gets the stock ticker symbol
            int quantity = transaction.getQuantity(); // Gets the number of shares
            double price = transaction.getPrice(); // Gets the price per share
            boolean isBuy = transaction.getAction().equalsIgnoreCase("Buy"); // Determines transaction type

            // Updates quantities in holdings map, adding for buys and subtracting for sells
            holdings.put(symbol, holdings.getOrDefault(symbol, 0) +
                    (isBuy ? quantity : -quantity));

            // Updates monetary values, adding for buys and subtracting for sells
            double transactionValue = quantity * price; // Calculates total value of this transaction
            totalValues.put(symbol, totalValues.getOrDefault(symbol, 0.0) +
                    (isBuy ? transactionValue : -transactionValue));
        }

        // Removes stocks that have been completely sold (zero or negative quantities)
        holdings.entrySet().removeIf(entry -> entry.getValue() <= 0);
        // Removes values for stocks no longer in holdings or with negative values
        totalValues.entrySet().removeIf(entry -> entry.getValue() <= 0 ||
                !holdings.containsKey(entry.getKey()));
    }

    /**
     * Gets the map of stock quantities in the portfolio.
     *
     * @return A map where the key is the stock symbol and the value is the quantity
     */
    public Map<String, Integer> getHoldings() {
        return holdings; // Returns reference to current holdings
    }

    /**
     * Gets the map of stock values in the portfolio.
     *
     * @return A map where the key is the stock symbol and the value is the monetary value
     */
    public Map<String, Double> getTotalValues() {
        return totalValues; // Returns reference to current values
    }

    /**
     * Gets the total value of the portfolio.
     *
     * @return The total portfolio value as a double
     */
    public double getPortfolioTotalValue() {
        return portfolioTotalValue; // Returns the calculated total value
    }
}