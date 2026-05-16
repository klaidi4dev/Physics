/*
 * Лабораторна робота № 6-5 "Питомий заряд електрона".
 * Клас: LabController65.
 * Призначення: керує інтерфейсом лабораторної роботи, запуском досліду,
 * введенням параметрів, таблицею результатів та обробкою вимірювань.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level6.lab6_5.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level6.lab6_5.model.Measurement;
import dev.ua._klaidi4_.physics.level6.lab6_5.view.MagnetronCanvas;
import javafx.animation.AnimationTimer;
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

import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.function.Consumer;

public class LabController65 extends BaseLabController {

    private MagnetronCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;

    private TextField fieldL;
    private TextField fieldD;
    private TextField fieldN;
    private TextField fieldRa;

    private TextField uaField;
    private TextField icField;

    private final double MU_0 = 4 * Math.PI * 1e-7;
    private final double E_M_TRUE = 1.75882e11;

    private double currentUa = 6.3;
    private double currentIc = 0.0;
    private double currentIa = 0.0;

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
    private Button clearBtn;

    private boolean isAutoRunning = false;
    private AnimationTimer autoTimer;
    private Queue<Double> autoQueue = new LinkedList<>();

    /*
     * Лабораторна робота № 6-5 "Питомий заряд електрона".
     * Функція: LabController65.
     * Призначення: Конструктор класу, ініціалізує інтерфейс та налаштування.
     */
    public LabController65() {
        initUI();
    }

    /*
     * Лабораторна робота № 6-5 "Питомий заряд електрона".
     * Функція: shutdown.
     * Призначення: Зупиняє процеси та очищує ресурси при закритті вікна.
     */
    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        if (autoTimer != null) autoTimer.stop();
        autoQueue.clear();
    }

    /*
     * Лабораторна робота № 6-5 "Питомий заряд електрона".
     * Функція: initUI.
     * Призначення: Ініціалізує графічний інтерфейс користувача, створює панелі управління та графіки.
     */
    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(320);
        leftPanel.setMinWidth(320);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 6-5)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane setupPane = new TitledPane();
        setupPane.setText("Геометрія установки");
        setupPane.setCollapsible(false);
        VBox setupParams = new VBox(12);
        setupParams.setPadding(new Insets(5));

        fieldL = createNumberField(0.25, val -> updatePhysics());
        fieldD = createNumberField(0.05, val -> updatePhysics());
        fieldN = createNumberField(1000, val -> updatePhysics());
        fieldRa = createNumberField(0.0094, val -> updatePhysics());

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

        uaField = createNumberField(currentUa, val -> {
            currentUa = val;
            updatePhysics();
        });

        icField = createNumberField(currentIc, val -> {
            currentIc = val;
            updatePhysics();
        });

        controlParams.getChildren().addAll(
                createInputGroup("Анодна напруга Ua (В):", uaField),
                createInputGroup("Струм соленоїда Ic (А):", icField)
        );
        controlPane.setContent(controlParams);

        // --- ІНІЦІАЛІЗАЦІЯ ТАБЛИЦІ ---
        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Double> colIc = new TableColumn<>("Ic (А)");
        colIc.setCellValueFactory(new PropertyValueFactory<>("ic"));
        TableColumn<Measurement, Double> colIa = new TableColumn<>("Ia (мА)");
        colIa.setCellValueFactory(new PropertyValueFactory<>("ia"));

        table.getColumns().addAll(colIc, colIa);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150); // Висота таблиці в лівій панелі

        addPointBtn = new Button("✍ ЗАПИСАТИ ТОЧКУ");
        addPointBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        addPointBtn.setMaxWidth(Double.MAX_VALUE);
        addPointBtn.setOnAction(e -> recordPoint(currentIc));

        autoRunBtn = new Button("⚙ АВТО-ВИМІРЮВАННЯ");
        autoRunBtn.setStyle("-fx-background-color: #0288d1; -fx-text-fill: white; -fx-font-weight: bold;");
        autoRunBtn.setMaxWidth(Double.MAX_VALUE);
        autoRunBtn.setOnAction(e -> startAutoMode());

        clearBtn = new Button("🗑 ОЧИСТИТИ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> clearAll());

        // Додаємо таблицю над кнопками
        VBox leftControls = new VBox(10, title, setupPane, controlPane, table, addPointBtn, autoRunBtn, clearBtn);

        ScrollPane scrollLeft = new ScrollPane(leftControls);
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

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Струм соленоїда Ic (А)");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Анодний струм Ia (мА)");

        chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        chart.setPrefHeight(300); // Збільшено висоту графіка

        dataSeries = new XYChart.Series<>();
        dataSeries.setName("Залежність Ia = f(Ic)");
        topTangentSeries = new XYChart.Series<>();
        topTangentSeries.setName("Дотична 1");
        dropTangentSeries = new XYChart.Series<>();
        dropTangentSeries.setName("Дотична 2");
        vLineSeries = new XYChart.Series<>();
        vLineSeries.setName("I_кр");

        chart.getData().addAll(dataSeries, topTangentSeries, dropTangentSeries, vLineSeries);

        VBox centerPanel = new VBox(10, canvasStack, chart);
        VBox.setVgrow(chart, Priority.ALWAYS); // Графік займає весь вільний простір

        VBox statsBox = createStatsBox();
        statsBox.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(statsBox); // Таблиці тут більше немає, тільки статистика

        updatePhysics();
    }

    /*
     * Лабораторна робота № 6-5 "Питомий заряд електрона".
     * Функція: createNumberField.
     * Призначення: Створює і повертає новий елемент інтерфейсу або об'єкт.
     */
    private TextField createNumberField(double initialValue, Consumer<Double> onChange) {
        TextField field = new TextField(String.valueOf(initialValue));
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                field.setText(newValue.replaceAll("[^\\d.]", ""));
            } else if (!newValue.isEmpty() && !newValue.equals(".")) {
                try {
                    double val = Double.parseDouble(newValue);
                    onChange.accept(val);
                } catch (NumberFormatException ignored) {}
            }
        });
        return field;
    }

    /*
     * Лабораторна робота № 6-5 "Питомий заряд електрона".
     * Функція: getTrueIkr.
     * Призначення: Повертає необхідне значення параметра або об'єкта.
     */
    private double getTrueIkr() {
        try {
            double L = Double.parseDouble(fieldL.getText());
            double D = Double.parseDouble(fieldD.getText());
            double N = Double.parseDouble(fieldN.getText());
            double Ra = Double.parseDouble(fieldRa.getText());

            double B_kr = Math.sqrt((8 * currentUa) / (E_M_TRUE * Ra * Ra));
            return B_kr * Math.sqrt(L * L + D * D) / (MU_0 * N);
        } catch (Exception e) {
            return 0.5;
        }
    }

    /*
     * Лабораторна робота № 6-5 "Питомий заряд електрона".
     * Функція: updatePhysics.
     * Призначення: Оновлює дані, статистику або графічний інтерфейс.
     */
    private void updatePhysics() {
        double trueIkr = getTrueIkr();
        double iaMax = 2.0 + (currentUa * 0.4);

        currentIa = iaMax / (1 + Math.exp(50 * (currentIc - trueIkr)));

        liveUaLabel.setText(String.format(Locale.US, "Ua = %.2f В", currentUa));
        liveIcLabel.setText(String.format(Locale.US, "Ic = %.2f А", currentIc));
        liveIaLabel.setText(String.format(Locale.US, "Ia = %.2f мА", currentIa));

        if (canvas != null) {
            double ratio = (trueIkr > 0) ? (currentIc / trueIkr) : 0;
            canvas.updatePhysicsParameters(ratio, currentUa, currentIc);
        }
    }

    /*
     * Лабораторна робота № 6-5 "Питомий заряд електрона".
     * Функція: recordPoint.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void recordPoint(double ic) {
        for (Measurement m : data) {
            if (Math.abs(m.getIc() - ic) < 0.01) return;
        }

        double trueIkr = getTrueIkr();
        double iaMax = 2.0 + (currentUa * 0.4);
        double ia = iaMax / (1 + Math.exp(50 * (ic - trueIkr)));

        ia += (Math.random() - 0.5) * (iaMax * 0.05);
        if (ia < 0.02) ia = 0.02;

        Measurement m = new Measurement(idCounter++, Math.round(ic * 100) / 100.0, Math.round(ia * 1000) / 1000.0);
        data.add(m);
        dataSeries.getData().add(new XYChart.Data<>(m.getIc(), m.getIa()));

        // Автоматично прокручуємо таблицю вниз при додаванні нових даних
        table.scrollTo(data.size() - 1);

        performAnalysis();
    }

    /*
     * Лабораторна робота № 6-5 "Питомий заряд електрона".
     * Функція: startAutoMode.
     * Призначення: Запускає процес симуляції або відповідний режим роботи.
     */
    private void startAutoMode() {
        clearAll();
        isAutoRunning = true;
        liveStatusLabel.setText("Статус: АВТОВИМІРЮВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: red;");
        setControlsDisable(true);

        double targetMaxIc = getTrueIkr() * 1.6;
        double step = targetMaxIc / 20.0;

        autoQueue.clear();
        for (double ic = 0.0; ic <= targetMaxIc + step; ic += step) {
            autoQueue.add(ic);
        }

        autoTimer = new AnimationTimer() {
            private long lastTime = 0;

    /*
     * Лабораторна робота № 6-5 "Питомий заряд електрона".
     * Функція: handle.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                if ((now - lastTime) / 1_000_000_000.0 < 0.2) return;
                lastTime = now;

                if (autoQueue.isEmpty()) {
                    this.stop();
                    isAutoRunning = false;
                    liveStatusLabel.setText("Статус: ГОТОВО");
                    liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
                    setControlsDisable(false);
                    performAnalysis();
                    return;
                }

                double nextIc = autoQueue.poll();
                icField.setText(String.format(Locale.US, "%.3f", nextIc));
                recordPoint(nextIc);
            }
        };
        autoTimer.start();
    }

    /*
     * Лабораторна робота № 6-5 "Питомий заряд електрона".
     * Функція: setControlsDisable.
     * Призначення: Встановлює нове значення для вказаного параметра.
     */
    private void setControlsDisable(boolean disable) {
        icField.setDisable(disable);
        uaField.setDisable(disable);
        addPointBtn.setDisable(disable);
        autoRunBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        fieldL.setDisable(disable);
        fieldD.setDisable(disable);
        fieldN.setDisable(disable);
        fieldRa.setDisable(disable);
    }

    /*
     * Лабораторна робота № 6-5 "Питомий заряд електрона".
     * Функція: performAnalysis.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void performAnalysis() {
        if (isAutoRunning || data.size() < 6) return;

        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
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
            topTangentSeries.getData().add(new XYChart.Data<>(getTrueIkr() * 2, yMax));

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
            double dropStartX = x0 - (yMax * 0.4) / Math.abs(minSlope);
            double dropEndX = x0 + (yMax * 0.6) / Math.abs(minSlope);
            dropTangentSeries.getData().add(new XYChart.Data<>(dropStartX, y0 + minSlope * (dropStartX - x0)));
            dropTangentSeries.getData().add(new XYChart.Data<>(dropEndX, y0 + minSlope * (dropEndX - x0)));

            double i_kr_exp = x0 + (yMax - y0) / minSlope;
            vLineSeries.getData().clear();
            vLineSeries.getData().add(new XYChart.Data<>(i_kr_exp, yMax));
            vLineSeries.getData().add(new XYChart.Data<>(i_kr_exp, 0.0));

            double B_kr = (MU_0 * N_val * i_kr_exp) / Math.sqrt(L_val * L_val + D_val * D_val);
            double em = (8 * currentUa) / (B_kr * B_kr * Ra_val * Ra_val);

            double error = Math.abs(em - E_M_TRUE) / E_M_TRUE * 100.0;

            String conclusion = String.format(Locale.US,
                    "АВТОМАТИЧНА ОБРОБКА РЕЗУЛЬТАТІВ:\n" +
                            "1. Метод дотичних визначає критичний струм: Iкр = %.3f А.\n" +
                            "2. Індукція магнітного поля соленоїда: Bкр = %.4e Тл.\n" +
                            "3. Обчислено питомий заряд електрона: e/m = %.2e Кл/кг.\n" +
                            "4. Відносна похибка (від табличного 1.76e11): ε = %.1f %%.\n" +
                            "ВИСНОВОК: Побудовано скидну характеристику. Результати підтверджують теорію руху в схрещених полях.",
                    i_kr_exp, B_kr, em, error);

            finalResultLabel.setText(conclusion);
        } catch (Exception ignored) {}
    }

    /*
     * Лабораторна робота № 6-5 "Питомий заряд електрона".
     * Функція: clearAll.
     * Призначення: Очищує зібрані дані та скидає стан.
     */
    private void clearAll() {
        data.clear();
        dataSeries.getData().clear();
        topTangentSeries.getData().clear();
        dropTangentSeries.getData().clear();
        vLineSeries.getData().clear();
        idCounter = 1;
        finalResultLabel.setText("Обробка результатів: Дані очищено.");
        icField.setText("0.0");
    }
}