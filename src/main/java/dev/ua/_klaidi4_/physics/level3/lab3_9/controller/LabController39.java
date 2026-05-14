package dev.ua._klaidi4_.physics.level3.lab3_9.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level3.lab3_9.model.Measurement;
import dev.ua._klaidi4_.physics.level3.lab3_9.view.CuriePointCanvas;
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
import java.util.Locale;
import java.util.Queue;

public class LabController39 extends BaseLabController {

    private CuriePointCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;

    private LineChart<Number, Number> chart;
    private XYChart.Series<Number, Number> dataSeries;
    private XYChart.Series<Number, Number> curiePointSeries;

    private ComboBox<String> materialBox;
    private TextField startTempField;
    private TextField endTempField;
    private TextField heatSpeedField;

    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;

    private Label liveStatusLabel;
    private Label liveTempLabel;
    private Label liveCurrentLabel;
    private Label liveCurieLabel;

    private Queue<Double> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    private int idCounter = 1;

    private double temperature = 20;
    private double current = 96;
    private double curieTemp = 120;
    private double endTemp = 125;

    public LabController39() {
        initUI();
        updateMaterialSettings();
    }

    @Override
    public void shutdown() {
        isAutoRunning = false;
        autoQueue.clear();
        if (canvas != null) canvas.stopAnimation();
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(320);
        leftPanel.setMinWidth(320);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Управління установкою (Лаб 3-9)");
        title.setFont(Font.font("System", FontWeight.BOLD, 17));

        TitledPane paramsPane = new TitledPane();
        paramsPane.setText("Параметри досліду");
        paramsPane.setCollapsible(false);

        VBox paramsBox = new VBox(10);
        paramsBox.setPadding(new Insets(5));

        materialBox = new ComboBox<>(FXCollections.observableArrayList(
                "Феромагнетик зразок №1",
                "Нікель",
                "Залізо"
        ));
        materialBox.getSelectionModel().selectFirst();
        materialBox.setMaxWidth(Double.MAX_VALUE);
        materialBox.setOnAction(e -> updateMaterialSettings());

        startTempField = new TextField("20");
        endTempField = new TextField("125");
        heatSpeedField = new TextField("350");

        paramsBox.getChildren().addAll(
                createInputGroup("Матеріал:", materialBox),
                createInputGroup("Початкова температура t₀ (°C):", startTempField),
                createInputGroup("Кінцева температура tₖ (°C):", endTempField),
                createInputGroup("Затримка між точками (мс):", heatSpeedField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(230);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        paramsPane.setContent(scrollParams);

        startBtn = new Button("▶ ПОЧАТИ НАГРІВ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startExperiment(false));

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startExperiment(true));

        clearBtn = new Button("🗑 ОЧИСТИТИ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> clearAll());

        leftPanel.getChildren().addAll(title, paramsPane, startBtn, autoBtn, clearBtn);

        canvas = new CuriePointCanvas(600, 300);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0,0,0,0.82); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(230, 100);

        Label dashTitle = new Label("ПОКАЗНИКИ УСТАНОВКИ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");

        liveTempLabel = new Label("t = 20.0 °C");
        liveTempLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 15));
        liveTempLabel.setStyle("-fx-text-fill: #00ff00;");

        liveCurrentLabel = new Label("I₂ = 96.0 mA");
        liveCurrentLabel.setStyle("-fx-text-fill: cyan; -fx-font-weight: bold;");

        liveCurieLabel = new Label("Tc = -");
        liveCurieLabel.setStyle("-fx-text-fill: #facc15; -fx-font-weight: bold;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveTempLabel, liveCurrentLabel, liveCurieLabel);

        StackPane canvasStack = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        canvasStack.setStyle("-fx-background-color: #ffffff;");

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Температура t (°C)");
        xAxis.setAutoRanging(true);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Струм I₂ (mA)");
        yAxis.setAutoRanging(true);

        chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(true);
        chart.setCreateSymbols(true);
        chart.setPrefHeight(260);
        VBox.setVgrow(chart, Priority.ALWAYS);

        rebuildChartSeries();

        VBox centerPanel = new VBox(canvasStack, chart);

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        table.getColumns().addAll(
                createColInt("№", "id"),
                createColString("Матеріал", "material"),
                createCol("t (°C)", "temperature"),
                createCol("I₂ (mA)", "current")
        );

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        updateStats();
    }

    private void rebuildChartSeries() {
        chart.getData().clear();

        dataSeries = new XYChart.Series<>();
        dataSeries.setName("I₂ = f(t)");

        curiePointSeries = new XYChart.Series<>();
        curiePointSeries.setName("Tc");

        chart.getData().addAll(dataSeries, curiePointSeries);
    }

    private TableColumn<Measurement, Double> createCol(String title, String property) {
        TableColumn<Measurement, Double> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format(Locale.US, "%.2f", item));
            }
        });
        return col;
    }

    private TableColumn<Measurement, Integer> createColInt(String title, String property) {
        TableColumn<Measurement, Integer> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }

    private TableColumn<Measurement, String> createColString(String title, String property) {
        TableColumn<Measurement, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }

    private void updateMaterialSettings() {
        String material = materialBox.getValue();
        if (material == null) return;

        if (material.equals("Феромагнетик зразок №1")) {
            curieTemp = 120;
            startTempField.setText("20");
            endTempField.setText("125");
        } else if (material.equals("Нікель")) {
            curieTemp = 365;
            startTempField.setText("300");
            endTempField.setText("390");
        } else {
            curieTemp = 770;
            startTempField.setText("700");
            endTempField.setText("800");
        }

        temperature = Double.parseDouble(startTempField.getText().replace(',', '.'));
        current = calculateCurrent(temperature);

        liveTempLabel.setText(String.format(Locale.US, "t = %.1f °C", temperature));
        liveCurrentLabel.setText(String.format(Locale.US, "I₂ = %.1f mA", current));
        liveCurieLabel.setText(String.format(Locale.US, "Tc ≈ %.0f °C", curieTemp));

        canvas.updateState(material, temperature, current, curieTemp, false);
    }

    private void startExperiment(boolean auto) {
        try {
            data.clear();
            rebuildChartSeries();
            idCounter = 1;
            updateStats();

            temperature = Double.parseDouble(startTempField.getText().replace(',', '.'));
            endTemp = Double.parseDouble(endTempField.getText().replace(',', '.'));

            autoQueue.clear();
            isAutoRunning = true;

            buildTemperatureQueue(temperature, endTemp);

            setControlsDisable(true);

            liveStatusLabel.setText("Статус: НАГРІВАННЯ...");
            liveStatusLabel.setStyle("-fx-text-fill: red;");

            runNextPoint();

        } catch (Exception e) {
            showAlert("Помилка", "Перевірте правильність введених чисел.");
        }
    }

    private void buildTemperatureQueue(double start, double end) {
        autoQueue.clear();

        for (double t = start; t <= Math.min(curieTemp - 10, end); t += 5) {
            autoQueue.add(round1(t));
        }

        for (double t = Math.max(start, curieTemp - 8); t <= Math.min(curieTemp + 5, end); t += 1) {
            autoQueue.add(round1(t));
        }

        for (double t = Math.max(start, curieTemp + 10); t <= end; t += 5) {
            autoQueue.add(round1(t));
        }

        if (autoQueue.isEmpty() || Math.abs(autoQueue.peek() - start) > 0.01) {
            autoQueue.add(round1(start));
        }

        if (end > start) {
            autoQueue.add(round1(end));
        }
    }

    private void runNextPoint() {
        if (!isAutoRunning) return;

        if (autoQueue.isEmpty()) {
            finishExperiment();
            return;
        }

        double targetTemp = autoQueue.poll();

        temperature = targetTemp;
        current = calculateCurrent(temperature);

        liveTempLabel.setText(String.format(Locale.US, "t = %.1f °C", temperature));
        liveCurrentLabel.setText(String.format(Locale.US, "I₂ = %.1f mA", current));

        canvas.updateState(materialBox.getValue(), temperature, current, curieTemp, true);
        recordMeasurement();

        int delay = 350;
        try {
            delay = Integer.parseInt(heatSpeedField.getText().replace(',', '.'));
        } catch (Exception ignored) {}

        int finalDelay = Math.max(50, delay);

        new Thread(() -> {
            try {
                Thread.sleep(finalDelay);
            } catch (InterruptedException ignored) {}

            Platform.runLater(this::runNextPoint);
        }).start();
    }

    private void finishExperiment() {
        isAutoRunning = false;
        setControlsDisable(false);

        double foundTc = findCuriePoint();

        liveStatusLabel.setText("Статус: ГОТОВО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
        liveCurieLabel.setText(String.format(Locale.US, "Tc = %.1f °C", foundTc));

        canvas.updateState(materialBox.getValue(), temperature, current, curieTemp, false);
        updateStats();
    }

    private double calculateCurrent(double temp) {
        String material = materialBox.getValue();

        double localTc = curieTemp;
        double high = 96.0;
        double low = 22.0;

        if ("Нікель".equals(material)) {
            high = 92.0;
            low = 20.0;
            localTc = 365.0;
        } else if ("Залізо".equals(material)) {
            high = 98.0;
            low = 25.0;
            localTc = 770.0;
        }

        double dropWidth = 1.45;
        double drop = 1.0 / (1.0 + Math.exp((temp - localTc) / dropWidth));

        double result = low + (high - low) * drop;

        double smallPeak = 3.5 * Math.exp(-Math.pow((temp - (localTc - 4.0)) / 4.0, 2));
        result += smallPeak;

        result += (Math.random() - 0.5) * 0.45;

        if (result < low) result = low;
        if (result > 105) result = 105;

        return result;
    }

    private void recordMeasurement() {
        Measurement m = new Measurement(
                idCounter++,
                materialBox.getValue(),
                round1(temperature),
                round1(current)
        );

        data.add(m);
        dataSeries.getData().add(new XYChart.Data<>(m.getTemperature(), m.getCurrent()));

        table.scrollTo(m);
        updateStats();
    }

    private double findCuriePoint() {
        if (data.size() < 3) return curieTemp;

        double maxDrop = 0;
        double found = curieTemp;

        for (int i = 1; i < data.size(); i++) {
            Measurement prev = data.get(i - 1);
            Measurement now = data.get(i);

            double dI = prev.getCurrent() - now.getCurrent();
            double dT = now.getTemperature() - prev.getTemperature();

            if (dT <= 0) continue;

            double speed = dI / dT;

            if (speed > maxDrop) {
                maxDrop = speed;
                found = (prev.getTemperature() + now.getTemperature()) / 2.0;
            }
        }

        return found;
    }

    private Measurement findClosestMeasurement(double temp) {
        if (data.isEmpty()) return null;

        Measurement closest = data.get(0);
        double bestDiff = Math.abs(closest.getTemperature() - temp);

        for (Measurement m : data) {
            double diff = Math.abs(m.getTemperature() - temp);
            if (diff < bestDiff) {
                bestDiff = diff;
                closest = m;
            }
        }

        return closest;
    }

    private void setControlsDisable(boolean disable) {
        materialBox.setDisable(disable);
        startTempField.setDisable(disable);
        endTempField.setDisable(disable);
        heatSpeedField.setDisable(disable);
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
    }

    private void clearAll() {
        isAutoRunning = false;
        autoQueue.clear();

        data.clear();
        rebuildChartSeries();
        idCounter = 1;

        updateMaterialSettings();

        liveStatusLabel.setText("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow;");

        setControlsDisable(false);
        updateStats();
    }

    private void updateStats() {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: очікування вимірювань...");
            return;
        }

        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }

        double foundTc = findCuriePoint();

        curiePointSeries.getData().clear();

        Measurement closest = findClosestMeasurement(foundTc);
        if (closest != null) {
            curiePointSeries.getData().add(new XYChart.Data<>(closest.getTemperature(), closest.getCurrent()));
        }

        String text = String.format(
                Locale.US,
                "ОБРОБКА РЕЗУЛЬТАТІВ:\n" +
                        "1. Проведено вимірювань: %d.\n" +
                        "2. Побудовано залежність I₂ = f(t°).\n" +
                        "3. Початковий струм I₂: %.2f mA.\n" +
                        "4. Кінцевий струм I₂: %.2f mA.\n" +
                        "5. Експериментально знайдена точка Кюрі: Tc ≈ %.2f °C.\n" +
                        "ВИСНОВОК: при нагріванні феромагнетика біля точки Кюрі струм у вторинній обмотці різко зменшується.",
                data.size(),
                data.get(0).getCurrent(),
                data.get(data.size() - 1).getCurrent(),
                foundTc
        );

        finalResultLabel.setText(text);
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}