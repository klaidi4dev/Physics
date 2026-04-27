package dev.ua._klaidi4_.physics.level2.lab2_6.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level2.lab2_6.model.Measurement;
import dev.ua._klaidi4_.physics.level2.lab2_6.view.ResistanceCanvas;
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

public class LabController26 extends BaseLabController {

    private ResistanceCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> modeBox;
    private VBox bridgePanel, thermoPanel;
    private ComboBox<String> targetResistorBox;
    private TextField fieldRetalon;
    private Slider lengthSlider;
    private TextField lengthField;
    private Button heatBtn;
    private Button autoBtn, recordBtn, calcBtn, clearBtn;
    private Label liveStatusLabel, liveModeLabel, liveIgLabel, liveTempLabel, liveRtLabel;
    private LineChart<Number, Number> chart;
    private XYChart.Series<Number, Number> dataSeries;
    private final double REAL_R1 = 150.0;
    private final double REAL_R2 = 300.0;
    private final double R_COPPER_0 = 10.0;
    private final double ALPHA_COPPER = 0.0043;

    private int currentMode = 1;
    private double currentIg = 0.0;
    private double currentTemp = 20.0;
    private boolean isAutoRunning = false;

    public LabController26() {
        initUI();
    }

    @Override
    public void shutdown() {
        isAutoRunning = false;
        if (canvas != null) canvas.stopSimulation();
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(320);
        leftPanel.setMinWidth(320);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 2-6)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane modePane = new TitledPane();
        modePane.setText("Вибір експерименту");
        modePane.setCollapsible(false);
        VBox modeBoxContainer = new VBox(10);
        modeBoxContainer.setPadding(new Insets(5));

        modeBox = new ComboBox<>();
        modeBox.getItems().addAll("1. Міст Уітстона (Вимір опорів)", "2. Термостат (Нагрівання)");
        modeBox.getSelectionModel().selectFirst();
        modeBox.setMaxWidth(Double.MAX_VALUE);
        modeBox.setOnAction(e -> switchMode(modeBox.getSelectionModel().getSelectedIndex() + 1));
        modeBoxContainer.getChildren().add(modeBox);
        modePane.setContent(modeBoxContainer);

        bridgePanel = new VBox(10);
        bridgePanel.setPadding(new Insets(5));

        targetResistorBox = new ComboBox<>();
        targetResistorBox.getItems().addAll("Резистор R1", "Резистор R2", "R1 + R2 (Послідовно)", "R1 || R2 (Паралельно)");
        targetResistorBox.getSelectionModel().selectFirst();
        targetResistorBox.setMaxWidth(Double.MAX_VALUE);
        targetResistorBox.setOnAction(e -> updateBridgePhysics());

        fieldRetalon = new TextField("100.0");
        fieldRetalon.textProperty().addListener((o, old, val) -> updateBridgePhysics());

        lengthSlider = new Slider(0, 1000, 500);
        lengthSlider.setShowTickMarks(true);
        lengthSlider.setMajorTickUnit(200);
        lengthField = new TextField("500.0");

        lengthSlider.valueProperty().addListener((o, old, val) -> {
            if(!lengthField.isFocused()) {
                lengthField.setText(String.format("%.1f", val.doubleValue()).replace(',', '.'));
            }
            updateBridgePhysics();
        });

        lengthField.setOnAction(e -> {
            try {
                double val = Double.parseDouble(lengthField.getText().replace(',', '.'));
                if (val >= 0 && val <= 1000) lengthSlider.setValue(val);
            } catch (Exception ignored) {}
        });

        bridgePanel.getChildren().addAll(
                createInputGroup("Об'єкт вимірювання (Rx):", targetResistorBox),
                createInputGroup("Опір еталона Rет (Ом):", fieldRetalon),
                new Label("Повзунок реохорда l1 (мм):"),
                lengthSlider,
                lengthField
        );

        thermoPanel = new VBox(10);
        thermoPanel.setPadding(new Insets(5));
        heatBtn = new Button("▶ ПОЧАТИ НАГРІВАННЯ");
        heatBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold;");
        heatBtn.setMaxWidth(Double.MAX_VALUE);
        heatBtn.setOnAction(e -> startHeatingManual());
        thermoPanel.getChildren().add(heatBtn);

        VBox dynamicControlBox = new VBox(bridgePanel);
        TitledPane controlPane = new TitledPane("Параметри установки", dynamicControlBox);
        controlPane.setCollapsible(false);

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAuto());

        recordBtn = new Button("✍ ЗАПИСАТИ ПОКАЗНИКИ");
        recordBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        recordBtn.setMaxWidth(Double.MAX_VALUE);
        recordBtn.setOnAction(e -> recordMeasurement());

        calcBtn = new Button("🖩 ОБРОБИТИ РЕЗУЛЬТАТИ");
        calcBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold;");
        calcBtn.setMaxWidth(Double.MAX_VALUE);
        calcBtn.setOnAction(e -> calculateResults());

        clearBtn = new Button("🗑 ОЧИСТИТИ ДАНІ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> resetAll());

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, modePane, controlPane, autoBtn, recordBtn, calcBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new ResistanceCanvas(450, 260);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(200, 100);

        Label dashTitle = new Label("ПОКАЗНИКИ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ГОТОВО");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveModeLabel = new Label("Режим: Опори");
        liveModeLabel.setStyle("-fx-text-fill: #4fc3f7;");

        liveIgLabel = new Label("Ig = 0.0 мкА");
        liveIgLabel.setStyle("-fx-text-fill: #ff3333; -fx-font-weight: bold;");

        liveTempLabel = new Label("t = 20.0 °C");
        liveTempLabel.setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
        liveRtLabel = new Label("Rt = 0.0 Ом");
        liveRtLabel.setStyle("-fx-text-fill: #00ff00;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveModeLabel, liveIgLabel, liveTempLabel, liveRtLabel);

        StackPane canvasStack = new StackPane(canvas, topBar, dash);
        canvasStack.setStyle("-fx-background-color: #ffffff;");
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Температура t (°C)");
        xAxis.setAutoRanging(true);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Опір Rt (Ом)");
        yAxis.setAutoRanging(true);
        chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(true);
        chart.setCreateSymbols(true);
        chart.setPrefHeight(200);
        rebuildChartSeries();

        VBox centerTopPanel = new VBox(canvasStack, chart);
        VBox.setVgrow(chart, Priority.ALWAYS);

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        String[] cols = {"№", "Режим", "Об'єкт", "t (°C)", "l1 (мм)", "Rет (Ом)", "Опір (Ом)"};
        String[] props = {"id", "mode", "target", "temperature", "l1", "rEtalon", "resistance"};

        for (int i = 0; i < cols.length; i++) {
            TableColumn<Measurement, Object> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(new PropertyValueFactory<>(props[i]));
            table.getColumns().add(col);
        }
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();
        HBox bottomHBox = new HBox(10, table, statsBox);
        HBox.setHgrow(statsBox, Priority.ALWAYS);
        bottomHBox.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerTopPanel);
        this.setBottom(bottomHBox);

        switchMode(1);
    }

    private void rebuildChartSeries() {
        chart.getData().clear();
        dataSeries = new XYChart.Series<>();
        dataSeries.setName("Залежність Rt = f(t)");
        chart.getData().add(dataSeries);
    }

    private void switchMode(int mode) {
        this.currentMode = mode;
        TitledPane ctrlPane = (TitledPane) ((VBox)((ScrollPane)leftPanel.getChildren().get(0)).getContent()).getChildren().get(2);
        VBox dynBox = (VBox) ctrlPane.getContent();
        dynBox.getChildren().clear();

        if (mode == 1) {
            dynBox.getChildren().add(bridgePanel);
            liveModeLabel.setText("Режим: Міст Уітстона");
            liveIgLabel.setManaged(true);
            liveIgLabel.setVisible(true);
            liveTempLabel.setManaged(false);
            liveTempLabel.setVisible(false);
            liveRtLabel.setManaged(false);
            liveRtLabel.setVisible(false);
            chart.setManaged(false);
            chart.setVisible(false);
            recordBtn.setDisable(false);
            updateBridgePhysics();
        } else {
            dynBox.getChildren().add(thermoPanel);
            liveModeLabel.setText("Режим: Термостат");
            liveIgLabel.setManaged(false);
            liveIgLabel.setVisible(false);
            liveTempLabel.setManaged(true);
            liveTempLabel.setVisible(true);
            liveRtLabel.setManaged(true);
            liveRtLabel.setVisible(true);
            chart.setManaged(true);
            chart.setVisible(true);
            recordBtn.setDisable(true);
            currentTemp = 20.0;
            updateThermoPhysics();
        }
        canvas.setMode(mode);
    }

    private double getCurrentRxReal() {
        int idx = targetResistorBox.getSelectionModel().getSelectedIndex();
        if (idx == 0) return REAL_R1;
        if (idx == 1) return REAL_R2;
        if (idx == 2) return REAL_R1 + REAL_R2;
        return (REAL_R1 * REAL_R2) / (REAL_R1 + REAL_R2);
    }

    private void updateBridgePhysics() {
        if (currentMode != 1 || isAutoRunning) return;

        double rEt = 100.0;
        try { rEt = Double.parseDouble(fieldRetalon.getText().replace(',', '.')); } catch(Exception ignored){}

        double rxReal = getCurrentRxReal();
        double l1 = lengthSlider.getValue();
        double vTop = 5.0 * (rEt / (rxReal + rEt));
        double vBot = 5.0 * (1000.0 - l1) / 1000.0;
        currentIg = (vTop - vBot) / 100.0 * 1e6;

        String targetName = targetResistorBox.getValue().split(" ")[0];

        Platform.runLater(() -> {
            if (Math.abs(currentIg) < 1.0) {
                liveIgLabel.setText(String.format("Ig = %.1f мкА", currentIg));
                liveIgLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
                liveStatusLabel.setText("Статус: СКОМПЕНСОВАНО!");
            } else {
                liveIgLabel.setText(String.format("Ig = %.1f мкА", currentIg));
                liveIgLabel.setStyle("-fx-text-fill: #ff3333; -fx-font-weight: bold;");
                liveStatusLabel.setText("Статус: НАЛАШТУВАННЯ...");
            }
            canvas.updateBridge(l1, currentIg, targetName);
        });
    }

    private void updateThermoPhysics() {
        double rt = R_COPPER_0 * (1 + ALPHA_COPPER * currentTemp);
        double noise = (Math.random() - 0.5) * 0.05;
        double measuredRt = rt + noise;

        Platform.runLater(() -> {
            liveTempLabel.setText(String.format("t = %.1f °C", currentTemp));
            liveRtLabel.setText(String.format("Rt = %.2f Ом", measuredRt));
            canvas.updateThermostat(currentTemp, isAutoRunning || heatBtn.isDisabled());
        });
    }

    private void setControlsDisable(boolean disable) {
        modeBox.setDisable(disable);
        targetResistorBox.setDisable(disable);
        fieldRetalon.setDisable(disable);
        lengthSlider.setDisable(disable);
        lengthField.setDisable(disable);
        heatBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        calcBtn.setDisable(disable);
    }

    private void startAuto() {
        if (currentMode == 1) {
            runAutoBridge();
        } else {
            runAutoThermo();
        }
    }

    private void startHeatingManual() {
        runAutoThermo();
    }

    private void runAutoBridge() {
        data.clear();
        idCounter = 1;
        isAutoRunning = true;
        setControlsDisable(true);

        new Thread(() -> {
            for (int i = 0; i < 4; i++) {
                final int idx = i;
                Platform.runLater(() -> targetResistorBox.getSelectionModel().select(idx));
                try { Thread.sleep(500); } catch (Exception ignored) {}

                double rEt = 100.0;
                double rxReal = getCurrentRxReal();
                double targetL1 = 1000.0 * rxReal / (rxReal + rEt);

                double current = lengthSlider.getValue();
                double step = (targetL1 > current) ? 25.0 : -25.0;

                Platform.runLater(() -> liveStatusLabel.setText("Статус: ПОШУК НУЛЯ..."));

                while (Math.abs(targetL1 - current) > Math.abs(step) && isAutoRunning) {
                    current += step;
                    final double val = current;

                    double vTop = 5.0 * (rEt / (rxReal + rEt));
                    double vBot = 5.0 * (1000.0 - val) / 1000.0;
                    currentIg = (vTop - vBot) / 100.0 * 1e6;

                    Platform.runLater(() -> {
                        lengthSlider.setValue(val);
                        liveIgLabel.setText(String.format("Ig = %.1f мкА", currentIg));
                        canvas.updateBridge(val, currentIg, targetResistorBox.getValue().split(" ")[0]);
                    });
                    try { Thread.sleep(50); } catch (Exception ignored) {}
                }

                currentIg = 0.0;
                Platform.runLater(() -> {
                    lengthSlider.setValue(targetL1);
                    liveIgLabel.setText("Ig = 0.0 мкА");
                    liveIgLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
                    canvas.updateBridge(targetL1, 0.0, targetResistorBox.getValue().split(" ")[0]);
                    recordMeasurement();
                });
                try { Thread.sleep(1000); } catch (Exception ignored) {}
            }

            Platform.runLater(() -> {
                isAutoRunning = false;
                setControlsDisable(false);
                liveStatusLabel.setText("Статус: АВТО ЗАВЕРШЕНО");
                calculateResults();
            });
        }).start();
    }

    private void runAutoThermo() {
        data.clear();
        rebuildChartSeries();
        idCounter = 1;
        isAutoRunning = true;
        setControlsDisable(true);
        currentTemp = 20.0;

        new Thread(() -> {
            Platform.runLater(() -> liveStatusLabel.setText("Статус: НАГРІВАННЯ..."));

            for (double t = 20; t <= 90; t += 5) {
                if (!isAutoRunning) break;
                currentTemp = t;
                updateThermoPhysics();

                final double tempToRecord = t;
                final double rtToRecord = R_COPPER_0 * (1 + ALPHA_COPPER * t) + (Math.random() - 0.5) * 0.05;

                Platform.runLater(() -> {
                    Measurement m = new Measurement(idCounter++, "Термостат", "Мідь",
                            tempToRecord, 0, 0, Math.round(rtToRecord * 100.0) / 100.0);
                    data.add(m);
                    dataSeries.getData().add(new XYChart.Data<>(m.getTemperature(), m.getResistance()));
                });
                try { Thread.sleep(600); } catch (Exception ignored) {}
            }

            Platform.runLater(() -> {
                isAutoRunning = false;
                setControlsDisable(false);
                liveStatusLabel.setText("Статус: ОХОЛОДЖЕННЯ");
                canvas.updateThermostat(90.0, false);
                calculateResults();
            });
        }).start();
    }

    private void recordMeasurement() {
        if (currentMode == 1) {
            String target = targetResistorBox.getValue().split(" ")[0];
            double l1 = Math.round(lengthSlider.getValue() * 10.0) / 10.0;
            double rEt = Double.parseDouble(fieldRetalon.getText().replace(',', '.'));
            double rxCalc = rEt * (l1 / (1000.0 - l1));

            Measurement m = new Measurement(idCounter++, "Міст", target, 0, l1, rEt, Math.round(rxCalc * 100.0) / 100.0);
            data.add(m);
        }
    }

    private void calculateResults() {
        if (data.isEmpty()) return;

        StringBuilder sb = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ:\n");
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }
        if (currentMode == 1) {
            double r1 = 0, r2 = 0, rSer = 0, rPar = 0;
            for (Measurement m : data) {
                if (m.getTarget().equals("Резистор") && m.getId() == 1) r1 = m.getResistance();
                if (m.getTarget().equals("R1")) r1 = m.getResistance();
                if (m.getTarget().equals("R2")) r2 = m.getResistance();
                if (m.getTarget().equals("R1+R2")) rSer = m.getResistance();
                if (m.getTarget().equals("R1||R2")) rPar = m.getResistance();
            }
            sb.append("Дослід 1. Перевірка законів з'єднання провідників:\n");
            if (r1 > 0 && r2 > 0) {
                double theorSer = r1 + r2;
                double theorPar = (r1 * r2) / (r1 + r2);
                sb.append(String.format("Виміряно: R1 = %.1f Ом, R2 = %.1f Ом.\n", r1, r2));
                if (rSer > 0) sb.append(String.format("Послідовне: Експ = %.1f Ом, Теор = %.1f Ом.\n", rSer, theorSer));
                if (rPar > 0) sb.append(String.format("Паралельне: Експ = %.1f Ом, Теор = %.1f Ом.\n", rPar, theorPar));
                sb.append("Висновок: Закони послідовного та паралельного з'єднання підтверджуються.");
            } else {
                sb.append("Недостатньо даних для перевірки з'єднань.");
            }
        } else {
            Measurement m20 = data.get(0);
            Measurement m90 = data.get(data.size() - 1);

            double r0Exp = m20.getResistance() / (1 + ALPHA_COPPER * m20.getTemperature());
            double alphaExp = (m90.getResistance() - m20.getResistance()) /
                    (m20.getResistance() * m90.getTemperature() - m90.getResistance() * m20.getTemperature());

            double alphaSimple = (m90.getResistance() - r0Exp) / (r0Exp * m90.getTemperature());

            sb.append("Дослід 2. Залежність опору металу від температури:\n");
            sb.append("1. Побудовано лінійний графік Rt = f(t).\n");
            sb.append(String.format("2. Шляхом екстраполяції визначено опір при 0°C: R0 = %.2f Ом.\n", r0Exp));
            sb.append(String.format("3. Розраховано температурний коефіцієнт: α = %.5f K^-1.\n", alphaSimple));
            sb.append(String.format("4. Табличне значення для міді: α_теор = %.4f K^-1.\n", ALPHA_COPPER));
            sb.append("Висновок: Опір металів лінійно зростає з температурою.");
        }

        finalResultLabel.setText(sb.toString());
    }

    private void resetAll() {
        data.clear();
        rebuildChartSeries();
        idCounter = 1;
        finalResultLabel.setText("Обробка результатів: -");
        if (currentMode == 1) {
            lengthSlider.setValue(500);
            updateBridgePhysics();
        } else {
            currentTemp = 20.0;
            updateThermoPhysics();
        }
        liveStatusLabel.setText("Статус: ОЧИЩЕНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
    }
}