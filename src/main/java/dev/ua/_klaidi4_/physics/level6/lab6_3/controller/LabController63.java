/*
 * Лабораторна робота № 6-3 "Гамма-ослаблення".
 * Клас: LabController63.
 * Призначення: керує інтерфейсом лабораторної роботи, запуском досліду,
 * введенням параметрів, таблицею результатів та обробкою вимірювань.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level6.lab6_3.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level6.lab6_3.model.Measurement;
import dev.ua._klaidi4_.physics.level6.lab6_3.view.GammaAttenuationCanvas;
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
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.function.Consumer;

public class LabController63 extends BaseLabController {

    private GammaAttenuationCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> isotopeComboBox;

    private TextField xField;
    private TextField timeField;
    private TextField intensityField;

    private double currentXValue = 0.0;
    private double currentTimeValue = 60.0;
    private double currentIntensityValue = 15000.0;

    private Button measureBtn;
    private Button autoBtn;
    private Button clearBtn;

    private Label liveStepLabel;
    private Label liveTimerLabel;
    private Label liveCountLabel;
    private Label liveLnCountLabel;
    private boolean isMeasuring = false;

    private AnimationTimer measureTimer;
    private AnimationTimer autoTimer;
    private Queue<Double> autoQueue = new LinkedList<>();

    /*
     * Лабораторна робота № 6-3 "Гамма-ослаблення".
     * Функція: LabController63.
     * Призначення: Конструктор класу, ініціалізує інтерфейс та налаштування.
     */
    public LabController63() {
        initUI();
        applyPhysicsSettings();
    }

    /*
     * Лабораторна робота № 6-3 "Гамма-ослаблення".
     * Функція: shutdown.
     * Призначення: Зупиняє процеси та очищує ресурси при закритті вікна.
     */
    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        if (measureTimer != null) measureTimer.stop();
        if (autoTimer != null) autoTimer.stop();
        autoQueue.clear();
    }

    /*
     * Лабораторна робота № 6-3 "Гамма-ослаблення".
     * Функція: initUI.
     * Призначення: Ініціалізує графічний інтерфейс користувача, створює панелі управління та графіки.
     */
    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 6-3)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane configPane = new TitledPane();
        configPane.setText("Параметри установки");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(12);
        configBox.setPadding(new Insets(5));

        Label studentLabel = new Label("Робочі параметри:");
        studentLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

        xField = createNumberField(currentXValue, val -> {
            currentXValue = val;
            applyPhysicsSettings();
        });

        timeField = createNumberField(currentTimeValue, val -> {
            currentTimeValue = val;
            if (!isMeasuring) {
                liveTimerLabel.setText(String.format(Locale.US, "Таймер: 0 / %.0f с", currentTimeValue));
            }
        });

        Label envSettingsLabel = new Label("Налаштування середовища:");
        envSettingsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b; -fx-padding: 10 0 0 0;");

        isotopeComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "Кобальт-60 (Co-60, E ≈ 1.25 МеВ)",
                "Цезій-137 (Cs-137, E ≈ 0.66 МеВ)",
                "Радій-226 (Ra-226, E ≈ 0.18 МеВ)"
        ));
        isotopeComboBox.getSelectionModel().selectFirst();
        isotopeComboBox.setMaxWidth(Double.MAX_VALUE);
        isotopeComboBox.setOnAction(e -> {
            data.clear();
            idCounter = 1;
            applyPhysicsSettings();
            updateStats();
        });

        intensityField = createNumberField(currentIntensityValue, val -> {
            currentIntensityValue = val;
        });

        configBox.getChildren().addAll(
                studentLabel,
                createInputGroup("Товщина фільтрів x (см):", xField),
                createInputGroup("Час вимірювання t (с):", timeField),
                new Separator(),
                envSettingsLabel,
                createInputGroup("Ізотоп:", isotopeComboBox),
                createInputGroup("Початкова інтенсивність (імп/хв):", intensityField)
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
            updateStats();
            applyPhysicsSettings();
        });

        leftPanel.getChildren().addAll(title, configPane, measureBtn, autoBtn, clearBtn);

        canvas = new GammaAttenuationCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(4);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-text-fill: #00e5ff; -fx-padding: 10; -fx-border-color: #00e5ff; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(240, 130);

        Label dashTitle = new Label("ДАНІ ЛІЧИЛЬНИКА");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        liveStepLabel = new Label("Статус: Очікування");
        liveStepLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");

        liveTimerLabel = new Label("Таймер: 0 / 60 с");
        liveTimerLabel.setStyle("-fx-text-fill: yellow;");

        liveCountLabel = new Label("N(x): ---");
        liveCountLabel.setStyle("-fx-text-fill: #00ffcc; -fx-font-size: 14px; -fx-font-weight: bold;");

        liveLnCountLabel = new Label("ln N(x): ---");
        liveLnCountLabel.setStyle("-fx-text-fill: #ff007f;");

        dash.getChildren().addAll(dashTitle, liveStepLabel, liveTimerLabel, liveCountLabel, liveLnCountLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, String> isoCol = new TableColumn<>("Ізотоп");
        isoCol.setCellValueFactory(new PropertyValueFactory<>("isotope"));
        TableColumn<Measurement, Double> xCol = new TableColumn<>("x (см)");
        xCol.setCellValueFactory(new PropertyValueFactory<>("x"));
        TableColumn<Measurement, Integer> tCol = new TableColumn<>("t (с)");
        tCol.setCellValueFactory(new PropertyValueFactory<>("timeSec"));
        TableColumn<Measurement, Integer> nCol = new TableColumn<>("N(x)");
        nCol.setCellValueFactory(new PropertyValueFactory<>("counts"));
        TableColumn<Measurement, Double> lnCol = new TableColumn<>("ln N(x)");
        lnCol.setCellValueFactory(new PropertyValueFactory<>("lnCounts"));

        table.getColumns().addAll(idCol, isoCol, xCol, tCol, nCol, lnCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(140);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        updateStats();
    }

    /*
     * Лабораторна робота № 6-3 "Гамма-ослаблення".
     * Функція: createNumberField.
     * Призначення: Створює і повертає новий елемент інтерфейсу або об'єкт.
     */
    private TextField createNumberField(double initialValue, Consumer<Double> onChange) {
        TextField field = new TextField(String.valueOf(initialValue));
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                field.setText(newValue.replaceAll("[^\\d.]", ""));
            } else if (!newValue.isEmpty() && !newValue.equals(".")) {
                try {
                    double val = Double.parseDouble(newValue);
                    onChange.accept(val);
                } catch (NumberFormatException ignored) {}
            }
        });
        return field;
    }

    /*
     * Лабораторна робота № 6-3 "Гамма-ослаблення".
     * Функція: getTrueMu.
     * Призначення: Повертає необхідне значення параметра або об'єкта.
     */
    private double getTrueMu() {
        int idx = isotopeComboBox.getSelectionModel().getSelectedIndex();
        if (idx == 0) return 0.65;
        if (idx == 1) return 1.20;
        return 2.50;
    }

    /*
     * Лабораторна робота № 6-3 "Гамма-ослаблення".
     * Функція: applyPhysicsSettings.
     * Призначення: Застосовує поточні налаштування до симуляції.
     */
    private void applyPhysicsSettings() {
        canvas.setPhysicsParameters(currentXValue, getTrueMu(), isMeasuring, new ArrayList<>(data));
    }

    /*
     * Лабораторна робота № 6-3 "Гамма-ослаблення".
     * Функція: setControlsDisable.
     * Призначення: Встановлює нове значення для вказаного параметра.
     */
    private void setControlsDisable(boolean disable) {
        xField.setDisable(disable);
        timeField.setDisable(disable);
        isotopeComboBox.setDisable(disable);
        intensityField.setDisable(disable);
        measureBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
    }

    /*
     * Лабораторна робота № 6-3 "Гамма-ослаблення".
     * Функція: startSingleMeasurement.
     * Призначення: Запускає процес симуляції або відповідний режим роботи.
     */
    private void startSingleMeasurement() {
        if (currentTimeValue <= 0) {
            showAlert("Помилка", "Час вимірювання має бути більшим за 0.");
            return;
        }

        double currentX = Math.round(currentXValue * 10.0) / 10.0;
        double targetTime = currentTimeValue;
        String currentIsotope = isotopeComboBox.getValue().split(" ")[0];

        for (Measurement m : data) {
            if (Math.abs(m.getX() - currentX) < 0.05 && m.getIsotope().equals(currentIsotope)) {
                showAlert("Увага", "Вимірювання для цієї товщини вже проведено!");
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
    /*
     * Лабораторна робота № 6-3 "Гамма-ослаблення".
     * Функція: handle.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
            @Override
            public void handle(long now) {
                double elapsed = (now - start) / 1_000_000_000.0;
                double simRate = targetTime / 1.5;
                int simulatedSeconds = (int) (elapsed * simRate);

                if (simulatedSeconds < targetTime) {
                    liveTimerLabel.setText(String.format(Locale.US, "Таймер: %d / %.0f с", simulatedSeconds, targetTime));
                    int currentCount = (int) ((generateCount(currentX, targetTime) / targetTime) * simulatedSeconds);
                    liveCountLabel.setText(String.format("N(x): %d", currentCount));
                    liveLnCountLabel.setText(String.format(Locale.US, "ln N(x): %.3f", currentCount > 0 ? Math.log(currentCount) : 0));
                } else {
                    this.stop();
                    completeMeasurement(currentX, targetTime, currentIsotope);
                }
            }
        };
        measureTimer.start();
    }

    /*
     * Лабораторна робота № 6-3 "Гамма-ослаблення".
     * Функція: generateCount.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private int generateCount(double x, double timeSec) {
        double mu = getTrueMu();
        double i0PerMin = currentIntensityValue;
        double i0PerSec = i0PerMin / 60.0;
        double expectedRate = i0PerSec * Math.exp(-mu * x);
        double expectedTotal = expectedRate * timeSec;
        double noise = (Math.random() - 0.5) * Math.sqrt(expectedTotal) * 2;
        expectedTotal += noise;

        if (expectedTotal < 1.0 * timeSec) {
            expectedTotal = 1.0 * timeSec + (Math.random() - 0.5) * Math.sqrt(timeSec);
        }

        return Math.max(1, (int) Math.round(expectedTotal));
    }

    /*
     * Лабораторна робота № 6-3 "Гамма-ослаблення".
     * Функція: completeMeasurement.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void completeMeasurement(double x, double timeSec, String isotope) {
        isMeasuring = false;
        liveStepLabel.setText("Статус: ГОТОВО");
        liveStepLabel.setStyle("-fx-text-fill: #00ffcc; -fx-font-weight: bold;");
        liveTimerLabel.setText(String.format(Locale.US, "Таймер: %.0f / %.0f с", timeSec, timeSec));

        int finalCount = generateCount(x, timeSec);
        double lnCount = Math.log(finalCount);

        liveCountLabel.setText(String.format("N(x): %d", finalCount));
        liveLnCountLabel.setText(String.format(Locale.US, "ln N(x): %.3f", lnCount));

        data.add(new Measurement(idCounter++, isotope, x, (int)timeSec, finalCount, Math.round(lnCount * 1000.0) / 1000.0));

        updateStats();
        applyPhysicsSettings();
        setControlsDisable(false);

        // Автоматичний розрахунок
        performAutomaticCalculation();
    }

    /*
     * Лабораторна робота № 6-3 "Гамма-ослаблення".
     * Функція: performAutomaticCalculation.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void performAutomaticCalculation() {
        if (data.size() < 3) return;

        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }

        int n = data.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;

        for (Measurement m : data) {
            double x = m.getX();
            double normCount = m.getCounts() * (60.0 / m.getTimeSec());
            double y = Math.log(normCount);

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        double muExp = -slope;
        double trueMu = getTrueMu();
        double error = Math.abs(muExp - trueMu) / trueMu * 100.0;

        double energyExp = 1.3 / Math.max(0.1, muExp);

        String conclusion = String.format(Locale.US,
                "АВТОМАТИЧНИЙ РОЗРАХУНОК:\n" +
                        "1. Рівняння: ln N(x) = ln N_0 - μx. За нахилом прямої: μ = %.3f см⁻¹.\n" +
                        "2. Відносна похибка визначення (відносно табличного %.2f): ε = %.1f %%.\n" +
                        "3. Залежність μ=f(E) для свинцю дає оціночну енергію: E ≈ %.2f МеВ.",
                muExp, trueMu, error, energyExp
        );
        finalResultLabel.setText(conclusion);
    }

    /*
     * Лабораторна робота № 6-3 "Гамма-ослаблення".
     * Функція: startAutoMode.
     * Призначення: Запускає процес симуляції або відповідний режим роботи.
     */
    private void startAutoMode() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        for (int i = 0; i <= 10; i++) {
            autoQueue.add(i / 10.0);
        }

        timeField.setText("60");
        processNextAuto();
    }

    /*
     * Лабораторна робота № 6-3 "Гамма-ослаблення".
     * Функція: processNextAuto.
     * Призначення: Обробляє поточний крок симуляції або дію користувача.
     */
    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            liveStepLabel.setText("АВТОПРОХОДЖЕННЯ ЗАВЕРШЕНО");
            liveStepLabel.setStyle("-fx-text-fill: #a3e635; -fx-font-weight: bold;");
            setControlsDisable(false);
            return;
        }

        double nextX = autoQueue.poll();
        xField.setText(String.valueOf(nextX));

        Platform.runLater(this::startSingleMeasurement);

        autoTimer = new AnimationTimer() {
            long start = System.nanoTime();
    /*
     * Лабораторна робота № 6-3 "Гамма-ослаблення".
     * Функція: handle.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
            @Override
            public void handle(long now) {
                if (!isMeasuring && ((now - start) / 1_000_000_000.0) > 1.8) {
                    this.stop();
                    processNextAuto();
                }
            }
        };
        autoTimer.start();
    }

    /*
     * Лабораторна робота № 6-3 "Гамма-ослаблення".
     * Функція: updateStats.
     * Призначення: Розраховує середні значення та похибки на основі даних у таблиці.
     */
    private void updateStats() {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: -");
            return;
        }
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }

        if (data.size() < 3) {
            finalResultLabel.setText(String.format("СТАТУС: Зібрано %d точок. Потрібно мінімум 3 для розрахунку μ.", data.size()));
        }
    }
}