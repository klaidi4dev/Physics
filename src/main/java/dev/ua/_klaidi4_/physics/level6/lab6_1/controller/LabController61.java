package dev.ua._klaidi4_.physics.level6.lab6_1.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level6.lab6_1.model.Measurement;
import dev.ua._klaidi4_.physics.level6.lab6_1.view.AlphaDecayCanvas;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

public class LabController61 extends BaseLabController {

    private AlphaDecayCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private Slider xSlider;
    private Slider timeSlider;
    private ComboBox<String> sourceComboBox;
    private Slider pressureSlider;
    private Slider activitySlider;
    private Label xValueLabel;
    private Label timeValueLabel;
    private Label pressureValueLabel;
    private Label activityValueLabel;
    private Button measureBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStepLabel;
    private Label liveTimerLabel;
    private Label liveCountLabel;
    private boolean isMeasuring = false;
    private AnimationTimer measureTimer;
    private AnimationTimer autoTimer;
    private Queue<Double> autoQueue = new LinkedList<>();

    public LabController61() {
        initUI();
        applyPhysicsSettings();
    }

    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        if (measureTimer != null) measureTimer.stop();
        if (autoTimer != null) autoTimer.stop();
        autoQueue.clear();
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 6-1)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane configPane = new TitledPane();
        configPane.setText("Параметри установки");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(12);
        configBox.setPadding(new Insets(5));

        Label studentLabel = new Label("Робочі параметри:");
        studentLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

        xValueLabel = new Label("Відстань мікрометра X: 0.0 см");
        xSlider = new Slider(0.0, 6.0, 0.0);
        xSlider.setShowTickMarks(true);
        xSlider.setMajorTickUnit(1.0);
        xSlider.setMinorTickCount(9);
        xSlider.valueProperty().addListener((o, ov, nv) -> {
            xValueLabel.setText(String.format(Locale.US, "Відстань мікрометра X: %.1f см", nv.doubleValue()));
            applyPhysicsSettings();
        });

        timeValueLabel = new Label("Час вимірювання t: 100 с");
        timeSlider = new Slider(10.0, 300.0, 100.0);
        timeSlider.setShowTickMarks(true);
        timeSlider.setMajorTickUnit(50.0);
        timeSlider.valueProperty().addListener((o, ov, nv) -> {
            timeValueLabel.setText(String.format(Locale.US, "Час вимірювання t: %.0f с", nv.doubleValue()));
            if (!isMeasuring) {
                liveTimerLabel.setText(String.format(Locale.US, "Таймер: 0 / %.0f с", nv.doubleValue()));
            }
        });

        Label teacherLabel = new Label("Налаштування середовища:");
        teacherLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #d32f2f; -fx-padding: 10 0 0 0;");

        sourceComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "Плутоній-239 (E ≈ 5.15 МеВ)",
                "Полоній-210 (E ≈ 5.30 МеВ)",
                "Америцій-241 (E ≈ 5.48 МеВ)"
        ));
        sourceComboBox.getSelectionModel().selectFirst();
        sourceComboBox.setMaxWidth(Double.MAX_VALUE);
        sourceComboBox.setOnAction(e -> { data.clear(); applyPhysicsSettings(); updateStats(); });

        pressureValueLabel = new Label("Тиск повітря: 1.0 атм");
        pressureSlider = new Slider(0.5, 1.5, 1.0);
        pressureSlider.setShowTickMarks(true);
        pressureSlider.setMajorTickUnit(0.5);
        pressureSlider.valueProperty().addListener((o, ov, nv) -> {
            pressureValueLabel.setText(String.format(Locale.US, "Тиск повітря: %.2f атм", nv.doubleValue()));
            applyPhysicsSettings();
        });

        activityValueLabel = new Label("Активність джерела: 50 імп/с");
        activitySlider = new Slider(10, 200, 50);
        activitySlider.setShowTickMarks(true);
        activitySlider.setMajorTickUnit(50);
        activitySlider.valueProperty().addListener((o, ov, nv) -> {
            activityValueLabel.setText(String.format(Locale.US, "Активність джерела: %.0f імп/с", nv.doubleValue()));
            applyPhysicsSettings();
        });

        configBox.getChildren().addAll(
                studentLabel, xValueLabel, xSlider,
                timeValueLabel, timeSlider,
                new Separator(),
                teacherLabel,
                createInputGroup("Ізотоп:", sourceComboBox),
                pressureValueLabel, pressureSlider,
                activityValueLabel, activitySlider
        );

        ScrollPane scrollParams = new ScrollPane(configBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(320);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        configPane.setContent(scrollParams);

        measureBtn = new Button("🔴 ПОЧАТИ ВИМІРЮВАННЯ");
        measureBtn.setStyle("-fx-background-color: #ff007f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
        measureBtn.setMaxWidth(Double.MAX_VALUE);
        measureBtn.setOnAction(e -> startSingleMeasurement());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ ЛАБИ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAutoMode());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear();
            idCounter = 1;
            applyPhysicsSettings();
            updateStats();
        });

        leftPanel.getChildren().addAll(title, configPane, measureBtn, autoBtn, clearBtn);

        canvas = new AlphaDecayCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(4);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-text-fill: #00e5ff; -fx-padding: 10; -fx-border-color: #00e5ff; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(240, 110);

        Label dashTitle = new Label("ПРИЛАД ПСО-2,4");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        liveStepLabel = new Label("Статус: Очікування");
        liveStepLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");

        liveTimerLabel = new Label("Таймер: 0 / 100 с");
        liveTimerLabel.setStyle("-fx-text-fill: yellow;");

        liveCountLabel = new Label("N(x): ---");
        liveCountLabel.setStyle("-fx-text-fill: #00ffcc; -fx-font-size: 14px; -fx-font-weight: bold;");

        dash.getChildren().addAll(dashTitle, liveStepLabel, liveTimerLabel, liveCountLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, String> srcCol = new TableColumn<>("Ізотоп");
        srcCol.setCellValueFactory(new PropertyValueFactory<>("sourceName"));
        TableColumn<Measurement, Double> pCol = new TableColumn<>("P (атм)");
        pCol.setCellValueFactory(new PropertyValueFactory<>("pressure"));
        TableColumn<Measurement, Double> xCol = new TableColumn<>("x (см)");
        xCol.setCellValueFactory(new PropertyValueFactory<>("x"));
        TableColumn<Measurement, Integer> nCountCol = new TableColumn<>("N(x)");
        nCountCol.setCellValueFactory(new PropertyValueFactory<>("counts"));

        table.getColumns().addAll(idCol, srcCol, pCol, xCol, nCountCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(130);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);
    }

    private double getBaseR0() {
        int idx = sourceComboBox.getSelectionModel().getSelectedIndex();
        if (idx == 0) return 3.65;
        if (idx == 1) return 3.88;
        return 4.08;
    }

    private double getActualR0() {
        double pressure = pressureSlider.getValue();
        return getBaseR0() / pressure;
    }

    private void applyPhysicsSettings() {
        canvas.setPhysicsParameters(xSlider.getValue(), isMeasuring, new ArrayList<>(data), getActualR0());
    }

    private void setControlsDisable(boolean disable) {
        xSlider.setDisable(disable);
        timeSlider.setDisable(disable);
        sourceComboBox.setDisable(disable);
        pressureSlider.setDisable(disable);
        activitySlider.setDisable(disable);
        measureBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
    }

    private void startSingleMeasurement() {
        double currentX = Math.round(xSlider.getValue() * 10.0) / 10.0;
        double targetTime = timeSlider.getValue();

        for (Measurement m : data) {
            if (Math.abs(m.getX() - currentX) < 0.05 && m.getSourceName().equals(sourceComboBox.getValue().split(" ")[0])) {
                showAlert("Увага", "Вимірювання для цієї відстані вже проведено!");
                return;
            }
        }

        isMeasuring = true;
        setControlsDisable(true);
        liveStepLabel.setText("Статус: ВИМІРЮВАННЯ...");
        liveStepLabel.setStyle("-fx-text-fill: #ff007f; -fx-font-weight: bold;");
        applyPhysicsSettings();

        measureTimer = new AnimationTimer() {
            long start = System.nanoTime();
            @Override
            public void handle(long now) {
                double elapsed = (now - start) / 1_000_000_000.0;
                double simRate = targetTime / 1.5;
                int simulatedSeconds = (int) (elapsed * simRate);

                if (simulatedSeconds < targetTime) {
                    liveTimerLabel.setText(String.format(Locale.US, "Таймер: %d / %.0f с", simulatedSeconds, targetTime));

                    int currentCount = (int) ((generateCountForX(currentX, targetTime) / targetTime) * simulatedSeconds);
                    liveCountLabel.setText(String.format("N(x): %d", currentCount));
                } else {
                    this.stop();
                    completeMeasurement(currentX, targetTime);
                }
            }
        };
        measureTimer.start();
    }

    private void completeMeasurement(double x, double timeSec) {
        isMeasuring = false;
        liveStepLabel.setText("Статус: ГОТОВО");
        liveStepLabel.setStyle("-fx-text-fill: #00ffcc; -fx-font-weight: bold;");
        liveTimerLabel.setText(String.format(Locale.US, "Таймер: %.0f / %.0f с", timeSec, timeSec));

        int finalCount = generateCountForX(x, timeSec);
        liveCountLabel.setText(String.format("N(x): %d", finalCount));

        String srcName = sourceComboBox.getValue().split(" ")[0];
        double pressure = Math.round(pressureSlider.getValue() * 100.0) / 100.0;

        data.add(new Measurement(idCounter++, srcName, pressure, x, finalCount));
        updateStats();
        applyPhysicsSettings();
        setControlsDisable(false);
    }

    private int generateCountForX(double x, double timeSec) {
        double actualR0 = getActualR0();
        double ratePerSec = activitySlider.getValue();
        double peakPos = actualR0 - 0.25;
        double peakFactor = 1.0 + 0.18 * Math.exp(-Math.pow(x - peakPos, 2) / 0.08);
        double dropOff = 1.0 / (1.0 + Math.exp(30.0 * (x - actualR0)));
        double expectedRate = ratePerSec * peakFactor * dropOff;
        double expectedTotal = expectedRate * timeSec;
        double noise = (Math.random() - 0.5) * Math.sqrt(expectedTotal) * 2;
        expectedTotal += noise;

        double bgRate = 0.2;
        if (expectedTotal < bgRate * timeSec) {
            expectedTotal = bgRate * timeSec + (Math.random() - 0.5) * Math.sqrt(bgRate * timeSec);
        }

        return Math.max(0, (int) Math.round(expectedTotal));
    }

    private void startAutoMode() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        double currentR0 = getActualR0();

        double[] autoPoints = {0.0, 1.0, 2.0,
                currentR0 - 0.8, currentR0 - 0.5, currentR0 - 0.3, currentR0 - 0.1,
                currentR0, currentR0 + 0.1, currentR0 + 0.3, currentR0 + 0.8};

        for (double p : autoPoints) {
            if (p >= 0.0 && p <= 6.0) autoQueue.add(Math.round(p * 10.0) / 10.0);
        }

        processNextAuto();
    }

    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            liveStepLabel.setText("АВТОПРОХОДЖЕННЯ ЗАВЕРШЕНО");
            liveStepLabel.setStyle("-fx-text-fill: #a3e635; -fx-font-weight: bold;");
            setControlsDisable(false);
            return;
        }

        double nextX = autoQueue.poll();
        xSlider.setValue(nextX);

        Platform.runLater(this::startSingleMeasurement);

        autoTimer = new AnimationTimer() {
            long start = System.nanoTime();
            @Override
            public void handle(long now) {
                if (!isMeasuring && ((now - start) / 1_000_000_000.0) > 1.2) {
                    this.stop();
                    processNextAuto();
                }
            }
        };
        autoTimer.start();
    }

    private void updateStats() {
        if (data.size() < 5) {
            finalResultLabel.setText("Обробка результатів: [Зберіть більше даних для аналізу кривої]");
            return;
        }

        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }

        List<Measurement> sorted = new ArrayList<>(data);
        sorted.sort(Comparator.comparingDouble(Measurement::getX));

        double maxN = 0;
        for (Measurement m : sorted) {
            if (m.getCounts() > maxN) maxN = m.getCounts();
        }

        double halfN = maxN / 2.0;
        double r0_exp = -1.0;

        for (int i = 0; i < sorted.size() - 1; i++) {
            Measurement m1 = sorted.get(i);
            Measurement m2 = sorted.get(i + 1);

            if (m1.getCounts() >= halfN && m2.getCounts() < halfN) {
                double fraction = (m1.getCounts() - halfN) / (double) (m1.getCounts() - m2.getCounts());
                r0_exp = m1.getX() + fraction * (m2.getX() - m1.getX());
                break;
            }
        }

        if (r0_exp > 0) {
            double currentPressure = pressureSlider.getValue();
            double r0_normalized = r0_exp * currentPressure;

            double energyExp = Math.pow(r0_normalized / 0.318, 2.0 / 3.0);

            int idx = sourceComboBox.getSelectionModel().getSelectedIndex();
            double trueEnergy = (idx == 0) ? 5.15 : (idx == 1) ? 5.30 : 5.48;

            double error = Math.abs(energyExp - trueEnergy) / trueEnergy * 100.0;

            String conclusion = String.format(Locale.US,
                    "ОБРОБКА РЕЗУЛЬТАТІВ:\n" +
                            "1. Експериментальний пробіг: R_exp = %.2f см (при тиску %.2f атм).\n" +
                            "2. Пробіг за нормальних умов (1 атм): R_0 = %.2f см.\n" +
                            "3. Розрахована енергія: E = (R_0 / 0.318)^(2/3) = %.2f МеВ.\n" +
                            "ВИСНОВОК: Похибка склала ε = %.1f %%. Чим більший час вимірювання t, тим плавніше виглядає крива Брегга.",
                    r0_exp, currentPressure, r0_normalized, energyExp, error
            );
            finalResultLabel.setText(conclusion);
        } else {
            finalResultLabel.setText("Обробка результатів: [Неможливо знайти R0. Зберіть точки у зоні різкого спаду N(x)]");
        }
    }
}