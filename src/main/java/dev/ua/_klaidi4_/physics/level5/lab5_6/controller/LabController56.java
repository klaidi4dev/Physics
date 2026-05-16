/*
 * Лабораторна робота № 5-6 "Дифракційна решітка".
 * Клас: LabController56.
 * Призначення: керує інтерфейсом лабораторної роботи, запуском досліду,
 * введенням параметрів, таблицею результатів та обробкою вимірювань.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level5.lab5_6.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level5.lab5_6.model.Measurement;
import dev.ua._klaidi4_.physics.level5.lab5_6.view.DiffractionCanvas;
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

public class LabController56 extends BaseLabController {

    private DiffractionCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> filterComboBox;
    private ComboBox<String> gratingComboBox;
    private Slider angleSlider;
    private Label angleValueLabel;
    private Button togglePowerBtn;
    private Button recordBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStepLabel;
    private Label phiR1Label, phiL1Label, phiR2Label, phiL2Label;
    private boolean isPowerOn = false;
    private int currentStep = 0;
    private Double phiR1 = null, phiL1 = null, phiR2 = null, phiL2 = null;
    private AnimationTimer autoTimer;
    private Queue<Integer> autoQueue = new LinkedList<>();

    /*
     * Лабораторна робота № 5-6 "Дифракційна решітка".
     * Функція: LabController56.
     * Призначення: Конструктор класу, ініціалізує інтерфейс та налаштування.
     */
    public LabController56() {
        initUI();
        applyPhysicsSettings();
    }

    /*
     * Лабораторна робота № 5-6 "Дифракційна решітка".
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
     * Лабораторна робота № 5-6 "Дифракційна решітка".
     * Функція: initUI.
     * Призначення: Ініціалізує графічний інтерфейс користувача, створює панелі управління та графіки.
     */
    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 5-6)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane configPane = new TitledPane();
        configPane.setText("Параметри установки");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(12);
        configBox.setPadding(new Insets(5));

        filterComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "Жовтий (589 нм)",
                "Зелений (532 нм)",
                "Синій (470 нм)"
        ));
        filterComboBox.getSelectionModel().selectFirst();
        filterComboBox.setMaxWidth(Double.MAX_VALUE);
        filterComboBox.setOnAction(e -> {
            resetMeasurementState();
            applyPhysicsSettings();
        });

        gratingComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "Решітка 1 (100 штр/мм, d=10 мкм)",
                "Решітка 2 (50 штр/мм, d=20 мкм)"
        ));
        gratingComboBox.getSelectionModel().selectFirst();
        gratingComboBox.setMaxWidth(Double.MAX_VALUE);
        gratingComboBox.setOnAction(e -> {
            resetMeasurementState();
            applyPhysicsSettings();
        });

        angleValueLabel = new Label("Кут повороту труби φ: 0.00°");

        angleSlider = new Slider(-15.0, 15.0, 0.0);
        angleSlider.setShowTickMarks(true);
        angleSlider.setMajorTickUnit(5.0);
        angleSlider.valueProperty().addListener((o, ov, nv) -> {
            angleValueLabel.setText(String.format(Locale.US, "Кут повороту труби φ: %.2f°", nv.doubleValue()));
            applyPhysicsSettings();
        });

        configBox.getChildren().addAll(
                createInputGroup("Світлофільтр (Джерело):", filterComboBox),
                createInputGroup("Дифракційна решітка:", gratingComboBox),
                angleValueLabel, angleSlider
        );

        ScrollPane scrollParams = new ScrollPane(configBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(200);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        configPane.setContent(scrollParams);

        togglePowerBtn = new Button("⚡ УВІМКНУТИ ОСВІТЛЮВАЧ");
        togglePowerBtn.setStyle("-fx-background-color: #A155FF; -fx-text-fill: white; -fx-font-weight: bold;");
        togglePowerBtn.setMaxWidth(Double.MAX_VALUE);
        togglePowerBtn.setOnAction(e -> handleTogglePower());

        recordBtn = new Button("ОЧІКУВАННЯ");
        recordBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold;");
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

        canvas = new DiffractionCanvas(600, 440);

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

        Label dashTitle = new Label("ДАНІ ГОНІОМЕТРА");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        liveStepLabel = new Label("Крок: Увімкніть живлення");
        liveStepLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");

        phiR1Label = new Label("φ_прав (k=1): ---"); phiR1Label.setStyle("-fx-text-fill: #ff007f;");
        phiL1Label = new Label("φ_лів (k=1): ---"); phiL1Label.setStyle("-fx-text-fill: #ff007f;");
        phiR2Label = new Label("φ_прав (k=2): ---"); phiR2Label.setStyle("-fx-text-fill: #00ffcc;");
        phiL2Label = new Label("φ_лів (k=2): ---"); phiL2Label.setStyle("-fx-text-fill: #00ffcc;");

        dash.getChildren().addAll(dashTitle, liveStepLabel, phiR1Label, phiL1Label, phiR2Label, phiL2Label);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, String> fCol = new TableColumn<>("Фільтр");
        fCol.setCellValueFactory(new PropertyValueFactory<>("filterColor"));
        TableColumn<Measurement, Double> dCol = new TableColumn<>("d (мкм)");
        dCol.setCellValueFactory(new PropertyValueFactory<>("dGrating"));
        TableColumn<Measurement, Double> p1Col = new TableColumn<>("φ₁ (°)");
        p1Col.setCellValueFactory(new PropertyValueFactory<>("phi1"));
        TableColumn<Measurement, Double> p2Col = new TableColumn<>("φ₂ (°)");
        p2Col.setCellValueFactory(new PropertyValueFactory<>("phi2"));
        TableColumn<Measurement, Double> lamCol = new TableColumn<>("λ (нм)");
        lamCol.setCellValueFactory(new PropertyValueFactory<>("lambdaExp"));
        TableColumn<Measurement, Double> errCol = new TableColumn<>("ε (%)");
        errCol.setCellValueFactory(new PropertyValueFactory<>("errorPercent"));

        table.getColumns().addAll(idCol, fCol, dCol, p1Col, p2Col, lamCol, errCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(130);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        updateButtonUI();
    }

    /*
     * Лабораторна робота № 5-6 "Дифракційна решітка".
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
        updateButtonUI();
        applyPhysicsSettings();
    }

    /*
     * Лабораторна робота № 5-6 "Дифракційна решітка".
     * Функція: getCurrentWavelength.
     * Призначення: Повертає необхідне значення параметра або об'єкта.
     */
    private double getCurrentWavelength() {
        int index = filterComboBox.getSelectionModel().getSelectedIndex();
        if (index == 0) return 589.0;
        if (index == 1) return 532.0;
        return 470.0;
    }

    /*
     * Лабораторна робота № 5-6 "Дифракційна решітка".
     * Функція: getCurrentGratingD.
     * Призначення: Повертає необхідне значення параметра або об'єкта.
     */
    private double getCurrentGratingD() {
        int index = gratingComboBox.getSelectionModel().getSelectedIndex();
        if (index == 0) return 10000.0;
        return 20000.0;
    }

    /*
     * Лабораторна робота № 5-6 "Дифракційна решітка".
     * Функція: applyPhysicsSettings.
     * Призначення: Застосовує поточні налаштування до симуляції.
     */
    private void applyPhysicsSettings() {
        canvas.setPhysicsParameters(isPowerOn, angleSlider.getValue(), getCurrentWavelength(), getCurrentGratingD());
    }

    /*
     * Лабораторна робота № 5-6 "Дифракційна решітка".
     * Функція: resetMeasurementState.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void resetMeasurementState() {
        currentStep = 0;
        phiR1 = null; phiL1 = null; phiR2 = null; phiL2 = null;

        phiR1Label.setText("φ_прав (k=1): ---");
        phiL1Label.setText("φ_лів (k=1): ---");
        phiR2Label.setText("φ_прав (k=2): ---");
        phiL2Label.setText("φ_лів (k=2): ---");

        if (isPowerOn) {
            liveStepLabel.setText("Етап 1: Знайдіть ПРАВИЙ максимум k=1");
            liveStepLabel.setStyle("-fx-text-fill: yellow; -fx-font-weight: bold;");
        }
        updateButtonUI();
    }

    /*
     * Лабораторна робота № 5-6 "Дифракційна решітка".
     * Функція: updateButtonUI.
     * Призначення: Оновлює дані, статистику або графічний інтерфейс.
     */
    private void updateButtonUI() {
        if (!isPowerOn) {
            recordBtn.setDisable(true);
            recordBtn.setText("ОЧІКУВАННЯ");
            recordBtn.setStyle("-fx-background-color: #90a4ae; -fx-text-fill: white; -fx-font-weight: bold;");
            return;
        }

        recordBtn.setDisable(false);

        if (currentStep == 0) {
            recordBtn.setText("🔴 ЗАФІКСУВАТИ φ_прав (k=1)");
            recordBtn.setStyle("-fx-background-color: #c2185b; -fx-text-fill: white; -fx-font-weight: bold;");
        } else if (currentStep == 1) {
            recordBtn.setText("🔴 ЗАФІКСУВАТИ φ_лів (k=1)");
            recordBtn.setStyle("-fx-background-color: #e91e63; -fx-text-fill: white; -fx-font-weight: bold;");
        } else if (currentStep == 2) {
            recordBtn.setText("🔵 ЗАФІКСУВАТИ φ_прав (k=2)");
            recordBtn.setStyle("-fx-background-color: #0097a7; -fx-text-fill: white; -fx-font-weight: bold;");
        } else if (currentStep == 3) {
            recordBtn.setText("💾 ЗАФІКСУВАТИ φ_лів (k=2) ТА ОБЧИСЛИТИ");
            recordBtn.setStyle("-fx-background-color: #00bcd4; -fx-text-fill: black; -fx-font-weight: bold;");
        }
    }

    /*
     * Лабораторна робота № 5-6 "Дифракційна решітка".
     * Функція: handleRecord.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void handleRecord() {
        double angle = angleSlider.getValue();
        double trueWave = getCurrentWavelength();
        double d = getCurrentGratingD();
        double targetPhi1 = Math.toDegrees(Math.asin(1 * trueWave / d));
        double targetPhi2 = Math.toDegrees(Math.asin(2 * trueWave / d));

        double currentAbsAngle = Math.abs(angle);

        if (currentStep == 0) {
            if (angle < 0 || Math.abs(currentAbsAngle - targetPhi1) > 0.15) {
                showAlert("Увага", "Візир не на ПРАВОМУ максимумі 1-го порядку! Знайдіть чітку смугу справа від центру.");
                return;
            }
            phiR1 = angle;
            phiR1Label.setText(String.format(Locale.US, "φ_прав (k=1): %.2f°", phiR1));
            currentStep = 1;
            liveStepLabel.setText("Етап 2: Знайдіть ЛІВИЙ максимум k=1");
        } else if (currentStep == 1) {
            if (angle > 0 || Math.abs(currentAbsAngle - targetPhi1) > 0.15) {
                showAlert("Увага", "Візир не на ЛІВОМУ максимумі 1-го порядку! Перейдіть у від'ємну зону.");
                return;
            }
            phiL1 = angle;
            phiL1Label.setText(String.format(Locale.US, "φ_лів (k=1): %.2f°", phiL1));
            currentStep = 2;
            liveStepLabel.setText("Етап 3: Знайдіть ПРАВИЙ максимум k=2");
        } else if (currentStep == 2) {
            if (angle < 0 || Math.abs(currentAbsAngle - targetPhi2) > 0.15) {
                showAlert("Увага", "Візир не на ПРАВОМУ максимумі 2-го порядку! Знайдіть другу смугу справа.");
                return;
            }
            phiR2 = angle;
            phiR2Label.setText(String.format(Locale.US, "φ_прав (k=2): %.2f°", phiR2));
            currentStep = 3;
            liveStepLabel.setText("Етап 4: Знайдіть ЛІВИЙ максимум k=2");
        } else if (currentStep == 3) {
            if (angle > 0 || Math.abs(currentAbsAngle - targetPhi2) > 0.15) {
                showAlert("Увага", "Візир не на ЛІВОМУ максимумі 2-го порядку! Перейдіть у від'ємну зону.");
                return;
            }
            phiL2 = angle;
            phiL2Label.setText(String.format(Locale.US, "φ_лів (k=2): %.2f°", phiL2));

            executeMeasurement(trueWave, d);

            currentStep = 0;
            liveStepLabel.setText("ГОТОВО! Змініть налаштування для нового досліду.");
            liveStepLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
        }

        updateButtonUI();
    }

    /*
     * Лабораторна робота № 5-6 "Дифракційна решітка".
     * Функція: executeMeasurement.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void executeMeasurement(double trueWave, double d) {
        try {
            String filterName = filterComboBox.getSelectionModel().getSelectedItem().split(" ")[0];

            double calcPhi1 = Math.abs(phiR1 - phiL1) / 2.0;
            double calcPhi2 = Math.abs(phiR2 - phiL2) / 2.0;
            double lam1 = d * Math.sin(Math.toRadians(calcPhi1)) / 1.0;
            double lam2 = d * Math.sin(Math.toRadians(calcPhi2)) / 2.0;
            double lambdaExpNm = (lam1 + lam2) / 2.0;
            double error = Math.abs(lambdaExpNm - trueWave) / trueWave * 100.0;

            Measurement m = new Measurement(
                    idCounter++, filterName,
                    d / 1000.0,
                    Math.round(calcPhi1 * 100.0) / 100.0,
                    Math.round(calcPhi2 * 100.0) / 100.0,
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
     * Лабораторна робота № 5-6 "Дифракційна решітка".
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
     * Лабораторна робота № 5-6 "Дифракційна решітка".
     * Функція: processNextAuto.
     * Призначення: Обробляє поточний крок симуляції або дію користувача.
     */
    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            liveStepLabel.setText("АВТОПРОХОДЖЕННЯ ЗАВЕРШЕНО");
            return;
        }

        int mode = autoQueue.poll();
        filterComboBox.getSelectionModel().select(mode);
        applyPhysicsSettings();

        double trueWave = getCurrentWavelength();
        double d = getCurrentGratingD();

        double targetPhi1 = Math.toDegrees(Math.asin(1 * trueWave / d));
        double targetPhi2 = Math.toDegrees(Math.asin(2 * trueWave / d));

        autoTimer = new AnimationTimer() {
            long start = System.nanoTime();
            int phase = 0;

    /*
     * Лабораторна робота № 5-6 "Дифракційна решітка".
     * Функція: handle.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
            @Override
            public void handle(long now) {
                double elapsed = (now - start) / 1_000_000_000.0;

                if (phase == 0) {
                    angleSlider.setValue(targetPhi1);
                    if (elapsed > 0.6) { handleRecord(); phase = 1; }
                } else if (phase == 1 && elapsed > 1.2) {
                    angleSlider.setValue(-targetPhi1);
                    if (elapsed > 1.8) { handleRecord(); phase = 2; }
                } else if (phase == 2 && elapsed > 2.4) {
                    angleSlider.setValue(targetPhi2);
                    if (elapsed > 3.0) { handleRecord(); phase = 3; }
                } else if (phase == 3 && elapsed > 3.6) {
                    angleSlider.setValue(-targetPhi2);
                    if (elapsed > 4.2) {
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
     * Лабораторна робота № 5-6 "Дифракційна решітка".
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
            if (m.getErrorPercent() > maxError) {
                maxError = m.getErrorPercent();
            }
        }

        String conclusion = String.format(Locale.US,
                "ОБРОБКА РЕЗУЛЬТАТІВ:\n" +
                        "1. Довжина хвилі визначається за формулою дифракційної решітки: λ = (d · sin φ) / k.\n" +
                        "2. Усереднення результатів по кількох порядках (k=1, k=2) збільшує точність експерименту.\n" +
                        "3. Максимальна відносна похибка: ε_max = %.1f %%.\n" +
                        "ВИСНОВОК: Дифракційна решітка дозволяє з високою точністю вимірювати довжини хвиль різних кольорів.",
                maxError
        );

        finalResultLabel.setText(conclusion);
    }
}