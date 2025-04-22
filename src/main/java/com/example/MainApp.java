package com.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainApp extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showMainPage();
    }

    private void showMainPage() {
        VBox root = new VBox(8);
        root.setPadding(new Insets(16));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #ffffff;");

        // Begrüßung
        Label greeting = new Label("Hello User");
        greeting.setStyle("-fx-font-size: 16px;");

        // Logo laden
        try {
            Image logo = new Image(getClass().getResourceAsStream("/logo.png"));
            ImageView logoView = new ImageView(logo);
            logoView.setFitHeight(100);
            logoView.setPreserveRatio(true);
            root.getChildren().add(logoView);
        } catch (Exception e) {
            System.out.println("Logo konnte nicht geladen werden");
        }

        // Menüzeile
        HBox menu = new HBox(5);
        Label menuArrow = new Label("Menu →");
        menuArrow.setStyle("-fx-font-size: 16px;");

        // Optionen - jetzt als klickbare Buttons
        VBox options = new VBox(5);

        Button expensesBtn = new Button("My expenses");
        expensesBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-alignment: center-left;");
        expensesBtn.setOnAction(e -> showExpenseManager());

        Button balanceBtn = new Button("Balance overview");
        balanceBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-alignment: center-left;");
        balanceBtn.setOnAction(e -> showBalanceOverview()); // Platzhalter

        Button savingsBtn = new Button("Saving goals");
        savingsBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-alignment: center-left;");
        savingsBtn.setOnAction(e -> showSavingGoals()); // Platzhalter

        Button investmentBtn = new Button("Investment");
        investmentBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-alignment: center-left;");
        investmentBtn.setOnAction(e -> showInvestment()); // Platzhalter

        options.getChildren().addAll(expensesBtn, balanceBtn, savingsBtn, investmentBtn);

        // Navigation (horizontal)
        HBox nav = new HBox(20);
        nav.setAlignment(Pos.CENTER);
        nav.getChildren().addAll(
                new Label("Home"),
                new Label("Stock"),
                new Label("Account")
        );

        root.getChildren().addAll(
                greeting,
                menu,
                new Separator(),
                options,
                new Separator(),
                nav
        );

        primaryStage.setScene(new Scene(root, 300, 400));
        primaryStage.setTitle("Finance Tracker");
        primaryStage.show();
    }

    private void showExpenseManager() {
        ExpenseManagerPage expensePage = new ExpenseManagerPage();
        Scene scene = new Scene(expensePage, 400, 600);

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> showMainPage());
        expensePage.getChildren().add(backButton);

        primaryStage.setScene(scene);
    }

    // Platzhalter-Methoden für die anderen Menüoptionen
    private void showBalanceOverview() {
        // Hier später die BalanceOverviewPage einbinden
        System.out.println("Balance overview wird gezeigt");
    }

    private void showSavingGoals() {
        // Hier später die SavingGoalsPage einbinden
        System.out.println("Saving goals wird gezeigt");
    }

    private void showInvestment() {
        // Hier später die InvestmentPage einbinden
        System.out.println("Investment wird gezeigt");
    }

    public static void main(String[] args) {
        launch(args);
    }
}