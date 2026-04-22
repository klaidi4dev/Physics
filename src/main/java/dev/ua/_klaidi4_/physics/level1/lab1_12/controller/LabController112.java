package dev.ua._klaidi4_.physics.level1.lab1_12.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level1.lab1_12.model.Measurement;
import dev.ua._klaidi4_.physics.level1.lab1_12.view.GyroscopeCanvas;
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

public class LabController112 extends BaseLabController {

    private GyroscopeCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField mField;
    private TextField lField;
    private TextField freqField;
    private TextField revsField;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveTimeLabel;
    private Queue<Double> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;
    private final double M_ROTOR = 0.8;
    private final double R_ROTOR = 0.05;

    public LabController112() {
        initUI();
    }

    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        isAutoRunning = false;
        autoQueue.clear();
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 1-12)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри установки");
        labPane.setCollapsible(false);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        mField = new TextField("0.050");
        lField = new TextField("0.15");
        freqField = new TextField("60");
        revsField = new TextField("1");

        paramsBox.getChildren().addAll(
                createInputGroup("Маса тягарця m (кг):", mField),
                createInputGroup("Плече сили тягарця l (м):", lField),
                createInputGroup("Частота ротора ν (Гц):", freqField),
                createInputGroup("Кількість обертів прецесії N:", revsField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(260);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        startBtn = new Button("▶ ЗАПУСТИТИ ПРЕЦЕСІЮ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ (5 мас)");
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

        leftPanel.getChildren().addAll(title, labPane, startBtn, autoBtn, clearBtn);

        canvas = new GyroscopeCanvas(600, 440);

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
        Label dashTitle = new Label("СЕКУНДОМІР");
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
        TableColumn<Measurement, Double> mCol = new TableColumn<>("m (кг)");
        mCol.setCellValueFactory(new PropertyValueFactory<>("m"));
        TableColumn<Measurement, Double> tCol = new TableColumn<>("t (с)");
        tCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        TableColumn<Measurement, Double> wpCol = new TableColumn<>("ω_p (рад/с)");
        wpCol.setCellValueFactory(new PropertyValueFactory<>("omegaP"));
        TableColumn<Measurement, Double> torCol = new TableColumn<>("M (Н·м)");
        torCol.setCellValueFactory(new PropertyValueFactory<>("torque"));
        TableColumn<Measurement, Double> momCol = new TableColumn<>("L (Дж·с)");
        momCol.setCellValueFactory(new PropertyValueFactory<>("momentum"));
        TableColumn<Measurement, Double> expCol = new TableColumn<>("I_експ");
        expCol.setCellValueFactory(new PropertyValueFactory<>("expI"));

        table.getColumns().addAll(idCol, mCol, tCol, wpCol, torCol, momCol, expCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        mField.setDisable(disable);
        lField.setDisable(disable);
        freqField.setDisable(disable);
        revsField.setDisable(disable);
    }

    private void startManual() {
        try {
            Double.parseDouble(mField.getText());
            isAutoRunning = false;
            runSimulation();
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа в поля.");
        }
    }

    private void startAuto() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        autoQueue.add(0.04);
        autoQueue.add(0.06);
        autoQueue.add(0.08);
        autoQueue.add(0.10);
        autoQueue.add(0.12);

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
        mField.setText(String.valueOf(autoQueue.poll()));
        runSimulation();
    }

    private void runSimulation() {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: ПРЕЦЕСІЯ");
        liveStatusLabel.setStyle("-fx-text-fill: cyan;");

        double m = Double.parseDouble(mField.getText());
        double l = Double.parseDouble(lField.getText());
        double freq = Double.parseDouble(freqField.getText());
        int revs = Integer.parseInt(revsField.getText());

        double iTheory = 0.5 * M_ROTOR * R_ROTOR * R_ROTOR;
        double Omega = 2 * Math.PI * freq;
        double torque = m * 9.81 * l;
        double exactOmegaP = torque / (iTheory * Omega);
        double exactTime = (revs * 2 * Math.PI) / exactOmegaP;
        final double finalMeasuredTime = exactTime + (Math.random() - 0.5) * 0.3;
        final int finalRevs = revs;
        final double finalITheory = iTheory;
        final double finalOmega = Omega;
        final double finalTorque = torque;

        canvas.setOnFinishCallback(() -> Platform.runLater(() ->
                finishMeasurement(finalMeasuredTime, finalRevs, finalTorque, finalOmega, finalITheory)
        ));
        canvas.startSimulation(finalMeasuredTime, finalRevs);

        new Thread(() -> {
            long start = System.currentTimeMillis();
            long targetMs = (long) (finalMeasuredTime * 1000);

            while (System.currentTimeMillis() - start < targetMs) {
                long elapsed = System.currentTimeMillis() - start;
                double seconds = elapsed / 1000.0;
                Platform.runLater(() -> liveTimeLabel.setText(String.format("t = %.3f с", seconds)));
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    private void finishMeasurement(double measuredTime, int revs, double torque, double Omega, double iTheory) {
        liveTimeLabel.setText(String.format("t = %.3f с", measuredTime));
        liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

        double m = Double.parseDouble(mField.getText());
        double phi = revs * 2 * Math.PI;
        double expOmegaP = phi / measuredTime;
        double expL = torque / expOmegaP;
        double expI = expL / Omega;

        Measurement meas = new Measurement(
                idCounter++, m,
                Math.round(measuredTime * 100.0) / 100.0,
                Math.round(expOmegaP * 10000.0) / 10000.0,
                Math.round(torque * 10000.0) / 10000.0,
                Math.round(expL * 10000.0) / 10000.0,
                Math.round(expI * 1000000.0) / 1000000.0,
                iTheory
        );
        data.add(meas);
        updateStats();

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(1200); Platform.runLater(this::processNextAuto); }
                catch (InterruptedException ignored) {}
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

        double sumL = 0;
        double sumI = 0;

        for (Measurement meas : data) {
            sumL += meas.getMomentum();
            sumI += meas.getExpI();
        }

        double avgL = sumL / data.size();
        double avgI = sumI / data.size();
        double theoI = data.get(0).getTheoI();
        double deltaI = Math.abs(avgI - theoI);
        double epsI = (deltaI / theoI) * 100;
        Measurement last = data.get(data.size() - 1);
        double freq = Double.parseDouble(freqField.getText());

        String conclusion = String.format(
                "ОБРОБКА РЕЗУЛЬТАТІВ ВИМІРЮВАНЬ:\n" +
                        "ЗАВДАННЯ 1: Кутова швидкість прецесії ω_p розрахована для кожної маси (див. табл.). " +
                        "Швидкість прецесії (ω_p ≈ %.3f рад/с) набагато менша за швидкість вала двигуна (Ω = %.1f рад/с).\n" +
                        "ЗАВДАННЯ 2: Момент сили M та момент імпульсу гіроскопа L розраховані за ф.(8) і (10). Середнє L = %.4f Дж·с.\n" +
                        "ЗАВДАННЯ 3: Середній експ. момент інерції I_експ = %.6f кг·м². Теоретичний I_теор = %.6f кг·м².\n" +
                        "ВИСНОВОК: Абсолютна похибка ΔI = %.6f кг·м², відносна ε = %.1f %%. Гіроскопічний ефект підтверджено.",
                last.getOmegaP(), (freq * 2 * Math.PI), avgL, avgI, theoI, deltaI, epsI
        );

        finalResultLabel.setText(conclusion);
    }
}