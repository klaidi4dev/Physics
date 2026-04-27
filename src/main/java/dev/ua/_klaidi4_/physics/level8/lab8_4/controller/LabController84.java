package dev.ua._klaidi4_.physics.level8.lab8_4.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level8.lab8_4.enums.DiodeType;
import dev.ua._klaidi4_.physics.level8.lab8_4.model.Measurement;
import dev.ua._klaidi4_.physics.level8.lab8_4.view.DiodeCanvas;
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
import java.util.Queue;

public class LabController84 extends BaseLabController {

    private DiodeCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;

    private ComboBox<DiodeType> diodeComboBox;
    private Slider tempSlider, idealitySlider, breakdownSlider, voltageSlider;
    private Button startBtn, autoBtn, clearBtn;

    private Label liveStatusLabel;
    private Label liveULabel;
    private Label liveILabel;

    private Queue<Double> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    public LabController84() {
        initUI();
        updateDisplayFromSlider();
    }

    @Override
    public void shutdown() {
        isAutoRunning = false;
        autoQueue.clear();
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(320);
        leftPanel.setMinWidth(320);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 8-4)");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));

        TitledPane diodePane = new TitledPane();
        diodePane.setText("Параметри напівпровідника");
        diodePane.setCollapsible(false);
        VBox diodeBox = new VBox(8);
        diodeBox.setPadding(new Insets(5));

        diodeComboBox = new ComboBox<>(FXCollections.observableArrayList(DiodeType.values()));
        diodeComboBox.getSelectionModel().selectFirst();
        diodeComboBox.setMaxWidth(Double.MAX_VALUE);
        diodeComboBox.setOnAction(e -> resetAndRecalculate());

        Label nLabel = new Label("Коеф. ідеальності (n): 2.0");
        idealitySlider = new Slider(1.0, 3.0, 2.0);
        idealitySlider.setShowTickMarks(true);
        idealitySlider.valueProperty().addListener((o, old, newVal) -> {
            nLabel.setText(String.format("Коеф. ідеальності (n): %.1f", newVal.doubleValue()));
            updateDisplayFromSlider();
        });

        Label breakLabel = new Label("Напруга пробою (В): -5.0");
        breakdownSlider = new Slider(-10.0, -2.0, -5.0);
        breakdownSlider.setShowTickMarks(true);
        breakdownSlider.valueProperty().addListener((o, old, newVal) -> {
            breakLabel.setText(String.format("Напруга пробою (В): %.1f", newVal.doubleValue()));
            updateDisplayFromSlider();
        });

        diodeBox.getChildren().addAll(
                new Label("Тип діода:"), diodeComboBox,
                nLabel, idealitySlider,
                breakLabel, breakdownSlider
        );
        diodePane.setContent(diodeBox);

        TitledPane envPane = new TitledPane();
        envPane.setText("Умови середовища");
        envPane.setCollapsible(false);
        VBox envBox = new VBox(8);
        envBox.setPadding(new Insets(5));

        Label tempLabel = new Label("Температура (T): 20 °C");
        tempSlider = new Slider(0, 100, 20);
        tempSlider.setShowTickMarks(true);
        tempSlider.setShowTickLabels(true);
        tempSlider.setMajorTickUnit(20);
        tempSlider.valueProperty().addListener((o, old, newVal) -> {
            tempLabel.setText(String.format("Температура (T): %.0f °C", newVal.doubleValue()));
            updateDisplayFromSlider();
        });

        envBox.getChildren().addAll(tempLabel, tempSlider);
        envPane.setContent(envBox);

        TitledPane voltagePane = new TitledPane();
        voltagePane.setText("Джерело живлення");
        voltagePane.setCollapsible(false);
        VBox voltBox = new VBox(8);
        voltBox.setPadding(new Insets(5));

        Label voltageLabel = new Label("Напруга U: 0.00 В");
        voltageSlider = new Slider(-10.0, 1.2, 0.0);
        voltageSlider.setShowTickMarks(true);
        voltageSlider.setShowTickLabels(true);
        voltageSlider.setMajorTickUnit(2.0);
        voltageSlider.valueProperty().addListener((o, oldVal, newVal) -> {
            voltageLabel.setText(String.format("Напруга U: %.2f В", newVal.doubleValue()));
            updateDisplayFromSlider();
        });

        voltBox.getChildren().addAll(voltageLabel, voltageSlider);
        voltagePane.setContent(voltBox);

        startBtn = new Button("▶ ЗАПИСАТИ ТОЧКУ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> recordMeasurement(voltageSlider.getValue()));

        autoBtn = new Button("⚙ АВТОЗНЯТТЯ ВАХ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAuto());

        clearBtn = new Button("🗑 ОЧИСТИТИ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> resetAndRecalculate());

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, diodePane, envPane, voltagePane, startBtn, autoBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new DiodeCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(200, 80);

        Label dashTitle = new Label("ДАТЧИКИ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ГОТОВО");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveULabel = new Label("U = 0.00 В");
        liveULabel.setStyle("-fx-text-fill: #00ff00;");
        liveILabel = new Label("I = 0.00 мА");
        liveILabel.setStyle("-fx-text-fill: #00ff00;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveULabel, liveILabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        String[] cols = {"№", "Тип діода", "U (В)", "I (мА)", "R стат (Ом)"};
        String[] props = {"id", "type", "u", "i", "rStat"};

        for (int i = 0; i < cols.length; i++) {
            TableColumn<Measurement, Object> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(new PropertyValueFactory<>(props[i]));
            table.getColumns().add(col);
        }
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(130);

        VBox statsBox = createStatsBox();

        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        updateStats();
    }

    private void resetAndRecalculate() {
        data.clear();
        idCounter = 1;
        canvas.clearGraph();
        updateStats();
        updateDisplayFromSlider();
    }

    private double calculateCurrent(DiodeType type, double u) {
        if (Math.abs(u) < 0.01) return 0.0;

        double T = tempSlider.getValue() + 273.15;
        double k = 1.38e-23;
        double q = 1.6e-19;
        double Vt = (k * T) / q;
        double n = idealitySlider.getValue();

        double Is = (type == DiodeType.SILICON) ? 5e-6 : 1e-3;

        double tempFactor = Math.pow(2.0, (tempSlider.getValue() - 20) / 10.0);
        Is = Is * tempFactor;

        double i = 0;
        if (u >= 0) {
            i = Is * (Math.exp(u / (n * Vt)) - 1.0);
        } else {
            i = -Is;
            double uBreak = breakdownSlider.getValue();
            if (u <= uBreak) {
                i -= 0.1 * Math.exp(Math.abs(u - uBreak) * 3.0);
            }
        }

        if (i > 45.0) i = 45.0 + (i - 45.0)*0.01;
        if (i < -45.0) i = -45.0 - (Math.abs(i) - 45.0)*0.01;

        return i;
    }

    private void updateDisplayFromSlider() {
        if (isAutoRunning) return;
        double u = voltageSlider.getValue();
        double i = calculateCurrent(diodeComboBox.getValue(), u);

        liveULabel.setText(String.format("U = %.2f В", u));
        liveILabel.setText(String.format("I = %.3f мА", i));

        canvas.updateLiveValues(u, i);
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        diodeComboBox.setDisable(disable);
        tempSlider.setDisable(disable);
        idealitySlider.setDisable(disable);
        breakdownSlider.setDisable(disable);
        voltageSlider.setDisable(disable);
    }

    private void startAuto() {
        resetAndRecalculate();

        double[] points = {-10.0, -5.0, -2.0, -0.5, 0.0, 0.2, 0.4, 0.5, 0.6, 0.7, 0.8};
        for (double p : points) autoQueue.add(p);

        isAutoRunning = true;
        setControlsDisable(true);
        processNextAuto();
    }

    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoRunning = false;
            setControlsDisable(false);
            liveStatusLabel.setText("Статус: ГОТОВО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            return;
        }

        double u = autoQueue.poll();
        voltageSlider.setValue(u);
        liveStatusLabel.setText("Статус: ВИМІРЮВАННЯ...");
        liveStatusLabel.setStyle("-fx-text-fill: red;");

        double i = calculateCurrent(diodeComboBox.getValue(), u);

        liveULabel.setText(String.format("U = %.2f В", u));
        liveILabel.setText(String.format("I = %.3f мА", i));

        canvas.updateLiveValues(u, i);

        new Thread(() -> {
            try { Thread.sleep(500); } catch (Exception ignored) {}
            Platform.runLater(() -> recordMeasurement(u));
        }).start();
    }

    private void recordMeasurement(double u) {
        DiodeType type = diodeComboBox.getValue();
        double i = calculateCurrent(type, u);

        String rStat = "∞";
        if (Math.abs(i) > 0.0005) {
            double r = Math.abs(u / (i / 1000.0));
            rStat = String.format("%.0f", r);
        } else if (u == 0) {
            rStat = "-";
        }

        Measurement m = new Measurement(idCounter++, type.getName(),
                Math.round(u * 100.0) / 100.0,
                Math.round(i * 1000.0) / 1000.0,
                rStat);
        data.add(m);
        canvas.addGraphPoint(u, i);
        updateStats();

        if (isAutoRunning) {
            processNextAuto();
        } else {
            liveStatusLabel.setText("Статус: ДАНІ ЗАПИСАНО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
        }
    }

    private void updateStats() {
        if (data.size() < 2) {
            finalResultLabel.setText("Обробка результатів: Недостатньо даних для аналізу...");
            return;
        }
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }
        double rd = 0.0;
        double uDyn = 0.0;
        for (int i = data.size() - 1; i > 0; i--) {
            Measurement m1 = data.get(i);
            Measurement m2 = data.get(i - 1);
            if (m1.getU() > 0.4 && m1.getU() != m2.getU() && m1.getI() != m2.getI()) {
                double du = m1.getU() - m2.getU();
                double di = (m1.getI() - m2.getI()) / 1000.0;
                if (di > 0) {
                    rd = du / di;
                    uDyn = m1.getU();
                    break;
                }
            }
        }

        double kFactor = 0.0;
        double uK = 0.0;
        for (Measurement mPr : data) {
            if (mPr.getU() >= 0.2 && mPr.getI() > 0) {
                for (Measurement mZv : data) {
                    if (Math.abs(mZv.getU() - (-mPr.getU())) < 0.05) {
                        double iPr = mPr.getI();
                        double iZv = Math.abs(mZv.getI());
                        if (iZv > 1e-6) {
                            kFactor = iPr / iZv;
                            uK = mPr.getU();
                        }
                    }
                }
            }
            if (kFactor > 0) break;
        }

        StringBuilder sb = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ ТА ЇХ АНАЛІЗ:\n");
        sb.append("1. Вольт-амперна характеристика I = f(U) побудована на графіку.\n");
        sb.append("2. Статичний опір R = U/I обчислено для кожної точки (див. таблицю).\n");

        if (rd > 0) {
            sb.append(String.format("3. Динамічний опір в точці U=%.2f В: Rd = %.2f Ом.\n", uDyn, rd));
        } else {
            sb.append("3. Динамічний опір: Недостатньо точок на прямій вітці.\n");
        }

        if (kFactor > 0) {
            sb.append(String.format("4. Коефіцієнт випрямлення при U=%.2f В: K = %.0f.\n", uK, kFactor));
        } else {
            sb.append("4. Коефіцієнт випрямлення: Немає симетричних точок напруги (напр. ±0.5 В).\n");
        }

        sb.append("5. Висновок: Діод добре проводить струм у прямому напрямі і практично не проводить у зворотному. Велике значення коефіцієнта випрямлення підтверджує його вентильні властивості.");

        finalResultLabel.setText(sb.toString());
    }
}