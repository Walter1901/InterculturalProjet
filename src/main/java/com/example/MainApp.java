package com.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Hauptcontainer
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // Menüzeile
        Label menuLabel = new Label("Menu → My expenses");

        // Trennlinie
        Separator separator = new Separator();

        // Überschrift
        Label titleLabel = new Label("Balance overview");

        // Checkboxen
        CheckBox savingCheck = new CheckBox("Saving goals");
        CheckBox investmentCheck = new CheckBox("Investment");
        CheckBox homeCheck = new CheckBox("Home");
        CheckBox stockCheck = new CheckBox("Stock");
        stockCheck.setSelected(true);
        CheckBox accountCheck = new CheckBox("Account");

        // Alle Komponenten hinzufügen
        root.getChildren().addAll(
                menuLabel,
                separator,
                titleLabel,
                savingCheck,
                investmentCheck,
                homeCheck,
                stockCheck,
                accountCheck
        );

        // Fenster erstellen
        Scene scene = new Scene(root, 300, 400);
        primaryStage.setTitle("Finance Tracker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}