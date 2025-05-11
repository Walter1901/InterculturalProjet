public class Transaction {
    public String action;
    public String symbol;
    public int quantity;
    public double price;

    public Transaction(String action, String symbol, int quantity, double price) {
        this.action = action;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
    }

    public String getAction() { return action; }
    public String getSymbol() { return symbol; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
}
