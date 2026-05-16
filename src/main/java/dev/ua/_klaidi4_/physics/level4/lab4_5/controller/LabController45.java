/*
 * Лабораторна робота № 4-5 "Частота мультивібратора".
 * Клас: LabController45.
 * Призначення: керує інтерфейсом лабораторної роботи, запуском досліду,
 * введенням параметрів, таблицею результатів та обробкою вимірювань.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level4.lab4_5.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level4.lab4_5.model.Measurement;
import dev.ua._klaidi4_.physics.level4.lab4_5.view.StandingWaveCanvas;
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

public class LabController45 extends BaseLabController {

    private StandingWaveCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField nuField;
    private TextField tauField;
    private TextField l0Field;
    private TextField deltaLField;
    private TextField cosPhiField;
    private Slider fSlider;
    private Label fValueLabel;
    private Label liveStatusLabel;
    private Label liveAmpLabel;
    private Label liveNLabel;
    private Button toggleGenBtn;
    private Button measureBtn;
    private Button autoBtn;
    private Button clearBtn;
    private boolean isGeneratorOn = false;
    private AnimationTimer measurementTimer;
    private Queue<Double> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    /*
     * Лабораторна робота № 4-5 "Частота мультивібратора".
     * Функція: LabController45.
     * Призначення: Конструктор класу, ініціалізує інтерфейс та налаштування.
     */
    public LabController45() {
        initUI();
        applyPhysicsSettings();
    }

    /*
     * Лабораторна робота № 4-5 "Частота мультивібратора".
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
     * Лабораторна робота № 4-5 "Частота мультивібратора".
     * Функція: initUI.
     * Призначення: Ініціалізує графічний інтерфейс користувача, створює панелі управління та графіки.
     */
    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 4-5)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane configPane = new TitledPane();
        configPane.setText("Параметри струни");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(12);
        configBox.setPadding(new Insets(5));

        nuField = new TextField("50.0");
        tauField = new TextField("0.002");
        l0Field = new TextField("1.05");
        deltaLField = new TextField("0.05");
        cosPhiField = new TextField("0.5");

        fSlider = new Slider(0.5, 35.0, 0.5);
        fSlider.setShowTickMarks(true);
        fSlider.setMajorTickUnit(5.0);
        fValueLabel = new Label("Натяг струни F = 0.5 Н");

        nuField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        tauField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        l0Field.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        deltaLField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        cosPhiField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());

        fSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            fValueLabel.setText(String.format(Locale.US, "Натяг струни F = %.1f Н", newVal.doubleValue()));
            applyPhysicsSettings();
        });

        configBox.getChildren().addAll(
                createInputGroup("Частота генератора ν (Гц):", nuField),
                createInputGroup("Лінійна густина τ (кг/м):", tauField),
                createInputGroup("Початкова довжина l0 (м):", l0Field),
                createInputGroup("Зміна довжини Δl (м):", deltaLField),
                createInputGroup("Коеф. кута cos(φ):", cosPhiField),
                new Label("Регулювання натягу динамометра:"), fValueLabel, fSlider
        );

        ScrollPane scrollParams = new ScrollPane(configBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(280);
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

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ ЛАБИ");
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

        canvas = new StandingWaveCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(4);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-text-fill: #00e5ff; -fx-padding: 12; -fx-border-color: #00e5ff; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(240, 100);
        Label dashTitle = new Label("СЕНСОРНА ПАНЕЛЬ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        liveStatusLabel = new Label("Генератор: ВИМКНЕНО");
        liveStatusLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
        liveAmpLabel = new Label("Стан: ШУМ");
        liveAmpLabel.setStyle("-fx-text-fill: yellow;");
        liveNLabel = new Label("Пучностей (n): 0");
        liveNLabel.setStyle("-fx-text-fill: #ff007f; -fx-font-weight: bold;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveAmpLabel, liveNLabel);

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
        fCol.setCellValueFactory(new PropertyValueFactory<>("f"));
        TableColumn<Measurement, Integer> nCol = new TableColumn<>("n (пучн)");
        nCol.setCellValueFactory(new PropertyValueFactory<>("n"));
        TableColumn<Measurement, Double> nuCol = new TableColumn<>("ν експ (Гц)");
        nuCol.setCellValueFactory(new PropertyValueFactory<>("nuExp"));
        TableColumn<Measurement, Double> errCol = new TableColumn<>("ε (%)");
        errCol.setCellValueFactory(new PropertyValueFactory<>("errorPercent"));

        table.getColumns().addAll(idCol, fCol, nCol, nuCol, errCol);
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
     * Лабораторна робота № 4-5 "Частота мультивібратора".
     * Функція: applyPhysicsSettings.
     * Призначення: Застосовує поточні налаштування до симуляції.
     */
    private void applyPhysicsSettings() {
        try {
            double targetNu = Double.parseDouble(nuField.getText());
            double tau = Double.parseDouble(tauField.getText());
            double l0 = Double.parseDouble(l0Field.getText());
            double deltaL = Double.parseDouble(deltaLField.getText());
            double cosPhi = Double.parseDouble(cosPhiField.getText());
            double lActive = l0 - deltaL;

            canvas.setPhysicsParameters(fSlider.getValue(), tau, cosPhi, lActive, targetNu);
            updateDashboard();
        } catch (NumberFormatException ignored) {}
    }

    /*
     * Лабораторна робота № 4-5 "Частота мультивібратора".
     * Функція: updateDashboard.
     * Призначення: Оновлює дані, статистику або графічний інтерфейс.
     */
    private void updateDashboard() {
        if (!isGeneratorOn) {
            liveStatusLabel.setText("Генератор: ВИМКНЕНО");
            liveStatusLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
            liveAmpLabel.setText("Стан: СПОКІЙ");
            liveAmpLabel.setStyle("-fx-text-fill: gray;");
            liveNLabel.setText("Пучностей (n): 0");
            return;
        }

        liveStatusLabel.setText("Генератор: ПРАЦЮЄ");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");

        if (canvas.getCurrentAmp() > 15.0) {
            liveAmpLabel.setText("Стан: РЕЗОНАНС");
            liveAmpLabel.setStyle("-fx-text-fill: #00e5ff; -fx-font-weight: bold;");
            liveNLabel.setText("Пучностей (n): " + canvas.getActiveN());
        } else {
            liveAmpLabel.setText("Стан: ШУМ (Шукайте резонанс)");
            liveAmpLabel.setStyle("-fx-text-fill: yellow;");
            liveNLabel.setText("Пучностей (n): -");
        }
    }

    /*
     * Лабораторна робота № 4-5 "Частота мультивібратора".
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
     * Лабораторна робота № 4-5 "Частота мультивібратора".
     * Функція: setControlsDisable.
     * Призначення: Встановлює нове значення для вказаного параметра.
     */
    private void setControlsDisable(boolean disable) {
        fSlider.setDisable(disable);
        nuField.setDisable(disable);
        tauField.setDisable(disable);
        l0Field.setDisable(disable);
        deltaLField.setDisable(disable);
        cosPhiField.setDisable(disable);
        measureBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        toggleGenBtn.setDisable(disable);
    }

    /*
     * Лабораторна робота № 4-5 "Частота мультивібратора".
     * Функція: handleManualMeasurement.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void handleManualMeasurement() {
        if (!isGeneratorOn) {
            showAlert("Увага", "Спочатку увімкніть генератор і налаштуйте резонанс!");
            return;
        }
        if (canvas.getCurrentAmp() <= 15.0) {
            showAlert("Помилка", "Немає стійкої стоячої хвилі! Плавно змініть силу натягу F, щоб отримати резонанс (чіткі пучності).");
            return;
        }

        isAutoRunning = false;
        executeMeasurement(fSlider.getValue(), canvas.getActiveN());
    }

    /*
     * Лабораторна робота № 4-5 "Частота мультивібратора".
     * Функція: startAuto.
     * Призначення: Запускає процес симуляції або відповідний режим роботи.
     */
    private void startAuto() {
        if (!isGeneratorOn) handleToggleGenerator();

        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        autoQueue.add(30.0);
        autoQueue.add(7.5);
        autoQueue.add(3.3);
        autoQueue.add(1.9);

        isAutoRunning = true;
        processNextAuto();
    }

    /*
     * Лабораторна робота № 4-5 "Частота мультивібратора".
     * Функція: processNextAuto.
     * Призначення: Обробляє поточний крок симуляції або дію користувача.
     */
    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoRunning = false;
            setControlsDisable(false);
            liveStatusLabel.setText("Генератор: ГОТОВО");
            return;
        }

        double nextF = autoQueue.poll();
        fSlider.setValue(nextF);

        setControlsDisable(true);
        long start = System.nanoTime();

        measurementTimer = new AnimationTimer() {
    /*
     * Лабораторна робота № 4-5 "Частота мультивібратора".
     * Функція: handle.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
            @Override
            public void handle(long now) {
                double elapsed = (now - start) / 1_000_000_000.0;
                if (elapsed >= 1.5) {
                    this.stop();
                    executeMeasurement(nextF, canvas.getActiveN());
                }
            }
        };
        measurementTimer.start();
    }

    /*
     * Лабораторна робота № 4-5 "Частота мультивібратора".
     * Функція: executeMeasurement.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void executeMeasurement(double f, int n) {
        try {
            double tau = Double.parseDouble(tauField.getText());
            double l0 = Double.parseDouble(l0Field.getText());
            double deltaL = Double.parseDouble(deltaLField.getText());
            double cosPhi = Double.parseDouble(cosPhiField.getText());
            double targetTrueNu = Double.parseDouble(nuField.getText());

            double nuExp = (n / (2.0 * (l0 - deltaL))) * Math.sqrt(f / (tau * (1.0 + cosPhi)));

            double error = Math.abs(nuExp - targetTrueNu) / targetTrueNu * 100.0;

            Measurement m = new Measurement(
                    idCounter++,
                    Math.round(f * 10.0) / 10.0,
                    n,
                    Math.round(nuExp * 100.0) / 100.0,
                    Math.round(error * 10.0) / 10.0
            );

            data.add(m);
            updateStats();

        } catch (Exception e) {
            showAlert("Помилка", "Перевірте правильність введених параметрів.");
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
     * Лабораторна робота № 4-5 "Частота мультивібратора".
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

        double sumNu = 0;
        double maxError = 0;
        for (Measurement m : data) {
            sumNu += m.getNuExp();
            if (m.getErrorPercent() > maxError) maxError = m.getErrorPercent();
        }
        double avgNu = sumNu / data.size();

        String conclusion = String.format(Locale.US,
                "ОБРОБКА РЕЗУЛЬТАТІВ:\n" +
                        "1. Середня експериментальна частота генератора: ν_сер = %.2f Гц.\n" +
                        "2. Максимальна відносна похибка вимірювання: ε_max = %.1f %%.\n" +
                        "ВИСНОВОК: Зафіксовано обернено пропорційну залежність між кількістю пучностей n " +
                        "та коренем із сили натягу струни. Отримана частота відповідає заданій частоті мультивібратора.",
                avgNu, maxError
        );

        finalResultLabel.setText(conclusion);
    }
}