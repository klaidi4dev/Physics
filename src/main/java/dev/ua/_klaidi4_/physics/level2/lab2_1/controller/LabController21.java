package dev.ua._klaidi4_.physics.level2.lab2_1.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level2.lab2_1.model.Measurement;
import dev.ua._klaidi4_.physics.level2.lab2_1.view.ElectrostaticCanvas;
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

public class LabController21 extends BaseLabController {

    private ElectrostaticCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;

    private Slider voltageSlider;
    private Slider distSlider;
    private Slider radiusSlider;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;

    private Label liveStatusLabel;
    private Label liveVoltageLabel;

    private Queue<Double> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    public LabController21() {
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
        leftPanel.setPrefWidth(320);
        leftPanel.setMinWidth(320);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 2-1)");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));

        TitledPane paramsPane = new TitledPane();
        paramsPane.setText("Параметри установки");
        paramsPane.setCollapsible(false);
        VBox paramsBox = new VBox(8);
        paramsBox.setPadding(new Insets(5));

        Label voltageLabel = new Label("Напруга між електродами U: 20.0 В");
        voltageSlider = new Slider(10.0, 30.0, 20.0);
        voltageSlider.setShowTickMarks(true);
        voltageSlider.setShowTickLabels(true);
        voltageSlider.setMajorTickUnit(5.0);
        voltageSlider.valueProperty().addListener((o, old, newVal) -> {
            voltageLabel.setText(String.format("Напруга між електродами U: %.1f В", newVal.doubleValue()));
            updateDisplayFromSlider();
        });

        Label distLabel = new Label("Відстань між електродами d: 20.0 см");
        distSlider = new Slider(10.0, 30.0, 20.0);
        distSlider.setShowTickMarks(true);
        distSlider.setShowTickLabels(true);
        distSlider.setMajorTickUnit(5.0);
        distSlider.valueProperty().addListener((o, old, newVal) -> {
            distLabel.setText(String.format("Відстань між електродами d: %.1f см", newVal.doubleValue()));
            updateDisplayFromSlider();
        });

        Label radiusLabel = new Label("Радіус електрода r: 1.0 см");
        radiusSlider = new Slider(0.5, 2.5, 1.0);
        radiusSlider.setShowTickMarks(true);
        radiusSlider.setShowTickLabels(true);
        radiusSlider.setMajorTickUnit(0.5);
        radiusSlider.valueProperty().addListener((o, old, newVal) -> {
            radiusLabel.setText(String.format("Радіус електрода r: %.1f см", newVal.doubleValue()));
            updateDisplayFromSlider();
        });

        paramsBox.getChildren().addAll(voltageLabel, voltageSlider, distLabel, distSlider, radiusLabel, radiusSlider);
        paramsPane.setContent(paramsBox);

        startBtn = new Button("▶ ПОБУДУВАТИ ЛІНІЇ ПОЛЯ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startMeasurement(voltageSlider.getValue()));

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ ЛАБИ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAuto());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> resetAndRecalculate());

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, paramsPane, startBtn, autoBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

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
        dash.setMaxSize(200, 60);

        Label dashTitle = new Label("ДАТЧИК ПОТЕНЦІАЛУ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ГОТОВО");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveVoltageLabel = new Label("U = 0.0 В");
        liveVoltageLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold; -fx-font-size: 14px;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveVoltageLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        String[] cols = {"№", "φ1 (В)", "φ2 (В)", "Δφ (В)", "x1 (см)", "x2 (см)", "Δx (см)", "E (В/м)"};
        String[] props = {"id", "phi1", "phi2", "dPhi", "x1", "x2", "dX", "eField"};

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
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        updateDisplayFromSlider();
        updateStats();
    }

    private void resetAndRecalculate() {
        data.clear();
        idCounter = 1;
        canvas.resetSystem();
        updateStats();
        updateDisplayFromSlider();
    }

    private void updateDisplayFromSlider() {
        if (isAutoRunning) return;
        double u = voltageSlider.getValue();
        double d = distSlider.getValue();
        double r = radiusSlider.getValue();
        canvas.updatePhysicsParameters(u, d, r);
        liveVoltageLabel.setText(String.format("U = %.1f В", u));
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        voltageSlider.setDisable(disable);
        distSlider.setDisable(disable);
        radiusSlider.setDisable(disable);
    }

    private void startAuto() {
        resetAndRecalculate();
        autoQueue.add(10.0);
        autoQueue.add(20.0);
        autoQueue.add(30.0);

        isAutoRunning = true;
        setControlsDisable(true);
        processNextAuto();
    }

    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoRunning = false;
            setControlsDisable(false);
            liveStatusLabel.setText("Статус: ГОТОВО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            return;
        }
        double u = autoQueue.poll();
        voltageSlider.setValue(u);
        startMeasurement(u);
    }

    private void startMeasurement(double u) {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: СКАНУВАННЯ...");
        liveStatusLabel.setStyle("-fx-text-fill: red;");

        double d = distSlider.getValue() / 100.0;
        double r = radiusSlider.getValue() / 100.0;

        canvas.startSimulation();

        new Thread(() -> {
            try { Thread.sleep(2500); } catch (Exception ignored) {}
            Platform.runLater(() -> recordMeasurementData(u, d, r));
        }).start();
    }

    private void recordMeasurementData(double u0, double d, double r) {
        double currentX = 0;
        double currentPhi = 0;
        double stepPhi = u0 / 5.0;

        for (int i = 1; i <= 4; i++) {
            double phi2 = currentPhi + stepPhi;
            double targetV = phi2;

            double k = Math.exp((2 * targetV * Math.log(d / r)) / u0);
            double x2 = (d / 2.0) * (k - 1) / (k + 1);

            double dX = x2 - currentX;
            double eField = stepPhi / dX;

            Measurement m = new Measurement(
                    idCounter++,
                    Math.round(currentPhi * 100.0) / 100.0,
                    Math.round(phi2 * 100.0) / 100.0,
                    Math.round(stepPhi * 100.0) / 100.0,
                    Math.round((currentX * 100.0) * 100.0) / 100.0,
                    Math.round((x2 * 100.0) * 100.0) / 100.0,
                    Math.round((dX * 100.0) * 100.0) / 100.0,
                    Math.round(eField)
            );
            data.add(m);

            currentX = x2;
            currentPhi = phi2;
        }

        updateStats();

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(1000); } catch (Exception ignored) {}
                Platform.runLater(this::processNextAuto);
            }).start();
        } else {
            liveStatusLabel.setText("Статус: ДАНІ ЗАПИСАНО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            setControlsDisable(false);
        }
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
        double d = distSlider.getValue() / 100.0;
        double r = radiusSlider.getValue() / 100.0;

        double eps0 = 8.854e-12;
        double cLinear = (Math.PI * eps0) / Math.log(d / r);

        StringBuilder sb = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ ДОСЛІДЖЕННЯ:\n");
        sb.append("1. На карті побудовано еквіпотенціальні поверхні та лінії напруженості електричного поля.\n");
        sb.append("2. Розраховано напруженість електричного поля E = Δφ/Δx на кожному з відрізків (див. таблицю).\n");
        sb.append(String.format("3. Погонна ємність модельованої системи (ε=1): Cl = %.2e Ф/м (%.2f пФ/м).\n", cLinear, cLinear * 1e12));
        sb.append("4. Висновок: Електричне поле між двома електродами неоднорідне. Напруженість поля E зростає при наближенні до електродів.");

        finalResultLabel.setText(sb.toString());
    }
}