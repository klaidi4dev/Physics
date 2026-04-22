package dev.ua._klaidi4_.physics.level1.lab1_4.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level1.lab1_4.model.Measurement;
import dev.ua._klaidi4_.physics.level1.lab1_4.view.BallisticCanvas;
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

public class LabController14 extends BaseLabController {

    private BallisticCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField mPendField;
    private TextField mBulletField;
    private TextField lengthField;
    private Slider speedSlider;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label dxLabel;
    private Label vCalcLabel;
    private Queue<Double> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    private double currentPendulumMass;
    private double currentDx;
    private double currentVCalc;

    public LabController14() {
        initUI();
        resetPendulumMass();
    }

    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        isAutoRunning = false;
        autoQueue.clear();
    }

    private void resetPendulumMass() {
        try {
            currentPendulumMass = Double.parseDouble(mPendField.getText());
        } catch (Exception e) {
            currentPendulumMass = 1.5;
        }
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 1-4)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри установки");
        labPane.setCollapsible(true);
        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        mPendField = new TextField("1.5");
        mBulletField = new TextField("0.01");
        lengthField = new TextField("1.0");

        mPendField.textProperty().addListener((o, ov, nv) -> resetPendulumMass());

        paramsBox.getChildren().addAll(
                createInputGroup("Початкова маса M (кг):", mPendField),
                createInputGroup("Маса кулі m (кг):", mBulletField),
                createInputGroup("Довжина підвісу L (м):", lengthField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(200);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        TitledPane physicsPane = new TitledPane();
        physicsPane.setText("Налаштування пострілу");
        VBox physBox = new VBox(5);
        physBox.setPadding(new Insets(5));

        Label speedLabel = new Label("Швидкість пострілу: 150 м/с");
        speedSlider = new Slider(50, 400, 150);
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);
        speedSlider.valueProperty().addListener((o, ov, nv) ->
                speedLabel.setText(String.format("Швидкість пострілу: %.0f м/с", nv.doubleValue()))
        );
        physBox.getChildren().addAll(speedLabel, speedSlider);
        physicsPane.setContent(physBox);

        startBtn = new Button("▶ ЗДІЙСНИТИ ПОСТРІЛ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ (5 пострілів)");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAuto());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ (Скидає масу)");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear();
            idCounter = 1;
            resetPendulumMass();
            updateStats();
            dxLabel.setText("Δx = 0.000 м");
            vCalcLabel.setText("V = 0.00 м/с");
            mPendField.setDisable(false);
            lengthField.setDisable(false);
            mBulletField.setDisable(false);
        });

        leftPanel.getChildren().addAll(title, labPane, physicsPane, startBtn, autoBtn, clearBtn);

        canvas = new BallisticCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(220, 80);
        Label dashTitle = new Label("ПОКАЗНИКИ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        dxLabel = new Label("Δx = 0.000 м");
        dxLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        dxLabel.setStyle("-fx-text-fill: #3498db;");
        vCalcLabel = new Label("V = 0.00 м/с");
        vCalcLabel.setStyle("-fx-text-fill: #00ff00;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, dxLabel, vCalcLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, Double> mCol = new TableColumn<>("m кулі (кг)");
        mCol.setCellValueFactory(new PropertyValueFactory<>("m"));
        TableColumn<Measurement, Double> pCol = new TableColumn<>("M маят. (кг)");
        pCol.setCellValueFactory(new PropertyValueFactory<>("mPend"));
        TableColumn<Measurement, Double> lCol = new TableColumn<>("L (м)");
        lCol.setCellValueFactory(new PropertyValueFactory<>("length"));
        TableColumn<Measurement, Double> dxCol = new TableColumn<>("Δx (м)");
        dxCol.setCellValueFactory(new PropertyValueFactory<>("dx"));
        TableColumn<Measurement, Double> vCol = new TableColumn<>("V (м/с)");
        vCol.setCellValueFactory(new PropertyValueFactory<>("v"));

        table.getColumns().addAll(idCol, mCol, pCol, lCol, dxCol, vCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(140);

        VBox statsBox = createStatsBox();

        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        canvas.setCallbacks(
                () -> Platform.runLater(() -> {
                    liveStatusLabel.setText("Статус: ЗІТКНЕННЯ");
                    liveStatusLabel.setStyle("-fx-text-fill: red;");
                }),
                () -> Platform.runLater(this::finishMeasurement)
        );
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        mPendField.setDisable(true);
        mBulletField.setDisable(true);
        lengthField.setDisable(true);
        speedSlider.setDisable(disable);
    }

    private void startManual() {
        try {
            Double.parseDouble(mBulletField.getText());
            Double.parseDouble(lengthField.getText());
            isAutoRunning = false;
            runSimulation(speedSlider.getValue());
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа в поля.");
        }
    }

    private void startAuto() {
        data.clear();
        idCounter = 1;
        resetPendulumMass();
        updateStats();
        autoQueue.clear();

        for (int i = 0; i < 5; i++) {
            autoQueue.add(150.0 + Math.random() * 10);
        }

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

    private void runSimulation(double simVelocity) {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: ПОЛІТ КУЛІ");
        liveStatusLabel.setStyle("-fx-text-fill: cyan;");

        double m = Double.parseDouble(mBulletField.getText());
        double L = Double.parseDouble(lengthField.getText());

        double u = (m * simVelocity) / (currentPendulumMass + m);
        double h = (u * u) / (2 * 9.81);
        double maxAngle = Math.acos(1.0 - (h / L));
        double exactDx = L * Math.sin(maxAngle);

        currentDx = exactDx + (Math.random() - 0.5) * 0.004;
        if (currentDx <= 0) currentDx = 0.001;

        double calcH = L - Math.sqrt(L * L - currentDx * currentDx);
        currentVCalc = ((currentPendulumMass + m) / m) * Math.sqrt(2 * 9.81 * calcH);

        canvas.startSimulation(maxAngle);
    }

    private void finishMeasurement() {
        double m = Double.parseDouble(mBulletField.getText());
        double L = Double.parseDouble(lengthField.getText());

        dxLabel.setText(String.format("Δx = %.3f м", currentDx));
        vCalcLabel.setText(String.format("V = %.2f м/с", currentVCalc));
        liveStatusLabel.setText("Статус: ВИМІРЯНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

        Measurement meas = new Measurement(
                idCounter++, m,
                Math.round(currentPendulumMass * 1000.0) / 1000.0,
                L,
                Math.round(currentDx * 1000.0) / 1000.0,
                Math.round(currentVCalc * 100.0) / 100.0
        );
        data.add(meas);

        currentPendulumMass += m;
        updateStats();

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(1500); Platform.runLater(this::processNextAuto); }
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

        double sumV = 0;
        for (Measurement m : data) {
            sumV += m.getV();
        }

        double vAvg = sumV / data.size();
        double sumDelta = 0;
        for (Measurement m : data) {
            sumDelta += Math.abs(m.getV() - vAvg);
        }
        double deltaV = sumDelta / data.size();
        double eps = (deltaV / vAvg) * 100;

        String conclusion = String.format(
                "ОБРОБКА РЕЗУЛЬТАТІВ ТА АНАЛІЗ:\n" +
                        "1. Швидкість кулі для кожного пострілу розрахована та внесена до таблиці. " +
                        "(Під час розрахунків враховано, що маса маятника після кожного пострілу збільшувалася на величину маси кулі m = %.3f кг).\n" +
                        "2. Середнє значення швидкості кулі: v_ср = %.2f м/с.\n" +
                        "   Абсолютна похибка: Δv = %.2f м/с. Відносна похибка: ε = %.1f %%.\n\n" +
                        "ВІДПОВІДЬ: v = (%.2f ± %.2f) м/с.",
                Double.parseDouble(mBulletField.getText()), vAvg, deltaV, eps, vAvg, deltaV
        );

        finalResultLabel.setText(conclusion);
    }
}