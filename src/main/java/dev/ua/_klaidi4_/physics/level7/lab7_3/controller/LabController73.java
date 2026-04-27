package dev.ua._klaidi4_.physics.level7.lab7_3.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level7.lab7_3.enums.LiquidType;
import dev.ua._klaidi4_.physics.level7.lab7_3.model.Measurement;
import dev.ua._klaidi4_.physics.level7.lab7_3.view.StokesCanvas;
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
import javafx.animation.AnimationTimer;

import java.util.LinkedList;
import java.util.Queue;

public class LabController73 extends BaseLabController {

    private StokesCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;

    private ComboBox<LiquidType> liquidComboBox;
    private TextField lengthField, densityField;
    private Slider radiusSlider;
    private Button startBtn, autoBtn, clearBtn;
    private Label liveStatusLabel, liveTimeLabel;

    private Queue<AutoTestParam> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;
    private AnimationTimer uiTimer;

    private static class AutoTestParam {
        LiquidType liq; double r, l, density;
        AutoTestParam(LiquidType liq, double r, double l, double density) {
            this.liq = liq; this.r = r; this.l = l; this.density = density;
        }
    }

    public LabController73() {
        initUI();
        applyPhysicsSettings();
    }

    @Override
    public void shutdown() {
        if (canvas != null) canvas.resetSystem();
        if (uiTimer != null) uiTimer.stop();
        isAutoRunning = false;
        autoQueue.clear();
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 7-3)");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри досліду");
        labPane.setCollapsible(false);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        liquidComboBox = new ComboBox<>(FXCollections.observableArrayList(LiquidType.values()));
        liquidComboBox.getSelectionModel().selectFirst();
        liquidComboBox.setMaxWidth(Double.MAX_VALUE);
        liquidComboBox.setOnAction(e -> applyPhysicsSettings());

        densityField = new TextField("7800");
        densityField.textProperty().addListener((o, oldVal, newVal) -> applyPhysicsSettings());

        lengthField = new TextField("0.3");
        lengthField.textProperty().addListener((o, oldVal, newVal) -> applyPhysicsSettings());

        paramsBox.getChildren().addAll(
                createInputGroup("Досліджувана рідина:", liquidComboBox),
                createInputGroup("Густина кульки ρ (кг/м³):", densityField),
                createInputGroup("Відстань між мітками l (м):", lengthField)
        );
        labPane.setContent(paramsBox);

        TitledPane physicsPane = new TitledPane();
        physicsPane.setText("Розмір кульки");
        physicsPane.setCollapsible(false);

        VBox physBox = new VBox(5);
        physBox.setPadding(new Insets(5));

        Label radiusLabel = new Label("Радіус кульки r: 2.0 мм");
        radiusSlider = new Slider(1.0, 3.5, 2.0);
        radiusSlider.setShowTickMarks(true);
        radiusSlider.setShowTickLabels(true);
        radiusSlider.setMajorTickUnit(0.5);
        radiusSlider.valueProperty().addListener((o, oldVal, newVal) -> {
            radiusLabel.setText(String.format("Радіус кульки r: %.1f мм", newVal.doubleValue()));
            applyPhysicsSettings();
        });

        physBox.getChildren().addAll(radiusLabel, radiusSlider);
        physicsPane.setContent(physBox);

        startBtn = new Button("▶ КИНУТИ КУЛЬКУ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ ЛАБИ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAuto());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear();
            idCounter = 1;
            updateStats();
        });

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, labPane, physicsPane, startBtn, autoBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new StokesCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));
        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(200, 60);
        Label dashTitle = new Label("СЕКУНДОМІР");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ГОТОВО");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveTimeLabel = new Label("t = 0.00 с");
        liveTimeLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        liveTimeLabel.setStyle("-fx-text-fill: #00ff00;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveTimeLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        String[] cols = {"№", "Рідина", "r (мм)", "l (м)", "t (с)", "η експ (Па·с)", "η теор (Па·с)"};
        String[] props = {"id", "liquid", "radius", "l", "time", "expViscosity", "theorViscosity"};

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
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        uiTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (canvas.isTimerStarted()) {
                    liveTimeLabel.setText(String.format("t = %.2f с", canvas.getElapsedTime()));
                }
            }
        };
        uiTimer.start();

        canvas.setOnMeasurementCompleted(this::onMeasurementTick);
        updateStats();
    }

    private void applyPhysicsSettings() {
        if (isAutoRunning) return;
        try {
            LiquidType liq = liquidComboBox.getValue();
            double r = radiusSlider.getValue();
            double l = Double.parseDouble(lengthField.getText());
            double rho = Double.parseDouble(densityField.getText());
            canvas.setPhysicsParameters(liq, r, l, rho);
            liveTimeLabel.setText("t = 0.00 с");
        } catch (NumberFormatException ignored) {}
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        liquidComboBox.setDisable(disable);
        radiusSlider.setDisable(disable);
        lengthField.setDisable(disable);
        densityField.setDisable(disable);
    }

    private void startManual() {
        try {
            Double.parseDouble(lengthField.getText());
            Double.parseDouble(densityField.getText());
            isAutoRunning = false;
            applyPhysicsSettings();

            setControlsDisable(true);
            liveStatusLabel.setText("Статус: ПАДІННЯ");
            liveStatusLabel.setStyle("-fx-text-fill: red;");
            canvas.startSimulation();
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа.");
        }
    }

    private void startAuto() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        autoQueue.add(new AutoTestParam(LiquidType.GLYCERIN, 1.5, 0.3, 7800));
        autoQueue.add(new AutoTestParam(LiquidType.GLYCERIN, 2.5, 0.3, 7800));
        autoQueue.add(new AutoTestParam(LiquidType.CASTOR_OIL, 2.0, 0.3, 7800));
        autoQueue.add(new AutoTestParam(LiquidType.CASTOR_OIL, 3.0, 0.3, 7800));

        isAutoRunning = true;
        processNextAuto();
    }

    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoRunning = false;
            liveStatusLabel.setText("Статус: ГОТОВО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            setControlsDisable(false);
            applyPhysicsSettings();
            return;
        }

        setControlsDisable(true);
        AutoTestParam param = autoQueue.poll();

        liquidComboBox.setValue(param.liq);
        radiusSlider.setValue(param.r);
        lengthField.setText(String.valueOf(param.l));
        densityField.setText(String.valueOf(param.density));

        canvas.setPhysicsParameters(param.liq, param.r, param.l, param.density);
        liveStatusLabel.setText("Статус: АВТО-ПАДІННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: red;");
        canvas.startSimulation();
    }

    private void onMeasurementTick() {
        double t = canvas.getElapsedTime();
        liveTimeLabel.setText(String.format("t = %.2f с", t));
        liveStatusLabel.setText("Статус: ЗАПИС");
        liveStatusLabel.setStyle("-fx-text-fill: yellow;");

        LiquidType liq = liquidComboBox.getValue();
        double r = radiusSlider.getValue();
        double l = Double.parseDouble(lengthField.getText());
        double rho = Double.parseDouble(densityField.getText());
        double rMeters = r / 1000.0;
        double etaExp = (2 * rMeters * rMeters * 9.81 * (rho - liq.getDensity()) * t) / (9 * l);

        Measurement m = new Measurement(
                idCounter++, liq.getName(), Math.round(r * 10.0) / 10.0, l,
                Math.round(t * 100.0) / 100.0,
                Math.round(etaExp * 1000.0) / 1000.0,
                liq.getTheoreticalViscosity()
        );
        data.add(m);
        updateStats();

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                Platform.runLater(this::processNextAuto);
            }).start();
        } else {
            setControlsDisable(false);
            liveStatusLabel.setText("Статус: ГОТОВО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
        }
    }

    private void updateStats() {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів експерименту: Очікування вимірювань...");
            return;
        }
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }
        StringBuilder sb = new StringBuilder("Обробка результатів експерименту та їх аналіз:\n");
        java.util.Map<String, java.util.List<Measurement>> grouped = new java.util.HashMap<>();

        for (Measurement m : data) {
            grouped.computeIfAbsent(m.getLiquid(), k -> new java.util.ArrayList<>()).add(m);
        }

        for (java.util.Map.Entry<String, java.util.List<Measurement>> entry : grouped.entrySet()) {
            String liquidName = entry.getKey();
            java.util.List<Measurement> measList = entry.getValue();

            double sumEta = 0;
            for (Measurement m : measList) {
                sumEta += m.getExpViscosity();
            }
            double avgEta = sumEta / measList.size();

            double sumAbsErr = 0;
            for (Measurement m : measList) {
                sumAbsErr += Math.abs(m.getExpViscosity() - avgEta);
            }
            double deltaEta = measList.size() > 0 ? sumAbsErr / measList.size() : 0;
            double relErr = (avgEta > 0) ? (deltaEta / avgEta) * 100.0 : 0;

            sb.append(String.format("Рідина: %s\n", liquidName));
            sb.append(String.format("1. Середнє значення коефіцієнта в'язкості: <η> = %.4f Па·с\n", avgEta));
            sb.append(String.format("2. Середня абсолютна похибка: Δη = %.4f Па·с\n", deltaEta));
            sb.append(String.format("   Відносна похибка: ε = %.1f %%\n", relErr));
            sb.append(String.format("3. Кінцевий результат: η = %.4f ± %.4f Па·с\n\n", avgEta, deltaEta));
        }

        finalResultLabel.setText(sb.toString().trim());
    }
}