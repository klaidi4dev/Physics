/*
 * Лабораторна робота № 5-4 "Біпризма Френеля".
 * Клас: LabController54.
 * Призначення: керує інтерфейсом лабораторної роботи, запуском досліду,
 * введенням параметрів, таблицею результатів та обробкою вимірювань.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level5.lab5_4.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level5.lab5_4.model.Measurement;
import dev.ua._klaidi4_.physics.level5.lab5_4.view.BiprismCanvas;
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

public class LabController54 extends BaseLabController {

    private BiprismCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> filterComboBox;
    private Slider microSlider;
    private Slider lensSlider;
    private ToggleButton toggleLensBtn;
    private Label microValueLabel;
    private Label lensValueLabel;
    private Button togglePowerBtn;
    private Button recordBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStepLabel;
    private Label y1Label, y2Label, s1Label, s2Label;

    private boolean isPowerOn = false;
    private int currentStep = 0;
    private Double valY1 = null, valY2 = null, valS1 = null, valS2 = null;

    private AnimationTimer autoTimer;
    private Queue<Integer> autoQueue = new LinkedList<>();

    /*
     * Лабораторна робота № 5-4 "Біпризма Френеля".
     * Функція: LabController54.
     * Призначення: Конструктор класу, ініціалізує інтерфейс та налаштування.
     */
    public LabController54() {
        initUI();
        applyPhysicsSettings();
    }

    /*
     * Лабораторна робота № 5-4 "Біпризма Френеля".
     * Функція: shutdown.
     * Призначення: Зупиняє процеси та очищує ресурси при закритті вікна.
     */
    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        if (autoTimer != null) autoTimer.stop();
        autoQueue.clear();
    }

    /*
     * Лабораторна робота № 5-4 "Біпризма Френеля".
     * Функція: initUI.
     * Призначення: Ініціалізує графічний інтерфейс користувача, створює панелі управління та графіки.
     */
    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 5-4)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane configPane = new TitledPane();
        configPane.setText("Параметри установки");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(12);
        configBox.setPadding(new Insets(5));

        filterComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "Червоний (650 нм)",
                "Зелений (532 нм)",
                "Синій (470 нм)"
        ));
        filterComboBox.getSelectionModel().selectFirst();
        filterComboBox.setMaxWidth(Double.MAX_VALUE);
        filterComboBox.setOnAction(e -> {
            resetMeasurementState();
            applyPhysicsSettings();
        });

        microValueLabel = new Label("Окулярний мікрометр z: 0.00 мм");

        microSlider = new Slider(-2.5, 2.5, 0.0);
        microSlider.setShowTickMarks(true);
        microSlider.setMajorTickUnit(0.5);
        microSlider.valueProperty().addListener((o, ov, nv) -> {
            microValueLabel.setText(String.format(Locale.US, "Окулярний мікрометр z: %.2f мм", nv.doubleValue()));
            applyPhysicsSettings();
        });

        lensValueLabel = new Label("Позиція лінзи x: 20.0 см");

        lensSlider = new Slider(10.0, 30.0, 15.0);
        lensSlider.setShowTickMarks(true);
        lensSlider.setMajorTickUnit(5.0);
        lensSlider.setDisable(true);
        lensSlider.valueProperty().addListener((o, ov, nv) -> {
            lensValueLabel.setText(String.format(Locale.US, "Позиція лінзи x: %.1f см", nv.doubleValue()));
            applyPhysicsSettings();
        });

        toggleLensBtn = new ToggleButton("ВСТАВИТИ ЛІНЗУ (F=10 см)");
        toggleLensBtn.setMaxWidth(Double.MAX_VALUE);
        toggleLensBtn.setOnAction(e -> {
            lensSlider.setDisable(!toggleLensBtn.isSelected());
            applyPhysicsSettings();
        });

        configBox.getChildren().addAll(
                createInputGroup("Світлофільтр:", filterComboBox),
                microValueLabel, microSlider,
                toggleLensBtn, lensValueLabel, lensSlider
        );

        ScrollPane scrollParams = new ScrollPane(configBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(240);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        configPane.setContent(scrollParams);

        togglePowerBtn = new Button("⚡ УВІМКНУТИ ОСВІТЛЮВАЧ");
        togglePowerBtn.setStyle("-fx-background-color: #A155FF; -fx-text-fill: white; -fx-font-weight: bold;");
        togglePowerBtn.setMaxWidth(Double.MAX_VALUE);
        togglePowerBtn.setOnAction(e -> handleTogglePower());

        recordBtn = new Button("📝 ЗАФІКСУВАТИ y1 (СМУГА 0)");
        recordBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        recordBtn.setMaxWidth(Double.MAX_VALUE);
        recordBtn.setOnAction(e -> handleRecord());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAutoMode());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear();
            idCounter = 1;
            resetMeasurementState();
            updateStats();
        });

        leftPanel.getChildren().addAll(title, configPane, togglePowerBtn, recordBtn, autoBtn, clearBtn);

        canvas = new BiprismCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(4);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-text-fill: #A155FF; -fx-padding: 10; -fx-border-color: #A155FF; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(240, 110);

        Label dashTitle = new Label("ОПТИЧНИЙ СЕНСОР");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        liveStepLabel = new Label("Крок: Увімкніть живлення");
        liveStepLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");

        y1Label = new Label("y1 (смуга 0) = ---"); y1Label.setStyle("-fx-text-fill: #ff007f;");
        y2Label = new Label("y2 (смуга 10) = ---"); y2Label.setStyle("-fx-text-fill: #ff007f;");
        s1Label = new Label("S1 (джерело 1) = ---"); s1Label.setStyle("-fx-text-fill: #00e5ff;");
        s2Label = new Label("S2 (джерело 2) = ---"); s2Label.setStyle("-fx-text-fill: #00e5ff;");

        dash.getChildren().addAll(dashTitle, liveStepLabel, y1Label, y2Label, s1Label, s2Label);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, String> fCol = new TableColumn<>("Світлофільтр");
        fCol.setCellValueFactory(new PropertyValueFactory<>("filterColor"));
        TableColumn<Measurement, Double> dyCol = new TableColumn<>("Δy (мм)");
        dyCol.setCellValueFactory(new PropertyValueFactory<>("dy"));
        TableColumn<Measurement, Double> l1Col = new TableColumn<>("l1 (мм)");
        l1Col.setCellValueFactory(new PropertyValueFactory<>("l1"));
        TableColumn<Measurement, Double> x1Col = new TableColumn<>("x1 (см)");
        x1Col.setCellValueFactory(new PropertyValueFactory<>("x1"));
        TableColumn<Measurement, Double> lamCol = new TableColumn<>("λ експ (нм)");
        lamCol.setCellValueFactory(new PropertyValueFactory<>("lambdaExp"));
        TableColumn<Measurement, Double> errCol = new TableColumn<>("ε (%)");
        errCol.setCellValueFactory(new PropertyValueFactory<>("errorPercent"));

        table.getColumns().addAll(idCol, fCol, dyCol, l1Col, x1Col, lamCol, errCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(130);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        updateRecordButtonUI();
    }

    /*
     * Лабораторна робота № 5-4 "Біпризма Френеля".
     * Функція: handleTogglePower.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void handleTogglePower() {
        isPowerOn = !isPowerOn;
        if (isPowerOn) {
            togglePowerBtn.setText("⏹ ВИМКНУТИ ОСВІТЛЮВАЧ");
            togglePowerBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold;");
            resetMeasurementState();
        } else {
            togglePowerBtn.setText("⚡ УВІМКНУТИ ОСВІТЛЮВАЧ");
            togglePowerBtn.setStyle("-fx-background-color: #A155FF; -fx-text-fill: white; -fx-font-weight: bold;");
            liveStepLabel.setText("Крок: Увімкніть живлення");
            liveStepLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
        }
        updateRecordButtonUI();
        applyPhysicsSettings();
    }

    /*
     * Лабораторна робота № 5-4 "Біпризма Френеля".
     * Функція: applyPhysicsSettings.
     * Призначення: Застосовує поточні налаштування до симуляції.
     */
    private void applyPhysicsSettings() {
        int index = filterComboBox.getSelectionModel().getSelectedIndex();
        double wave = (index == 0) ? 650.0 : (index == 1) ? 532.0 : 470.0;

        boolean lensOn = toggleLensBtn.isSelected();
        double lx = lensSlider.getValue();
        double mz = microSlider.getValue();

        canvas.setPhysicsParameters(isPowerOn, lensOn, lx, mz, wave);
    }

    /*
     * Лабораторна робота № 5-4 "Біпризма Френеля".
     * Функція: resetMeasurementState.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void resetMeasurementState() {
        currentStep = 0;
        valY1 = valY2 = valS1 = valS2 = null;
        y1Label.setText("y1 (смуга 0) = ---");
        y2Label.setText("y2 (смуга 10) = ---");
        s1Label.setText("S1 (джерело 1) = ---");
        s2Label.setText("S2 (джерело 2) = ---");

        if (isPowerOn) {
            liveStepLabel.setText("Етап 1: Інтерференція (без лінзи)");
            liveStepLabel.setStyle("-fx-text-fill: #ff007f; -fx-font-weight: bold;");
        }
        updateRecordButtonUI();
    }

    /*
     * Лабораторна робота № 5-4 "Біпризма Френеля".
     * Функція: updateRecordButtonUI.
     * Призначення: Оновлює дані, статистику або графічний інтерфейс.
     */
    private void updateRecordButtonUI() {
        if (!isPowerOn) {
            recordBtn.setDisable(true);
            recordBtn.setText("ОЧІКУВАННЯ");
            recordBtn.setStyle("-fx-background-color: #90a4ae; -fx-text-fill: white; -fx-font-weight: bold;");
            return;
        }
        recordBtn.setDisable(false);
        switch (currentStep) {
            case 0:
                recordBtn.setText("📝 ЗАФІКСУВАТИ y1 (СМУГА 0)");
                recordBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
                break;
            case 1:
                recordBtn.setText("📝 ЗАФІКСУВАТИ y2 (СМУГА 10)");
                recordBtn.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white; -fx-font-weight: bold;");
                break;
            case 2:
                recordBtn.setText("📝 ЗАФІКСУВАТИ S1 (ЛІВЕ ДЖЕРЕЛО)");
                recordBtn.setStyle("-fx-background-color: #f57c00; -fx-text-fill: white; -fx-font-weight: bold;");
                break;
            case 3:
                recordBtn.setText("💾 ЗАФІКСУВАТИ S2 ТА ОБЧИСЛИТИ");
                recordBtn.setStyle("-fx-background-color: #d84315; -fx-text-fill: white; -fx-font-weight: bold;");
                break;
        }
    }

    /*
     * Лабораторна робота № 5-4 "Біпризма Френеля".
     * Функція: handleRecord.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void handleRecord() {
        if (!isPowerOn) return;

        double mz = microSlider.getValue();
        boolean isLens = toggleLensBtn.isSelected();
        double lensX = lensSlider.getValue();

        switch (currentStep) {
            case 0:
                if (isLens) { showAlert("Увага", "Для вимірювання інтерференції потрібно витягнути лінзу!"); return; }
                valY1 = mz;
                y1Label.setText(String.format(Locale.US, "y1 (смуга 0) = %.3f мм", valY1));
                currentStep = 1;
                liveStepLabel.setText("Етап 1: Відрахуйте 10 смуг");
                break;
            case 1:
                if (isLens) { showAlert("Увага", "Для вимірювання інтерференції потрібно витягнути лінзу!"); return; }
                if (Math.abs(mz - valY1) < 0.1) { showAlert("Увага", "Ви не змістили мікрометр!"); return; }
                valY2 = mz;
                y2Label.setText(String.format(Locale.US, "y2 (смуга 10) = %.3f мм", valY2));
                currentStep = 2;
                liveStepLabel.setText("Етап 2: Вставте лінзу та сфокусуйте");
                liveStepLabel.setStyle("-fx-text-fill: #00e5ff; -fx-font-weight: bold;");
                break;
            case 2:
                if (!isLens) { showAlert("Увага", "Для вимірювання l1 потрібно вставити лінзу!"); return; }
                if (Math.abs(lensX - 20.0) > 0.5) { showAlert("Увага", "Зображення щілин розмите! Знайдіть чіткий фокус лінзи."); return; }
                valS1 = mz;
                s1Label.setText(String.format(Locale.US, "S1 (джерело 1) = %.3f мм", valS1));
                currentStep = 3;
                liveStepLabel.setText("Етап 2: Наведіть на праве джерело");
                break;
            case 3:
                if (!isLens) { showAlert("Увага", "Для вимірювання l1 потрібно вставити лінзу!"); return; }
                valS2 = mz;
                s2Label.setText(String.format(Locale.US, "S2 (джерело 2) = %.3f мм", valS2));

                double dy = Math.abs(valY1 - valY2);
                double l1 = Math.abs(valS1 - valS2);
                double x1_cm = 40.0 - lensX;

                executeMeasurement(dy, l1, x1_cm);

                currentStep = 0;
                liveStepLabel.setText("ГОТОВО! Змініть світлофільтр.");
                liveStepLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
                break;
        }
        updateRecordButtonUI();
    }

    /*
     * Лабораторна робота № 5-4 "Біпризма Френеля".
     * Функція: executeMeasurement.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void executeMeasurement(double dy, double l1, double x1_cm) {
        try {
            int index = filterComboBox.getSelectionModel().getSelectedIndex();
            double trueWave = (index == 0) ? 650.0 : (index == 1) ? 532.0 : 470.0;
            String filterName = filterComboBox.getSelectionModel().getSelectedItem().split(" ")[0];

            double F_mm = 100.0;
            double x1_mm = x1_cm * 10.0;

            double lambdaExpMm = (dy * l1 * F_mm) / (10.0 * x1_mm * x1_mm);
            double lambdaExpNm = lambdaExpMm * 1_000_000.0;

            double error = Math.abs(lambdaExpNm - trueWave) / trueWave * 100.0;

            Measurement m = new Measurement(
                    idCounter++, filterName,
                    Math.round(dy * 1000.0) / 1000.0,
                    Math.round(l1 * 1000.0) / 1000.0,
                    Math.round(x1_cm * 10.0) / 10.0,
                    Math.round(lambdaExpNm * 10.0) / 10.0,
                    Math.round(error * 10.0) / 10.0
            );

            data.add(m);
            updateStats();

        } catch (Exception e) {
            showAlert("Помилка", "Помилка обчислення.");
        }
    }

    /*
     * Лабораторна робота № 5-4 "Біпризма Френеля".
     * Функція: startAutoMode.
     * Призначення: Запускає процес симуляції або відповідний режим роботи.
     */
    private void startAutoMode() {
        if (!isPowerOn) handleTogglePower();
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        autoQueue.add(0);
        autoQueue.add(1);
        autoQueue.add(2);

        processNextAuto();
    }

    /*
     * Лабораторна робота № 5-4 "Біпризма Френеля".
     * Функція: processNextAuto.
     * Призначення: Обробляє поточний крок симуляції або дію користувача.
     */
    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            liveStepLabel.setText("АВТО: ЗАВЕРШЕНО");
            return;
        }

        int mode = autoQueue.poll();
        filterComboBox.getSelectionModel().select(mode);
        double trueWave = (mode == 0) ? 650.0 : (mode == 1) ? 532.0 : 470.0;

        double dy_fringe = (trueWave * 400.0) / (2.0 * 1000000.0);
        double targetY1 = -dy_fringe * 5.0;
        double targetY2 = dy_fringe * 5.0;

        autoTimer = new AnimationTimer() {
            long start = System.nanoTime();
            int phase = 0;

    /*
     * Лабораторна робота № 5-4 "Біпризма Френеля".
     * Функція: handle.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
            @Override
            public void handle(long now) {
                double elapsed = (now - start) / 1_000_000_000.0;

                if (phase == 0) {
                    if (toggleLensBtn.isSelected()) toggleLensBtn.fire();
                    microSlider.setValue(targetY1);
                    if (elapsed > 0.5) { handleRecord(); phase = 1; }
                } else if (phase == 1 && elapsed > 1.0) {
                    microSlider.setValue(targetY2);
                    if (elapsed > 1.5) { handleRecord(); phase = 2; }
                } else if (phase == 2 && elapsed > 2.0) {
                    if (!toggleLensBtn.isSelected()) toggleLensBtn.fire();
                    lensSlider.setValue(20.0);
                    microSlider.setValue(-1.0);
                    if (elapsed > 2.5) { handleRecord(); phase = 3; }
                } else if (phase == 3 && elapsed > 3.0) {
                    microSlider.setValue(1.0);
                    if (elapsed > 3.5) {
                        this.stop();
                        handleRecord();
                        new Thread(() -> {
                            try { Thread.sleep(1000); Platform.runLater(() -> processNextAuto()); }
                            catch (InterruptedException ignored) {}
                        }).start();
                    }
                }
            }
        };
        autoTimer.start();
    }

    /*
     * Лабораторна робота № 5-4 "Біпризма Френеля".
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

        double maxError = 0;
        for (Measurement m : data) {
            if (m.getErrorPercent() > maxError) maxError = m.getErrorPercent();
        }

        String conclusion = String.format(Locale.US,
                "ОБРОБКА РЕЗУЛЬТАТІВ:\n" +
                        "1. Довжина хвилі визначається за формулою: λ = (Δy · l1 · F) / (10 · x1²).\n" +
                        "2. Відстань між уявними джерелами (l1) успішно визначена за допомогою допоміжної лінзи.\n" +
                        "3. Максимальна відносна похибка: ε_max = %.1f %%.\n" +
                        "ВИСНОВОК: Експеримент підтверджує хвильову природу світла. Довжини хвиль збігаються з табличними.",
                maxError
        );

        finalResultLabel.setText(conclusion);
    }
}