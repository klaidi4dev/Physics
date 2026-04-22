package dev.ua._klaidi4_.physics.level1.lab1_10.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level1.lab1_10.model.Measurement;
import dev.ua._klaidi4_.physics.level1.lab1_10.view.RollingCanvas;
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

public class LabController110 extends BaseLabController {

    private RollingCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> bodyCombo;
    private TextField mField;
    private TextField rField;
    private TextField sField;
    private Slider angleSlider;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveTimeLabel;
    private Queue<Integer> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    public LabController110() {
        initUI();
        applyPhysicsSettings();
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

        Label title = new Label("Система управління (Лаб 1-10)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри установки");
        labPane.setCollapsible(false);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        bodyCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Суцільний циліндр (Диск)",
                "Суцільна куля",
                "Порожнистий циліндр (Кільце)"
        ));
        bodyCombo.getSelectionModel().selectFirst();

        mField = new TextField("0.400");
        rField = new TextField("0.05");
        sField = new TextField("1.0");

        bodyCombo.setOnAction(e -> applyPhysicsSettings());
        mField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        rField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        sField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());

        paramsBox.getChildren().addAll(
                createInputGroup("Досліджуване тіло:", bodyCombo),
                createInputGroup("Маса тіла m (кг):", mField),
                createInputGroup("Радіус тіла R (м):", rField),
                createInputGroup("Шлях скочування S (м):", sField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(220);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        TitledPane physicsPane = new TitledPane();
        physicsPane.setText("Налаштування площини");
        VBox physBox = new VBox(5);
        physBox.setPadding(new Insets(5));

        Label angleLabel = new Label("Кут нахилу α: 15.0°");
        angleSlider = new Slider(5, 45, 15);
        angleSlider.setShowTickMarks(true);
        angleSlider.setShowTickLabels(true);
        angleSlider.valueProperty().addListener((o, ov, nv) -> {
            angleLabel.setText(String.format("Кут нахилу α: %.1f°", nv.doubleValue()));
            applyPhysicsSettings();
        });
        physBox.getChildren().addAll(angleLabel, angleSlider);
        physicsPane.setContent(physBox);

        startBtn = new Button("▶ ВІДПУСТИТИ ТІЛО");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ (Всі 3 тіла)");
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
        });

        leftPanel.getChildren().addAll(title, labPane, physicsPane, startBtn, autoBtn, clearBtn);

        canvas = new RollingCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(220, 60);
        Label dashTitle = new Label("ЕЛЕКТРОННИЙ СЕКУНДОМІР");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveTimeLabel = new Label("t = 0.00 с");
        liveTimeLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        liveTimeLabel.setStyle("-fx-text-fill: #00ff00;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveTimeLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, String> bodyCol = new TableColumn<>("Досліджуване тіло");
        bodyCol.setCellValueFactory(new PropertyValueFactory<>("bodyType"));
        TableColumn<Measurement, Double> mCol = new TableColumn<>("m (кг)");
        mCol.setCellValueFactory(new PropertyValueFactory<>("m"));
        TableColumn<Measurement, Double> rCol = new TableColumn<>("R (м)");
        rCol.setCellValueFactory(new PropertyValueFactory<>("r"));
        TableColumn<Measurement, Double> tCol = new TableColumn<>("t (с)");
        tCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        TableColumn<Measurement, Double> eCol = new TableColumn<>("I експ (кг·м²)");
        eCol.setCellValueFactory(new PropertyValueFactory<>("expI"));
        TableColumn<Measurement, Double> tICol = new TableColumn<>("I теор (кг·м²)");
        tICol.setCellValueFactory(new PropertyValueFactory<>("theoI"));

        table.getColumns().addAll(idCol, bodyCol, mCol, rCol, tCol, eCol, tICol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(140);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);
    }

    private void applyPhysicsSettings() {
        try {
            int bodyIndex = bodyCombo.getSelectionModel().getSelectedIndex();
            double angle = angleSlider.getValue();
            canvas.setParameters(bodyIndex, angle);
        } catch (Exception ignored) {}
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        bodyCombo.setDisable(disable);
        mField.setDisable(disable);
        rField.setDisable(disable);
        sField.setDisable(disable);
        angleSlider.setDisable(disable);
    }

    private void startManual() {
        try {
            Double.parseDouble(mField.getText());
            isAutoRunning = false;
            runSimulation(bodyCombo.getSelectionModel().getSelectedIndex());
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа в поля.");
        }
    }

    private void startAuto() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();
        autoQueue.add(0);
        autoQueue.add(1);
        autoQueue.add(2);

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
        int nextBody = autoQueue.poll();
        bodyCombo.getSelectionModel().select(nextBody);
        applyPhysicsSettings();
        runSimulation(nextBody);
    }

    private void runSimulation(int bodyIndex) {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: СКОЧУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: cyan;");

        double m = Double.parseDouble(mField.getText());
        double R = Double.parseDouble(rField.getText());
        double S = Double.parseDouble(sField.getText());
        double angleRad = Math.toRadians(angleSlider.getValue());

        double iTheory = 0;
        if (bodyIndex == 0) iTheory = 0.5 * m * R * R;
        else if (bodyIndex == 1) iTheory = 0.4 * m * R * R;
        else if (bodyIndex == 2) iTheory = 1.0 * m * R * R;

        double a = (9.81 * Math.sin(angleRad)) / (1 + iTheory / (m * R * R));

        final double exactTime = Math.sqrt(2 * S / a);

        final double finalMeasuredTime = exactTime + (Math.random() - 0.5) * 0.06;
        final double finalITheory = iTheory;

        canvas.setOnFinishCallback(() -> Platform.runLater(() -> finishMeasurement(bodyIndex, finalMeasuredTime, finalITheory)));        canvas.startSimulation(finalMeasuredTime);

        new Thread(() -> {
            long start = System.currentTimeMillis();
            long targetMs = (long) (finalMeasuredTime * 1000);

            while (System.currentTimeMillis() - start < targetMs) {
                long elapsed = System.currentTimeMillis() - start;
                double seconds = elapsed / 1000.0;
                Platform.runLater(() -> liveTimeLabel.setText(String.format("t = %.3f с", seconds)));
                try { Thread.sleep(30); } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    private void finishMeasurement(int bodyIndex, double measuredTime, double iTheory) {
        liveTimeLabel.setText(String.format("t = %.3f с", measuredTime));
        liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

        double m = Double.parseDouble(mField.getText());
        double R = Double.parseDouble(rField.getText());
        double S = Double.parseDouble(sField.getText());
        double angleRad = Math.toRadians(angleSlider.getValue());
        double iExp = m * R * R * ((9.81 * measuredTime * measuredTime * Math.sin(angleRad)) / (2 * S) - 1);

        String bodyName = bodyCombo.getItems().get(bodyIndex);

        Measurement meas = new Measurement(
                idCounter++, bodyName, m, R,
                Math.round(measuredTime * 1000.0) / 1000.0,
                Math.round(iExp * 100000.0) / 100000.0,
                Math.round(iTheory * 100000.0) / 100000.0
        );
        data.add(meas);
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

        double sumEps = 0;
        double sumDelta = 0;

        for (Measurement meas : data) {
            double delta = Math.abs(meas.getExpI() - meas.getTheoI());
            sumDelta += delta;
            sumEps += (delta / meas.getTheoI()) * 100;
        }

        double avgDelta = sumDelta / data.size();
        double avgEps = sumEps / data.size();

        String conclusion = String.format(
                "ОБРОБКА РЕЗУЛЬТАТІВ ЕКСПЕРИМЕНТУ ТА ЇХ АНАЛІЗ:\n" +
                        "1. За формулою (9) вирахувано експериментальний момент інерції I_експ для кожного тіла.\n" +
                        "2. За формулами з Таблиці 1 вирахувано теоретичний момент інерції I_теор (Циліндр: 1/2 mR², Куля: 2/5 mR², Кільце: mR²).\n" +
                        "3. СПІВСТАВЛЕННЯ ТА ВИСНОВКИ: Результати експерименту підтверджують, що розподіл маси впливає на момент інерції. " +
                        "Найбільший момент інерції (і найменше прискорення) має порожнистий циліндр, а найменший — суцільна куля.\n" +
                        "4. Похибки експерименту: Середня абсолютна похибка ΔI = %.5f кг·м², відносна похибка ε = %.1f %%.\n" +
                        "   (Похибка зумовлена нехтуванням тертям кочення та опором повітря).",
                avgDelta, avgEps
        );

        finalResultLabel.setText(conclusion);
    }
}