/**
 * Represents a stock transaction in the Investify application.
 * Stores information about buy/sell actions, including the stock symbol,
 * quantity of shares, and price per share.
 * This class is used for recording and tracking user investment activities.
 */
public class Transaction {
    public String action; // The type of transaction ("Buy" or "Sell")
    public String symbol; // The stock symbol/ticker (e.g., "AAPL", "MSFT")
    public int quantity; // Number of shares in the transaction
    public double price; // Price per share in USD

    /**
     * Creates a new transaction with the specified details.
     *
     * @param action   Type of transaction ("Buy" or "Sell")
     * @param symbol   Stock symbol/ticker
     * @param quantity Number of shares
     * @param price    Price per share in USD
     */
    public Transaction(String action, String symbol, int quantity, double price) {
        this.action = action;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
    }

    // return The type of transaction ("Buy" or "Sell")
    public String getAction() {
        return action;
    }

    // return The stock symbol/ticker
    public String getSymbol() {
        return symbol;
    }

    // return Number of shares in the transaction
    public int getQuantity() {
        return quantity;
    }

    // return Price per share in USD
    public double getPrice() {
        return price;
    }
}