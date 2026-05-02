package dev.ua._klaidi4_.physics.level2.lab2_4.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level2.lab2_4.model.Measurement;
import dev.ua._klaidi4_.physics.level2.lab2_4.view.FerroelectricCanvas;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.LinkedList;
import java.util.Queue;

public class LabController24 extends BaseLabController {

    private FerroelectricCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField fieldD, fieldThickness, fieldFreq, fieldMaxTemp, fieldVoltage;
    private Button startBtn, autoBtn, clearBtn;
    private Label liveStatusLabel, liveTempLabel, liveCurrentLabel;
    private LineChart<Number, Number> chart;
    private XYChart.Series<Number, Number> dataSeries;
    private XYChart.Series<Number, Number> curiePointSeries;
    private Queue<Double> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;
    private final double EPSILON_0 = 8.85e-12;
    private double currentU = 120.0;
    private double currentTemp = 20.0;
    private double baseEpsilon = 0;
    private double baseCurrent = 0;

    public LabController24() {
        initUI();
    }

    @Override
    public void shutdown() {
        isAutoRunning = false;
        autoQueue.clear();
        if (canvas != null) canvas.resetSystem();
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(330);
        leftPanel.setMinWidth(330);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 2-4)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane paramsPane = new TitledPane();
        paramsPane.setText("Параметри зразка (BaTiO3)");
        paramsPane.setCollapsible(false);
        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(10));

        fieldD = new TextField("8.0");
        fieldThickness = new TextField("1.0");
        fieldFreq = new TextField("50.0");

        paramsBox.getChildren().addAll(
                createInputGroup("Діаметр D (мм):", fieldD),
                createInputGroup("Товщина d (мм):", fieldThickness),
                createInputGroup("Частота струму  (Гц):", fieldFreq)
        );
        paramsPane.setContent(paramsBox);

        TitledPane controlPane = new TitledPane();
        controlPane.setText("Управління установкою");
        controlPane.setCollapsible(false);
        VBox controlBox = new VBox(12);
        controlBox.setPadding(new Insets(10));

        fieldVoltage = new TextField("120.0");
        fieldVoltage.textProperty().addListener((o, old, val) -> {
            try {
                currentU = Double.parseDouble(val);
                calculateInitialValues();
            } catch (Exception ignored) {}
        });

        fieldMaxTemp = new TextField("140.0");

        controlBox.getChildren().addAll(
                createInputGroup("Робоча напруга U (В):", fieldVoltage),
                createInputGroup("Кінцева температура t_max (°C):", fieldMaxTemp)
        );
        controlPane.setContent(controlBox);

        startBtn = new Button("▶ ПОЧАТИ НАГРІВАННЯ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAuto());

        clearBtn = new Button("🗑 ОЧИСТИТИ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> resetAll());

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, paramsPane, controlPane, startBtn, autoBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new FerroelectricCanvas(400, 240);

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

        Label dashTitle = new Label("ДАТЧИКИ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ГОТОВО");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveTempLabel = new Label("t = 20.0 °C");
        liveTempLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
        liveCurrentLabel = new Label("Ic = 0.00 мкА");
        liveCurrentLabel.setStyle("-fx-text-fill: #00ff00;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveTempLabel, liveCurrentLabel);

        StackPane canvasStack = new StackPane(canvas, topBar, dash);
        canvasStack.setStyle("-fx-background-color: #ffffff;");
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Температура t (°C)");
        xAxis.setAutoRanging(true);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Відношення ε/ε1");
        yAxis.setAutoRanging(true);

        chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(true);
        chart.setCreateSymbols(true);
        chart.setPrefHeight(250);
        VBox.setVgrow(chart, Priority.ALWAYS);

        rebuildChartSeries();

        VBox centerTopPanel = new VBox(canvasStack, chart);

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        String[] cols = {"№", "t (°C)", "Ic (мкА)", "ε", "ε/ε1"};
        String[] props = {"id", "temperature", "currentIc", "epsilon", "ratio"};

        for (int i = 0; i < cols.length; i++) {
            TableColumn<Measurement, Object> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(new PropertyValueFactory<>(props[i]));
            table.getColumns().add(col);
        }
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));
        this.setLeft(leftPanel);
        this.setCenter(centerTopPanel);
        this.setBottom(bottomPanel);

        calculateInitialValues();
    }

    private void rebuildChartSeries() {
        chart.getData().clear();
        dataSeries = new XYChart.Series<>();
        dataSeries.setName("Залежність ε/ε1 = f(t)");
        curiePointSeries = new XYChart.Series<>();
        curiePointSeries.setName("Точка Кюрі");
        chart.getData().addAll(dataSeries, curiePointSeries);
    }

    private void resetAll() {
        data.clear();
        rebuildChartSeries();
        idCounter = 1;
        currentTemp = 20.0;
        canvas.resetSystem();
        updateStats();
        calculateInitialValues();
        liveStatusLabel.setText("Статус: ГОТОВО");
        liveStatusLabel.setStyle("-fx-text-fill: yellow;");
    }

    private void calculateInitialValues() {
        try {
            double d_m = Double.parseDouble(fieldD.getText()) / 1000.0;
            double thickness_m = Double.parseDouble(fieldThickness.getText()) / 1000.0;
            double freq = Double.parseDouble(fieldFreq.getText());
            double S = Math.PI * Math.pow(d_m / 2.0, 2);
            baseEpsilon = 1200;
            double omega = 2 * Math.PI * freq;
            double C1 = (EPSILON_0 * baseEpsilon * S) / thickness_m;
            double I_eff = currentU * omega * C1;
            baseCurrent = I_eff / 1.11;

            liveTempLabel.setText("t = 20.0 °C");
            liveCurrentLabel.setText(String.format("Ic = %.2f мкА", baseCurrent * 1e6));
        } catch (Exception ignored) {}
    }

    private double getEpsilonAtTemp(double temp) {
        double curieTemp = 120.0;
        double curieConstant = 1.0e5;

        if (temp <= curieTemp) {
            double epsMax = 8000;
            double a = (epsMax - baseEpsilon) / Math.pow(curieTemp - 20, 2);
            return baseEpsilon + a * Math.pow(temp - 20, 2);
        } else {
            return curieConstant / (temp - curieTemp + 10);
        }
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        fieldVoltage.setDisable(disable);
        fieldD.setDisable(disable);
        fieldThickness.setDisable(disable);
        fieldFreq.setDisable(disable);
        fieldMaxTemp.setDisable(disable);
    }

    private void startManual() {
        isAutoRunning = false;
        data.clear();
        rebuildChartSeries();
        idCounter = 1;
        updateStats();
        runHeatingProcess();
    }

    private void startAuto() {
        data.clear();
        rebuildChartSeries();
        idCounter = 1;
        updateStats();

        isAutoRunning = true;
        autoQueue.clear();

        double maxTemp = 140.0;
        try { maxTemp = Double.parseDouble(fieldMaxTemp.getText()); } catch (Exception ignored) {}

        for (double t = 20; t <= 110 && t <= maxTemp; t += 10) autoQueue.add(t);
        for (double t = 115; t <= 125 && t <= maxTemp; t += 2) autoQueue.add(t);
        for (double t = 130; t <= maxTemp; t += 10) autoQueue.add(t);

        runHeatingProcess();
    }

    private void runHeatingProcess() {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: НАГРІВАННЯ...");
        liveStatusLabel.setStyle("-fx-text-fill: red;");
        canvas.startSimulation();

        double maxTemp = 140.0;
        try { maxTemp = Double.parseDouble(fieldMaxTemp.getText()); } catch (Exception ignored) {}
        final double limit = maxTemp;

        new Thread(() -> {
            if (isAutoRunning) {
                while (!autoQueue.isEmpty()) {
                    double targetT = autoQueue.poll();
                    simulateToTemp(targetT);
                    try { Thread.sleep(600); } catch (Exception ignored) {}
                }
            } else {
                for (double t = 20; t <= 110 && t <= limit; t += 15) {
                    simulateToTemp(t);
                    try { Thread.sleep(800); } catch (Exception ignored) {}
                }
                for (double t = 115; t <= 125 && t <= limit; t += 2) {
                    simulateToTemp(t);
                    try { Thread.sleep(800); } catch (Exception ignored) {}
                }
                for (double t = 130; t <= limit; t += 5) {
                    simulateToTemp(t);
                    try { Thread.sleep(800); } catch (Exception ignored) {}
                }
            }

            Platform.runLater(() -> {
                canvas.stopSimulation();
                liveStatusLabel.setText("Статус: ОХОЛОДЖЕННЯ");
                liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
                setControlsDisable(false);
                updateStats();
            });
        }).start();
    }

    private void simulateToTemp(double targetTemp) {
        currentTemp = targetTemp;
        double eps = getEpsilonAtTemp(currentTemp);

        try {
            double d_m = Double.parseDouble(fieldD.getText()) / 1000.0;
            double thickness_m = Double.parseDouble(fieldThickness.getText()) / 1000.0;
            double freq = Double.parseDouble(fieldFreq.getText());
            double S = Math.PI * Math.pow(d_m / 2.0, 2);
            double omega = 2 * Math.PI * freq;
            double C = (EPSILON_0 * eps * S) / thickness_m;
            double I_eff = currentU * omega * C;
            double Ic = I_eff / 1.11;
            double noise = (Math.random() - 0.5) * 0.02 * Ic;
            Ic += noise;
            double calcEps = (1.11 * Ic * thickness_m) / (omega * EPSILON_0 * S * currentU);
            double ratio = calcEps / baseEpsilon;

            final double finalTemp = currentTemp;
            final double finalIc = Ic;
            final double finalCalcEps = calcEps;

            Platform.runLater(() -> {
                liveTempLabel.setText(String.format("t = %.1f °C", finalTemp));
                liveCurrentLabel.setText(String.format("Ic = %.2f мкА", finalIc * 1e6));
                canvas.updateTemperature(finalTemp);

                Measurement m = new Measurement(idCounter++,
                        Math.round(finalTemp * 10) / 10.0,
                        Math.round((finalIc * 1e6) * 100) / 100.0,
                        Math.round(finalCalcEps),
                        Math.round(ratio * 100) / 100.0);

                data.add(m);
                dataSeries.getData().add(new XYChart.Data<>(m.getTemperature(), m.getRatio()));
            });

        } catch (Exception ignored) {}
    }

    private void updateStats() {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: Очікування вимірювань...");
            return;
        }
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }
        double maxRatio = 0;
        double curieTempExp = 0;

        for (Measurement m : data) {
            if (m.getRatio() > maxRatio) {
                maxRatio = m.getRatio();
                curieTempExp = m.getTemperature();
            }
        }

        curiePointSeries.getData().clear();
        curiePointSeries.getData().add(new XYChart.Data<>(curieTempExp, 0));
        curiePointSeries.getData().add(new XYChart.Data<>(curieTempExp, maxRatio));

        double curieTempTheor = 120.0;
        double absErr = Math.abs(curieTempExp - curieTempTheor);
        double relErr = (absErr / curieTempTheor) * 100.0;

        Measurement firstM = data.get(0);

        StringBuilder sb = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ ЕКСПЕРИМЕНТУ:\n");
        sb.append(String.format("1. Відносна діелектрична проникність при кімнатній температурі (%.1f °C): ε1 = %.0f.\n", firstM.getTemperature(), firstM.getEpsilon()));
        sb.append("2. Побудовано температурну залежність ε/ε1 = f(t).\n");
        sb.append(String.format("3. З графіка визначено точку Кюрі (максимум проникності): t_K = %.1f °C.\n", curieTempExp));
        sb.append(String.format("4. Похибки визначення точки Кюрі: Абсолютна Δt = %.1f °C, Відносна ε = %.1f %%.\n", absErr, relErr));
        sb.append("5. Висновок: Підтверджено, що діелектрична проникність різко зростає при наближенні до точки Кюрі.");

        finalResultLabel.setText(sb.toString());
    }
}