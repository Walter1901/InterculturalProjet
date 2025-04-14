package com.example;

public class Expense {
    private String description;
    private double amount;

    // Constructor, Getter & Setter
    public Expense(String description, double amount) {
        this.description = description;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return description + ": " + amount + " EUR";
    }
}