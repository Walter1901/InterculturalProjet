package Finance.model;

import java.time.LocalDate;

// This class is for a saving goal, like "Save $1000 by this date"
public class Goal {

    // I store the start and end dates as Strings (in ISO format yyyy-MM-dd)
    // I do this to make it easier to save/load later (like in a file or DB)
    private String startDate;
    private String endDate;
    private String name; // Name of the goal
    private double currentAmount; // How much money is already saved
    private double targetAmount; // The total money goal we want to reach

    // Constructor to make a new goal
    // It takes the name, start and end dates (as LocalDate), and amounts
    public Goal(String name, LocalDate startDate, LocalDate endDate, double currentAmount, double targetAmount) {
        this.name = name;
        // Convert the dates to String format to store internally
        setStartDateFromLocalDate(startDate);
        setEndDateFromLocalDate(endDate);
        this.currentAmount = currentAmount;
        this.targetAmount = targetAmount;
    }

    // Get the start date as a LocalDate by parsing the stored String
    // If startDate is null, just return null so we don't crash
    public LocalDate getStartDate() {
        return startDate == null ? null : LocalDate.parse(startDate);
    }

    // Set start date from a LocalDate, save it as a String
    // If the input is null, set the stored date to null
    public void setStartDateFromLocalDate(LocalDate date) {
        this.startDate = (date == null) ? null : date.toString(); // format: yyyy-MM-dd
    }

    // Same for end date — get it as LocalDate by parsing the stored string
    public LocalDate getEndDate() {
        return endDate == null ? null : LocalDate.parse(endDate);
    }

    // Set the end date from a LocalDate and save as String
    public void setEndDateFromLocalDate(LocalDate date) {
        this.endDate = (date == null) ? null : date.toString();
    }

    // Just a getter for the goal's name
    public String getName() {
        return name;
    }

    // Get how much money is saved so far
    public double getCurrentAmount() {
        return currentAmount;
    }

    // Get the target amount to save
    public double getTargetAmount() {
        return targetAmount;
    }

    // Add some money to the current amount, like when you save more
    public void addAmount(double amount) {
        this.currentAmount += amount;
    }
}