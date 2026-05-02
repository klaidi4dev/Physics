package dev.ua._klaidi4_.physics.level2.lab2_1.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level2.lab2_1.model.Measurement;
import dev.ua._klaidi4_.physics.level2.lab2_1.view.ElectrostaticCanvas;
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

public class LabController21 extends BaseLabController {

    private ElectrostaticCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;

    private TextField voltageField;
    private TextField distField;
    private TextField radiusField;
    private TextField numMeasurementsField;
    private ComboBox<String> scaleCombo;

    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;

    private Label liveStatusLabel;
    private Label liveVoltageLabel;

    private Queue<Double> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    public LabController21() {
        initUI();
    }

    @Override
    public void shutdown() {
        isAutoRunning = false;
        autoQueue.clear();
        if (canvas != null) canvas.resetSystem();
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(340);
        leftPanel.setMinWidth(340);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 2-1)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane paramsPane = new TitledPane();
        paramsPane.setText("Налаштування Установки");
        paramsPane.setCollapsible(false);
        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(10));

        voltageField = new TextField("20.0");
        distField = new TextField("20.0");
        radiusField = new TextField("1.0");
        numMeasurementsField = new TextField("4");

        scaleCombo = new ComboBox<>(FXCollections.observableArrayList("1:1 (15 пікс/см)", "1:2 (7.5 пікс/см)", "2:1 (30 пікс/см)"));
        scaleCombo.getSelectionModel().selectFirst();

        voltageField.textProperty().addListener((o, old, newVal) -> updateDisplayFromInput());
        distField.textProperty().addListener((o, old, newVal) -> updateDisplayFromInput());
        radiusField.textProperty().addListener((o, old, newVal) -> updateDisplayFromInput());
        numMeasurementsField.textProperty().addListener((o, old, newVal) -> updateDisplayFromInput());
        scaleCombo.setOnAction(e -> updateDisplayFromInput());

        paramsBox.getChildren().addAll(
                createInputGroup("Напруга джерела U (В):", voltageField),
                createInputGroup("Відстань між центрами d (см):", distField),
                createInputGroup("Радіус електродів r (см):", radiusField),
                createInputGroup("Кількість замірів n:", numMeasurementsField),
                createInputGroup("Масштаб відображення:", scaleCombo)
        );
        paramsPane.setContent(paramsBox);

        startBtn = new Button("▶ ПОБУДУВАТИ КАРТУ ПОЛЯ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ (3 напруги)");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAuto());

        clearBtn = new Button("🗑 ОЧИСТИТИ ДАНІ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> resetAndRecalculate());

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, paramsPane, startBtn, autoBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new ElectrostaticCanvas(600, 440);

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

        Label dashTitle = new Label("ДАТЧИК ПОТЕНЦІАЛУ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ГОТОВО");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveVoltageLabel = new Label("U = 20.0 В");
        liveVoltageLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        liveVoltageLabel.setStyle("-fx-text-fill: #00ff00;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveVoltageLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        String[] cols = {"№", "φ1 (В)", "φ2 (В)", "Δφ (В)", "x1 (см)", "x2 (см)", "Δx (см)", "E (В/м)"};
        String[] props = {"id", "phi1", "phi2", "dPhi", "x1", "x2", "dX", "eField"};

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

        updateDisplayFromInput();
        updateStats();

        canvas.setOnFinishCallback(this::onCanvasFinished);
    }

    private double parseVoltage() {
        try { return Double.parseDouble(voltageField.getText()); }
        catch (Exception e) { return 20.0; }
    }

    private int parseMeasurements() {
        try {
            int n = Integer.parseInt(numMeasurementsField.getText());
            return n > 0 ? n : 4;
        } catch (Exception e) {
            return 4;
        }
    }

    private void resetAndRecalculate() {
        data.clear();
        idCounter = 1;
        canvas.resetSystem();
        updateStats();
        updateDisplayFromInput();
    }

    private void updateDisplayFromInput() {
        if (isAutoRunning) return;
        try {
            double u = Double.parseDouble(voltageField.getText());
            double d = Double.parseDouble(distField.getText());
            double r = Double.parseDouble(radiusField.getText());
            int n = parseMeasurements();
            canvas.updatePhysicsParameters(u, d, r, n);
            liveVoltageLabel.setText(String.format("U = %.1f В", u));
        } catch (NumberFormatException ignored) {}
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        voltageField.setDisable(disable);
        distField.setDisable(disable);
        radiusField.setDisable(disable);
        numMeasurementsField.setDisable(disable);
        scaleCombo.setDisable(disable);
    }

    private void startManual() {
        if (isAutoRunning) return;
        startMeasurement(parseVoltage());
    }

    private void startAuto() {
        try {
            Double.parseDouble(distField.getText());
            Double.parseDouble(radiusField.getText());
        } catch (NumberFormatException e) {
            showAlert("Помилка", "Введіть коректні значення для відстані та радіуса.");
            return;
        }

        resetAndRecalculate();
        autoQueue.add(10.0);
        autoQueue.add(20.0);
        autoQueue.add(30.0);

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
        voltageField.setText(String.valueOf(u));
        updateDisplayFromInput();
        startMeasurement(u);
    }

    private double recordedU, recordedD, recordedR;
    private int recordedN;

    private void startMeasurement(double u) {
        try {
            double d = Double.parseDouble(distField.getText());
            double r = Double.parseDouble(radiusField.getText());
            int n = parseMeasurements();

            if (r >= d/2) {
                showAlert("Помилка фізики", "Радіус електрода не може бути більшим за половину відстані між ними.");
                return;
            }

            setControlsDisable(true);
            liveStatusLabel.setText("Статус: МОДЕЛЮВАННЯ ПОЛЯ...");
            liveStatusLabel.setStyle("-fx-text-fill: red;");

            recordedU = u;
            recordedD = d;
            recordedR = r;
            recordedN = n;

            canvas.startSimulation();

        } catch (NumberFormatException e) {
            showAlert("Помилка", "Перевірте правильність введених даних.");
        }
    }

    private void onCanvasFinished() {
        Platform.runLater(() -> {
            recordMeasurementData(recordedU, recordedD / 100.0, recordedR / 100.0, recordedN);
        });
    }

    private void recordMeasurementData(double u0, double d, double r, int n) {
        double currentX = 0;
        double currentPhi = 0;
        double stepPhi = (u0 / 2.0) / n;

        for (int i = 1; i <= n; i++) {
            double phi2 = currentPhi + stepPhi;

            double k = Math.exp((2 * phi2 * Math.log(d / r)) / u0);
            double x2 = (d / 2.0) * (k - 1) / (k + 1);

            double dX = x2 - currentX;
            double eField = stepPhi / dX;

            Measurement m = new Measurement(
                    idCounter++,
                    Math.round(currentPhi * 100.0) / 100.0,
                    Math.round(phi2 * 100.0) / 100.0,
                    Math.round(stepPhi * 100.0) / 100.0,
                    Math.round((currentX * 100.0) * 100.0) / 100.0,
                    Math.round((x2 * 100.0) * 100.0) / 100.0,
                    Math.round((dX * 100.0) * 100.0) / 100.0,
                    Math.round(eField)
            );
            data.add(m);

            currentX = x2;
            currentPhi = phi2;
        }

        updateStats();

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(1200); } catch (Exception ignored) {}
                Platform.runLater(this::processNextAuto);
            }).start();
        } else {
            liveStatusLabel.setText("Статус: ДАНІ ЗАПИСАНО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            setControlsDisable(false);
        }
    }

    private void updateStats() {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: Очікування моделювання...");
            return;
        }
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }
        try {
            double d = Double.parseDouble(distField.getText()) / 100.0;
            double r = Double.parseDouble(radiusField.getText()) / 100.0;

            double eps0 = 8.854e-12;
            double cLinear = (Math.PI * eps0) / Math.log(d / r);

            StringBuilder sb = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ ДОСЛІДЖЕННЯ:\n");
            sb.append("1. Побудовано еквіпотенціальні поверхні та ортогональні до них лінії напруженості електричного поля.\n");
            sb.append("2. Розраховано напруженість поля E = Δφ/Δx на кожному з відрізків між еквіпотенціалями (див. табл.).\n");
            sb.append(String.format("3. Погонна ємність модельованої двопровідної лінії (при ε=1): C_l = %.2e Ф/м (або %.2f пФ/м).\n", cLinear, cLinear * 1e12));
            sb.append("4. ВИСНОВОК: Поле між циліндричними електродами суттєво неоднорідне. Напруженість E стрімко зростає при наближенні до електродів (Δx зменшується).");

            finalResultLabel.setText(sb.toString());
        } catch (Exception ignored) {}
    }
}