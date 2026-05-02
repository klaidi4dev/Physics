package dev.ua._klaidi4_.physics.level7.lab7_8.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level7.lab7_8.model.Measurement;
import dev.ua._klaidi4_.physics.level7.lab7_8.view.RebinderCanvas;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.LinkedList;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;

public class LabController78 extends BaseLabController {

    private RebinderCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> liquidCombo;
    private Button pumpBtn;
    private Button recordBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveHLabel;
    private AnimationTimer uiTimer;
    private boolean isPumping = false;
    private boolean isAutoRunning = false;
    private Queue<String> autoQueue = new LinkedList<>();
    private final double ALPHA_WATER = 72.8;
    private final double ALPHA_ETHANOL = 22.4;
    private final double ALPHA_GLYCERIN = 64.0;

    public LabController78() {
        initUI();
        updateLiquidInCanvas();
    }

    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        if (uiTimer != null) uiTimer.stop();
        isAutoRunning = false;
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(330);
        leftPanel.setMinWidth(330);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 7-8)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane configPane = new TitledPane();
        configPane.setText("Параметри установки");
        configPane.setCollapsible(false);
        VBox configBox = new VBox(12);
        configBox.setPadding(new Insets(5));

        liquidCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Дистильована вода", "Спирт етиловий", "Гліцерин"
        ));
        liquidCombo.getSelectionModel().selectFirst();
        liquidCombo.setMaxWidth(Double.MAX_VALUE);
        liquidCombo.setOnAction(e -> {
            updateLiquidInCanvas();
            updateStats();
        });

        configBox.getChildren().add(createInputGroup("Досліджувана рідина:", liquidCombo));
        configPane.setContent(configBox);

        pumpBtn = new Button("▶ ПОЧАТИ НАГНІТАННЯ ПОВІТРЯ");
        pumpBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        pumpBtn.setMaxWidth(Double.MAX_VALUE);
        pumpBtn.setOnAction(e -> togglePumping());

        recordBtn = new Button("📝 ЗАПИСАТИ МАКСИМАЛЬНИЙ h");
        recordBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        recordBtn.setMaxWidth(Double.MAX_VALUE);
        recordBtn.setDisable(true);
        recordBtn.setOnAction(e -> recordMeasurement(canvas.getCurrentH()));

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ ЛАБИ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAutoMode());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear();
            idCounter = 1;
            updateStats();
        });

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, configPane, pumpBtn, recordBtn, autoBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new RebinderCanvas(600, 440);
        canvas.setOnBubblePop(() -> {
            if (isPumping && !isAutoRunning) {
                Platform.runLater(() -> {
                    liveStatusLabel.setText("Статус: ВІДРИВ БУЛЬБАШКИ!");
                    liveStatusLabel.setStyle("-fx-text-fill: #ff9800;");
                });
            }
        });

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(220, 80);
        Label dashTitle = new Label("ДАТЧИК ТИСКУ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveHLabel = new Label("h = 0.0 мм");
        liveHLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        liveHLabel.setStyle("-fx-text-fill: #00ff00;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveHLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        table.getColumns().addAll(
                createCol("№", "id"),
                createCol("Рідина", "liquid"),
                createCol("h (мм)", "h"),
                createCol("α (мН/м)", "alpha")
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();

        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        uiTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                liveHLabel.setText(String.format("h = %.1f мм", canvas.getCurrentH()));
            }
        };
        uiTimer.start();
        updateStats();
    }

    private TableColumn<Measurement, Object> createCol(String title, String prop) {
        TableColumn<Measurement, Object> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        return col;
    }

    private void updateLiquidInCanvas() {
        String liq = liquidCombo.getValue();
        double theoreticalAlpha = ALPHA_WATER;
        if (liq.contains("Спирт")) theoreticalAlpha = ALPHA_ETHANOL;
        if (liq.contains("Гліцерин")) theoreticalAlpha = ALPHA_GLYCERIN;

        canvas.setLiquidParams(liq, theoreticalAlpha);
    }

    private void togglePumping() {
        isPumping = !isPumping;
        canvas.setPumping(isPumping);

        if (isPumping) {
            pumpBtn.setText("⏹ ЗУПИНИТИ НАГНІТАННЯ");
            pumpBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
            recordBtn.setDisable(false);
            liquidCombo.setDisable(true);
            liveStatusLabel.setText("Статус: НАГНІТАННЯ");
            liveStatusLabel.setStyle("-fx-text-fill: red;");
        } else {
            pumpBtn.setText("▶ ПОЧАТИ НАГНІТАННЯ ПОВІТРЯ");
            pumpBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
            recordBtn.setDisable(true);
            liquidCombo.setDisable(false);
            liveStatusLabel.setText("Статус: ОЧІКУВАННЯ");
            liveStatusLabel.setStyle("-fx-text-fill: yellow;");
        }
    }

    private void recordMeasurement(double measuredH) {
        String currentLiq = liquidCombo.getValue();
        double alphaValue = 0.0;
        double avgH0 = getAverageHForWater();

        if (currentLiq.equals("Дистильована вода")) {
            alphaValue = ALPHA_WATER;
        } else {
            if (avgH0 == 0) {
                showAlert("Увага", "Спочатку необхідно провести вимірювання для дистильованої води (еталон), щоб знайти h0!");
                return;
            }
            alphaValue = ALPHA_WATER * (measuredH / avgH0);
        }

        Measurement m = new Measurement(
                idCounter++,
                currentLiq,
                Math.round(measuredH * 10.0) / 10.0,
                Math.round(alphaValue * 100.0) / 100.0
        );

        data.add(m);
        table.scrollTo(data.size() - 1);
        updateStats();
    }

    private double getAverageHForWater() {
        double sumH = 0;
        int count = 0;
        for (Measurement m : data) {
            if (m.getLiquid().equals("Дистильована вода")) {
                sumH += m.getH();
                count++;
            }
        }
        return count > 0 ? sumH / count : 0.0;
    }

    private void startAutoMode() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        autoQueue.add("Дистильована вода");
        autoQueue.add("Дистильована вода");
        autoQueue.add("Дистильована вода");
        autoQueue.add("Спирт етиловий");
        autoQueue.add("Спирт етиловий");
        autoQueue.add("Спирт етиловий");
        autoQueue.add("Гліцерин");
        autoQueue.add("Гліцерин");
        autoQueue.add("Гліцерин");

        isAutoRunning = true;
        setControlsDisable(true);
        processNextAuto();
    }

    private void processNextAuto() {
        if (autoQueue.isEmpty() || !isAutoRunning) {
            isAutoRunning = false;
            setControlsDisable(false);
            if (isPumping) togglePumping();
            liveStatusLabel.setText("Статус: ГОТОВО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            return;
        }

        String nextLiq = autoQueue.poll();
        Platform.runLater(() -> {
            liquidCombo.getSelectionModel().select(nextLiq);
            updateLiquidInCanvas();
            if (!isPumping) togglePumping();
            liveStatusLabel.setText("Статус: АВТО-ВИМІРЮВАННЯ");
        });

        canvas.setOnBubblePop(() -> {
            if (isAutoRunning) {
                double noiseH = canvas.getCurrentH() + (Math.random() - 0.5) * 1.5;
                Platform.runLater(() -> recordMeasurement(noiseH));

                canvas.setOnBubblePop(null);

                new Thread(() -> {
                    try { Thread.sleep(800); } catch (Exception ignored) {}
                    Platform.runLater(this::processNextAuto);
                }).start();
            }
        });
    }

    private void updateStats() {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: Очікування вимірювань...\nПочніть з дистильованої води.");
            return;
        }
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }

        double avgH0 = getAverageHForWater();
        if (avgH0 == 0) {
            finalResultLabel.setText("Увага: Немає вимірів для еталонної рідини (води). Подальші розрахунки неможливі.");
            return;
        }

        StringBuilder sb = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ:\n");
        sb.append(String.format("1. Еталонна рідина (Дистильована вода): α0 = %.1f мН/м. Середнє h0 = %.1f мм.\n", ALPHA_WATER, avgH0));

        List<String> processedLiqs = new ArrayList<>();
        processedLiqs.add("Дистильована вода");

        for (Measurement m : data) {
            String liqName = m.getLiquid();
            if (!processedLiqs.contains(liqName)) {
                processedLiqs.add(liqName);

                double sumHx = 0;
                int countX = 0;
                for (Measurement innerM : data) {
                    if (innerM.getLiquid().equals(liqName)) {
                        sumHx += innerM.getH();
                        countX++;
                    }
                }
                double avgHx = sumHx / countX;
                double calcAlpha = ALPHA_WATER * (avgHx / avgH0);

                double theorAlpha = liqName.contains("Спирт") ? ALPHA_ETHANOL : ALPHA_GLYCERIN;
                double error = Math.abs(calcAlpha - theorAlpha) / theorAlpha * 100.0;

                sb.append(String.format("2. %s: Середнє hx = %.1f мм. Розраховано αx = %.1f мН/м. (Похибка %.1f%%)\n",
                        liqName, avgHx, calcAlpha, error));
            }
        }

        sb.append("Висновок: Розрахунок коефіцієнтів поверхневого натягу за методом Ребіндера показав збіжність з табличними значеннями.");
        finalResultLabel.setText(sb.toString());
    }

    private void setControlsDisable(boolean disable) {
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        liquidCombo.setDisable(disable);
        pumpBtn.setDisable(disable);
    }
}