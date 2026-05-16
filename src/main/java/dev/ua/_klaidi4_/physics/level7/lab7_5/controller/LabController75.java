/*
 * Лабораторна робота № 7-5 "Критичний стан".
 * Клас: LabController75.
 * Призначення: керує інтерфейсом лабораторної роботи, запуском досліду,
 * введенням параметрів, таблицею результатів та обробкою вимірювань.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level7.lab7_5.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level7.lab7_5.model.Measurement;
import dev.ua._klaidi4_.physics.level7.lab7_5.view.CriticalStateCanvas;
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

public class LabController75 extends BaseLabController {

    private CriticalStateCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> substanceBox;
    private Slider heatRateSlider;
    private Slider voltageSlider;
    private Label voltageLabel;
    private Button recordTk1Btn;
    private Button recordTk2Btn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveTempLabel;
    private Label liveStateLabel;
    private Label tk1SavedLabel;
    private Label tk2SavedLabel;
    private Double tk1 = null;
    private boolean isAutoRunning = false;
    private AnimationTimer autoTimer;
    private Queue<Integer> autoQueue = new LinkedList<>();

    private final double[][] SUBSTANCES = {
            {193.6, 35.5},
            {289.0, 48.3},
            {132.4, 111.3}
    };

    /*
     * Лабораторна робота № 7-5 "Критичний стан".
     * Функція: LabController75.
     * Призначення: Конструктор класу, ініціалізує інтерфейс та налаштування.
     */
    public LabController75() {
        initUI();
    }

    /*
     * Лабораторна робота № 7-5 "Критичний стан".
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
     * Лабораторна робота № 7-5 "Критичний стан".
     * Функція: initUI.
     * Призначення: Ініціалізує графічний інтерфейс користувача, створює панелі управління та графіки.
     */
    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(320);
        leftPanel.setMinWidth(320);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 7-5)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane advancedPane = new TitledPane();
        advancedPane.setText("Розширені налаштування");
        advancedPane.setExpanded(false);

        VBox advancedBox = new VBox(10);
        advancedBox.setPadding(new Insets(5));

        substanceBox = new ComboBox<>(FXCollections.observableArrayList(
                "Діетиловий ефір (Tк=193.6°C)",
                "Бензол (Tк=289.0°C)",
                "Аміак (Tк=132.4°C)"
        ));
        substanceBox.getSelectionModel().selectFirst();
        substanceBox.setMaxWidth(Double.MAX_VALUE);
        substanceBox.setOnAction(e -> applyPhysicsParams());

        Label heatRateLabel = new Label("Швидкість нагрівання: 1.0x");
        heatRateSlider = new Slider(0.5, 3.0, 1.0);
        heatRateSlider.setShowTickMarks(true);
        heatRateSlider.setMajorTickUnit(0.5);
        heatRateSlider.valueProperty().addListener((o, ov, nv) -> {
            heatRateLabel.setText(String.format(Locale.US, "Швидкість нагрівання: %.1fx", nv.doubleValue()));
            applyPhysicsParams();
        });

        advancedBox.getChildren().addAll(
                createInputGroup("Досліджувана речовина:", substanceBox),
                heatRateLabel, heatRateSlider
        );
        advancedPane.setContent(advancedBox);

        TitledPane configPane = new TitledPane();
        configPane.setText("Керування ЛАТР");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(12);
        configBox.setPadding(new Insets(5));

        voltageLabel = new Label("Напруга ЛАТР: 0 В");
        voltageSlider = new Slider(0.0, 220.0, 0.0);
        voltageSlider.setShowTickMarks(true);
        voltageSlider.setMajorTickUnit(20.0);
        voltageSlider.valueProperty().addListener((o, ov, nv) -> {
            voltageLabel.setText(String.format(Locale.US, "Напруга ЛАТР: %.0f В", nv.doubleValue()));
            applyPhysicsParams();
        });

        configBox.getChildren().addAll(voltageLabel, voltageSlider);
        configPane.setContent(configBox);

        recordTk1Btn = new Button("🔴 ЗАФІКСУВАТИ ЗНИКНЕННЯ (Tк1)");
        recordTk1Btn.setStyle("-fx-background-color: #ff007f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        recordTk1Btn.setMaxWidth(Double.MAX_VALUE);
        recordTk1Btn.setOnAction(e -> handleRecordTk1());

        recordTk2Btn = new Button("🔵 ЗАФІКСУВАТИ ПОЯВУ (Tк2)");
        recordTk2Btn.setStyle("-fx-background-color: #00e5ff; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-padding: 8;");
        recordTk2Btn.setMaxWidth(Double.MAX_VALUE);
        recordTk2Btn.setDisable(true);
        recordTk2Btn.setOnAction(e -> handleRecordTk2());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ ЛАБИ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAutoMode());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear(); idCounter = 1; updateStats();
            resetMeasurementState();
        });

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, advancedPane, configPane, recordTk1Btn, recordTk2Btn, autoBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new CriticalStateCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(4);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-text-fill: #00ffcc; -fx-padding: 10; -fx-border-color: #00ffcc; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(260, 140);

        Label dashTitle = new Label("ДАТЧИК ТЕМПЕРАТУРИ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        liveTempLabel = new Label("T = 20.0 °C");
        liveTempLabel.setStyle("-fx-text-fill: #ff007f; -fx-font-size: 16px; -fx-font-weight: bold;");

        liveStateLabel = new Label("Фаза: Рідина + Пара");
        liveStateLabel.setStyle("-fx-text-fill: #a3e635; -fx-font-weight: bold;");

        tk1SavedLabel = new Label("Tк1 (нагрів): ---");
        tk1SavedLabel.setStyle("-fx-text-fill: #ff007f;");

        tk2SavedLabel = new Label("Tк2 (охолодж): ---");
        tk2SavedLabel.setStyle("-fx-text-fill: #00e5ff;");

        dash.getChildren().addAll(dashTitle, liveTempLabel, liveStateLabel, new Separator(), tk1SavedLabel, tk2SavedLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, Double> tk1Col = new TableColumn<>("Tк1 (°C)");
        tk1Col.setCellValueFactory(new PropertyValueFactory<>("tk1"));
        TableColumn<Measurement, Double> tk2Col = new TableColumn<>("Tк2 (°C)");
        tk2Col.setCellValueFactory(new PropertyValueFactory<>("tk2"));
        TableColumn<Measurement, Double> tkAvgCol = new TableColumn<>("Tк сер (°C)");
        tkAvgCol.setCellValueFactory(new PropertyValueFactory<>("tkAvg"));
        TableColumn<Measurement, Double> aCol = new TableColumn<>("a (м⁶Па/моль²)");
        aCol.setCellValueFactory(new PropertyValueFactory<>("a"));
        TableColumn<Measurement, Double> bCol = new TableColumn<>("b (м³/моль)");
        bCol.setCellValueFactory(new PropertyValueFactory<>("b"));

        table.getColumns().addAll(idCol, tk1Col, tk2Col, tkAvgCol, aCol, bCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(140);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        AnimationTimer uiTimer = new AnimationTimer() {
    /*
     * Лабораторна робота № 7-5 "Критичний стан".
     * Функція: handle.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
            @Override
            public void handle(long now) {
                double t = canvas.getCurrentTemp();
                liveTempLabel.setText(String.format(Locale.US, "T = %.1f °C", t));

                double actualTk = SUBSTANCES[substanceBox.getSelectionModel().getSelectedIndex()][0];

                if (t >= actualTk) {
                    liveStateLabel.setText("Фаза: Надкритичний флюїд");
                    liveStateLabel.setStyle("-fx-text-fill: #ffeb3b; -fx-font-weight: bold;");
                } else if (Math.abs(t - actualTk) < 2.0) {
                    liveStateLabel.setText("Фаза: Критична опалесценція");
                    liveStateLabel.setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
                } else {
                    liveStateLabel.setText("Фаза: Рідина + Пара");
                    liveStateLabel.setStyle("-fx-text-fill: #a3e635; -fx-font-weight: bold;");
                }
            }
        };
        uiTimer.start();

        applyPhysicsParams();
        updateStats();
    }

    /*
     * Лабораторна робота № 7-5 "Критичний стан".
     * Функція: applyPhysicsParams.
     * Призначення: Застосовує поточні налаштування до симуляції.
     */
    private void applyPhysicsParams() {
        if (isAutoRunning) return;
        int subIdx = substanceBox.getSelectionModel().getSelectedIndex();
        double tk = SUBSTANCES[subIdx][0];
        double voltage = voltageSlider.getValue();
        double heatRate = heatRateSlider.getValue();

        canvas.setPhysicsParams(voltage, tk, heatRate);
    }

    /*
     * Лабораторна робота № 7-5 "Критичний стан".
     * Функція: resetMeasurementState.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void resetMeasurementState() {
        tk1 = null;
        tk1SavedLabel.setText("Tк1 (нагрів): ---");
        tk2SavedLabel.setText("Tк2 (охолодж): ---");
        recordTk1Btn.setDisable(false);
        recordTk2Btn.setDisable(true);
    }

    /*
     * Лабораторна робота № 7-5 "Критичний стан".
     * Функція: handleRecordTk1.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void handleRecordTk1() {
        double currentT = canvas.getCurrentTemp();
        double actualTk = SUBSTANCES[substanceBox.getSelectionModel().getSelectedIndex()][0];

        if (currentT < actualTk - 1.0) {
            showAlert("Увага", String.format(Locale.US, "Меніск ще не зник! Дочекайтесь критичної температури (~%.1f °C).", actualTk));
            return;
        }
        tk1 = Math.round(currentT * 10.0) / 10.0;
        tk1SavedLabel.setText(String.format(Locale.US, "Tк1 (нагрів): %.1f °C", tk1));

        recordTk1Btn.setDisable(true);
        recordTk2Btn.setDisable(false);
        voltageSlider.setValue(0);
    }

    /*
     * Лабораторна робота № 7-5 "Критичний стан".
     * Функція: handleRecordTk2.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void handleRecordTk2() {
        double currentT = canvas.getCurrentTemp();
        double actualTk = SUBSTANCES[substanceBox.getSelectionModel().getSelectedIndex()][0];

        if (currentT > actualTk + 1.0) {
            showAlert("Увага", "Охолодіть ампулу! Меніск з'явиться при температурі нижче критичної.");
            return;
        }
        double tk2 = Math.round(currentT * 10.0) / 10.0;
        tk2SavedLabel.setText(String.format(Locale.US, "Tк2 (охолодж): %.1f °C", tk2));

        calculateAndSave(tk1, tk2);
        resetMeasurementState();
    }

    /*
     * Лабораторна робота № 7-5 "Критичний стан".
     * Функція: calculateAndSave.
     * Призначення: Виконує математичні обчислення для отримання результатів.
     */
    private void calculateAndSave(double t1, double t2) {
        int subIdx = substanceBox.getSelectionModel().getSelectedIndex();
        double pkAtm = SUBSTANCES[subIdx][1];
        double tAvg = (t1 + t2) / 2.0;
        double tKelvin = tAvg + 273.15;
        double pk = pkAtm * 101325;
        double R = 8.314;
        double a = (27.0 * R * R * tKelvin * tKelvin) / (64.0 * pk);
        double b = (R * tKelvin) / (8.0 * pk);

        Measurement m = new Measurement(
                idCounter++,
                t1, t2, tAvg,
                Math.round(a * 1000.0) / 1000.0,
                Math.round(b * 1000000.0) / 1000000.0
        );
        data.add(m);
        updateStats();
    }

    /*
     * Лабораторна робота № 7-5 "Критичний стан".
     * Функція: startAutoMode.
     * Призначення: Запускає процес симуляції або відповідний режим роботи.
     */
    private void startAutoMode() {
        isAutoRunning = true;
        voltageSlider.setDisable(true);
        substanceBox.setDisable(true);
        recordTk1Btn.setDisable(true);
        recordTk2Btn.setDisable(true);
        clearBtn.setDisable(true);
        autoBtn.setDisable(true);

        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        for (int i = 0; i < 3; i++) autoQueue.add(i);

        processNextAuto();
    }

    /*
     * Лабораторна робота № 7-5 "Критичний стан".
     * Функція: processNextAuto.
     * Призначення: Обробляє поточний крок симуляції або дію користувача.
     */
    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoRunning = false;
            voltageSlider.setDisable(false);
            substanceBox.setDisable(false);
            recordTk1Btn.setDisable(false);
            clearBtn.setDisable(false);
            autoBtn.setDisable(false);
            return;
        }
        autoQueue.poll();
        resetMeasurementState();

        double actualTk = SUBSTANCES[substanceBox.getSelectionModel().getSelectedIndex()][0];
        double simTk1 = actualTk + (Math.random() * 0.5);
        double simTk2 = actualTk - (Math.random() * 0.5);

        autoTimer = new AnimationTimer() {
            int state = 0;
            long delayStart = 0;

    /*
     * Лабораторна робота № 7-5 "Критичний стан".
     * Функція: handle.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
            @Override
            public void handle(long now) {
                double t = canvas.getCurrentTemp();

                if (state == 0) {
                    canvas.setPhysicsParams(220, actualTk, heatRateSlider.getValue());
                    if (t >= simTk1) {
                        tk1 = Math.round(t * 10.0) / 10.0;
                        Platform.runLater(() -> tk1SavedLabel.setText(String.format(Locale.US, "Tк1 (нагрів): %.1f °C", tk1)));
                        state = 1;
                        delayStart = System.currentTimeMillis();
                    }
                } else if (state == 1) {
                    canvas.setPhysicsParams(0, actualTk, heatRateSlider.getValue());
                    if (System.currentTimeMillis() - delayStart > 1000) state = 2;
                } else if (state == 2) {
                    if (t <= simTk2) {
                        double finalTk2 = Math.round(t * 10.0) / 10.0;
                        Platform.runLater(() -> {
                            tk2SavedLabel.setText(String.format(Locale.US, "Tк2 (охолодж): %.1f °C", finalTk2));
                            calculateAndSave(tk1, finalTk2);
                        });
                        state = 3;
                        delayStart = System.currentTimeMillis();
                    }
                } else if (state == 3) {
                    if (System.currentTimeMillis() - delayStart > 1500) {
                        this.stop();
                        processNextAuto();
                    }
                }
            }
        };
        autoTimer.start();
    }

    /*
     * Лабораторна робота № 7-5 "Критичний стан".
     * Функція: updateStats.
     * Призначення: Розраховує середні значення та похибки на основі даних у таблиці.
     */
    private void updateStats() {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: Очікування вимірювань...");
            return;
        }
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }

        double sumA = 0, sumB = 0, sumTk = 0;
        for (Measurement m : data) {
            sumA += m.getA();
            sumB += m.getB();
            sumTk += m.getTkAvg();
        }
        int n = data.size();
        String subName = substanceBox.getValue().split(" ")[0];

        String conclusion = String.format(Locale.US,
                "ОБРОБКА РЕЗУЛЬТАТІВ (%s):\n" +
                        "1. Критична температура T_k = %.1f °C (%.1f K).\n" +
                        "2. Розраховано поправку Ван-дер-Ваальса на взаємодію a = %.3f м⁶·Па/моль².\n" +
                        "3. Розраховано поправку Ван-дер-Ваальса на власний об'єм b = %.6f м³/моль.\n" +
                        "ВИСНОВОК: Значення поправок залежать від критичних параметрів обраної речовини.",
                subName, (sumTk / n), (sumTk / n) + 273.15, (sumA / n), (sumB / n)
        );

        finalResultLabel.setText(conclusion);
    }
}