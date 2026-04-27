package dev.ua._klaidi4_.physics.core.brigade;

import dev.ua._klaidi4_.physics.core.DashboardController;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class BrigadeSelectionScreen extends BorderPane {

    private final Runnable onLoginSuccess;

    public BrigadeSelectionScreen(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        initUI();
    }

    private void initUI() {
        this.setStyle("-fx-background-color: #f1f5f9;");

        VBox centerBox = new VBox(25);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setMaxWidth(450);
        centerBox.setMaxHeight(350);
        centerBox.setPadding(new Insets(40));
        centerBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 20, 0, 0, 10);"
        );

        Label titleLabel = new Label("Лабораторний Практикум");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        titleLabel.setStyle("-fx-text-fill: #0f172a;");

        Label subtitleLabel = new Label("Оберіть вашу бригаду для початку роботи");
        subtitleLabel.setFont(Font.font("Segoe UI", 14));
        subtitleLabel.setStyle("-fx-text-fill: #64748b;");

        ComboBox<String> brigadeComboBox = new ComboBox<>();
        brigadeComboBox.getItems().addAll(BrigadeConfig.getBrigades());
        brigadeComboBox.setPromptText("Виберіть бригаду...");
        brigadeComboBox.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; " +
                        "-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; " +
                        "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 5;"
        );
        brigadeComboBox.setPrefWidth(300);

        Button continueBtn = new Button("Увійти");
        continueBtn.setPrefWidth(300);
        continueBtn.setPrefHeight(40);
        continueBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        continueBtn.setStyle(
                "-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;"
        );
        continueBtn.setDisable(true);

        brigadeComboBox.setOnAction(e -> {
            boolean isSelected = brigadeComboBox.getValue() != null;
            continueBtn.setDisable(!isSelected);
        });

        continueBtn.setOnAction(e -> {
            String selectedBrigade = brigadeComboBox.getValue();
            DashboardController.setCurrentBrigade(selectedBrigade);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> onLoginSuccess.run());
            fadeOut.play();
        });

        continueBtn.setOnMouseEntered(e -> {
            if (!continueBtn.isDisabled()) {
                continueBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
            }
        });
        continueBtn.setOnMouseExited(e -> {
            if (!continueBtn.isDisabled()) {
                continueBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
            }
        });

        centerBox.getChildren().addAll(titleLabel, subtitleLabel, brigadeComboBox, continueBtn);
        this.setCenter(centerBox);
    }
}