package com.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
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

        // 1. Begrüßung (Component 1)
        Label greeting = new Label("Hello User");

        // 2. Trennlinie
        Separator separator1 = new Separator();

        // 3. Finanz-Tracker Überschrift
        Label title = new Label("Finance Tracker");

        // 4. Menüzeile
        Label menu = new Label("Menu → My expenses");

        // 5. Weitere Trennlinie
        Separator separator2 = new Separator();

        // 6. Kontostand-Übersicht
        Label balance = new Label("Balance overview");

        // 7. Optionen
        Label options = new Label("Saving goals   Investment   Home   Stock   Account");

        // Alle Komponenten hinzufügen
        root.getChildren().addAll(
                greeting,
                separator1,
                title,
                menu,
                separator2,
                balance,
                options
        );

        // Fenster erstellen und anzeigen
        Scene scene = new Scene(root, 300, 250);
        primaryStage.setTitle("Finance Tracker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}