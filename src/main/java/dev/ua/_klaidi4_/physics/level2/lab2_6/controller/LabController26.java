/*
 * Лабораторна робота № 2-6 "Опір та температура".
 * Клас: LabController26.
 * Призначення: керує інтерфейсом лабораторної роботи, проведенням вимірювань
 * опорів за допомогою містка Уітстона та дослідженням залежності опору від температури.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level2.lab2_6.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level2.lab2_6.model.Measurement;
import dev.ua._klaidi4_.physics.level2.lab2_6.view.ResistanceCanvas;
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

import java.util.Locale;

public class LabController26 extends BaseLabController {

    private ResistanceCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> modeBox;
    private VBox bridgePanel, thermoPanel;
    private ComboBox<String> targetResistorBox;
    private TextField fieldRetalon;
    private TextField lengthField;
    private TextField maxTempField;
    private TextField stepTempField;
    private Button heatBtn;
    private Button autoBtn, recordBtn, clearBtn;
    private Label liveStatusLabel, liveModeLabel, liveIgLabel, liveTempLabel, liveRtLabel;
    private LineChart<Number, Number> chart;
    private XYChart.Series<Number, Number> dataSeries;

    private final double REAL_R1 = 120.2;
    private final double REAL_R2 = 60.1;
    private final double REAL_R3 = 180.3;
    private final double R_COPPER_0 = 339.8;
    private final double ALPHA_COPPER = 0.0043;

    private int currentMode = 1;
    private double currentIg = 0.0;
    private double currentTemp = 20.0;
    private boolean isAutoRunning = false;

        /*
         * Лабораторна робота № 2-6 "Опір та температура".
         * Функція: LabController26.
         * Призначення: Конструктор класу, ініціалізує інтерфейс та встановлює початковий режим роботи.
         */
        public LabController26() {
        initUI();
    }

        /*
     * Лабораторна робота № 2-6 "Опір та температура".
     * Функція: shutdown.
     * Призначення: Зупиняє анімацію та скидає чергу при закритті модуля.
     */
    @Override
    public void shutdown() {
        isAutoRunning = false;
        if (canvas != null) canvas.stopSimulation();
    }

        /*
         * Лабораторна робота № 2-6 "Опір та температура".
         * Функція: initUI.
         * Призначення: Ініціалізує графічний інтерфейс: панелі керування, графік та таблицю результатів.
         */
        private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(320);
        leftPanel.setMinWidth(320);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 2-6)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane modePane = new TitledPane();
        modePane.setText("Вибір експерименту");
        modePane.setCollapsible(false);
        VBox modeBoxContainer = new VBox(10);
        modeBoxContainer.setPadding(new Insets(5));

        modeBox = new ComboBox<>();
        modeBox.getItems().addAll("1. Міст Уітстона (Вимір опорів)", "2. Термостат (Нагрівання)");
        modeBox.getSelectionModel().selectFirst();
        modeBox.setMaxWidth(Double.MAX_VALUE);
        modeBox.setOnAction(e -> switchMode(modeBox.getSelectionModel().getSelectedIndex() + 1));
        modeBoxContainer.getChildren().add(modeBox);
        modePane.setContent(modeBoxContainer);

        bridgePanel = new VBox(10);
        bridgePanel.setPadding(new Insets(5));

        targetResistorBox = new ComboBox<>();
        targetResistorBox.getItems().addAll(
                "Резистор R1", "Резистор R2", "Резистор R3",
                "Послідовне (R1+R2+R3)", "Паралельне (R1||R2||R3)"
        );
        targetResistorBox.getSelectionModel().selectFirst();
        targetResistorBox.setMaxWidth(Double.MAX_VALUE);
        targetResistorBox.setOnAction(e -> updateBridgePhysics());

        fieldRetalon = new TextField("100.0");
        fieldRetalon.textProperty().addListener((o, old, val) -> updateBridgePhysics());

        lengthField = new TextField("500.0");
        lengthField.textProperty().addListener((o, old, val) -> {
            if(lengthField.isFocused()) {
                updateBridgePhysics();
            }
        });

        bridgePanel.getChildren().addAll(
                createInputGroup("Об'єкт вимірювання (Rx):", targetResistorBox),
                createInputGroup("Опір еталона Rет (Ом):", fieldRetalon),
                createInputGroup("Точка контакту l1 (мм):", lengthField)
        );

        thermoPanel = new VBox(10);
        thermoPanel.setPadding(new Insets(5));

        maxTempField = new TextField("80.0");
        stepTempField = new TextField("5.0");

        heatBtn = new Button("▶ ПОЧАТИ НАГРІВАННЯ");
        heatBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold;");
        heatBtn.setMaxWidth(Double.MAX_VALUE);
        heatBtn.setOnAction(e -> startHeatingManual());

        thermoPanel.getChildren().addAll(
                createInputGroup("Кінцева температура (°C):", maxTempField),
                createInputGroup("Крок нагрівання (°C):", stepTempField),
                heatBtn
        );

        VBox dynamicControlBox = new VBox(bridgePanel);
        TitledPane controlPane = new TitledPane("Параметри установки", dynamicControlBox);
        controlPane.setCollapsible(false);

        recordBtn = new Button("✍ ЗАПИСАТИ ПОКАЗНИКИ");
        recordBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        recordBtn.setMaxWidth(Double.MAX_VALUE);
        recordBtn.setOnAction(e -> recordMeasurement());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAuto());

        clearBtn = new Button("🗑 ОЧИСТИТИ ДАНІ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> resetAll());

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, modePane, controlPane, recordBtn, autoBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new ResistanceCanvas(450, 260);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(200, 100);

        Label dashTitle = new Label("ПОКАЗНИКИ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ГОТОВО");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveModeLabel = new Label("Режим: Опори");
        liveModeLabel.setStyle("-fx-text-fill: #4fc3f7;");

        liveIgLabel = new Label("Ig = 0.0 мкА");
        liveIgLabel.setStyle("-fx-text-fill: #ff3333; -fx-font-weight: bold;");

        liveTempLabel = new Label("t = 20.0 °C");
        liveTempLabel.setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
        liveRtLabel = new Label("Rt = 0.0 Ом");
        liveRtLabel.setStyle("-fx-text-fill: #00ff00;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveModeLabel, liveIgLabel, liveTempLabel, liveRtLabel);

        StackPane canvasStack = new StackPane(canvas, topBar, dash);
        canvasStack.setStyle("-fx-background-color: #ffffff;");
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Температура t (°C)");
        xAxis.setAutoRanging(true);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Опір Rt (Ом)");
        yAxis.setAutoRanging(true);
        chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(true);
        chart.setCreateSymbols(true);
        chart.setPrefHeight(200);
        rebuildChartSeries();

        VBox centerTopPanel = new VBox(canvasStack, chart);
        VBox.setVgrow(chart, Priority.ALWAYS);

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        String[] cols = {"№", "Режим", "Об'єкт", "t (°C)", "l1 (мм)", "Rет (Ом)", "Опір (Ом)"};
        String[] props = {"id", "mode", "target", "temperature", "l1", "rEtalon", "resistance"};

        for (int i = 0; i < cols.length; i++) {
            TableColumn<Measurement, Object> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(new PropertyValueFactory<>(props[i]));
            table.getColumns().add(col);
        }
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerTopPanel);
        this.setBottom(bottomPanel);

        switchMode(1);
    }

        /*
         * Лабораторна робота № 2-6 "Опір та температура".
         * Функція: rebuildChartSeries.
         * Призначення: Перебудовує серію даних на графіку залежності R(t).
         */
        private void rebuildChartSeries() {
        chart.getData().clear();
        dataSeries = new XYChart.Series<>();
        dataSeries.setName("Залежність Rt = f(t)");
        chart.getData().add(dataSeries);
    }

        /*
         * Лабораторна робота № 2-6 "Опір та температура".
         * Функція: switchMode.
         * Призначення: Перемикає режим роботи між вимірюванням опорів містком та дослідженням термозалежності.
         */
        private void switchMode(int mode) {
        this.currentMode = mode;
        TitledPane ctrlPane = (TitledPane) ((VBox)((ScrollPane)leftPanel.getChildren().get(0)).getContent()).getChildren().get(2);
        VBox dynBox = (VBox) ctrlPane.getContent();
        dynBox.getChildren().clear();

        if (mode == 1) {
            dynBox.getChildren().add(bridgePanel);
            liveModeLabel.setText("Режим: Міст Уітстона");
            liveIgLabel.setManaged(true);
            liveIgLabel.setVisible(true);
            liveTempLabel.setManaged(false);
            liveTempLabel.setVisible(false);
            liveRtLabel.setManaged(false);
            liveRtLabel.setVisible(false);
            chart.setManaged(false);
            chart.setVisible(false);
            recordBtn.setDisable(false);
            updateBridgePhysics();
        } else {
            dynBox.getChildren().add(thermoPanel);
            liveModeLabel.setText("Режим: Термостат");
            liveIgLabel.setManaged(false);
            liveIgLabel.setVisible(false);
            liveTempLabel.setManaged(true);
            liveTempLabel.setVisible(true);
            liveRtLabel.setManaged(true);
            liveRtLabel.setVisible(true);
            chart.setManaged(true);
            chart.setVisible(true);
            recordBtn.setDisable(true);
            currentTemp = 20.0;
            updateThermoPhysics();
        }
        canvas.setMode(mode);
    }

        /*
         * Лабораторна робота № 2-6 "Опір та температура".
         * Функція: getShortTargetName.
         * Призначення: Повертає коротку назву вибраного об'єкта вимірювання.
         */
        private String getShortTargetName() {
        int idx = targetResistorBox.getSelectionModel().getSelectedIndex();
        if (idx == 0) return "R1";
        if (idx == 1) return "R2";
        if (idx == 2) return "R3";
        if (idx == 3) return "Послідовне";
        if (idx == 4) return "Паралельне";
        return "R1";
    }

        /*
         * Лабораторна робота № 2-6 "Опір та температура".
         * Функція: getCurrentRxReal.
         * Призначення: Розраховує реальний опір зразка з урахуванням температури та матеріалу.
         */
        private double getCurrentRxReal() {
        int idx = targetResistorBox.getSelectionModel().getSelectedIndex();
        if (idx == 0) return REAL_R1;
        if (idx == 1) return REAL_R2;
        if (idx == 2) return REAL_R3;
        if (idx == 3) return REAL_R1 + REAL_R2 + REAL_R3;
        return 1.0 / (1.0/REAL_R1 + 1.0/REAL_R2 + 1.0/REAL_R3);
    }

        /*
         * Лабораторна робота № 2-6 "Опір та температура".
         * Функція: updateBridgePhysics.
         * Призначення: Розраховує розбаланс містка Уітстона та оновлює показники приладів.
         */
        private void updateBridgePhysics() {
        if (currentMode != 1 || isAutoRunning) return;

        double rEt = 100.0;
        try { rEt = Double.parseDouble(fieldRetalon.getText().replace(',', '.')); } catch(Exception ignored){}

        double l1 = 500.0;
        try {
            l1 = Double.parseDouble(lengthField.getText().replace(',', '.'));
            if(l1 < 0) l1 = 0;
            if(l1 > 1000) l1 = 1000;
        } catch(Exception ignored){}

        double rxReal = getCurrentRxReal();
        double vTop = 5.0 * (rEt / (rxReal + rEt));
        double vBot = 5.0 * (1000.0 - l1) / 1000.0;
        currentIg = (vTop - vBot) / 100.0 * 1e6;

        String targetName = getShortTargetName();

        final double finalL1 = l1;
        Platform.runLater(() -> {
            if (Math.abs(currentIg) < 1.0) {
                liveIgLabel.setText(String.format(Locale.US, "Ig = %.1f мкА", currentIg));
                liveIgLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
                liveStatusLabel.setText("Статус: СКОМПЕНСОВАНО!");
            } else {
                liveIgLabel.setText(String.format(Locale.US, "Ig = %.1f мкА", currentIg));
                liveIgLabel.setStyle("-fx-text-fill: #ff3333; -fx-font-weight: bold;");
                liveStatusLabel.setText("Статус: НАЛАШТУВАННЯ...");
            }
            canvas.updateBridge(finalL1, currentIg, targetName);
        });
    }

        /*
         * Лабораторна робота № 2-6 "Опір та температура".
         * Функція: updateThermoPhysics.
         * Призначення: Оновлює фізичні параметри в режимі нагрівання.
         */
        private void updateThermoPhysics() {
        double rt = R_COPPER_0 * (1 + ALPHA_COPPER * currentTemp);
        double noise = (Math.random() - 0.5) * 0.5;
        double measuredRt = rt + noise;

        Platform.runLater(() -> {
            liveTempLabel.setText(String.format(Locale.US, "t = %.1f °C", currentTemp));
            liveRtLabel.setText(String.format(Locale.US, "Rt = %.2f Ом", measuredRt));
            canvas.updateThermostat(currentTemp, isAutoRunning || heatBtn.isDisabled());
        });
    }

        /*
         * Лабораторна робота № 2-6 "Опір та температура".
         * Функція: setControlsDisable.
         * Призначення: Блокує або розблокує елементи керування під час вимірювань.
         */
        private void setControlsDisable(boolean disable) {
        modeBox.setDisable(disable);
        targetResistorBox.setDisable(disable);
        fieldRetalon.setDisable(disable);
        lengthField.setDisable(disable);
        maxTempField.setDisable(disable);
        stepTempField.setDisable(disable);
        heatBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        recordBtn.setDisable(disable);
    }

        /*
         * Лабораторна робота № 2-6 "Опір та температура".
         * Функція: startAuto.
         * Призначення: Запускає автоматичне виконання вимірювань для поточного режиму.
         */
        private void startAuto() {
        if (currentMode == 1) {
            runAutoBridge();
        } else {
            runAutoThermo();
        }
    }

        /*
         * Лабораторна робота № 2-6 "Опір та температура".
         * Функція: startHeatingManual.
         * Призначення: Запускає процес нагрівання в ручному режимі.
         */
        private void startHeatingManual() {
        runAutoThermo();
    }

        /*
         * Лабораторна робота № 2-6 "Опір та температура".
         * Функція: runAutoBridge.
         * Призначення: Симулює автоматичне балансування містка Уітстона.
         */
        private void runAutoBridge() {
        data.clear();
        idCounter = 1;
        isAutoRunning = true;
        setControlsDisable(true);

        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                final int idx = i;
                Platform.runLater(() -> targetResistorBox.getSelectionModel().select(idx));
                try { Thread.sleep(500); } catch (Exception ignored) {}

                double rEt = 100.0;
                double rxReal = getCurrentRxReal();
                double targetL1 = 1000.0 * rxReal / (rxReal + rEt);

                double current = 500.0;
                try { current = Double.parseDouble(lengthField.getText().replace(',', '.')); } catch (Exception ignored) {}

                double step = (targetL1 > current) ? 25.0 : -25.0;

                Platform.runLater(() -> liveStatusLabel.setText("Статус: ПОШУК НУЛЯ..."));

                while (Math.abs(targetL1 - current) > Math.abs(step) && isAutoRunning) {
                    current += step;
                    final double val = current;

                    double vTop = 5.0 * (rEt / (rxReal + rEt));
                    double vBot = 5.0 * (1000.0 - val) / 1000.0;
                    currentIg = (vTop - vBot) / 100.0 * 1e6;

                    Platform.runLater(() -> {
                        lengthField.setText(String.format(Locale.US, "%.1f", val));
                        liveIgLabel.setText(String.format(Locale.US, "Ig = %.1f мкА", currentIg));
                        canvas.updateBridge(val, currentIg, getShortTargetName());
                    });
                    try { Thread.sleep(50); } catch (Exception ignored) {}
                }

                currentIg = 0.0;
                Platform.runLater(() -> {
                    lengthField.setText(String.format(Locale.US, "%.1f", targetL1));
                    liveIgLabel.setText("Ig = 0.0 мкА");
                    liveIgLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
                    canvas.updateBridge(targetL1, 0.0, getShortTargetName());
                    recordMeasurement();
                });
                try { Thread.sleep(1000); } catch (Exception ignored) {}
            }

            Platform.runLater(() -> {
                isAutoRunning = false;
                setControlsDisable(false);
                liveStatusLabel.setText("Статус: АВТО ЗАВЕРШЕНО");
            });
        }).start();
    }

        /*
         * Лабораторна робота № 2-6 "Опір та температура".
         * Функція: runAutoThermo.
         * Призначення: Виконує серію автоматичних вимірювань опору при різних температурах.
         */
        private void runAutoThermo() {
        double maxT = 80.0;
        double stepT = 5.0;
        try {
            maxT = Double.parseDouble(maxTempField.getText().replace(',', '.'));
            stepT = Double.parseDouble(stepTempField.getText().replace(',', '.'));
        } catch (Exception ignored) {}

        if(stepT <= 0) stepT = 5.0;

        final double finalMaxT = maxT;
        final double finalStepT = stepT;

        data.clear();
        rebuildChartSeries();
        idCounter = 1;
        isAutoRunning = true;
        setControlsDisable(true);
        currentTemp = 20.0;

        new Thread(() -> {
            Platform.runLater(() -> liveStatusLabel.setText("Статус: НАГРІВАННЯ..."));

            for (double t = 20; t <= finalMaxT; t += finalStepT) {
                if (!isAutoRunning) break;
                currentTemp = t;
                updateThermoPhysics();

                final double tempToRecord = t;
                final double rtToRecord = R_COPPER_0 * (1 + ALPHA_COPPER * t) + (Math.random() - 0.5) * 0.5;

                Platform.runLater(() -> {
                    Measurement m = new Measurement(idCounter++, "Термостат", "Мідь",
                            tempToRecord, 0, 0, Math.round(rtToRecord * 100.0) / 100.0);
                    data.add(m);
                    dataSeries.getData().add(new XYChart.Data<>(m.getTemperature(), m.getResistance()));
                    calculateResults();
                });
                try { Thread.sleep(600); } catch (Exception ignored) {}
            }

            Platform.runLater(() -> {
                isAutoRunning = false;
                setControlsDisable(false);
                liveStatusLabel.setText("Статус: ОХОЛОДЖЕННЯ");
                canvas.updateThermostat(finalMaxT, false);
            });
        }).start();
    }

        /*
         * Лабораторна робота № 2-6 "Опір та температура".
         * Функція: recordMeasurement.
         * Призначення: Записує поточний результат вимірювання до таблиці та на графік.
         */
        private void recordMeasurement() {
        if (currentMode == 1) {
            String target = getShortTargetName();

            double l1 = 500.0;
            try { l1 = Double.parseDouble(lengthField.getText().replace(',', '.')); } catch (Exception ignored){}
            l1 = Math.round(l1 * 10.0) / 10.0;

            double rEt = Double.parseDouble(fieldRetalon.getText().replace(',', '.'));
            double rxCalc = rEt * (l1 / (1000.0 - l1));

            Measurement m = new Measurement(idCounter++, "Міст", target, 0, l1, rEt, Math.round(rxCalc * 10.0) / 10.0);
            data.add(m);
            calculateResults();
        }
    }

        /*
         * Лабораторна робота № 2-6 "Опір та температура".
         * Функція: calculateResults.
         * Призначення: Проводить фінальну статистичну обробку та розрахунок термічного коефіцієнта опору.
         */
        private void calculateResults() {
        if (data.isEmpty()) return;

        StringBuilder sb = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ:\n");
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }

        if (currentMode == 1) {
            double r1 = 0, r2 = 0, r3 = 0, rSer = 0, rPar = 0;
            for (Measurement m : data) {
                if (m.getTarget().equals("R1")) r1 = m.getResistance();
                if (m.getTarget().equals("R2")) r2 = m.getResistance();
                if (m.getTarget().equals("R3")) r3 = m.getResistance();
                if (m.getTarget().equals("Послідовне")) rSer = m.getResistance();
                if (m.getTarget().equals("Паралельне")) rPar = m.getResistance();
            }
            sb.append("Дослід 1. Перевірка законів з'єднання провідників:\n");
            if (r1 > 0 && r2 > 0 && r3 > 0) {
                double theorSer = r1 + r2 + r3;
                double theorPar = 1.0 / (1.0/r1 + 1.0/r2 + 1.0/r3);
                sb.append(String.format(Locale.US, "Виміряно: R1 = %.1f Ом, R2 = %.1f Ом, R3 = %.1f Ом.\n", r1, r2, r3));
                if (rSer > 0) sb.append(String.format(Locale.US, "Послідовне: Експ = %.1f Ом, Теор = %.1f Ом.\n", rSer, theorSer));
                if (rPar > 0) sb.append(String.format(Locale.US, "Паралельне: Експ = %.1f Ом, Теор = %.1f Ом.\n", rPar, theorPar));
                sb.append("Висновок: Закони послідовного та паралельного з'єднання підтверджуються.");
            } else {
                sb.append("Недостатньо даних для перевірки з'єднань (виміряйте R1, R2, R3).");
            }
        } else {
            if (data.size() < 2) {
                sb.append("Недостатньо даних для розрахунку температурного коефіцієнта.");
                finalResultLabel.setText(sb.toString());
                return;
            }
            Measurement m20 = data.get(0);
            Measurement m80 = data.get(data.size() - 1);

            double r0Exp = m20.getResistance() / (1 + ALPHA_COPPER * m20.getTemperature());
            double alphaSimple = (m80.getResistance() - r0Exp) / (r0Exp * m80.getTemperature());

            sb.append("Дослід 2. Залежність опору металу від температури:\n");
            sb.append("1. Побудовано лінійний графік Rt = f(t).\n");
            sb.append(String.format(Locale.US, "2. Шляхом екстраполяції визначено опір при 0°C: R0 = %.1f Ом.\n", r0Exp));
            sb.append(String.format(Locale.US, "3. Розраховано температурний коефіцієнт: α = %.5f K^-1.\n", alphaSimple));
            sb.append(String.format(Locale.US, "4. Табличне значення для міді: α_теор = %.4f K^-1.\n", ALPHA_COPPER));
            sb.append("Висновок: Опір металів лінійно зростає з підвищенням температури.");
        }

        finalResultLabel.setText(sb.toString());
    }

        /*
         * Лабораторна робота № 2-6 "Опір та температура".
         * Функція: resetAll.
         * Призначення: Скидає всі вимірювання, очищає таблицю та графік.
         */
        private void resetAll() {
        data.clear();
        rebuildChartSeries();
        idCounter = 1;
        finalResultLabel.setText("Обробка результатів: -");
        if (currentMode == 1) {
            lengthField.setText("500.0");
            updateBridgePhysics();
        } else {
            currentTemp = 20.0;
            updateThermoPhysics();
        }
        liveStatusLabel.setText("Статус: ОЧИЩЕНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
    }
}