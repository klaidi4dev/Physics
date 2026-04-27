package dev.ua._klaidi4_.physics.level4.lab4_1.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level4.lab4_1.model.Measurement;
import dev.ua._klaidi4_.physics.level4.lab4_1.view.PhysicalPendulumCanvas;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class LabController41 extends BaseLabController {

    private PhysicalPendulumCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> axisBox;
    private TextField xDField;
    private TextField nField;
    private TextField angleField;
    private TextField mRodField;
    private TextField mDField;
    private Button startBtn;
    private Button autoTaskBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveOscLabel;
    private Label liveTimeLabel;
    private Queue<AutoTestParam> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;
    private final double L_ROD = 1.0;
    private final double X_N = 0.1;
    private final double X_N_PRIME = 0.8;
    private final double M_B = 1.5;
    private final double X_B = 0.6;
    private final double G_TRUE = 9.81;

    private static class AutoTestParam {
        String axis;
        double xD;
        AutoTestParam(String axis, double xD) {
            this.axis = axis; this.xD = xD;
        }
    }

    public LabController41() {
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
        leftPanel.setPrefWidth(320);
        leftPanel.setMinWidth(320);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Управління установкою (4-1)");
        title.setFont(Font.font("System", FontWeight.BOLD, 17));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри маятника");
        labPane.setCollapsible(false);

        VBox paramsBox = new VBox(10);
        paramsBox.setPadding(new Insets(5));

        axisBox = new ComboBox<>();
        axisBox.getItems().addAll("Призма N (Пряме)", "Призма N' (Перевернуте)");
        axisBox.setValue("Призма N (Пряме)");
        axisBox.setMaxWidth(Double.MAX_VALUE);
        axisBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-padding: 2;");

        xDField = new TextField("30.0");
        nField = new TextField("10");

        angleField = new TextField("8.0");
        mRodField = new TextField("1.0");
        mDField = new TextField("1.5");

        paramsBox.getChildren().addAll(
                new VBox(4, createLabel("Вісь підвішування:"), axisBox),
                createInputGroup("Положення вантажу D, x_D (см):", xDField),
                createInputGroup("Кількість коливань n:", nField),
                new Separator(),
                createInputGroup("Початковий кут відхилення (°):", angleField),
                createInputGroup("Маса стрижня (кг):", mRodField),
                createInputGroup("Маса вантажу D (кг):", mDField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(280);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        startBtn = new Button("▶ ЗАПУСТИТИ КОЛИВАННЯ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoTaskBtn = new Button("⚙ СЕРІЯ ВИМІРІВ (Шукати перетин)");
        autoTaskBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold;");
        autoTaskBtn.setMaxWidth(Double.MAX_VALUE);
        autoTaskBtn.setOnAction(e -> startAutoTask());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> clearData());

        leftPanel.getChildren().addAll(title, labPane, startBtn, autoTaskBtn, clearBtn);

        canvas = new PhysicalPendulumCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(200, 80);

        Label dashTitle = new Label("СЕКУНДОМІР");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ГОТОВО");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveOscLabel = new Label("n = 0");
        liveOscLabel.setStyle("-fx-text-fill: cyan; -fx-font-size: 13px; -fx-font-weight: bold;");
        liveTimeLabel = new Label("0.000 с");
        liveTimeLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        liveTimeLabel.setStyle("-fx-text-fill: #00ff00;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveOscLabel, liveTimeLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, String> axisCol = new TableColumn<>("Вісь");
        axisCol.setCellValueFactory(new PropertyValueFactory<>("axis"));
        TableColumn<Measurement, Double> xdCol = new TableColumn<>("x_D (см)");
        xdCol.setCellValueFactory(new PropertyValueFactory<>("xD"));
        TableColumn<Measurement, Integer> nCol = new TableColumn<>("n");
        nCol.setCellValueFactory(new PropertyValueFactory<>("n"));
        TableColumn<Measurement, Double> timeCol = new TableColumn<>("t (с)");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        TableColumn<Measurement, Double> perCol = new TableColumn<>("T (с)");
        perCol.setCellValueFactory(new PropertyValueFactory<>("period"));

        table.getColumns().addAll(idCol, axisCol, xdCol, nCol, timeCol, perCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        axisBox.valueProperty().addListener((obs, oldVal, newVal) -> updateCanvasPreview());
        xDField.textProperty().addListener((obs, oldVal, newVal) -> updateCanvasPreview());
        angleField.textProperty().addListener((obs, oldVal, newVal) -> updateCanvasPreview());
        mRodField.textProperty().addListener((obs, oldVal, newVal) -> updateCanvasPreview());
        mDField.textProperty().addListener((obs, oldVal, newVal) -> updateCanvasPreview());

        Platform.runLater(this::updateCanvasPreview);
    }

    private void updateCanvasPreview() {
        if (isAutoRunning || startBtn.isDisabled()) return;

        try {
            String axisStr = axisBox.getValue().contains("N'") ? "N'" : "N";
            double xD_cm = Double.parseDouble(xDField.getText());
            double angleDeg = Double.parseDouble(angleField.getText());
            double mRod = Double.parseDouble(mRodField.getText());
            double mD = Double.parseDouble(mDField.getText());

            if (mRod < 0.1) mRod = 0.1;
            if (mD < 0.1) mD = 0.1;

            canvas.updatePreview(axisStr, xD_cm, angleDeg, mRod, mD);
        } catch (Exception ignored) {
        }
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        label.setStyle("-fx-text-fill: #475569;");
        return label;
    }

    private void clearData() {
        data.clear();
        idCounter = 1;
        updateStats();
        liveTimeLabel.setText("0.000 с");
        liveOscLabel.setText("n = 0");
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoTaskBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        axisBox.setDisable(disable);
        xDField.setDisable(disable);
        nField.setDisable(disable);
        angleField.setDisable(disable);
        mRodField.setDisable(disable);
        mDField.setDisable(disable);
    }

    private void startManual() {
        try {
            Double.parseDouble(xDField.getText());
            Integer.parseInt(nField.getText());
            Double.parseDouble(angleField.getText());
            Double.parseDouble(mRodField.getText());
            Double.parseDouble(mDField.getText());
            isAutoRunning = false;
            runMeasurement();
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числові значення.");
        }
    }

    private void startAutoTask() {
        clearData();
        autoQueue.clear();
        isAutoRunning = true;

        for (double x = 20; x <= 70; x += 10) {
            autoQueue.add(new AutoTestParam("N", x));
        }
        for (double x = 20; x <= 70; x += 10) {
            autoQueue.add(new AutoTestParam("N'", x));
        }
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
        AutoTestParam param = autoQueue.poll();
        axisBox.setValue(param.axis.equals("N") ? "Призма N (Пряме)" : "Призма N' (Перевернуте)");
        xDField.setText(String.format(java.util.Locale.US, "%.1f", param.xD));
        nField.setText("10");

        mRodField.setText("1.0");
        mDField.setText("1.5");

        updateCanvasPreview();
        runMeasurement();
    }

    private void runMeasurement() {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: КОЛИВАННЯ...");
        liveStatusLabel.setStyle("-fx-text-fill: cyan;");

        String axisStr = axisBox.getValue().contains("N'") ? "N'" : "N";
        double xD_cm = Double.parseDouble(xDField.getText());
        int n = Integer.parseInt(nField.getText());

        double initAngle = Double.parseDouble(angleField.getText());
        double userMRod = Double.parseDouble(mRodField.getText());
        double userMD = Double.parseDouble(mDField.getText());

        double xD_m = xD_cm / 100.0;
        double totalMass = userMRod + M_B + userMD;

        double xCm = (userMRod * (L_ROD / 2.0) + M_B * X_B + userMD * xD_m) / totalMass;

        double xPivot = axisStr.equals("N") ? X_N : X_N_PRIME;
        double a = Math.abs(xCm - xPivot);

        double jRod = (1.0 / 12.0) * userMRod * Math.pow(L_ROD, 2) + userMRod * Math.pow((L_ROD / 2.0) - xPivot, 2);
        double jB = M_B * Math.pow(X_B - xPivot, 2);
        double jD = userMD * Math.pow(xD_m - xPivot, 2);
        double jTotal = jRod + jB + jD;

        double exactPeriod = 2 * Math.PI * Math.sqrt(jTotal / (totalMass * G_TRUE * a));

        double noise = exactPeriod * (Math.random() - 0.5) * 0.01;
        final double measuredPeriod = exactPeriod + noise;

        canvas.setTimerUpdateCallback((time, currentN) -> Platform.runLater(() -> {
            liveTimeLabel.setText(String.format("%.3f с", time));
            liveOscLabel.setText("n = " + currentN);
        }));

        canvas.setOnReadyCallback(() -> Platform.runLater(() -> finalizeMeasurement(axisStr, xD_cm, n, measuredPeriod)));

        canvas.startSimulation(axisStr, xD_cm, n, measuredPeriod, initAngle, userMRod, userMD);
    }

    private void finalizeMeasurement(String axis, double xD, int n, double period) {
        liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

        double totalTime = period * n;
        Measurement meas = new Measurement(
                idCounter++, axis, xD, n,
                Math.round(totalTime * 1000.0) / 1000.0,
                Math.round(period * 10000.0) / 10000.0
        );
        data.add(meas);
        updateStats();

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(800); Platform.runLater(this::processNextAuto); }
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

        List<Measurement> listN = new ArrayList<>();
        List<Measurement> listNPrime = new ArrayList<>();
        for (Measurement m : data) {
            if (m.getAxis().equals("N")) listN.add(m);
            else listNPrime.add(m);
        }

        double bestDiff = Double.MAX_VALUE;
        double bestT = 0;
        boolean found = false;

        for (Measurement mN : listN) {
            for (Measurement mP : listNPrime) {
                if (Math.abs(mN.getXD() - mP.getXD()) < 0.1) {
                    double diff = Math.abs(mN.getPeriod() - mP.getPeriod());
                    if (diff < bestDiff && diff < 0.05) {
                        bestDiff = diff;
                        bestT = (mN.getPeriod() + mP.getPeriod()) / 2.0;
                        found = true;
                    }
                }
            }
        }

        StringBuilder analysis = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ:\n");
        analysis.append("1. Побудовано графіки залежності T=f(a) для прямого та перевернутого маятника.\n");

        if (found) {
            double L = Math.abs(X_N_PRIME - X_N);
            double gCalc = (4 * Math.PI * Math.PI * L) / (bestT * bestT);

            analysis.append(String.format("2. Знайдено точку збігу періодів: T_збіг ≈ %.3f с.\n", bestT));
            analysis.append(String.format("3. Зведена довжина фізичного маятника L = %.2f м.\n", L));
            analysis.append(String.format("4. Розрахункове прискорення вільного падіння: g = 4π²L / T² = %.2f м/с².\n", gCalc));
            analysis.append("ВИСНОВОК: Метод оборотного маятника дозволяє з високою точністю виміряти g без необхідності обчислення складного моменту інерції тіла.");
        } else {
            analysis.append("2. Точку збігу періодів (перетин графіків) поки не знайдено. Продовжуйте змінювати положення x_D.\n");
            analysis.append("Підказка: Виконайте серію вимірів в авторежимі.");
        }

        finalResultLabel.setText(analysis.toString());
    }
}