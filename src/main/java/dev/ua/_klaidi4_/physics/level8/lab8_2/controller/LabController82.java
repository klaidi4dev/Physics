package dev.ua._klaidi4_.physics.level8.lab8_2.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level8.lab8_2.model.Measurement;
import dev.ua._klaidi4_.physics.level8.lab8_2.view.HallEffectCanvas;
import javafx.animation.AnimationTimer;
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

import java.util.Locale;

public class LabController82 extends BaseLabController {

    private HallEffectCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> materialBox;
    private TextField magneticFieldField;
    private TextField thicknessField;
    private TextField iMainField;
    private Button togglePowerBtn;
    private Button measureBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveIMainLabel;
    private Label liveIHallLabel;
    private AnimationTimer simTimer;
    private boolean isPowerOn = false;
    private boolean isAutoMode = false;
    private double currentIMainTarget = 3.0;
    private long lastUiUpdateTime = 0;
    private double currentIHallNoise = 0.0;
    private final double R_12 = 126.503;
    private final double R_36 = 481.601;
    private final double R_MU = 385.002;
    private final double A_M = 0.01;
    private final double B_M = 0.005;
    private final double Q_E = 1.6e-19;
    private final double THEORETICAL_R_HALL = 0.0006;

    public LabController82() {
        initUI();
        startSimulationLoop();
    }

    @Override
    public void shutdown() {
        if (simTimer != null) {
            simTimer.stop();
        }
        isAutoMode = false;
        isPowerOn = false;
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(320);
        leftPanel.setMinWidth(320);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Установка Ефект Холла");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane configPane = new TitledPane();
        configPane.setText("Параметри установки");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(10);
        configBox.setPadding(new Insets(5));

        materialBox = new ComboBox<>();
        materialBox.getItems().addAll("Кремній (p-тип, дірковий)", "Кремній (n-тип, електронний)");
        materialBox.getSelectionModel().selectFirst();
        materialBox.setMaxWidth(Double.MAX_VALUE);

        magneticFieldField = new TextField("0.4");
        thicknessField = new TextField("0.425");
        iMainField = new TextField("3.0");

        configBox.getChildren().addAll(
                new VBox(4, createLabel("Тип напівпровідника:"), materialBox),
                createInputGroup("Магнітна індукція B (Тл):", magneticFieldField),
                createInputGroup("Товщина зразка d (мм):", thicknessField),
                new Separator(),
                createInputGroup("Основний струм I_1,2 (мА):", iMainField)
        );
        configPane.setContent(configBox);

        TitledPane controlPane = new TitledPane();
        controlPane.setText("Керування");
        controlPane.setCollapsible(false);

        VBox controlBox = new VBox(12);
        controlBox.setPadding(new Insets(10));

        togglePowerBtn = new Button("⚡ УВІМКНУТИ ЖИВЛЕННЯ");
        togglePowerBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
        togglePowerBtn.setMaxWidth(Double.MAX_VALUE);
        togglePowerBtn.setOnAction(e -> togglePower());

        measureBtn = new Button("⏺ ЗАПИСАТИ ПОКАЗНИКИ");
        measureBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold;");
        measureBtn.setMaxWidth(Double.MAX_VALUE);
        measureBtn.setOnAction(e -> takeMeasurement());

        autoBtn = new Button("⚙ СЕРІЯ ВИМІРІВ (3, 5, 7 мА)");
        autoBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAutoMode());

        clearBtn = new Button("🗑 ОЧИСТИТИ ДАНІ");
        clearBtn.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> clearData());

        controlBox.getChildren().addAll(togglePowerBtn, new Separator(), measureBtn, autoBtn, clearBtn);
        controlPane.setContent(controlBox);

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, configPane, controlPane));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");

        leftPanel.getChildren().add(scrollLeft);

        canvas = new HallEffectCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(4);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-padding: 10; -fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(240, 110);

        Label dashTitle = new Label("МІКРОАМПЕРМЕТРИ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        liveStatusLabel = new Label("Статус: Вимкнено");
        liveStatusLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");

        liveIMainLabel = new Label("I_1,2: 0.0 мА");
        liveIMainLabel.setStyle("-fx-text-fill: #eab308; -fx-font-size: 16px; -fx-font-weight: bold;");

        liveIHallLabel = new Label("I_x: 0.0 мкА");
        liveIHallLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 16px; -fx-font-weight: bold;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, new Separator(), liveIMainLabel, liveIHallLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Measurement, Double> iMainCol = new TableColumn<>("I_1,2 (мА)");
        iMainCol.setCellValueFactory(new PropertyValueFactory<>("iMain"));

        TableColumn<Measurement, Double> iHallCol = new TableColumn<>("I_x (мкА)");
        iHallCol.setCellValueFactory(new PropertyValueFactory<>("iHall"));

        TableColumn<Measurement, Double> uHallCol = new TableColumn<>("U_x (мВ)");
        uHallCol.setCellValueFactory(new PropertyValueFactory<>("uHall"));

        TableColumn<Measurement, Double> rHallCol = new TableColumn<>("R_x·10⁻⁴ (м³/Кл)");
        rHallCol.setCellValueFactory(new PropertyValueFactory<>("rHall"));

        TableColumn<Measurement, Double> nCol = new TableColumn<>("n·10²¹ (м⁻³)");
        nCol.setCellValueFactory(new PropertyValueFactory<>("n"));

        table.getColumns().addAll(idCol, iMainCol, iHallCol, uHallCol, rHallCol, nCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(160);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        materialBox.setOnAction(e -> clearData());
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        label.setStyle("-fx-text-fill: #475569;");
        return label;
    }

    private double getDoubleValue(TextField field, double defaultValue) {
        try {
            return Double.parseDouble(field.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void togglePower() {
        isPowerOn = !isPowerOn;
        materialBox.setDisable(isPowerOn || isAutoMode);
        magneticFieldField.setDisable(isPowerOn || isAutoMode);
        thicknessField.setDisable(isPowerOn || isAutoMode);
        iMainField.setDisable(isPowerOn || isAutoMode);

        if (isPowerOn) {
            togglePowerBtn.setText("⏹ ВИМКНУТИ ЖИВЛЕННЯ");
            togglePowerBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
            liveStatusLabel.setText("Статус: Вимірювання...");
            liveStatusLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
        } else {
            togglePowerBtn.setText("⚡ УВІМКНУТИ ЖИВЛЕННЯ");
            togglePowerBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
            isAutoMode = false;
            liveStatusLabel.setText("Статус: Вимкнено");
            liveStatusLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
        }
    }

    private void startAutoMode() {
        clearData();
        isAutoMode = true;
        currentIMainTarget = 3.0;
        iMainField.setText(String.format(Locale.US, "%.1f", currentIMainTarget));

        if (!isPowerOn) {
            togglePower();
        }
        liveStatusLabel.setText("Статус: Авторежим...");
        liveStatusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
    }

    private void clearData() {
        data.clear();
        idCounter = 1;
        updateStats();
    }

    private double calculateIHall(double iMain_mA, double b_T, double d_mm, boolean isPType) {
        double iMain_A = iMain_mA * 1e-3;
        double d_m = d_mm * 1e-3;
        double uHall_V = (THEORETICAL_R_HALL * b_T * iMain_A) / d_m;
        double iHall_A = uHall_V / (R_MU + R_36);
        double iHall_uA = iHall_A * 1e6;
        double sign = isPType ? 1.0 : -1.0;

        return (iHall_uA * sign) + currentIHallNoise;
    }

    private void takeMeasurement() {
        if (!isPowerOn) return;

        double iMain_mA = getDoubleValue(iMainField, 3.0);
        double b_T = getDoubleValue(magneticFieldField, 0.4);
        double d_mm = getDoubleValue(thicknessField, 0.425);
        boolean isPType = materialBox.getSelectionModel().getSelectedIndex() == 0;

        currentIHallNoise = (Math.random() - 0.5) * 0.4;
        double iHall_uA = calculateIHall(iMain_mA, b_T, d_mm, isPType);
        double uHall_V = (Math.abs(iHall_uA) * 1e-6) * (R_MU + R_36);
        double uHall_mV = uHall_V * 1000.0;
        double iMain_A = iMain_mA * 1e-3;
        double d_m = d_mm * 1e-3;
        double rHall_m3C = (uHall_V * d_m) / (b_T * iMain_A);
        double rHall_scaled = rHall_m3C * 1e4;
        double n_m3 = 1.0 / (Q_E * rHall_m3C);
        double n_scaled = n_m3 * 1e-21;

        Measurement m = new Measurement(
                idCounter++,
                Math.round(iMain_mA * 10.0) / 10.0,
                Math.round(iHall_uA * 10.0) / 10.0,
                Math.round(uHall_mV * 1000.0) / 1000.0,
                Math.round(rHall_scaled * 1000.0) / 1000.0,
                Math.round(n_scaled * 1000.0) / 1000.0
        );
        data.add(m);
        table.scrollTo(data.size() - 1);
        updateStats();
    }

    private void startSimulationLoop() {
        simTimer = new AnimationTimer() {
            private long lastTime = 0;
            private double timeSinceLastAutoMeasure = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }

                double rawDt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                final double dt = Math.min(rawDt, 0.1);
                final double currentIMain_mA = isPowerOn ? getDoubleValue(iMainField, 3.0) : 0.0;
                final double b_T = getDoubleValue(magneticFieldField, 0.4);
                final double d_mm = getDoubleValue(thicknessField, 0.425);
                final boolean isPType = materialBox.getSelectionModel().getSelectedIndex() == 0;

                if (isAutoMode && isPowerOn) {
                    timeSinceLastAutoMeasure += dt;
                    if (timeSinceLastAutoMeasure > 1.5) {
                        takeMeasurement();
                        timeSinceLastAutoMeasure = 0;
                        currentIMainTarget += 2.0;

                        if (currentIMainTarget > 7.1) {
                            isAutoMode = false;
                            togglePower();
                            liveStatusLabel.setText("Статус: Авторежим завершено");
                            liveStatusLabel.setStyle("-fx-text-fill: #a3e635; -fx-font-weight: bold;");
                        } else {
                            iMainField.setText(String.format(Locale.US, "%.1f", currentIMainTarget));
                        }
                    }
                }

                if (now - lastUiUpdateTime > 330_000_000L) {
                    lastUiUpdateTime = now;
                    if (isPowerOn) {
                        currentIHallNoise = (Math.random() - 0.5) * 0.3;

                        final double currentIHall_uA = calculateIHall(currentIMain_mA, b_T, d_mm, isPType);

                        Platform.runLater(() -> {
                            liveIMainLabel.setText(String.format(Locale.US, "I_1,2: %.1f мА", currentIMain_mA));
                            liveIHallLabel.setText(String.format(Locale.US, "I_x: %.1f мкА", currentIHall_uA));
                        });
                    } else {
                        Platform.runLater(() -> {
                            liveIMainLabel.setText("I_1,2: 0.0 мА");
                            liveIHallLabel.setText("I_x: 0.0 мкА");
                        });
                    }
                }

                Platform.runLater(() -> canvas.updateState(currentIMain_mA, b_T, isPType, isPowerOn, dt));
            }
        };
        simTimer.start();
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

        double sumRx = 0;
        double sumN = 0;
        for (Measurement m : data) {
            sumRx += m.getRHall() * 1e-4;
            sumN += m.getN() * 1e21;
        }
        double avgRx = sumRx / data.size();
        double avgN = sumN / data.size();
        double d_mm = getDoubleValue(thicknessField, 0.425);
        double d_m = d_mm * 1e-3;
        double sigma = A_M / (B_M * d_m * R_12);
        double u_p = sigma / (Q_E * avgN);

        boolean isPType = materialBox.getSelectionModel().getSelectedIndex() == 0;
        String typeStr = isPType ? "діркову (р-тип)" : "електронну (n-тип)";

        StringBuilder analysis = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ:\n");
        analysis.append(String.format(Locale.US, "1. Середня стала Холла: R_x ≈ %.3f · 10⁻⁴ м³/Кл.\n", avgRx * 1e4));
        analysis.append(String.format(Locale.US, "2. Концентрація носіїв: n = 1/(q·R_x) ≈ %.2f · 10²¹ м⁻³.\n", avgN * 1e-21));
        analysis.append(String.format(Locale.US, "3. Питома електропровідність: σ = a/(b·d·r_1,2) ≈ %.1f Ом⁻¹·м⁻¹.\n", sigma));
        analysis.append(String.format(Locale.US, "4. Рухливість носіїв: U_p = σ/(q·n) ≈ %.4f м²/(В·с).\n", u_p));
        analysis.append(String.format("ВИСНОВОК: Зафіксовано %s знак ефекту Холла, що свідчить про %s провідність досліджуваного напівпровідника.",
                isPType ? "позитивний" : "негативний", typeStr));

        finalResultLabel.setText(analysis.toString());
    }
}