package dev.ua._klaidi4_.physics.level8.lab8_1.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level8.lab8_1.model.Measurement;
import dev.ua._klaidi4_.physics.level8.lab8_1.view.SemiconductorCanvas;
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

public class LabController81 extends BaseLabController {

    private SemiconductorCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField maxTempField;
    private TextField heatRateField;
    private TextField autoStepField;
    private Button toggleHeaterBtn;
    private Button measureBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveTempLabel;
    private Label liveResLabel;
    private AnimationTimer simTimer;
    private double currentTempC = 20.0;
    private boolean isHeating = false;
    private boolean isAutoMode = false;
    private double nextAutoTargetC = 20.0;
    private long lastUiUpdateTime = 0;
    private double currentNoise = 0.0;
    private final double R0_KOHM = 0.00276;
    private final double B_CONST = 2450.0;
    private final double BOLTZMANN_K_J = 1.38e-23;

    public LabController81() {
        initUI();
        startSimulationLoop();
    }

    @Override
    public void shutdown() {
        if (simTimer != null) {
            simTimer.stop();
        }
        isAutoMode = false;
        isHeating = false;
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(320);
        leftPanel.setMinWidth(320);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Установка (Лаб 8-1)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane configPane = new TitledPane();
        configPane.setText("Налаштування термостата");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(10);
        configBox.setPadding(new Insets(5));

        maxTempField = new TextField("80.0");
        heatRateField = new TextField("4.0");
        autoStepField = new TextField("5.0");

        configBox.getChildren().addAll(
                createInputGroup("Макс. температура (°C):", maxTempField),
                createInputGroup("Швидкість нагрівання (°C/с):", heatRateField),
                createInputGroup("Крок авторежиму (°C):", autoStepField)
        );
        configPane.setContent(configBox);

        TitledPane controlPane = new TitledPane();
        controlPane.setText("Керування процесом");
        controlPane.setCollapsible(false);

        VBox controlBox = new VBox(12);
        controlBox.setPadding(new Insets(10));

        toggleHeaterBtn = new Button("🔥 УВІМКНУТИ НАГРІВ");
        toggleHeaterBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
        toggleHeaterBtn.setMaxWidth(Double.MAX_VALUE);
        toggleHeaterBtn.setOnAction(e -> toggleHeater());

        measureBtn = new Button("⏺ ЗНЯТИ ПОКАЗНИКИ");
        measureBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold;");
        measureBtn.setMaxWidth(Double.MAX_VALUE);
        measureBtn.setOnAction(e -> takeMeasurement());

        autoBtn = new Button("⚙ АВТОМАТИЧНИЙ ДОСЛІД");
        autoBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAutoMode());

        clearBtn = new Button("🗑 ОЧИСТИТИ ДАНІ");
        clearBtn.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> clearData());

        controlBox.getChildren().addAll(toggleHeaterBtn, new Separator(), measureBtn, autoBtn, clearBtn);
        controlPane.setContent(controlBox);

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, configPane, controlPane));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");

        leftPanel.getChildren().add(scrollLeft);

        canvas = new SemiconductorCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(4);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-padding: 10; -fx-border-color: #10b981; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(260, 110);

        Label dashTitle = new Label("ЦИФРОВИЙ МУЛЬТИМЕТР");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        liveStatusLabel = new Label("Статус: Очікування");
        liveStatusLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");

        liveTempLabel = new Label("T: 20.0 °C");
        liveTempLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 16px; -fx-font-weight: bold;");

        liveResLabel = new Label("R: 0.00 kОм");
        liveResLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 16px; -fx-font-weight: bold;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, new Separator(), liveTempLabel, liveResLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Measurement, Double> tcCol = new TableColumn<>("t (°C)");
        tcCol.setCellValueFactory(new PropertyValueFactory<>("tC"));

        TableColumn<Measurement, Double> tkCol = new TableColumn<>("T (K)");
        tkCol.setCellValueFactory(new PropertyValueFactory<>("tK"));

        TableColumn<Measurement, Double> invTCol = new TableColumn<>("1/T * 10³");
        invTCol.setCellValueFactory(new PropertyValueFactory<>("invT"));

        TableColumn<Measurement, Double> rCol = new TableColumn<>("R (кОм)");
        rCol.setCellValueFactory(new PropertyValueFactory<>("rKohm"));

        TableColumn<Measurement, Double> lnRCol = new TableColumn<>("ln(R)");
        lnRCol.setCellValueFactory(new PropertyValueFactory<>("lnR"));

        table.getColumns().addAll(idCol, tcCol, tkCol, invTCol, rCol, lnRCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(160);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);
    }

    private double getDoubleValue(TextField field, double defaultValue) {
        try {
            return Double.parseDouble(field.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void toggleHeater() {
        isHeating = !isHeating;
        setSettingsDisable(isHeating || isAutoMode);

        if (isHeating) {
            toggleHeaterBtn.setText("⏹ ВИМКНУТИ НАГРІВ");
            toggleHeaterBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
            liveStatusLabel.setText("Статус: Йде нагрівання...");
            liveStatusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        } else {
            toggleHeaterBtn.setText("🔥 УВІМКНУТИ НАГРІВ");
            toggleHeaterBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
            isAutoMode = false;
            liveStatusLabel.setText("Статус: Охолодження...");
            liveStatusLabel.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");
        }
    }

    private void setSettingsDisable(boolean disable) {
        maxTempField.setDisable(disable);
        heatRateField.setDisable(disable);
        autoStepField.setDisable(disable);
    }

    private void startAutoMode() {
        clearData();
        currentTempC = 20.0;

        double step = getDoubleValue(autoStepField, 5.0);
        double maxTemp = getDoubleValue(maxTempField, 80.0);

        nextAutoTargetC = 20.0;
        isAutoMode = true;

        if (!isHeating) {
            toggleHeater();
        }
        liveStatusLabel.setText(String.format(Locale.US, "Авторежим (до %.1f°C)", maxTemp));
        liveStatusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
    }

    private void clearData() {
        data.clear();
        idCounter = 1;
        updateStats();
    }

    private double calculateCurrentResistance() {
        double tempK = currentTempC + 273.15;
        double theoreticalR = R0_KOHM * Math.exp(B_CONST / tempK);
        return theoreticalR * (1.0 + currentNoise);
    }

    private void takeMeasurement() {
        double tempK = currentTempC + 273.15;
        double invT = (1.0 / tempK) * 1000.0;
        currentNoise = (Math.random() - 0.5) * 0.015;
        double resistance = calculateCurrentResistance();
        double lnR = Math.log(resistance);

        Measurement m = new Measurement(
                idCounter++,
                Math.round(currentTempC * 10.0) / 10.0,
                Math.round(tempK * 10.0) / 10.0,
                Math.round(invT * 1000.0) / 1000.0,
                Math.round(resistance * 100.0) / 100.0,
                Math.round(lnR * 1000.0) / 1000.0
        );
        data.add(m);
        table.scrollTo(data.size() - 1);
        updateStats();
    }

    private void startSimulationLoop() {
        simTimer = new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                if (dt > 0.1) dt = 0.1;

                double maxTemp = getDoubleValue(maxTempField, 80.0);
                double heatRate = getDoubleValue(heatRateField, 4.0);
                double autoStep = getDoubleValue(autoStepField, 5.0);

                if (isHeating) {
                    currentTempC += heatRate * dt;
                    if (currentTempC > maxTemp) {
                        currentTempC = maxTemp;
                        if (isAutoMode) {
                            toggleHeater();
                            liveStatusLabel.setText("Статус: Авторежим завершено");
                            liveStatusLabel.setStyle("-fx-text-fill: #a3e635; -fx-font-weight: bold;");
                        }
                    }
                } else {
                    if (currentTempC > 20.0) {
                        currentTempC -= 1.5 * dt;
                    } else {
                        currentTempC = 20.0;
                        if (!isAutoMode && liveStatusLabel.getText().contains("Охолодження")) {
                            liveStatusLabel.setText("Статус: Очікування");
                            liveStatusLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
                        }
                    }
                }

                if (isAutoMode && isHeating && currentTempC >= nextAutoTargetC) {
                    takeMeasurement();
                    nextAutoTargetC += autoStep;

                    if (nextAutoTargetC > maxTemp + 0.1) {
                        isAutoMode = false;
                        toggleHeater();
                        liveStatusLabel.setText("Статус: Авторежим завершено");
                        liveStatusLabel.setStyle("-fx-text-fill: #a3e635; -fx-font-weight: bold;");
                    }
                }

                if (now - lastUiUpdateTime > 330_000_000L) {
                    lastUiUpdateTime = now;
                    currentNoise = (Math.random() - 0.5) * 0.015;
                    double res = calculateCurrentResistance();

                    Platform.runLater(() -> {
                        liveTempLabel.setText(String.format(Locale.US, "T: %.1f °C", currentTempC));
                        liveResLabel.setText(String.format(Locale.US, "R: %.2f kОм", res));
                    });
                }

                Platform.runLater(() -> canvas.updateState(currentTempC, isHeating, maxTemp));
            }
        };
        simTimer.start();
    }

    private void updateStats() {
        if (data.size() < 2) {
            finalResultLabel.setText("Обробка результатів: Недостатньо даних (мінімум 2 виміри)");
            return;
        }

        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }

        Measurement m1 = data.get(0);
        Measurement m2 = data.get(data.size() - 1);

        double t1 = m1.getTK();
        double t2 = m2.getTK();
        double r1 = m1.getRKohm();
        double r2 = m2.getRKohm();

        if (Math.abs(t2 - t1) < 1.0) {
            finalResultLabel.setText("Обробка результатів: Температури повинні відрізнятися для розрахунку.");
            return;
        }

        double bCalculated = ((t1 * t2) / (t2 - t1)) * Math.log(r1 / r2);

        double deltaEJoules = BOLTZMANN_K_J * bCalculated;
        double deltaEEv = deltaEJoules / 1.6e-19;

        StringBuilder analysis = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ:\n");
        analysis.append(String.format(Locale.US, "1. Обрані крайні точки: T1 = %.1f K, R1 = %.2f кОм; T2 = %.1f K, R2 = %.2f кОм.\n", t1, r1, t2, r2));
        analysis.append(String.format(Locale.US, "2. Коефіцієнт температурної чутливості: B = (T1·T2)/(T2-T1)·ln(R1/R2) ≈ %.0f K.\n", bCalculated));
        analysis.append(String.format(Locale.US, "3. Енергія активації (в Дж): ΔE = k·B = 1.38·10⁻²³ · %.0f ≈ %.2e Дж.\n", bCalculated, deltaEJoules));
        analysis.append(String.format(Locale.US, "4. Енергія активації (в еВ): ΔE ≈ %.3f еВ.\n", deltaEEv));
        analysis.append("ВИСНОВОК: Отримане значення ΔE відповідає домішковому напівпровіднику (терморезистору), де температурна залежність має експоненціальний характер.");

        finalResultLabel.setText(analysis.toString());
    }
}