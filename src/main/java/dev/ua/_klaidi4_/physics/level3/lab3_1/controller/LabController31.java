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
import java.util.Locale;
import java.util.Queue;

public class LabController31 extends BaseLabController {

    private GalvanometerCanvas canvas;
    private TableView<Measurement31> table;
    private ObservableList<Measurement31> data;
    private int idCounter = 1;

    private ComboBox<String> l0Box;
    private TextField iField;
    private TextField xField;

    private TextField fieldR;
    private TextField fieldRg;
    private TextField fieldBeta;
    private TextField fieldN;
    private TextField fieldS;

    private Button recordBtn;
    private Button autoBtn;
    private Button analyzeBtn;
    private Button clearBtn;

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

        Label title = new Label("Система управління (Лаб 3-1)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane constantsPane = new TitledPane();
        constantsPane.setText("Константи приладів");
        constantsPane.setCollapsible(true);
        constantsPane.setExpanded(false);

        VBox constantsBox = new VBox(10);
        constantsBox.setPadding(new Insets(5));

        fieldR = new TextField("14.0");
        fieldRg = new TextField("61.0");
        fieldBeta = new TextField("3.61");
        fieldN = new TextField("1734");
        fieldS = new TextField("16.0");

        constantsBox.getChildren().addAll(
                createInputGroup("Опір котушки R (Ом):", fieldR),
                createInputGroup("Опір гальванометра Rg (Ом):", fieldRg),
                createInputGroup("Стала гальв. β (нКл/под):", fieldBeta),
                createInputGroup("Кількість витків N:", fieldN),
                createInputGroup("Площа витка S (мм²):", fieldS)
        );
        constantsPane.setContent(constantsBox);

        TitledPane paramsPane = new TitledPane();
        paramsPane.setText("Параметри установки");
        paramsPane.setCollapsible(false);
        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        l0Box = new ComboBox<>();
        l0Box.getItems().addAll("20.0", "30.0");
        l0Box.getSelectionModel().selectFirst();
        l0Box.setMaxWidth(Double.MAX_VALUE);

        iField = new TextField("1.00");
        xField = new TextField("0");

        l0Box.setOnAction(e -> updateCanvasVisuals());
        iField.textProperty().addListener((o, ov, nv) -> updateCanvasVisuals());
        xField.textProperty().addListener((o, ov, nv) -> updateCanvasVisuals());

        paramsBox.getChildren().addAll(
                createInputGroup("Проміжок l0 (мм):", l0Box),
                createInputGroup("Струм електромагніту I (А):", iField),
                createInputGroup("Координата котушки x (см):", xField)
        );
        paramsPane.setContent(paramsBox);

        recordBtn = new Button("▶ ПЕРЕМИКНУТИ СТРУМ");
        recordBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        recordBtn.setMaxWidth(Double.MAX_VALUE);
        recordBtn.setOnAction(e -> triggerMeasurement());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ (Всі завдання)");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
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
            liveNLabel.setText("n = 0 под.");
            bLabel.setText("B = 0.0000 мТл");
        });

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, constantsPane, paramsPane, recordBtn, autoBtn, analyzeBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new GalvanometerCanvas(600, 300);

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
        Label dashTitle = new Label("ПОКАЗНИКИ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ГОТОВО");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveNLabel = new Label("n = 0 под.");
        liveNLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        liveNLabel.setStyle("-fx-text-fill: #00ff00;");
        bLabel = new Label("B = 0.0000 мТл");
        bLabel.setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveNLabel, bLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        String[] cols = {"№", "l0 (мм)", "I (А)", "x (см)", "n (под.)", "B (мТл)"};
        String[] props = {"id", "l0", "currentI", "x", "n", "b"};

        for (int i = 0; i < cols.length; i++) {
            TableColumn<Measurement31, Object> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(new PropertyValueFactory<>(props[i]));
            table.getColumns().add(col);
        }
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        updateCanvasVisuals();
    }

    private double calculateCoeffC() {
        try {
            double R = Double.parseDouble(fieldR.getText().replace(',', '.'));
            double Rg = Double.parseDouble(fieldRg.getText().replace(',', '.'));
            double beta = Double.parseDouble(fieldBeta.getText().replace(',', '.'));
            double N = Double.parseDouble(fieldN.getText().replace(',', '.'));
            double S = Double.parseDouble(fieldS.getText().replace(',', '.'));

            return (beta * 1e-9 * (R + Rg)) / (2 * N * S * 1e-6) * 1000.0;
        } catch (Exception e) {
            return 0.0048794;
        }
    }

    private void updateCanvasVisuals() {
        if (isAutoRunning) return;
        try {
            double l0 = Double.parseDouble(l0Box.getValue());
            double x = Double.parseDouble(xField.getText().replace(',', '.'));
            canvas.updatePhysics(l0, x);
        } catch (Exception ignored) {}
    }

    private void setControlsDisable(boolean disable) {
        l0Box.setDisable(disable);
        iField.setDisable(disable);
        xField.setDisable(disable);
        fieldR.setDisable(disable);
        fieldRg.setDisable(disable);
        fieldBeta.setDisable(disable);
        fieldN.setDisable(disable);
        fieldS.setDisable(disable);
        recordBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        analyzeBtn.setDisable(disable);
        clearBtn.setDisable(disable);
    }

    private double calculateTargetN(double l0, double I, double x) {
        double nMax = (l0 == 30.0) ? 41.0 : 55.0;
        double decay = 1.0;

        if (l0 == 30.0) {
            if (x == 0) decay = 1.0;
            else if (x == 1) decay = 30.0 / 41.0;
            else if (x == 2) decay = 23.0 / 41.0;
            else if (x == 3) decay = 18.0 / 41.0;
            else if (x == 4) decay = 14.0 / 41.0;
            else if (x == 5) decay = 10.0 / 41.0;
            else if (x == 6) decay = 7.0 / 41.0;
            else if (x == 7) decay = 5.0 / 41.0;
            else decay = Math.exp(-0.25 * x);
        } else {
            if (x == 0) decay = 1.0;
            else if (x == 1) decay = 40.0 / 55.0;
            else if (x == 2) decay = 30.0 / 55.0;
            else if (x == 3) decay = 20.0 / 55.0;
            else if (x == 4) decay = 15.0 / 55.0;
            else if (x == 5) decay = 11.0 / 55.0;
            else if (x == 6) decay = 8.0 / 55.0;
            else if (x == 7) decay = 6.0 / 55.0;
            else decay = Math.exp(-0.28 * x);
        }

        return Math.round(nMax * (I / 1.0) * decay);
    }

    private void triggerMeasurement() {
        try {
            double l0 = Double.parseDouble(l0Box.getValue());
            double currentI = Double.parseDouble(iField.getText().replace(',', '.'));
            double x = Double.parseDouble(xField.getText().replace(',', '.'));

            setControlsDisable(true);
            liveStatusLabel.setText("Статус: ІМПУЛЬС...");
            liveStatusLabel.setStyle("-fx-text-fill: red;");

            double targetN = calculateTargetN(l0, currentI, x);
            double coeffC = calculateCoeffC();

            canvas.updatePhysics(l0, x);
            canvas.playPulse(targetN, () -> {
                double bVal = targetN * coeffC;
                Measurement31 meas = new Measurement31(
                        idCounter++, l0, currentI, x, targetN,
                        Math.round(bVal * 10000.0) / 10000.0
                );
                data.add(meas);

                Platform.runLater(() -> {
                    liveNLabel.setText(String.format(Locale.US, "n = %.0f под.", targetN));
                    bLabel.setText(String.format(Locale.US, "B = %.4f мТл", bVal));
                    liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
                    liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
                    updateStats();

                    if (isAutoRunning) {
                        processNextAuto();
                    } else {
                        setControlsDisable(false);
                    }
                });
            });

        } catch (Exception e) {
            showAlert("Помилка", "Перевірте правильність введених даних.");
            setControlsDisable(false);
        }
    }

    private void startAuto() {
        data.clear();
        idCounter = 1;
        autoQueue.clear();

        for (int x = 0; x <= 7; x++) autoQueue.add(new AutoTestParam(20, 1.0, x));
        for (int x = 0; x <= 7; x++) autoQueue.add(new AutoTestParam(30, 1.0, x));

        double[] currents = {0.95, 0.90, 0.85, 0.80, 0.75, 0.70, 0.65, 0.60, 0.55};
        for (double i : currents) {
            autoQueue.add(new AutoTestParam(20, i, 0));
        }

        isAutoRunning = true;
        processNextAuto();
    }

    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoRunning = false;
            Platform.runLater(() -> {
                liveStatusLabel.setText("СИСТЕМА: АВТО ЗАВЕРШЕНО");
                setControlsDisable(false);
                updateStats();
            });
            return;
        }

        AutoTestParam param = autoQueue.poll();
        Platform.runLater(() -> {
            l0Box.getSelectionModel().select(param.l0 == 20.0 ? 0 : 1);
            iField.setText(String.format(Locale.US, "%.2f", param.I));
            xField.setText(String.format(Locale.US, "%.0f", param.x));
            triggerMeasurement();
        });
    }

    private void updateStats() {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: Очікування вимірювань...");
            return;
        }

        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }

        double maxB = 0;
        for (Measurement31 m : data) {
            if (m.getB() > maxB) maxB = m.getB();
        }

        Measurement31 last = data.get(data.size() - 1);
        double c = calculateCoeffC();

        StringBuilder sb = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ ЕКСПЕРИМЕНТУ:\n");
        sb.append("1. Робоча формула: B = [β(R+Rg) / 2NS] * n = C * n.\n");
        sb.append(String.format(Locale.US, "2. Динамічна стала: C = %.6f мТл/под.\n", c));
        sb.append(String.format(Locale.US, "3. Останній вимір: n = %.0f, B = %.4f мТл.\n", last.getN(), last.getB()));
        sb.append(String.format(Locale.US, "4. Максимальна індукція: B_max = %.4f мТл.\n", maxB));
        sb.append("Висновок: Індукція зменшується віддаляючись від центру і при розширенні зазору.");

        finalResultLabel.setText(sb.toString());
    }

    private void showAnalysisDialog() {
        if (data.isEmpty()) {
            showAlert("Увага", "Спочатку проведіть вимірювання або запустіть автопроходження!");
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Графіки (Лаб 3-1)");

        TabPane tabPane = new TabPane();

        NumberAxis xAxis1 = new NumberAxis();
        xAxis1.setLabel("Координата x (см)");
        NumberAxis yAxis1 = new NumberAxis();
        yAxis1.setLabel("Індукція B (мТл)");
        LineChart<Number, Number> chart1 = new LineChart<>(xAxis1, yAxis1);
        chart1.setTitle("Залежність магнітної індукції від координати: B = f(x)");

        XYChart.Series<Number, Number> series20 = new XYChart.Series<>();
        series20.setName("l0 = 20 мм");
        XYChart.Series<Number, Number> series30 = new XYChart.Series<>();
        series30.setName("l0 = 30 мм");

        NumberAxis xAxis2 = new NumberAxis();
        xAxis2.setLabel("Струм I (А)");
        NumberAxis yAxis2 = new NumberAxis();
        yAxis2.setLabel("Індукція B (мТл)");
        LineChart<Number, Number> chart2 = new LineChart<>(xAxis2, yAxis2);
        chart2.setTitle("Залежність магнітної індукції від струму: B = f(I)");
        XYChart.Series<Number, Number> seriesI = new XYChart.Series<>();
        seriesI.setName("l0 = 20 мм, x = 0");

        for (Measurement31 m : data) {
            if (m.getCurrentI() >= 0.99 && m.getCurrentI() <= 1.01) {
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