package dev.ua._klaidi4_.physics.level7.lab7_7.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level7.lab7_7.model.Measurement;
import dev.ua._klaidi4_.physics.level7.lab7_7.view.MaxwellCanvas;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LabController77 extends BaseLabController {

    private MaxwellCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField voltageField;
    private TextField tempField;
    private TextField emissionField;
    private TextField speedField;
    private Button powerBtn;
    private Button recordBtn;
    private Button autoBtn;
    private Button chartBtn;
    private Button clearBtn;

    private boolean isPowerOn = false;

    private final double[] RECOMMENDED_V = {0.1, 0.2, 0.4, 0.6, 0.8, 1.0, 1.5, 2.0, 2.5, 3.0, 4.0, 6.0, 8.0, 10.0, 15.0};

    public LabController77() {
        initUI();
    }

    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(330);
        leftPanel.setMinWidth(330);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 7-7)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane advancedPane = new TitledPane();
        advancedPane.setText("Параметри лампи (Розширені)");
        advancedPane.setCollapsible(true);
        advancedPane.setExpanded(false);

        VBox advancedBox = new VBox(10);
        advancedBox.setPadding(new Insets(5));

        tempField = new TextField("2000.0");
        tempField.textProperty().addListener((o, ov, nv) -> updateAdvancedParams());

        emissionField = new TextField("1.0");
        emissionField.textProperty().addListener((o, ov, nv) -> updateAdvancedParams());

        speedField = new TextField("1.0");
        speedField.textProperty().addListener((o, ov, nv) -> updateAdvancedParams());

        advancedBox.getChildren().addAll(
                createInputGroup("Температура катода T (К):", tempField),
                createInputGroup("Інтенсивність емісії (x):", emissionField),
                createInputGroup("Швидкість анімації (x):", speedField)
        );
        advancedPane.setContent(advancedBox);

        TitledPane configPane = new TitledPane();
        configPane.setText("Стримувальне поле (ВС-24М)");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(10);
        configBox.setPadding(new Insets(5));

        voltageField = new TextField("0.0");
        voltageField.setDisable(true);
        voltageField.textProperty().addListener((o, ov, nv) -> {
            if (canvas != null) {
                canvas.updateState(isPowerOn, getDoubleValue(voltageField, 0.0));
            }
        });

        configBox.getChildren().add(createInputGroup("Стримувальна напруга Δφ3 (В):", voltageField));
        configPane.setContent(configBox);

        powerBtn = new Button("⚡ УВІМКНУТИ ЖИВЛЕННЯ (ВУП-2)");
        powerBtn.setStyle("-fx-background-color: #ff007f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        powerBtn.setMaxWidth(Double.MAX_VALUE);
        powerBtn.setOnAction(e -> togglePower());

        recordBtn = new Button("📝 ЗАПИСАТИ СТРУМ І_a");
        recordBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        recordBtn.setMaxWidth(Double.MAX_VALUE);
        recordBtn.setDisable(true);
        recordBtn.setOnAction(e -> recordMeasurement());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ ЛАБИ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAutoMode());

        chartBtn = new Button("📈 ПОБУДУВАТИ ГРАФІКИ (ОБРОБКА)");
        chartBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        chartBtn.setMaxWidth(Double.MAX_VALUE);
        chartBtn.setOnAction(e -> processAndShowCharts());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> resetLab());

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, advancedPane, configPane, powerBtn, recordBtn, autoBtn, chartBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new MaxwellCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        StackPane centerPanel = new StackPane(canvas, topBar);
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(40);

        TableColumn<Measurement, Double> vCol = new TableColumn<>("Δφ3 (В)");
        vCol.setCellValueFactory(new PropertyValueFactory<>("voltage"));

        TableColumn<Measurement, Double> iCol = new TableColumn<>("I_a (мкА)");
        iCol.setCellValueFactory(new PropertyValueFactory<>("current"));

        TableColumn<Measurement, Double> sqrtCol = new TableColumn<>("√Δφ3");
        sqrtCol.setCellValueFactory(new PropertyValueFactory<>("sqrtVoltage"));

        TableColumn<Measurement, Double> uCol = new TableColumn<>("U");
        uCol.setCellValueFactory(new PropertyValueFactory<>("u"));

        TableColumn<Measurement, Double> fCol = new TableColumn<>("f(U) експ");
        fCol.setCellValueFactory(new PropertyValueFactory<>("fU"));

        table.getColumns().addAll(idCol, vCol, iCol, sqrtCol, uCol, fCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);
    }

    private double getDoubleValue(TextField field, double defaultValue) {
        try {
            return Double.parseDouble(field.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void updateAdvancedParams() {
        if (canvas != null) {
            canvas.setAdvancedParams(
                    getDoubleValue(tempField, 2000.0),
                    getDoubleValue(emissionField, 1.0),
                    getDoubleValue(speedField, 1.0)
            );
        }
    }

    private void togglePower() {
        isPowerOn = !isPowerOn;
        if (isPowerOn) {
            powerBtn.setText("⏹ ВИМКНУТИ ЖИВЛЕННЯ");
            powerBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
            voltageField.setDisable(false);
            recordBtn.setDisable(false);

            tempField.setDisable(true);
            emissionField.setDisable(true);

            updateAdvancedParams();
            canvas.updateState(true, getDoubleValue(voltageField, 0.0));
        } else {
            powerBtn.setText("⚡ УВІМКНУТИ ЖИВЛЕННЯ (ВУП-2)");
            powerBtn.setStyle("-fx-background-color: #ff007f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
            voltageField.setDisable(true);
            recordBtn.setDisable(true);

            tempField.setDisable(false);
            emissionField.setDisable(false);

            canvas.updateState(false, 0);
        }
    }

    private void resetLab() {
        data.clear();
        idCounter = 1;
        voltageField.setText("0.0");
        if (finalResultLabel != null) {
            finalResultLabel.setText("Обробка результатів: Очікування даних...");
        }
    }

    private void recordMeasurement() {
        double v = Math.round(getDoubleValue(voltageField, 0.0) * 10.0) / 10.0;

        for (Measurement m : data) {
            if (Math.abs(m.getVoltage() - v) < 0.05) {
                showAlert("Увага", "Показник для напруги " + v + " В вже записано!");
                return;
            }
        }

        double current = Math.round(canvas.getCurrentIa() * 10.0) / 10.0;
        double sqrtV = Math.round(Math.sqrt(v) * 100.0) / 100.0;

        Measurement m = new Measurement(idCounter++, v, current, sqrtV, 0.0, 0.0);
        data.add(m);

        data.sort((m1, m2) -> Double.compare(m1.getVoltage(), m2.getVoltage()));

        int id = 1;
        for (Measurement meas : data) {
            meas.setId(id++);
        }

        table.refresh();

        if (data.size() > 0) {
            Platform.runLater(() -> {
                try {
                    table.scrollTo(data.size() - 1);
                } catch (Exception e) {}
            });
        }
    }

    private void startAutoMode() {
        resetLab();
        if (!isPowerOn) togglePower();

        autoBtn.setDisable(true);
        voltageField.setDisable(true);
        recordBtn.setDisable(true);

        new Thread(() -> {
            try {
                for (double v : RECOMMENDED_V) {
                    Platform.runLater(() -> voltageField.setText(String.valueOf(v)));
                    Thread.sleep(300);
                    Platform.runLater(this::recordMeasurement);
                    Thread.sleep(200);
                }

                Platform.runLater(() -> {
                    autoBtn.setDisable(false);
                    voltageField.setDisable(false);
                    recordBtn.setDisable(false);
                    processAndShowCharts();
                });

            } catch (InterruptedException ignored) {}
        }).start();
    }

    private void processAndShowCharts() {
        if (data.size() < 5) {
            showAlert("Помилка", "Зберіть мінімум 5 точок для побудови графіка та диференціювання.");
            return;
        }

        List<Double> derivatives = new ArrayList<>();
        double maxDerivative = 0;
        double maxDerivativeSqrtV = 0;

        for (int i = 0; i < data.size() - 1; i++) {
            Measurement m1 = data.get(i);
            Measurement m2 = data.get(i+1);

            double dI = Math.abs(m2.getCurrent() - m1.getCurrent());
            double dSqrtV = m2.getSqrtVoltage() - m1.getSqrtVoltage();

            double deriv = (dSqrtV != 0) ? (dI / dSqrtV) : 0;
            derivatives.add(deriv);

            if (deriv > maxDerivative) {
                maxDerivative = deriv;
                maxDerivativeSqrtV = (m1.getSqrtVoltage() + m2.getSqrtVoltage()) / 2.0;
            }
        }
        derivatives.add(derivatives.get(derivatives.size() - 1));

        double alpha = 1.0 / maxDerivativeSqrtV;
        double c = 0.83 / maxDerivative;

        for (int i = 0; i < data.size(); i++) {
            Measurement m = data.get(i);
            m.setU(Math.round(alpha * m.getSqrtVoltage() * 100.0) / 100.0);
            m.setFU(Math.round(c * derivatives.get(i) * 1000.0) / 1000.0);
        }
        table.refresh();

        updateStatsFinal(alpha, c);
        drawDistributionCharts(derivatives, alpha, c);
    }

    private void drawDistributionCharts(List<Double> derivatives, double alpha, double cScale) {
        Stage chartStage = new Stage();
        chartStage.setTitle("Обробка результатів - Лабораторна 7.7");

        NumberAxis x1 = new NumberAxis(); x1.setLabel("√Δφ3");
        NumberAxis y1 = new NumberAxis(); y1.setLabel("Анодний струм I_a (мкА)");
        LineChart<Number, Number> chart1 = new LineChart<>(x1, y1);
        chart1.setTitle("Експериментальна крива гальмування");
        chart1.setLegendVisible(false);

        XYChart.Series<Number, Number> currentSeries = new XYChart.Series<>();
        for (Measurement m : data) {
            currentSeries.getData().add(new XYChart.Data<>(m.getSqrtVoltage(), m.getCurrent()));
        }
        chart1.getData().add(currentSeries);

        NumberAxis x2 = new NumberAxis(); x2.setLabel("Відносна швидкість U = v/v_i");
        NumberAxis y2 = new NumberAxis(); y2.setLabel("Функція розподілу f(U)");
        LineChart<Number, Number> chart2 = new LineChart<>(x2, y2);
        chart2.setTitle("Розподіл Максвелла: Теорія vs Експеримент");

        XYChart.Series<Number, Number> expSeries = new XYChart.Series<>();
        expSeries.setName("Експеримент f(U) експ");
        for (int i = 0; i < data.size(); i++) {
            Measurement m = data.get(i);
            expSeries.getData().add(new XYChart.Data<>(m.getU(), m.getFU()));
        }

        XYChart.Series<Number, Number> theorySeries = new XYChart.Series<>();
        theorySeries.setName("Теорія Максвелла");
        for (double u = 0; u <= 3.0; u += 0.1) {
            double f = (4.0 / Math.sqrt(Math.PI)) * Math.pow(u, 2) * Math.exp(-Math.pow(u, 2));
            theorySeries.getData().add(new XYChart.Data<>(u, f));
        }

        chart2.getData().addAll(theorySeries, expSeries);

        for (XYChart.Data<Number, Number> d : theorySeries.getData()) {
            if (d.getNode() != null) d.getNode().setStyle("-fx-background-color: transparent;");
        }
        theorySeries.getNode().setStyle("-fx-stroke: #1976d2; -fx-stroke-width: 2px; -fx-stroke-dash-array: 5 5;");

        expSeries.getNode().setStyle("-fx-stroke: #ef5350; -fx-stroke-width: 3px;");

        VBox layout = new VBox(10, chart1, chart2);
        VBox.setVgrow(chart1, Priority.ALWAYS);
        VBox.setVgrow(chart2, Priority.ALWAYS);
        layout.setPadding(new Insets(10));
        layout.setStyle("-fx-background-color: #f8fafc;");

        Scene scene = new Scene(layout, 800, 700);
        chartStage.setScene(scene);
        chartStage.show();
    }

    private void updateStatsFinal(double alpha, double c) {
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }

        String conclusion = String.format(Locale.US,
                "ОБРОБКА РЕЗУЛЬТАТІВ:\n" +
                        "1. Проведено чисельне диференціювання експериментальної кривої I_a(√Δφ3).\n" +
                        "2. Знайдено нормувальні коефіцієнти: α = %.3f, c = %.3f (максимум розподілу при U=1).\n" +
                        "3. Побудовано експериментальну криву розподілу швидкостей f(U).\n" +
                        "ВИСНОВОК: Форма кривої (максимум біля U=1 та асиметрія) дуже добре узгоджується з теоретичним законом Максвелла. Зі збільшенням температури катода крива струму стає більш похилою.",
                alpha, c
        );

        finalResultLabel.setText(conclusion);
    }
}