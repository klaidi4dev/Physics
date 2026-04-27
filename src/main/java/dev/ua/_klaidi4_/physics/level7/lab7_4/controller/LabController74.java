package dev.ua._klaidi4_.physics.level7.lab7_4.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level7.lab7_4.model.Measurement;
import dev.ua._klaidi4_.physics.level7.lab7_4.view.HeatConductionCanvas;
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
import java.util.Locale;
import java.util.Queue;

public class LabController74 extends BaseLabController {

    private HeatConductionCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;

    private Button toggleHeaterBtn;
    private Slider flowSlider;
    private Label flowLabel;
    private Slider timeMeasureSlider;
    private Label timeMeasureLabel;

    private ComboBox<String> metalComboBox;
    private Slider inputTempSlider;

    private Button startVolMeasureBtn;
    private Button autoBtn;
    private Button clearBtn;

    private Label liveStepLabel;
    private Label t1Label, t2Label, t3Label, t4Label;
    private Label deltaTLabel;
    private Label timerLabel, volLabel;

    private boolean isHeaterOn = false;
    private boolean isMeasuringVol = false;
    private boolean isAutoModeActive = false;

    private double currentT1 = 20.0, currentT2 = 20.0, currentT3 = 20.0, currentT4 = 20.0;
    private double heaterTemp = 20.0;
    private double currentVolumeMl = 0.0;

    private final double L_DISTANCE = 0.1;
    private final double S_AREA = 70.0e-6;
    private final double WATER_DENSITY = 1000.0;
    private final double WATER_C = 4180.0;

    private AnimationTimer physicsTimer, volTimer, autoTimer;
    private Queue<Double> autoQueue = new LinkedList<>();

    public LabController74() {
        initUI();
        startPhysicsEngine();
    }

    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        if (physicsTimer != null) physicsTimer.stop();
        if (volTimer != null) volTimer.stop();
        if (autoTimer != null) autoTimer.stop();
        autoQueue.clear();
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 7-4)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane configPane = new TitledPane();
        configPane.setText("Параметри установки");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(12);
        configBox.setPadding(new Insets(5));

        Label studentLabel = new Label("Робочі параметри:");
        studentLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

        toggleHeaterBtn = new Button("⚡ УВІМКНУТИ ПІЧ");
        toggleHeaterBtn.setStyle("-fx-background-color: #ff007f; -fx-text-fill: white; -fx-font-weight: bold;");
        toggleHeaterBtn.setMaxWidth(Double.MAX_VALUE);
        toggleHeaterBtn.setOnAction(e -> handleToggleHeater());

        flowLabel = new Label("Потік води в холодильнику: 0 %");
        flowSlider = new Slider(0.0, 100.0, 0.0);
        flowSlider.setShowTickMarks(true);
        flowSlider.setMajorTickUnit(20.0);
        flowSlider.valueProperty().addListener((o, ov, nv) -> {
            flowLabel.setText(String.format(Locale.US, "Потік води в холодильнику: %.0f %%", nv.doubleValue()));
        });

        timeMeasureLabel = new Label("Час заміру об'єму τ: 60 с");
        timeMeasureSlider = new Slider(30.0, 180.0, 60.0);
        timeMeasureSlider.setShowTickMarks(true);
        timeMeasureSlider.setMajorTickUnit(30.0);
        timeMeasureSlider.valueProperty().addListener((o, ov, nv) -> {
            timeMeasureLabel.setText(String.format(Locale.US, "Час заміру об'єму τ: %.0f с", nv.doubleValue()));
            if (!isMeasuringVol) timerLabel.setText(String.format(Locale.US, "Таймер: 0 / %.0f с", nv.doubleValue()));
        });

        Label envSettingsLabel = new Label("Налаштування середовища:");
        envSettingsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b; -fx-padding: 10 0 0 0;");

        metalComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "Мідь (k ≈ 390 Вт/м·К)",
                "Алюміній (k ≈ 230 Вт/м·К)",
                "Сталь (k ≈ 50 Вт/м·К)"
        ));
        metalComboBox.getSelectionModel().selectFirst();
        metalComboBox.setMaxWidth(Double.MAX_VALUE);
        metalComboBox.setOnAction(e -> {
            data.clear(); idCounter = 1; updateStats();
        });

        Label inputTempLabel = new Label("Температура води на вході t1:");
        inputTempSlider = new Slider(10.0, 25.0, 20.0);
        inputTempSlider.setShowTickMarks(true);
        inputTempSlider.valueProperty().addListener((o, ov, nv) -> {
            inputTempLabel.setText(String.format(Locale.US, "Температура води на вході t1: %.1f °C", nv.doubleValue()));
            currentT1 = nv.doubleValue();
        });

        configBox.getChildren().addAll(
                studentLabel, toggleHeaterBtn, flowLabel, flowSlider, timeMeasureLabel, timeMeasureSlider,
                new Separator(), envSettingsLabel, createInputGroup("Матеріал стрижня:", metalComboBox),
                inputTempLabel, inputTempSlider
        );

        ScrollPane scrollParams = new ScrollPane(configBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(320);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        configPane.setContent(scrollParams);

        startVolMeasureBtn = new Button("💧 ВИМІРЯТИ ОБ'ЄМ V ЗА ЧАС τ");
        startVolMeasureBtn.setStyle("-fx-background-color: #0288d1; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
        startVolMeasureBtn.setMaxWidth(Double.MAX_VALUE);
        startVolMeasureBtn.setOnAction(e -> startVolumeMeasurement());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ ЛАБИ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAutoMode());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear(); idCounter = 1; updateStats();
        });

        leftPanel.getChildren().addAll(title, configPane, startVolMeasureBtn, autoBtn, clearBtn);

        canvas = new HeatConductionCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(4);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-text-fill: #00e5ff; -fx-padding: 10; -fx-border-color: #00e5ff; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(260, 210);

        Label dashTitle = new Label("ДАНІ ВИМІРЮВАНЬ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        liveStepLabel = new Label("Статус: Очікування");
        liveStepLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");

        t1Label = new Label(); t1Label.setStyle("-fx-text-fill: #0ea5e9;");
        t2Label = new Label(); t2Label.setStyle("-fx-text-fill: #38bdf8;");
        t3Label = new Label(); t3Label.setStyle("-fx-text-fill: #c084fc;");
        t4Label = new Label(); t4Label.setStyle("-fx-text-fill: #f43f5e;");
        deltaTLabel = new Label(); deltaTLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");

        timerLabel = new Label("Таймер: 0 / 60 с"); timerLabel.setStyle("-fx-text-fill: yellow;");
        volLabel = new Label("Об'єм води V: 0.0 мл"); volLabel.setStyle("-fx-text-fill: #00ffcc; -fx-font-weight: bold;");

        dash.getChildren().addAll(dashTitle, liveStepLabel, new Separator(), t1Label, t2Label, t3Label, t4Label, deltaTLabel, new Separator(), timerLabel, volLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, String> matCol = new TableColumn<>("Метал");
        matCol.setCellValueFactory(new PropertyValueFactory<>("metalName"));
        TableColumn<Measurement, Double> dtWatCol = new TableColumn<>("t2 - t1");
        dtWatCol.setCellValueFactory(new PropertyValueFactory<>("t2"));
        TableColumn<Measurement, Double> dtRodCol = new TableColumn<>("t4 - t3");
        dtRodCol.setCellValueFactory(new PropertyValueFactory<>("t4"));
        TableColumn<Measurement, Double> vCol = new TableColumn<>("V (мл)");
        vCol.setCellValueFactory(new PropertyValueFactory<>("volumeMl"));
        TableColumn<Measurement, Double> kCol = new TableColumn<>("k (Вт/м·К)");
        kCol.setCellValueFactory(new PropertyValueFactory<>("kValue"));

        table.getColumns().addAll(idCol, matCol, dtWatCol, dtRodCol, vCol, kCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(140);

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(new VBox(5, table, createStatsBox()));
    }

    private void setAllControlsDisable(boolean disable) {
        toggleHeaterBtn.setDisable(disable);
        flowSlider.setDisable(disable);
        timeMeasureSlider.setDisable(disable);
        metalComboBox.setDisable(disable);
        inputTempSlider.setDisable(disable);
        startVolMeasureBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
    }

    private void handleToggleHeater() {
        isHeaterOn = !isHeaterOn;
        toggleHeaterBtn.setText(isHeaterOn ? "⏹ ВИМКНУТИ ПІЧ" : "⚡ УВІМКНУТИ ПІЧ");
        toggleHeaterBtn.setStyle(isHeaterOn ? "-fx-background-color: #475569; -fx-text-fill: white;" : "-fx-background-color: #ff007f; -fx-text-fill: white;");
        if (!isAutoModeActive) {
            liveStepLabel.setText(isHeaterOn ? "Статус: Нагрівання..." : "Статус: Піч вимкнена.");
            liveStepLabel.setStyle(isHeaterOn ? "-fx-text-fill: #ffeb3b;" : "-fx-text-fill: #94a3b8;");
        }
    }

    private void startPhysicsEngine() {
        physicsTimer = new AnimationTimer() {
            long lastTime = 0;
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double dt = (now - lastTime) / 1_000_000_000.0; lastTime = now;
                double flow = flowSlider.getValue() / 100.0;
                double trueK = metalComboBox.getSelectionModel().getSelectedIndex() == 0 ? 390.0 : (metalComboBox.getSelectionModel().getSelectedIndex() == 1 ? 230.0 : 50.0);

                heaterTemp += ((isHeaterOn ? 180.0 : currentT1) - heaterTemp) * 0.2 * dt;
                currentT4 += (heaterTemp - currentT4) * (trueK / 400.0) * dt;
                currentT3 += ((currentT4 - currentT3) * (trueK / 500.0) - (currentT3 - currentT1) * flow * 3.0) * dt;
                currentT2 += ((flow < 0.05 && isHeaterOn ? currentT3 : currentT1 + (currentT3 - currentT1) * flow * 0.4) - currentT2) * 1.5 * dt;
                currentT1 = inputTempSlider.getValue();

                Platform.runLater(() -> {
                    t1Label.setText(String.format(Locale.US, "t1 (вхід): %.1f °C", currentT1));
                    t2Label.setText(String.format(Locale.US, "t2 (вихід): %.1f °C", currentT2));
                    t3Label.setText(String.format(Locale.US, "t3 (хол): %.1f °C", currentT3));
                    t4Label.setText(String.format(Locale.US, "t4 (гар): %.1f °C", currentT4));
                    deltaTLabel.setText(String.format(Locale.US, "ΔT стрижня: %.1f °C", currentT4 - currentT3));

                    canvas.updateState(isHeaterOn, flow, currentT1, currentT2, currentT3, currentT4, heaterTemp, trueK);
                });
            }
        };
        physicsTimer.start();
    }

    private void startVolumeMeasurement() {
        if (!isHeaterOn || flowSlider.getValue() < 5) {
            showAlert("Помилка", "Піч має бути увімкнена, а потік води відкритий!"); return;
        }
        if (currentT4 - currentT3 < 10.0) {
            showAlert("Увага", "Стрижень ще не прогрівся. Дочекайтесь стабілізації!"); return;
        }

        isMeasuringVol = true;
        if (!isAutoModeActive) setAllControlsDisable(true);
        liveStepLabel.setText("Статус: Наповнення мензурки...");
        liveStepLabel.setStyle("-fx-text-fill: #0288d1; -fx-font-weight: bold;");

        double targetTime = timeMeasureSlider.getValue();
        currentVolumeMl = 0.0;
        final double fT1 = currentT1, fT2 = currentT2, fT3 = currentT3, fT4 = currentT4;

        volTimer = new AnimationTimer() {
            long start = System.nanoTime();
            @Override
            public void handle(long now) {
                double elapsed = (now - start) / 1_000_000_000.0;
                double simElapsed = elapsed * 4.0; // Швидкість х4

                if (simElapsed < targetTime) {
                    timerLabel.setText(String.format(Locale.US, "Таймер: %.0f / %.0f с", simElapsed, targetTime));
                    double flow = flowSlider.getValue() / 100.0;

                    currentVolumeMl = flow * 3.0 * simElapsed;

                    if (currentVolumeMl < 0) currentVolumeMl = 0;
                    if (currentVolumeMl > 200) currentVolumeMl = 200;

                    volLabel.setText(String.format(Locale.US, "Об'єм води V: %.1f мл", currentVolumeMl));
                    canvas.updateVolumeState(true, currentVolumeMl);
                } else {
                    this.stop();
                    completeVolumeMeasurement(targetTime, fT1, fT2, fT3, fT4);
                }
            }
        };
        volTimer.start();
    }

    private void completeVolumeMeasurement(double timeSec, double t1, double t2, double t3, double t4) {
        isMeasuringVol = false;
        if (!isAutoModeActive) { setAllControlsDisable(false); liveStepLabel.setText("Статус: Замір завершено"); liveStepLabel.setStyle("-fx-text-fill: #a3e635;"); }
        timerLabel.setText(String.format(Locale.US, "Таймер: %.0f / %.0f с", timeSec, timeSec));

        double calcK = (WATER_DENSITY * (currentVolumeMl * 1e-6) * WATER_C * (t2 - t1) * L_DISTANCE) / (S_AREA * timeSec * (t4 - t3));
        data.add(new Measurement(idCounter++, metalComboBox.getValue().split(" ")[0], t1, Math.round((t2-t1)*10.0)/10.0, t3, Math.round((t4-t3)*10.0)/10.0, Math.round(currentVolumeMl*10.0)/10.0, timeSec, Math.round(calcK*10.0)/10.0));

        canvas.updateVolumeState(false, 0.0);
        currentVolumeMl = 0.0;

        updateStats();
    }

    private void startAutoMode() {
        isAutoModeActive = true; setAllControlsDisable(true);
        data.clear(); idCounter = 1; updateStats(); autoQueue.clear();
        autoQueue.addAll(java.util.Arrays.asList(30.0, 60.0, 90.0));
        if (!isHeaterOn) handleToggleHeater();
        processNextAuto();
    }

    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoModeActive = false; setAllControlsDisable(false);
            liveStepLabel.setText("АВТОПРОХОДЖЕННЯ ЗАВЕРШЕНО"); liveStepLabel.setStyle("-fx-text-fill: #a3e635; -fx-font-weight: bold;"); return;
        }
        flowSlider.setValue(autoQueue.poll()); timeMeasureSlider.setValue(60.0);
        liveStepLabel.setText("Статус: АВТО. Очікування стабілізації..."); liveStepLabel.setStyle("-fx-text-fill: #ffeb3b;");
        autoTimer = new AnimationTimer() {
            long start = System.nanoTime();
            @Override
            public void handle(long now) {
                if ((now - start) / 1_000_000_000.0 > 5.0) {
                    this.stop();
                    Platform.runLater(() -> startVolumeMeasurement());
                    new Thread(() -> { try { Thread.sleep(17000); Platform.runLater(() -> processNextAuto()); } catch (Exception ignored) {} }).start();
                }
            }
        };
        autoTimer.start();
    }

    private void updateStats() {
        if (data.isEmpty()) { finalResultLabel.setText("Обробка результатів: -"); return; }
        if (!showCalculations) { finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]"); return; }
        if (data.size() < 3) { finalResultLabel.setText(String.format("СТАТУС: Зібрано %d вимірів. Проведіть ще досліди.", data.size())); return; }

        double sumK = 0; for (Measurement m : data) sumK += m.getKValue();
        double avgK = sumK / data.size();
        double trueK = metalComboBox.getSelectionModel().getSelectedIndex() == 0 ? 390.0 : (metalComboBox.getSelectionModel().getSelectedIndex() == 1 ? 230.0 : 50.0);

        finalResultLabel.setText(String.format(Locale.US, "ОБРОБКА РЕЗУЛЬТАТІВ:\n1. Досліджуваний метал: %s (k ≈ %.0f Вт/м·К).\n2. k_експ = %.1f Вт/м·К.\nВИСНОВОК: Відносна похибка ε = %.1f %%.", metalComboBox.getValue().split(" ")[0], trueK, avgK, Math.abs(avgK - trueK) / trueK * 100.0));
    }
}