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
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
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
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.FontPosture;
import javafx.scene.CacheHint;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BrigadeSelectionScreen extends BorderPane {

    private final Runnable onLoginSuccess;

    public BrigadeSelectionScreen(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        initUI();
    }

    private void initUI() {
        getStyleClass().add("main-background");

        StackPane rootPane = new StackPane();
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setPadding(new Insets(35));

        Pane backgroundPane = new Pane();
        backgroundPane.setMouseTransparent(true);

        spawnFloatingFormulas(backgroundPane);

        VBox centerBox = new VBox(13);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setMaxWidth(500);
        centerBox.setPrefWidth(500);
        centerBox.setMaxHeight(500);
        centerBox.setPrefHeight(500);
        centerBox.setPadding(new Insets(32, 46, 32, 46));
        centerBox.getStyleClass().add("login-box");

        Label topLabel = new Label("Віртуальна лабораторія");
        topLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        topLabel.getStyleClass().add("badge-label");

        ImageView physicsIcon = createImage("/images/physics-icon.png", 72);
        physicsIcon.setOpacity(0.98);

        StackPane iconBox = new StackPane(physicsIcon);
        iconBox.setPrefSize(100, 100);
        iconBox.setMaxSize(100, 100);
        iconBox.setAlignment(Pos.CENTER);
        iconBox.getStyleClass().add("icon-box");

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
        brigadeComboBox.getStyleClass().add("combo-box-custom");

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
        continueBtn.getStyleClass().add("primary-btn");

        Label hintLabel = new Label("Після вибору бригади відкриється головне меню");
        hintLabel.setFont(Font.font("Segoe UI", 11));
        hintLabel.setStyle("-fx-text-fill: #94a3b8;");

        brigadeComboBox.setOnAction(e -> {
            boolean isSelected = brigadeComboBox.getValue() != null;
            continueBtn.setDisable(!isSelected);
        });

        ScaleTransition btnScaleUp = new ScaleTransition(Duration.millis(150), continueBtn);
        btnScaleUp.setToX(1.02);
        btnScaleUp.setToY(1.02);

        ScaleTransition btnScaleDown = new ScaleTransition(Duration.millis(150), continueBtn);
        btnScaleDown.setToX(1.0);
        btnScaleDown.setToY(1.0);

        continueBtn.setOnMouseEntered(e -> {
            if (!continueBtn.isDisabled()) {
                btnScaleUp.playFromStart();
            }
        });

        continueBtn.setOnMouseExited(e -> {
            if (!continueBtn.isDisabled()) {
                btnScaleDown.playFromStart();
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

    private void spawnFloatingFormulas(Pane pane) {
        String[] formulas = {
                "E = mc²", "F = m⋅a", "pV = νRT", "λ = h / p", 
                "ΔxΔp ≥ ℏ/2", "T = 2π√(l/g)", "F = G(m₁m₂)/r²", 
                "U = q + A", "I = U/R", "Φ = B⋅S⋅cosα"
        };
        Random random = new Random();
        List<Text> nodes = new ArrayList<>();
        double[] speedsX = new double[16];
        double[] speedsY = new double[16];

        for (int i = 0; i < 16; i++) {
            Text text = new Text(formulas[random.nextInt(formulas.length)]);
            text.setFont(Font.font("Times New Roman", FontPosture.ITALIC, 26 + random.nextInt(26))); 
            text.setStyle("-fx-fill: rgba(59, 130, 246, 0." + (10 + random.nextInt(15)) + ");");
            text.setCache(true);
            text.setCacheHint(CacheHint.SPEED);
            
            text.setX(random.nextDouble() * 1200);
            text.setY(random.nextDouble() * 800);
            
            pane.getChildren().add(text);
            nodes.add(text);
            
            speedsX[i] = (random.nextDouble() - 0.5) * 45; 
            speedsY[i] = (random.nextDouble() - 0.5) * 45; 

            RotateTransition rt = new RotateTransition(Duration.seconds(25 + random.nextInt(30)), text);
            rt.setByAngle((random.nextBoolean() ? 1 : -1) * 360);
            rt.setCycleCount(Animation.INDEFINITE);
            rt.setInterpolator(Interpolator.LINEAR);
            rt.play();
        }

        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double elapsedSeconds = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;

                for (int i = 0; i < nodes.size(); i++) {
                    Text text = nodes.get(i);
                    double nx = text.getX() + speedsX[i] * elapsedSeconds;
                    double ny = text.getY() + speedsY[i] * elapsedSeconds;
                    
                    if (nx > 1250) nx = -100;
                    if (nx < -100) nx = 1250;
                    if (ny > 850) ny = -50;
                    if (ny < -50) ny = 850;
                    
                    text.setX(nx);
                    text.setY(ny);
                }
            }
        };
        timer.start();
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