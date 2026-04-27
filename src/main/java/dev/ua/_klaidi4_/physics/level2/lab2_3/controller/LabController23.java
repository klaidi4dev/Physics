package dev.ua._klaidi4_.physics.level2.lab2_3.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level2.lab2_3.model.Measurement;
import dev.ua._klaidi4_.physics.level2.lab2_3.view.BridgeCanvas;
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
import java.util.Queue;

public class LabController23 extends BaseLabController {

    private BridgeCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField eps0Field, refBaTiO3Field, refSaltField, refGlassField;
    private ComboBox<String> materialCombo;
    private TextField lengthAField, widthBField, thicknessDField;
    private Button startBtn, autoBtn, clearBtn;
    private Label liveStatusLabel, liveCapLabel;
    private AnimationTimer measurementTimer;
    private long startTime;
    private double targetCapacitance;
    private boolean isMeasuring = false;
    private Queue<double[]> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    public LabController23() {
        initUI();
    }

    @Override
    public void shutdown() {
        if (measurementTimer != null) measurementTimer.stop();
        isAutoRunning = false;
        autoQueue.clear();
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 2-3)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane constPane = new TitledPane();
        constPane.setText("Константи та еталони");
        constPane.setCollapsible(true);
        VBox constBox = new VBox(12);
        constBox.setPadding(new Insets(5));

        eps0Field = new TextField("8.854e-12");
        refBaTiO3Field = new TextField("1500.0");
        refSaltField = new TextField("4000.0");
        refGlassField = new TextField("7.0");

        constBox.getChildren().addAll(
                createInputGroup("ε₀ (Ф/м):", eps0Field),
                createInputGroup("ε еталон (BaTiO3):", refBaTiO3Field),
                createInputGroup("ε еталон (Сегн. сіль):", refSaltField),
                createInputGroup("ε еталон (Скло):", refGlassField)
        );
        constPane.setContent(constBox);

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри зразка");
        labPane.setCollapsible(false);
        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        materialCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Титанат барію (BaTiO3)", "Сегнетова сіль", "Скло"
        ));
        materialCombo.getSelectionModel().selectFirst();
        materialCombo.setOnAction(e -> {
            updateCanvasVisuals();
            updateStats();
        });

        lengthAField = new TextField("10.0");
        widthBField = new TextField("10.0");
        thicknessDField = new TextField("2.0");
        thicknessDField.textProperty().addListener((o, ov, nv) -> updateCanvasVisuals());

        paramsBox.getChildren().addAll(
                createInputGroup("Матеріал зразка:", materialCombo),
                createInputGroup("Довжина a (мм):", lengthAField),
                createInputGroup("Ширина b (мм):", widthBField),
                createInputGroup("Товщина d (мм):", thicknessDField)
        );
        labPane.setContent(paramsBox);

        startBtn = new Button("▶ ВИМІРЯТИ ЄМНІСТЬ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManualMeasurement());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ ЛАБИ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAutoMeasurement());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear();
            idCounter = 1;
            updateStats();
            canvas.updateDisplay("---- pF", false);
            liveCapLabel.setText("C = 0.00 pF");
        });

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, constPane, labPane, startBtn, autoBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new BridgeCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(220, 80);
        Label dashTitle = new Label("ВИМІРЮВАЧ Р-577");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveCapLabel = new Label("C = 0.00 pF");
        liveCapLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        liveCapLabel.setStyle("-fx-text-fill: #00ff00;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveCapLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        table.getColumns().addAll(
                createCol("№", "id"),
                createCol("Матеріал", "material"),
                createCol("a (мм)", "a"),
                createCol("b (мм)", "b"),
                createCol("d (мм)", "d"),
                createCol("C (пФ)", "capacitance"),
                createCol("ε", "epsilon")
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(130);

        VBox statsBox = createStatsBox();

        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        updateCanvasVisuals();
        updateStats();
    }

    private TableColumn<Measurement, Object> createCol(String title, String prop) {
        TableColumn<Measurement, Object> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        return col;
    }

    private void updateCanvasVisuals() {
        try {
            double d = Double.parseDouble(thicknessDField.getText());
            canvas.updateSample(d, materialCombo.getValue());
        } catch (Exception ignored) {}
    }

    private double getTargetEpsilon() {
        try {
            String mat = materialCombo.getValue();
            if (mat.contains("BaTiO3")) return Double.parseDouble(refBaTiO3Field.getText());
            if (mat.contains("сіль")) return Double.parseDouble(refSaltField.getText());
            return Double.parseDouble(refGlassField.getText());
        } catch (Exception e) { return 1.0; }
    }

    private void startManualMeasurement() {
        try {
            double a = Double.parseDouble(lengthAField.getText());
            double b = Double.parseDouble(widthBField.getText());
            double d = Double.parseDouble(thicknessDField.getText());
            isAutoRunning = false;
            runMeasurementSimulation(a, b, d, materialCombo.getValue());
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа в поля розмірів.");
        }
    }

    private void startAutoMeasurement() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        autoQueue.add(new double[]{10.0, 10.0, 2.0});
        autoQueue.add(new double[]{10.0, 10.0, 1.5});
        autoQueue.add(new double[]{12.0, 12.0, 2.0});
        isAutoRunning = true;
        processNextAuto();
    }

    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoRunning = false;
            liveStatusLabel.setText("Статус: ГОТОВО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            setControlsDisable(false);
            return;
        }
        setControlsDisable(true);
        double[] p = autoQueue.poll();
        lengthAField.setText(String.valueOf(p[0]));
        widthBField.setText(String.valueOf(p[1]));
        thicknessDField.setText(String.valueOf(p[2]));
        updateCanvasVisuals();
        runMeasurementSimulation(p[0], p[1], p[2], materialCombo.getValue());
    }

    private void runMeasurementSimulation(double aMm, double bMm, double dMm, String mat) {
        try {
            isMeasuring = true;
            setControlsDisable(true);
            liveStatusLabel.setText("Статус: ВИМІРЮВАННЯ");
            liveStatusLabel.setStyle("-fx-text-fill: red;");

            double S = (aMm / 1000.0) * (bMm / 1000.0);
            double eps0 = Double.parseDouble(eps0Field.getText());
            double trueEps = getTargetEpsilon();
            double cPico = ((eps0 * trueEps * S) / (dMm / 1000.0)) * 1e12;
            targetCapacitance = cPico + cPico * (Math.random() * 0.04 - 0.02);

            startTime = System.nanoTime();
            measurementTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    double elapsed = (now - startTime) / 1_000_000_000.0;
                    double fluctuatingCap = targetCapacitance * (0.8 + Math.random() * 0.4);

                    canvas.updateDisplay(String.format("%.1f pF", fluctuatingCap), true);
                    liveCapLabel.setText(String.format("C = %.1f pF", fluctuatingCap));

                    if (elapsed >= 1.5) {
                        this.stop();
                        finishMeasurement(aMm, bMm, dMm, S, mat, eps0);
                    }
                }
            };
            measurementTimer.start();
        } catch (Exception e) {
            isMeasuring = false;
            setControlsDisable(false);
        }
    }

    private void finishMeasurement(double a, double b, double d, double S, String mat, double eps0) {
        isMeasuring = false;

        canvas.updateDisplay(String.format("%.2f pF", targetCapacitance), false);
        liveCapLabel.setText(String.format("C = %.2f pF", targetCapacitance));
        liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

        double calcEps = (targetCapacitance * 1e-12 * (d / 1000.0)) / (eps0 * S);

        Measurement meas = new Measurement(idCounter++, mat, a, b, d,
                Math.round(S*1e6)/1e6,
                Math.round(targetCapacitance*100)/100.0,
                Math.round(calcEps));
        data.add(meas);
        updateStats();

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(800); Platform.runLater(this::processNextAuto); } catch (Exception ignored) {}
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
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }
        double sumEps = 0;
        for (Measurement m : data) {
            sumEps += m.getEpsilon();
        }
        double avgEps = sumEps / data.size();

        double targetEps = getTargetEpsilon();
        double absError = Math.abs(avgEps - targetEps);
        double relError = (absError / targetEps) * 100.0;

        String materialName = materialCombo.getValue();

        String conclusionText = String.format(
                "Обробка результатів експерименту:\n" +
                        "1. Вирахувано відносну діелектричну проникність сегнетоелектрика (%s) за формулою C = εε₀S/d.\n" +
                        "   Експериментальне середнє значення: ε = %.0f.\n" +
                        "2. Абсолютна похибка вимірювань: Δε = %.0f. Відносна похибка: ε_відн = %.1f %%.\n" +
                        "3. Висновок: Експериментальні результати %s з теоретичним значенням (ε = %.0f).",
                materialName, avgEps, absError, relError,
                (relError <= 5.0 ? "добре збігаються" : "мають суттєве відхилення"), targetEps
        );

        finalResultLabel.setText(conclusionText);
    }

    private void setControlsDisable(boolean d) {
        startBtn.setDisable(d);
        autoBtn.setDisable(d);
        clearBtn.setDisable(d);
        materialCombo.setDisable(d);
        lengthAField.setDisable(d);
        widthBField.setDisable(d);
        thicknessDField.setDisable(d);
    }
}