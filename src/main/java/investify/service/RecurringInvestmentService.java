package investify.service;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import investify.app.Investify;
import shared.RecurringInvestment;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.nio.file.Paths;

/**
 * Service for managing recurring investments in the Investify application.
 * <p>
 * This service is responsible for:
 * - Importing recurring investments from shared storage
 * - Executing recurring investments based on their scheduled frequency
 * - Maintaining execution history to prevent duplicate executions
 * - Processing investment transactions when due
 * <p>
 * The service works with the shared RecurringInvestment data structure
 * and ensures proper synchronization with the main application.
 */
public class RecurringInvestmentService {

    /**
     * Reference to the main application instance for accessing other services
     */
    private final Investify app;

    /**
     * Constructs a new RecurringInvestmentService with a reference to the main application.
     *
     * @param app The Investify application instance providing access to other services
     */
    public RecurringInvestmentService(Investify app) {
        this.app = app;
    }

    /**
     * Imports recurring investments from the shared storage file.
     * <p>
     * This method reads serialized investment objects from a file in the user's
     * home directory that is shared between applications.
     *
     * @return A list of recurring investments found in the shared storage
     */
    public List<RecurringInvestment> importRecurringInvestments() {
        List<RecurringInvestment> result = new java.util.ArrayList<>();  // Create empty list to store results
        String sharedFilePath = Paths.get(System.getProperty("user.home"), "recurringInvestments.dat").toString();  // Build path to shared file in user's home directory

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(sharedFilePath))) {  // Open object input stream with automatic resource management
            List<Object> importedObjects = (List<Object>) in.readObject();  // Read list of objects from file
            for (Object obj : importedObjects) {  // Iterate through all imported objects
                if (obj instanceof RecurringInvestment) {  // Check if object is a RecurringInvestment
                    RecurringInvestment sharedInv = (RecurringInvestment) obj;  // Cast to proper type
                    result.add(sharedInv);  // Add to result list
                }
            }
        } catch (Exception e) {
            // Ignore errors, return empty list if file not found or error occurred
        }
        return result;  // Return collected investments
    }

    /**
     * Executes all recurring investments that are due today.
     * <p>
     * This method checks all imported investments against their schedules
     * and executes those that should run today. It maintains an execution
     * history to prevent duplicate executions on the same day.
     *
     * @return The number of investments successfully executed
     */
    public int executeRecurringInvestments() {
        int executedCount = 0;  // Initialize counter for executed investments
        List<RecurringInvestment> investments = importRecurringInvestments();  // Import all recurring investments
        LocalDate today = LocalDate.now();  // Get current date

        Map<String, LocalDate> executionHistory = loadExecutionHistory();  // Load previous execution history

        for (RecurringInvestment investment : investments) {  // Process each investment
            String key = investment.getSymbol() + "-" + investment.getName();  // Create unique key for tracking
            if (shouldExecuteToday(investment, today)) {  // Check if investment should execute today
                LocalDate lastExecution = executionHistory.get(key);  // Get last execution date
                if (lastExecution == null || !lastExecution.equals(today)) {  // Only execute if not already done today
                    executeSingleInvestment(investment);  // Execute the investment
                    executedCount++;  // Increment success counter
                    executionHistory.put(key, today);  // Record execution in history
                }
            }
        }
        saveExecutionHistory(executionHistory);  // Save updated execution history
        return executedCount;  // Return number of executions
    }

    /**
     * Determines if a recurring investment should be executed today based on its schedule.
     * <p>
     * This method checks the investment's frequency (daily, weekly, monthly, yearly)
     * against the current date to determine if execution is due.
     *
     * @param investment The recurring investment to evaluate
     * @param today      The current date
     * @return true if the investment should execute today, false otherwise
     */
    private boolean shouldExecuteToday(RecurringInvestment investment, LocalDate today) {
        LocalDate startDate = investment.getStartDate();  // Get the start date of the investment
        if (startDate.isAfter(today)) return false;  // Don't execute if start date is in the future
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, today);  // Calculate days since start
        String freq = investment.getFrequency().toUpperCase();  // Get frequency in uppercase for comparison
        switch (freq) {  // Check each frequency type
            case "DAILY":
                return true;  // Daily investments execute every day
            case "WEEKLY":
                return days % 7 == 0;  // Weekly investments execute every 7 days
            case "MONTHLY":
                return startDate.getDayOfMonth() == today.getDayOfMonth();  // Monthly on same day of month
            case "YEARLY":
                return startDate.getDayOfYear() == today.getDayOfYear();  // Yearly on same day of year
            default:
                return false;  // Unknown frequency does not execute
        }
    }

    /**
     * Executes a single recurring investment by purchasing shares based on current price.
     *
     * @param investment The recurring investment to execute
     */
    private void executeSingleInvestment(RecurringInvestment investment) {
        // Extract the stock symbol from the investment object
        String symbol = investment.getSymbol();

        try {
            // Query AlphaVantage API to get current market data for the stock
            // Using synchronous request to fetch daily time series with compact output
            TimeSeriesResponse response = AlphaVantage.api()
                    .timeSeries()
                    .daily()
                    .forSymbol(symbol)
                    .outputSize(OutputSize.COMPACT)
                    .fetchSync();

            // Check if the API returned an error message
            if (response.getErrorMessage() != null) {
                System.err.println("API error: " + response.getErrorMessage());
                return; // Exit method if API returned an error
            }

            // Extract the most recent closing price from the time series data
            // Using Java streams to find first stock unit and get its close price
            // Defaults to 0.0 if no data is available
            double currentPrice = response.getStockUnits().stream()
                    .findFirst()
                    .map(unit -> unit.getClose())
                    .orElse(0.0);

            // Validate that we received a positive price value
            if (currentPrice <= 0) {
                System.err.println("Could not get valid price for " + symbol);
                return; // Exit if price is invalid
            }

            // Get configured investment amount and calculate shares to purchase
            // Integer division intentionally truncates fractional shares
            double amount = investment.getAmount();
            int quantity = (int) (amount / currentPrice);

            // Only proceed if we can purchase at least one full share
            if (quantity > 0) {
                // Record the buy transaction in the system using transaction service
                app.getTransactionService().saveTransaction("Buy", symbol, quantity, currentPrice);
                // Log successful investment execution with details
                System.out.println("Executed recurring investment: " + quantity +
                        " shares of " + symbol + " at " + currentPrice +
                        " (total: " + (quantity * currentPrice) + ")");
            } else {
                javax.swing.JOptionPane.showMessageDialog(
                        null,
                        "Investment amount too small to purchase shares of " + symbol,
                        "Investment Warning",
                        javax.swing.JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "Error executing investment for " + symbol + ": " + e.getMessage(),
                    "Investment Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Loads the execution history from persistent storage.
     * <p>
     * The execution history tracks which investments were already
     * executed on which dates to prevent duplicate executions.
     *
     * @return A map of investment keys to their last execution dates
     */
    private Map<String, LocalDate> loadExecutionHistory() {
        String path = Paths.get(System.getProperty("user.home"), "investifyExecutionHistory.dat").toString();  // Build path to history file
        File file = new File(path);  // Create file object
        if (!file.exists()) return new HashMap<>();  // Return empty map if file doesn't exist
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {  // Open input stream with automatic resource management
            return (Map<String, LocalDate>) in.readObject();  // Read and return the execution history map
        } catch (Exception e) {
            return new HashMap<>();  // Return empty map on error
        }
    }

    /**
     * Saves the execution history to persistent storage.
     * <p>
     * This method serializes the execution history map to a file
     * to maintain state between application runs.
     *
     * @param history The execution history map to save
     */
    private void saveExecutionHistory(Map<String, LocalDate> history) {
        String path = Paths.get(System.getProperty("user.home"), "investifyExecutionHistory.dat").toString();  // Build path to history file
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path))) {  // Open output stream with automatic resource management
            out.writeObject(history);  // Write the execution history map to the file
        } catch (Exception e) {
            System.err.println("Error saving execution history: " + e.getMessage());
        }
    }
}
