/*
 * Лабораторна робота № 3-5 "Індуктивність соленоїда".
 * Клас: LabController35.
 * Призначення: керує інтерфейсом лабораторної роботи, проведенням дослідів
 * з визначення індуктивності соленоїда за допомогою ЛАТР.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level3.lab3_5.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level3.lab3_5.model.Measurement;
import dev.ua._klaidi4_.physics.level3.lab3_5.view.CircuitCanvas;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LabController35 extends BaseLabController {

    private CircuitCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField freqField;
    private TextField r0Field;
    private TextField l0Field;
    private TextField lCoreField;
    private TextField rCoreField;
    private ComboBox<String> coreCombo;
    private TextField uField;
    private Button addPointBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveULabel;
    private Label liveILabel;
    private Label livePLabel;
    private boolean isAutoRunning = false;
    private double currentU_meas;
    private double currentI_meas;
    private double currentP_meas;

    /*
     * Лабораторна робота № 3-5 "Індуктивність соленоїда".
     * Функція: LabController35.
     * Призначення: Конструктор класу, ініціалізує інтерфейс.
     */
    public LabController35() {
        initUI();
        updatePhysics();
    }

    /*
     * Лабораторна робота № 3-5 "Індуктивність соленоїда".
     * Функція: shutdown.
     * Призначення: Скидає стан системи при закритті вікна.
     */
    @Override
    public void shutdown() {
        isAutoRunning = false;
    }

    /*
     * Лабораторна робота № 3-5 "Індуктивність соленоїда".
     * Функція: initUI.
     * Призначення: Ініціалізує графічний інтерфейс: схему установки, панелі керування та таблицю результатів.
     */
    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 3-5)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane paramsPane = new TitledPane();
        paramsPane.setText("Параметри кола (Приховані константи)");
        paramsPane.setCollapsible(true);
        paramsPane.setExpanded(false);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        freqField = new TextField("50.0");
        r0Field = new TextField("31.5");
        l0Field = new TextField("0.67");
        lCoreField = new TextField("1.38");
        rCoreField = new TextField("32.1");

        freqField.textProperty().addListener((o, old, val) -> updatePhysics());
        r0Field.textProperty().addListener((o, old, val) -> updatePhysics());
        l0Field.textProperty().addListener((o, old, val) -> updatePhysics());
        lCoreField.textProperty().addListener((o, old, val) -> updatePhysics());
        rCoreField.textProperty().addListener((o, old, val) -> updatePhysics());

        paramsBox.getChildren().addAll(
                createInputGroup("Частота ν (Гц):", freqField),
                createInputGroup("Опір дроту R0 (Ом):", r0Field),
                createInputGroup("L без осердя (Гн):", l0Field),
                createInputGroup("L з осердям (Гн):", lCoreField),
                createInputGroup("R з осердям (Ом):", rCoreField)
        );
        paramsPane.setContent(paramsBox);

        TitledPane controlPane = new TitledPane();
        controlPane.setText("Управління");
        controlPane.setCollapsible(false);

        VBox controlBox = new VBox(12);
        controlBox.setPadding(new Insets(5));

        coreCombo = new ComboBox<>(FXCollections.observableArrayList("Без осердя", "З осердям"));
        coreCombo.getSelectionModel().selectFirst();
        coreCombo.setMaxWidth(Double.MAX_VALUE);
        coreCombo.setOnAction(e -> updatePhysics());

        uField = new TextField("100.0");
        uField.textProperty().addListener((o, old, val) -> updatePhysics());

        controlBox.getChildren().addAll(
                createInputGroup("Стан котушки:", coreCombo),
                createInputGroup("Напруга ЛАТР U (В):", uField)
        );
        controlPane.setContent(controlBox);

        addPointBtn = new Button("✍ ЗАПИСАТИ ПОКАЗИ");
        addPointBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        addPointBtn.setMaxWidth(Double.MAX_VALUE);
        addPointBtn.setOnAction(e -> recordPoint());

        autoBtn = new Button("⚙ АВТО-ВИМІРЮВАННЯ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> runAuto());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear();
            idCounter = 1;
            updateStats();
        });

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, paramsPane, controlPane, addPointBtn, autoBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new CircuitCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(220, 100);

        Label dashTitle = new Label("ПОКАЗИ ПРИЛАДІВ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ГОТОВО");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveULabel = new Label("U = 0.00 В");
        liveULabel.setStyle("-fx-text-fill: #00ff00;");
        liveILabel = new Label("I = 0.00 А");
        liveILabel.setStyle("-fx-text-fill: #00ff00;");
        livePLabel = new Label("P = 0.00 Вт");
        livePLabel.setStyle("-fx-text-fill: #00ff00;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveULabel, liveILabel, livePLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);
        table.getColumns().addAll(
                createColStr("Стан", "state"),
                createCol("U (В)", "u"),
                createCol("I (А)", "i"),
                createCol("P (Вт)", "p")
        );
        table.setPrefHeight(130);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox statsBox = createStatsBox();

        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        updateStats();
    }

    /*
     * Лабораторна робота № 3-5 "Індуктивність соленоїда".
     * Функція: createColStr.
     * Призначення: Допоміжна функція для створення текстових колонок таблиці.
     */
    private TableColumn<Measurement, String> createColStr(String title, String property) {
        TableColumn<Measurement, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }

    /*
     * Лабораторна робота № 3-5 "Індуктивність соленоїда".
     * Функція: createCol.
     * Призначення: Допоміжна функція для створення числових колонок таблиці з форматуванням.
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
     * Лабораторна робота № 3-5 "Індуктивність соленоїда".
     * Функція: updatePhysics.
     * Призначення: Розраховує параметри змінного струму в колі соленоїда залежно від положення ЛАТР та сердечника.
     */
    private void updatePhysics() {
        try {
            double u = Double.parseDouble(uField.getText().replace(',', '.'));
            double freq = Double.parseDouble(freqField.getText().replace(',', '.'));
            double omega = 2 * Math.PI * freq;
            double r0 = Double.parseDouble(r0Field.getText().replace(',', '.'));
            double l0 = Double.parseDouble(l0Field.getText().replace(',', '.'));
            double lCore = Double.parseDouble(lCoreField.getText().replace(',', '.'));
            double rCore = Double.parseDouble(rCoreField.getText().replace(',', '.'));

            boolean hasCore = coreCombo.getSelectionModel().getSelectedIndex() == 1;
            double Z, R_active, I_true, P_true;

            if (hasCore) {
                Z = Math.sqrt(rCore * rCore + Math.pow(omega * lCore, 2));
                R_active = rCore;
            } else {
                Z = Math.sqrt(r0 * r0 + Math.pow(omega * l0, 2));
                R_active = r0;
            }

            if (u <= 0 || Z == 0) {
                I_true = 0;
                P_true = 0;
            } else {
                I_true = u / Z;
                P_true = I_true * I_true * R_active;
            }

            double noiseFactI = 1.0 + (Math.random() - 0.5) * 0.02;
            double noiseFactP = 1.0 + (Math.random() - 0.5) * 0.02;

            currentU_meas = u;
            currentI_meas = I_true * noiseFactI;
            currentP_meas = P_true * noiseFactP;

            liveULabel.setText(String.format(Locale.US, "U = %.1f В", currentU_meas));
            liveILabel.setText(String.format(Locale.US, "I = %.2f А", currentI_meas));
            livePLabel.setText(String.format(Locale.US, "P = %.1f Вт", currentP_meas));

            if (canvas != null) {
                canvas.updateMeters(currentU_meas, currentI_meas, currentP_meas, hasCore);
            }
        } catch (NumberFormatException ignored) {}
    }

    /*
     * Лабораторна робота № 3-5 "Індуктивність соленоїда".
     * Функція: recordPoint.
     * Призначення: Записує поточні показники вольтметра, амперметра та ватметра до таблиці.
     */
    private void recordPoint() {
        if (currentU_meas <= 0) return;

        Measurement m = new Measurement(
                idCounter++,
                coreCombo.getValue(),
                currentU_meas,
                currentI_meas,
                currentP_meas
        );

        data.add(m);
        updateStats();
    }

    /*
     * Лабораторна робота № 3-5 "Індуктивність соленоїда".
     * Функція: runAuto.
     * Призначення: Виконує автоматичне зняття вольт-амперної характеристики соленоїда.
     */
    private void runAuto() {
        data.clear();
        idCounter = 1;
        updateStats();

        isAutoRunning = true;
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: АВТОВИМІРЮВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: red;");

        new Thread(() -> {
            try {
                Platform.runLater(() -> coreCombo.getSelectionModel().select(0));
                for (int u = 100; u <= 140; u += 20) {
                    final int voltage = u;
                    Platform.runLater(() -> {
                        uField.setText(String.valueOf(voltage));
                        updatePhysics();
                        recordPoint();
                    });
                    Thread.sleep(1000);
                }

                Platform.runLater(() -> coreCombo.getSelectionModel().select(1));
                for (int u = 100; u <= 140; u += 20) {
                    final int voltage = u;
                    Platform.runLater(() -> {
                        uField.setText(String.valueOf(voltage));
                        updatePhysics();
                        recordPoint();
                    });
                    Thread.sleep(1000);
                }

                Platform.runLater(() -> {
                    isAutoRunning = false;
                    setControlsDisable(false);
                    liveStatusLabel.setText("Статус: ГОТОВО");
                    liveStatusLabel.setStyle("-fx-text-fill: yellow;");
                });
            } catch (InterruptedException ignored) {}
        }).start();
    }

    /*
     * Лабораторна робота № 3-5 "Індуктивність соленоїда".
     * Функція: setControlsDisable.
     * Призначення: Керує доступністю інтерфейсу під час автоматичного вимірювання.
     */
    private void setControlsDisable(boolean disable) {
        uField.setDisable(disable);
        coreCombo.setDisable(disable);
        addPointBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        freqField.setDisable(disable);
        r0Field.setDisable(disable);
        l0Field.setDisable(disable);
        lCoreField.setDisable(disable);
        rCoreField.setDisable(disable);
    }

    /*
     * Лабораторна робота № 3-5 "Індуктивність соленоїда".
     * Функція: updateStats.
     * Призначення: Проводить розрахунок індуктивності та інших параметрів котушки для всієї серії вимірювань.
     */
    private void updateStats() {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: Очікування вимірювань...");
            return;
        }
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }
        try {
            double freq = Double.parseDouble(freqField.getText().replace(',', '.'));
            double omega = 2 * Math.PI * freq;
            double r0 = Double.parseDouble(r0Field.getText().replace(',', '.'));

            List<Measurement> noCoreData = new ArrayList<>();
            List<Measurement> coreData = new ArrayList<>();

            for (Measurement m : data) {
                if (m.getState().equals("Без осердя")) noCoreData.add(m);
                else coreData.add(m);
            }

            StringBuilder sb = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ:\n");

            if (!noCoreData.isEmpty()) {
                double sumZ = 0;
                double sumR = 0;

                for (Measurement m : noCoreData) {
                    sumZ += (m.getU() / m.getI());
                    sumR += (m.getP() / (m.getI() * m.getI()));
                }

                double zAvg = sumZ / noCoreData.size();
                double rAvg = sumR / noCoreData.size();

                double val = zAvg * zAvg - rAvg * rAvg;
                double l0 = 0;
                if (val > 0) l0 = Math.sqrt(val) / omega;

                sb.append(String.format(Locale.US, "1. Без осердя: Повний опір Z0 = %.2f Ом.\n", zAvg));
                sb.append(String.format(Locale.US, "2. Без осердя: Активний опір R0 = %.2f Ом.\n", rAvg));
                sb.append(String.format(Locale.US, "3. Індуктивність без осердя: L0 = %.4f Гн.\n", l0));
            }

            if (!coreData.isEmpty()) {
                double sumZc = 0, sumRc = 0;
                for (Measurement m : coreData) {
                    sumZc += (m.getU() / m.getI());
                    sumRc += (m.getP() / (m.getI() * m.getI()));
                }
                double zcAvg = sumZc / coreData.size();
                double rcAvg = sumRc / coreData.size();

                double val = zcAvg * zcAvg - rcAvg * rcAvg;
                double lc = 0;
                if (val > 0) lc = Math.sqrt(val) / omega;

                sb.append(String.format(Locale.US, "4. З осердям: Повний опір Z' = %.2f Ом.\n", zcAvg));
                sb.append(String.format(Locale.US, "5. Активний опір з осердям R' = %.2f Ом.\n", rcAvg));
                sb.append(String.format(Locale.US, "6. Індуктивність з осердям L' = %.4f Гн.\n", lc));
            }

            if (!noCoreData.isEmpty() && !coreData.isEmpty()) {
                sb.append("\nВИСНОВОК: Введення феромагнітного осердя призводить до збільшення магнітного потоку, що спричиняє зростання індуктивності (L' > L0) та активних втрат (R' > R0).");
            }

            finalResultLabel.setText(sb.toString());
        } catch (NumberFormatException ignored) {}
    }
}