/*
 * Лабораторна робота № 3-7 "Магнітна проникність".
 * Клас: LabController37.
 * Призначення: керує інтерфейсом лабораторної роботи, проведенням дослідів
 * з визначення відносної магнетної проникності магнетиків з допомогою містка Максвелла.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level3.lab3_7.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level3.lab3_7.model.Measurement;
import dev.ua._klaidi4_.physics.level3.lab3_7.view.MaxwellBridgeCanvas;
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

public class LabController37 extends BaseLabController {

    private MaxwellBridgeCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;

    private int idCounter = 1;

    private ComboBox<String> toroidBox;
    private TextField voltageField;
    private TextField l1Field;
    private TextField l2Field;
    private TextField rField;
    private TextField refLField;

    private Button measureBtn;
    private Button autoBtn;
    private Button clearBtn;

    private Label liveStatusLabel;
    private Label liveLxLabel;
    private Label liveMuLabel;

    private boolean isAutoRunning = false;
    private Queue<ToroidPreset> autoQueue = new LinkedList<>();

    private static final double MU_0 = 4 * Math.PI * 1e-7;
    private static final double FREQ = 50.0;
    private static final double OMEGA = 2 * Math.PI * FREQ;

    private static class ToroidPreset {
        String name;
        double voltage;
        int n;
        double l1;
        double l2;
        double d1;
        double d2;
        double h;
        double rx;

        ToroidPreset(String name, double voltage, int n, double l1, double l2,
                     double d1, double d2, double h, double rx) {
            this.name = name;
            this.voltage = voltage;
            this.n = n;
            this.l1 = l1;
            this.l2 = l2;
            this.d1 = d1;
            this.d2 = d2;
            this.h = h;
            this.rx = rx;
        }
    }

    /*
     * Лабораторна робота № 3-7 "Магнітна проникність".
     * Функція: LabController37.
     * Призначення: Конструктор класу, ініціалізує інтерфейс.
     */
    public LabController37() {
        initUI();
        updateFieldsFromToroid();
    }

    /*
     * Лабораторна робота № 3-7 "Магнітна проникність".
     * Функція: shutdown.
     * Призначення: Зупиняє анімацію при закритті модуля.
     */
    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        isAutoRunning = false;
        autoQueue.clear();
    }

    /*
     * Лабораторна робота № 3-7 "Магнітна проникність".
     * Функція: initUI.
     * Призначення: Створює графічний інтерфейс: панель параметрів тороїдів, таблицю та візуалізацію містка.
     */
    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(320);
        leftPanel.setMinWidth(320);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Управління установкою (Лаб 3-7)");
        title.setFont(Font.font("System", FontWeight.BOLD, 17));

        TitledPane paramsPane = new TitledPane();
        paramsPane.setText("Параметри містка Максвелла");
        paramsPane.setCollapsible(false);

        VBox paramsBox = new VBox(10);
        paramsBox.setPadding(new Insets(5));

        toroidBox = new ComboBox<>(FXCollections.observableArrayList(
                "Тороїд 1",
                "Тороїд 2",
                "Тороїд 3"
        ));
        toroidBox.getSelectionModel().selectFirst();
        toroidBox.setMaxWidth(Double.MAX_VALUE);
        toroidBox.setOnAction(e -> updateFieldsFromToroid());

        voltageField = new TextField("1.0");
        l1Field = new TextField("61.5");
        l2Field = new TextField("24.0");
        rField = new TextField("0.51");
        refLField = new TextField("2.1");

        paramsBox.getChildren().addAll(
                createInputGroup("Досліджуваний тороїд:", toroidBox),
                createInputGroup("Напруга U (В):", voltageField),
                createInputGroup("Плече реохорда l1 (под.):", l1Field),
                createInputGroup("Плече реохорда l2 (под.):", l2Field),
                createInputGroup("Опір еталонної котушки R (Ом):", rField),
                createInputGroup("Індуктивність еталонної L (мГн):", refLField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(300);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        paramsPane.setContent(scrollParams);

        measureBtn = new Button("▶ ЗБАЛАНСУВАТИ МІСТ");
        measureBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        measureBtn.setMaxWidth(Double.MAX_VALUE);
        measureBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAuto());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> clearTable());

        leftPanel.getChildren().addAll(title, paramsPane, measureBtn, autoBtn, clearBtn);

        canvas = new MaxwellBridgeCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0,0,0,0.82); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(220, 85);

        Label dashTitle = new Label("ПОКАЗНИКИ МІСТКА");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");

        liveLxLabel = new Label("Lx = 0.000 мГн");
        liveLxLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 15));
        liveLxLabel.setStyle("-fx-text-fill: #00ff00;");

        liveMuLabel = new Label("μ = 0.00");
        liveMuLabel.setStyle("-fx-text-fill: cyan; -fx-font-weight: bold;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveLxLabel, liveMuLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        table.getColumns().addAll(
                createColInt("№", "id"),
                createColString("Тороїд", "toroidName"),
                createCol("U (В)", "voltage"),
                createColInt("N", "n"),
                createCol("l1", "l1"),
                createCol("l2", "l2"),
                createCol("d1", "d1"),
                createCol("d2", "d2"),
                createCol("h", "h"),
                createCol("Rx", "rx"),
                createCol("Lx", "lx"),
                createCol("μ", "mu")
        );

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        updateStats();
    }

    /*
     * Лабораторна робота № 3-7 "Магнітна проникність".
     * Функція: createCol.
     * Призначення: Допоміжна функція для створення числових колонок таблиці.
     */
    private TableColumn<Measurement, Double> createCol(String title, String property) {
        TableColumn<Measurement, Double> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));

        col.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format(Locale.US, "%.2f", item));
                }
            }
        });

        return col;
    }

    /*
     * Лабораторна робота № 3-7 "Магнітна проникність".
     * Функція: createColInt.
     * Призначення: Створює колонку для цілих чисел.
     */
    private TableColumn<Measurement, Integer> createColInt(String title, String property) {
        TableColumn<Measurement, Integer> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }

    /*
     * Лабораторна робота № 3-7 "Магнітна проникність".
     * Функція: createColString.
     * Призначення: Створює текстову колонку таблиці.
     */
    private TableColumn<Measurement, String> createColString(String title, String property) {
        TableColumn<Measurement, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }

    /*
     * Лабораторна робота № 3-7 "Магнітна проникність".
     * Функція: getPreset.
     * Призначення: Повертає параметри тороїда (геометрія, кількість витків) за його індексом.
     */
    private ToroidPreset getPreset(int index, double voltage) {
        if (index == 1) {
            return new ToroidPreset("Тороїд 2", voltage, 40, 64.0, 21.5, 31.3, 19.5, 6.0, 0.252);
        }

        if (index == 2) {
            return new ToroidPreset("Тороїд 3", voltage, 15, 50.5, 35.0, 31.1, 15.3, 8.0, 0.174);
        }

        return new ToroidPreset("Тороїд 1", voltage, 30, 61.5, 24.0, 54.3, 31.5, 9.0, 0.272);
    }

    /*
     * Лабораторна робота № 3-7 "Магнітна проникність".
     * Функція: updateFieldsFromToroid.
     * Призначення: Оновлює поля введення параметрів при виборі іншого тороїда.
     */
    private void updateFieldsFromToroid() {
        try {
            double voltage = Double.parseDouble(voltageField.getText().replace(',', '.'));
            ToroidPreset preset = getPreset(toroidBox.getSelectionModel().getSelectedIndex(), voltage);

            l1Field.setText(String.format(Locale.US, "%.1f", preset.l1));
            l2Field.setText(String.format(Locale.US, "%.1f", preset.l2));
        } catch (Exception ignored) {}
    }

    /*
     * Лабораторна робота № 3-7 "Магнітна проникність".
     * Функція: startManual.
     * Призначення: Запускає процес балансування містка вручну для вибраного тороїда.
     */
    private void startManual() {
        try {
            double voltage = Double.parseDouble(voltageField.getText().replace(',', '.'));
            ToroidPreset preset = getPreset(toroidBox.getSelectionModel().getSelectedIndex(), voltage);
            isAutoRunning = false;
            runMeasurement(preset);
        } catch (Exception e) {
            showAlert("Помилка", "Перевірте правильність введених чисел.");
        }
    }

    /*
     * Лабораторна робота № 3-7 "Магнітна проникність".
     * Функція: startAuto.
     * Призначення: Ініціює автоматичне вимірювання для всіх доступних тороїдів.
     */
    private void startAuto() {
        clearTable();

        autoQueue.clear();

        autoQueue.add(getPreset(0, 1.0));
        autoQueue.add(getPreset(0, 2.0));

        autoQueue.add(getPreset(1, 1.0));
        autoQueue.add(getPreset(1, 2.0));

        autoQueue.add(getPreset(2, 1.0));
        autoQueue.add(getPreset(2, 2.0));

        isAutoRunning = true;
        processNextAuto();
    }

    /*
     * Лабораторна робота № 3-7 "Магнітна проникність".
     * Функція: processNextAuto.
     * Призначення: Керує чергою автоматичних вимірювань.
     */
    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoRunning = false;
            setControlsDisable(false);
            liveStatusLabel.setText("Статус: ГОТОВО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            return;
        }

        ToroidPreset preset = autoQueue.poll();

        if (preset.name.equals("Тороїд 1")) toroidBox.getSelectionModel().select(0);
        if (preset.name.equals("Тороїд 2")) toroidBox.getSelectionModel().select(1);
        if (preset.name.equals("Тороїд 3")) toroidBox.getSelectionModel().select(2);

        voltageField.setText(String.format(Locale.US, "%.1f", preset.voltage));
        l1Field.setText(String.format(Locale.US, "%.1f", preset.l1));
        l2Field.setText(String.format(Locale.US, "%.1f", preset.l2));

        runMeasurement(preset);
    }

    /*
     * Лабораторна робота № 3-7 "Магнітна проникність".
     * Функція: runMeasurement.
     * Призначення: Симулює процес автоматичного балансування містка Максвелла.
     */
    private void runMeasurement(ToroidPreset preset) {
        setControlsDisable(true);

        double l1 = Double.parseDouble(l1Field.getText().replace(',', '.'));
        double l2 = Double.parseDouble(l2Field.getText().replace(',', '.'));

        liveStatusLabel.setText("Статус: БАЛАНСУВАННЯ...");
        liveStatusLabel.setStyle("-fx-text-fill: red;");

        canvas.animateBalance(l1, l2, preset.voltage,
                () -> Platform.runLater(() -> finishMeasurement(preset, l1, l2)));
    }

    /*
     * Лабораторна робота № 3-7 "Магнітна проникність".
     * Функція: finishMeasurement.
     * Призначення: Розраховує індуктивність та магнітну проникність, додає дані до таблиці.
     */
    private void finishMeasurement(ToroidPreset preset, double l1, double l2) {
        double R = Double.parseDouble(rField.getText().replace(',', '.'));
        double L = Double.parseDouble(refLField.getText().replace(',', '.')) / 1000.0;
        double rx = preset.rx;

        double ratio = l1 / l2;

        double lxSquared = (ratio * ratio) * (R * R + OMEGA * OMEGA * L * L) - rx * rx;

        double lx;
        if (lxSquared <= 0) {
            lx = 0;
        } else {
            lx = Math.sqrt(lxSquared) / OMEGA;
        }

        double lxMilli = lx * 1000.0;

        double dOuter = preset.d1 / 1000.0;
        double dInner = preset.d2 / 1000.0;
        double h = preset.h / 1000.0;

        double meanDiameter = (dOuter + dInner) / 2.0;
        double magneticLength = Math.PI * meanDiameter;
        double crossSection = ((dOuter - dInner) / 2.0) * h;

        double mu = 0;
        if (crossSection > 0 && preset.n > 0) {
            mu = (lx * magneticLength) / (MU_0 * preset.n * preset.n * crossSection);
        }

        mu *= (1.0 + (Math.random() - 0.5) * 0.035);

        liveStatusLabel.setText("Статус: МІСТ ЗБАЛАНСОВАНО");
        liveStatusLabel.setStyle("-fx-text-fill: yellow;");
        liveLxLabel.setText(String.format(Locale.US, "Lx = %.3f мГн", lxMilli));
        liveMuLabel.setText(String.format(Locale.US, "μ = %.2f", mu));

        Measurement measurement = new Measurement(
                idCounter++,
                preset.name,
                preset.voltage,
                preset.n,
                l1,
                l2,
                preset.d1,
                preset.d2,
                preset.h,
                rx,
                lxMilli,
                mu
        );

        data.add(measurement);
        updateStats();

        if (isAutoRunning) {
            new Thread(() -> {
                try {
                    Thread.sleep(1300);
                    Platform.runLater(this::processNextAuto);
                } catch (InterruptedException ignored) {}
            }).start();
        } else {
            setControlsDisable(false);
        }
    }

    /*
     * Лабораторна робота № 3-7 "Магнітна проникність".
     * Функція: setControlsDisable.
     * Призначення: Керує доступністю полів введення під час вимірювання.
     */
    private void setControlsDisable(boolean disable) {
        toroidBox.setDisable(disable);
        voltageField.setDisable(disable);
        l1Field.setDisable(disable);
        l2Field.setDisable(disable);
        rField.setDisable(disable);
        refLField.setDisable(disable);
        measureBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
    }

    /*
     * Лабораторна робота № 3-7 "Магнітна проникність".
     * Функція: clearTable.
     * Призначення: Очищає таблицю результатів.
     */
    private void clearTable() {
        data.clear();
        idCounter = 1;
        updateStats();

        liveStatusLabel.setText("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow;");
        liveLxLabel.setText("Lx = 0.000 мГн");
        liveMuLabel.setText("μ = 0.00");

        canvas.reset();
    }

    /*
     * Лабораторна робота № 3-7 "Магнітна проникність".
     * Функція: updateStats.
     * Призначення: Проводить фінальну статистичну обробку результатів вимірювань проникності.
     */
    private void updateStats() {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: очікування вимірювань...");
            return;
        }

        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }

        StringBuilder sb = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ:\n");
        sb.append(String.format(Locale.US, "1. Проведено вимірювань: %d.\n", data.size()));

        appendToroidStats(sb, "Тороїд 1");
        appendToroidStats(sb, "Тороїд 2");
        appendToroidStats(sb, "Тороїд 3");

        sb.append("4. Lx знайдено за умовою рівноваги містка Максвелла.\n");
        sb.append("5. μ знайдено через формулу індуктивності тороїда.\n");
        sb.append("ВИСНОВОК: різні тороїди мають різну відносну магнетну проникність через різні геометричні параметри та властивості осердя.");

        finalResultLabel.setText(sb.toString());
    }

    /*
     * Лабораторна робота № 3-7 "Магнітна проникність".
     * Функція: appendToroidStats.
     * Призначення: Допоміжна функція для формування тексту висновку по конкретному тороїду.
     */
    private void appendToroidStats(StringBuilder sb, String name) {
        double sumMu = 0;
        double sumLx = 0;
        int count = 0;

        for (Measurement m : data) {
            if (m.getToroidName().equals(name)) {
                sumMu += m.getMu();
                sumLx += m.getLx();
                count++;
            }
        }

        if (count > 0) {
            double avgMu = sumMu / count;
            double avgLx = sumLx / count;

            sb.append(String.format(
                    Locale.US,
                    "%s: середнє Lx = %.3f мГн, середнє μ = %.2f.\n",
                    name,
                    avgLx,
                    avgMu
            ));
        }
    }
}