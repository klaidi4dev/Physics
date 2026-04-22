package dev.ua._klaidi4_.physics;

import dev.ua._klaidi4_.physics.core.DashboardController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main {
    public static void main(String[] args) {
        Application.launch(Starter.class, args);
    }
    public static class Starter extends Application {
        @Override
        public void start(Stage primaryStage) {
            DashboardController root = new DashboardController();
            Scene scene = new Scene(root, 1150, 750);

            primaryStage.setTitle("Лабораторний практикум - Фізика");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);

            primaryStage.setOnCloseRequest(event -> {
                javafx.application.Platform.exit();
                System.exit(0);
            });

            primaryStage.show();
        }
    }
}