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
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.InputStream;
import java.net.URI;

public class WebInstructionWindow {

    public static void showUrl(String titleText, String url) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(titleText);
        stage.setWidth(1100);
        stage.setHeight(800);
        stage.setMinWidth(850);
        stage.setMinHeight(600);

        setAppIcon(stage);

        WebView webView = new WebView();
        webView.setContextMenuEnabled(true);
        webView.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #dbeafe;" +
                        "-fx-border-width: 1 0 0 0;"
        );

        WebEngine engine = webView.getEngine();

        ProgressIndicator loading = new ProgressIndicator();
        loading.setMaxSize(24, 24);
        loading.setStyle("-fx-progress-color: #3b82f6;");
        loading.visibleProperty().bind(engine.getLoadWorker().runningProperty());
        loading.managedProperty().bind(loading.visibleProperty());

        Label titleLabel = new Label("Інструкція");
        titleLabel.setStyle(
                "-fx-text-fill: #0f172a;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 17px;" +
                        "-fx-font-weight: bold;"
        );

        Label statusLabel = new Label("Завантаження...");
        statusLabel.setStyle(
                "-fx-text-fill: #64748b;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 13px;"
        );

        Button reloadBtn = new Button("🔄 Оновити");
        Button browserBtn = new Button("🌐 В браузері");
        Button closeBtn = new Button("✖ Закрити");

        reloadBtn.setCursor(Cursor.HAND);
        browserBtn.setCursor(Cursor.HAND);
        closeBtn.setCursor(Cursor.HAND);

        reloadBtn.setStyle(getPrimaryButtonStyle("#3b82f6"));
        browserBtn.setStyle(getSoftButtonStyle("#ecfdf5", "#059669", "#a7f3d0"));
        closeBtn.setStyle(getSoftButtonStyle("#fef2f2", "#dc2626", "#fecaca"));

        reloadBtn.setOnMouseEntered(e -> reloadBtn.setStyle(getPrimaryButtonStyle("#2563eb")));
        reloadBtn.setOnMouseExited(e -> reloadBtn.setStyle(getPrimaryButtonStyle("#3b82f6")));

        browserBtn.setOnMouseEntered(e -> browserBtn.setStyle(getSolidButtonStyle("#10b981")));
        browserBtn.setOnMouseExited(e -> browserBtn.setStyle(getSoftButtonStyle("#ecfdf5", "#059669", "#a7f3d0")));

        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(getSolidButtonStyle("#ef4444")));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(getSoftButtonStyle("#fef2f2", "#dc2626", "#fecaca")));

        reloadBtn.setOnAction(e -> engine.reload());
        browserBtn.setOnAction(e -> openInBrowser(url));
        closeBtn.setOnAction(e -> stage.close());

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.RUNNING) {
                statusLabel.setText("Завантаження...");
                statusLabel.setStyle(
                        "-fx-text-fill: #64748b;" +
                                "-fx-font-family: 'Segoe UI';" +
                                "-fx-font-size: 13px;"
                );
            } else if (newState == Worker.State.SUCCEEDED) {
                statusLabel.setText("Інструкція відкрита");
                statusLabel.setStyle(
                        "-fx-text-fill: #059669;" +
                                "-fx-font-family: 'Segoe UI';" +
                                "-fx-font-size: 13px;" +
                                "-fx-font-weight: bold;"
                );
            } else if (newState == Worker.State.FAILED) {
                statusLabel.setText("Не вдалося завантажити сторінку");
                statusLabel.setStyle(
                        "-fx-text-fill: #dc2626;" +
                                "-fx-font-family: 'Segoe UI';" +
                                "-fx-font-size: 13px;" +
                                "-fx-font-weight: bold;"
                );
            }
        });

        HBox statusBox = new HBox(10, loading, statusLabel);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(12, 18, 12, 18));
        topBar.setStyle(
                "-fx-background-color: rgba(255,255,255,0.97);" +
                        "-fx-border-color: #dbeafe;" +
                        "-fx-border-width: 0 0 1 0;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.08), 14, 0, 0, 4);"
        );

        topBar.getChildren().addAll(
                titleLabel,
                statusBox,
                spacer,
                reloadBtn,
                browserBtn,
                closeBtn
        );

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8fafc, #eaf6ff);");
        root.setTop(topBar);
        root.setCenter(webView);

        Scene scene = new Scene(root);
        stage.setScene(scene);

        engine.load(url);

        stage.show();
    }

    // Доданий метод для встановлення іконки
    private static void setAppIcon(Stage stage) {
        try (InputStream iconStream = WebInstructionWindow.class.getResourceAsStream("/images/physics-icon.png")) {
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception e) {
            System.err.println("Не вдалося завантажити іконку програми: " + e.getMessage());
        }
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

    private static String getPrimaryButtonStyle(String color) {
        return "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 8 15 8 15;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(59,130,246,0.24), 10, 0, 0, 4);";
    }

    private static String getSoftButtonStyle(String background, String text, String border) {
        return "-fx-background-color: " + background + ";" +
                "-fx-text-fill: " + text + ";" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: " + border + ";" +
                "-fx-border-radius: 10;" +
                "-fx-padding: 8 15 8 15;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;";
    }

    private static String getSolidButtonStyle(String color) {
        return "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: " + color + ";" +
                "-fx-border-radius: 10;" +
                "-fx-padding: 8 15 8 15;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;";
    }
}