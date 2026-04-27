package dev.ua._klaidi4_.physics.level6.lab6_3.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level6.lab6_3.model.Measurement;
import dev.ua._klaidi4_.physics.level6.lab6_3.view.GammaAttenuationCanvas;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class LabController63 extends BaseLabController {

    private GammaAttenuationCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> isotopeComboBox;
    private Slider xSlider;
    private Slider timeSlider;
    private Slider intensitySlider;
    private Label xValueLabel;
    private Label timeValueLabel;
    private Label intensityLabel;
    private Button measureBtn;
    private Button calcBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStepLabel;
    private Label liveTimerLabel;
    private Label liveCountLabel;
    private Label liveLnCountLabel;
    private boolean isMeasuring = false;

    private AnimationTimer measureTimer;
    private AnimationTimer autoTimer;
    private Queue<Double> autoQueue = new LinkedList<>();

    public LabController63() {
        initUI();
        applyPhysicsSettings();
    }

    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        if (measureTimer != null) measureTimer.stop();
        if (autoTimer != null) autoTimer.stop();
        autoQueue.clear();
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 6-3)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane configPane = new TitledPane();
        configPane.setText("Параметри установки");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(12);
        configBox.setPadding(new Insets(5));

        Label studentLabel = new Label("Робочі параметри:");
        studentLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

        xValueLabel = new Label("Товщина фільтрів x: 0.0 см (0 шт)");
        xSlider = new Slider(0.0, 1.0, 0.0);
        xSlider.setShowTickMarks(true);
        xSlider.setMajorTickUnit(0.1);
        xSlider.setMinorTickCount(0);
        xSlider.setSnapToTicks(true);
        xSlider.valueProperty().addListener((o, ov, nv) -> {
            int plates = (int) Math.round(nv.doubleValue() * 10);
            xValueLabel.setText(String.format(Locale.US, "Товщина фільтрів x: %.1f см (%d шт)", nv.doubleValue(), plates));
            applyPhysicsSettings();
        });

        timeValueLabel = new Label("Час вимірювання t: 60 с");
        timeSlider = new Slider(30.0, 180.0, 60.0);
        timeSlider.setShowTickMarks(true);
        timeSlider.setMajorTickUnit(30.0);
        timeSlider.setSnapToTicks(true);
        timeSlider.valueProperty().addListener((o, ov, nv) -> {
            timeValueLabel.setText(String.format(Locale.US, "Час вимірювання t: %.0f с", nv.doubleValue()));
            if (!isMeasuring) {
                liveTimerLabel.setText(String.format(Locale.US, "Таймер: 0 / %.0f с", nv.doubleValue()));
            }
        });

        Label envSettingsLabel = new Label("Налаштування середовища:");
        envSettingsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b; -fx-padding: 10 0 0 0;");

        isotopeComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "Кобальт-60 (Co-60, E ≈ 1.25 МеВ)",
                "Цезій-137 (Cs-137, E ≈ 0.66 МеВ)",
                "Радій-226 (Ra-226, E ≈ 0.18 МеВ)"
        ));
        isotopeComboBox.getSelectionModel().selectFirst();
        isotopeComboBox.setMaxWidth(Double.MAX_VALUE);
        isotopeComboBox.setOnAction(e -> { data.clear(); applyPhysicsSettings(); updateStats(); });

        intensityLabel = new Label("Початкова інтенсивність (I_0): 15000 імп/хв");
        intensitySlider = new Slider(5000.0, 30000.0, 15000.0);
        intensitySlider.setShowTickMarks(true);
        intensitySlider.valueProperty().addListener((o, ov, nv) -> {
            intensityLabel.setText(String.format(Locale.US, "Початкова інтенсивність (I_0): %.0f імп/хв", nv.doubleValue()));
        });

        configBox.getChildren().addAll(
                studentLabel, xValueLabel, xSlider, timeValueLabel, timeSlider,
                new Separator(),
                envSettingsLabel, createInputGroup("Ізотоп:", isotopeComboBox),
                intensityLabel, intensitySlider
        );

        ScrollPane scrollParams = new ScrollPane(configBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(320);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        configPane.setContent(scrollParams);

        measureBtn = new Button("🔴 ПОЧАТИ ВИМІРЮВАННЯ");
        measureBtn.setStyle("-fx-background-color: #ff007f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
        measureBtn.setMaxWidth(Double.MAX_VALUE);
        measureBtn.setOnAction(e -> startSingleMeasurement());

        calcBtn = new Button("📝 ОБЧИСЛИТИ μ ТА ЕНЕРГІЮ");
        calcBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold;");
        calcBtn.setMaxWidth(Double.MAX_VALUE);
        calcBtn.setDisable(true);
        calcBtn.setOnAction(e -> executeCalculation());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ ЛАБИ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAutoMode());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear();
            idCounter = 1;
            updateStats();
            applyPhysicsSettings();
            calcBtn.setDisable(true);
        });

        leftPanel.getChildren().addAll(title, configPane, measureBtn, calcBtn, autoBtn, clearBtn);

        canvas = new GammaAttenuationCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(4);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-text-fill: #00e5ff; -fx-padding: 10; -fx-border-color: #00e5ff; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(240, 130);

        Label dashTitle = new Label("ДАНІ ЛІЧИЛЬНИКА");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        liveStepLabel = new Label("Статус: Очікування");
        liveStepLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");

        liveTimerLabel = new Label("Таймер: 0 / 60 с");
        liveTimerLabel.setStyle("-fx-text-fill: yellow;");

        liveCountLabel = new Label("N(x): ---");
        liveCountLabel.setStyle("-fx-text-fill: #00ffcc; -fx-font-size: 14px; -fx-font-weight: bold;");

        liveLnCountLabel = new Label("ln N(x): ---");
        liveLnCountLabel.setStyle("-fx-text-fill: #ff007f;");

        dash.getChildren().addAll(dashTitle, liveStepLabel, liveTimerLabel, liveCountLabel, liveLnCountLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, String> isoCol = new TableColumn<>("Ізотоп");
        isoCol.setCellValueFactory(new PropertyValueFactory<>("isotope"));
        TableColumn<Measurement, Double> xCol = new TableColumn<>("x (см)");
        xCol.setCellValueFactory(new PropertyValueFactory<>("x"));
        TableColumn<Measurement, Integer> tCol = new TableColumn<>("t (с)");
        tCol.setCellValueFactory(new PropertyValueFactory<>("timeSec"));
        TableColumn<Measurement, Integer> nCol = new TableColumn<>("N(x)");
        nCol.setCellValueFactory(new PropertyValueFactory<>("counts"));
        TableColumn<Measurement, Double> lnCol = new TableColumn<>("ln N(x)");
        lnCol.setCellValueFactory(new PropertyValueFactory<>("lnCounts"));

        table.getColumns().addAll(idCol, isoCol, xCol, tCol, nCol, lnCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(140);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        updateStats();
    }

    private void applyPhysicsSettings() {
        canvas.setPhysicsParameters(xSlider.getValue(), isMeasuring, new ArrayList<>(data));
    }

    private void setControlsDisable(boolean disable) {
        xSlider.setDisable(disable);
        timeSlider.setDisable(disable);
        isotopeComboBox.setDisable(disable);
        intensitySlider.setDisable(disable);
        measureBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        if (disable) calcBtn.setDisable(true);
    }

    private double getTrueMu() {
        int idx = isotopeComboBox.getSelectionModel().getSelectedIndex();
        if (idx == 0) return 0.65;
        if (idx == 1) return 1.20;
        return 2.50;
    }

    private void startSingleMeasurement() {
        double currentX = Math.round(xSlider.getValue() * 10.0) / 10.0;
        double targetTime = timeSlider.getValue();
        String currentIsotope = isotopeComboBox.getValue().split(" ")[0];

        for (Measurement m : data) {
            if (Math.abs(m.getX() - currentX) < 0.05 && m.getIsotope().equals(currentIsotope)) {
                showAlert("Увага", "Вимірювання для цієї товщини вже проведено!");
                return;
            }
        }

        isMeasuring = true;
        setControlsDisable(true);
        liveStepLabel.setText("Статус: ВИМІРЮВАННЯ...");
        liveStepLabel.setStyle("-fx-text-fill: #ff007f; -fx-font-weight: bold;");
        applyPhysicsSettings();

        measureTimer = new AnimationTimer() {
            long start = System.nanoTime();
            @Override
            public void handle(long now) {
                double elapsed = (now - start) / 1_000_000_000.0;
                double simRate = targetTime / 1.5;
                int simulatedSeconds = (int) (elapsed * simRate);

                if (simulatedSeconds < targetTime) {
                    liveTimerLabel.setText(String.format(Locale.US, "Таймер: %d / %.0f с", simulatedSeconds, targetTime));
                    int currentCount = (int) ((generateCount(currentX, targetTime) / targetTime) * simulatedSeconds);
                    liveCountLabel.setText(String.format("N(x): %d", currentCount));
                    liveLnCountLabel.setText(String.format(Locale.US, "ln N(x): %.3f", currentCount > 0 ? Math.log(currentCount) : 0));
                } else {
                    this.stop();
                    completeMeasurement(currentX, targetTime, currentIsotope);
                }
            }
        };
        measureTimer.start();
    }

    private int generateCount(double x, double timeSec) {
        double mu = getTrueMu();
        double i0PerMin = intensitySlider.getValue();
        double i0PerSec = i0PerMin / 60.0;
        double expectedRate = i0PerSec * Math.exp(-mu * x);
        double expectedTotal = expectedRate * timeSec;
        double noise = (Math.random() - 0.5) * Math.sqrt(expectedTotal) * 2;
        expectedTotal += noise;

        if (expectedTotal < 1.0 * timeSec) {
            expectedTotal = 1.0 * timeSec + (Math.random() - 0.5) * Math.sqrt(timeSec);
        }

        return Math.max(1, (int) Math.round(expectedTotal));
    }

    private void completeMeasurement(double x, double timeSec, String isotope) {
        isMeasuring = false;
        liveStepLabel.setText("Статус: ГОТОВО");
        liveStepLabel.setStyle("-fx-text-fill: #00ffcc; -fx-font-weight: bold;");
        liveTimerLabel.setText(String.format(Locale.US, "Таймер: %.0f / %.0f с", timeSec, timeSec));

        int finalCount = generateCount(x, timeSec);
        double lnCount = Math.log(finalCount);

        liveCountLabel.setText(String.format("N(x): %d", finalCount));
        liveLnCountLabel.setText(String.format(Locale.US, "ln N(x): %.3f", lnCount));

        data.add(new Measurement(idCounter++, isotope, x, (int)timeSec, finalCount, Math.round(lnCount * 1000.0) / 1000.0));

        updateStats();
        applyPhysicsSettings();
        setControlsDisable(false);

        if (data.size() >= 3) calcBtn.setDisable(false);
    }

    private void executeCalculation() {
        if (data.size() < 3) {
            showAlert("Помилка", "Зберіть щонайменше 3 точки для побудови прямої lnN=f(x).");
            return;
        }

        int n = data.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;

        for (Measurement m : data) {
            double x = m.getX();
            double normCount = m.getCounts() * (60.0 / m.getTimeSec());
            double y = Math.log(normCount);

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        double muExp = -slope;

        double trueMu = getTrueMu();
        double error = Math.abs(muExp - trueMu) / trueMu * 100.0;

        double energyExp = 1.3 / Math.max(0.1, muExp);

        String conclusion = String.format(Locale.US,
                "ОБРОБКА РЕЗУЛЬТАТІВ:\n" +
                        "1. Рівняння: ln N(x) = ln N_0 - μx. За нахилом прямої знайдено: μ = %.3f см⁻¹.\n" +
                        "2. Відносна похибка визначення коефіцієнта (відносно табличного %.2f): ε = %.1f %%.\n" +
                        "3. Залежність μ=f(E) для свинцю дає оціночну енергію γ-квантів: E ≈ %.2f МеВ.\n" +
                        "ВИСНОВОК: Експоненціальний закон загасання γ-променів експериментально підтверджено.",
                muExp, trueMu, error, energyExp
        );
        finalResultLabel.setText(conclusion);
        calcBtn.setDisable(true);
    }

    private void startAutoMode() {
        data.clear();
        idCounter = 1;
        updateStats();
        calcBtn.setDisable(true);
        autoQueue.clear();

        for (int i = 0; i <= 10; i++) {
            autoQueue.add(i / 10.0);
        }

        processNextAuto();
    }

    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            liveStepLabel.setText("АВТОПРОХОДЖЕННЯ ЗАВЕРШЕНО");
            liveStepLabel.setStyle("-fx-text-fill: #a3e635; -fx-font-weight: bold;");
            setControlsDisable(false);
            executeCalculation();
            return;
        }

        double nextX = autoQueue.poll();
        xSlider.setValue(nextX);
        timeSlider.setValue(60.0);

        Platform.runLater(this::startSingleMeasurement);

        autoTimer = new AnimationTimer() {
            long start = System.nanoTime();
            @Override
            public void handle(long now) {
                if (!isMeasuring && ((now - start) / 1_000_000_000.0) > 1.8) {
                    this.stop();
                    processNextAuto();
                }
            }
        };
        autoTimer.start();
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

        if (data.size() < 3) {
            finalResultLabel.setText(String.format("СТАТУС: Зібрано %d точок. Потрібно мінімум 3 для розрахунку μ.", data.size()));
        } else {
            finalResultLabel.setText(String.format("СТАТУС: Зібрано %d точок. Натисніть 'ОБЧИСЛИТИ μ ТА ЕНЕРГІЮ'.", data.size()));
        }
    }
}