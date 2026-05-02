package dev.ua._klaidi4_.physics.level6.lab6_1.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level6.lab6_1.model.Measurement;
import dev.ua._klaidi4_.physics.level6.lab6_1.view.AlphaDecayCanvas;
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

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class LabController61 extends BaseLabController {

    public static class IsotopeDef {
        public String name;
        public double energy;
        public double windowLoss;
        public IsotopeDef(String n, double e, double w) { this.name = n; this.energy = e; this.windowLoss = w; }
        @Override public String toString() { return name; }
    }

    private AlphaDecayCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField xField;
    private TextField tField;
    private TextField pressureField;
    private ComboBox<IsotopeDef> sourceCombo;
    private Button measureBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveTimeLabel;
    private Label liveCountsLabel;
    private AnimationTimer measurementTimer;
    private long startTime;
    private Queue<Double> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;
    private final double[] tableX = {0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8};
    private final double[] tableN = {1471, 1465, 1440, 1350, 1100, 600, 300, 150, 80, 40, 20, 10, 5, 2, 1, 0, 0, 0, 0};
    private final double TABLE_R0 = 0.468;
    private final double PU_FULL_ENERGY = 5.5;

    public LabController61() {
        initUI();
        updateCanvasPreview();
    }

    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        if (measurementTimer != null) measurementTimer.stop();
        isAutoRunning = false;
        autoQueue.clear();
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(320);
        leftPanel.setMinWidth(320);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Управління (Лаб 6-1)");
        title.setFont(Font.font("System", FontWeight.BOLD, 17));

        TitledPane envPane = new TitledPane();
        envPane.setText("Налаштування середовища");
        envPane.setCollapsible(false);
        VBox envBox = new VBox(10);
        envBox.setPadding(new Insets(5));

        pressureField = new TextField("1.0");
        pressureField.setStyle("-fx-background-color: #e0f2fe; -fx-border-color: #38bdf8; -fx-border-radius: 4;");
        pressureField.textProperty().addListener((obs, ov, nv) -> updateCanvasPreview());
        envBox.getChildren().add(createInputGroup("Тиск повітря P (атм):", pressureField));
        envPane.setContent(envBox);

        TitledPane configPane = new TitledPane();
        configPane.setText("Параметри вимірювання");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(12);
        configBox.setPadding(new Insets(5));

        ObservableList<IsotopeDef> isotopes = FXCollections.observableArrayList(
                new IsotopeDef("Pu-239 (Дані з методички)", 5.5, 4.21),
                new IsotopeDef("Pu-239 (Відкрите джерело)", 5.5, 0.0),
                new IsotopeDef("Po-210 (Полоній)", 5.30, 0.0),
                new IsotopeDef("Am-241 (Америцій)", 5.48, 0.0),
                new IsotopeDef("U-238 (Уран)", 4.20, 0.0)
        );

        sourceCombo = new ComboBox<>(isotopes);
        sourceCombo.getSelectionModel().selectFirst();
        sourceCombo.setMaxWidth(Double.MAX_VALUE);
        sourceCombo.setOnAction(e -> { data.clear(); updateStats(); updateCanvasPreview(); });

        xField = new TextField("0.0");
        tField = new TextField("100");

        xField.textProperty().addListener((obs, ov, nv) -> updateCanvasPreview());

        configBox.getChildren().addAll(
                createInputGroup("Джерело α-випромінювання:", sourceCombo),
                createInputGroup("Відстань до детектора x (см):", xField),
                createInputGroup("Час вимірювання t (с):", tField)
        );
        configPane.setContent(configBox);

        measureBtn = new Button("▶ ВИМІРЯТИ ІМПУЛЬСИ");
        measureBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        measureBtn.setMaxWidth(Double.MAX_VALUE);
        measureBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ (Графік N(x))");
        autoBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAuto());

        clearBtn = new Button("🗑 ОЧИСТИТИ ДАНІ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear();
            idCounter = 1;
            updateStats();
            updateCanvasPreview();
            liveCountsLabel.setText("N = 0 імп");
        });

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, envPane, configPane, measureBtn, autoBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new AlphaDecayCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(10, 20, 10, 0.9); -fx-padding: 10; -fx-border-color: #00ffcc; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(220, 80);

        Label dashTitle = new Label("ДЕТЕКТОР (ПСО-2,4)");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveTimeLabel = new Label("t = 0.0 с");
        liveTimeLabel.setStyle("-fx-text-fill: #ffffff;");
        liveCountsLabel = new Label("N = 0 імп");
        liveCountsLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        liveCountsLabel.setStyle("-fx-text-fill: #00ffcc;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveTimeLabel, liveCountsLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, String> srcCol = new TableColumn<>("Препарат");
        srcCol.setCellValueFactory(new PropertyValueFactory<>("sourceName"));
        TableColumn<Measurement, Double> pCol = new TableColumn<>("P (атм)");
        pCol.setCellValueFactory(new PropertyValueFactory<>("pressure"));
        TableColumn<Measurement, Double> xCol = new TableColumn<>("x (см)");
        xCol.setCellValueFactory(new PropertyValueFactory<>("x"));
        TableColumn<Measurement, Integer> nCol = new TableColumn<>("N (імп)");
        nCol.setCellValueFactory(new PropertyValueFactory<>("counts"));

        table.getColumns().addAll(idCol, srcCol, pCol, xCol, nCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);
    }

    private double calculateR0() {
        try {
            IsotopeDef iso = sourceCombo.getValue();
            double p = Double.parseDouble(pressureField.getText().replace(',', '.'));
            if(p <= 0) p = 1.0;
            double effectiveE = iso.energy - iso.windowLoss;
            if(effectiveE <= 0) effectiveE = 0.1;
            return (0.318 * Math.pow(effectiveE, 1.5)) / p;
        } catch (Exception e) {
            return TABLE_R0;
        }
    }

    private void updateCanvasPreview() {
        if (isAutoRunning || measureBtn.isDisabled()) return;
        try {
            double x = Double.parseDouble(xField.getText().replace(',', '.'));
            canvas.setPhysicsParameters(x, false, data, calculateR0());
        } catch (NumberFormatException ignored) {}
    }

    private void setControlsDisable(boolean disable) {
        measureBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        xField.setDisable(disable);
        tField.setDisable(disable);
        pressureField.setDisable(disable);
        sourceCombo.setDisable(disable);
    }

    private void startManual() {
        try {
            double x = Double.parseDouble(xField.getText().replace(',', '.'));
            int t = Integer.parseInt(tField.getText());
            Double.parseDouble(pressureField.getText().replace(',', '.'));
            isAutoRunning = false;
            runMeasurement(x, t);
        } catch (Exception e) {
            showAlert("Помилка", "Перевірте правильність введених даних.");
        }
    }

    private void startAuto() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        double currentR0 = calculateR0();
        double step = currentR0 / 15.0;
        if(step < 0.05) step = 0.05;
        if(step > 0.5) step = 0.5;

        for (double px = 0.0; px <= currentR0 * 1.5; px += step) {
            autoQueue.add(Math.round(px * 100.0) / 100.0);
        }

        isAutoRunning = true;
        processNextAuto();
    }

    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoRunning = false;
            liveStatusLabel.setText("СИСТЕМА: ГОТОВО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            setControlsDisable(false);
            updateStats();
            return;
        }

        double nextX = autoQueue.poll();
        xField.setText(String.format(Locale.US, "%.2f", nextX));
        runMeasurement(nextX, 100);
    }

    private double getExpectedRate(double x, double r0) {
        double mappedX = x * (TABLE_R0 / r0);

        if (mappedX <= 0) return tableN[0];
        if (mappedX >= 1.8) return 0;

        for (int i = 0; i < tableX.length - 1; i++) {
            if (mappedX >= tableX[i] && mappedX <= tableX[i+1]) {
                double t = (mappedX - tableX[i]) / (tableX[i+1] - tableX[i]);
                return tableN[i] + t * (tableN[i+1] - tableN[i]);
            }
        }
        return 0;
    }

    private void runMeasurement(double xCm, int tSec) {
        setControlsDisable(true);
        liveStatusLabel.setText("СИСТЕМА: ВИМІРЮВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: red;");

        double r0 = calculateR0();
        double expectedRate = getExpectedRate(xCm, r0);
        double scaleT = tSec / 100.0;
        double noise = 0;

        final int finalCounts = (int) Math.round((expectedRate + noise) * scaleT);
        canvas.setPhysicsParameters(xCm, true, data, r0);
        startTime = System.nanoTime();
        double animDuration = 0.5;

        measurementTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double elapsed = (now - startTime) / 1_000_000_000.0;
                double progress = Math.min(elapsed / animDuration, 1.0);

                liveTimeLabel.setText(String.format(Locale.US, "t = %.1f с", progress * tSec));
                liveCountsLabel.setText(String.format("N = %d імп", (int)(finalCounts * progress)));

                if (progress >= 1.0) {
                    this.stop();
                    finishMeasurement(xCm, tSec, finalCounts);
                }
            }
        };
        measurementTimer.start();
    }

    private void finishMeasurement(double xCm, int tSec, int counts) {
        liveStatusLabel.setText("СИСТЕМА: ЗАПИС");
        liveStatusLabel.setStyle("-fx-text-fill: yellow;");
        liveTimeLabel.setText(String.format(Locale.US, "t = %d.0 с", tSec));
        liveCountsLabel.setText(String.format("N = %d імп", counts));

        try {
            double p = Double.parseDouble(pressureField.getText().replace(',', '.'));
            Measurement m = new Measurement(idCounter++, sourceCombo.getValue().name, p, xCm, counts);

            data.removeIf(existing -> Math.abs(existing.getX() - xCm) < 0.001);
            data.add(m);
            canvas.setPhysicsParameters(xCm, false, data, calculateR0());
        } catch (Exception ignored){}

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(50); Platform.runLater(this::processNextAuto); }
                catch (InterruptedException ignored) {}
            }).start();
        } else {
            updateStats();
            setControlsDisable(false);
            liveStatusLabel.setText("СИСТЕМА: ОЧІКУВАННЯ");
            liveStatusLabel.setStyle("-fx-text-fill: yellow;");
        }
    }

    private void updateStats() {
        if (data.isEmpty() || isAutoRunning) {
            finalResultLabel.setText("Обробка результатів: Очікування завершення серії...");
            return;
        }

        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }

        data.sort(Comparator.comparingDouble(Measurement::getX));
        double maxN = data.get(0).getCounts();
        double targetN = maxN / 2.0;

        double expR0 = 0.0;
        double expRe = 0.0;

        for (int i = 0; i < data.size() - 1; i++) {
            if (data.get(i).getCounts() >= targetN && data.get(i+1).getCounts() < targetN) {
                double n1 = data.get(i).getCounts();
                double n2 = data.get(i+1).getCounts();
                double x1 = data.get(i).getX();
                double x2 = data.get(i+1).getX();

                expR0 = x1 + (x2 - x1) * ((targetN - n1) / (n2 - n1));
                double slope = (n2 - n1) / (x2 - x1);
                expRe = expR0 - (targetN / slope);
                break;
            }
        }

        if (expR0 > 0) {
            IsotopeDef iso = sourceCombo.getValue();
            double p = Double.parseDouble(pressureField.getText().replace(',', '.'));

            double energyExp = Math.pow((expR0 * p) / 0.318, 2.0 / 3.0);

            StringBuilder conc = new StringBuilder();
            conc.append(String.format(Locale.US, "ОБРОБКА РЕЗУЛЬТАТІВ:\n1. За графіком знайдено точку перегину А. Середній пробіг R0 ≈ %.3f см.\n", expR0));
            conc.append(String.format(Locale.US, "   Екстрапольований пробіг Re (за дотичною) ≈ %.3f см.\n", expRe));
            conc.append(String.format(Locale.US, "2. За формулою E = (R0 * P / 0.318)^(2/3) розраховано енергію частинок: E_експ ≈ %.2f МеВ.\n", energyExp));

            if (iso.windowLoss > 0 && p == 1.0) {
                double lostEnergy = PU_FULL_ENERGY - energyExp;
                conc.append(String.format(Locale.US, "3. ВІДПОВІДЬ НА ПИТАННЯ №7: Початкова енергія Pu-239 дорівнює %.1f МеВ. Різниця (%.1f - %.2f = %.2f МеВ) — це енергія, яка була витрачена частинками у вікні лічильника та захисному шарі препарату.\n", PU_FULL_ENERGY, PU_FULL_ENERGY, energyExp, lostEnergy));
            } else {
                double error = Math.abs(energyExp - iso.energy) / iso.energy * 100.0;
                conc.append(String.format(Locale.US, "3. Теоретичне значення початкової енергії для %s: E_теор = %.2f МеВ. Похибка: %.1f %%.\n", iso.name, iso.energy, error));
                if (p != 1.0) {
                    conc.append(String.format(Locale.US, "4. ВИСНОВОК: Зміна тиску повітря (P=%.2f атм) змінює довжину пробігу R0, але розрахована початкова енергія частинок залишається незмінною.", p));
                }
            }

            finalResultLabel.setText(conc.toString());
        } else {
            finalResultLabel.setText("ОБРОБКА РЕЗУЛЬТАТІВ: Зробіть більше вимірів на ділянці різкого спаду кількості імпульсів (для пошуку R0).");
        }
    }
}