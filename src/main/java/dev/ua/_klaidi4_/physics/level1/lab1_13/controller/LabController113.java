package dev.ua._klaidi4_.physics.level1.lab1_13.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level1.lab1_13.model.Measurement;
import dev.ua._klaidi4_.physics.level1.lab1_13.view.InclinedPendulumCanvas;
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

public class LabController113 extends BaseLabController {

    private InclinedPendulumCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField betaField;
    private TextField rField;
    private TextField a0Field;
    private TextField nField;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveOscLabel;
    private Queue<Double> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    private final double F_THEO = 0.00005;
    private final double EXACT_PERIOD = 1.5;

    public LabController113() {
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

        Label title = new Label("Система управління (Лаб 1-13)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри установки");
        labPane.setCollapsible(false);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        betaField = new TextField("60");
        rField = new TextField("0.02");
        a0Field = new TextField("5.0");
        nField = new TextField("10");

        paramsBox.getChildren().addAll(
                createInputGroup("Кут нахилу площини β (°):", betaField),
                createInputGroup("Радіус кульки r (м):", rField),
                createInputGroup("Початковий кут відхилення α0 (°):", a0Field),
                createInputGroup("Кількість коливань n:", nField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(260);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        startBtn = new Button("▶ ЗАПУСТИТИ КОЛИВАННЯ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ СЕРІЯ ВИМІРІВ (9 дослідів)");
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
            liveOscLabel.setText("n = 0");
        });

        leftPanel.getChildren().addAll(title, labPane, startBtn, autoBtn, clearBtn);

        canvas = new InclinedPendulumCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(200, 60);
        Label dashTitle = new Label("ДАТЧИК КОЛИВАНЬ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveOscLabel = new Label("n = 0");
        liveOscLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        liveOscLabel.setStyle("-fx-text-fill: #00ff00; -fx-transition: all 0.2s ease;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveOscLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, Double> betaCol = new TableColumn<>("β (°)");
        betaCol.setCellValueFactory(new PropertyValueFactory<>("beta"));
        TableColumn<Measurement, Double> a0Col = new TableColumn<>("α0 (°)");
        a0Col.setCellValueFactory(new PropertyValueFactory<>("a0"));
        TableColumn<Measurement, Integer> nCol = new TableColumn<>("n");
        nCol.setCellValueFactory(new PropertyValueFactory<>("n"));
        TableColumn<Measurement, Double> anCol = new TableColumn<>("α_n (°)");
        anCol.setCellValueFactory(new PropertyValueFactory<>("an"));
        TableColumn<Measurement, Double> fCol = new TableColumn<>("f (м)");
        fCol.setCellValueFactory(new PropertyValueFactory<>("f"));

        table.getColumns().addAll(idCol, betaCol, a0Col, nCol, anCol, fCol);
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
        betaField.setDisable(disable);
        rField.setDisable(disable);
        a0Field.setDisable(disable);
        nField.setDisable(disable);
    }

    private void startManual() {
        try {
            Double.parseDouble(betaField.getText());
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

        autoQueue.add(60.0); autoQueue.add(60.0); autoQueue.add(60.0);
        autoQueue.add(45.0); autoQueue.add(45.0); autoQueue.add(45.0);
        autoQueue.add(30.0); autoQueue.add(30.0); autoQueue.add(30.0);

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
        betaField.setText(String.valueOf(autoQueue.poll()));
        runSimulation();
    }

    private void runSimulation() {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: КОЛИВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: cyan;");

        double betaDeg = Double.parseDouble(betaField.getText());
        double r = Double.parseDouble(rField.getText());
        double a0Deg = Double.parseDouble(a0Field.getText());
        int targetOsc = Integer.parseInt(nField.getText());

        liveOscLabel.setText("n = 0");

        double betaRad = Math.toRadians(betaDeg);
        double a0Rad = Math.toRadians(a0Deg);
        double decayRad = (F_THEO * 4 * targetOsc) / (r * Math.tan(betaRad));

        double exactAnRad = a0Rad - decayRad;
        if (exactAnRad < 0) exactAnRad = 0;

        double noiseRad = Math.toRadians((Math.random() - 0.5) * 0.4);
        final double finalAnRad = exactAnRad + noiseRad;
        double finalAnDeg = Math.toDegrees(finalAnRad);
        if (finalAnDeg < 0) finalAnDeg = 0;

        final double passAnDeg = finalAnDeg;

        canvas.setOnFinishCallback(() -> Platform.runLater(() -> finishMeasurement(passAnDeg, targetOsc)));
        canvas.startSimulation(a0Deg, finalAnDeg, targetOsc);

        new Thread(() -> {
            long start = System.currentTimeMillis();
            long targetMs = (long) (targetOsc * EXACT_PERIOD * 1000);
            int[] currentOsc = {0};

            while (System.currentTimeMillis() - start < targetMs) {
                long elapsed = System.currentTimeMillis() - start;
                int newOsc = (int) ((elapsed / 1000.0) / EXACT_PERIOD);

                if (newOsc > currentOsc[0] && newOsc <= targetOsc) {
                    currentOsc[0] = newOsc;
                    final int displayOsc = currentOsc[0];

                    Platform.runLater(() -> {
                        liveOscLabel.setText("n = " + displayOsc);
                        liveOscLabel.setStyle("-fx-text-fill: #2ecc71; -fx-scale-x: 1.4; -fx-scale-y: 1.4;");

                        new Thread(() -> {
                            try { Thread.sleep(200); } catch (Exception ignored) {}
                            Platform.runLater(() -> liveOscLabel.setStyle("-fx-text-fill: #00ff00; -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));
                        }).start();
                    });
                }

                try { Thread.sleep(30); } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    private void finishMeasurement(double anDeg, int targetOsc) {
        liveOscLabel.setText("n = " + targetOsc);
        liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

        double betaDeg = Double.parseDouble(betaField.getText());
        double r = Double.parseDouble(rField.getText());
        double a0Deg = Double.parseDouble(a0Field.getText());

        double betaRad = Math.toRadians(betaDeg);
        double a0Rad = Math.toRadians(a0Deg);
        double anRad = Math.toRadians(anDeg);

        double fExp = (r * Math.tan(betaRad) * (a0Rad - anRad)) / (4.0 * targetOsc);
        if (fExp < 0) fExp = 0;

        Measurement meas = new Measurement(
                idCounter++, betaDeg, a0Deg, targetOsc,
                Math.round(anDeg * 100.0) / 100.0,
                Math.round(fExp * 1000000.0) / 1000000.0
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

        Map<Double, List<Double>> groupedF = new TreeMap<>(Collections.reverseOrder());
        double totalSumF = 0;

        for (Measurement m : data) {
            groupedF.computeIfAbsent(m.getBeta(), k -> new ArrayList<>()).add(m.getF());
            totalSumF += m.getF();
        }

        StringBuilder step2 = new StringBuilder("2. Середні значення f при фіксованих кутах нахилу:\n");
        for (Map.Entry<Double, List<Double>> entry : groupedF.entrySet()) {
            double sum = 0;
            for (Double v : entry.getValue()) sum += v;
            double avg = sum / entry.getValue().size();
            step2.append(String.format("   β = %.1f° -> f_ср = %.6f м\n", entry.getKey(), avg));
        }

        double avgTotalF = totalSumF / data.size();

        double sumDelta = 0;
        for (Measurement m : data) {
            sumDelta += Math.abs(m.getF() - avgTotalF);
        }
        double avgDelta = sumDelta / data.size();
        double eps = (avgTotalF != 0) ? (avgDelta / avgTotalF) * 100 : 0;

        String conclusion = String.format(
                "ОБРОБКА РЕЗУЛЬТАТІВ ЕКСПЕРИМЕНТУ ТА ЇХ АНАЛІЗ:\n" +
                        "1. За формулою (16) вирахувано коефіцієнт f для кожного вимірювання (див. таблицю).\n" +
                        "%s" +
                        "3. Загальне середнє значення f_ср = %.6f м.\n" +
                        "   Абсолютна похибка: Δf = %.6f м. Відносна похибка: ε = %.1f %%.\n" +
                        "4. ВИСНОВОК: Позитивною стороною методу похилого маятника є можливість багаторазового проходження " +
                        "шляху кульки (накопичення втрат), що дозволяє фіксувати дуже мале тертя кочення. Недоліком є " +
                        "складність точного візуального відліку малих кінцевих кутів (an), що формує основну похибку.",
                step2.toString(), avgTotalF, avgDelta, eps
        );

        finalResultLabel.setText(conclusion);
    }
}