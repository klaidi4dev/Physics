/*
 * Лабораторна робота № 4-6 "Поперечні коливання струни".
 * Клас: LabController46.
 * Призначення: керує інтерфейсом лабораторної роботи, запуском досліду,
 * введенням параметрів, таблицею результатів та обробкою вимірювань.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level4.lab4_6.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level4.lab4_6.model.Measurement;
import dev.ua._klaidi4_.physics.level4.lab4_6.view.StringResonanceCanvas;
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

public class LabController46 extends BaseLabController {

    private StringResonanceCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField rhoField;
    private TextField dField;
    private TextField lField;
    private TextField fField;
    private Slider freqSlider;
    private Label freqValueLabel;
    private Label liveStatusLabel;
    private Label liveAmpLabel;
    private Label liveHarmonicLabel;
    private Button toggleGenBtn;
    private Button measureBtn;
    private Button autoBtn;
    private Button clearBtn;

    private boolean isGeneratorOn = false;
    private AnimationTimer measurementTimer;

    private Queue<Integer> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: LabController46.
     * Призначення: Конструктор класу, ініціалізує інтерфейс та налаштування.
     */
    public LabController46() {
        initUI();
        applyPhysicsSettings();
    }

    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: shutdown.
     * Призначення: Зупиняє процеси та очищує ресурси при закритті вікна.
     */
    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        if (measurementTimer != null) measurementTimer.stop();
        isAutoRunning = false;
        isGeneratorOn = false;
        autoQueue.clear();
    }

    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: initUI.
     * Призначення: Ініціалізує графічний інтерфейс користувача, створює панелі управління та графіки.
     */
    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 4-6)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane configPane = new TitledPane();
        configPane.setText("Параметри струни та генератора");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(12);
        configBox.setPadding(new Insets(5));

        rhoField = new TextField("7800");
        dField = new TextField("1.0");
        lField = new TextField("1.0");
        fField = new TextField("20.0");

        freqSlider = new Slider(10.0, 160.0, 10.0);
        freqSlider.setShowTickMarks(true);
        freqSlider.setMajorTickUnit(20.0);
        freqValueLabel = new Label("Частота генератора f = 10.0 Гц");

        rhoField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        dField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        lField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        fField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());

        freqSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            freqValueLabel.setText(String.format(Locale.US, "Частота генератора f = %.1f Гц", newVal.doubleValue()));
            applyPhysicsSettings();
        });

        configBox.getChildren().addAll(
                createInputGroup("Об'ємна густина ρ (кг/м³):", rhoField),
                createInputGroup("Діаметр струни d (мм):", dField),
                createInputGroup("Довжина струни l (м):", lField),
                createInputGroup("Сила натягу струни F (Н):", fField),
                new Label("Генератор змінного струму:"), freqValueLabel, freqSlider
        );

        ScrollPane scrollParams = new ScrollPane(configBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(300);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        configPane.setContent(scrollParams);

        toggleGenBtn = new Button("⚡ УВІМКНУТИ ГЕНЕРАТОР");
        toggleGenBtn.setStyle("-fx-background-color: #A155FF; -fx-text-fill: white; -fx-font-weight: bold;");
        toggleGenBtn.setMaxWidth(Double.MAX_VALUE);
        toggleGenBtn.setOnAction(e -> handleToggleGenerator());

        measureBtn = new Button("📝 ЗАПИСАТИ ВИМІРИ");
        measureBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        measureBtn.setMaxWidth(Double.MAX_VALUE);
        measureBtn.setOnAction(e -> handleManualMeasurement());

        autoBtn = new Button("⚙ ЗНАЙТИ 1, 3, 5 ГАРМОНІКИ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAuto());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear();
            idCounter = 1;
            updateStats();
        });

        leftPanel.getChildren().addAll(title, configPane, toggleGenBtn, measureBtn, autoBtn, clearBtn);

        canvas = new StringResonanceCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(4);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-text-fill: #00ffcc; -fx-padding: 12; -fx-border-color: #00ffcc; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(240, 90);
        Label dashTitle = new Label("ОСЦИЛОГРАМА");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        liveStatusLabel = new Label("Генератор: ВИМКНЕНО");
        liveStatusLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
        liveAmpLabel = new Label("Сигнал: ВІДСУТНІЙ");
        liveAmpLabel.setStyle("-fx-text-fill: gray;");
        liveHarmonicLabel = new Label("Гармоніка (n): 0");
        liveHarmonicLabel.setStyle("-fx-text-fill: #ff007f; -fx-font-weight: bold;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveAmpLabel, liveHarmonicLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, Double> fCol = new TableColumn<>("F (Н)");
        fCol.setCellValueFactory(new PropertyValueFactory<>("tension"));
        TableColumn<Measurement, Integer> nCol = new TableColumn<>("Гарм (n)");
        nCol.setCellValueFactory(new PropertyValueFactory<>("n"));
        TableColumn<Measurement, Double> expCol = new TableColumn<>("f_ген (Гц)");
        expCol.setCellValueFactory(new PropertyValueFactory<>("fExp"));
        TableColumn<Measurement, Double> thCol = new TableColumn<>("ν_теор (Гц)");
        thCol.setCellValueFactory(new PropertyValueFactory<>("nuTheo"));
        TableColumn<Measurement, Double> errCol = new TableColumn<>("ε (%)");
        errCol.setCellValueFactory(new PropertyValueFactory<>("errorPercent"));

        table.getColumns().addAll(idCol, fCol, nCol, expCol, thCol, errCol);
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
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: applyPhysicsSettings.
     * Призначення: Застосовує поточні налаштування до симуляції.
     */
    private void applyPhysicsSettings() {
        try {
            double rho = Double.parseDouble(rhoField.getText());
            double d = Double.parseDouble(dField.getText()) * 0.001;
            double l = Double.parseDouble(lField.getText());
            double f = Double.parseDouble(fField.getText());

            canvas.setPhysicsParameters(l, d, rho, f, freqSlider.getValue());
            updateDashboard();
        } catch (NumberFormatException ignored) {}
    }

    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: updateDashboard.
     * Призначення: Оновлює дані, статистику або графічний інтерфейс.
     */
    private void updateDashboard() {
        if (!isGeneratorOn) {
            liveStatusLabel.setText("Генератор: ВИМКНЕНО");
            liveStatusLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
            liveAmpLabel.setText("Сигнал: ВІДСУТНІЙ");
            liveAmpLabel.setStyle("-fx-text-fill: gray;");
            liveHarmonicLabel.setText("Гармоніка (n): 0");
            return;
        }

        liveStatusLabel.setText("Генератор: ПРАЦЮЄ");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");

        if (canvas.getCurrentAmp() > 15.0) {
            liveAmpLabel.setText("Сигнал: РЕЗОНАНС");
            liveAmpLabel.setStyle("-fx-text-fill: #00ffcc; -fx-font-weight: bold;");
            liveHarmonicLabel.setText("Гармоніка (n): " + canvas.getActiveHarmonic());
        } else {
            liveAmpLabel.setText("Сигнал: ШУМ");
            liveAmpLabel.setStyle("-fx-text-fill: yellow;");
            liveHarmonicLabel.setText("Гармоніка (n): -");
        }
    }

    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: handleToggleGenerator.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void handleToggleGenerator() {
        isGeneratorOn = !isGeneratorOn;
        if (isGeneratorOn) {
            toggleGenBtn.setText("⏹ ВИМКНУТИ ГЕНЕРАТОР");
            toggleGenBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            toggleGenBtn.setText("⚡ УВІМКНУТИ ГЕНЕРАТОР");
            toggleGenBtn.setStyle("-fx-background-color: #A155FF; -fx-text-fill: white; -fx-font-weight: bold;");
        }

        canvas.toggleGenerator(isGeneratorOn);
        updateDashboard();
    }

    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: setControlsDisable.
     * Призначення: Встановлює нове значення для вказаного параметра.
     */
    private void setControlsDisable(boolean disable) {
        freqSlider.setDisable(disable);
        rhoField.setDisable(disable);
        dField.setDisable(disable);
        lField.setDisable(disable);
        fField.setDisable(disable);
        measureBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        toggleGenBtn.setDisable(disable);
    }

    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: handleManualMeasurement.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void handleManualMeasurement() {
        if (!isGeneratorOn) {
            showAlert("Увага", "Спочатку увімкніть генератор!");
            return;
        }
        if (canvas.getCurrentAmp() <= 15.0) {
            showAlert("Помилка", "Відсутній резонанс! Змініть частоту генератора так, щоб зловити чітку стоячу хвилю (гармоніку).");
            return;
        }

        isAutoRunning = false;
        executeMeasurement(freqSlider.getValue(), canvas.getActiveHarmonic());
    }

    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: startAuto.
     * Призначення: Запускає процес симуляції або відповідний режим роботи.
     */
    private void startAuto() {
        if (!isGeneratorOn) handleToggleGenerator();

        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        autoQueue.add(1);
        autoQueue.add(3);
        autoQueue.add(5);

        isAutoRunning = true;
        processNextAuto();
    }

    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: processNextAuto.
     * Призначення: Обробляє поточний крок симуляції або дію користувача.
     */
    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoRunning = false;
            setControlsDisable(false);
            liveStatusLabel.setText("Генератор: ПРАЦЮЄ (ГОТОВО)");
            return;
        }

        int targetN = autoQueue.poll();

        try {
            double rho = Double.parseDouble(rhoField.getText());
            double d = Double.parseDouble(dField.getText()) * 0.001;
            double l = Double.parseDouble(lField.getText());
            double tension = Double.parseDouble(fField.getText());

            double v1 = (1.0 / (l * d)) * Math.sqrt(tension / (Math.PI * rho));
            double targetFreq = targetN * v1;

            double simFreq = targetFreq + (Math.random() - 0.5) * 0.6;

            freqSlider.setValue(simFreq);
            setControlsDisable(true);

            long start = System.nanoTime();
            measurementTimer = new AnimationTimer() {
    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: handle.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
                @Override
                public void handle(long now) {
                    double elapsed = (now - start) / 1_000_000_000.0;
                    if (elapsed >= 1.5) {
                        this.stop();
                        executeMeasurement(freqSlider.getValue(), targetN);
                    }
                }
            };
            measurementTimer.start();

        } catch (Exception e) {
            showAlert("Помилка", "Некоректні параметри системи.");
            isAutoRunning = false;
            setControlsDisable(false);
        }
    }

    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: executeMeasurement.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void executeMeasurement(double fExp, int n) {
        try {
            double rho = Double.parseDouble(rhoField.getText());
            double d = Double.parseDouble(dField.getText()) * 0.001;
            double l = Double.parseDouble(lField.getText());
            double tension = Double.parseDouble(fField.getText());

            double nuTheo = (n / (l * d)) * Math.sqrt(tension / (Math.PI * rho));
            double error = Math.abs(fExp - nuTheo) / nuTheo * 100.0;

            Measurement m = new Measurement(
                    idCounter++, tension, n,
                    Math.round(fExp * 10.0) / 10.0,
                    Math.round(nuTheo * 10.0) / 10.0,
                    Math.round(error * 100.0) / 100.0
            );

            data.add(m);
            updateStats();

        } catch (Exception e) {
            showAlert("Помилка", "Помилка при записі вимірів.");
        }

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(1000); Platform.runLater(this::processNextAuto); }
                catch (InterruptedException ignored) {}
            }).start();
        } else {
            setControlsDisable(false);
        }
    }

    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
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
                        "1. Частоти знайдених гармонік (f1, f3, f5) відповідають формулі ν_n = n * ν_1.\n" +
                        "2. Максимальна відносна похибка: ε_max = %.2f %%.\n" +
                        "ВИСНОВОК: Експериментально підтверджено дискретність частотного спектра струни. " +
                        "Магніт по центру збуджує виключно непарні гармоніки, оскільки для парних там знаходиться вузол.",
                maxError
        );

        finalResultLabel.setText(conclusion);
    }
}