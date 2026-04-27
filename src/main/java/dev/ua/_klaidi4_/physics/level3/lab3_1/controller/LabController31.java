package dev.ua._klaidi4_.physics.level3.lab3_1.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level3.lab3_1.model.Measurement31;
import dev.ua._klaidi4_.physics.level3.lab3_1.view.GalvanometerCanvas;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

public class LabController31 extends BaseLabController {

    private GalvanometerCanvas canvas;
    private TableView<Measurement31> table;
    private ObservableList<Measurement31> data;
    private int idCounter = 1;
    private TextField l0Field;
    private TextField iField;
    private TextField xField;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Button analyzeBtn;
    private Label liveStatusLabel;
    private Label liveNLabel;
    private Label bLabel;
    private Queue<AutoTestParam> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    private static class AutoTestParam {
        double l0, I, x;
        AutoTestParam(double l0, double I, double x) {
            this.l0 = l0; this.I = I; this.x = x;
        }
    }

    public LabController31() {
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

        Label title = new Label("Система управління (Лаб 3-1)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри установки");
        labPane.setCollapsible(true);
        labPane.setExpanded(true);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        l0Field = new TextField("20");
        iField = new TextField("1.5");
        xField = new TextField("0.0");

        l0Field.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        iField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        xField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());

        paramsBox.getChildren().addAll(
                createInputGroup("Проміжок l0 (мм) [20 або 30]:", l0Field),
                createInputGroup("Струм магніту I (А):", iField),
                createInputGroup("Координата котушки x (см):", xField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(200);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        startBtn = new Button("▶ ПЕРЕМИКНУТИ СТРУМ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ (ЗАВ. 1 і 2)");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAuto());

        analyzeBtn = new Button("📊 ОБРОБКА ТА ГРАФІКИ");
        analyzeBtn.setStyle("-fx-background-color: #8e24aa; -fx-text-fill: white; -fx-font-weight: bold;");
        analyzeBtn.setMaxWidth(Double.MAX_VALUE);
        analyzeBtn.setOnAction(e -> showAnalysisDialog());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear();
            idCounter = 1;
            updateStats();
            liveNLabel.setText("n = 0.0 под.");
            bLabel.setText("B = 0.000 Тл");
        });

        leftPanel.getChildren().addAll(title, labPane, startBtn, autoBtn, analyzeBtn, clearBtn);

        canvas = new GalvanometerCanvas(600, 440);

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
        Label dashTitle = new Label("ГАЛЬВАНОМЕТР");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveNLabel = new Label("n = 0.0 под.");
        liveNLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        liveNLabel.setStyle("-fx-text-fill: #00ff00;");
        bLabel = new Label("B = 0.000 Тл");
        bLabel.setStyle("-fx-text-fill: #00ff00;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveNLabel, bLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement31, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement31, Double> l0Col = new TableColumn<>("l0 (мм)");
        l0Col.setCellValueFactory(new PropertyValueFactory<>("l0"));
        TableColumn<Measurement31, Double> iCol = new TableColumn<>("I (А)");
        iCol.setCellValueFactory(new PropertyValueFactory<>("currentI"));
        TableColumn<Measurement31, Double> xCol = new TableColumn<>("x (см)");
        xCol.setCellValueFactory(new PropertyValueFactory<>("x"));
        TableColumn<Measurement31, Double> nCol = new TableColumn<>("n (под.)");
        nCol.setCellValueFactory(new PropertyValueFactory<>("n"));
        TableColumn<Measurement31, Double> bCol = new TableColumn<>("B (Тл)");
        bCol.setCellValueFactory(new PropertyValueFactory<>("b"));

        table.getColumns().addAll(idCol, l0Col, iCol, xCol, nCol, bCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(130);

        VBox statsBox = createStatsBox();
        finalResultLabel.setText("Для перегляду графіків та аналізу натисніть кнопку «ОБРОБКА ТА ГРАФІКИ».");

        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        canvas.setCallbacks(
                () -> Platform.runLater(() -> liveNLabel.setText(String.format("n = %.1f под.", canvas.getMeasuredN()))),
                () -> Platform.runLater(this::finishMeasurement)
        );
    }

    private void applyPhysicsSettings() {
        try {
            double l0 = Double.parseDouble(l0Field.getText());
            double current = Double.parseDouble(iField.getText());
            double x = Double.parseDouble(xField.getText());
            canvas.setParameters(l0, current, x);
            if (!isAutoRunning) {
                liveStatusLabel.setText("Статус: ГОТОВО");
                liveStatusLabel.setStyle("-fx-text-fill: yellow;");
            }
        } catch (NumberFormatException ignored) {}
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        analyzeBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        l0Field.setDisable(disable);
        iField.setDisable(disable);
        xField.setDisable(disable);
    }

    private void startManual() {
        try {
            Double.parseDouble(l0Field.getText());
            Double.parseDouble(iField.getText());
            Double.parseDouble(xField.getText());
            isAutoRunning = false;
            applyPhysicsSettings();
            startSimulation();
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа в поля.");
        }
    }

    private void startSimulation() {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: ІМПУЛЬС");
        liveStatusLabel.setStyle("-fx-text-fill: red;");
        liveNLabel.setText("n = 0.0 под.");
        bLabel.setText("B = 0.000 Тл");
        canvas.startSimulation();
    }

    private void startAuto() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        autoQueue.add(new AutoTestParam(20, 1.5, 0));
        autoQueue.add(new AutoTestParam(20, 1.5, 1));
        autoQueue.add(new AutoTestParam(20, 1.5, 2));
        autoQueue.add(new AutoTestParam(20, 1.5, 3));
        autoQueue.add(new AutoTestParam(20, 1.5, 4));

        autoQueue.add(new AutoTestParam(30, 1.5, 0));
        autoQueue.add(new AutoTestParam(30, 1.5, 1));
        autoQueue.add(new AutoTestParam(30, 1.5, 2));
        autoQueue.add(new AutoTestParam(30, 1.5, 3));
        autoQueue.add(new AutoTestParam(30, 1.5, 4));

        autoQueue.add(new AutoTestParam(20, 1.2, 0));
        autoQueue.add(new AutoTestParam(20, 0.9, 0));
        autoQueue.add(new AutoTestParam(20, 0.6, 0));
        autoQueue.add(new AutoTestParam(20, 0.3, 0));

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
        AutoTestParam param = autoQueue.poll();
        l0Field.setText(String.valueOf(param.l0));
        iField.setText(String.valueOf(param.I));
        xField.setText(String.valueOf(param.x));

        applyPhysicsSettings();
        startSimulation();
    }

    private void finishMeasurement() {
        try {
            double l0 = Double.parseDouble(l0Field.getText());
            double current = Double.parseDouble(iField.getText());
            double x = Double.parseDouble(xField.getText());

            double measuredN = canvas.getMeasuredN();

            liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

            double C = 0.0045;
            double b = measuredN * C;

            bLabel.setText(String.format("B = %.3f Тл", b));

            Measurement31 meas = new Measurement31(
                    idCounter++, l0, current, x,
                    Math.round(measuredN * 10.0) / 10.0,
                    Math.round(b * 1000.0) / 1000.0
            );
            data.add(meas);
            updateStats();

            if (isAutoRunning) {
                new Thread(() -> {
                    try { Thread.sleep(700); Platform.runLater(this::processNextAuto); }
                    catch (InterruptedException ignored) {}
                }).start();
            } else {
                setControlsDisable(false);
            }
        } catch (Exception ignored) {}
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

        double maxB = 0;
        for (Measurement31 m : data) {
            if (m.getB() > maxB) {
                maxB = m.getB();
            }
        }

        Measurement31 last = data.get(data.size() - 1);
        double c = 0.0045;

        String conclusion = String.format(
                "ОБРОБКА РЕЗУЛЬТАТІВ:\n" +
                        "1. Робоча формула: B = [β(R+R_g) / (2NS)] * n = C * n\n" +
                        "2. Приклад розрахунку (останній вимір): B = %.4f * %.1f = %.4f Тл.\n" +
                        "3. Максимальне зафіксоване поле в центрі зазору: B_max = %.4f Тл.\n\n" +
                        "➡ Натисніть «ОБРОБКА ТА ГРАФІКИ» для побудови залежностей B=f(x) та B=f(I).",
                c, last.getN(), last.getB(), maxB
        );

        finalResultLabel.setText(conclusion);
    }

    private void showAnalysisDialog() {
        if (data.isEmpty()) {
            showAlert("Увага", "Спочатку проведіть вимірювання або запустіть автопроходження!");
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Обробка результатів та графіки (Лаб 3-1)");

        TabPane tabPane = new TabPane();

        NumberAxis xAxis1 = new NumberAxis();
        xAxis1.setLabel("Координата x (см)");
        NumberAxis yAxis1 = new NumberAxis();
        yAxis1.setLabel("Індукція B (Тл)");
        LineChart<Number, Number> chart1 = new LineChart<>(xAxis1, yAxis1);
        chart1.setTitle("Залежність магнітної індукції від координати: B = f(x)");

        XYChart.Series<Number, Number> series20 = new XYChart.Series<>();
        series20.setName("l0 = 20 мм");
        XYChart.Series<Number, Number> series30 = new XYChart.Series<>();
        series30.setName("l0 = 30 мм");

        NumberAxis xAxis2 = new NumberAxis();
        xAxis2.setLabel("Струм I (А)");
        NumberAxis yAxis2 = new NumberAxis();
        yAxis2.setLabel("Індукція B (Тл)");
        LineChart<Number, Number> chart2 = new LineChart<>(xAxis2, yAxis2);
        chart2.setTitle("Залежність магнітної індукції від струму: B = f(I)");
        XYChart.Series<Number, Number> seriesI = new XYChart.Series<>();
        seriesI.setName("l0 = 20 мм, x = 0");

        for (Measurement31 m : data) {
            if (m.getCurrentI() >= 1.4 && m.getCurrentI() <= 1.6) {
                if (m.getL0() == 20) series20.getData().add(new XYChart.Data<>(m.getX(), m.getB()));
                if (m.getL0() == 30) series30.getData().add(new XYChart.Data<>(m.getX(), m.getB()));
            }
            if (m.getX() == 0 && m.getL0() == 20) {
                seriesI.getData().add(new XYChart.Data<>(m.getCurrentI(), m.getB()));
            }
        }

        series20.getData().sort(Comparator.comparing(d -> d.getXValue().doubleValue()));
        series30.getData().sort(Comparator.comparing(d -> d.getXValue().doubleValue()));
        seriesI.getData().sort(Comparator.comparing(d -> d.getXValue().doubleValue()));

        chart1.getData().addAll(series20, series30);
        chart2.getData().add(seriesI);

        Tab tab1 = new Tab("Завдання 1: B=f(x)", chart1);
        tab1.setClosable(false);
        Tab tab2 = new Tab("Завдання 2: B=f(I)", chart2);
        tab2.setClosable(false);

        tabPane.getTabs().addAll(tab1, tab2);

        Scene scene = new Scene(tabPane, 750, 500);
        stage.setScene(scene);
        stage.show();
    }
}