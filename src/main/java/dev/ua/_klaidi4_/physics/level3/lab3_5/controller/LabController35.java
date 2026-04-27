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
    private Slider uSlider;
    private Label uLabel;

    private Button addPointBtn;
    private Button autoRunBtn;
    private Button clearBtn;

    private Label liveStatusLabel;
    private Label liveULabel;
    private Label liveILabel;
    private Label livePLabel;

    private boolean isAutoRunning = false;
    private double currentU_meas;
    private double currentI_meas;
    private double currentP_meas;

    public LabController35() {
        initUI();
        updatePhysics();
    }

    @Override
    public void shutdown() {
        isAutoRunning = false;
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 3-5)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane paramsPane = new TitledPane();
        paramsPane.setText("Параметри кола");
        paramsPane.setCollapsible(false);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        freqField = new TextField("50.0");
        r0Field = new TextField("15.0");
        l0Field = new TextField("0.04");
        lCoreField = new TextField("0.25");
        rCoreField = new TextField("45.0");

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

        uLabel = new Label("Напруга ЛАТР: 50 В");
        uSlider = new Slider(0, 100, 50);
        uSlider.setBlockIncrement(5);
        uSlider.setShowTickMarks(true);
        uSlider.valueProperty().addListener((o, old, val) -> {
            uLabel.setText(String.format("Напруга ЛАТР: %d В", val.intValue()));
            updatePhysics();
        });

        controlBox.getChildren().addAll(
                createInputGroup("Стан котушки:", coreCombo),
                uLabel,
                uSlider
        );
        controlPane.setContent(controlBox);

        addPointBtn = new Button("✍ ЗАПИСАТИ ПОКАЗИ");
        addPointBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        addPointBtn.setMaxWidth(Double.MAX_VALUE);
        addPointBtn.setOnAction(e -> recordPoint());

        autoRunBtn = new Button("⚙ АВТО-ВИМІРЮВАННЯ");
        autoRunBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoRunBtn.setMaxWidth(Double.MAX_VALUE);
        autoRunBtn.setOnAction(e -> runAuto());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear();
            idCounter = 1;
            updateStats();
        });

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, paramsPane, controlPane, addPointBtn, autoRunBtn, clearBtn));
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

    private TableColumn<Measurement, String> createColStr(String title, String property) {
        TableColumn<Measurement, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }

    private TableColumn<Measurement, Double> createCol(String title, String property) {
        TableColumn<Measurement, Double> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }

    private void updatePhysics() {
        if (isAutoRunning) return;
        try {
            double u = uSlider.getValue();
            double freq = Double.parseDouble(freqField.getText());
            double omega = 2 * Math.PI * freq;
            double r0 = Double.parseDouble(r0Field.getText());
            double l0 = Double.parseDouble(l0Field.getText());
            double lCore = Double.parseDouble(lCoreField.getText());
            double rCore = Double.parseDouble(rCoreField.getText());

            boolean hasCore = coreCombo.getSelectionModel().getSelectedIndex() == 1;
            double Z, R_active, I_true, P_true;

            if (hasCore) {
                Z = Math.sqrt(rCore * rCore + Math.pow(omega * lCore, 2));
                R_active = rCore;
            } else {
                Z = Math.sqrt(r0 * r0 + Math.pow(omega * l0, 2));
                R_active = r0;
            }

            if (u == 0 || Z == 0) {
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

            liveULabel.setText(String.format("U = %.1f В", currentU_meas));
            liveILabel.setText(String.format("I = %.2f А", currentI_meas));
            livePLabel.setText(String.format("P = %.1f Вт", currentP_meas));

            if (canvas != null) {
                canvas.updateMeters(currentU_meas, currentI_meas, currentP_meas, hasCore);
            }
        } catch (NumberFormatException ignored) {}
    }

    private void recordPoint() {
        if (currentU_meas == 0) return;

        Measurement m = new Measurement(
                idCounter++,
                coreCombo.getValue(),
                Math.round(currentU_meas),
                Math.round(currentI_meas * 100) / 100.0,
                Math.round(currentP_meas * 10) / 10.0
        );
        data.add(m);
        updateStats();
    }

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
                for (int u = 30; u <= 50; u += 10) {
                    final int voltage = u;
                    Platform.runLater(() -> {
                        uSlider.setValue(voltage);
                        updatePhysics();
                        recordPoint();
                    });
                    Thread.sleep(800);
                }

                Platform.runLater(() -> coreCombo.getSelectionModel().select(1));
                for (int u = 30; u <= 50; u += 10) {
                    final int voltage = u;
                    Platform.runLater(() -> {
                        uSlider.setValue(voltage);
                        updatePhysics();
                        recordPoint();
                    });
                    Thread.sleep(800);
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

    private void setControlsDisable(boolean disable) {
        uSlider.setDisable(disable);
        coreCombo.setDisable(disable);
        addPointBtn.setDisable(disable);
        autoRunBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        freqField.setDisable(disable);
        r0Field.setDisable(disable);
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
        try {
            double freq = Double.parseDouble(freqField.getText());
            double omega = 2 * Math.PI * freq;
            double r0 = Double.parseDouble(r0Field.getText());

            List<Measurement> noCoreData = new ArrayList<>();
            List<Measurement> coreData = new ArrayList<>();

            for (Measurement m : data) {
                if (m.getState().equals("Без осердя")) noCoreData.add(m);
                else coreData.add(m);
            }

            StringBuilder sb = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ:\n");

            if (!noCoreData.isEmpty()) {
                double sumZ = 0;
                for (Measurement m : noCoreData) {
                    sumZ += (m.getU() / m.getI());
                }
                double zAvg = sumZ / noCoreData.size();
                double val = zAvg * zAvg - r0 * r0;
                double l0 = 0;
                if (val > 0) l0 = Math.sqrt(val) / omega;

                sb.append(String.format("1. Без осердя: Середній повний опір Zср = %.2f Ом.\n", zAvg));
                sb.append(String.format("2. Індуктивність без осердя: L0 = %.4f Гн.\n", l0));
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

                sb.append(String.format("3. З осердям: Повний опір Z' = %.2f Ом.\n", zcAvg));
                sb.append(String.format("4. Активний опір з осердям R' = %.2f Ом.\n", rcAvg));
                sb.append(String.format("5. Індуктивність з осердям L' = %.4f Гн.\n", lc));
            }

            if (!noCoreData.isEmpty() && !coreData.isEmpty()) {
                sb.append("\nВИСНОВОК: Введення феромагнітного осердя призводить до різкого збільшення магнітного потоку, що спричиняє зростання індуктивності (L > L0) та активних втрат (R' > R0).");
            }

            finalResultLabel.setText(sb.toString());
        } catch (NumberFormatException ignored) {}
    }
}