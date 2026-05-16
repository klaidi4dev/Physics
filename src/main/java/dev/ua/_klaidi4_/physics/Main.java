/*
 * Проєкт: Лабораторний практикум з фізики.
 * Клас: Main.
 * Призначення: Головний клас програми, що запускає застосунок та ініціалізує основне вікно.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics;

import dev.ua._klaidi4_.physics.core.DashboardController;
import dev.ua._klaidi4_.physics.core.brigade.BrigadeSelectionScreen;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main {
    public static void main(String[] args) {
        Application.launch(Starter.class, args);
    }

    public static class Starter extends Application {
        private StackPane rootPane;

        @Override
        public void start(Stage primaryStage) {
            rootPane = new StackPane();

            showBrigadeSelection();

            Scene scene = new Scene(rootPane, 1150, 750);

            primaryStage.setTitle("Лабораторний практикум - Фізика");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);

            primaryStage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });

            primaryStage.show();
        }

        private void showBrigadeSelection() {
            BrigadeSelectionScreen selectionScreen = new BrigadeSelectionScreen(this::showDashboard);
            rootPane.getChildren().setAll(selectionScreen);
        }

        private void showDashboard() {
            DashboardController dashboard = new DashboardController(this::showBrigadeSelection);
            rootPane.getChildren().setAll(dashboard);
        }
    }
}