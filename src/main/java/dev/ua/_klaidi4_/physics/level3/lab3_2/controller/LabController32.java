package dev.ua._klaidi4_.physics.level3.lab3_2.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level3.lab3_2.model.Measurement32;
import dev.ua._klaidi4_.physics.level3.lab3_2.view.TangentGalvanometerCanvas;
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

public class LabController32 extends BaseLabController {

    private TangentGalvanometerCanvas canvas;
    private TableView<Measurement32> table;
    private ObservableList<Measurement32> data;
    private int idCounter = 1;

    private ComboBox<String> nComboBox;
    private TextField iField;
    private TextField rField;
    private ComboBox<String> polarityBox;
    private TextField h0EarthField;

    private Button startBtn;
    private Button autoBtn;
    private Button analyzeBtn;
    private Button clearBtn;

    private Label liveStatusLabel;
    private Label gammaLabel;
    private Label h0Label;

    private Queue<AutoTestParam> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    private static class AutoTestParam {
        int n; double i; String polarity;
        AutoTestParam(int n, double i, String p) {
            this.n = n; this.i = i; this.polarity = p;
        }
    }

    public LabController32() {
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

        Label title = new Label("Керування (Лаб 3-2)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри тангенс-гальванометра");
        labPane.setCollapsible(true);
        labPane.setExpanded(true);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        nComboBox = new ComboBox<>(FXCollections.observableArrayList("100", "75", "50"));
        nComboBox.setValue("100");
        nComboBox.setMaxWidth(Double.MAX_VALUE);

        iField = new TextField("33.0");
        rField = new TextField("10.0");

        polarityBox = new ComboBox<>(FXCollections.observableArrayList("Позитивна (+)", "Негативна (-)"));
        polarityBox.setValue("Позитивна (+)");
        polarityBox.setMaxWidth(Double.MAX_VALUE);

        h0EarthField = new TextField("16.5");

        nComboBox.valueProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        iField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        rField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        polarityBox.valueProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        h0EarthField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());

        VBox constBox = new VBox(4);
        constBox.setStyle("-fx-background-color: #e8eff5; -fx-padding: 8; -fx-border-radius: 4;");
        Label constTitle = new Label("Налаштування середовища:");
        constTitle.setFont(Font.font("System", FontWeight.BOLD, 11));
        constBox.getChildren().addAll(
                constTitle,
                createInputGroup("Істинне поле Землі H0 (А/м):", h0EarthField)
        );

        paramsBox.getChildren().addAll(
                createInputGroup("Кількість витків N:", nComboBox),
                createInputGroup("Струм I (мА):", iField),
                createInputGroup("Радіус котушки R (см):", rField),
                createInputGroup("Напрямок струму:", polarityBox),
                constBox
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(290);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        startBtn = new Button("▶ ПОДАТИ СТРУМ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ АВТО (Точна лінійність 30°-60°)");
        autoBtn.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAuto());

        analyzeBtn = new Button("📊 ГРАФІКИ");
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
            gammaLabel.setText("γ = 0.0°");
            h0Label.setText("H0 = 0.0 А/м");
        });

        leftPanel.getChildren().addAll(title, labPane, startBtn, autoBtn, analyzeBtn, clearBtn);

        canvas = new TangentGalvanometerCanvas(600, 440);

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
        Label dashTitle = new Label("ШКАЛА ПРИЛАДУ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        gammaLabel = new Label("γ = 0.0°");
        gammaLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        gammaLabel.setStyle("-fx-text-fill: #00ff00;");
        h0Label = new Label("H0 = -- А/м");
        h0Label.setStyle("-fx-text-fill: #00ff00;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, gammaLabel, h0Label);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement32, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(35);
        TableColumn<Measurement32, Integer> nCol = new TableColumn<>("N");
        nCol.setCellValueFactory(new PropertyValueFactory<>("n"));
        TableColumn<Measurement32, Double> iCol = new TableColumn<>("I (А)");
        iCol.setCellValueFactory(new PropertyValueFactory<>("currentI"));
        TableColumn<Measurement32, String> polCol = new TableColumn<>("Пол.");
        polCol.setCellValueFactory(new PropertyValueFactory<>("polarity"));
        TableColumn<Measurement32, Double> angCol = new TableColumn<>("γ (°)");
        angCol.setCellValueFactory(new PropertyValueFactory<>("angle"));
        TableColumn<Measurement32, Double> h0Col = new TableColumn<>("H0 (А/м)");
        h0Col.setCellValueFactory(new PropertyValueFactory<>("h0"));

        table.getColumns().addAll(idCol, nCol, iCol, polCol, angCol, h0Col);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(130);

        VBox statsBox = createStatsBox();
        finalResultLabel.setText("Зробіть виміри та натисніть «ГРАФІКИ» для візуалізації.");

        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        canvas.setCallbacks(() -> Platform.runLater(this::finishMeasurement));
    }

    private void applyPhysicsSettings() {
        try {
            int n = Integer.parseInt(nComboBox.getValue());
            double currentA = Double.parseDouble(iField.getText()) / 1000.0;
            double rMeters = Double.parseDouble(rField.getText()) / 100.0;
            int pol = polarityBox.getValue().contains("+") ? 1 : -1;
            double earthH0 = Double.parseDouble(h0EarthField.getText());

            canvas.setEarthField(earthH0);
            canvas.setParameters(n, currentA, rMeters, pol);

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
        nComboBox.setDisable(disable);
        iField.setDisable(disable);
        rField.setDisable(disable);
        polarityBox.setDisable(disable);
        h0EarthField.setDisable(disable);
    }

    private void startManual() {
        try {
            Double.parseDouble(iField.getText());
            Double.parseDouble(rField.getText());
            Double.parseDouble(h0EarthField.getText());
            isAutoRunning = false;
            applyPhysicsSettings();
            startSimulation();
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа в поля.");
        }
    }

    private void startSimulation() {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: КОЛИВАННЯ...");
        liveStatusLabel.setStyle("-fx-text-fill: red;");
        gammaLabel.setText("γ = ???°");
        h0Label.setText("H0 = -- А/м");
        canvas.startSimulation();

        new Thread(() -> {
            while (startBtn.isDisabled()) {
                Platform.runLater(() -> {
                    if (!liveStatusLabel.getText().contains("ЗАВЕРШЕНО")) {
                        gammaLabel.setText(String.format("γ = %.1f°", Math.abs(canvas.getMeasuredAngle())));
                    }
                });
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    private void startAuto() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        try {
            double rMeters = Double.parseDouble(rField.getText()) / 100.0;
            double earthH0 = Double.parseDouble(h0EarthField.getText());
            int[] turnsArray = {100, 75, 50};
            double[] angles = {30.0, 45.0, 60.0};

            for (int n : turnsArray) {
                for (double targetAngle : angles) {
                    double requiredCurrentA = (2.0 * rMeters * earthH0 * Math.tan(Math.toRadians(targetAngle))) / n;
                    double requiredCurrent_mA = requiredCurrentA * 1000.0;
                    requiredCurrent_mA = Math.round(requiredCurrent_mA * 10.0) / 10.0;
                    autoQueue.add(new AutoTestParam(n, requiredCurrent_mA, "+"));
                }
            }

            isAutoRunning = true;
            processNextAuto();

        } catch (Exception e) {
            showAlert("Помилка", "Перевірте правильність вводу полів R та H0.");
        }
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
        nComboBox.setValue(String.valueOf(param.n));
        iField.setText(String.valueOf(param.i));
        polarityBox.setValue(param.polarity.equals("+") ? "Позитивна (+)" : "Негативна (-)");

        applyPhysicsSettings();
        startSimulation();
    }

    private void finishMeasurement() {
        try {
            int n = Integer.parseInt(nComboBox.getValue());
            double currentA = Double.parseDouble(iField.getText()) / 1000.0;
            double rM = Double.parseDouble(rField.getText()) / 100.0;
            String pol = polarityBox.getValue().contains("+") ? "+" : "-";

            double gammaRaw = canvas.getMeasuredAngle();
            if (pol.equals("-")) gammaRaw = -Math.abs(gammaRaw);
            else gammaRaw = Math.abs(gammaRaw);

            liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            gammaLabel.setText(String.format("γ = %.1f°", Math.abs(gammaRaw)));

            double tgGamma = Math.tan(Math.toRadians(gammaRaw));
            double h0 = tgGamma == 0 ? 0 : (n * currentA) / (2.0 * rM * tgGamma);

            h0Label.setText(String.format("H0 = %.2f А/м", Math.abs(h0)));

            Measurement32 meas = new Measurement32(
                    idCounter++, n, rM,
                    Math.round(currentA * 1000.0) / 1000.0,
                    pol,
                    Math.round(gammaRaw * 10.0) / 10.0,
                    Math.round(Math.abs(h0) * 100.0) / 100.0
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

        Measurement32 last = data.get(data.size() - 1);
        String calculation = String.format(
                "Останній розрахунок: H_0 = (N * I) / (2 * R * tg(γ))\n" +
                        "H_0 = (%d * %.3f) / (2 * %.2f * tg(%.1f°)) = %.2f А/м.",
                last.getN(), Math.abs(last.getCurrentI()), last.getR(), Math.abs(last.getAngle()), last.getH0()
        );

        finalResultLabel.setText(calculation);
    }

    private void showAnalysisDialog() {
        if (data.isEmpty()) {
            showAlert("Увага", "Спочатку проведіть вимірювання!");
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Графіки (Лаб 3-2)");

        TabPane tabPane = new TabPane();

        NumberAxis xAxis1 = new NumberAxis();
        xAxis1.setLabel("tg(γ)");
        NumberAxis yAxis1 = new NumberAxis();
        yAxis1.setLabel("Струм I (А)");

        LineChart<Number, Number> chart1 = new LineChart<>(xAxis1, yAxis1);
        chart1.setTitle("Перевірка лінійності: I = f(tg γ)");
        chart1.setCreateSymbols(true);

        XYChart.Series<Number, Number> seriesN100 = new XYChart.Series<>();
        seriesN100.setName("N = 100");
        XYChart.Series<Number, Number> seriesN75 = new XYChart.Series<>();
        seriesN75.setName("N = 75");
        XYChart.Series<Number, Number> seriesN50 = new XYChart.Series<>();
        seriesN50.setName("N = 50");

        seriesN100.getData().add(new XYChart.Data<>(0.0, 0.0));
        seriesN75.getData().add(new XYChart.Data<>(0.0, 0.0));
        seriesN50.getData().add(new XYChart.Data<>(0.0, 0.0));

        for (Measurement32 m : data) {
            double tg = Math.tan(Math.toRadians(m.getAngle()));
            double current = m.getPolarity().equals("-") ? -m.getCurrentI() : m.getCurrentI();

            if (m.getN() == 100) seriesN100.getData().add(new XYChart.Data<>(tg, current));
            if (m.getN() == 75) seriesN75.getData().add(new XYChart.Data<>(tg, current));
            if (m.getN() == 50) seriesN50.getData().add(new XYChart.Data<>(tg, current));
        }

        Comparator<XYChart.Data<Number, Number>> xComparator = Comparator.comparing(d -> d.getXValue().doubleValue());
        seriesN100.getData().sort(xComparator);
        seriesN75.getData().sort(xComparator);
        seriesN50.getData().sort(xComparator);

        chart1.getData().addAll(seriesN100, seriesN75, seriesN50);
        Tab tab1 = new Tab("Лінійність I(tg γ)", chart1);
        tab1.setClosable(false);

        NumberAxis xAxis2 = new NumberAxis();
        xAxis2.setLabel("Кут відхилення γ (градуси)");
        NumberAxis yAxis2 = new NumberAxis();
        yAxis2.setLabel("Коефіцієнт похибки 1/sin(2γ)");
        LineChart<Number, Number> chart2 = new LineChart<>(xAxis2, yAxis2);
        chart2.setTitle("Вплив кута на точність (Найточніше при 45°)");
        chart2.setCreateSymbols(false);

        XYChart.Series<Number, Number> errorTheory = new XYChart.Series<>();
        errorTheory.setName("Теоретична похибка");
        for (int a = 5; a <= 85; a += 2) {
            double errFactor = 1.0 / Math.abs(Math.sin(Math.toRadians(2 * a)));
            errorTheory.getData().add(new XYChart.Data<>(a, errFactor));
        }

        chart2.getData().add(errorTheory);
        Tab tab2 = new Tab("Аналіз похибки", chart2);
        tab2.setClosable(false);

        tabPane.getTabs().addAll(tab1, tab2);

        Scene scene = new Scene(tabPane, 750, 500);
        stage.setScene(scene);
        stage.show();
    }
}