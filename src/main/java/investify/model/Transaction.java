package investify.model;

/**
 * Represents a stock transaction in the Investify application.
 * Stores information about buy/sell actions, including the stock symbol,
 * quantity of shares, and price per share.
 * This class is used for recording and tracking user investment activities.
 */
public class Transaction { // Main class definition for representing financial transactions
    private String action; // Stores the transaction type ("Buy" or "Sell")
    private String symbol; // Stores the stock identifier code (ticker)
    private int quantity; // Stores the number of shares involved in the transaction
    private double price; // Stores the price per share in USD currency

    /**
     * Creates a new transaction with the specified details.
     *
     * @param action   Type of transaction ("Buy" or "Sell")
     * @param symbol   Stock symbol/ticker
     * @param quantity Number of shares
     * @param price    Price per share in USD
     */
    public Transaction(String action, String symbol, int quantity, double price) { // Constructor initializing all fields
        this.action = action; // Sets the action field with the provided transaction type
        this.symbol = symbol; // Sets the symbol field with the provided stock ticker
        this.quantity = quantity; // Sets the quantity field with the provided number of shares
        this.price = price; // Sets the price field with the provided price per share
    }

    /**
     * Gets the transaction type.
     *
     * @return The transaction type ("Buy" or "Sell")
     */
    public String getAction() {
        return action; // Returns the current action value
    }

    /**
     * Gets the stock symbol/ticker.
     *
     * @return The stock symbol
     */
    public String getSymbol() {
        return symbol; // Returns the current symbol value
    }

    /**
     * Gets the number of shares involved in this transaction.
     *
     * @return The quantity of shares
     */
    public int getQuantity() {
        return quantity; // Returns the current quantity value
    }

    /**
     * Gets the price per share in USD.
     *
     * @return The price per share
     */
    public double getPrice() {
        return price; // Returns the current price value
    }

    /**
     * Sets a new transaction type.
     *
     * @param action The new transaction type ("Buy" or "Sell")
     */
    public void setAction(String action) {
        this.action = action; // Updates the action field
    }

    /**
     * Sets a new stock symbol for this transaction.
     *
     * @param symbol The new stock symbol/ticker
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol; // Updates the symbol field
    }

    /**
     * Sets a new quantity of shares for this transaction.
     *
     * @param quantity The new number of shares
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity; // Updates the quantity field
    }

    /**
     * Sets a new price per share for this transaction.
     *
     * @param price The new price per share in USD
     */
    public void setPrice(double price) {
        this.price = price; // Updates the price field
    }

}