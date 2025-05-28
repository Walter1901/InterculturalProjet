package shared;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Shared model class that represents a recurring investment.
 * This class is designed to transfer investment data between different applications
 * through serialization.
 */
public class RecurringInvestment implements Serializable {
    /** The name identifier of this recurring investment */
    private String name;

    /** The monetary amount to invest on each occurrence */
    private double amount;

    /** The frequency of the investment (e.g. "DAILY", "WEEKLY", "MONTHLY") */
    private String frequency;

    /** The date when this recurring investment begins */
    private LocalDate startDate;

    /** The stock/asset symbol this investment targets */
    private String symbol;

    /**
     * Constructs a new RecurringInvestment with the specified parameters.
     *
     * @param name      The name of the recurring investment
     * @param amount    The monetary amount for each investment
     * @param frequency How often the investment occurs
     * @param startDate When the recurring investment begins
     * @param symbol    The stock symbol or identifier for the investment
     */
    public RecurringInvestment(String name, double amount, String frequency,
                               LocalDate startDate, String symbol) {
        this.name = name;         // Initialize the investment name
        this.amount = amount;     // Initialize the investment amount
        this.frequency = frequency; // Initialize how often investment occurs
        this.startDate = startDate; // Initialize when investment begins
        this.symbol = symbol;     // Initialize the investment target symbol
    }

    /**
     * Returns the name of this recurring investment.
     * @return The investment name
     */
    public String getName() { return name; }

    /**
     * Returns the amount invested on each occurrence.
     * @return The investment amount
     */
    public double getAmount() { return amount; }

    /**
     * Returns the frequency at which this investment occurs.
     * @return The investment frequency
     */
    public String getFrequency() { return frequency; }

    /**
     * Returns the start date of this recurring investment.
     * @return The date when investment begins
     */
    public LocalDate getStartDate() { return startDate; }

    /**
     * Returns the symbol of the investment target.
     * @return The investment target symbol
     */
    public String getSymbol() { return symbol; }
}