package dev.ua._klaidi4_.physics.level3.lab3_3.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level3.lab3_3.model.Measurement;
import dev.ua._klaidi4_.physics.level3.lab3_3.view.FocusingCanvas;
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

public class LabController33 extends BaseLabController {

    private FocusingCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField turnsField;
    private TextField lengthField;
    private TextField voltageField;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveTimeLabel;
    private Label liveCurrentLabel;
    private AnimationTimer measurementTimer;
    private long startTime;
    private double targetTime = 3.0;
    private double currentSimI = 0;
    private Queue<Double> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    private final double MU_0 = 4 * Math.PI * 1e-7;
    private final double EM_THEORY = 1.7588e11;

    public LabController33() {
        initUI();
        applyPhysicsSettings();
    }

    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        if (measurementTimer != null) measurementTimer.stop();
        isAutoRunning = false;
        autoQueue.clear();
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 3-3)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри установки");
        labPane.setCollapsible(false);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        turnsField = new TextField("2000");
        lengthField = new TextField("0.15");
        voltageField = new TextField("1000");

        paramsBox.getChildren().addAll(
                createInputGroup("Кількість витків соленоїда n (1/м):", turnsField),
                createInputGroup("Відстань до екрану l (м):", lengthField),
                createInputGroup("Анодна напруга U (В):", voltageField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(200);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        startBtn = new Button("▶ ФОКУСУВАТИ (РУЧНИЙ)");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

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
            liveCurrentLabel.setText("I = 0.000 А");
            canvas.setPhysicsParameters(1000, 0, 2000, 0.15);
        });

        leftPanel.getChildren().addAll(title, labPane, startBtn, autoBtn, clearBtn);

        canvas = new FocusingCanvas(600, 440);

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
        Label dashTitle = new Label("ПАНЕЛЬ ФОКУСУВАННЯ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveTimeLabel = new Label("t = 0.00 с");
        liveTimeLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        liveTimeLabel.setStyle("-fx-text-fill: #00ff00;");
        liveCurrentLabel = new Label("I = 0.000 А");
        liveCurrentLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveTimeLabel, liveCurrentLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, Double> uCol = new TableColumn<>("U (В)");
        uCol.setCellValueFactory(new PropertyValueFactory<>("voltage"));
        TableColumn<Measurement, Double> iCol = new TableColumn<>("I (А)");
        iCol.setCellValueFactory(new PropertyValueFactory<>("current"));
        TableColumn<Measurement, Double> eExpCol = new TableColumn<>("e/m експ");
        eExpCol.setCellValueFactory(new PropertyValueFactory<>("expEm"));
        TableColumn<Measurement, Double> eTheoCol = new TableColumn<>("e/m теор");
        eTheoCol.setCellValueFactory(new PropertyValueFactory<>("theoEm"));
        TableColumn<Measurement, Double> errCol = new TableColumn<>("ε (%)");
        errCol.setCellValueFactory(new PropertyValueFactory<>("errorPercent"));

        table.getColumns().addAll(idCol, uCol, iCol, eExpCol, eTheoCol, errCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);
    }

    private void applyPhysicsSettings() {
        try {
            double u = Double.parseDouble(voltageField.getText());
            double n = Double.parseDouble(turnsField.getText());
            double l = Double.parseDouble(lengthField.getText());
            canvas.setPhysicsParameters(u, 0, n, l);
        } catch (NumberFormatException ignored) {}
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        turnsField.setDisable(disable);
        lengthField.setDisable(disable);
        voltageField.setDisable(disable);
    }

    private void startManual() {
        try {
            double u = Double.parseDouble(voltageField.getText());
            isAutoRunning = false;
            runMeasurement(u);
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа в поля.");
        }
    }

    private void startAuto() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        autoQueue.add(600.0);
        autoQueue.add(800.0);
        autoQueue.add(1000.0);
        autoQueue.add(1200.0);

        isAutoRunning = true;
        processNextAuto();
    }

    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoRunning = false;
            liveStatusLabel.setText("СИСТЕМА: ГОТОВО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            setControlsDisable(false);
            return;
        }

        double nextVoltage = autoQueue.poll();
        voltageField.setText(String.valueOf(nextVoltage));
        runMeasurement(nextVoltage);
    }

    private void runMeasurement(double voltage) {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: ПІДБІР СТРУМУ");
        liveStatusLabel.setStyle("-fx-text-fill: red;");

        double n = Double.parseDouble(turnsField.getText());
        double l = Double.parseDouble(lengthField.getText());
        double piSquared = Math.PI * Math.PI;
        double muSquared = MU_0 * MU_0;
        double exactISquared = (8 * piSquared * voltage) / (l * l * muSquared * n * n * EM_THEORY);
        double exactI = Math.sqrt(exactISquared);

        final double finalTargetI = exactI * (1.0 + (Math.random() - 0.5) * 0.03);

        startTime = System.nanoTime();

        measurementTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double elapsed = (now - startTime) / 1_000_000_000.0;

                double progress = Math.min(elapsed / targetTime, 1.0);
                currentSimI = finalTargetI * progress;

                liveTimeLabel.setText(String.format("t = %.2f с", elapsed));
                liveCurrentLabel.setText(String.format("I = %.3f А", currentSimI));

                canvas.setPhysicsParameters(voltage, currentSimI, n, l);

                if (elapsed >= targetTime) {
                    this.stop();
                    finishMeasurement(voltage, currentSimI, n, l);
                }
            }
        };
        measurementTimer.start();
    }

    private void finishMeasurement(double u, double finalI, double n, double l) {
        liveStatusLabel.setText("Статус: ЗАФОКУСОВАНО");
        liveStatusLabel.setStyle("-fx-text-fill: yellow;");

        double piSquared = Math.PI * Math.PI;
        double muSquared = MU_0 * MU_0;
        double expEm = (8 * piSquared * u) / (muSquared * n * n * finalI * finalI * l * l);
        double error = Math.abs(expEm - EM_THEORY) / EM_THEORY * 100.0;

        Measurement m = new Measurement(
                idCounter++,
                Math.round(u * 10.0) / 10.0,
                Math.round(finalI * 1000.0) / 1000.0,
                expEm,
                EM_THEORY,
                Math.round(error * 100.0) / 100.0
        );
        data.add(m);
        updateStats();

        if (isAutoRunning) {
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    Platform.runLater(this::processNextAuto);
                } catch (InterruptedException e) {}
            }).start();
        } else {
            setControlsDisable(false);
        }
    }

    private void updateStats() {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: -");
            return;
        }
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }

        double sumExp = 0;
        for (Measurement m : data) {
            sumExp += m.getExpEm();
        }
        double avgExp = sumExp / data.size();
        double totalError = Math.abs(avgExp - EM_THEORY) / EM_THEORY * 100.0;

        String conclusion = String.format(
                "ОБРОБКА РЕЗУЛЬТАТІВ:\n" +
                        "1. Проведено вимірювань: %d шт.\n" +
                        "2. Середнє експериментальне значення: e/m = %.4e Кл/кг.\n" +
                        "3. Теоретичне значення (табличне): e/m = %.4e Кл/кг.\n" +
                        "4. Середня відносна похибка: ε = %.2f %%.\n" +
                        "ВИСНОВОК: За допомогою методу магнетного фокусування успішно визначено питомий заряд електрона. " +
                        "Результати співпадають з теоретичними в межах похибки.",
                data.size(), avgExp, EM_THEORY, totalError
        );

        finalResultLabel.setText(conclusion);
    }
}