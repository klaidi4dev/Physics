/*
 * Лабораторна робота № 1-1 "Машина Атвуда".
 * Клас: LabController11.
 * Призначення: керує інтерфейсом лабораторної роботи, запуском досліду,
 * введенням параметрів, таблицею результатів та обробкою вимірювань.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level1.lab1_1.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level1.lab1_1.model.Measurement;
import dev.ua._klaidi4_.physics.level1.lab1_1.view.AtwoodCanvas;
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

public class LabController11 extends BaseLabController {

    private AtwoodCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField bigMField;
    private TextField smallMField;
    private TextField s1Field;
    private TextField s2Field;
    private TextField gravityField;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveTimeLabel;
    private Label vLabel;
    private Queue<AutoTestParam> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    private static class AutoTestParam {
        double M, m, s1, s2, g;
        AutoTestParam(double M, double m, double s1, double s2, double g) {
            this.M = M;
            this.m = m;
            this.s1 = s1;
            this.s2 = s2;
            this.g = g;
        }
    }

    /*
     * Лабораторна робота № 1-1 "Машина Атвуда".
     * Функція: LabController11.
     * Призначення: Конструктор класу, ініціалізує інтерфейс та налаштування.
     */
    public LabController11() {
        initUI();
        applyPhysicsSettings();
    }

    /*
     * Лабораторна робота № 1-1 "Машина Атвуда".
     * Функція: shutdown.
     * Призначення: Зупиняє анімацію та скидає чергу автоматичного тестування при закритті вікна.
     */
    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        isAutoRunning = false;
        autoQueue.clear();
    }

    /*
     * Лабораторна робота № 1-1 "Машина Атвуда".
     * Функція: initUI.
     * Призначення: Ініціалізує графічний інтерфейс користувача, створює панелі управління, графік та таблицю.
     */
    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 1-1)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри установки");
        labPane.setCollapsible(true);
        labPane.setExpanded(true);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        bigMField = new TextField("0.0628");
        smallMField = new TextField("0.012");
        s1Field = new TextField("0.2");
        s2Field = new TextField("0.25");
        gravityField = new TextField("9.81");

        bigMField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        smallMField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        s1Field.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        s2Field.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        gravityField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());

        paramsBox.getChildren().addAll(
                createInputGroup("Маса вантажу M (кг):", bigMField),
                createInputGroup("Тягарець m (кг):", smallMField),
                createInputGroup("Шлях розгону S1 (м):", s1Field),
                createInputGroup("Шлях рівн. руху S2 (м):", s2Field),
                createInputGroup("Сила тяжіння g (м/с²):", gravityField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(280);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        startBtn = new Button("▶ ЗАПУСТИТИ МАШИНУ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ");
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
            liveTimeLabel.setText("t = 0.000 с");
        });

        leftPanel.getChildren().addAll(title, labPane, startBtn, autoBtn, clearBtn);

        canvas = new AtwoodCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(200, 80);
        Label dashTitle = new Label("МІЛІСЕКУНДОМІР");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveTimeLabel = new Label("t = 0.000 с");
        liveTimeLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        liveTimeLabel.setStyle("-fx-text-fill: #00ff00;");
        vLabel = new Label("V = 0.00 м/с");
        vLabel.setStyle("-fx-text-fill: #00ff00;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveTimeLabel, vLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№ досліду");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, Double> mCol = new TableColumn<>("M (кг)");
        mCol.setCellValueFactory(new PropertyValueFactory<>("bigM"));
        TableColumn<Measurement, Double> smCol = new TableColumn<>("m (кг)");
        smCol.setCellValueFactory(new PropertyValueFactory<>("smallM"));
        TableColumn<Measurement, Double> s1Col = new TableColumn<>("S1 (м)");
        s1Col.setCellValueFactory(new PropertyValueFactory<>("s1"));
        TableColumn<Measurement, Double> s2Col = new TableColumn<>("S2 (м)");
        s2Col.setCellValueFactory(new PropertyValueFactory<>("s2"));
        TableColumn<Measurement, Double> tCol = new TableColumn<>("t (с)");
        tCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        TableColumn<Measurement, Double> gCol = new TableColumn<>("g (м/с²)");
        gCol.setCellValueFactory(new PropertyValueFactory<>("gravity"));

        table.getColumns().addAll(idCol, mCol, smCol, s1Col, s2Col, tCol, gCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(130);

        VBox statsBox = createStatsBox();

        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        canvas.setCallbacks(
                () -> Platform.runLater(() -> {
                    liveStatusLabel.setText("Статус: ВИМІР ЧАСУ");
                    liveStatusLabel.setStyle("-fx-text-fill: cyan;");
                }),
                () -> Platform.runLater(this::finishMeasurement)
        );
    }

    /*
     * Лабораторна робота № 1-1 "Машина Атвуда".
     * Функція: applyPhysicsSettings.
     * Призначення: Зчитує параметри з полів введення та застосовує їх до фізичної моделі.
     */
    private void applyPhysicsSettings() {
        try {
            double M = Double.parseDouble(bigMField.getText());
            double m = Double.parseDouble(smallMField.getText());
            double s1 = Double.parseDouble(s1Field.getText());
            double s2 = Double.parseDouble(s2Field.getText());
            double g = Double.parseDouble(gravityField.getText());

            canvas.setParameters(M, m, s1, s2, g);

            liveStatusLabel.setText("Статус: ГОТОВО");
            liveStatusLabel.setStyle("-fx-text-fill: yellow;");
            liveTimeLabel.setText("t = 0.000 с");
        } catch (NumberFormatException ignored) {}
    }

    /*
     * Лабораторна робота № 1-1 "Машина Атвуда".
     * Функція: setControlsDisable.
     * Призначення: Блокує або розблокує елементи керування інтерфейсом під час симуляції.
     */
    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        bigMField.setDisable(disable);
        smallMField.setDisable(disable);
        s1Field.setDisable(disable);
        s2Field.setDisable(disable);
        gravityField.setDisable(disable);
    }

    /*
     * Лабораторна робота № 1-1 "Машина Атвуда".
     * Функція: startManual.
     * Призначення: Запускає симуляцію вручну після перевірки введених параметрів.
     */
    private void startManual() {
        try {
            Double.parseDouble(bigMField.getText());
            Double.parseDouble(smallMField.getText());
            Double.parseDouble(gravityField.getText());
            isAutoRunning = false;
            applyPhysicsSettings();
            startSimulation();
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа в поля параметрів.");
        }
    }

    /*
     * Лабораторна робота № 1-1 "Машина Атвуда".
     * Функція: startSimulation.
     * Призначення: Запускає процес симуляції руху та оновлює таймер у реальному часі.
     */
    private void startSimulation() {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: РОЗГІН");
        liveStatusLabel.setStyle("-fx-text-fill: red;");
        canvas.startSimulation();

        new Thread(() -> {
            while (startBtn.isDisabled()) {
                Platform.runLater(() -> {
                    if (!liveStatusLabel.getText().contains("ЗАВЕРШЕНО")) {
                        liveTimeLabel.setText(String.format("t = %.3f с", canvas.getMeasuredTime()));
                    }
                });
                try { Thread.sleep(30); } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    /*
     * Лабораторна робота № 1-1 "Машина Атвуда".
     * Функція: startAuto.
     * Призначення: Формує чергу параметрів та запускає режим автоматичного виконання серії дослідів.
     */
    private void startAuto() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        double currentG;
        try {
            currentG = Double.parseDouble(gravityField.getText());
        } catch (Exception e) {
            currentG = 9.81;
        }

        for (int i = 0; i < 5; i++) {
            autoQueue.add(new AutoTestParam(0.0628, 0.012, 0.2, 0.25, currentG));
        }
        for (int i = 0; i < 5; i++) {
            autoQueue.add(new AutoTestParam(0.0628, 0.021, 0.2, 0.25, currentG));
        }

        isAutoRunning = true;
        processNextAuto();
    }

    /*
     * Лабораторна робота № 1-1 "Машина Атвуда".
     * Функція: processNextAuto.
     * Призначення: Витягує наступний набір параметрів з черги та запускає черговий дослід.
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
        bigMField.setText(String.valueOf(param.M));
        smallMField.setText(String.valueOf(param.m));
        s1Field.setText(String.valueOf(param.s1));
        s2Field.setText(String.valueOf(param.s2));
        gravityField.setText(String.valueOf(param.g));

        applyPhysicsSettings();
        startSimulation();
    }

    /*
     * Лабораторна робота № 1-1 "Машина Атвуда".
     * Функція: finishMeasurement.
     * Призначення: Обробляє завершення руху, розраховує результати вимірювання (з урахуванням похибки) та додає їх до таблиці.
     */
    private void finishMeasurement() {
        try {
            double M = Double.parseDouble(bigMField.getText());
            double m = Double.parseDouble(smallMField.getText());
            double s1 = Double.parseDouble(s1Field.getText());
            double s2 = Double.parseDouble(s2Field.getText());
            double exactTime = canvas.getMeasuredTime();
            double humanError = (Math.random() - 0.5) * 0.015;
            double measuredT = exactTime + humanError;

            if (measuredT <= 0) measuredT = 0.001;

            liveTimeLabel.setText(String.format("t = %.3f с", measuredT));
            liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

            double v = s2 / measuredT;
            double a = (s2 * s2) / (2 * s1 * measuredT * measuredT);

            double gExp = ((2 * M + m) / m) * a;

            vLabel.setText(String.format("V = %.2f м/с", v));

            Measurement meas = new Measurement(
                    idCounter++, M, m, s1, s2,
                    Math.round(measuredT * 1000.0) / 1000.0,
                    Math.round(gExp * 100.0) / 100.0
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
        } catch (Exception ignored) {}
    }

    /*
     * Лабораторна робота № 1-1 "Машина Атвуда".
     * Функція: updateStats.
     * Призначення: Розраховує середні значення, абсолютну та відносну похибки на основі даних у таблиці.
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

        double sumT = 0;
        double sumG = 0;

        for (Measurement meas : data) {
            sumT += meas.getTime();
            sumG += meas.getGravity();
        }

        double tAvg = sumT / data.size();
        double gAvg = sumG / data.size();
        double sumDelta = 0;
        for (Measurement meas : data) {
            sumDelta += Math.abs(meas.getGravity() - gAvg);
        }
        double deltaAvg = sumDelta / data.size();
        double epsilon = (deltaAvg / gAvg) * 100;

        String conclusion = String.format(
                "ОБРОБКА РЕЗУЛЬТАТІВ:\n" +
                        "1. Середнє значення часу руху вантажу на шляху S2: t_ср = %.3f с.\n" +
                        "2. Середнє значення прискорення вільного падіння: g = %.2f м/с².\n" +
                        "3. Абсолютна похибка: Δg = %.2f м/с². Відносна похибка: ε = %.1f %%.\n\n" +
                        "ВІДПОВІДЬ: g = (%.2f ± %.2f) м/с².",
                tAvg, gAvg, deltaAvg, epsilon, gAvg, deltaAvg
        );

        finalResultLabel.setText(conclusion);
    }
}