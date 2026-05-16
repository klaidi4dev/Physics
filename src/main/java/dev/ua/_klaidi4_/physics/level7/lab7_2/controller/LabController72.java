/*
 * Лабораторна робота № 7-2 "В'язкість газів".
 * Клас: LabController72.
 * Призначення: керує інтерфейсом лабораторної роботи, запуском досліду,
 * введенням параметрів, таблицею результатів та обробкою вимірювань.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level7.lab7_2.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level7.lab7_2.model.Measurement;
import dev.ua._klaidi4_.physics.level7.lab7_2.view.MariotteCanvas;
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
import java.util.Queue;

public class LabController72 extends BaseLabController {

    private MariotteCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField fieldRho, fieldG, fieldPatm, fieldR, fieldMu, fieldS;
    private TextField fieldRadius, fieldLength, fieldTemp, fieldVolume;
    private Button startBtn, autoBtn, clearBtn;
    private AnimationTimer measurementTimer;
    private long startTime;
    private double measuredTau;
    private double simulatedDuration = 3.5;
    private boolean isMeasuring = false;
    private Queue<Double> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    /*
     * Лабораторна робота № 7-2 "В'язкість газів".
     * Функція: LabController72.
     * Призначення: Конструктор класу, ініціалізує інтерфейс та налаштування.
     */
    public LabController72() {
        initUI();
        updatePhysics();
    }

    /*
     * Лабораторна робота № 7-2 "В'язкість газів".
     * Функція: shutdown.
     * Призначення: Зупиняє процеси та очищує ресурси при закритті вікна.
     */
    @Override
    public void shutdown() {
        if (measurementTimer != null) measurementTimer.stop();
        isAutoRunning = false;
        autoQueue.clear();
    }

    /*
     * Лабораторна робота № 7-2 "В'язкість газів".
     * Функція: initUI.
     * Призначення: Ініціалізує графічний інтерфейс користувача, створює панелі управління та графіки.
     */
    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Панель управління (Лаб 7-2)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane constPane = new TitledPane();
        constPane.setText("Константи та параметри");
        constPane.setCollapsible(true);
        VBox constBox = new VBox(12);
        constBox.setPadding(new Insets(5));

        fieldRho = new TextField("1000.0");
        fieldG = new TextField("9.81");
        fieldPatm = new TextField("101325.0");
        fieldR = new TextField("8.31");
        fieldMu = new TextField("0.029");
        fieldS = new TextField("0.005");

        fieldRho.textProperty().addListener((o, old, val) -> updatePhysics());
        fieldG.textProperty().addListener((o, old, val) -> updatePhysics());
        fieldPatm.textProperty().addListener((o, old, val) -> updatePhysics());
        fieldR.textProperty().addListener((o, old, val) -> updatePhysics());
        fieldMu.textProperty().addListener((o, old, val) -> updatePhysics());
        fieldS.textProperty().addListener((o, old, val) -> updatePhysics());

        constBox.getChildren().addAll(
                createInputGroup("ρ води (кг/м³):", fieldRho),
                createInputGroup("g (м/с²):", fieldG),
                createInputGroup("P атм (Па):", fieldPatm),
                createInputGroup("R (Дж/моль·К):", fieldR),
                createInputGroup("μ повітря (кг/моль):", fieldMu),
                createInputGroup("Площа посудини S (м²):", fieldS)
        );
        constPane.setContent(constBox);

        TitledPane settingsPane = new TitledPane();
        settingsPane.setText("Налаштування експерименту");
        settingsPane.setCollapsible(false);
        VBox settingsBox = new VBox(12);
        settingsBox.setPadding(new Insets(5));

        fieldRadius = new TextField("0.20");
        fieldLength = new TextField("10.0");
        fieldTemp = new TextField("293.0");
        fieldVolume = new TextField("50.0");

        fieldRadius.textProperty().addListener((o, old, val) -> updatePhysics());
        fieldLength.textProperty().addListener((o, old, val) -> updatePhysics());
        fieldTemp.textProperty().addListener((o, old, val) -> updatePhysics());
        fieldVolume.textProperty().addListener((o, old, val) -> updatePhysics());

        settingsBox.getChildren().addAll(
                createInputGroup("Радіус капіляра r (мм):", fieldRadius),
                createInputGroup("Довжина капіляра L (см):", fieldLength),
                createInputGroup("Температура T (К):", fieldTemp),
                createInputGroup("Об'єм витікання V (см³):", fieldVolume)
        );

        settingsPane.setContent(settingsBox);

        startBtn = new Button("▶ ВІДКРИТИ КРАН (Замір)");
        startBtn.setStyle("-fx-background-color: #0288d1; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAuto());

        clearBtn = new Button("🗑 ОЧИСТИТИ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear();
            idCounter = 1;
            updateConclusions();
            if (canvas != null) canvas.updateState(0, 0, 0);
        });

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, constPane, settingsPane, startBtn, autoBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new MariotteCanvas(600, 400);
        canvas.startAnimation();

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));
        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        StackPane centerPanel = new StackPane(canvas, topBar);
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);
        table.getColumns().addAll(
                createCol("V (см³)", "volume"),
                createCol("h1 (м)", "h1"),
                createCol("h2 (м)", "h2"),
                createCol("Δp (Па)", "deltaP"),
                createCol("τ (с)", "tau"),
                createCol("T (К)", "temp"),
                createCol("η (мкПа·с)", "eta"),
                createCol("λ (нм)", "lambda")
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();

        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);
    }

    private TableColumn<Measurement, Double> createCol(String title, String property) {
        TableColumn<Measurement, Double> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }

    /*
     * Лабораторна робота № 7-2 "В'язкість газів".
     * Функція: getDoubleValue.
     * Призначення: Повертає необхідне значення параметра або об'єкта.
     */
    private double getDoubleValue(TextField field, double defaultValue) {
        try {
            return Double.parseDouble(field.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /*
     * Лабораторна робота № 7-2 "В'язкість газів".
     * Функція: updatePhysics.
     * Призначення: Оновлює дані, статистику або графічний інтерфейс.
     */
    private void updatePhysics() {
        if (canvas != null && !isMeasuring) {
            canvas.updateState(0, 0, 0);
        }
    }

    /*
     * Лабораторна робота № 7-2 "В'язкість газів".
     * Функція: startManual.
     * Призначення: Запускає процес симуляції або відповідний режим роботи.
     */
    private void startManual() {
        isAutoRunning = false;
        double currentVolume = getDoubleValue(fieldVolume, 50.0);
        runSimulation(currentVolume);
    }

    /*
     * Лабораторна робота № 7-2 "В'язкість газів".
     * Функція: startAuto.
     * Призначення: Запускає процес симуляції або відповідний режим роботи.
     */
    private void startAuto() {
        data.clear(); idCounter = 1;
        autoQueue.clear();
        autoQueue.add(40.0); autoQueue.add(50.0); autoQueue.add(60.0);
        isAutoRunning = true;
        processNextAuto();
    }

    /*
     * Лабораторна робота № 7-2 "В'язкість газів".
     * Функція: processNextAuto.
     * Призначення: Обробляє поточний крок симуляції або дію користувача.
     */
    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoRunning = false;
            setControlsDisable(false);
            return;
        }
        double nextV = autoQueue.poll();
        fieldVolume.setText(String.valueOf(nextV));
        runSimulation(nextV);
    }

    /*
     * Лабораторна робота № 7-2 "В'язкість газів".
     * Функція: runSimulation.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void runSimulation(double v_cm3) {
        try {
            isMeasuring = true;
            setControlsDisable(true);
            canvas.setMeasuring(true);

            double rho = getDoubleValue(fieldRho, 1000.0);
            double g = getDoubleValue(fieldG, 9.81);
            double S = getDoubleValue(fieldS, 0.005);

            double r_m = getDoubleValue(fieldRadius, 0.20) * 1e-3;
            double L_m = getDoubleValue(fieldLength, 10.0) * 1e-2;
            double T = getDoubleValue(fieldTemp, 293.0);
            double V_m3 = v_cm3 * 1e-6;

            double h1 = 0.25;
            double h2 = h1 - (V_m3 / S);
            double deltaP = rho * g * ((h1 + h2) / 2.0);

            double eta_true = 1.7e-5 * Math.pow(T / 273.15, 0.76);
            double exactTau = (8.0 * eta_true * V_m3 * L_m) / (Math.PI * Math.pow(r_m, 4) * deltaP);
            measuredTau = exactTau + (Math.random() - 0.5) * 0.5;

            double volumeDropPct = (V_m3 / S) / 0.3;
            startTime = System.nanoTime();

            measurementTimer = new AnimationTimer() {
    /*
     * Лабораторна робота № 7-2 "В'язкість газів".
     * Функція: handle.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
                @Override
                public void handle(long now) {
                    double elapsed = (now - startTime) / 1_000_000_000.0;
                    double animProgress = elapsed / simulatedDuration;
                    if (animProgress >= 1.0) {
                        animProgress = 1.0;
                        this.stop();
                        finishMeasurement(v_cm3, h1, h2, deltaP, T, volumeDropPct, eta_true);
                    }
                    canvas.updateState(animProgress, animProgress * measuredTau, volumeDropPct);
                }
            };
            measurementTimer.start();
        } catch (Exception e) {
            isMeasuring = false;
            setControlsDisable(false);
        }
    }

    /*
     * Лабораторна робота № 7-2 "В'язкість газів".
     * Функція: finishMeasurement.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void finishMeasurement(double V_cm3, double h1, double h2, double deltaP, double T, double dropPct, double eta_true) {
        isMeasuring = false;
        canvas.setMeasuring(false);
        canvas.updateState(1.0, measuredTau, dropPct);
        try {
            double r_m = getDoubleValue(fieldRadius, 0.20) * 1e-3;
            double L_m = getDoubleValue(fieldLength, 10.0) * 1e-2;
            double V_m3 = V_cm3 * 1e-6;

            double Patm = getDoubleValue(fieldPatm, 101325.0);
            double R = getDoubleValue(fieldR, 8.31);
            double Mu = getDoubleValue(fieldMu, 0.029);

            double calcEta = (Math.PI * Math.pow(r_m, 4) * deltaP * measuredTau) / (8.0 * V_m3 * L_m);
            double calcLambda = 1.86 * (calcEta / Patm) * Math.sqrt((R * T) / Mu);

            double errEta = Math.abs(calcEta - eta_true) / eta_true * 100.0;

            Measurement m = new Measurement(idCounter++, V_cm3,
                    Math.round(h1*1000)/1000.0,
                    Math.round(h2*1000)/1000.0,
                    Math.round(deltaP*10)/10.0,
                    Math.round(measuredTau*10)/10.0,
                    T,
                    Math.round((calcEta * 1e6)*10)/10.0,
                    Math.round((calcLambda * 1e9)*10)/10.0);

            data.add(m);
            updateConclusions(eta_true);

        } catch (Exception ignored) {}

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(1000); Platform.runLater(this::processNextAuto); } catch (Exception ignored) {}
            }).start();
        } else {
            setControlsDisable(false);
        }
    }

    /*
     * Лабораторна робота № 7-2 "В'язкість газів".
     * Функція: updateConclusions.
     * Призначення: Оновлює дані, статистику або графічний інтерфейс.
     */
    private void updateConclusions() {
        updateConclusions(1.81e-5);
    }

    /*
     * Лабораторна робота № 7-2 "В'язкість газів".
     * Функція: updateConclusions.
     * Призначення: Оновлює дані, статистику або графічний інтерфейс.
     */
    private void updateConclusions(double etaTrue) {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: Очікування вимірювань...");
            return;
        }
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }
        double sumEta = 0;
        double sumLam = 0;
        for (Measurement m : data) {
            sumEta += m.getEta();
            sumLam += m.getLambda();
        }

        double avgEta = sumEta / data.size();
        double avgLam = sumLam / data.size();

        double absErr = Math.abs((avgEta * 1e-6) - etaTrue);
        double relErr = (absErr / etaTrue) * 100.0;

        StringBuilder sb = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ:\n");
        sb.append(String.format("1. Обчислене середнє значення Δр: %.1f Па.\n", data.get(data.size()-1).getDeltaP()));
        sb.append(String.format("2. Середній коефіцієнт внутрішнього тертя η: %.2f мкПа·с.\n", avgEta));
        sb.append(String.format("3. Середня довжина вільного пробігу молекул λ: %.2f нм.\n", avgLam));
        sb.append(String.format("4. Похибки: Абсолютна Δη = %.2e Па·с, Відносна ε = %.1f %%.\n", absErr, relErr));
        sb.append("5. Висновок: Експериментальні значення в'язкості та довжини вільного пробігу молекул добре узгоджуються з молекулярно-кінетичною теорією газів.");

        finalResultLabel.setText(sb.toString());
    }

    /*
     * Лабораторна робота № 7-2 "В'язкість газів".
     * Функція: setControlsDisable.
     * Призначення: Встановлює нове значення для вказаного параметра.
     */
    private void setControlsDisable(boolean disable) {
        fieldRadius.setDisable(disable);
        fieldLength.setDisable(disable);
        fieldTemp.setDisable(disable);
        fieldVolume.setDisable(disable);
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        fieldRho.setDisable(disable);
        fieldG.setDisable(disable);
        fieldPatm.setDisable(disable);
        fieldR.setDisable(disable);
        fieldMu.setDisable(disable);
        fieldS.setDisable(disable);
    }
}