package com.example;

import java.util.Scanner;

public class Menu {
    public static void showMainMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- Menu ---");
            System.out.println("1. My Expenses");
            System.out.println("2. Balance Overview");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            switch (choice) {
                case 1 -> showExpenses();
                case 2 -> showBalanceOverview();
                case 3 -> System.exit(0);
                default -> System.out.println("Invalid option!");
            }
        }
    }

    private static void showExpenses() {
        System.out.println("\nMy Expenses:");
        // Hier würden echte Expenses aus einer Datenbank angezeigt werden
    }

    private static void showBalanceOverview() {
        System.out.println("\nBalance Overview:");
        System.out.println("- Saving Goals");
        System.out.println("- Investments");
    }
}
