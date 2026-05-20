/*
 * Проєкт: Лабораторний практикум з фізики.
 * Клас: BrigadeSelectionScreen.
 * Призначення: Екран вибору бригади перед початком роботи з застосунком.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.core.brigade;

import dev.ua._klaidi4_.physics.core.DashboardController;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.io.InputStream;

public class BrigadeSelectionScreen extends BorderPane {

    private final Runnable onLoginSuccess;

    public BrigadeSelectionScreen(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        initUI();
    }

    private void initUI() {
        setStyle("-fx-background-color: linear-gradient(to bottom right, #f8fafc, #eaf6ff);");

        StackPane rootPane = new StackPane();
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setPadding(new Insets(35));

        Pane backgroundPane = new Pane();
        backgroundPane.setMouseTransparent(true);

        Circle circleLeft = new Circle(120);
        circleLeft.setStyle("-fx-fill: rgba(59, 130, 246, 0.07);");
        circleLeft.setLayoutX(170);
        circleLeft.setLayoutY(145);

        Circle circleRight = new Circle(165);
        circleRight.setStyle("-fx-fill: rgba(14, 165, 233, 0.06);");
        circleRight.setLayoutX(1120);
        circleRight.setLayoutY(690);

        Circle circleSmall = new Circle(55);
        circleSmall.setStyle("-fx-fill: rgba(37, 99, 235, 0.055);");
        circleSmall.setLayoutX(950);
        circleSmall.setLayoutY(125);

        Circle circleSoft = new Circle(38);
        circleSoft.setStyle("-fx-fill: rgba(59, 130, 246, 0.045);");
        circleSoft.setLayoutX(735);
        circleSoft.setLayoutY(580);

        backgroundPane.getChildren().addAll(circleLeft, circleRight, circleSmall, circleSoft);

        VBox centerBox = new VBox(13);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setMaxWidth(500);
        centerBox.setPrefWidth(500);
        centerBox.setMaxHeight(500);
        centerBox.setPrefHeight(500);
        centerBox.setPadding(new Insets(32, 46, 32, 46));
        centerBox.setStyle(
                "-fx-background-color: rgba(255,255,255,0.96);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: rgba(203,213,225,0.85);" +
                        "-fx-border-radius: 18;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.13), 24, 0, 0, 10);"
        );

        Label topLabel = new Label("Віртуальна лабораторія");
        topLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        topLabel.setStyle(
                "-fx-text-fill: #2563eb;" +
                        "-fx-background-color: #eff6ff;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 6 15 6 15;"
        );

        ImageView physicsIcon = createImage("/images/physics-icon.png", 72);
        physicsIcon.setOpacity(0.98);

        StackPane iconBox = new StackPane(physicsIcon);
        iconBox.setPrefSize(100, 100);
        iconBox.setMaxSize(100, 100);
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #f8fafc, #eff6ff);" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-color: #dbeafe;" +
                        "-fx-border-radius: 24;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(59,130,246,0.18), 18, 0, 0, 7);"
        );

        Label titleLabel = new Label("Лабораторний Практикум");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 27));
        titleLabel.setStyle("-fx-text-fill: #0f172a;");

        Label subtitleLabel = new Label("Оберіть вашу бригаду для початку роботи");
        subtitleLabel.setFont(Font.font("Segoe UI", 14));
        subtitleLabel.setStyle("-fx-text-fill: #64748b;");

        Label authorsLabel = new Label("Created by _Klaidi4_, Ankai, 7ei");
        authorsLabel.setFont(Font.font("Segoe UI", 12));
        authorsLabel.setStyle("-fx-text-fill: #94a3b8;");
        authorsLabel.setWrapText(true);
        authorsLabel.setAlignment(Pos.CENTER);

        ComboBox<String> brigadeComboBox = new ComboBox<>();
        brigadeComboBox.getItems().addAll(BrigadeConfig.getBrigades());
        brigadeComboBox.setPromptText("Виберіть бригаду...");
        brigadeComboBox.setPrefWidth(320);
        brigadeComboBox.setPrefHeight(45);
        brigadeComboBox.setVisibleRowCount(7);
        brigadeComboBox.setStyle(
                "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-color: #f8fafc;" +
                        "-fx-border-color: #cbd5e1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 4 8 4 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.05), 10, 0, 0, 4);"
        );

        brigadeComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText("Виберіть бригаду...");
                    setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #0f172a; -fx-font-size: 14px;");
                }
            }
        });

        brigadeComboBox.setCellFactory(listView -> {
            listView.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: #cbd5e1;" +
                            "-fx-border-radius: 10;" +
                            "-fx-padding: 4;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.16), 16, 0, 0, 8);"
            );

            return new ListCell<>() {
                {
                    setFont(Font.font("Segoe UI", 14));
                    hoverProperty().addListener((obs, oldValue, newValue) -> updateCellStyle());
                    selectedProperty().addListener((obs, oldValue, newValue) -> updateCellStyle());
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setStyle("-fx-background-color: white;");
                    } else {
                        setText(item);
                        updateCellStyle();
                    }
                }

                private void updateCellStyle() {
                    if (isEmpty() || getItem() == null) {
                        setStyle("-fx-background-color: white;");
                        return;
                    }

                    if (isSelected()) {
                        setStyle(
                                "-fx-background-color: #dbeafe;" +
                                        "-fx-text-fill: #1d4ed8;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-background-radius: 8;" +
                                        "-fx-padding: 9 12 9 12;"
                        );
                    } else if (isHover()) {
                        setStyle(
                                "-fx-background-color: #eff6ff;" +
                                        "-fx-text-fill: #0f172a;" +
                                        "-fx-background-radius: 8;" +
                                        "-fx-padding: 9 12 9 12;"
                        );
                    } else {
                        setStyle(
                                "-fx-background-color: white;" +
                                        "-fx-text-fill: #334155;" +
                                        "-fx-background-radius: 8;" +
                                        "-fx-padding: 9 12 9 12;"
                        );
                    }
                }
            };
        });

        Button continueBtn = new Button("Увійти");
        continueBtn.setPrefWidth(320);
        continueBtn.setPrefHeight(43);
        continueBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        continueBtn.setDisable(true);
        continueBtn.setStyle(
                "-fx-background-color: #bfdbfe;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-opacity: 0.75;"
        );

        Label hintLabel = new Label("Після вибору бригади відкриється головне меню");
        hintLabel.setFont(Font.font("Segoe UI", 11));
        hintLabel.setStyle("-fx-text-fill: #94a3b8;");

        brigadeComboBox.setOnAction(e -> {
            boolean isSelected = brigadeComboBox.getValue() != null;
            continueBtn.setDisable(!isSelected);

            if (isSelected) {
                continueBtn.setStyle(
                        "-fx-background-color: #3b82f6;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 10;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(59,130,246,0.28), 12, 0, 0, 5);"
                );
            } else {
                continueBtn.setStyle(
                        "-fx-background-color: #bfdbfe;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 10;" +
                                "-fx-opacity: 0.75;"
                );
            }
        });

        continueBtn.setOnMouseEntered(e -> {
            if (!continueBtn.isDisabled()) {
                continueBtn.setStyle(
                        "-fx-background-color: #2563eb;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 10;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(37,99,235,0.34), 14, 0, 0, 6);"
                );
            }
        });

        continueBtn.setOnMouseExited(e -> {
            if (!continueBtn.isDisabled()) {
                continueBtn.setStyle(
                        "-fx-background-color: #3b82f6;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 10;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(59,130,246,0.28), 12, 0, 0, 5);"
                );
            }
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

        centerBox.getChildren().addAll(
                topLabel,
                iconBox,
                titleLabel,
                subtitleLabel,
                authorsLabel,
                brigadeComboBox,
                continueBtn,
                hintLabel
        );

        rootPane.getChildren().addAll(backgroundPane, centerBox);
        setCenter(rootPane);
    }

    private ImageView createImage(String path, double size) {
        InputStream stream = getClass().getResourceAsStream(path);

        if (stream == null) {
            return new ImageView();
        }

        ImageView imageView = new ImageView(new Image(stream));
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        return imageView;
    }
}