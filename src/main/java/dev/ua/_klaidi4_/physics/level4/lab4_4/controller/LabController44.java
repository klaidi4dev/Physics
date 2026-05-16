/*
 * Лабораторна робота № 4-4 "Згасаючі коливання".
 * Клас: LabController44.
 * Призначення: керує інтерфейсом лабораторної роботи, запуском досліду,
 * введенням параметрів, таблицею результатів та обробкою вимірювань.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level4.lab4_4.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level4.lab4_4.model.Measurement;
import dev.ua._klaidi4_.physics.level4.lab4_4.view.OscilloscopeCanvas;
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

public class LabController44 extends BaseLabController {

    private OscilloscopeCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField cField;
    private TextField lField;
    private TextField rInternalField;
    private TextField rKrField;
    private Slider rdSlider;
    private Label rdValueLabel;
    private ComboBox<String> timeScaleCombo;
    private Label liveStatusLabel;
    private Label liveTimeLabel;
    private Label rKrDashLabel;
    private Button measureBtn;
    private Button autoBtn;
    private Button clearBtn;
    private AnimationTimer measurementTimer;
    private long startTime;
    private final double targetTime = 2.0;

    private Queue<Double> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    /*
     * Лабораторна робота № 4-4 "Згасаючі коливання".
     * Функція: LabController44.
     * Призначення: Конструктор класу, ініціалізує інтерфейс та налаштування.
     */
    public LabController44() {
        initUI();
        applyPhysicsSettings();
    }

    /*
     * Лабораторна робота № 4-4 "Згасаючі коливання".
     * Функція: shutdown.
     * Призначення: Зупиняє процеси та очищує ресурси при закритті вікна.
     */
    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        if (measurementTimer != null) measurementTimer.stop();
        isAutoRunning = false;
        autoQueue.clear();
    }

    /*
     * Лабораторна робота № 4-4 "Згасаючі коливання".
     * Функція: initUI.
     * Призначення: Ініціалізує графічний інтерфейс користувача, створює панелі управління та графіки.
     */
    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 4-4)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane configPane = new TitledPane();
        configPane.setText("Параметри контуру");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(12);
        configBox.setPadding(new Insets(5));

        cField = new TextField("0.5");
        lField = new TextField("50");
        rInternalField = new TextField("10");
        rKrField = new TextField("");

        rdSlider = new Slider(0, 1000, 0);
        rdSlider.setShowTickMarks(true);
        rdSlider.setMajorTickUnit(200);
        rdValueLabel = new Label("R_дод = 0 Ом");

        timeScaleCombo = new ComboBox<>(FXCollections.observableArrayList(
                "0.1 мс/под", "0.2 мс/под", "0.5 мс/под", "1.0 мс/под", "2.0 мс/под"
        ));
        timeScaleCombo.getSelectionModel().select(3);

        cField.textProperty().addListener((o, ov, nv) -> { if (cField.isFocused()) applyPhysicsSettings(); });
        lField.textProperty().addListener((o, ov, nv) -> { if (lField.isFocused()) applyPhysicsSettings(); });
        rInternalField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        timeScaleCombo.setOnAction(e -> applyPhysicsSettings());

        rKrField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                try {
                    double rKr = Double.parseDouble(rKrField.getText());
                    double lHenries = Double.parseDouble(lField.getText()) * 1e-3;
                    if (rKr > 0) {
                        double cFarads = (4 * lHenries) / (rKr * rKr);
                        cField.setText(String.format(Locale.US, "%.3f", cFarads * 1e6));
                        applyPhysicsSettings();
                    }
                } catch (NumberFormatException ignored) {}
            }
        });

        rdSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            rdValueLabel.setText(String.format("R_дод = %.0f Ом", newVal.doubleValue()));
            applyPhysicsSettings();
        });

        configBox.getChildren().addAll(
                createInputGroup("Ємність C (мкФ):", cField),
                createInputGroup("Індуктивність L (мГн):", lField),
                createInputGroup("Власний опір котушки R (Ом):", rInternalField),
                createInputGroup("Критичний опір Rкр (Ом):", rKrField),
                new Label("Магазин опорів (додатковий Rд):"), rdValueLabel, rdSlider,
                createInputGroup("Розгортка осцилографа (t0):", timeScaleCombo)
        );

        ScrollPane scrollParams = new ScrollPane(configBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(320);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        configPane.setContent(scrollParams);

        measureBtn = new Button("⚡ ВИМІРЯТИ ІМПУЛЬС");
        measureBtn.setStyle("-fx-background-color: #A155FF; -fx-text-fill: white; -fx-font-weight: bold;");
        measureBtn.setMaxWidth(Double.MAX_VALUE);
        measureBtn.setOnAction(e -> startManual());

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
            liveTimeLabel.setText("t = 0.00 с");
        });

        leftPanel.getChildren().addAll(title, configPane, measureBtn, autoBtn, clearBtn);

        canvas = new OscilloscopeCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-text-fill: #A155FF; -fx-padding: 10; -fx-border-color: #A155FF; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(220, 80);
        Label dashTitle = new Label("СЕНСОРНА ПАНЕЛЬ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveTimeLabel = new Label("t = 0.00 с");
        liveTimeLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        liveTimeLabel.setStyle("-fx-text-fill: #00ff00;");
        rKrDashLabel = new Label("Rкр = 0.0 Ом");
        rKrDashLabel.setStyle("-fx-text-fill: #00ffcc; -fx-font-weight: bold;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveTimeLabel, rKrDashLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, Double> rdCol = new TableColumn<>("Rд (Ом)");
        rdCol.setCellValueFactory(new PropertyValueFactory<>("rd"));
        TableColumn<Measurement, Integer> nCol = new TableColumn<>("n");
        nCol.setCellValueFactory(new PropertyValueFactory<>("n"));
        TableColumn<Measurement, Double> ndCol = new TableColumn<>("N (под)");
        ndCol.setCellValueFactory(new PropertyValueFactory<>("nDivs"));
        TableColumn<Measurement, Double> u1Col = new TableColumn<>("U_m (В)");
        u1Col.setCellValueFactory(new PropertyValueFactory<>("um"));
        TableColumn<Measurement, Double> u2Col = new TableColumn<>("U_m+1 (В)");
        u2Col.setCellValueFactory(new PropertyValueFactory<>("umNext"));
        TableColumn<Measurement, Double> tCol = new TableColumn<>("T (с)");
        tCol.setCellValueFactory(new PropertyValueFactory<>("period"));
        TableColumn<Measurement, Double> dCol = new TableColumn<>("δ");
        dCol.setCellValueFactory(new PropertyValueFactory<>("decrement"));

        table.getColumns().addAll(idCol, rdCol, nCol, ndCol, u1Col, u2Col, tCol, dCol);
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
     * Лабораторна робота № 4-4 "Згасаючі коливання".
     * Функція: applyPhysicsSettings.
     * Призначення: Застосовує поточні налаштування до симуляції.
     */
    private void applyPhysicsSettings() {
        try {
            double cFarads = Double.parseDouble(cField.getText()) * 1e-6;
            double lHenries = Double.parseDouble(lField.getText()) * 1e-3;
            double rTotal = Double.parseDouble(rInternalField.getText()) + rdSlider.getValue();

            String scaleStr = timeScaleCombo.getSelectionModel().getSelectedItem();
            double scaleSeconds = Double.parseDouble(scaleStr.split(" ")[0]) * 1e-3;

            double rKr = 2 * Math.sqrt(lHenries / cFarads);
            rKrDashLabel.setText(String.format(Locale.US, "Rкр = %.1f Ом", rKr));

            if (!rKrField.isFocused()) {
                rKrField.setText(String.format(Locale.US, "%.1f", rKr));
            }

            canvas.setSetupParameters(lHenries, cFarads, rTotal, scaleSeconds);
        } catch (NumberFormatException ignored) {}
    }

    /*
     * Лабораторна робота № 4-4 "Згасаючі коливання".
     * Функція: setControlsDisable.
     * Призначення: Встановлює нове значення для вказаного параметра.
     */
    private void setControlsDisable(boolean disable) {
        measureBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        rdSlider.setDisable(disable);
        cField.setDisable(disable);
        lField.setDisable(disable);
        rInternalField.setDisable(disable);
        rKrField.setDisable(disable);
        timeScaleCombo.setDisable(disable);
    }

    /*
     * Лабораторна робота № 4-4 "Згасаючі коливання".
     * Функція: startManual.
     * Призначення: Запускає процес симуляції або відповідний режим роботи.
     */
    private void startManual() {
        isAutoRunning = false;
        runSimulation(rdSlider.getValue());
    }

    /*
     * Лабораторна робота № 4-4 "Згасаючі коливання".
     * Функція: startAuto.
     * Призначення: Запускає процес симуляції або відповідний режим роботи.
     */
    private void startAuto() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        try {
            double rKr = Double.parseDouble(rKrField.getText());
            autoQueue.add(0.0);
            autoQueue.add((double) Math.round(rKr * 0.2));
            autoQueue.add((double) Math.round(rKr * 0.5));
            autoQueue.add((double) Math.round(rKr * 1.1));

            isAutoRunning = true;
            processNextAuto();
        } catch (Exception e) {
            showAlert("Помилка", "Перевірте правильність параметрів.");
        }
    }

    /*
     * Лабораторна робота № 4-4 "Згасаючі коливання".
     * Функція: processNextAuto.
     * Призначення: Обробляє поточний крок симуляції або дію користувача.
     */
    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoRunning = false;
            liveStatusLabel.setText("СИСТЕМА: ГОТОВО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            setControlsDisable(false);
            return;
        }

        double nextVal = autoQueue.poll();
        rdSlider.setValue(nextVal);
        runSimulation(nextVal);
    }

    /*
     * Лабораторна робота № 4-4 "Згасаючі коливання".
     * Функція: runSimulation.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void runSimulation(double targetRd) {
        setControlsDisable(true);
        applyPhysicsSettings();

        liveStatusLabel.setText("СИСТЕМА: ГЕНЕРАЦІЯ");
        liveStatusLabel.setStyle("-fx-text-fill: red;");
        canvas.startSimulation();

        startTime = System.nanoTime();

        measurementTimer = new AnimationTimer() {
    /*
     * Лабораторна робота № 4-4 "Згасаючі коливання".
     * Функція: handle.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
            @Override
            public void handle(long now) {
                double elapsed = (now - startTime) / 1_000_000_000.0;
                liveTimeLabel.setText(String.format(Locale.US, "t = %.2f с", elapsed));

                if (elapsed >= targetTime) {
                    this.stop();
                    finishMeasurement(targetRd);
                }
            }
        };
        measurementTimer.start();
    }

    /*
     * Лабораторна робота № 4-4 "Згасаючі коливання".
     * Функція: finishMeasurement.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void finishMeasurement(double rd) {
        liveStatusLabel.setText("СИСТЕМА: ЗАПИС");
        liveStatusLabel.setStyle("-fx-text-fill: yellow;");

        try {
            double cFarads = Double.parseDouble(cField.getText()) * 1e-6;
            double lHenries = Double.parseDouble(lField.getText()) * 1e-3;
            double rIn = Double.parseDouble(rInternalField.getText());
            double rTotal = rIn + rd;

            String scaleStr = timeScaleCombo.getSelectionModel().getSelectedItem();
            double t0 = Double.parseDouble(scaleStr.split(" ")[0]) * 1e-3;

            double rKr = 2 * Math.sqrt(lHenries / cFarads);
            boolean isAperiodic = rTotal >= rKr;

            int n = 0;
            double nDivs = 0;
            double um = 0;
            double umNext = 0;
            double periodExp = 0;
            double decrementExp = 0;

            if (!isAperiodic) {
                double omega0Sq = 1.0 / (lHenries * cFarads);
                double beta = rTotal / (2 * lHenries);
                double omega = Math.sqrt(omega0Sq - beta * beta);
                double truePeriod = 2 * Math.PI / omega;

                n = (int) ((t0 * 10) / truePeriod);
                if (n < 1) n = 1;

                nDivs = Math.round((truePeriod * n) / t0 * 10.0) / 10.0;
                periodExp = (nDivs * t0) / n;

                double u0 = 100.0;
                um = Math.round((u0 * Math.exp(-beta * periodExp)) * 10.0) / 10.0;
                umNext = Math.round((u0 * Math.exp(-beta * periodExp * 2)) * 10.0) / 10.0;

                decrementExp = Math.log(um / umNext);
            }

            Measurement m = new Measurement(
                    idCounter++, rd, n, nDivs, um, umNext,
                    Math.round(periodExp * 100000.0) / 100000.0,
                    Math.round(decrementExp * 10000.0) / 10000.0
            );
            data.add(m);
            updateStats();

        } catch (Exception e) {
            showAlert("Помилка", "Некоректні дані при розрахунках.");
        }

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(1200); Platform.runLater(this::processNextAuto); }
                catch (InterruptedException ignored) {}
            }).start();
        } else {
            setControlsDisable(false);
            liveStatusLabel.setText("СИСТЕМА: ОЧІКУВАННЯ");
            liveStatusLabel.setStyle("-fx-text-fill: yellow;");
        }
    }

    /*
     * Лабораторна робота № 4-4 "Згасаючі коливання".
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

        Measurement last = data.get(data.size() - 1);

        if (last.getPeriod() == 0) {
            finalResultLabel.setText(String.format(Locale.US,
                    "ОБРОБКА (№%d): Загальний опір R = %.1f Ом перевищив R_кр (%.1f Ом). " +
                            "Виник АПЕРІОДИЧНИЙ процес розряду. Коливання відсутні.",
                    last.getId(), last.getRd() + Double.parseDouble(rInternalField.getText()), Double.parseDouble(rKrField.getText())
            ));
            return;
        }

        double cFarads = Double.parseDouble(cField.getText()) * 1e-6;
        double lHenries = Double.parseDouble(lField.getText()) * 1e-3;
        double rTotal = last.getRd() + Double.parseDouble(rInternalField.getText());

        double periodTheo = 2 * Math.PI / Math.sqrt((1.0 / (lHenries * cFarads)) - Math.pow(rTotal / (2 * lHenries), 2));
        double decrementTheo = (rTotal / (2 * lHenries)) * periodTheo;

        String conclusion = String.format(Locale.US,
                "ОБРОБКА (Останній замір №%d):\n" +
                        "1. Експериментальний період T = %.5f с (Теоретичний за ф.12: %.5f с).\n" +
                        "2. Логарифмічний декремент згасання: експеримент δ = %.4f (Теорія: %.4f).\n" +
                        "ВИСНОВОК: Експериментальні дані підтверджують, що зі збільшенням опору R контуру " +
                        "швидкість згасання зростає.",
                last.getId(), last.getPeriod(), periodTheo, last.getDecrement(), decrementTheo
        );

        finalResultLabel.setText(conclusion);
    }
}