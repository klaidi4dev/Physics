/*
 * Лабораторна робота № 5-5 "Кільця Ньютона".
 * Клас: LabController55.
 * Призначення: керує інтерфейсом лабораторної роботи, запуском досліду,
 * введенням параметрів, таблицею результатів та обробкою вимірювань.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level5.lab5_5.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level5.lab5_5.model.Measurement;
import dev.ua._klaidi4_.physics.level5.lab5_5.view.NewtonRingsCanvas;
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

public class LabController55 extends BaseLabController {

    private NewtonRingsCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> filterComboBox;
    private ComboBox<String> lensComboBox;
    private Slider microSlider;
    private Slider zoomSlider;
    private Label microValueLabel;
    private Label zoomValueLabel;
    private Button togglePowerBtn;
    private Button recordBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStepLabel;
    private Label zL5Label, zR5Label, zL11Label, zR11Label;

    private boolean isPowerOn = false;
    private int currentStep = 0;

    private Double zL5 = null, zR5 = null, zL11 = null, zR11 = null;

    private AnimationTimer autoTimer;
    private Queue<Integer> autoQueue = new LinkedList<>();

    /*
     * Лабораторна робота № 5-5 "Кільця Ньютона".
     * Функція: LabController55.
     * Призначення: Конструктор класу, ініціалізує інтерфейс та налаштування.
     */
    public LabController55() {
        initUI();
        applyPhysicsSettings();
    }

    /*
     * Лабораторна робота № 5-5 "Кільця Ньютона".
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
     * Лабораторна робота № 5-5 "Кільця Ньютона".
     * Функція: initUI.
     * Призначення: Ініціалізує графічний інтерфейс користувача, створює панелі управління та графіки.
     */
    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 5-5)");
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

        lensComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "Лінза 1 (R = 800 мм)",
                "Лінза 2 (R = 1000 мм)",
                "Лінза 3 (R = 1200 мм)"
        ));
        lensComboBox.getSelectionModel().select(1);
        lensComboBox.setMaxWidth(Double.MAX_VALUE);
        lensComboBox.setOnAction(e -> {
            resetMeasurementState();
            applyPhysicsSettings();
        });

        microValueLabel = new Label("Мікрометр z: 0.00 мм");

        microSlider = new Slider(-3.5, 3.5, 0.0);
        microSlider.setShowTickMarks(true);
        microSlider.setMajorTickUnit(1.0);
        microSlider.valueProperty().addListener((o, ov, nv) -> {
            microValueLabel.setText(String.format(Locale.US, "Мікрометр z: %.2f мм", nv.doubleValue()));
            applyPhysicsSettings();
        });

        zoomValueLabel = new Label("Збільшення (Zoom): 1.0x");

        zoomSlider = new Slider(1.0, 5.0, 1.0);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setMajorTickUnit(1.0);
        zoomSlider.valueProperty().addListener((o, ov, nv) -> {
            zoomValueLabel.setText(String.format(Locale.US, "Збільшення (Zoom): %.1fx", nv.doubleValue()));
            applyPhysicsSettings();
        });

        configBox.getChildren().addAll(
                createInputGroup("Світлофільтр K:", filterComboBox),
                createInputGroup("Лінза L:", lensComboBox),
                microValueLabel, microSlider,
                zoomValueLabel, zoomSlider
        );

        ScrollPane scrollParams = new ScrollPane(configBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(250);
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

        canvas = new NewtonRingsCanvas(600, 440);

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

        Label dashTitle = new Label("ДАНІ ВИМІРЮВАННЯ ДІАМЕТРА");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        liveStepLabel = new Label("Крок: Увімкніть живлення");
        liveStepLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");

        zL5Label = new Label("z (Лів) Кільце 5: ---"); zL5Label.setStyle("-fx-text-fill: #ff007f;");
        zR5Label = new Label("z (Прав) Кільце 5: ---"); zR5Label.setStyle("-fx-text-fill: #ff007f;");
        zL11Label = new Label("z (Лів) Кільце 11: ---"); zL11Label.setStyle("-fx-text-fill: #00ffcc;");
        zR11Label = new Label("z (Прав) Кільце 11: ---"); zR11Label.setStyle("-fx-text-fill: #00ffcc;");

        dash.getChildren().addAll(dashTitle, liveStepLabel, zL5Label, zR5Label, zL11Label, zR11Label);

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
        TableColumn<Measurement, Double> rLensCol = new TableColumn<>("R (мм)");
        rLensCol.setCellValueFactory(new PropertyValueFactory<>("rCurvature"));
        TableColumn<Measurement, Double> r5Col = new TableColumn<>("r5 (мм)");
        r5Col.setCellValueFactory(new PropertyValueFactory<>("r5"));
        TableColumn<Measurement, Double> r11Col = new TableColumn<>("r11 (мм)");
        r11Col.setCellValueFactory(new PropertyValueFactory<>("r11"));
        TableColumn<Measurement, Double> lamCol = new TableColumn<>("λ (нм)");
        lamCol.setCellValueFactory(new PropertyValueFactory<>("lambdaExp"));
        TableColumn<Measurement, Double> errCol = new TableColumn<>("ε (%)");
        errCol.setCellValueFactory(new PropertyValueFactory<>("errorPercent"));

        table.getColumns().addAll(idCol, fCol, rLensCol, r5Col, r11Col, lamCol, errCol);
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
     * Лабораторна робота № 5-5 "Кільця Ньютона".
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
     * Лабораторна робота № 5-5 "Кільця Ньютона".
     * Функція: getCurrentWavelength.
     * Призначення: Повертає необхідне значення параметра або об'єкта.
     */
    private double getCurrentWavelength() {
        int index = filterComboBox.getSelectionModel().getSelectedIndex();
        if (index == 0) return 650.0;
        if (index == 1) return 532.0;
        return 470.0;
    }

    /*
     * Лабораторна робота № 5-5 "Кільця Ньютона".
     * Функція: getCurrentRadius.
     * Призначення: Повертає необхідне значення параметра або об'єкта.
     */
    private double getCurrentRadius() {
        int index = lensComboBox.getSelectionModel().getSelectedIndex();
        if (index == 0) return 800.0;
        if (index == 1) return 1000.0;
        return 1200.0;
    }

    /*
     * Лабораторна робота № 5-5 "Кільця Ньютона".
     * Функція: applyPhysicsSettings.
     * Призначення: Застосовує поточні налаштування до симуляції.
     */
    private void applyPhysicsSettings() {
        canvas.setPhysicsParameters(isPowerOn, microSlider.getValue(), getCurrentWavelength(), getCurrentRadius(), zoomSlider.getValue());
    }

    /*
     * Лабораторна робота № 5-5 "Кільця Ньютона".
     * Функція: resetMeasurementState.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void resetMeasurementState() {
        currentStep = 0;
        zL5 = null;
        zR5 = null;
        zL11 = null;
        zR11 = null;

        zL5Label.setText("z (Лів) Кільце 5: ---");
        zR5Label.setText("z (Прав) Кільце 5: ---");
        zL11Label.setText("z (Лів) Кільце 11: ---");
        zR11Label.setText("z (Прав) Кільце 11: ---");

        if (isPowerOn) {
            liveStepLabel.setText("Етап 1: Знайдіть ЛІВУ частину 5-го кільця");
            liveStepLabel.setStyle("-fx-text-fill: yellow; -fx-font-weight: bold;");
        }
        updateButtonUI();
    }

    /*
     * Лабораторна робота № 5-5 "Кільця Ньютона".
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
            recordBtn.setText("🔴 ЗАФІКСУВАТИ z_ліве (КІЛЬЦЕ 5)");
            recordBtn.setStyle("-fx-background-color: #c2185b; -fx-text-fill: white; -fx-font-weight: bold;");
        } else if (currentStep == 1) {
            recordBtn.setText("🔴 ЗАФІКСУВАТИ z_праве (КІЛЬЦЕ 5)");
            recordBtn.setStyle("-fx-background-color: #e91e63; -fx-text-fill: white; -fx-font-weight: bold;");
        } else if (currentStep == 2) {
            recordBtn.setText("🔵 ЗАФІКСУВАТИ z_ліве (КІЛЬЦЕ 11)");
            recordBtn.setStyle("-fx-background-color: #0097a7; -fx-text-fill: white; -fx-font-weight: bold;");
        } else if (currentStep == 3) {
            recordBtn.setText("💾 ЗАФІКСУВАТИ z_праве (КІЛЬЦЕ 11) ТА ОБЧИСЛИТИ");
            recordBtn.setStyle("-fx-background-color: #00bcd4; -fx-text-fill: black; -fx-font-weight: bold;");
        }
    }

    /*
     * Лабораторна робота № 5-5 "Кільця Ньютона".
     * Функція: handleRecord.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void handleRecord() {
        double z = microSlider.getValue();
        double trueWave = getCurrentWavelength() / 1_000_000.0;
        double targetR5 = Math.sqrt(5 * trueWave * getCurrentRadius());
        double targetR11 = Math.sqrt(11 * trueWave * getCurrentRadius());

        if (currentStep == 0) {
            if (z > 0 || Math.abs(Math.abs(z) - targetR5) > 0.15) {
                showAlert("Увага", "Ви знаходитесь не на лівій частині 5-го темного кільця!");
                return;
            }
            zL5 = z;
            zL5Label.setText(String.format(Locale.US, "z (Лів) Кільце 5: %.3f мм", zL5));
            currentStep = 1;
            liveStepLabel.setText("Етап 2: Знайдіть ПРАВУ частину 5-го кільця");
        } else if (currentStep == 1) {
            if (z < 0 || Math.abs(z - targetR5) > 0.15) {
                showAlert("Увага", "Ви знаходитесь не на правій частині 5-го темного кільця!");
                return;
            }
            zR5 = z;
            zR5Label.setText(String.format(Locale.US, "z (Прав) Кільце 5: %.3f мм", zR5));
            currentStep = 2;
            liveStepLabel.setText("Етап 3: Знайдіть ЛІВУ частину 11-го кільця");
        } else if (currentStep == 2) {
            if (z > 0 || Math.abs(Math.abs(z) - targetR11) > 0.15) {
                showAlert("Увага", "Ви знаходитесь не на лівій частині 11-го темного кільця!");
                return;
            }
            zL11 = z;
            zL11Label.setText(String.format(Locale.US, "z (Лів) Кільце 11: %.3f мм", zL11));
            currentStep = 3;
            liveStepLabel.setText("Етап 4: Знайдіть ПРАВУ частину 11-го кільця");
        } else if (currentStep == 3) {
            if (z < 0 || Math.abs(z - targetR11) > 0.15) {
                showAlert("Увага", "Ви знаходитесь не на правій частині 11-го темного кільця!");
                return;
            }
            zR11 = z;
            zR11Label.setText(String.format(Locale.US, "z (Прав) Кільце 11: %.3f мм", zR11));

            executeMeasurement();

            currentStep = 0;
            liveStepLabel.setText("ГОТОВО! Змініть налаштування для нового досліду.");
            liveStepLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
        }

        updateButtonUI();
    }

    /*
     * Лабораторна робота № 5-5 "Кільця Ньютона".
     * Функція: executeMeasurement.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void executeMeasurement() {
        try {
            double trueWave = getCurrentWavelength();
            double rLens = getCurrentRadius();
            String filterName = filterComboBox.getSelectionModel().getSelectedItem().split(" ")[0];

            double calcR5 = Math.abs(zR5 - zL5) / 2.0;
            double calcR11 = Math.abs(zR11 - zL11) / 2.0;

            double lambdaExpMm = (calcR11 * calcR11 - calcR5 * calcR5) / (rLens * 6.0);
            double lambdaExpNm = lambdaExpMm * 1_000_000.0;

            double error = Math.abs(lambdaExpNm - trueWave) / trueWave * 100.0;

            Measurement m = new Measurement(
                    idCounter++, filterName,
                    rLens,
                    Math.round(calcR5 * 1000.0) / 1000.0,
                    Math.round(calcR11 * 1000.0) / 1000.0,
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
     * Лабораторна робота № 5-5 "Кільця Ньютона".
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
     * Лабораторна робота № 5-5 "Кільця Ньютона".
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

        double trueWave = getCurrentWavelength() / 1_000_000.0;
        double rLens = getCurrentRadius();

        double targetR5 = Math.sqrt(5 * trueWave * rLens);
        double targetR11 = Math.sqrt(11 * trueWave * rLens);

        autoTimer = new AnimationTimer() {
            long start = System.nanoTime();
            int phase = 0;

    /*
     * Лабораторна робота № 5-5 "Кільця Ньютона".
     * Функція: handle.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
            @Override
            public void handle(long now) {
                double elapsed = (now - start) / 1_000_000_000.0;

                if (phase == 0) {
                    microSlider.setValue(-targetR5);
                    if (elapsed > 0.5) {
                        handleRecord();
                        phase = 1;
                    }
                } else if (phase == 1 && elapsed > 1.0) {
                    microSlider.setValue(targetR5);
                    if (elapsed > 1.5) {
                        handleRecord();
                        phase = 2;
                    }
                } else if (phase == 2 && elapsed > 2.0) {
                    microSlider.setValue(-targetR11);
                    if (elapsed > 2.5) {
                        handleRecord();
                        phase = 3;
                    }
                } else if (phase == 3 && elapsed > 3.0) {
                    microSlider.setValue(targetR11);
                    if (elapsed > 3.5) {
                        this.stop();
                        handleRecord();
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                                Platform.runLater(() -> processNextAuto());
                            } catch (InterruptedException ignored) {}
                        }).start();
                    }
                }
            }
        };
        autoTimer.start();
    }

    /*
     * Лабораторна робота № 5-5 "Кільця Ньютона".
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
                        "1. Довжина хвилі визначається за формулою: λ = (r_m² - r_k²) / (R · (m - k)).\n" +
                        "2. Радіуси кілець розраховано через вимірювання їхніх діаметрів: r = |z_прав - z_лів| / 2.\n" +
                        "3. Максимальна відносна похибка: ε_max = %.1f %%.\n" +
                        "ВИСНОВОК: Двоетапне вимірювання країв кілець дозволяє значно підвищити точність визначення довжини хвилі.",
                maxError
        );

        finalResultLabel.setText(conclusion);
    }
}