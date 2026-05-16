/*
 * Лабораторна робота № 3-8 "Петля гістерезису".
 * Клас: LabController38.
 * Призначення: керує інтерфейсом лабораторної роботи, проведенням дослідів
 * з дослідження кривих намагнечування та петель гістерезису феромагнетиків з допомогою осцилографа.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level3.lab3_8.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level3.lab3_8.model.Measurement38;
import dev.ua._klaidi4_.physics.level3.lab3_8.view.OscilloscopeCanvas;
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
import java.util.List;
import java.util.Locale;

public class LabController38 extends BaseLabController {

    private OscilloscopeCanvas canvas;
    private TableView<Measurement38> table;
    private ObservableList<Measurement38> data;
    private int idCounter = 1;
    private ComboBox<String> materialBox;
    private Slider ampSlider;
    private TextField cxField;
    private TextField cyField;
    private Button recordPeakBtn;
    private Button recordPointBtn;
    private Button autoBtn;
    private Button analyzeBtn;
    private Button clearBtn;

    /*
     * Лабораторна робота № 3-8 "Петля гістерезису".
     * Функція: LabController38.
     * Призначення: Конструктор класу, ініціалізує інтерфейс.
     */
    public LabController38() {
        initUI();
        updatePhysics();
    }

    /*
     * Лабораторна робота № 3-8 "Петля гістерезису".
     * Функція: shutdown.
     * Призначення: Скидає стан системи при закритті вікна.
     */
    @Override
    public void shutdown() {
    }

    /*
     * Лабораторна робота № 3-8 "Петля гістерезису".
     * Функція: initUI.
     * Призначення: Ініціалізує графічний інтерфейс: панель параметрів осцилографа та таблицю результатів.
     */
    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(330);
        leftPanel.setMinWidth(330);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 3-8)");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));

        TitledPane constantsPane = new TitledPane();
        constantsPane.setText("Константи установки");
        constantsPane.setCollapsible(true);
        constantsPane.setExpanded(false);
        VBox constBox = new VBox(10);
        constBox.setPadding(new Insets(5));

        cxField = new TextField("150.0");
        cyField = new TextField("0.35");
        constBox.getChildren().addAll(
                createInputGroup("Масштаб Cx (А/м / под):", cxField),
                createInputGroup("Масштаб Cy (Тл / под):", cyField)
        );
        constantsPane.setContent(constBox);

        TitledPane controlPane = new TitledPane();
        controlPane.setText("Керування осцилографом");
        controlPane.setCollapsible(false);
        VBox controlBox = new VBox(12);
        controlBox.setPadding(new Insets(5));

        materialBox = new ComboBox<>(FXCollections.observableArrayList(
                "М'який (Трансформаторна сталь)",
                "Жорсткий (Вуглецева сталь)"
        ));
        materialBox.getSelectionModel().selectFirst();
        materialBox.setMaxWidth(Double.MAX_VALUE);
        materialBox.setOnAction(e -> updatePhysics());

        ampSlider = new Slider(0, 100, 100);
        ampSlider.setShowTickLabels(true);
        ampSlider.setShowTickMarks(true);
        ampSlider.setMajorTickUnit(25);
        ampSlider.valueProperty().addListener((obs, oldV, newV) -> updatePhysics());

        controlBox.getChildren().addAll(
                new Label("Матеріал осердя:"), materialBox,
                new Label("Напруга потенціометра r (Амплітуда):"), ampSlider
        );
        controlPane.setContent(controlBox);

        recordPeakBtn = new Button("✍ ЗАПИСАТИ ВЕРШИНУ (Завд. 1)");
        recordPeakBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold;");
        recordPeakBtn.setMaxWidth(Double.MAX_VALUE);
        recordPeakBtn.setOnAction(e -> recordPeakMeasurement());

        recordPointBtn = new Button("✍ ЗАПИСАТИ ТОЧКУ ПЕТЛІ (Завд. 2)");
        recordPointBtn.setStyle("-fx-background-color: #388e3c; -fx-text-fill: white; -fx-font-weight: bold;");
        recordPointBtn.setMaxWidth(Double.MAX_VALUE);
        recordPointBtn.setOnAction(e -> recordHoverMeasurement());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> runAuto());

        analyzeBtn = new Button("📊 ПОБУДУВАТИ ГРАФІКИ");
        analyzeBtn.setStyle("-fx-background-color: #8e24aa; -fx-text-fill: white; -fx-font-weight: bold;");
        analyzeBtn.setMaxWidth(Double.MAX_VALUE);
        analyzeBtn.setOnAction(e -> showAnalysisDialog());

        clearBtn = new Button("🗑 ОЧИСТИТИ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear();
            idCounter = 1;
            updateStats();
        });

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, constantsPane, controlPane, recordPeakBtn, recordPointBtn, autoBtn, analyzeBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new OscilloscopeCanvas(500, 400);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        StackPane centerPanel = new StackPane(canvas, topBar);
        centerPanel.setStyle("-fx-background-color: #2c3e50; -fx-border-color: #34495e; -fx-border-width: 5;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        String[] cols = {"№", "Завдання", "nx (под)", "ny (под)", "H (А/м)", "B (Тл)"};
        String[] props = {"id", "task", "nx", "ny", "h", "b"};

        for (int i = 0; i < cols.length; i++) {
            TableColumn<Measurement38, Object> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(new PropertyValueFactory<>(props[i]));

            if (i >= 2) {
                col.setCellFactory(column -> new TableCell<>() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) setText(null);
                        else setText(String.format(Locale.US, "%.2f", ((Number)item).doubleValue()));
                    }
                });
            }
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
    }

    /*
     * Лабораторна робота № 3-8 "Петля гістерезису".
     * Функція: updatePhysics.
     * Призначення: Оновлює візуалізацію петлі гістерезису на основі введених параметрів.
     */
    private void updatePhysics() {
        if (canvas != null) {
            canvas.setParameters(ampSlider.getValue(), materialBox.getValue());
        }
    }

    /*
     * Лабораторна робота № 3-8 "Петля гістерезису".
     * Функція: recordPeakMeasurement.
     * Призначення: Записує координати пікової точки петлі (максимальне намагнечування).
     */
    private void recordPeakMeasurement() {
        double nx = canvas.getPeakNx();
        double ny = canvas.getPeakNy();
        saveMeasurement("Завд. 1 (Основна крива)", nx, ny);
    }

    /*
     * Лабораторна робота № 3-8 "Петля гістерезису".
     * Функція: recordHoverMeasurement.
     * Призначення: Записує координати поточної точки під курсором на екрані осцилографа.
     */
    private void recordHoverMeasurement() {
        double nx = canvas.getMouseNx();
        double ny = canvas.getMouseNy();
        saveMeasurement("Завд. 2 (Петля)", nx, ny);
    }

    /*
     * Лабораторна робота № 3-8 "Петля гістерезису".
     * Функція: saveMeasurement.
     * Призначення: Розраховує напруженість H та індукцію B на основі поділок сітки та додає дані до таблиці.
     */
    private void saveMeasurement(String task, double nx, double ny) {
        try {
            double cx = Double.parseDouble(cxField.getText().replace(',', '.'));
            double cy = Double.parseDouble(cyField.getText().replace(',', '.'));

            double h = nx * cx;
            double b = ny * cy;

            data.add(new Measurement38(idCounter++, task, nx, ny, Math.abs(h), Math.abs(b)));
            updateStats();
        } catch (NumberFormatException e) {
            showAlert("Помилка", "Перевірте константи Сx та Сy.");
        }
    }

    /*
     * Лабораторна робота № 3-8 "Петля гістерезису".
     * Функція: runAuto.
     * Призначення: Виконує автоматичне зняття основної кривої намагнечування для різних напруг.
     */
    private void runAuto() {
        data.clear();
        idCounter = 1;

        new Thread(() -> {
            Platform.runLater(() -> {
                ampSlider.setDisable(true);
                recordPeakBtn.setDisable(true);
                recordPointBtn.setDisable(true);
            });

            try {
                for (double amp = 100; amp >= 10; amp -= 15) {
                    final double currentAmp = amp;
                    Platform.runLater(() -> ampSlider.setValue(currentAmp));
                    Thread.sleep(300);
                    Platform.runLater(this::recordPeakMeasurement);
                }

                Platform.runLater(() -> ampSlider.setValue(100));
                Thread.sleep(500);

                Platform.runLater(() -> {
                    List<double[]> points = canvas.calculateCurvePoints();
                    int step = points.size() / 20;
                    for (int i = 0; i < points.size(); i += step) {
                        double[] p = points.get(i);
                        saveMeasurement("Завд. 2 (Петля)", p[0], p[1]);
                    }
                });

            } catch (InterruptedException ignored) {}

            Platform.runLater(() -> {
                ampSlider.setDisable(false);
                recordPeakBtn.setDisable(false);
                recordPointBtn.setDisable(false);
                updateStats();
            });
        }).start();
    }

    /*
     * Лабораторна робота № 3-8 "Петля гістерезису".
     * Функція: updateStats.
     * Призначення: Розраховує відносну магнітну проникність на основі виміряних значень.
     */
    private void updateStats() {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка: Очікування даних...");
            return;
        }
        if (!showCalculations) {
            finalResultLabel.setText("Обробка: [Приховано]");
            return;
        }

        double maxB = data.stream().mapToDouble(Measurement38::getB).max().orElse(0);
        double maxH = data.stream().mapToDouble(Measurement38::getH).max().orElse(0);

        StringBuilder sb = new StringBuilder("РЕЗУЛЬТАТИ АНАЛІЗУ:\n");
        sb.append(String.format(Locale.US, "1. Максимальна індукція (насичення): Bs ≈ %.2f Тл.\n", maxB));
        sb.append(String.format(Locale.US, "2. Відповідна напруженість поля: Hs ≈ %.0f А/м.\n", maxH));
        if (maxH > 0) {
            double mu = maxB / (4 * Math.PI * 1e-7 * maxH);
            sb.append(String.format(Locale.US, "3. Відносна магн. проникність (μ) в точці насичення: %.0f\n", mu));
        }

        finalResultLabel.setText(sb.toString());
    }

    /*
     * Лабораторна робота № 3-8 "Петля гістерезису".
     * Функція: showAnalysisDialog.
     * Призначення: Виводить вікно з графічним представленням отриманих результатів.
     */
    private void showAnalysisDialog() {
        if (data.isEmpty()) {
            showAlert("Увага", "Немає даних для побудови графіків.");
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Графіки намагнічування (Лаб 3-8)");

        TabPane tabPane = new TabPane();

        NumberAxis xAxis1 = new NumberAxis(); xAxis1.setLabel("Напруженість H (А/м)");
        NumberAxis yAxis1 = new NumberAxis(); yAxis1.setLabel("Індукція B (Тл)");
        LineChart<Number, Number> chart1 = new LineChart<>(xAxis1, yAxis1);
        chart1.setTitle("Основна крива намагнічування B = f(H)");
        XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
        series1.setName(materialBox.getValue());

        NumberAxis xAxis2 = new NumberAxis(); xAxis2.setLabel("Напруженість H (А/м)");
        NumberAxis yAxis2 = new NumberAxis(); yAxis2.setLabel("Індукція B (Тл)");
        LineChart<Number, Number> chart2 = new LineChart<>(xAxis2, yAxis2);
        chart2.setTitle("Петля гістерезису");
        XYChart.Series<Number, Number> series2 = new XYChart.Series<>();
        series2.setName("Петля");

        for (Measurement38 m : data) {
            if (m.getTask().contains("Завд. 1")) {
                series1.getData().add(new XYChart.Data<>(m.getH(), m.getB()));
            } else {
                try {
                    double cx = Double.parseDouble(cxField.getText().replace(',', '.'));
                    double cy = Double.parseDouble(cyField.getText().replace(',', '.'));
                    series2.getData().add(new XYChart.Data<>(m.getNx() * cx, m.getNy() * cy));
                } catch (Exception ignored) {}
            }
        }

        series1.getData().sort(Comparator.comparing(d -> d.getXValue().doubleValue()));

        chart1.getData().add(series1);
        chart2.getData().add(series2);
        chart2.setCreateSymbols(false);

        Tab tab1 = new Tab("Завд 1: Основна крива", chart1); tab1.setClosable(false);
        Tab tab2 = new Tab("Завд 2: Петля", chart2); tab2.setClosable(false);

        tabPane.getTabs().addAll(tab1, tab2);

        Scene scene = new Scene(tabPane, 700, 500);
        stage.setScene(scene);
        stage.show();
    }
}