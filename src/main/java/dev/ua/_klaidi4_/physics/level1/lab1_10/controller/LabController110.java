/*
 * Лабораторна робота № 1-10 "Момент інерції: Скочування тіл".
 * Клас: LabController110.
 * Призначення: керує інтерфейсом лабораторної роботи, проведенням дослідів
 * з визначення моменту інерції тіл методом скочування з похилої площини.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level1.lab1_10.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level1.lab1_10.model.Measurement;
import dev.ua._klaidi4_.physics.level1.lab1_10.view.RollingCanvas;
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
import java.util.Queue;

public class LabController110 extends BaseLabController {

    private RollingCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> bodyCombo;
    private TextField mField;
    private TextField rField;
    private TextField hField;
    private TextField sField;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveTimeLabel;
    private Queue<AutoTestParam> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    private static class AutoTestParam {
        int bodyIdx;
        double m, r, h, s;
        AutoTestParam(int bodyIdx, double m, double r, double h, double s) {
            this.bodyIdx = bodyIdx;
            this.m = m;
            this.r = r;
            this.h = h;
            this.s = s;
        }
    }

    /*
     * Лабораторна робота № 1-10 "Момент інерції: Скочування тіл".
     * Функція: LabController110.
     * Призначення: Конструктор класу, ініціалізує інтерфейс та встановлює початкові налаштування.
     */
    public LabController110() {
        initUI();
        applyPhysicsSettings();
    }

    /*
     * Лабораторна робота № 1-10 "Момент інерції: Скочування тіл".
     * Функція: shutdown.
     * Призначення: Зупиняє анімацію та очищає чергу при закритті модуля.
     */
    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        isAutoRunning = false;
        autoQueue.clear();
    }

    /*
     * Лабораторна робота № 1-10 "Момент інерції: Скочування тіл".
     * Функція: initUI.
     * Призначення: Створює графічний інтерфейс: панелі керування, полотно симуляції та таблицю результатів.
     */
    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 1-10)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри установки");
        labPane.setCollapsible(false);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        bodyCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Суцільний циліндр",
                "Суцільна куля",
                "Порожнистий циліндр"
        ));
        bodyCombo.getSelectionModel().select(1);

        mField = new TextField("0.080");
        hField = new TextField("0.24");
        sField = new TextField("1.21");
        rField = new TextField("0.0212");

        bodyCombo.setOnAction(e -> applyPhysicsSettings());
        mField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        rField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        hField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        sField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());

        paramsBox.getChildren().addAll(
                createInputGroup("Досліджуване тіло:", bodyCombo),
                createInputGroup("Маса тіла m (кг):", mField),
                createInputGroup("Радіус тіла R (м):", rField),
                createInputGroup("Висота площини h (м):", hField),
                createInputGroup("Шлях скочування S (м):", sField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(280);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        startBtn = new Button("▶ ВІДПУСТИТИ ТІЛО");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ (По таблиці)");
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

        leftPanel.getChildren().addAll(title, labPane, startBtn, autoBtn, clearBtn);

        canvas = new RollingCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(220, 60);
        Label dashTitle = new Label("ЕЛЕКТРОННИЙ СЕКУНДОМІР");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveTimeLabel = new Label("t = 0.00 с");
        liveTimeLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        liveTimeLabel.setStyle("-fx-text-fill: #00ff00;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveTimeLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, String> bodyCol = new TableColumn<>("Тіло");
        bodyCol.setCellValueFactory(new PropertyValueFactory<>("bodyType"));
        TableColumn<Measurement, Double> tCol = new TableColumn<>("t (с)");
        tCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        TableColumn<Measurement, Double> mCol = new TableColumn<>("m (кг)");
        mCol.setCellValueFactory(new PropertyValueFactory<>("m"));
        TableColumn<Measurement, Double> hCol = new TableColumn<>("h (м)");
        hCol.setCellValueFactory(new PropertyValueFactory<>("h"));
        TableColumn<Measurement, Double> sCol = new TableColumn<>("S (м)");
        sCol.setCellValueFactory(new PropertyValueFactory<>("s"));
        TableColumn<Measurement, Double> rCol = new TableColumn<>("R (м)");
        rCol.setCellValueFactory(new PropertyValueFactory<>("r"));
        TableColumn<Measurement, Double> eCol = new TableColumn<>("I експ");
        eCol.setCellValueFactory(new PropertyValueFactory<>("expI"));
        TableColumn<Measurement, Double> tICol = new TableColumn<>("I теор");
        tICol.setCellValueFactory(new PropertyValueFactory<>("theoI"));

        table.getColumns().addAll(idCol, bodyCol, tCol, mCol, hCol, sCol, rCol, eCol, tICol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);
    }

    /*
     * Лабораторна робота № 1-10 "Момент інерції: Скочування тіл".
     * Функція: applyPhysicsSettings.
     * Призначення: Передає актуальні геометричні параметри похилої площини до візуальної моделі.
     */
    private void applyPhysicsSettings() {
        try {
            int bodyIndex = bodyCombo.getSelectionModel().getSelectedIndex();
            double h = Double.parseDouble(hField.getText());
            double s = Double.parseDouble(sField.getText());
            canvas.setParameters(bodyIndex, h, s);
        } catch (Exception ignored) {}
    }

    /*
     * Лабораторна робота № 1-10 "Момент інерції: Скочування тіл".
     * Функція: setControlsDisable.
     * Призначення: Керує доступністю полів введення під час активного руху тіла.
     */
    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        bodyCombo.setDisable(disable);
        mField.setDisable(disable);
        rField.setDisable(disable);
        hField.setDisable(disable);
        sField.setDisable(disable);
    }

    /*
     * Лабораторна робота № 1-10 "Момент інерції: Скочування тіл".
     * Функція: startManual.
     * Призначення: Запускає поодиноке вимірювання часу скочування тіла.
     */
    private void startManual() {
        try {
            Double.parseDouble(mField.getText());
            Double.parseDouble(rField.getText());
            Double.parseDouble(hField.getText());
            Double.parseDouble(sField.getText());
            isAutoRunning = false;
            runSimulation(bodyCombo.getSelectionModel().getSelectedIndex());
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа в поля.");
        }
    }

    /*
     * Лабораторна робота № 1-10 "Момент інерції: Скочування тіл".
     * Функція: startAuto.
     * Призначення: Ініціює автоматичну серію дослідів для всіх типів тіл.
     */
    private void startAuto() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        autoQueue.add(new AutoTestParam(1, 0.080, 0.0212, 0.24, 1.21));
        autoQueue.add(new AutoTestParam(1, 0.080, 0.0212, 0.24, 1.21));
        autoQueue.add(new AutoTestParam(1, 0.080, 0.0212, 0.24, 1.21));
        autoQueue.add(new AutoTestParam(0, 0.089, 0.0113, 0.24, 1.21));
        autoQueue.add(new AutoTestParam(0, 0.089, 0.0113, 0.24, 1.21));
        autoQueue.add(new AutoTestParam(0, 0.089, 0.0113, 0.24, 1.21));

        isAutoRunning = true;
        processNextAuto();
    }

    /*
     * Лабораторна робота № 1-10 "Момент інерції: Скочування тіл".
     * Функція: processNextAuto.
     * Призначення: Виконує наступний крок в автоматичному режимі.
     */
    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoRunning = false;
            liveStatusLabel.setText("СИСТЕМА: ГОТОВО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            setControlsDisable(false);
            return;
        }
        AutoTestParam param = autoQueue.poll();
        bodyCombo.getSelectionModel().select(param.bodyIdx);
        mField.setText(String.valueOf(param.m));
        rField.setText(String.valueOf(param.r));
        hField.setText(String.valueOf(param.h));
        sField.setText(String.valueOf(param.s));

        applyPhysicsSettings();
        runSimulation(param.bodyIdx);
    }

    /*
     * Лабораторна робота № 1-10 "Момент інерції: Скочування тіл".
     * Функція: runSimulation.
     * Призначення: Запускає фізичну симуляцію руху тіла по похилій площині.
     */
    private void runSimulation(int bodyIndex) {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: СКОЧУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: cyan;");

        double m = Double.parseDouble(mField.getText());
        double R = Double.parseDouble(rField.getText());
        double h = Double.parseDouble(hField.getText());
        double S = Double.parseDouble(sField.getText());

        double iTheory = 0;
        if (bodyIndex == 0) iTheory = 0.5 * m * R * R;
        else if (bodyIndex == 1) iTheory = 0.4 * m * R * R;
        else if (bodyIndex == 2) iTheory = 1.0 * m * R * R;

        double a = (9.81 * (h / S)) / (1 + iTheory / (m * R * R));
        final double exactTime = Math.sqrt(2 * S / a);
        final double finalMeasuredTime = exactTime + (Math.random() - 0.5) * 0.04;
        final double finalITheory = iTheory;

        canvas.setOnFinishCallback(() -> Platform.runLater(() -> finishMeasurement(bodyIndex, finalMeasuredTime, finalITheory)));
        canvas.startSimulation(finalMeasuredTime);

        new Thread(() -> {
            long start = System.currentTimeMillis();
            long targetMs = (long) (finalMeasuredTime * 1000);

            while (System.currentTimeMillis() - start < targetMs) {
                long elapsed = System.currentTimeMillis() - start;
                double seconds = elapsed / 1000.0;
                Platform.runLater(() -> liveTimeLabel.setText(String.format("t = %.3f с", seconds)));
                try { Thread.sleep(30); } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    /*
     * Лабораторна робота № 1-10 "Момент інерції: Скочування тіл".
     * Функція: finishMeasurement.
     * Призначення: Фіксує час скочування, розраховує експериментальний момент інерції та додає дані до таблиці.
     */
    private void finishMeasurement(int bodyIndex, double measuredTime, double iTheory) {
        liveTimeLabel.setText(String.format("t = %.3f с", measuredTime));
        liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

        double m = Double.parseDouble(mField.getText());
        double R = Double.parseDouble(rField.getText());
        double h = Double.parseDouble(hField.getText());
        double S = Double.parseDouble(sField.getText());
        double iExp = m * R * R * ((9.81 * h * measuredTime * measuredTime) / (2 * S * S) - 1);

        String bodyName = bodyCombo.getItems().get(bodyIndex);

        Measurement meas = new Measurement(
                idCounter++, bodyName, m, h, S, R,
                Math.round(measuredTime * 100.0) / 100.0,
                Math.round(iExp * 1000000.0) / 1000000.0,
                Math.round(iTheory * 1000000.0) / 1000000.0
        );
        data.add(meas);
        updateStats();

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(1500); Platform.runLater(this::processNextAuto); }
                catch (InterruptedException ignored) {}
            }).start();
        } else {
            setControlsDisable(false);
        }
    }

    /*
     * Лабораторна робота № 1-10 "Момент інерції: Скочування тіл".
     * Функція: updateStats.
     * Призначення: Проводить статистичний аналіз отриманих результатів вимірювань.
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
        double sumEps = 0;
        double sumDelta = 0;

        for (Measurement meas : data) {
            double delta = Math.abs(meas.getExpI() - meas.getTheoI());
            sumDelta += delta;
            sumEps += (delta / meas.getTheoI()) * 100;
        }

        double avgDelta = sumDelta / data.size();
        double avgEps = sumEps / data.size();

        String conclusion = String.format(
                "ОБРОБКА РЕЗУЛЬТАТІВ ЕКСПЕРИМЕНТУ ТА ЇХ АНАЛІЗ:\n" +
                        "1. За робочою формулою (9) I = m*R²*(g*h*t² / (2*S²) - 1) вирахувано експериментальний момент інерції I_експ.\n" +
                        "2. За формулами з Таблиці 1 вирахувано теоретичний момент інерції I_теор (Циліндр: 1/2 mR², Куля: 2/5 mR²).\n" +
                        "3. СПІВСТАВЛЕННЯ ТА ВИСНОВКИ: Результати експерименту підтверджують закон збереження механічної енергії. " +
                        "Момент інерції залежить від розподілу маси тіла. Суцільна куля має менший момент інерції (і більшу швидкість скочування), ніж циліндр.\n" +
                        "4. Похибки експерименту: Середня абсолютна похибка ΔI = %.6f кг·м², відносна похибка ε = %.1f %%.\n" +
                        "   (Похибка зумовлена втратою енергії на подолання тертя кочення та опору повітря, якими знехтували у формулі 1).",
                avgDelta, avgEps
        );

        finalResultLabel.setText(conclusion);
    }
}