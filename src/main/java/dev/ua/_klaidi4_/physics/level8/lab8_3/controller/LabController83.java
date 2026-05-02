package dev.ua._klaidi4_.physics.level8.lab8_3.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level8.lab8_3.model.Measurement;
import dev.ua._klaidi4_.physics.level8.lab8_3.view.PhotoresistorCanvas;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Locale;

public class LabController83 extends BaseLabController {

    private PhotoresistorCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField voltageField;
    private TextField distanceField;
    private Button manualMeasureBtn;
    private Button autoRunBtn;
    private Button clearBtn;
    private boolean isAutoRunning = false;
    private final double LIGHT_INTENSITY_CD = 1.8;
    private final double AREA_S_M2 = 2.78e-5;

    public LabController83() {
        initUI();
        canvas.setTarget(10.0, 30.0);
    }

    @Override
    public void shutdown() {
        if (canvas != null) {
            canvas.stopAnimation();
        }
        isAutoRunning = false;
    }

    private void initUI() {
        leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(320);
        leftPanel.setMinWidth(320);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Управління установкою (8-3)");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));

        TitledPane paramsPane = new TitledPane();
        paramsPane.setText("Керування параметрами");
        paramsPane.setCollapsible(false);

        VBox paramsBox = new VBox(10);
        paramsBox.setPadding(new Insets(10));

        voltageField = new TextField("10");
        distanceField = new TextField("30");

        paramsBox.getChildren().addAll(
                createInputGroup("Напруга U (В) [0...150]:", voltageField),
                createInputGroup("Відстань до лампи R (см) [5...40]:", distanceField)
        );
        paramsPane.setContent(paramsBox);

        manualMeasureBtn = new Button("🔴 ЗАПИСАТИ ВИМІРЮВАННЯ");
        manualMeasureBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        manualMeasureBtn.setMaxWidth(Double.MAX_VALUE);
        manualMeasureBtn.setOnAction(e -> doManualMeasurement());

        autoRunBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ ЛАБИ");
        autoRunBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
        autoRunBtn.setMaxWidth(Double.MAX_VALUE);
        autoRunBtn.setOnAction(e -> startAutoMode());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> clearData());

        leftPanel.getChildren().addAll(title, paramsPane, manualMeasureBtn, new Separator(), autoRunBtn, clearBtn);

        canvas = new PhotoresistorCanvas(600, 440);

        StackPane centerPanel = new StackPane(canvas);
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Measurement, Double> uCol = new TableColumn<>("U (В)");
        uCol.setCellValueFactory(new PropertyValueFactory<>("voltage"));

        TableColumn<Measurement, Double> rCol = new TableColumn<>("R (см)");
        rCol.setCellValueFactory(new PropertyValueFactory<>("distance"));

        TableColumn<Measurement, Double> eCol = new TableColumn<>("E (лк)");
        eCol.setCellValueFactory(new PropertyValueFactory<>("illuminance"));

        TableColumn<Measurement, Double> iCol = new TableColumn<>("I_ф (мА)");
        iCol.setCellValueFactory(new PropertyValueFactory<>("current"));

        table.getColumns().addAll(idCol, uCol, rCol, eCol, iCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        voltageField.textProperty().addListener((obs, oldVal, newVal) -> updateLivePreview());
        distanceField.textProperty().addListener((obs, oldVal, newVal) -> updateLivePreview());
    }

    private void updateLivePreview() {
        if (isAutoRunning) return;
        try {
            double u = Double.parseDouble(voltageField.getText().replace(",", "."));
            double r = Double.parseDouble(distanceField.getText().replace(",", "."));

            if (r < 5) r = 5;
            if (r > 40) r = 40;

            canvas.setTarget(u, r);
        } catch (NumberFormatException ignored) {
        }
    }

    private double calculatePhysicsModel(double u, double r_cm) {
        if (r_cm <= 0 || u <= 0) return 0;
        return (u / 10.0) * Math.pow(10.0 / r_cm, 1.8) * 0.1;
    }

    private void doManualMeasurement() {
        if (isAutoRunning) return;
        try {
            double u = Double.parseDouble(voltageField.getText().replace(",", "."));
            double r = Double.parseDouble(distanceField.getText().replace(",", "."));
            recordMeasurementExact(u, r);
        } catch (NumberFormatException ex) {
            showAlert("Помилка", "Будь ласка, введіть коректні числові значення.");
        }
    }

    private void recordMeasurementExact(double u, double r_cm) {
        double rawCurrent = calculatePhysicsModel(u, r_cm);

        double measurementNoise = 1.0 + (Math.random() - 0.5) * 0.02;
        rawCurrent *= measurementNoise;

        double r_m = r_cm / 100.0;
        double rawIlluminance = LIGHT_INTENSITY_CD / (r_m * r_m);

        final double currentMA = Math.round(rawCurrent * 1000.0) / 1000.0;
        final double illuminance = Math.round(rawIlluminance * 10.0) / 10.0;
        final double finalU = Math.round(u * 10.0) / 10.0;
        final double finalR = Math.round(r_cm * 10.0) / 10.0;

        Measurement meas = new Measurement(idCounter++, finalU, finalR, illuminance, currentMA);
        data.add(meas);

        Platform.runLater(() -> {
            updateStats();
            table.scrollTo(meas);
        });
    }

    private void clearData() {
        if (isAutoRunning) return;
        data.clear();
        idCounter = 1;
        updateStats();
    }

    private void startAutoMode() {
        if (isAutoRunning) return;
        isAutoRunning = true;

        voltageField.setDisable(true);
        distanceField.setDisable(true);
        manualMeasureBtn.setDisable(true);
        autoRunBtn.setDisable(true);
        clearBtn.setDisable(true);

        data.clear();
        idCounter = 1;

        Thread autoThread = new Thread(() -> {
            try {
                for (int u = 10; u <= 70; u += 10) {
                    if (!isAutoRunning) break;

                    final double stepU = u;
                    final double stepR = 20.0;

                    Platform.runLater(() -> {
                        voltageField.setText(String.valueOf((int)stepU));
                        distanceField.setText(String.valueOf((int)stepR));
                        canvas.setTarget(stepU, stepR);
                    });

                    Thread.sleep(1000);
                    Platform.runLater(() -> recordMeasurementExact(stepU, stepR));
                    Thread.sleep(200);
                }

                for (int r = 10; r <= 35; r += 5) {
                    if (!isAutoRunning) break;

                    final double stepU = 80.0;
                    final double stepR = r;

                    Platform.runLater(() -> {
                        voltageField.setText(String.valueOf((int)stepU));
                        distanceField.setText(String.valueOf((int)stepR));
                        canvas.setTarget(stepU, stepR);
                    });

                    Thread.sleep(1200);
                    Platform.runLater(() -> recordMeasurementExact(stepU, stepR));
                    Thread.sleep(200);
                }

                Platform.runLater(() -> {
                    voltageField.setDisable(false);
                    distanceField.setDisable(false);
                    manualMeasureBtn.setDisable(false);
                    autoRunBtn.setDisable(false);
                    clearBtn.setDisable(false);
                    isAutoRunning = false;
                });

            } catch (InterruptedException e) {
                isAutoRunning = false;
            }
        });

        autoThread.setDaemon(true);
        autoThread.start();
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

        Measurement last = data.get(data.size() - 1);

        if (last.getVoltage() <= 0) {
            finalResultLabel.setText("Для розрахунку K0 напруга повинна бути більше 0.");
            return;
        }

        double iAmp = last.getCurrent() * 1e-3;
        double rMeters = last.getDistance() / 100.0;

        double k0 = (iAmp * Math.pow(rMeters, 2)) / (LIGHT_INTENSITY_CD * AREA_S_M2 * last.getVoltage());

        String analysis = String.format(Locale.US,
                "ОБРОБКА РЕЗУЛЬТАТІВ (Останній вимір):\n" +
                        "1. Напруга: %.1f В, Відстань: %.1f см.\n" +
                        "2. Освітленість: E = I / R² = %.1f лк.\n" +
                        "3. Питома чутливість K0 = (I_ф * R²) / (I * S * U) = %.4f А/(лм·В).\n" +
                        "ВИСНОВОК: Дані підтверджують лінійну залежність I_ф від U (ВАХ) та різке падіння фотоструму при віддаленні лампи.",
                last.getVoltage(), last.getDistance(), last.getIlluminance(), k0);

        finalResultLabel.setText(analysis);
    }
}