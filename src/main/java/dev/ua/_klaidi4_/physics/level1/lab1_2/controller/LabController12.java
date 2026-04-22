package dev.ua._klaidi4_.physics.level1.lab1_2.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level1.lab1_2.enums.PendulumType;
import dev.ua._klaidi4_.physics.level1.lab1_2.model.Measurement;
import dev.ua._klaidi4_.physics.level1.lab1_2.view.PendulumCanvas;
import javafx.animation.AnimationTimer;
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

public class LabController12 extends BaseLabController {

    private PendulumCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> typeComboBox;
    private TextField lengthField;
    private TextField oscField;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Slider angleSlider;
    private Slider gravitySlider;
    private Slider frictionSlider;
    private Slider radiusSlider;
    private Label liveStatusLabel;
    private Label liveTimeLabel;
    private Label liveOscLabel;
    private AnimationTimer measurementTimer;
    private long startTime;
    private double targetTime;
    private int targetN;
    private double currentL;
    private PendulumType currentType;
    private Queue<Double> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    public LabController12() {
        initUI();
        applyPhysicsSettings();
    }

    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
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

        Label title = new Label("Система управління (Лаб 1-2)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри вимірювання");
        labPane.setCollapsible(false);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        typeComboBox = new ComboBox<>(FXCollections.observableArrayList("Математичний", "Оборотний"));
        typeComboBox.getSelectionModel().selectFirst();
        typeComboBox.setOnAction(e -> applyPhysicsSettings());

        lengthField = new TextField("0.6");
        oscField = new TextField("10");

        paramsBox.getChildren().addAll(
                createInputGroup("Тип маятника:", typeComboBox),
                createInputGroup("Довжина l (м):", lengthField),
                createInputGroup("Кіл-ть коливань n:", oscField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(200);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        TitledPane physicsPane = new TitledPane();
        physicsPane.setText("Тонкі налаштування фізики");
        VBox physBox = new VBox(5);
        physBox.setPadding(new Insets(5));

        angleSlider = createSlider("Початковий кут (°):", 5, 60, 30, physBox);
        gravitySlider = createSlider("Гравітація g (м/с²):", 1.0, 25.0, 9.81, physBox);
        frictionSlider = createSlider("Опір повітря:", 0.0, 0.5, 0.0, physBox);
        radiusSlider = createSlider("Розмір вантажу:", 10, 40, 18, physBox);

        angleSlider.valueProperty().addListener((o, oldVal, newVal) -> applyPhysicsSettings());
        gravitySlider.valueProperty().addListener((o, oldVal, newVal) -> applyPhysicsSettings());
        frictionSlider.valueProperty().addListener((o, oldVal, newVal) -> applyPhysicsSettings());
        radiusSlider.valueProperty().addListener((o, oldVal, newVal) -> applyPhysicsSettings());
        physicsPane.setContent(physBox);

        startBtn = new Button("▶ ЗАПУСТИТИ ВИМІРЮВАННЯ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ ЛАБИ");
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
            liveTimeLabel.setText("t = 0.00 с");
            liveOscLabel.setText("n = 0");
        });

        leftPanel.getChildren().addAll(title, labPane, physicsPane, startBtn, autoBtn, clearBtn);

        canvas = new PendulumCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(200, 80);
        Label dashTitle = new Label("СЕНСОРНА ПАНЕЛЬ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveTimeLabel = new Label("t = 0.00 с");
        liveTimeLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        liveTimeLabel.setStyle("-fx-text-fill: #00ff00;");
        liveOscLabel = new Label("n = 0");
        liveOscLabel.setStyle("-fx-text-fill: #00ff00;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveTimeLabel, liveOscLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<Measurement, Double> lenCol = new TableColumn<>("l (м)");
        lenCol.setCellValueFactory(new PropertyValueFactory<>("length"));
        TableColumn<Measurement, Integer> oscCol = new TableColumn<>("n");
        oscCol.setCellValueFactory(new PropertyValueFactory<>("oscillations"));
        TableColumn<Measurement, Double> timeCol = new TableColumn<>("t (с)");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        TableColumn<Measurement, Double> periodCol = new TableColumn<>("T (с)");
        periodCol.setCellValueFactory(new PropertyValueFactory<>("period"));
        TableColumn<Measurement, Double> gCol = new TableColumn<>("g (м/с²)");
        gCol.setCellValueFactory(new PropertyValueFactory<>("gravity"));

        table.getColumns().addAll(idCol, typeCol, lenCol, oscCol, timeCol, periodCol, gCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(130);

        VBox statsBox = createStatsBox();

        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);
    }

    private Slider createSlider(String labelText, double min, double max, double val, VBox parent) {
        Label label = new Label(labelText + " " + String.format("%.2f", val));
        Slider slider = new Slider(min, max, val);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.valueProperty().addListener((obs, oldVal, newVal) -> label.setText(labelText + " " + String.format("%.2f", newVal.doubleValue())));
        parent.getChildren().addAll(label, slider);
        return slider;
    }

    private void applyPhysicsSettings() {
        try {
            PendulumType type = typeComboBox.getSelectionModel().getSelectedIndex() == 0 ? PendulumType.MATHEMATICAL : PendulumType.PHYSICAL;
            double l = Double.parseDouble(lengthField.getText());
            double g = gravitySlider.getValue();
            double angle = angleSlider.getValue();
            double friction = frictionSlider.getValue();
            double radius = radiusSlider.getValue();

            canvas.setPhysicsParameters(type, l, g, angle, friction, radius);
        } catch (NumberFormatException ignored) {}
    }

    private void startManual() {
        try {
            double l = Double.parseDouble(lengthField.getText());
            int n = Integer.parseInt(oscField.getText());
            isAutoRunning = false;
            applyPhysicsSettings();
            runMeasurement(l, n);
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа в поля довжини та коливань.");
        }
    }

    private void startAuto() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        autoQueue.add(0.4); autoQueue.add(0.5); autoQueue.add(0.6);
        autoQueue.add(-0.6); autoQueue.add(-0.7);

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

        setControlsDisable(true);
        double nextVal = autoQueue.poll();
        if (nextVal > 0) {
            typeComboBox.getSelectionModel().select(0);
            lengthField.setText(String.valueOf(nextVal));
            applyPhysicsSettings();
            runMeasurement(nextVal, 10);
        } else {
            typeComboBox.getSelectionModel().select(1);
            double physL = Math.abs(nextVal);
            lengthField.setText(String.valueOf(physL));
            applyPhysicsSettings();
            runMeasurement(physL, 10);
        }
    }

    private void runMeasurement(double l, int n) {
        setControlsDisable(true);
        currentL = l;
        targetN = n;
        currentType = typeComboBox.getSelectionModel().getSelectedIndex() == 0 ? PendulumType.MATHEMATICAL : PendulumType.PHYSICAL;

        double customG = gravitySlider.getValue();
        double exactPeriod = 2 * Math.PI * Math.sqrt(l / customG);
        double humanError = (Math.random() - 0.5) * 0.2;
        targetTime = (exactPeriod * n) + humanError;

        liveStatusLabel.setText("СИСТЕМА: ВИМІРЮВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: red;");
        startTime = System.nanoTime();

        measurementTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double elapsed = (now - startTime) / 1_000_000_000.0;

                int currentOsc = (int) (elapsed / exactPeriod);
                if (targetTime - elapsed < 0.25) {
                    currentOsc = targetN;
                }
                if (currentOsc > targetN) currentOsc = targetN;

                liveTimeLabel.setText(String.format("t = %.2f с", elapsed));
                liveOscLabel.setText(String.format("n = %d", currentOsc));

                if (elapsed >= targetTime) {
                    this.stop();
                    finishMeasurement(elapsed);
                }
            }
        };
        measurementTimer.start();
    }

    private void finishMeasurement(double finalTime) {
        liveStatusLabel.setText("СИСТЕМА: ЗАПИС");
        liveStatusLabel.setStyle("-fx-text-fill: yellow;");

        liveOscLabel.setText(String.format("n = %d", targetN));

        double period = finalTime / targetN;
        double calcG = (4 * Math.PI * Math.PI * currentL) / (period * period);
        String typeName = currentType == PendulumType.MATHEMATICAL ? "Математичний" : "Оборотний";

        Measurement m = new Measurement(
                idCounter++, typeName, Math.round(currentL * 100.0) / 100.0,
                targetN, Math.round(finalTime * 100.0) / 100.0,
                Math.round(period * 1000.0) / 1000.0,
                Math.round(calcG * 100.0) / 100.0
        );
        data.add(m);
        updateStats();

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(1500); javafx.application.Platform.runLater(this::processNextAuto); }
                catch (InterruptedException e) {}
            }).start();
        } else {
            setControlsDisable(false);
            liveStatusLabel.setText("СИСТЕМА: ОЧІКУВАННЯ");
            liveStatusLabel.setStyle("-fx-text-fill: yellow;");
        }
    }

    private void updateStats() {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: -");
            return;
        }

        List<Measurement> mathData = new ArrayList<>();
        List<Measurement> physData = new ArrayList<>();

        for (Measurement m : data) {
            if (m.getType().equals("Математичний")) {
                mathData.add(m);
            } else {
                physData.add(m);
            }
        }

        StringBuilder resultText = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ:\n");

        if (!mathData.isEmpty()) {
            double sumT = 0, sumG = 0;
            for (Measurement m : mathData) {
                sumT += m.getPeriod();
                sumG += m.getGravity();
            }
            double avgT = sumT / mathData.size();
            double avgG = sumG / mathData.size();

            double sumDelta = 0;
            for (Measurement m : mathData) {
                sumDelta += Math.abs(m.getGravity() - avgG);
            }
            double deltaG = sumDelta / mathData.size();
            double eps = (avgG != 0) ? (deltaG / avgG) * 100 : 0;

            resultText.append(String.format("1. Середній період коливань математичного маятника: T = %.3f с.\n", avgT));
            resultText.append(String.format("2. Прискорення вільного падіння (математичний): g = %.2f м/с².\n", avgG));
            resultText.append(String.format("3. Похибки (математичний): Δg = %.2f м/с², ε = %.1f %%.\n", deltaG, eps));
        }

        if (!physData.isEmpty()) {
            double sumG = 0;
            for (Measurement m : physData) {
                sumG += m.getGravity();
            }
            double avgG = sumG / physData.size();

            double sumDelta = 0;
            for (Measurement m : physData) {
                sumDelta += Math.abs(m.getGravity() - avgG);
            }
            double deltaG = sumDelta / physData.size();
            double eps = (avgG != 0) ? (deltaG / avgG) * 100 : 0;

            resultText.append(String.format("4. Прискорення вільного падіння (оборотний): g = %.2f м/с².\n", avgG));
            resultText.append(String.format("5. Похибки (оборотний): Δg = %.2f м/с², ε = %.1f %%.\n", deltaG, eps));
        }

        finalResultLabel.setText(resultText.toString());
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        typeComboBox.setDisable(disable);
        lengthField.setDisable(disable);
        oscField.setDisable(disable);
        angleSlider.setDisable(disable);
        gravitySlider.setDisable(disable);
        frictionSlider.setDisable(disable);
        radiusSlider.setDisable(disable);
    }
}