package dev.ua._klaidi4_.physics.level1.lab1_9.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level1.lab1_9.model.Measurement;
import dev.ua._klaidi4_.physics.level1.lab1_9.view.TorsionBallisticCanvas;
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

public class LabController19 extends BaseLabController {

    private TorsionBallisticCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField mBulletField;
    private TextField rTargetField;
    private TextField mWeightsField;
    private TextField r1Field;
    private TextField r2Field;
    private TextField oscField;
    private TextField vField;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label vCalcLabel;
    private Label phiLabel;
    private Label time1Label;
    private Label time2Label;
    private Queue<AutoTestParam> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;
    private int targetOscillations = 10;

    private static class AutoTestParam {
        double t1, t2, phi;
        AutoTestParam(double t1, double t2, double phi) {
            this.t1 = t1;
            this.t2 = t2;
            this.phi = phi;
        }
    }

    public LabController19() {
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

        Label title = new Label("Система управління (Лаб 1-9)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри установки");
        labPane.setCollapsible(true);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        mBulletField = new TextField("0.0005");
        rTargetField = new TextField("0.15");
        mWeightsField = new TextField("0.200");
        r1Field = new TextField("0.09");
        r2Field = new TextField("0.02");
        oscField = new TextField("10");

        paramsBox.getChildren().addAll(
                createInputGroup("Маса снаряда m (кг):", mBulletField),
                createInputGroup("Відстань до мішені r (м):", rTargetField),
                createInputGroup("Маса додаткових тягарців M (кг):", mWeightsField),
                createInputGroup("Максимальна відстань R1 (м):", r1Field),
                createInputGroup("Мінімальна відстань R2 (м):", r2Field),
                createInputGroup("Кількість коливань N:", oscField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(240);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        TitledPane physicsPane = new TitledPane();
        physicsPane.setText("Налаштування пострілу");
        VBox physBox = new VBox(5);
        physBox.setPadding(new Insets(5));

        vField = new TextField("100.0");
        physBox.getChildren().add(createInputGroup("Швидкість кулі v (м/с):", vField));
        physicsPane.setContent(physBox);

        startBtn = new Button("▶ ЗДІЙСНИТИ ДОСЛІД");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ (5 дослідів)");
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
            phiLabel.setText("Кут φ = 0.0°");
            vCalcLabel.setText("V = 0.00 м/с");
            time1Label.setText("t1 = 0.00 с");
            time2Label.setText("t2 = 0.00 с");
        });

        leftPanel.getChildren().addAll(title, labPane, physicsPane, startBtn, autoBtn, clearBtn);

        canvas = new TorsionBallisticCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(220, 110);
        Label dashTitle = new Label("ДАТЧИКИ ТА ЧАС");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        phiLabel = new Label("Кут φ = 0.0°");
        phiLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        phiLabel.setStyle("-fx-text-fill: #3498db;");
        vCalcLabel = new Label("V = 0.00 м/с");
        vCalcLabel.setStyle("-fx-text-fill: #00ff00;");
        time1Label = new Label("t1 = 0.00 с");
        time1Label.setStyle("-fx-text-fill: #e67e22;");
        time2Label = new Label("t2 = 0.00 с");
        time2Label.setStyle("-fx-text-fill: #e67e22;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, phiLabel, vCalcLabel, time1Label, time2Label);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, Double> t1Col = new TableColumn<>("t1 (с)");
        t1Col.setCellValueFactory(new PropertyValueFactory<>("t1"));
        TableColumn<Measurement, Double> p1Col = new TableColumn<>("T1 (с)");
        p1Col.setCellValueFactory(new PropertyValueFactory<>("period1"));
        TableColumn<Measurement, Double> t2Col = new TableColumn<>("t2 (с)");
        t2Col.setCellValueFactory(new PropertyValueFactory<>("t2"));
        TableColumn<Measurement, Double> p2Col = new TableColumn<>("T2 (с)");
        p2Col.setCellValueFactory(new PropertyValueFactory<>("period2"));
        TableColumn<Measurement, Double> angCol = new TableColumn<>("Кут φ (°)");
        angCol.setCellValueFactory(new PropertyValueFactory<>("phi"));
        TableColumn<Measurement, Double> vCol = new TableColumn<>("V (м/с)");
        vCol.setCellValueFactory(new PropertyValueFactory<>("v"));

        table.getColumns().addAll(idCol, t1Col, p1Col, t2Col, p2Col, angCol, vCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(140);

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
        mBulletField.setDisable(disable);
        rTargetField.setDisable(disable);
        mWeightsField.setDisable(disable);
        r1Field.setDisable(disable);
        r2Field.setDisable(disable);
        oscField.setDisable(disable);
        vField.setDisable(disable);
    }

    private void startManual() {
        try {
            Double.parseDouble(mBulletField.getText());
            Double.parseDouble(vField.getText());
            targetOscillations = Integer.parseInt(oscField.getText());
            isAutoRunning = false;
            runSimulation(null);
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа в поля.");
        }
    }

    private void startAuto() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        autoQueue.add(new AutoTestParam(27.965, 16.795, 66.5));
        autoQueue.add(new AutoTestParam(27.977, 16.694, 66.2));
        autoQueue.add(new AutoTestParam(27.980, 16.759, 66.7));
        autoQueue.add(new AutoTestParam(27.986, 16.691, 66.4));
        autoQueue.add(new AutoTestParam(27.963, 16.749, 66.6));

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
        runSimulation(autoQueue.poll());
    }

    private void runSimulation(AutoTestParam autoParam) {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: ПОСТРІЛ");
        liveStatusLabel.setStyle("-fx-text-fill: red;");

        double M = Double.parseDouble(mWeightsField.getText());
        double R1 = Double.parseDouble(r1Field.getText());
        double R2 = Double.parseDouble(r2Field.getText());
        double m = Double.parseDouble(mBulletField.getText());
        double r = Double.parseDouble(rTargetField.getText());
        targetOscillations = Integer.parseInt(oscField.getText());

        double finalT1, finalT2, finalPhiDeg;

        if (autoParam != null) {
            finalT1 = autoParam.t1;
            finalT2 = autoParam.t2;
            finalPhiDeg = autoParam.phi;
        } else {
            double V = Double.parseDouble(vField.getText());
            double k = 0.0242;
            double I0 = 0.00155;
            double I1 = I0 + 2 * M * (R1 * R1 - R2 * R2);
            double T1 = 2 * Math.PI * Math.sqrt(I1 / k);
            double T2 = 2 * Math.PI * Math.sqrt(I0 / k);
            double alphaRad = (V * m * r) / Math.sqrt(k * I0);

            finalT1 = (T1 * targetOscillations) + (Math.random() - 0.5) * 0.02;
            finalT2 = (T2 * targetOscillations) + (Math.random() - 0.5) * 0.02;
            finalPhiDeg = Math.toDegrees(alphaRad);
        }

        canvas.setCallbacks(() -> {
            liveStatusLabel.setText("Статус: ЗІТКНЕННЯ");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

            new Thread(() -> {
                try { Thread.sleep(800); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> finishMeasurement(finalT1, finalT2, finalPhiDeg));
            }).start();
        });

        canvas.startSimulation(finalPhiDeg);
    }

    private void finishMeasurement(double t1, double t2, double phiDeg) {
        canvas.stopAnimation();

        double M = Double.parseDouble(mWeightsField.getText());
        double R1 = Double.parseDouble(r1Field.getText());
        double R2 = Double.parseDouble(r2Field.getText());
        double m = Double.parseDouble(mBulletField.getText());
        double r = Double.parseDouble(rTargetField.getText());
        double T1 = t1 / targetOscillations;
        double T2 = t2 / targetOscillations;
        double phiRad = Math.toRadians(phiDeg);
        double calcV = (4 * Math.PI * phiRad * M * (R1 * R1 - R2 * R2) * T2) / (m * r * (T1 * T1 - T2 * T2));

        phiLabel.setText(String.format("Кут φ = %.1f°", phiDeg));
        vCalcLabel.setText(String.format("V = %.2f м/с", calcV));
        time1Label.setText(String.format("t1 = %.3f с", t1));
        time2Label.setText(String.format("t2 = %.3f с", t2));
        liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");

        Measurement meas = new Measurement(
                idCounter++,
                Math.round(t1 * 1000.0) / 1000.0,
                Math.round(T1 * 10000.0) / 10000.0,
                Math.round(t2 * 1000.0) / 1000.0,
                Math.round(T2 * 10000.0) / 10000.0,
                Math.round(phiDeg * 10.0) / 10.0,
                Math.round(calcV * 100.0) / 100.0
        );
        data.add(meas);
        updateStats();

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(1000); Platform.runLater(this::processNextAuto); }
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
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }
        double sumT1 = 0, sumT2 = 0, sumV = 0;
        for (Measurement m : data) {
            sumT1 += m.getPeriod1();
            sumT2 += m.getPeriod2();
            sumV += m.getV();
        }

        double avgT1 = sumT1 / data.size();
        double avgT2 = sumT2 / data.size();
        double avgV = sumV / data.size();

        double sumDeltaT1 = 0, sumDeltaT2 = 0, sumDeltaV = 0;
        for (Measurement m : data) {
            sumDeltaT1 += Math.abs(m.getPeriod1() - avgT1);
            sumDeltaT2 += Math.abs(m.getPeriod2() - avgT2);
            sumDeltaV += Math.abs(m.getV() - avgV);
        }

        double deltaT1 = sumDeltaT1 / data.size();
        double deltaT2 = sumDeltaT2 / data.size();
        double deltaV = sumDeltaV / data.size();

        if (data.size() == 1) {
            deltaT1 = 0.01; deltaT2 = 0.01; deltaV = avgV * 0.02;
        }

        double epsV = (deltaV / avgV) * 100;

        String conclusion = String.format(
                "ОБРОБКА РЕЗУЛЬТАТІВ ВИМІРЮВАНЬ:\n" +
                        "1. За формулою T = t / N вирахувано періоди T1 та T2 для кожного досліду.\n" +
                        "2. Середні значення: T1_ср = %.4f с, T2_ср = %.4f с.\n" +
                        "   Абсолютні похибки: ΔT1 = %.4f с, ΔT2 = %.4f с.\n" +
                        "3. За робочою формулою (21) з методички розрахована середня швидкість снаряда: v_ср = %.2f м/с.\n" +
                        "4. Абсолютна похибка швидкості: Δv = %.2f м/с. Відносна похибка: ε = %.1f %%.\n\n" +
                        "ВІДПОВІДЬ: v = (%.2f ± %.2f) м/с.",
                avgT1, avgT2, deltaT1, deltaT2, avgV, deltaV, epsV, avgV, deltaV
        );

        finalResultLabel.setText(conclusion);
    }
}