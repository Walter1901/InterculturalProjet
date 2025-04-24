package com.example;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ExpenseManagerPage extends VBox {

    public ExpenseManagerPage() {
        setSpacing(10);
        setPadding(new Insets(15));

        // Header
        Label title = new Label("My expenses");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Controls
        HBox controls = new HBox(10);
        controls.getChildren().addAll(
                new Button("Add new expense"),
                new ComboBox<String>(),
                new ComboBox<String>()
        );

        // Total
        Label totalLabel = new Label("Total: CHF 2'344,35");
        totalLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Filter
        HBox filter = new HBox(10);
        filter.getChildren().addAll(
                new Button("Filter"),
                new Button("Classify")
        );

        // Expenses
        VBox expenses = new VBox(10);
        expenses.getChildren().addAll(
                createExpenseEntry("28.03.2025", "Coop", "Coop, Brig", "CHF 13.10"),
                createExpenseEntry("24.03.2025", "SBB Ticket Shop", "", "CHF 3.20"),
                createExpenseEntry("", "Müller, Brig", "", "CHF 3.70 - View 144 expenses")
        );

        // Navigation
        HBox nav = new HBox(20);
        nav.getChildren().addAll(
                new Label("Home"),
                new Label("Stock"),
                new Label("Account")
        );

        getChildren().addAll(
                title,
                controls,
                new Separator(),
                totalLabel,
                filter,
                new Separator(),
                expenses,
                new Separator(),
                nav
        );
    }

    private VBox createExpenseEntry(String date, String title, String subtitle, String amount) {
        VBox entry = new VBox(5);

        if (!date.isEmpty()) {
            Label dateLabel = new Label(date);
            dateLabel.setStyle("-fx-font-size: 12px;");
            entry.getChildren().add(dateLabel);
        }

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold;");

        if (!subtitle.isEmpty()) {
            Label subLabel = new Label(subtitle);
            entry.getChildren().addAll(titleLabel, subLabel);
        } else {
            entry.getChildren().add(titleLabel);
        }

        Label amountLabel = new Label(amount);
        entry.getChildren().add(amountLabel);

        return entry;
    }
}