package dev.ua._klaidi4_.physics.level7.lab7_6.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level7.lab7_6.model.Measurement;
import dev.ua._klaidi4_.physics.level7.lab7_6.view.EntropyCanvas;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Locale;

public class LabController76 extends BaseLabController {

    private EntropyCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> materialBox;
    private Slider massSlider;
    private Slider speedSlider;
    private Button manualLogBtn;
    private Button powerBtn;
    private Button stopBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Button chartBtn;
    private Label liveTempLabel;
    private Label liveTimeLabel;
    private Label livePhaseLabel;

    private final double[][] MATERIALS = {
            {327.0, 130.0, 140.0, 25000.0},
            {232.0, 230.0, 240.0, 59000.0},
            {420.0, 390.0, 410.0, 112000.0}
    };

    private final double P_FURNACE = 80.0;
    private final double EFFICIENCY = 0.65;
    private final double K_LOSS = 0.08;
    private enum Phase { SOLID, MELTING, LIQUID }
    private Phase currentPhase = Phase.SOLID;
    private boolean isHeaterOn = false;
    private boolean isSimRunning = false;
    private boolean isAutoRunning = false;
    private double currentTemp = 20.0;
    private double simTime = 0.0;
    private double phaseEnergy = 0.0;
    private double lastLogTime = -999;
    private AnimationTimer simTimer;

    public LabController76() {
        initUI();
    }

    @Override
    public void shutdown() {
        if (simTimer != null) simTimer.stop();
        isSimRunning = false;
        isAutoRunning = false;
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(320);
        leftPanel.setMinWidth(320);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 7-6)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane configPane = new TitledPane();
        configPane.setText("Параметри установки");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(10);
        configBox.setPadding(new Insets(5));

        materialBox = new ComboBox<>(FXCollections.observableArrayList(
                "Свинець (Tпл = 327 °C)",
                "Олово (Tпл = 232 °C)",
                "Цинк (Tпл = 420 °C)"
        ));
        materialBox.getSelectionModel().selectFirst();
        materialBox.setMaxWidth(Double.MAX_VALUE);
        materialBox.setOnAction(e -> {
            resetSimulation();
            updateUI();
        });

        Label massLabel = new Label("Маса металу m: 0.300 кг");
        massSlider = new Slider(0.100, 0.500, 0.300);
        massSlider.setMajorTickUnit(0.1);
        massSlider.setShowTickMarks(true);
        massSlider.valueProperty().addListener((o, ov, nv) ->
                massLabel.setText(String.format(Locale.US, "Маса металу m: %.3f кг", nv.doubleValue())));

        Label speedLabel = new Label("Швидкість симуляції: 5x");
        speedSlider = new Slider(1.0, 50.0, 5.0);
        speedSlider.setMajorTickUnit(10.0);
        speedSlider.setShowTickMarks(true);
        speedSlider.valueProperty().addListener((o, ov, nv) ->
                speedLabel.setText(String.format(Locale.US, "Швидкість симуляції: %.0fx", nv.doubleValue())));

        configBox.getChildren().addAll(new Label("Досліджуваний метал:"), materialBox, massLabel, massSlider, speedLabel, speedSlider);
        configPane.setContent(configBox);

        manualLogBtn = new Button("📝 ЗАПИСАТИ ПОКАЗНИК");
        manualLogBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        manualLogBtn.setMaxWidth(Double.MAX_VALUE);
        manualLogBtn.setOnAction(e -> forceRecordMeasurement());

        powerBtn = new Button("⚡ УВІМКНУТИ ПІЧ");
        powerBtn.setStyle("-fx-background-color: #ff007f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        powerBtn.setMaxWidth(Double.MAX_VALUE);
        powerBtn.setOnAction(e -> toggleHeater());

        stopBtn = new Button("⏸ ЗУПИНИТИ ДОСЛІД (ЧАС)");
        stopBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        stopBtn.setMaxWidth(Double.MAX_VALUE);
        stopBtn.setDisable(true);
        stopBtn.setOnAction(e -> pauseSimulation());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ ЛАБИ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAutoMode());

        chartBtn = new Button("📈 ДЕТАЛЬНИЙ ГРАФІК T(t)");
        chartBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        chartBtn.setMaxWidth(Double.MAX_VALUE);
        chartBtn.setOnAction(e -> showDetailedChart());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ (СКИДАННЯ)");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> resetSimulation());

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, configPane, manualLogBtn, powerBtn, stopBtn, autoBtn, chartBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new EntropyCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(4);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-text-fill: #00ffcc; -fx-padding: 10; -fx-border-color: #00ffcc; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(220, 100);

        Label dashTitle = new Label("ІНФО-ПАНЕЛЬ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        liveTempLabel = new Label("T = 20.0 °C");
        liveTempLabel.setStyle("-fx-text-fill: #ff007f; -fx-font-size: 16px; -fx-font-weight: bold;");

        liveTimeLabel = new Label("Час: 00:00");
        liveTimeLabel.setStyle("-fx-text-fill: #a3e635;");

        livePhaseLabel = new Label("Стан: Твердий");
        livePhaseLabel.setStyle("-fx-text-fill: #94a3b8;");

        dash.getChildren().addAll(dashTitle, liveTempLabel, liveTimeLabel, livePhaseLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(40);

        TableColumn<Measurement, String> timeCol = new TableColumn<>("Час (хв:с)");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("timeStr"));

        TableColumn<Measurement, Double> tempCol = new TableColumn<>("T (°C)");
        tempCol.setCellValueFactory(new PropertyValueFactory<>("temp"));

        TableColumn<Measurement, String> phaseCol = new TableColumn<>("Фаза");
        phaseCol.setCellValueFactory(new PropertyValueFactory<>("phase"));

        table.getColumns().addAll(idCol, timeCol, tempCol, phaseCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        updateUI();
    }

    private void toggleHeater() {
        isHeaterOn = !isHeaterOn;
        if (isHeaterOn) {
            powerBtn.setText("⏹ ВИМКНУТИ ПІЧ");
            powerBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
            massSlider.setDisable(true);
            materialBox.setDisable(true);
            if (!isSimRunning) startSimulation();
        } else {
            powerBtn.setText("⚡ УВІМКНУТИ ПІЧ");
            powerBtn.setStyle("-fx-background-color: #ff007f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        }
    }

    private void pauseSimulation() {
        if (simTimer != null) simTimer.stop();
        isSimRunning = false;
        isHeaterOn = false;
        powerBtn.setText("⚡ УВІМКНУТИ ПІЧ");
        powerBtn.setStyle("-fx-background-color: #ff007f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        powerBtn.setDisable(true);
        stopBtn.setDisable(true);
        updateStatsFinal();
    }

    private void resetSimulation() {
        shutdown();
        isHeaterOn = false;
        isSimRunning = false;
        isAutoRunning = false;
        currentTemp = 20.0;
        simTime = 0.0;
        phaseEnergy = 0.0;
        currentPhase = Phase.SOLID;
        lastLogTime = -999;

        massSlider.setDisable(false);
        materialBox.setDisable(false);
        speedSlider.setDisable(false);
        manualLogBtn.setDisable(false);
        autoBtn.setDisable(false);
        stopBtn.setDisable(true);
        powerBtn.setDisable(false);
        powerBtn.setText("⚡ УВІМКНУТИ ПІЧ");
        powerBtn.setStyle("-fx-background-color: #ff007f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");

        data.clear();
        idCounter = 1;
        canvas.resetGraph();
        updateUI();

        if (finalResultLabel != null) {
            finalResultLabel.setText("Обробка результатів: Очікування даних...");
        }
    }

    private void startSimulation() {
        isSimRunning = true;
        stopBtn.setDisable(false);

        simTimer = new AnimationTimer() {
            private long lastTime = 0;
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                double dtSim = dt * speedSlider.getValue();
                simTime += dtSim;

                physicsTick(dtSim);
                updateUI();

                if (isAutoRunning) {
                    handleAutoLogging();
                }
            }
        };
        simTimer.start();
    }

    private void physicsTick(double dtSim) {
        int matIdx = materialBox.getSelectionModel().getSelectedIndex();
        double targetMelt = MATERIALS[matIdx][0];
        double cSolid = MATERIALS[matIdx][1];
        double cLiquid = MATERIALS[matIdx][2];
        double lambda = MATERIALS[matIdx][3];

        double m = massSlider.getValue();
        double maxPhaseEnergy = lambda * m;

        double dQ = 0;
        if (isHeaterOn) dQ += P_FURNACE * EFFICIENCY * dtSim;
        dQ -= K_LOSS * (currentTemp - 20.0) * dtSim;

        if (currentPhase == Phase.SOLID) {
            currentTemp += dQ / (cSolid * m);
            if (currentTemp >= targetMelt && dQ > 0) {
                currentTemp = targetMelt;
                currentPhase = Phase.MELTING;
            }
        } else if (currentPhase == Phase.MELTING) {
            phaseEnergy += dQ;
            if (phaseEnergy >= maxPhaseEnergy) {
                phaseEnergy = maxPhaseEnergy;
                currentPhase = Phase.LIQUID;
            } else if (phaseEnergy <= 0) {
                phaseEnergy = 0;
                currentPhase = Phase.SOLID;
            }
        } else if (currentPhase == Phase.LIQUID) {
            currentTemp += dQ / (cLiquid * m);
            if (currentTemp <= targetMelt && dQ < 0) {
                currentTemp = targetMelt;
                currentPhase = Phase.MELTING;
            }
        }
    }

    private String getPhaseString() {
        if (currentPhase == Phase.MELTING) {
            return isHeaterOn ? "Плавлення" : "Кристалізація";
        } else if (currentPhase == Phase.LIQUID) {
            return "Рідкий";
        } else {
            return "Твердий";
        }
    }

    private void forceRecordMeasurement() {
        lastLogTime = simTime;
        recordMeasurementSafe();
    }

    private void handleAutoLogging() {
        double targetMelt = MATERIALS[materialBox.getSelectionModel().getSelectedIndex()][0];
        double interval = (currentTemp >= targetMelt - 37 && currentTemp <= targetMelt + 10) ? 30.0 : 120.0;

        if (simTime - lastLogTime >= interval) {
            lastLogTime = simTime;
            recordMeasurementSafe();
        }
    }

    private void recordMeasurementSafe() {
        int m = (int) (simTime / 60);
        int s = (int) (simTime % 60);
        String tStr = String.format("%02d:%02d", m, s);

        String status = isHeaterOn ? "Нагрів (" + getPhaseString() + ")" : "Охолодж (" + getPhaseString() + ")";

        Measurement newRecord = new Measurement(idCounter++, tStr, Math.round(currentTemp * 10.0) / 10.0, status);
        data.add(newRecord);

        if (data.size() > 0) {
            Platform.runLater(() -> {
                try {
                    table.scrollTo(data.size() - 1);
                } catch (Exception e) {
                    System.err.println("Помилка прокрутки: " + e.getMessage());
                }
            });
        }

        updateStats();
    }

    private void updateUI() {
        liveTempLabel.setText(String.format(Locale.US, "T = %.1f °C", currentTemp));

        int m = (int) (simTime / 60);
        int s = (int) (simTime % 60);
        liveTimeLabel.setText(String.format("Час: %02d:%02d", m, s));
        livePhaseLabel.setText("Стан: " + getPhaseString());

        int matIdx = materialBox.getSelectionModel().getSelectedIndex();
        double lambda = MATERIALS[matIdx][3];
        double targetMelt = MATERIALS[matIdx][0];

        double meltProg = phaseEnergy / (lambda * massSlider.getValue());
        canvas.updateState(isHeaterOn, currentTemp, meltProg, simTime, targetMelt);
    }

    private void startAutoMode() {
        resetSimulation();
        isAutoRunning = true;
        autoBtn.setDisable(true);
        manualLogBtn.setDisable(true);
        powerBtn.setDisable(true);
        speedSlider.setValue(40.0);
        speedSlider.setDisable(true);

        toggleHeater();

        new Thread(() -> {
            try {
                double targetMelt = MATERIALS[materialBox.getSelectionModel().getSelectedIndex()][0];

                while (currentTemp < targetMelt + 15.0 && isSimRunning) {
                    Thread.sleep(100);
                }

                if (isSimRunning) {
                    Platform.runLater(this::toggleHeater);
                }

                while (currentTemp > targetMelt - 25.0 && isSimRunning) {
                    Thread.sleep(100);
                }

                if (isSimRunning) {
                    Platform.runLater(this::pauseSimulation);
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void showDetailedChart() {
        if (data.isEmpty()) {
            showAlert("Увага", "Немає даних для побудови графіка. Спочатку зберіть показники в таблицю.");
            return;
        }

        Stage chartStage = new Stage();
        chartStage.setTitle("Детальний аналіз графіка T(t) - Лабораторна 7.6");

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Час симуляції (секунди)");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Температура (°C)");
        yAxis.setAutoRanging(true);

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Залежність температури від часу");
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(true);

        XYChart.Series<Number, Number> mainSeries = new XYChart.Series<>();
        mainSeries.setName("Показники з таблиці");

        double maxTime = 0;

        for (Measurement m : data) {
            String[] parts = m.getTimeStr().split(":");
            double timeInSeconds = Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
            maxTime = Math.max(maxTime, timeInSeconds);

            XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(timeInSeconds, m.getTemp());
            mainSeries.getData().add(dataPoint);
        }

        double targetMelt = MATERIALS[materialBox.getSelectionModel().getSelectedIndex()][0];
        XYChart.Series<Number, Number> meltSeries = new XYChart.Series<>();
        meltSeries.setName(String.format(Locale.US, "T_пл (%.1f °C)", targetMelt));
        meltSeries.getData().add(new XYChart.Data<>(0, targetMelt));
        meltSeries.getData().add(new XYChart.Data<>(maxTime + 60, targetMelt));

        lineChart.getData().addAll(mainSeries, meltSeries);

        for (XYChart.Data<Number, Number> d : mainSeries.getData()) {
            Node node = d.getNode();
            if (node != null) {
                int m = (int) (d.getXValue().doubleValue() / 60);
                int s = (int) (d.getXValue().doubleValue() % 60);
                String tStr = String.format("%02d:%02d", m, s);

                Tooltip t = new Tooltip(String.format("Час: %s\nТемпература: %.1f °C", tStr, d.getYValue()));
                t.setFont(Font.font("System", FontWeight.BOLD, 14));
                Tooltip.install(node, t);

                node.setStyle("-fx-background-color: #ff007f, white;");
                node.setOnMouseEntered(e -> {
                    node.setScaleX(1.5);
                    node.setScaleY(1.5);
                });
                node.setOnMouseExited(e -> {
                    node.setScaleX(1.0);
                    node.setScaleY(1.0);
                });
            }
        }

        for (XYChart.Data<Number, Number> d : meltSeries.getData()) {
            Node node = d.getNode();
            if (node != null) {
                node.setStyle("-fx-background-color: transparent;");
            }
        }

        lineChart.setStyle("-fx-background-color: transparent;");

        CheckBox showPointsCheck = new CheckBox("Відображати маркери точок");
        showPointsCheck.setSelected(true);
        showPointsCheck.setFont(Font.font("System", FontWeight.BOLD, 14));
        showPointsCheck.setStyle("-fx-text-fill: #1e293b;");
        showPointsCheck.setOnAction(e -> {
            boolean show = showPointsCheck.isSelected();
            for (XYChart.Data<Number, Number> d : mainSeries.getData()) {
                Node node = d.getNode();
                if (node != null) {
                    node.setVisible(show);
                }
            }
        });

        HBox topBar = new HBox(showPointsCheck);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(10, 20, 0, 0));

        VBox root = new VBox(topBar, lineChart);
        VBox.setVgrow(lineChart, Priority.ALWAYS);
        root.setStyle("-fx-background-color: #f1f5f9;");

        Scene scene = new Scene(root, 900, 600);
        chartStage.setScene(scene);
        chartStage.show();
    }

    private void updateStats() {
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }

        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: Очікування даних...");
            return;
        }
        finalResultLabel.setText("Обробка результатів: Збір даних графіка T(t)... Відкрийте детальний графік для аналізу.");
    }

    private void updateStatsFinal() {
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }

        int matIdx = materialBox.getSelectionModel().getSelectedIndex();
        double tk = MATERIALS[matIdx][0] + 273.15;
        double cSolid = MATERIALS[matIdx][1];
        double lambda = MATERIALS[matIdx][3];
        String matName = materialBox.getValue().split(" ")[0];

        double t1 = 20.0 + 273.15;
        double m = massSlider.getValue();

        double dS1 = cSolid * m * Math.log(tk / t1);
        double dS2 = (lambda * m) / tk;
        double dS_total = dS1 + dS2;

        String conclusion = String.format(Locale.US,
                "ДОСЛІД ЗАВЕРШЕНО. ОБРОБКА РЕЗУЛЬТАТІВ (%s):\n" +
                        "1. Зміна ентропії при нагріванні: ΔS₁ = c·m·ln(T_n/T₁) = %.2f Дж/К.\n" +
                        "2. Зміна ентропії при ізотермічному плавленні: ΔS₂ = λ·m / T_n = %.2f Дж/К.\n" +
                        "ВИСНОВОК: Повна зміна ентропії ΔS = %.2f Дж/К. Температура плавлення є сталою (на графіку це горизонтальне плато), а ентропія системи стрімко зростає через перехід металу в менш впорядкований рідкий стан.",
                matName, dS1, dS2, dS_total
        );

        finalResultLabel.setText(conclusion);
    }
}