/*
 * Проєкт: Лабораторний практикум з фізики.
 * Клас: WebInstructionWindow.
 * Призначення: Вікно для відображення HTML-інструкцій до конкретної лабораторної роботи.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.core.utils;

import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;

public class WebInstructionWindow {

    public static void showUrl(String titleText, String url) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(titleText);
        stage.setWidth(1100);
        stage.setHeight(800);

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        ProgressIndicator loading = new ProgressIndicator();
        loading.setMaxSize(28, 28);

        Label statusLabel = new Label("Завантаження...");
        statusLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Button reloadBtn = new Button("🔄 Оновити");
        Button browserBtn = new Button("🌐 Відкрити в браузері");
        Button closeBtn = new Button("✖ Закрити");

        reloadBtn.setOnAction(e -> engine.reload());
        browserBtn.setOnAction(e -> openInBrowser(url));
        closeBtn.setOnAction(e -> stage.close());

        reloadBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
        browserBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
        closeBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");

        loading.visibleProperty().bind(engine.getLoadWorker().runningProperty());

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.RUNNING) {
                statusLabel.setText("Завантаження...");
            } else if (newState == Worker.State.SUCCEEDED) {
                statusLabel.setText("Інструкція відкрита");
            } else if (newState == Worker.State.FAILED) {
                statusLabel.setText("Не вдалося завантажити сторінку");
            }
        });

        HBox topBar = new HBox(10, reloadBtn, browserBtn, closeBtn, loading, statusLabel);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #1e293b;");

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(webView);

        Scene scene = new Scene(root);
        stage.setScene(scene);

        engine.load(url);

        stage.show();
    }

    private static void openInBrowser(String url) {
        try {
            if (!Desktop.isDesktopSupported()) {
                showError("Браузер недоступний", "Не вдалося відкрити системний браузер.");
                return;
            }

            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            showError("Помилка", "Не вдалося відкрити посилання в браузері.");
        }
    }

    private static void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(content);
        alert.showAndWait();
    }
}