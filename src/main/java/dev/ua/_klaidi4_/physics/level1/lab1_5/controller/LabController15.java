/*
 * Лабораторна робота № 1-5 "Абсолютно пружний удар".
 * Клас: LabController15.
 * Призначення: керує інтерфейсом лабораторної роботи, проведенням дослідів
 * з вивчення абсолютно пружного удару за допомогою конденсаторного хронометра.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level1.lab1_5.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level1.lab1_5.model.Measurement;
import dev.ua._klaidi4_.physics.level1.lab1_5.view.ChronometerCanvas;
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

public class LabController15 extends BaseLabController {

    private ChronometerCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField cField;
    private TextField rField;
    private TextField n0Field;
    private TextField dhField;
    private TextField radiusField;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveN0Label;
    private Label liveNLabel;
    private Label liveTauLabel;
    private Queue<AutoTestParam> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;
    private double currentSimN0 = 0;
    private double currentSimN = 0;

    private static class AutoTestParam {
        double n0, n;
        AutoTestParam(double n0, double n) {
            this.n0 = n0;
            this.n = n;
        }
    }

    /*
     * Лабораторна робота № 1-5 "Абсолютно пружний удар".
     * Функція: LabController15.
     * Призначення: Конструктор класу, ініціалізує інтерфейс користувача.
     */
    public LabController15() {
        initUI();
    }

    /*
     * Лабораторна робота № 1-5 "Абсолютно пружний удар".
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
     * Лабораторна робота № 1-5 "Абсолютно пружний удар".
     * Функція: initUI.
     * Призначення: Створює візуальні компоненти, панелі управління та таблицю результатів.
     */
    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 1-5)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри установки");
        labPane.setCollapsible(false);
        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        cField = new TextField("6.9");
        rField = new TextField("147");
        n0Field = new TextField("100");
        dhField = new TextField("0.02");
        radiusField = new TextField("1.51");

        paramsBox.getChildren().addAll(
                createInputGroup("Ємність конденсатора C (мкФ):", cField),
                createInputGroup("Опір кола R (Ом):", rField),
                createInputGroup("Початковий показ n0 (под):", n0Field),
                createInputGroup("Висота піднімання Δh (м):", dhField),
                createInputGroup("Радіус кулі r (см):", radiusField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(280);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        startBtn = new Button("▶ ЗІТКНЕННЯ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ (10 дослідів)");
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
            liveN0Label.setText("n0 = 0");
            liveNLabel.setText("n = 0");
            liveTauLabel.setText("τ = 0.0 мкс");
        });

        leftPanel.getChildren().addAll(title, labPane, startBtn, autoBtn, clearBtn);

        canvas = new ChronometerCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(220, 100);
        Label dashTitle = new Label("ХРОНОМЕТР");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveN0Label = new Label("n0 = 0");
        liveN0Label.setStyle("-fx-text-fill: #3498db;");
        liveNLabel = new Label("n = 0");
        liveNLabel.setStyle("-fx-text-fill: #e74c3c;");
        liveTauLabel = new Label("τ = 0.0 мкс");
        liveTauLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        liveTauLabel.setStyle("-fx-text-fill: #00ff00;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveN0Label, liveNLabel, liveTauLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, Double> cCol = new TableColumn<>("C (мкФ)");
        cCol.setCellValueFactory(new PropertyValueFactory<>("c"));
        TableColumn<Measurement, Double> rCol = new TableColumn<>("R (Ом)");
        rCol.setCellValueFactory(new PropertyValueFactory<>("r"));
        TableColumn<Measurement, Double> n0Col = new TableColumn<>("n0");
        n0Col.setCellValueFactory(new PropertyValueFactory<>("n0"));
        TableColumn<Measurement, Double> nCol = new TableColumn<>("n");
        nCol.setCellValueFactory(new PropertyValueFactory<>("n"));
        TableColumn<Measurement, Double> tauCol = new TableColumn<>("τ (мкс)");
        tauCol.setCellValueFactory(new PropertyValueFactory<>("tau"));
        TableColumn<Measurement, Double> fCol = new TableColumn<>("F (Н)");
        fCol.setCellValueFactory(new PropertyValueFactory<>("force"));

        table.getColumns().addAll(idCol, cCol, rCol, n0Col, nCol, tauCol, fCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        canvas.setCallbacks(
                () -> Platform.runLater(() -> {
                    liveStatusLabel.setText("Статус: РОЗРЯДЖЕННЯ");
                    liveStatusLabel.setStyle("-fx-text-fill: red;");
                }),
                () -> Platform.runLater(this::finishMeasurement)
        );
    }

    /*
     * Лабораторна робота № 1-5 "Абсолютно пружний удар".
     * Функція: setControlsDisable.
     * Призначення: Блокує або розблокує кнопки та поля введення під час симуляції.
     */
    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        cField.setDisable(disable);
        rField.setDisable(disable);
        n0Field.setDisable(disable);
        dhField.setDisable(disable);
        radiusField.setDisable(disable);
    }

    /*
     * Лабораторна робота № 1-5 "Абсолютно пружний удар".
     * Функція: startManual.
     * Призначення: Запускає поодинокий дослід за параметрами користувача.
     */
    private void startManual() {
        try {
            double C = Double.parseDouble(cField.getText()) * 1e-6;
            double R = Double.parseDouble(rField.getText());
            double n0 = Double.parseDouble(n0Field.getText());
            double dh = Double.parseDouble(dhField.getText());
            Double.parseDouble(radiusField.getText());
            isAutoRunning = false;
            double typicalTau = (376.0 + (Math.random() - 0.5) * 10) * 1e-6;
            double targetN = n0 / Math.exp(typicalTau / (C * R));
            runSimulation(dh, n0, Math.round(targetN));
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа в поля.");
        }
    }

    /*
     * Лабораторна робота № 1-5 "Абсолютно пружний удар".
     * Функція: startAuto.
     * Призначення: Формує чергу та запускає серію автоматичних вимірювань.
     */
    private void startAuto() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        autoQueue.add(new AutoTestParam(100, 69));
        autoQueue.add(new AutoTestParam(110, 75));
        autoQueue.add(new AutoTestParam(106, 73));
        autoQueue.add(new AutoTestParam(101, 69));
        autoQueue.add(new AutoTestParam(107, 73));
        autoQueue.add(new AutoTestParam(100, 69));
        autoQueue.add(new AutoTestParam(108, 69));
        autoQueue.add(new AutoTestParam(102, 68));
        autoQueue.add(new AutoTestParam(109, 70));
        autoQueue.add(new AutoTestParam(108, 74));

        isAutoRunning = true;
        processNextAuto();
    }

    /*
     * Лабораторна робота № 1-5 "Абсолютно пружний удар".
     * Функція: processNextAuto.
     * Призначення: Витягує наступний набір параметрів та запускає черговий дослід в авторежимі.
     */
    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoRunning = false;
            liveStatusLabel.setText("СИСТЕМА: ГОТОВО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            setControlsDisable(false);
            return;
        }

        setControlsDisable(true);
        AutoTestParam param = autoQueue.poll();
        n0Field.setText(String.valueOf(param.n0));

        try {
            double dh = Double.parseDouble(dhField.getText());
            runSimulation(dh, param.n0, param.n);
        } catch (Exception ignored) {}
    }

    /*
     * Лабораторна робота № 1-5 "Абсолютно пружний удар".
     * Функція: runSimulation.
     * Призначення: Ініціює візуальну симуляцію удару куль та роботи хронометра.
     */
    private void runSimulation(double dh, double n0, double n) {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: РУХ КУЛІ");
        liveStatusLabel.setStyle("-fx-text-fill: cyan;");

        currentSimN0 = n0;
        currentSimN = n;

        liveN0Label.setText(String.format("n0 = %.1f", n0));
        liveNLabel.setText("n = ?");
        liveTauLabel.setText("τ = ?");

        canvas.startSimulation(dh, n0, n);
    }

    /*
     * Лабораторна робота № 1-5 "Абсолютно пружний удар".
     * Функція: finishMeasurement.
     * Призначення: Фіксує результати вимірювання (кількість поділок n) та додає їх у таблицю.
     */
    private void finishMeasurement() {
        try {
            double C_mkF = Double.parseDouble(cField.getText());
            double C = C_mkF * 1e-6;
            double R = Double.parseDouble(rField.getText());
            double dh = Double.parseDouble(dhField.getText());
            double r_cm = Double.parseDouble(radiusField.getText());
            double tau_seconds = C * R * Math.log(currentSimN0 / currentSimN);
            double tau_mks = tau_seconds * 1e6;
            double v = Math.sqrt(2 * 9.81 * dh);
            double r_m = r_cm / 100.0;
            double m = (4.0 / 3.0) * Math.PI * Math.pow(r_m, 3) * 7800;
            double force = (m * v) / tau_seconds;

            liveStatusLabel.setText("Статус: ВИМІРЯНО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            liveNLabel.setText(String.format("n = %.1f", currentSimN));
            liveTauLabel.setText(String.format("τ = %.1f мкс", tau_mks));

            Measurement meas = new Measurement(
                    idCounter++, C_mkF, R,
                    currentSimN0, currentSimN, dh, r_cm,
                    Math.round(tau_mks * 10.0) / 10.0,
                    Math.round(v * 100.0) / 100.0,
                    Math.round(force * 10.0) / 10.0
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
     * Лабораторна робота № 1-5 "Абсолютно пружний удар".
     * Функція: updateStats.
     * Призначення: Розраховує час зіткнення та проводить статистичну обробку результатів.
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

        double sumTau = 0;
        double sumF = 0;
        for (Measurement meas : data) {
            sumTau += meas.getTau();
            sumF += meas.getForce();
        }

        double tauAvg = sumTau / data.size();
        double fAvg = sumF / data.size();

        double sumDeltaTau = 0;
        for (Measurement meas : data) {
            sumDeltaTau += Math.abs(meas.getTau() - tauAvg);
        }
        double deltaTau = sumDeltaTau / data.size();
        double epsTau = (deltaTau / tauAvg) * 100;

        Measurement last = data.get(data.size() - 1);

        String conclusion = String.format(
                "ОБРОБКА РЕЗУЛЬТАТІВ ТА АНАЛІЗ:\n" +
                        "1. Час зіткнення розраховано за формулою: τ = C*R*ln(n0/n).\n" +
                        "   Середній час зіткнення: τ_ср = %.1f мкс.\n" +
                        "2. Швидкість кулі при ударі: v = √(2gΔh) = %.2f м/с.\n" +
                        "3. Середня сила удару: F_ср = %.1f Н.\n" +
                        "4. Похибки вимірювання часу: Абсолютна Δτ = %.1f мкс, Відносна ε = %.1f %%.\n\n" +
                        "ВИСНОВОК: Метод конденсаторного хронометра дозволяє ефективно вимірювати дуже малі " +
                        "проміжки часу удару (порядку сотень мікросекунд), що підтверджується стабільними " +
                        "результатами серії дослідів.",
                tauAvg, last.getV(), fAvg, deltaTau, epsTau
        );

        finalResultLabel.setText(conclusion);
    }
}