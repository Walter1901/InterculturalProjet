package investify.service;

import investify.model.Transaction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service for managing transactions in the Investify application.
 * This service handles the creation, display, and persistence of stock transactions.
 * It provides functionality to display transaction dialogs for buying and selling stocks,
 * save transaction records to persistent storage, and read transaction history from storage.
 * All transaction data is stored in a JSON file in the user's home directory.
 */
public class TransactionService { // Main class for handling stock transaction operations
    private final PortfolioManager portfolioManager; // Reference to the portfolio manager for updating holdings

    /**
     * Constructs a new TransactionService with the required dependencies.
     *
     * @param portfolioManager The portfolio manager used to update holdings after transactions
     */
    public TransactionService(PortfolioManager portfolioManager) {
        this.portfolioManager = portfolioManager; // Stores reference to portfolio manager for future use
    }

    /**
     * Opens a dialog for creating buy or sell transactions.
     * This method displays a modal dialog with fields for entering transaction details.
     * The dialog includes the stock symbol, current price, and a field for quantity input.
     * Upon confirmation, the transaction is saved and the portfolio is updated.
     *
     * @param action The type of transaction ("Buy" or "Sell")
     * @param symbol The stock ticker symbol for the transaction
     * @param currentPrice The current price of the stock
     * @param parentPanel The parent panel to which this dialog will be attached
     */
    public void openTransactionDialog(String action, String symbol, double currentPrice, JPanel parentPanel) { // Method to display transaction UI dialog
        if (symbol.isEmpty() || currentPrice == 0.0) { // Validates that required data is available before proceeding
            JOptionPane.showMessageDialog(parentPanel,
                    "Please search for a valid symbol first.", // Error message text
                    "Error", JOptionPane.ERROR_MESSAGE); // Dialog title and type
            return; // Exits the method if validation fails
        }

        // Creates a modal dialog attached to the parent window
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(parentPanel),
                action + " Stocks", true); // Sets dialog title based on action type (Buy or Sell)
        dialog.setSize(350, 280); // Sets dimensions for the dialog window
        dialog.setLayout(new GridBagLayout()); // Uses GridBagLayout for flexible component positioning
        dialog.setLocationRelativeTo(parentPanel); // Centers dialog relative to the parent panel
        dialog.setResizable(false); // Prevents the dialog from being resized

        // Sets up layout constraints for positioning components
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Adds padding around components
        gbc.gridx = 0; // Sets initial horizontal position
        gbc.gridy = 0; // Sets initial vertical position
        gbc.fill = GridBagConstraints.HORIZONTAL; // Makes components fill horizontal space
        gbc.weightx = 1.0; // Gives horizontal weight to components

        // Creates and adds the stock symbol label
        JLabel symbolLabel = new JLabel("Symbol: " + symbol);
        symbolLabel.setFont(new Font("Inter", Font.BOLD, 16)); // Sets bold font for emphasis
        dialog.add(symbolLabel, gbc); // Adds the label to the dialog

        // Moves to the next row and adds price label
        gbc.gridy = 1; // Increments vertical position
        JLabel priceLabel = new JLabel(String.format("Price: %.2f $", currentPrice));
        priceLabel.setFont(new Font("Inter", Font.PLAIN, 16)); // Sets regular font
        dialog.add(priceLabel, gbc); // Adds the label to the dialog

        // Moves to the next row and adds quantity label
        gbc.gridy = 2; // Increments vertical position
        JLabel quantityLabel = new JLabel("Quantity to " + action.toLowerCase() + ":");
        quantityLabel.setFont(new Font("Inter", Font.PLAIN, 16)); // Sets regular font
        dialog.add(quantityLabel, gbc); // Adds the label to the dialog

        // Creates and configures the text field for quantity input
        JTextField quantityField = new JTextField(20); // Creates text field with column width of 20
        quantityField.setFont(new Font("Inter", Font.PLAIN, 18)); // Sets larger font for better readability
        quantityField.setPreferredSize(new Dimension(250, 40)); // Sets size for the text field
        gbc.gridy = 3; // Increments vertical position
        gbc.ipady = 10; // Adds internal vertical padding
        dialog.add(quantityField, gbc); // Adds the text field to the dialog

        // Creates and configures the confirmation button
        JButton confirmButton = new JButton("Confirm");
        confirmButton.setFont(new Font("Inter", Font.BOLD, 16)); // Sets bold font for emphasis
        confirmButton.setPreferredSize(new Dimension(150, 40)); // Sets button size
        gbc.gridy = 4; // Increments vertical position
        gbc.ipady = 5; // Reduces internal vertical padding
        gbc.insets = new Insets(20, 10, 10, 10); // Increases top margin for visual separation
        dialog.add(confirmButton, gbc); // Adds the button to the dialog

        // Defines the action when the confirm button is clicked
        confirmButton.addActionListener(e -> {
            String quantityText = quantityField.getText().trim(); // Gets and cleans the quantity input
            try {
                int quantity = Integer.parseInt(quantityText); // Parses the input as an integer
                if (quantity > 0) { // Validates that quantity is positive
                    // Creates and saves the transaction record
                    saveTransaction(action, symbol, quantity, currentPrice);

                    // Updates portfolio data to reflect new transaction
                    portfolioManager.loadTransactions();

                    // Displays success message and closes dialog
                    JOptionPane.showMessageDialog(dialog, "Transaction recorded successfully!");
                    dialog.dispose(); // Closes the dialog window
                } else {
                    // Shows error for invalid quantity (zero or negative)
                    JOptionPane.showMessageDialog(dialog,
                            "Quantity must be greater than 0.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) { // Catches parsing errors for non-numeric input
                JOptionPane.showMessageDialog(dialog,
                        "Please enter a valid number.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) { // Catches any other exceptions that might occur
                JOptionPane.showMessageDialog(dialog,
                        "Error while recording the transaction.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true); // Displays the dialog and blocks until it's closed
    }

    /**
     * Saves a transaction to persistent storage.
     * This method creates a new Transaction object and appends it to the existing
     * transaction history in the JSON data file. If the file doesn't exist yet,
     * it will be created. Uses the Gson library with pretty printing for JSON formatting. Made by AI.
     *
     * @param action The type of transaction ("Buy" or "Sell")
     * @param symbol The stock ticker symbol
     * @param quantity The number of shares in the transaction
     * @param price The price per share
     * @throws Exception If an error occurs during file operations
     */
    public void saveTransaction(String action, String symbol, int quantity, double price) throws Exception { // Method to record transaction in storage
        // Creates a new Transaction object with the provided details
        Transaction transaction = new Transaction(action, symbol, quantity, price);

        // Sets up Gson for JSON serialization with pretty printing
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String filePath = System.getProperty("user.home") + "/investifyData.json"; // Defines path to user data file
        File file = new File(filePath); // Creates a File object for the data file

        // Prepares list to hold transactions
        List<Transaction> transactions = new ArrayList<>();
        if (file.exists()) { // Checks if the data file already exists
            try (FileReader reader = new FileReader(file)) { // Opens file with auto-closing
                // Reads existing transactions into an array
                Transaction[] existing = gson.fromJson(reader, Transaction[].class);
                if (existing != null) transactions.addAll(Arrays.asList(existing)); // Adds existing transactions to list
            }
        } else {
            file.createNewFile(); // Creates a new file if none exists
        }

        // Adds the new transaction to the list
        transactions.add(transaction);
        try (FileWriter writer = new FileWriter(file)) { // Opens writer with auto-closing
            gson.toJson(transactions, writer); // Writes the updated transaction list to file as JSON
        }
    }

    /**
     * Reads transactions from the specified JSON file.
     * This method deserializes the JSON content into an array of Transaction objects.
     * It uses the Gson library to parse the JSON data structure. Made by AI.
     *
     * @param file The JSON file containing transaction data
     * @return An array of Transaction objects, or null if an error occurs during reading or parsing
     */
    public static Transaction[] readTransactionsFromFile(File file) { // Static utility method to load transactions
        try (FileReader reader = new FileReader(file)) { // Opens file reader with auto-closing
            Gson gson = new Gson(); // Creates Gson instance for JSON parsing
            return gson.fromJson(reader, Transaction[].class); // Parses JSON into array of Transaction objects
        } catch (Exception e) { // Catches any exception that occurs during file reading or parsing
            JOptionPane.showMessageDialog( // Displays an error dialog to the user
                    null, // No parent component for the dialog
                    "An error occurred while loading transactions.", // Error message shown to the user
                    "Error", // Title of the dialog window
                    JOptionPane.ERROR_MESSAGE // Specifies the dialog type as an error
            );
            return null; // Returns null if an error occurred
        }
    }
}