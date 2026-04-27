package dev.ua._klaidi4_.physics.level7.lab7_1.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level7.lab7_1.model.Measurement;
import dev.ua._klaidi4_.physics.level7.lab7_1.view.ThermodynamicsCanvas;
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

import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class LabController71 extends BaseLabController {

    private ThermodynamicsCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> gasComboBox;
    private Slider pressureSlider;
    private Slider tempSlider;
    private Slider heatRateSlider;
    private Button measureBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStepLabel;
    private Label manoLabel;
    private Label h1SavedLabel;
    private Label h2SavedLabel;
    private boolean isMeasuring = false;
    private AnimationTimer measureTimer;
    private AnimationTimer autoTimer;
    private Queue<Integer> autoQueue = new LinkedList<>();

    public LabController71() {
        initUI();
        canvas.updateState(0.0, false, 1.0);
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

        Label title = new Label("Система управління (Лаб 7-1)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane configPane = new TitledPane();
        configPane.setText("Налаштування середовища");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(12);
        configBox.setPadding(new Insets(5));

        gasComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "Повітря (Двоатомний, γ ≈ 1.40)",
                "Аргон (Одноатомний, γ ≈ 1.67)",
                "Вуглекислий газ (Багатоатомний, γ ≈ 1.30)"
        ));
        gasComboBox.getSelectionModel().selectFirst();
        gasComboBox.setMaxWidth(Double.MAX_VALUE);
        gasComboBox.setOnAction(e -> {
            data.clear(); idCounter = 1; updateStats();
        });

        Label pressureLabel = new Label("Атмосферний тиск P0: 101.3 кПа");
        pressureSlider = new Slider(98.0, 104.0, 101.3);
        pressureSlider.setShowTickMarks(true);
        pressureSlider.valueProperty().addListener((o, ov, nv) -> {
            pressureLabel.setText(String.format(Locale.US, "Атмосферний тиск P0: %.1f кПа", nv.doubleValue()));
        });

        Label tempLabel = new Label("Кімнатна температура T0: 20 °C");
        tempSlider = new Slider(15.0, 30.0, 20.0);
        tempSlider.setShowTickMarks(true);
        tempSlider.valueProperty().addListener((o, ov, nv) -> {
            tempLabel.setText(String.format(Locale.US, "Кімнатна температура T0: %.1f °C", nv.doubleValue()));
        });

        Label heatRateLabel = new Label("Швидкість теплообміну: 1.0x");
        heatRateSlider = new Slider(0.5, 2.0, 1.0);
        heatRateSlider.setShowTickMarks(true);
        heatRateSlider.setMajorTickUnit(0.5);
        heatRateSlider.valueProperty().addListener((o, ov, nv) -> {
            heatRateLabel.setText(String.format(Locale.US, "Швидкість теплообміну: %.1fx", nv.doubleValue()));
        });

        configBox.getChildren().addAll(
                createInputGroup("Досліджуваний газ:", gasComboBox),
                pressureLabel, pressureSlider,
                tempLabel, tempSlider,
                heatRateLabel, heatRateSlider
        );

        ScrollPane scrollParams = new ScrollPane(configBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(250);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        configPane.setContent(scrollParams);

        measureBtn = new Button("🔴 ОДНЕ ВИМІРЮВАННЯ");
        measureBtn.setStyle("-fx-background-color: #ff007f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
        measureBtn.setMaxWidth(Double.MAX_VALUE);
        measureBtn.setOnAction(e -> startSingleMeasurement());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ ЛАБИ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAutoMode());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear(); idCounter = 1; updateStats();
            h1SavedLabel.setText("h1 (поч): ---");
            h2SavedLabel.setText("h2 (кін): ---");
            liveStepLabel.setText("Статус: Очікування");
            liveStepLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
        });

        leftPanel.getChildren().addAll(title, configPane, measureBtn, autoBtn, clearBtn);

        canvas = new ThermodynamicsCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(4);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-text-fill: #00e5ff; -fx-padding: 10; -fx-border-color: #00e5ff; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(260, 140);

        Label dashTitle = new Label("МАНОМЕТР (мм вод. ст.)");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        liveStepLabel = new Label("Статус: Очікування");
        liveStepLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");

        manoLabel = new Label("h поточне: 0.0");
        manoLabel.setStyle("-fx-text-fill: #00ffcc; -fx-font-size: 14px; -fx-font-weight: bold;");

        h1SavedLabel = new Label("h1 (поч): ---");
        h1SavedLabel.setStyle("-fx-text-fill: #ff007f;");

        h2SavedLabel = new Label("h2 (кін): ---");
        h2SavedLabel.setStyle("-fx-text-fill: #00e5ff;");

        dash.getChildren().addAll(dashTitle, liveStepLabel, manoLabel, new Separator(), h1SavedLabel, h2SavedLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, Double> h1Col = new TableColumn<>("h1 (мм)");
        h1Col.setCellValueFactory(new PropertyValueFactory<>("h1"));
        TableColumn<Measurement, Double> h2Col = new TableColumn<>("h2 (мм)");
        h2Col.setCellValueFactory(new PropertyValueFactory<>("h2"));
        TableColumn<Measurement, Double> gCol = new TableColumn<>("γ (h1 / (h1-h2))");
        gCol.setCellValueFactory(new PropertyValueFactory<>("gamma"));

        table.getColumns().addAll(idCol, h1Col, h2Col, gCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(140);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        AnimationTimer uiTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                manoLabel.setText(String.format(Locale.US, "h поточне: %.1f", canvas.getCurrentPressure()));
            }
        };
        uiTimer.start();

        updateStats();
    }

    private double getTrueGamma() {
        int idx = gasComboBox.getSelectionModel().getSelectedIndex();
        if (idx == 0) return 1.40;
        if (idx == 1) return 1.67;
        return 1.30;
    }

    private void setControlsDisable(boolean disable) {
        gasComboBox.setDisable(disable);
        pressureSlider.setDisable(disable);
        tempSlider.setDisable(disable);
        heatRateSlider.setDisable(disable);
        measureBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
    }

    private void startSingleMeasurement() {
        isMeasuring = true;
        setControlsDisable(true);

        h1SavedLabel.setText("h1 (поч): ---");
        h2SavedLabel.setText("h2 (кін): ---");

        double trueGamma = getTrueGamma();

        double tempFactor = tempSlider.getValue() / 20.0;
        double targetH1 = (100.0 + Math.random() * 40.0) * tempFactor;
        double pressureNoise = (101.3 / pressureSlider.getValue());
        double expectedH2 = targetH1 * (1.0 - 1.0 / trueGamma);
        double targetH2 = expectedH2 * (1.0 + (Math.random() - 0.5) * 0.05 * pressureNoise);
        double heatRate = heatRateSlider.getValue();

        measureTimer = new AnimationTimer() {
            long start = System.nanoTime();

            @Override
            public void handle(long now) {
                double elapsed = (now - start) / 1_000_000_000.0;
                double scaledElapsed = elapsed * heatRate;

                if (scaledElapsed < 2.0) {
                    liveStepLabel.setText("Статус: Накачування та ізохорне охолодження...");
                    liveStepLabel.setStyle("-fx-text-fill: #ffeb3b;");
                    canvas.updateState(targetH1, false, heatRate);

                } else if (scaledElapsed < 2.5) {
                    if (h1SavedLabel.getText().contains("---")) {
                        h1SavedLabel.setText(String.format(Locale.US, "h1 (поч): %.1f", targetH1));
                    }
                    liveStepLabel.setText("Статус: Адіабатичне розширення (кран відкрито)");
                    liveStepLabel.setStyle("-fx-text-fill: #00ffcc;");
                    canvas.updateState(0.0, true, heatRate);

                } else if (scaledElapsed < 4.5) {
                    liveStepLabel.setText("Статус: Ізохорне нагрівання...");
                    liveStepLabel.setStyle("-fx-text-fill: #ff9800;");
                    canvas.updateState(targetH2, false, heatRate);

                } else {
                    this.stop();
                    completeMeasurement(targetH1, targetH2);
                }
            }
        };
        measureTimer.start();
    }

    private void completeMeasurement(double h1, double h2) {
        isMeasuring = false;

        h1 = Math.round(h1 * 10.0) / 10.0;
        h2 = Math.round(h2 * 10.0) / 10.0;

        h2SavedLabel.setText(String.format(Locale.US, "h2 (кін): %.1f", h2));
        liveStepLabel.setText("Статус: Вимірювання завершено");
        liveStepLabel.setStyle("-fx-text-fill: #a3e635;");

        double calcGamma = h1 / (h1 - h2);

        data.add(new Measurement(idCounter++, h1, h2, Math.round(calcGamma * 1000.0) / 1000.0));

        updateStats();
        setControlsDisable(false);
    }

    private void startAutoMode() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        for (int i = 0; i < 3; i++) {
            autoQueue.add(i);
        }

        processNextAuto();
    }

    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            liveStepLabel.setText("АВТОПРОХОДЖЕННЯ ЗАВЕРШЕНО");
            liveStepLabel.setStyle("-fx-text-fill: #a3e635; -fx-font-weight: bold;");
            setControlsDisable(false);
            return;
        }

        autoQueue.poll();
        Platform.runLater(this::startSingleMeasurement);

        autoTimer = new AnimationTimer() {
            long start = System.nanoTime();
            @Override
            public void handle(long now) {
                double elapsed = (now - start) / 1_000_000_000.0;
                double heatRate = heatRateSlider.getValue();

                if (!isMeasuring && elapsed > (5.0 / heatRate)) {
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
            finalResultLabel.setText(String.format("СТАТУС: Зібрано %d вимірів. Потрібно мінімум 3 для розрахунку середнього γ.", data.size()));
            return;
        }

        double sumGamma = 0;
        for (Measurement m : data) {
            sumGamma += m.getGamma();
        }
        double avgGamma = sumGamma / data.size();

        double trueGamma = getTrueGamma();
        double error = Math.abs(avgGamma - trueGamma) / trueGamma * 100.0;

        String gasName = gasComboBox.getValue().split(" ")[0];

        String conclusion = String.format(Locale.US,
                "ОБРОБКА РЕЗУЛЬТАТІВ (АВТОМАТИЧНО):\n" +
                        "1. Досліджуваний газ: %s (Теоретичне γ ≈ %.2f).\n" +
                        "2. Робоча формула: γ = h1 / (h1 - h2).\n" +
                        "3. Середнє розраховане значення з %d дослідів: γ_експ = %.3f.\n" +
                        "ВИСНОВОК: Відносна похибка становить ε = %.1f %%. Метод Клемана-Дезорма дає точні результати при швидкому розширенні газу.",
                gasName, trueGamma, data.size(), avgGamma, error
        );
        finalResultLabel.setText(conclusion);
    }
}