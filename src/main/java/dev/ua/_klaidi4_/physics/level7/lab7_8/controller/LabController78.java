package dev.ua._klaidi4_.physics.level7.lab7_8.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level7.lab7_8.model.Measurement;
import dev.ua._klaidi4_.physics.level7.lab7_8.view.SurfaceTensionCanvas;
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

import java.util.Locale;

public class LabController78 extends BaseLabController {

    private SurfaceTensionCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;

    private ComboBox<String> liquidBox;
    private Slider tempSlider;
    private Slider radiusSlider;
    private Slider pumpSpeedSlider;

    private Button pumpBtn;
    private Button recordBtn;
    private Button autoBtn;
    private Button clearBtn;

    private Label livePressureLabel;
    private Label peakPressureLabel;
    private Label statusLabel;

    // Фізичні константи (Коефіцієнти поверхневого натягу, мН/м)
    private final double ALPHA_WATER_20 = 72.8;
    private final double ALPHA_ETHANOL_20 = 22.0;
    private final double ALPHA_GLYCERIN_20 = 63.0;

    // Зменшення натягу при нагріванні на 1°C
    private final double D_ALPHA_WATER = 0.15;
    private final double D_ALPHA_ETHANOL = 0.08;
    private final double D_ALPHA_GLYCERIN = 0.06;

    private boolean isPumping = false;
    private boolean isAutoRunning = false;

    private double currentH = 0.0;
    private double maxTargetH = 0.0;
    private double lastPeakH = 0.0;
    private double noiseOffset = 0.0;

    private AnimationTimer simTimer;

    public LabController78() {
        initUI();
        updateTargetPressure();
    }

    @Override
    public void shutdown() {
        if (simTimer != null) simTimer.stop();
        isPumping = false;
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

        // --- ПАНЕЛЬ ВИБОРУ РІДИНИ ---
        TitledPane liquidPane = new TitledPane();
        liquidPane.setText("Досліджувана рідина");
        liquidPane.setCollapsible(false);

        liquidBox = new ComboBox<>(FXCollections.observableArrayList(
                "Дистильована вода (Еталон)",
                "Рідина №1 (Етиловий спирт)",
                "Рідина №2 (Гліцерин)"
        ));
        liquidBox.getSelectionModel().selectFirst();
        liquidBox.setMaxWidth(Double.MAX_VALUE);
        liquidBox.setOnAction(e -> {
            updateTargetPressure();
            currentH = 0.0;
            lastPeakH = 0.0;
            updateUI();
        });
        liquidPane.setContent(liquidBox);

        // --- ПАНЕЛЬ НАЛАШТУВАНЬ ---
        TitledPane configPane = new TitledPane();
        configPane.setText("Параметри установки");
        configPane.setCollapsible(true);

        VBox configBox = new VBox(10);
        configBox.setPadding(new Insets(5));

        Label tempLabel = new Label("Температура досліду t: 20 °C");
        tempSlider = new Slider(10.0, 80.0, 20.0);
        tempSlider.setMajorTickUnit(10.0);
        tempSlider.setShowTickMarks(true);
        tempSlider.valueProperty().addListener((o, ov, nv) -> {
            tempLabel.setText(String.format(Locale.US, "Температура досліду t: %.0f °C", nv.doubleValue()));
            updateTargetPressure();
            updateUI();
        });

        Label radiusLabel = new Label("Радіус капіляра R: 0.20 мм");
        radiusSlider = new Slider(0.1, 0.4, 0.2);
        radiusSlider.setMajorTickUnit(0.1);
        radiusSlider.setShowTickMarks(true);
        radiusSlider.valueProperty().addListener((o, ov, nv) -> {
            radiusLabel.setText(String.format(Locale.US, "Радіус капіляра R: %.2f мм", nv.doubleValue()));
            updateTargetPressure();
            updateUI();
        });

        Label pumpSpeedLabel = new Label("Потужність нагнітання: 1.0x");
        pumpSpeedSlider = new Slider(0.2, 3.0, 1.0);
        pumpSpeedSlider.setMajorTickUnit(0.5);
        pumpSpeedSlider.setShowTickMarks(true);
        pumpSpeedSlider.valueProperty().addListener((o, ov, nv) -> {
            pumpSpeedLabel.setText(String.format(Locale.US, "Потужність нагнітання: %.1fx", nv.doubleValue()));
        });

        configBox.getChildren().addAll(tempLabel, tempSlider, radiusLabel, radiusSlider, pumpSpeedLabel, pumpSpeedSlider);
        configPane.setContent(configBox);

        // --- КНОПКИ ---
        pumpBtn = new Button("💨 НАГНІТАТИ ПОВІТРЯ (НАСОС)");
        pumpBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        pumpBtn.setMaxWidth(Double.MAX_VALUE);
        pumpBtn.setOnAction(e -> togglePump());

        recordBtn = new Button("📝 ЗАПИСАТИ ПІКОВИЙ ТИСК (h_x)");
        recordBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        recordBtn.setMaxWidth(Double.MAX_VALUE);
        recordBtn.setOnAction(e -> recordMeasurement());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ ЛАБИ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAutoMode());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> resetLab());

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, liquidPane, configPane, pumpBtn, recordBtn, autoBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new SurfaceTensionCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        // --- ІНФО ПАНЕЛЬ ---
        VBox dash = new VBox(4);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-text-fill: #00ffcc; -fx-padding: 10; -fx-border-color: #00ffcc; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(220, 110);

        Label dashTitle = new Label("МАНОМЕТР");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        livePressureLabel = new Label("Поточний h: 0.0 мм");
        livePressureLabel.setStyle("-fx-text-fill: #a3e635; -fx-font-size: 14px;");

        peakPressureLabel = new Label("Піковий h: 0.0 мм");
        peakPressureLabel.setStyle("-fx-text-fill: #ff007f; -fx-font-size: 16px; -fx-font-weight: bold;");

        statusLabel = new Label("Статус: Очікування");
        statusLabel.setStyle("-fx-text-fill: #94a3b8;");

        dash.getChildren().addAll(dashTitle, livePressureLabel, peakPressureLabel, statusLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #000000;");

        // --- ТАБЛИЦЯ ДАНИХ ---
        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(40);

        TableColumn<Measurement, String> liqCol = new TableColumn<>("Рідина");
        liqCol.setCellValueFactory(new PropertyValueFactory<>("liquidName"));

        TableColumn<Measurement, Double> hCol = new TableColumn<>("Max тиск h_x (мм)");
        hCol.setCellValueFactory(new PropertyValueFactory<>("h"));

        TableColumn<Measurement, Double> alphaCol = new TableColumn<>("α експ (мН/м)");
        alphaCol.setCellValueFactory(new PropertyValueFactory<>("alpha"));

        table.getColumns().addAll(idCol, liqCol, hCol, alphaCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        startSimulationLoop();
    }

    private void updateTargetPressure() {
        int idx = liquidBox.getSelectionModel().getSelectedIndex();
        double t = tempSlider.getValue();
        double r = radiusSlider.getValue();

        double currentAlpha = 0;
        if (idx == 0) currentAlpha = ALPHA_WATER_20 - D_ALPHA_WATER * (t - 20.0);
        if (idx == 1) currentAlpha = ALPHA_ETHANOL_20 - D_ALPHA_ETHANOL * (t - 20.0);
        if (idx == 2) currentAlpha = ALPHA_GLYCERIN_20 - D_ALPHA_GLYCERIN * (t - 20.0);

        currentAlpha = Math.max(5.0, currentAlpha);

        double baseManometerScale = 0.4;
        double idealH = (currentAlpha / r) * baseManometerScale;

        noiseOffset = (Math.random() - 0.5) * 2.5;
        maxTargetH = idealH + noiseOffset;
    }

    private void togglePump() {
        isPumping = !isPumping;
        if (isPumping) {
            pumpBtn.setText("⏹ ЗУПИНИТИ НАСОС");
            pumpBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
            statusLabel.setText("Статус: Нагнітання...");
            statusLabel.setStyle("-fx-text-fill: #ffeb3b;");

            radiusSlider.setDisable(true);
            tempSlider.setDisable(true);
        } else {
            pumpBtn.setText("💨 НАГНІТАТИ ПОВІТРЯ (НАСОС)");
            pumpBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
            statusLabel.setText("Статус: Очікування");
            statusLabel.setStyle("-fx-text-fill: #94a3b8;");

            radiusSlider.setDisable(false);
            tempSlider.setDisable(false);
        }
    }

    private void startSimulationLoop() {
        simTimer = new AnimationTimer() {
            private long lastTime = 0;
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                physicsTick(dt);
                updateUI();
            }
        };
        simTimer.start();
    }

    private void physicsTick(double dt) {
        if (isPumping) {
            currentH += 40.0 * pumpSpeedSlider.getValue() * dt;

            if (currentH >= maxTargetH) {
                lastPeakH = maxTargetH;
                currentH = maxTargetH * 0.15;
                updateTargetPressure();

                if (!isAutoRunning) {
                    statusLabel.setText("Статус: ВІДРИВ БУЛЬБАШКИ!");
                    statusLabel.setStyle("-fx-text-fill: #ff007f;");
                }
            }
        } else {
            if (currentH > 0) {
                currentH = Math.max(0, currentH - 5.0 * dt);
            }
        }
    }

    private void updateUI() {
        livePressureLabel.setText(String.format(Locale.US, "Поточний h: %.1f мм", currentH));
        peakPressureLabel.setText(String.format(Locale.US, "Піковий h: %.1f мм", lastPeakH));

        canvas.updateState(liquidBox.getSelectionModel().getSelectedIndex(), currentH, maxTargetH,
                tempSlider.getValue(), radiusSlider.getValue());
    }

    private void recordMeasurement() {
        if (lastPeakH == 0) {
            showAlert("Увага", "Спочатку нагнітайте повітря доки бульбашка не відірветься!");
            return;
        }

        String liqName = liquidBox.getValue().split(" ")[0];

        double alphaCalc = 0.0;
        int idx = liquidBox.getSelectionModel().getSelectedIndex();
        double t = tempSlider.getValue();

        double refAlphaWater = ALPHA_WATER_20 - D_ALPHA_WATER * (t - 20.0);

        if (idx == 0) {
            alphaCalc = refAlphaWater;
        } else {
            double sumH0 = 0;
            int count0 = 0;
            for (Measurement m : data) {
                if (m.getLiquidName().contains("Дистильована")) {
                    sumH0 += m.getH();
                    count0++;
                }
            }

            if (count0 == 0) {
                showAlert("Помилка", "Для розрахунку невідомої рідини спочатку необхідно виміряти еталон (Воду) при тих самих умовах!");
                return;
            }

            double avgH0 = sumH0 / count0;
            alphaCalc = refAlphaWater * (lastPeakH / avgH0);
        }

        Measurement m = new Measurement(
                idCounter++,
                liqName,
                Math.round(lastPeakH * 10.0) / 10.0,
                Math.round(alphaCalc * 10.0) / 10.0
        );
        data.add(m);

        Platform.runLater(() -> {
            try { table.scrollTo(data.size() - 1); } catch (Exception e) {}
        });

        updateStats();
    }

    private void startAutoMode() {
        resetLab();
        isAutoRunning = true;
        autoBtn.setDisable(true);
        pumpBtn.setDisable(true);
        recordBtn.setDisable(true);
        liquidBox.setDisable(true);
        radiusSlider.setDisable(true);
        tempSlider.setDisable(true);
        pumpSpeedSlider.setValue(2.0); // Пришвидшуємо для авто-режиму

        new Thread(() -> {
            try {
                for (int liq = 0; liq < 3; liq++) {
                    final int currentLiq = liq;
                    Platform.runLater(() -> liquidBox.getSelectionModel().select(currentLiq));
                    Thread.sleep(500);

                    Platform.runLater(this::togglePump);

                    for (int i = 0; i < 3; i++) {
                        double oldPeak = lastPeakH;
                        while (lastPeakH == oldPeak && isAutoRunning) {
                            Thread.sleep(100);
                        }

                        if (!isAutoRunning) break;

                        Thread.sleep(200);
                        Platform.runLater(this::recordMeasurement);
                    }

                    Platform.runLater(this::togglePump);
                    Thread.sleep(1000);
                }

                Platform.runLater(() -> {
                    isAutoRunning = false;
                    autoBtn.setDisable(false);
                    pumpBtn.setDisable(false);
                    recordBtn.setDisable(false);
                    liquidBox.setDisable(false);
                    radiusSlider.setDisable(false);
                    tempSlider.setDisable(false);
                    statusLabel.setText("Статус: АВТО ЗАВЕРШЕНО");
                    statusLabel.setStyle("-fx-text-fill: #a3e635;");
                });

            } catch (InterruptedException ignored) {}
        }).start();
    }

    private void resetLab() {
        isAutoRunning = false;
        if (isPumping) togglePump();

        data.clear();
        idCounter = 1;
        currentH = 0.0;
        lastPeakH = 0.0;
        liquidBox.getSelectionModel().selectFirst();
        updateTargetPressure();
        updateUI();

        if (finalResultLabel != null) {
            finalResultLabel.setText("Обробка результатів: Очікування даних...");
        }
    }

    private void updateStats() {
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }

        double h0 = 0, h1 = 0, h2 = 0;
        int c0 = 0, c1 = 0, c2 = 0;

        for (Measurement m : data) {
            if (m.getLiquidName().contains("Дистильована")) { h0 += m.getH(); c0++; }
            if (m.getLiquidName().contains("Рідина №1")) { h1 += m.getH(); c1++; }
            if (m.getLiquidName().contains("Рідина №2")) { h2 += m.getH(); c2++; }
        }

        if (c0 == 0) {
            finalResultLabel.setText("СТАТУС: Для розрахунків необхідно спочатку виміряти еталон (Дистильовану воду).");
            return;
        }

        h0 /= c0;

        double t = tempSlider.getValue();
        double refAlphaWater = ALPHA_WATER_20 - D_ALPHA_WATER * (t - 20.0);

        String conclusion = String.format(Locale.US,
                "ОБРОБКА РЕЗУЛЬТАТІВ (Температура %.0f °C, R = %.2f мм):\n" +
                        "1. Еталон (Вода): h_0 = %.1f мм вод.ст. (Табличне α_0 = %.1f мН/м).\n", t, radiusSlider.getValue(), h0, refAlphaWater);

        if (c1 > 0) {
            h1 /= c1;
            double a1 = refAlphaWater * (h1 / h0);
            conclusion += String.format(Locale.US, "2. Етиловий спирт: h_x = %.1f мм. Розраховано α_1 = α_0(h_1/h_0) = %.1f мН/м.\n", h1, a1);
        }
        if (c2 > 0) {
            h2 /= c2;
            double a2 = refAlphaWater * (h2 / h0);
            conclusion += String.format(Locale.US, "3. Гліцерин: h_x = %.1f мм. Розраховано α_2 = α_0(h_2/h_0) = %.1f мН/м.\n", h2, a2);
        }

        conclusion += "ВИСНОВОК: Поверхневий натяг (α) падає при нагріванні і залежить від рідини. Зменшення радіуса капіляра (R) вимагає більшого тиску для відриву бульбашки (формула Лапласа: Δp = 2α/R).";

        finalResultLabel.setText(conclusion);
    }
}