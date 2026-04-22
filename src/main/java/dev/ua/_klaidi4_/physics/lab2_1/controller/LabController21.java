package dev.ua._klaidi4_.physics.lab2_1.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.lab2_1.model.Measurement;
import dev.ua._klaidi4_.physics.lab2_1.view.ElectrostaticCanvas;
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

import java.util.*;

public class LabController21 extends BaseLabController {

    private ElectrostaticCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;

    private TextField uField;
    private TextField phiField;
    private TextField dField;
    private TextField rField;

    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;

    private Label liveStatusLabel;
    private Label liveCurrentLabel;

    private Queue<Double> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    public LabController21() {
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

        Label title = new Label("Система управління (Лаб 2-1)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри установки");
        labPane.setCollapsible(false);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        uField = new TextField("10.0");
        phiField = new TextField("8.0");
        dField = new TextField("0.20");
        rField = new TextField("0.02");

        paramsBox.getChildren().addAll(
                createInputGroup("Напруга між електродами U (В):", uField),
                createInputGroup("Шуканий потенціал φ (В):", phiField),
                createInputGroup("Відстань між електродами D (м):", dField),
                createInputGroup("Радіус електродів r (м):", rField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(260);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        startBtn = new Button("▶ ПОШУК ЛІНІЇ (8 точок)");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ СЕРІЯ ВИМІРІВ (3 лінії)");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAuto());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear();
            idCounter = 1;
            canvas.clearCanvas();
            updateStats();
        });

        leftPanel.getChildren().addAll(title, labPane, startBtn, autoBtn, clearBtn);

        canvas = new ElectrostaticCanvas(600, 440);

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
        Label dashTitle = new Label("МІКРОАМПЕРМЕТР");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveCurrentLabel = new Label("Струм: 0 мкА");
        liveCurrentLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        liveCurrentLabel.setStyle("-fx-text-fill: #00ff00;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveCurrentLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, Double> phiCol = new TableColumn<>("φ (В)");
        phiCol.setCellValueFactory(new PropertyValueFactory<>("phi"));
        TableColumn<Measurement, Integer> segCol = new TableColumn<>("Відрізок");
        segCol.setCellValueFactory(new PropertyValueFactory<>("segment"));
        TableColumn<Measurement, Double> drCol = new TableColumn<>("Δr (м)");
        drCol.setCellValueFactory(new PropertyValueFactory<>("deltaR"));
        TableColumn<Measurement, Double> dlCol = new TableColumn<>("Δl (м)");
        dlCol.setCellValueFactory(new PropertyValueFactory<>("deltaL"));
        TableColumn<Measurement, Double> eCol = new TableColumn<>("E (В/м)");
        eCol.setCellValueFactory(new PropertyValueFactory<>("eField"));

        table.getColumns().addAll(idCol, phiCol, segCol, drCol, dlCol, eCol);
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
        uField.setDisable(disable);
        phiField.setDisable(disable);
        dField.setDisable(disable);
        rField.setDisable(disable);
    }

    private void startManual() {
        try {
            double phi = Double.parseDouble(phiField.getText());
            double u = Double.parseDouble(uField.getText());
            if (phi >= u || phi <= 0) {
                showAlert("Помилка", "Потенціал φ повинен бути більшим за 0 і меншим за U!");
                return;
            }
            isAutoRunning = false;
            runSimulation(phi, u);
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа в поля.");
        }
    }

    private void startAuto() {
        data.clear();
        idCounter = 1;
        canvas.clearCanvas();
        updateStats();
        autoQueue.clear();

        double u = Double.parseDouble(uField.getText());
        autoQueue.add(u * 0.8);
        autoQueue.add(u * 0.6);
        autoQueue.add(u * 0.4);

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
        double nextPhi = autoQueue.poll();
        phiField.setText(String.format(java.util.Locale.US, "%.1f", nextPhi));
        double u = Double.parseDouble(uField.getText());
        runSimulation(nextPhi, u);
    }

    private void runSimulation(double phi, double u) {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: СКАНУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: cyan;");

        final double deltaPhi = u - phi;

        canvas.setOnFinishCallback(() -> Platform.runLater(() -> finishMeasurement(phi, u, deltaPhi)));
        canvas.startSimulation(phi, u);

        new Thread(() -> {
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 2000) {
                Platform.runLater(() -> {
                    if (Math.random() > 0.7) liveCurrentLabel.setText("Струм: 0.0 мкА");
                    else liveCurrentLabel.setText(String.format("Струм: %.1f мкА", (Math.random() - 0.5) * 5));
                });
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }
            Platform.runLater(() -> liveCurrentLabel.setText("Струм: 0.0 мкА"));
        }).start();
    }

    private void finishMeasurement(double phi, double u, double deltaPhi) {
        liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

        // Генеруємо 8 відрізків
        double k = 1.37 * (u / deltaPhi);
        double targetRatioPerSegment = k / 8.0;

        for (int i = 1; i <= 8; i++) {
            double angleRad = i * (Math.PI / 4.0);

            double dr = 0.01 * deltaPhi * (1.0 - 0.2 * Math.cos(angleRad));
            dr *= 1.0 + (Math.random() - 0.5) * 0.1;

            double dl = targetRatioPerSegment * dr * (1.0 + (Math.random() - 0.5) * 0.05);

            // Напруженість: E = Δφ / Δr
            double eField = deltaPhi / dr;

            Measurement meas = new Measurement(
                    idCounter++,
                    Math.round(phi * 10.0) / 10.0,
                    i,
                    Math.round(dr * 10000.0) / 10000.0,
                    Math.round(dl * 10000.0) / 10000.0,
                    Math.round(eField * 10.0) / 10.0
            );
            data.add(meas);
        }

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

    private void updateStats() {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: -");
            return;
        }

        double u = Double.parseDouble(uField.getText());
        double d = Double.parseDouble(dField.getText());
        double rElec = Double.parseDouble(rField.getText());

        double cTheo = (Math.PI * 8.85e-12) / Math.log(d / rElec);

        Map<Double, Double> sumRatios = new TreeMap<>(Collections.reverseOrder());
        for (Measurement m : data) {
            double ratio = m.getDeltaL() / m.getDeltaR();
            sumRatios.put(m.getPhi(), sumRatios.getOrDefault(m.getPhi(), 0.0) + ratio);
        }

        double sumCexp = 0;
        StringBuilder sb = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ ТА ЇХ АНАЛІЗ:\n");
        sb.append("1-2. За формулою (3) розраховано напруженість E = Δφ / Δr для кожного відрізка (див. таблицю).\n");
        sb.append("3. Розрахунок погонної ємності Ch (за формулою 5) для кожної еквіпотенціальної лінії:\n");

        for (Map.Entry<Double, Double> entry : sumRatios.entrySet()) {
            double phi1 = entry.getKey();
            double sumRatio = entry.getValue();
            // C = eps0 * ((U - phi) / U) * Sum(dl/dr)
            double cExp = 8.85e-12 * ((u - phi1) / u) * sumRatio;
            sumCexp += cExp;
            sb.append(String.format("   Лінія φ = %.1f В -> Σ(Δl/Δr) = %.2f -> Ch_експ = %.2f пФ/м\n",
                    phi1, sumRatio, cExp * 1e12));
        }

        double avgCexp = sumCexp / sumRatios.size();
        double error = Math.abs(avgCexp - cTheo) / cTheo * 100.0;

        sb.append(String.format("\n4. Середня експ. ємність: Ch_експ = %.2f пФ/м.\n", avgCexp * 1e12));
        sb.append(String.format("5. Теоретична ємність лінії: Ch_теор = %.2f пФ/м.\n", cTheo * 1e12));
        sb.append(String.format("ВИСНОВОК: Відносна похибка методу ε = %.1f %%. Метод моделювання дозволяє досить точно досліджувати складні поля.", error));

        finalResultLabel.setText(sb.toString());
    }
}