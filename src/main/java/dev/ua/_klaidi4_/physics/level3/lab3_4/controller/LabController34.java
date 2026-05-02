package dev.ua._klaidi4_.physics.level3.lab3_4.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level3.lab3_4.model.Measurement;
import dev.ua._klaidi4_.physics.level3.lab3_4.view.SolenoidCanvas;
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

public class LabController34 extends BaseLabController {

    private SolenoidCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField currentField;
    private TextField turnsField;
    private TextField lengthField;
    private TextField radiusField;
    private TextField xField;
    private Button measureBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveTimeLabel;
    private Label cPrimeLabel;
    private AnimationTimer measurementTimer;
    private long startTime;
    private final double targetTime = 2.5;

    private Queue<Double> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;
    private double cPrime = 0.0;

    public LabController34() {
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

        Label title = new Label("Система управління (Лаб 3-4)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane configPane = new TitledPane();
        configPane.setText("Параметри соленоїда");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(12);
        configBox.setPadding(new Insets(5));

        currentField = new TextField("1.5");
        turnsField = new TextField("5130");
        lengthField = new TextField("0.57");
        radiusField = new TextField("0.026");

        configBox.getChildren().addAll(
                createInputGroup("Струм соленоїда I (А):", currentField),
                createInputGroup("Кількість витків N:", turnsField),
                createInputGroup("Довжина соленоїда L (м):", lengthField),
                createInputGroup("Радіус соленоїда R (м):", radiusField)
        );

        ScrollPane scrollParams = new ScrollPane(configBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(200);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        configPane.setContent(scrollParams);

        xField = new TextField("0.0");
        xField.textProperty().addListener((obs, oldVal, newVal) -> applyPhysicsSettings());
        lengthField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        radiusField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());

        measureBtn = new Button("⚡ ВИМІРЯТИ H(x)");
        measureBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        measureBtn.setMaxWidth(Double.MAX_VALUE);
        measureBtn.setOnAction(e -> startManual());

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
            cPrime = 0.0;
            updateDash();
            updateStats();
            liveTimeLabel.setText("t = 0.00 с");
        });

        leftPanel.getChildren().addAll(
                title,
                configPane,
                createInputGroup("Координата котушки x (см):", xField),
                measureBtn,
                autoBtn,
                clearBtn
        );

        canvas = new SolenoidCanvas(600, 440);

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
        Label dashTitle = new Label("СЕНСОРНА ПАНЕЛЬ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveTimeLabel = new Label("t = 0.00 с");
        liveTimeLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        liveTimeLabel.setStyle("-fx-text-fill: #00ff00;");
        cPrimeLabel = new Label("C' = АВТО");
        cPrimeLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveTimeLabel, cPrimeLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, Double> xCol = new TableColumn<>("x (см)");
        xCol.setCellValueFactory(new PropertyValueFactory<>("x"));
        TableColumn<Measurement, Double> aCol = new TableColumn<>("α (под.)");
        aCol.setCellValueFactory(new PropertyValueFactory<>("alpha"));
        TableColumn<Measurement, Double> heCol = new TableColumn<>("H_експ (А/м)");
        heCol.setCellValueFactory(new PropertyValueFactory<>("expH"));
        TableColumn<Measurement, Double> htCol = new TableColumn<>("H_теор (А/м)");
        htCol.setCellValueFactory(new PropertyValueFactory<>("theoH"));
        TableColumn<Measurement, Double> errCol = new TableColumn<>("ε (%)");
        errCol.setCellValueFactory(new PropertyValueFactory<>("errorPercent"));

        table.getColumns().addAll(idCol, xCol, aCol, heCol, htCol, errCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(130);

        VBox statsBox = createStatsBox();

        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);
    }

    private void applyPhysicsSettings() {
        try {
            double l = Double.parseDouble(lengthField.getText());
            double r = Double.parseDouble(radiusField.getText());
            double x = Double.parseDouble(xField.getText().replace(',', '.')) / 100.0;
            canvas.setSetupParameters(l, r, x);
        } catch (NumberFormatException ignored) {}
    }

    private void setControlsDisable(boolean disable) {
        measureBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        xField.setDisable(disable);
        currentField.setDisable(disable);
        turnsField.setDisable(disable);
        lengthField.setDisable(disable);
        radiusField.setDisable(disable);
    }

    private void updateDash() {
        if (cPrime > 0) {
            cPrimeLabel.setText(String.format(Locale.US, "C' = %.2f (А/м)/под", cPrime));
        } else {
            cPrimeLabel.setText("C' = АВТО");
        }
    }

    private void startManual() {
        try {
            double xCm = Double.parseDouble(xField.getText().replace(',', '.'));
            isAutoRunning = false;
            runMeasurement(xCm);
        } catch (Exception e) {
            showAlert("Помилка", "Перевірте правильність введених даних.");
        }
    }

    private void startAuto() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        double[] autoPoints = {0, 1, 2, 3, 4, 5, 8, 11, 14, 17, 20, 23, 25};
        for (double px : autoPoints) {
            autoQueue.add(px);
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

        double nextVal = autoQueue.poll();
        xField.setText(String.format(Locale.US, "%.1f", nextVal));
        runMeasurement(nextVal);
    }

    private void runMeasurement(double xCm) {
        setControlsDisable(true);

        double I = Double.parseDouble(currentField.getText().replace(',', '.'));
        double N = Double.parseDouble(turnsField.getText().replace(',', '.'));
        double l = Double.parseDouble(lengthField.getText().replace(',', '.'));
        double R = Double.parseDouble(radiusField.getText().replace(',', '.'));

        double hTheoCenter = (I * N / (2.0 * l)) * (2.0 * (l / 2.0) / Math.sqrt((l / 2.0) * (l / 2.0) + R * R));
        cPrime = hTheoCenter / 14.14;
        updateDash();

        double xMeters = xCm / 100.0;
        double d1 = l / 2.0 + xMeters;
        double d2 = l / 2.0 - xMeters;
        double cos1 = d1 / Math.sqrt(d1 * d1 + R * R);
        double cos2 = d2 / Math.sqrt(d2 * d2 + R * R);

        double hTheo = (I * N / (2.0 * l)) * (cos1 + cos2);

        double finalAlpha = hTheo / cPrime + (Math.random() - 0.5) * 0.2;

        liveStatusLabel.setText("СИСТЕМА: ВИМІРЮВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: red;");
        canvas.triggerMeasurement(finalAlpha);

        startTime = System.nanoTime();

        measurementTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double elapsed = (now - startTime) / 1_000_000_000.0;
                liveTimeLabel.setText(String.format(Locale.US, "t = %.2f с", elapsed));

                if (elapsed >= targetTime) {
                    this.stop();
                    finishMeasurement(xCm, I, hTheo, finalAlpha);
                }
            }
        };
        measurementTimer.start();
    }

    private void finishMeasurement(double xCm, double I, double hTheo, double alpha) {
        liveStatusLabel.setText("СИСТЕМА: ЗАПИС");
        liveStatusLabel.setStyle("-fx-text-fill: yellow;");

        double hExp = cPrime * alpha;
        double error = Math.abs(hExp - hTheo) / hTheo * 100.0;

        Measurement m = new Measurement(
                idCounter++,
                Math.round(xCm * 10.0) / 10.0,
                I,
                Math.round(alpha * 10.0) / 10.0,
                Math.round(hExp * 10.0) / 10.0,
                Math.round(hTheo * 10.0) / 10.0,
                Math.round(error * 10.0) / 10.0
        );
        data.add(m);
        updateStats();

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(600); Platform.runLater(this::processNextAuto); }
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
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }

        double maxErr = 0;
        for (Measurement m : data) {
            if (m.getErrorPercent() > maxErr) maxErr = m.getErrorPercent();
        }

        String conclusion = String.format(Locale.US,
                "ОБРОБКА РЕЗУЛЬТАТІВ:\n" +
                        "1. Проведено вимірювань: %d шт.\n" +
                        "2. Балістична стала установки: C' = %.2f (А/м)/под.\n" +
                        "3. Максимальна відносна похибка: ε_max = %.1f %%.\n" +
                        "ВИСНОВОК: Зафіксовано спадання напруженості магнітного поля H від центру соленоїда до його країв. " +
                        "Експериментальні дані підтверджують розрахунки за законом Біо-Савара-Лапласа.",
                data.size(), cPrime, maxErr
        );

        finalResultLabel.setText(conclusion);
    }
}