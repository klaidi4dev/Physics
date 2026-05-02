package dev.ua._klaidi4_.physics.level8.lab8_5.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level8.lab8_5.model.Measurement;
import dev.ua._klaidi4_.physics.level8.lab8_5.view.TunnelDiodeCanvas;
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
import java.util.Locale;
import java.util.Queue;

public class LabController85 extends BaseLabController {

    private TunnelDiodeCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> modeBox;
    private ComboBox<String> materialBox;
    private TextField voltageField;
    private TextField dopingField;
    private TextField tempField;
    private Button measureBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveModeLabel;
    private Label liveVoltLabel;
    private Label liveCurrLabel;
    private Label liveStatusLabel;
    private boolean isMeasuring = false;
    private AnimationTimer autoTimer;
    private Queue<Double> autoQueue = new LinkedList<>();

    public LabController85() {
        initUI();
        canvas.updateState(0.0, "Прямий", 0.0);
    }

    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        if (autoTimer != null) autoTimer.stop();
        autoQueue.clear();
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(320);
        leftPanel.setMinWidth(320);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Управління (Лаб 8-5)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane configPane = new TitledPane();
        configPane.setText("Параметри діода та середовища");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(10);
        configBox.setPadding(new Insets(10, 5, 10, 5));

        materialBox = new ComboBox<>(FXCollections.observableArrayList(
                "Германій (Ge) - ГІ103",
                "Арсенід Галію (GaAs)"
        ));
        materialBox.getSelectionModel().selectFirst();
        materialBox.setMaxWidth(Double.MAX_VALUE);
        materialBox.setOnAction(e -> resetForModeChange());

        dopingField = new TextField("1.0");
        dopingField.setPromptText("Множник (0.1 ... 1.5)");
        dopingField.textProperty().addListener((obs, oldVal, newVal) -> updateLivePreview());

        tempField = new TextField("20.0");
        tempField.textProperty().addListener((obs, oldVal, newVal) -> updateLivePreview());

        configBox.getChildren().addAll(
                new VBox(4, createLabel("Матеріал діода:"), materialBox),
                createInputGroup("Рівень легування (множник N):", dopingField),
                createInputGroup("Температура T (°C):", tempField)
        );
        configPane.setContent(configBox);

        TitledPane powerPane = new TitledPane();
        powerPane.setText("Джерело живлення");
        powerPane.setCollapsible(false);

        VBox powerBox = new VBox(10);
        powerBox.setPadding(new Insets(10, 5, 10, 5));

        modeBox = new ComboBox<>(FXCollections.observableArrayList(
                "Прямий",
                "Зворотний"
        ));
        modeBox.getSelectionModel().selectFirst();
        modeBox.setMaxWidth(Double.MAX_VALUE);
        modeBox.setOnAction(e -> resetForModeChange());

        voltageField = new TextField("0.00");
        voltageField.textProperty().addListener((obs, oldVal, newVal) -> updateLivePreview());

        powerBox.getChildren().addAll(
                new VBox(4, createLabel("Режим включення:"), modeBox),
                createInputGroup("Напруга U (В):", voltageField)
        );
        powerPane.setContent(powerBox);

        measureBtn = new Button("🔴 ВИМІРЯТИ СТРУМ");
        measureBtn.setStyle("-fx-background-color: #ff007f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
        measureBtn.setMaxWidth(Double.MAX_VALUE);
        measureBtn.setOnAction(e -> startSingleMeasurement());

        autoBtn = new Button("⚙ АВТОМАТИЧНА СЕРІЯ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAutoMode());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear();
            idCounter = 1;
            updateStats();
            liveStatusLabel.setText("Статус: Очікування");
            liveStatusLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
        });

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, configPane, powerPane, measureBtn, autoBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");

        leftPanel.getChildren().add(scrollLeft);

        canvas = new TunnelDiodeCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(4);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-text-fill: #00e5ff; -fx-padding: 10; -fx-border-color: #00e5ff; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(240, 120);

        Label dashTitle = new Label("МІКРОАМПЕРМЕТР");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        liveStatusLabel = new Label("Статус: Очікування");
        liveStatusLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");

        liveModeLabel = new Label("Режим: Прямий");
        liveModeLabel.setStyle("-fx-text-fill: #ffeb3b; -fx-font-size: 12px;");

        liveVoltLabel = new Label("U = 0.00 В");
        liveVoltLabel.setStyle("-fx-text-fill: #00ffcc; -fx-font-size: 14px; -fx-font-weight: bold;");

        liveCurrLabel = new Label("I = 0.0 мкА");
        liveCurrLabel.setStyle("-fx-text-fill: #ff007f; -fx-font-size: 14px; -fx-font-weight: bold;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, new Separator(), liveModeLabel, liveVoltLabel, liveCurrLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, String> modeCol = new TableColumn<>("Режим");
        modeCol.setCellValueFactory(new PropertyValueFactory<>("mode"));
        TableColumn<Measurement, Double> uCol = new TableColumn<>("U (В)");
        uCol.setCellValueFactory(new PropertyValueFactory<>("voltage"));
        TableColumn<Measurement, Double> iCol = new TableColumn<>("I (мкА)");
        iCol.setCellValueFactory(new PropertyValueFactory<>("current"));

        table.getColumns().addAll(idCol, modeCol, uCol, iCol);
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

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        label.setStyle("-fx-text-fill: #475569;");
        return label;
    }

    private double getDoubleValue(TextField field, double defaultValue) {
        try {
            return Double.parseDouble(field.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void resetForModeChange() {
        data.clear();
        idCounter = 1;
        voltageField.setText("0.00");
        updateLivePreview();
        updateStats();
        liveModeLabel.setText("Режим: " + modeBox.getValue());
    }

    private void updateLivePreview() {
        if (isMeasuring) return;

        double u = getDoubleValue(voltageField, 0.0);
        String mode = modeBox.getValue();
        double i = calculateCurrent(u, mode);

        canvas.updateState(u, mode, i);
        liveVoltLabel.setText(String.format(Locale.US, "U = %.3f В", u));
        liveCurrLabel.setText(String.format(Locale.US, "I = %.1f мкА", i));
    }

    private void setControlsDisable(boolean disable) {
        materialBox.setDisable(disable);
        dopingField.setDisable(disable);
        tempField.setDisable(disable);
        modeBox.setDisable(disable);
        voltageField.setDisable(disable);
        measureBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
    }

    private void startSingleMeasurement() {
        double targetU = getDoubleValue(voltageField, 0.0);
        String mode = modeBox.getValue();
        performMeasurement(targetU, mode, false);
    }

    private void performMeasurement(double u, String mode, boolean isAuto) {
        isMeasuring = true;
        if (!isAuto) setControlsDisable(true);
        liveStatusLabel.setText("Статус: Встановлення режиму...");
        liveStatusLabel.setStyle("-fx-text-fill: #ffeb3b;");

        double targetI = calculateCurrent(u, mode);
        double currentNoise = targetI * (Math.random() - 0.5) * 0.02;
        final double finalI = Math.max(0, targetI + currentNoise);

        canvas.updateState(u, mode, finalI);

        new Thread(() -> {
            try {
                Thread.sleep(500);
                Platform.runLater(() -> {
                    liveVoltLabel.setText(String.format(Locale.US, "U = %.3f В", u));
                    liveCurrLabel.setText(String.format(Locale.US, "I = %.1f мкА", finalI));

                    Measurement m = new Measurement(idCounter++, mode, Math.round(u * 1000.0) / 1000.0, Math.round(finalI * 10.0) / 10.0);
                    data.add(m);
                    table.scrollTo(m);
                    updateStats();

                    liveStatusLabel.setText("Статус: Виміряно");
                    liveStatusLabel.setStyle("-fx-text-fill: #a3e635;");
                    isMeasuring = false;

                    if (isAuto) {
                        processNextAuto();
                    } else {
                        setControlsDisable(false);
                    }
                });
            } catch (InterruptedException ignored) {}
        }).start();
    }

    private void startAutoMode() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        String mode = modeBox.getValue();
        if (mode.equals("Прямий")) {
            double[] steps = {0, 0.01, 0.02, 0.03, 0.04, 0.045, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1, 0.15, 0.2, 0.25, 0.3, 0.4};
            for (double v : steps) autoQueue.add(v);
        } else {
            double[] steps = {0, 0.01, 0.02, 0.03, 0.035, 0.04, 0.045, 0.05, 0.06, 0.1};
            for (double v : steps) autoQueue.add(v);
        }

        setControlsDisable(true);
        processNextAuto();
    }

    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            liveStatusLabel.setText("АВТОСЕРІЯ ЗАВЕРШЕНА");
            liveStatusLabel.setStyle("-fx-text-fill: #00ffcc; -fx-font-weight: bold;");
            setControlsDisable(false);
            return;
        }
        double nextU = autoQueue.poll();
        voltageField.setText(String.format(Locale.US, "%.3f", nextU));
        performMeasurement(nextU, modeBox.getValue(), true);
    }

    private double calculateCurrent(double u, String mode) {
        double dopingMultiplier = getDoubleValue(dopingField, 1.0);
        if (dopingMultiplier < 0.01) dopingMultiplier = 0.01;

        double tempC = getDoubleValue(tempField, 20.0);
        double tempFactorDiff = Math.pow(2.0, (tempC - 20) / 10.0);
        double tempFactorTunnel = 1.0 - (tempC - 20) * 0.001;
        boolean isGaAs = materialBox.getSelectionModel().getSelectedIndex() == 1;
        double voltageStretch = isGaAs ? 2.0 : 1.0;

        u = Math.abs(u);
        double scaledU = u / voltageStretch;

        if (mode.equals("Зворотний")) {
            double[] vPts = {0, 0.01, 0.02, 0.03, 0.035, 0.04, 0.045, 0.05, 0.06, 0.1};
            double[] iPts = {0, 7, 15, 22, 28, 35, 40, 45, 57, 120};
            double baseI = interpolate(scaledU, vPts, iPts);
            return baseI * dopingMultiplier * tempFactorTunnel;

        } else {
            double[] vPts = {0, 0.01, 0.02, 0.03, 0.035, 0.04, 0.045, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1, 0.15, 0.2, 0.25, 0.3, 0.4, 0.5};
            double[] iPts = {0, 20, 33, 42, 47, 53, 57, 60, 64, 55, 48, 44, 41, 30, 23, 28, 40, 120, 250};

            double baseI = interpolate(scaledU, vPts, iPts);
            double tunnelComponent = 0;
            double diffusionComponent = 0;

            if (scaledU < 0.2) {
                tunnelComponent = baseI;
                diffusionComponent = Math.max(0, (scaledU - 0.15) * 10);
            } else {
                tunnelComponent = Math.max(0, 23 - (scaledU - 0.2) * 50);
                diffusionComponent = baseI - tunnelComponent;
                if(diffusionComponent < 0) diffusionComponent = baseI;
            }

            double actualTunnel = tunnelComponent * Math.pow(dopingMultiplier, 2) * tempFactorTunnel;
            double actualDiffusion = diffusionComponent * tempFactorDiff;

            return actualTunnel + actualDiffusion;
        }
    }

    private double interpolate(double x, double[] xPts, double[] yPts) {
        if (x <= xPts[0]) return yPts[0];
        if (x >= xPts[xPts.length - 1]) {
            int last = xPts.length - 1;
            double slope = (yPts[last] - yPts[last - 1]) / (xPts[last] - xPts[last - 1]);
            return yPts[last] + slope * (x - xPts[last]);
        }
        for (int i = 0; i < xPts.length - 1; i++) {
            if (x >= xPts[i] && x <= xPts[i + 1]) {
                double t = (x - xPts[i]) / (xPts[i + 1] - xPts[i]);
                return yPts[i] + t * (yPts[i + 1] - yPts[i]);
            }
        }
        return 0;
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

        double maxI = -1, minI = Double.MAX_VALUE, maxU = -1, minU = -1;
        boolean peakFound = false;

        for (Measurement m : data) {
            if (m.getMode().equals("Прямий")) {
                if (!peakFound && m.getCurrent() >= maxI) {
                    maxI = m.getCurrent();
                    maxU = m.getVoltage();
                } else if (m.getCurrent() < maxI) {
                    peakFound = true;
                }
                if (peakFound && m.getCurrent() <= minI && m.getVoltage() > maxU) {
                    minI = m.getCurrent();
                    minU = m.getVoltage();
                }
            }
        }

        if (maxI != -1 && peakFound && minI != Double.MAX_VALUE && minU > maxU) {
            double rDiff = (minU - maxU) / ((minI - maxI) * 1e-6);

            String conclusion = String.format(Locale.US,
                    "ОБРОБКА РЕЗУЛЬТАТІВ:\n" +
                            "1. Струм піку: I_m = %.1f мкА при U_m = %.3f В.\n" +
                            "2. Струм впадини: I_min = %.1f мкА при U_min = %.3f В.\n" +
                            "3. Диференціальний опір на ділянці спаду (АВ): R_diff ≈ %.0f Ом.\n" +
                            "ВИСНОВОК: Наявність від'ємного диференціального опору підтверджує переважання тунельного ефекту при малих прямих напругах. %s",
                    maxI, maxU, minI, minU, rDiff,
                    (getDoubleValue(dopingField, 1.0) < 0.5) ? "При низькому легуванні тунельний пік згладжується." : "");
            finalResultLabel.setText(conclusion);
        } else {
            finalResultLabel.setText("Для розрахунку диференціального опору виконайте повну серію вимірів у прямому режимі (перейдіть через точку максимуму).");
        }
    }
}