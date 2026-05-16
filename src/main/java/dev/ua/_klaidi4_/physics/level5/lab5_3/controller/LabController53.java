/*
 * Лабораторна робота № 5-3 "Показник заломлення".
 * Клас: LabController53.
 * Призначення: керує інтерфейсом лабораторної роботи, запуском досліду,
 * введенням параметрів, таблицею результатів та обробкою вимірювань.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level5.lab5_3.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level5.lab5_3.model.Measurement;
import dev.ua._klaidi4_.physics.level5.lab5_3.view.MicroscopeCanvas;
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

public class LabController53 extends BaseLabController {

    private MicroscopeCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> plateComboBox;
    private Slider focusSlider;
    private Label focusValueLabel;
    private Button togglePowerBtn;
    private Button recTopBtn;
    private Button recBotBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label livePowerLabel;
    private Label topFocusLabel;
    private Label botFocusLabel;
    private Label liveStepLabel;
    private boolean isIlluminatorOn = false;
    private Double recordedTop = null;
    private Double recordedBottom = null;
    private boolean isMeasurementComplete = false;
    private double currentD = 4.0;
    private double trueN = 1.51;
    private final double topAbsoluteZ = 3.0;

    private AnimationTimer autoTimer;
    private Queue<Integer> autoQueue = new LinkedList<>();

    /*
     * Лабораторна робота № 5-3 "Показник заломлення".
     * Функція: LabController53.
     * Призначення: Конструктор класу, ініціалізує інтерфейс та налаштування.
     */
    public LabController53() {
        initUI();
        applyPhysicsSettings();
    }

    /*
     * Лабораторна робота № 5-3 "Показник заломлення".
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
     * Лабораторна робота № 5-3 "Показник заломлення".
     * Функція: initUI.
     * Призначення: Ініціалізує графічний інтерфейс користувача, створює панелі управління та графіки.
     */
    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 5-3)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane configPane = new TitledPane();
        configPane.setText("Мікрометр та оптика");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(12);
        configBox.setPadding(new Insets(5));

        plateComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "Пластинка 1 (d = 4.0 мм)",
                "Пластинка 2 (d = 6.0 мм)",
                "Пластинка 3 (d = 8.0 мм)"
        ));
        plateComboBox.getSelectionModel().selectFirst();
        plateComboBox.setMaxWidth(Double.MAX_VALUE);
        plateComboBox.setOnAction(e -> {
            resetMeasurementState();
            applyPhysicsSettings();
        });

        focusValueLabel = new Label("Відлік мікрометра z: 0.00 мм");

        focusSlider = new Slider(0.0, 10.0, 0.0);
        focusSlider.setShowTickMarks(true);
        focusSlider.setMajorTickUnit(1.0);
        focusSlider.valueProperty().addListener((o, oldV, newV) -> {
            if (isMeasurementComplete) {
                resetMeasurementState();
            }
            focusValueLabel.setText(String.format(Locale.US, "Відлік мікрометра z: %.2f мм", newV.doubleValue()));
            applyPhysicsSettings();
        });

        configBox.getChildren().addAll(
                createInputGroup("Скляна пластинка:", plateComboBox),
                focusValueLabel, focusSlider
        );

        ScrollPane scrollParams = new ScrollPane(configBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(130);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        configPane.setContent(scrollParams);

        togglePowerBtn = new Button("⚡ УВІМКНУТИ ОСВІТЛЮВАЧ");
        togglePowerBtn.setStyle("-fx-background-color: #A155FF; -fx-text-fill: white; -fx-font-weight: bold;");
        togglePowerBtn.setMaxWidth(Double.MAX_VALUE);
        togglePowerBtn.setOnAction(e -> handleTogglePower());

        recTopBtn = new Button("🔴 ЗАФІКСУВАТИ z1 (ВЕРХНІЙ)");
        recTopBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold;");
        recTopBtn.setMaxWidth(Double.MAX_VALUE);
        recTopBtn.setOnAction(e -> handleRecordTop());

        recBotBtn = new Button("🔵 ЗАФІКСУВАТИ z2 (НИЖНІЙ)");
        recBotBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold;");
        recBotBtn.setMaxWidth(Double.MAX_VALUE);
        recBotBtn.setOnAction(e -> handleRecordBottom());

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
            resetMeasurementState();
            updateStats();
        });

        leftPanel.getChildren().addAll(title, configPane, togglePowerBtn, recTopBtn, recBotBtn, autoBtn, clearBtn);

        canvas = new MicroscopeCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(4);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-text-fill: #00e5ff; -fx-padding: 12; -fx-border-color: #00e5ff; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(240, 110);

        Label dashTitle = new Label("ОПТИЧНИЙ СЕНСОР");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        livePowerLabel = new Label("Живлення: ВИМКНЕНО");
        livePowerLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");

        liveStepLabel = new Label("Крок: Увімкніть живлення");
        liveStepLabel.setStyle("-fx-text-fill: yellow;");

        topFocusLabel = new Label("z1 (Верх): [Немає]");
        topFocusLabel.setStyle("-fx-text-fill: #ff007f;");

        botFocusLabel = new Label("z2 (Низ): [Немає]");
        botFocusLabel.setStyle("-fx-text-fill: #00e5ff;");

        dash.getChildren().addAll(dashTitle, livePowerLabel, liveStepLabel, topFocusLabel, botFocusLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, String> pCol = new TableColumn<>("Пластинка");
        pCol.setCellValueFactory(new PropertyValueFactory<>("plate"));
        TableColumn<Measurement, Double> dCol = new TableColumn<>("d (мм)");
        dCol.setCellValueFactory(new PropertyValueFactory<>("d"));
        TableColumn<Measurement, Double> z1Col = new TableColumn<>("z1 (мм)");
        z1Col.setCellValueFactory(new PropertyValueFactory<>("z1"));
        TableColumn<Measurement, Double> z2Col = new TableColumn<>("z2 (мм)");
        z2Col.setCellValueFactory(new PropertyValueFactory<>("z2"));
        TableColumn<Measurement, Double> hCol = new TableColumn<>("h (мм)");
        hCol.setCellValueFactory(new PropertyValueFactory<>("h"));
        TableColumn<Measurement, Double> nCol = new TableColumn<>("n експ");
        nCol.setCellValueFactory(new PropertyValueFactory<>("nExp"));
        TableColumn<Measurement, Double> errCol = new TableColumn<>("ε (%)");
        errCol.setCellValueFactory(new PropertyValueFactory<>("errorPercent"));

        table.getColumns().addAll(idCol, pCol, dCol, z1Col, z2Col, hCol, nCol, errCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(130);

        VBox statsBox = createStatsBox();

        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);
    }

    /*
     * Лабораторна робота № 5-3 "Показник заломлення".
     * Функція: handleTogglePower.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void handleTogglePower() {
        isIlluminatorOn = !isIlluminatorOn;
        if (isIlluminatorOn) {
            togglePowerBtn.setText("⏹ ВИМКНУТИ ОСВІТЛЮВАЧ");
            togglePowerBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold;");
            livePowerLabel.setText("Живлення: УВІМКНЕНО");
            livePowerLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
            resetMeasurementState();
        } else {
            togglePowerBtn.setText("⚡ УВІМКНУТИ ОСВІТЛЮВАЧ");
            togglePowerBtn.setStyle("-fx-background-color: #A155FF; -fx-text-fill: white; -fx-font-weight: bold;");
            livePowerLabel.setText("Живлення: ВИМКНЕНО");
            livePowerLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
            liveStepLabel.setText("Крок: Увімкніть живлення");
        }
        applyPhysicsSettings();
    }

    /*
     * Лабораторна робота № 5-3 "Показник заломлення".
     * Функція: applyPhysicsSettings.
     * Призначення: Застосовує поточні налаштування до симуляції.
     */
    private void applyPhysicsSettings() {
        int index = plateComboBox.getSelectionModel().getSelectedIndex();
        currentD = (index == 0) ? 4.0 : (index == 1) ? 6.0 : 8.0;

        if (recordedTop == null && recordedBottom == null) {
            trueN = 1.51 + (Math.random() - 0.5) * 0.02;
        }

        double focusZ = focusSlider.getValue();
        canvas.setPhysicsParameters(isIlluminatorOn, focusZ, currentD, trueN);
    }

    /*
     * Лабораторна робота № 5-3 "Показник заломлення".
     * Функція: resetMeasurementState.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void resetMeasurementState() {
        isMeasurementComplete = false;
        recordedTop = null;
        recordedBottom = null;
        topFocusLabel.setText("z1 (Верх): [Немає]");
        botFocusLabel.setText("z2 (Низ): [Немає]");

        if (isIlluminatorOn) {
            liveStepLabel.setText("Крок 1: Знайдіть верхній штрих");
        }
    }

    /*
     * Лабораторна робота № 5-3 "Показник заломлення".
     * Функція: handleRecordTop.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void handleRecordTop() {
        if (!isIlluminatorOn) {
            showAlert("Помилка", "Спочатку увімкніть освітлювач мікроскопа.");
            return;
        }
        double focusZ = focusSlider.getValue();
        if (Math.abs(focusZ - topAbsoluteZ) > 0.3) {
            showAlert("Увага", "Верхній штрих не у фокусі! Підкрутіть мікрометр.");
            return;
        }

        recordedTop = focusZ;
        topFocusLabel.setText(String.format(Locale.US, "z1 (Верх): %.3f мм", recordedTop));
        liveStepLabel.setText("Крок 2: Знайдіть нижній штрих");
        checkAndAutoCalculate();
    }

    /*
     * Лабораторна робота № 5-3 "Показник заломлення".
     * Функція: handleRecordBottom.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void handleRecordBottom() {
        if (!isIlluminatorOn) {
            showAlert("Помилка", "Спочатку увімкніть освітлювач мікроскопа.");
            return;
        }
        double focusZ = focusSlider.getValue();
        double apparentBotZ = topAbsoluteZ + (currentD / trueN);

        if (Math.abs(focusZ - apparentBotZ) > 0.3) {
            showAlert("Увага", "Нижній штрих не у фокусі! Підкрутіть мікрометр.");
            return;
        }

        recordedBottom = focusZ;
        botFocusLabel.setText(String.format(Locale.US, "z2 (Низ): %.3f мм", recordedBottom));
        checkAndAutoCalculate();
    }

    /*
     * Лабораторна робота № 5-3 "Показник заломлення".
     * Функція: checkAndAutoCalculate.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void checkAndAutoCalculate() {
        if (recordedTop != null && recordedBottom != null) {
            double h = Math.abs(recordedBottom - recordedTop);
            executeMeasurement(currentD, recordedTop, recordedBottom, h);

            isMeasurementComplete = true;
            liveStepLabel.setText("ГОТОВО! Змініть пластинку.");
        }
    }

    /*
     * Лабораторна робота № 5-3 "Показник заломлення".
     * Функція: executeMeasurement.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void executeMeasurement(double d, double z1, double z2, double h) {
        try {
            double nExp = d / h;
            double error = Math.abs(nExp - 1.51) / 1.51 * 100.0;
            String plateName = plateComboBox.getSelectionModel().getSelectedItem().substring(0, 11);

            Measurement m = new Measurement(
                    idCounter++, plateName,
                    Math.round(d * 10.0) / 10.0,
                    Math.round(z1 * 1000.0) / 1000.0,
                    Math.round(z2 * 1000.0) / 1000.0,
                    Math.round(h * 1000.0) / 1000.0,
                    Math.round(nExp * 1000.0) / 1000.0,
                    Math.round(error * 10.0) / 10.0
            );

            data.add(m);
            updateStats();

        } catch (Exception e) {
            showAlert("Помилка", "Сталася помилка при обчисленні.");
        }
    }

    /*
     * Лабораторна робота № 5-3 "Показник заломлення".
     * Функція: startAutoMode.
     * Призначення: Запускає процес симуляції або відповідний режим роботи.
     */
    private void startAutoMode() {
        if (!isIlluminatorOn) handleTogglePower();
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
     * Лабораторна робота № 5-3 "Показник заломлення".
     * Функція: processNextAuto.
     * Призначення: Обробляє поточний крок симуляції або дію користувача.
     */
    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            liveStepLabel.setText("АВТО: ЗАВЕРШЕНО");
            return;
        }

        int mode = autoQueue.poll();
        plateComboBox.getSelectionModel().select(mode);

        double targetZ1 = topAbsoluteZ;
        double targetZ2 = topAbsoluteZ + (currentD / trueN);

        autoTimer = new AnimationTimer() {
            long start = System.nanoTime();
            boolean topRecorded = false;

    /*
     * Лабораторна робота № 5-3 "Показник заломлення".
     * Функція: handle.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
            @Override
            public void handle(long now) {
                double elapsed = (now - start) / 1_000_000_000.0;

                if (elapsed < 1.0) {
                    focusSlider.setValue(targetZ1 * (elapsed / 1.0));
                } else if (elapsed > 1.2 && !topRecorded) {
                    handleRecordTop();
                    topRecorded = true;
                } else if (elapsed > 1.5 && elapsed < 3.0) {
                    double progress = (elapsed - 1.5) / 1.5;
                    focusSlider.setValue(targetZ1 + (targetZ2 - targetZ1) * progress);
                } else if (elapsed > 3.2) {
                    this.stop();
                    focusSlider.setValue(targetZ2);
                    handleRecordBottom();

                    new Thread(() -> {
                        try { Thread.sleep(1500); Platform.runLater(() -> processNextAuto()); }
                        catch (InterruptedException ignored) {}
                    }).start();
                }
            }
        };
        autoTimer.start();
    }

    /*
     * Лабораторна робота № 5-3 "Показник заломлення".
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
        double sumN = 0;
        for (Measurement m : data) {
            sumN += m.getNExp();
            if (m.getErrorPercent() > maxError) maxError = m.getErrorPercent();
        }
        double avgN = sumN / data.size();

        String conclusion = String.format(Locale.US,
                "ОБРОБКА РЕЗУЛЬТАТІВ:\n" +
                        "1. Середній показник заломлення скла: n_сер = %.3f.\n" +
                        "2. Розраховано за формулою: n = d / h, де h = |z1 - z2|.\n" +
                        "3. Максимальна відносна похибка: ε_max = %.1f %%.\n" +
                        "ВИСНОВОК: Метод уявного зменшення товщини дозволяє з високою точністю визначати показник заломлення.",
                avgN, maxError
        );

        finalResultLabel.setText(conclusion);
    }
}