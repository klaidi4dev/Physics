package dev.ua._klaidi4_.physics.level6.lab6_5.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level6.lab6_5.model.Measurement;
import dev.ua._klaidi4_.physics.level6.lab6_5.view.MagnetronCanvas;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LabController65 extends BaseLabController {

    private MagnetronCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;

    private TextField fieldL;
    private TextField fieldD;
    private TextField fieldN;
    private TextField fieldRa;
    private final double MU_0 = 4 * Math.PI * 1e-7;
    private double currentUa = 6.3;
    private double currentIc = 0.0;
    private double currentIa = 0.0;
    private double I_KR_TRUE = 0.285;

    private Slider icSlider;
    private Slider uaSlider;
    private LineChart<Number, Number> chart;
    private XYChart.Series<Number, Number> dataSeries;
    private XYChart.Series<Number, Number> topTangentSeries;
    private XYChart.Series<Number, Number> dropTangentSeries;
    private XYChart.Series<Number, Number> vLineSeries;

    private Label liveStatusLabel;
    private Label liveUaLabel;
    private Label liveIcLabel;
    private Label liveIaLabel;

    private Button addPointBtn;
    private Button autoRunBtn;
    private Button analyzeBtn;
    private Button clearBtn;

    public LabController65() {
        initUI();
    }

    @Override
    public void shutdown() {
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 6-5)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane setupPane = new TitledPane();
        setupPane.setText("Геометрія установки");
        setupPane.setCollapsible(false);
        VBox setupParams = new VBox(12);
        setupParams.setPadding(new Insets(5));

        fieldL = new TextField("0.15");
        fieldD = new TextField("0.05");
        fieldN = new TextField("1500");
        fieldRa = new TextField("0.005");

        setupParams.getChildren().addAll(
                createInputGroup("Довжина соленоїда L (м):", fieldL),
                createInputGroup("Діаметр D (м):", fieldD),
                createInputGroup("Кількість витків N:", fieldN),
                createInputGroup("Радіус анода Ra (м):", fieldRa)
        );
        setupPane.setContent(setupParams);

        TitledPane controlPane = new TitledPane();
        controlPane.setText("Управління живленням");
        controlPane.setCollapsible(false);
        VBox controlParams = new VBox(12);
        controlParams.setPadding(new Insets(5));

        Label uaLabel = new Label("Анодна напруга Ua: 6.3 В");
        uaSlider = new Slider(4.0, 10.0, 6.3);
        uaSlider.setShowTickMarks(true);
        uaSlider.valueProperty().addListener((o, old, val) -> {
            currentUa = val.doubleValue();
            uaLabel.setText(String.format("Анодна напруга Ua: %.1f В", currentUa));
            I_KR_TRUE = 0.285 * Math.sqrt(currentUa / 6.3);
            updatePhysics();
        });

        Label icLabel = new Label("Струм соленоїда Ic: 0.00 А");
        icSlider = new Slider(0.0, 1.2, 0.0);
        icSlider.setShowTickMarks(true);
        icSlider.valueProperty().addListener((o, old, val) -> {
            currentIc = val.doubleValue();
            icLabel.setText(String.format("Струм соленоїда Ic: %.2f А", currentIc));
            updatePhysics();
        });

        controlParams.getChildren().addAll(uaLabel, uaSlider, icLabel, icSlider);
        controlPane.setContent(controlParams);

        addPointBtn = new Button("✍ ЗАПИСАТИ ТОЧКУ");
        addPointBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        addPointBtn.setMaxWidth(Double.MAX_VALUE);
        addPointBtn.setOnAction(e -> recordPoint(currentIc));

        autoRunBtn = new Button("⚙ АВТО-ВИМІРЮВАННЯ");
        autoRunBtn.setStyle("-fx-background-color: #0288d1; -fx-text-fill: white; -fx-font-weight: bold;");
        autoRunBtn.setMaxWidth(Double.MAX_VALUE);
        autoRunBtn.setOnAction(e -> runAuto());

        analyzeBtn = new Button("📐 АНАЛІЗ (МЕТОД ДОТИЧНИХ)");
        analyzeBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        analyzeBtn.setMaxWidth(Double.MAX_VALUE);
        analyzeBtn.setOnAction(e -> performAnalysis());

        clearBtn = new Button("🗑 ОЧИСТИТИ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> clearAll());

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, setupPane, controlPane, addPointBtn, autoRunBtn, analyzeBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new MagnetronCanvas(550, 240);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));
        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(200, 100);

        Label dashTitle = new Label("ПОКАЗИ ПРИЛАДІВ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ГОТОВО");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveUaLabel = new Label("Ua = 6.30 В");
        liveUaLabel.setStyle("-fx-text-fill: #00ff00;");
        liveIcLabel = new Label("Ic = 0.00 А");
        liveIcLabel.setStyle("-fx-text-fill: #00ff00;");
        liveIaLabel = new Label("Ia = 0.00 мА");
        liveIaLabel.setStyle("-fx-text-fill: #00ff00;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveUaLabel, liveIcLabel, liveIaLabel);

        StackPane canvasStack = new StackPane(canvas, topBar, dash);
        canvasStack.setStyle("-fx-background-color: #ffffff;");
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));

        NumberAxis xAxis = new NumberAxis("Струм соленоїда Ic (А)", 0, 1.2, 0.1);
        NumberAxis yAxis = new NumberAxis("Анодний струм Ia (мА)", 0, 8, 1);
        chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        chart.setPrefHeight(250);

        dataSeries = new XYChart.Series<>();
        dataSeries.setName("Залежність Ia = f(Ic)");
        topTangentSeries = new XYChart.Series<>();
        topTangentSeries.setName("Дотична 1");
        dropTangentSeries = new XYChart.Series<>();
        dropTangentSeries.setName("Дотична 2");
        vLineSeries = new XYChart.Series<>();
        vLineSeries.setName("I_кр");

        chart.getData().addAll(dataSeries, topTangentSeries, dropTangentSeries, vLineSeries);

        VBox centerPanel = new VBox(canvasStack, chart);

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Double> colIc = new TableColumn<>("Ic (А)");
        colIc.setCellValueFactory(new PropertyValueFactory<>("ic"));
        TableColumn<Measurement, Double> colIa = new TableColumn<>("Ia (мА)");
        colIa.setCellValueFactory(new PropertyValueFactory<>("ia"));

        table.getColumns().addAll(colIc, colIa);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(120);
        table.setPrefWidth(200);

        VBox statsBox = createStatsBox();
        HBox bottomHBox = new HBox(10, table, statsBox);
        HBox.setHgrow(statsBox, Priority.ALWAYS);
        bottomHBox.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomHBox);

        updatePhysics();
    }

    private void updatePhysics() {
        double iaMax = 2.0 + (currentUa * 0.4);
        currentIa = iaMax / (1 + Math.exp(50 * (currentIc - I_KR_TRUE)));

        liveUaLabel.setText(String.format("Ua = %.2f В", currentUa));
        liveIcLabel.setText(String.format("Ic = %.2f А", currentIc));
        liveIaLabel.setText(String.format("Ia = %.2f мА", currentIa));

        if (canvas != null) {
            canvas.updatePhysicsParameters(currentIc, currentUa);
        }
    }

    private void recordPoint(double ic) {
        double iaMax = 2.0 + (currentUa * 0.4);
        double ia = iaMax / (1 + Math.exp(50 * (ic - I_KR_TRUE)));
        ia += (Math.random() - 0.5) * 0.15;
        if (ia < 0.05) ia = 0.05;

        Measurement m = new Measurement(idCounter++, Math.round(ic * 100) / 100.0, Math.round(ia * 100) / 100.0);
        data.add(m);
        dataSeries.getData().add(new XYChart.Data<>(m.getIc(), m.getIa()));
    }

    private void runAuto() {
        clearAll();
        liveStatusLabel.setText("Статус: АВТОВИМІРЮВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: red;");
        setControlsDisable(true);

        new Thread(() -> {
            for (double ic = 0.0; ic <= 1.0; ic += 0.05) {
                final double loopIc = ic;
                Platform.runLater(() -> {
                    icSlider.setValue(loopIc);
                    recordPoint(loopIc);
                });
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }
            Platform.runLater(() -> {
                liveStatusLabel.setText("Статус: ГОТОВО");
                liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
                setControlsDisable(false);
            });
        }).start();
    }

    private void setControlsDisable(boolean disable) {
        icSlider.setDisable(disable);
        uaSlider.setDisable(disable);
        addPointBtn.setDisable(disable);
        autoRunBtn.setDisable(disable);
        analyzeBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        fieldL.setDisable(disable);
        fieldD.setDisable(disable);
        fieldN.setDisable(disable);
        fieldRa.setDisable(disable);
    }

    private void performAnalysis() {
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }
        if (data.size() < 5) {
            finalResultLabel.setText("Помилка: Недостатньо даних для аналізу. Зніміть ВАХ.");
            return;
        }
        try {
            double L_val = Double.parseDouble(fieldL.getText());
            double D_val = Double.parseDouble(fieldD.getText());
            double N_val = Double.parseDouble(fieldN.getText());
            double Ra_val = Double.parseDouble(fieldRa.getText());

            double yMax = data.stream().limit(3).mapToDouble(Measurement::getIa).average().orElse(0);
            topTangentSeries.getData().clear();
            topTangentSeries.getData().add(new XYChart.Data<>(0.0, yMax));
            topTangentSeries.getData().add(new XYChart.Data<>(1.0, yMax));

            int bestIdx = 0;
            double minSlope = 0;
            for (int i = 0; i < data.size() - 1; i++) {
                double dx = data.get(i+1).getIc() - data.get(i).getIc();
                if (dx == 0) continue;
                double slope = (data.get(i+1).getIa() - data.get(i).getIa()) / dx;
                if (slope < minSlope) {
                    minSlope = slope;
                    bestIdx = i;
                }
            }

            double x0 = data.get(bestIdx).getIc();
            double y0 = data.get(bestIdx).getIa();
            dropTangentSeries.getData().clear();
            dropTangentSeries.getData().add(new XYChart.Data<>(x0 - 0.3, y0 + minSlope * (-0.3)));
            dropTangentSeries.getData().add(new XYChart.Data<>(x0 + 0.3, y0 + minSlope * (0.3)));

            double i_kr = x0 + (yMax - y0) / minSlope;
            vLineSeries.getData().clear();
            vLineSeries.getData().add(new XYChart.Data<>(i_kr, yMax));
            vLineSeries.getData().add(new XYChart.Data<>(i_kr, 0.0));

            double B_kr = (MU_0 * N_val * i_kr) / Math.sqrt(L_val * L_val + D_val * D_val);
            double em = (8 * currentUa) / (B_kr * B_kr * Ra_val * Ra_val);

            double emTrue = 1.75882e11;
            double error = Math.abs(em - emTrue) / emTrue * 100.0;

            StringBuilder sb = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ ЕКСПЕРИМЕНТУ:\n");
            sb.append(String.format("1. З графіка визначено критичний струм соленоїда: Iкр = %.3f А.\n", i_kr));
            sb.append(String.format("2. Розраховано індукцію магнітного поля: Bкр = %.4f Тл.\n", B_kr));
            sb.append(String.format("3. Обчислено питомий заряд електрона: e/m = %.2e Кл/кг.\n", em));
            sb.append(String.format("4. Відносна похибка вимірювань: ε = %.1f %%.\n", error));
            sb.append("5. Висновок: Побудовано скидну характеристику магнетрона Ia=f(Ic). Значення e/m збігається з табличним.");

            finalResultLabel.setText(sb.toString());
        } catch (Exception ignored) {}
    }

    private void clearAll() {
        data.clear();
        dataSeries.getData().clear();
        topTangentSeries.getData().clear();
        dropTangentSeries.getData().clear();
        vLineSeries.getData().clear();
        idCounter = 1;
        finalResultLabel.setText("Обробка результатів: Дані очищено.");
    }
}