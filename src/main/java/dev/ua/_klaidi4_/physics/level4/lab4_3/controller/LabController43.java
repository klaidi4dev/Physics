package dev.ua._klaidi4_.physics.level4.lab4_3.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level4.lab4_3.model.Measurement;
import dev.ua._klaidi4_.physics.level4.lab4_3.view.OscilloscopeCanvas;
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

public class LabController43 extends BaseLabController {

    private OscilloscopeCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> modeBox;
    private TextField f1Field;
    private TextField f2Field;
    private TextField a1Field;
    private TextField a2Field;
    private TextField phaseField;
    private TextField autoStepsField;
    private Button startBtn;
    private Button autoTask1Btn;
    private Button autoTask2Btn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveF1Label;
    private Label liveF2Label;

    private Queue<AutoTestParam> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;
    private String currentTaskName = "Ручний режим";

    private static class AutoTestParam {
        String mode;
        double f1, f2, a1, a2, phase;
        AutoTestParam(String mode, double f1, double f2, double a1, double a2, double phase) {
            this.mode = mode; this.f1 = f1; this.f2 = f2; this.a1 = a1; this.a2 = a2; this.phase = phase;
        }
    }

    public LabController43() {
        initUI();
    }

    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        isAutoRunning = false;
        autoQueue.clear();
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(330);
        leftPanel.setMinWidth(330);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Управління установкою (4-3)");
        title.setFont(Font.font("System", FontWeight.BOLD, 17));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри генераторів");
        labPane.setCollapsible(false);

        VBox paramsBox = new VBox(10);
        paramsBox.setPadding(new Insets(5));

        modeBox = new ComboBox<>();
        modeBox.getItems().addAll("1. Биття (Y = Y1 + Y2)", "2. Фігури Ліссажу (X та Y)");
        modeBox.setValue("1. Биття (Y = Y1 + Y2)");
        modeBox.setMaxWidth(Double.MAX_VALUE);
        modeBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-padding: 2;");

        f1Field = new TextField("40.0");
        f2Field = new TextField("42.0");

        a1Field = new TextField("1.0");
        a2Field = new TextField("1.0");

        phaseField = new TextField("0.0");
        phaseField.setDisable(true);

        autoStepsField = new TextField("5");
        autoStepsField.setStyle("-fx-background-color: #fff9c4; -fx-border-color: #fbc02d; -fx-border-radius: 4; -fx-padding: 6;");

        paramsBox.getChildren().addAll(
                new VBox(4, createLabel("Режим осцилографа:"), modeBox),
                createInputGroup("Частота CH1 f1 (Гц):", f1Field),
                createInputGroup("Амплітуда CH1 A1 (В):", a1Field),
                createInputGroup("Частота CH2 f2 (Гц):", f2Field),
                createInputGroup("Амплітуда CH2 A2 (В):", a2Field),
                createInputGroup("Різниця фаз Δφ (градуси):", phaseField),
                new Separator(),
                createInputGroup("Кількість вимірів (для авторежиму):", autoStepsField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(350);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        startBtn = new Button("▶ ВИМІРЯТИ (Зберегти показник)");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoTask1Btn = new Button("⚙ АВТО: Завдання 1 (Биття)");
        autoTask1Btn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold;");
        autoTask1Btn.setMaxWidth(Double.MAX_VALUE);
        autoTask1Btn.setOnAction(e -> startAutoTask(1));

        autoTask2Btn = new Button("⚙ АВТО: Завдання 2 (Ліссажу)");
        autoTask2Btn.setStyle("-fx-background-color: #8e24aa; -fx-text-fill: white; -fx-font-weight: bold;");
        autoTask2Btn.setMaxWidth(Double.MAX_VALUE);
        autoTask2Btn.setOnAction(e -> startAutoTask(2));

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> clearTable());

        leftPanel.getChildren().addAll(title, labPane, startBtn, autoTask1Btn, autoTask2Btn, clearBtn);

        canvas = new OscilloscopeCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(10, 20, 10, 0.9); -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(220, 80);

        Label dashTitle = new Label("СИГНАЛ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveF1Label = new Label("CH1: 40.0 Гц | 1.0 В");
        liveF1Label.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
        liveF2Label = new Label("CH2: 42.0 Гц | 1.0 В");
        liveF2Label.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveF1Label, liveF2Label);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, String> modeCol = new TableColumn<>("Режим");
        modeCol.setCellValueFactory(new PropertyValueFactory<>("mode"));
        TableColumn<Measurement, Double> f1Col = new TableColumn<>("f1 (Гц)");
        f1Col.setCellValueFactory(new PropertyValueFactory<>("f1"));
        TableColumn<Measurement, Double> f2Col = new TableColumn<>("f2 (Гц)");
        f2Col.setCellValueFactory(new PropertyValueFactory<>("f2"));
        TableColumn<Measurement, Double> phaseCol = new TableColumn<>("Δφ (°)");
        phaseCol.setCellValueFactory(new PropertyValueFactory<>("phase"));

        table.getColumns().addAll(idCol, modeCol, f1Col, f2Col, phaseCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(130);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        modeBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isLissajous = newVal.contains("Ліссажу");
            phaseField.setDisable(!isLissajous);
            if (!isLissajous) {
                f1Field.setText("40.0");
                f2Field.setText("42.0");
            } else {
                f1Field.setText("40.0");
                f2Field.setText("60.0");
            }
            updateCanvasPreview();
        });

        f1Field.textProperty().addListener((obs, oldVal, newVal) -> updateCanvasPreview());
        f2Field.textProperty().addListener((obs, oldVal, newVal) -> updateCanvasPreview());
        a1Field.textProperty().addListener((obs, oldVal, newVal) -> updateCanvasPreview());
        a2Field.textProperty().addListener((obs, oldVal, newVal) -> updateCanvasPreview());
        phaseField.textProperty().addListener((obs, oldVal, newVal) -> updateCanvasPreview());

        Platform.runLater(this::updateCanvasPreview);
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        label.setStyle("-fx-text-fill: #475569;");
        return label;
    }

    private void updateCanvasPreview() {
        if (isAutoRunning || startBtn.isDisabled()) return;

        try {
            boolean isLissajous = modeBox.getValue().contains("Ліссажу");
            double f1 = Double.parseDouble(f1Field.getText());
            double f2 = Double.parseDouble(f2Field.getText());
            double a1 = Double.parseDouble(a1Field.getText());
            double a2 = Double.parseDouble(a2Field.getText());
            double phase = isLissajous ? Double.parseDouble(phaseField.getText()) : 0.0;

            liveF1Label.setText(String.format("CH1: %.1f Гц | %.1f В", f1, a1));
            liveF2Label.setText(String.format("CH2: %.1f Гц | %.1f В", f2, a2));

            canvas.updatePreview(isLissajous ? "lissajous" : "beats", f1, f2, a1, a2, phase);
        } catch (Exception ignored) {}
    }

    private void clearTable() {
        data.clear();
        idCounter = 1;
        updateStats();
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoTask1Btn.setDisable(disable);
        autoTask2Btn.setDisable(disable);
        clearBtn.setDisable(disable);
        modeBox.setDisable(disable);
        f1Field.setDisable(disable);
        f2Field.setDisable(disable);
        a1Field.setDisable(disable);
        a2Field.setDisable(disable);
        phaseField.setDisable(disable);
        autoStepsField.setDisable(disable);
    }

    private void startManual() {
        try {
            Double.parseDouble(f1Field.getText());
            Double.parseDouble(f2Field.getText());
            Double.parseDouble(a1Field.getText());
            Double.parseDouble(a2Field.getText());
            if (!phaseField.isDisabled()) Double.parseDouble(phaseField.getText());

            isAutoRunning = false;
            currentTaskName = "Ручні вимірювання";
            runMeasurementCycle();
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа.");
        }
    }

    private void startAutoTask(int taskNum) {
        try {
            int steps = Integer.parseInt(autoStepsField.getText());
            if (steps < 2) {
                showAlert("Увага", "Введіть мінімум 2 кроки.");
                return;
            }

            clearTable();
            autoQueue.clear();
            isAutoRunning = true;

            a1Field.setText("1.0");
            a2Field.setText("1.0");

            if (taskNum == 1) {
                currentTaskName = "Завдання 1: Биття";
                modeBox.setValue("1. Биття (Y = Y1 + Y2)");
                double stepSize = 8.0 / (steps - 1);
                for (int i = 0; i < steps; i++) {
                    autoQueue.add(new AutoTestParam("beats", 40.0, 42.0 + (i * stepSize), 1.0, 1.0, 0.0));
                }
            } else {
                currentTaskName = "Завдання 2: Фігури Ліссажу";
                modeBox.setValue("2. Фігури Ліссажу (X та Y)");
                double stepSize = 180.0 / (steps - 1);
                for (int i = 0; i < steps; i++) {
                    autoQueue.add(new AutoTestParam("lissajous", 40.0, 60.0, 1.0, 1.0, i * stepSize));
                }
            }
            processNextAuto();
        } catch (NumberFormatException e) {
            showAlert("Помилка", "Невірне число кроків.");
        }
    }

    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoRunning = false;
            liveStatusLabel.setText("СИСТЕМА: ГОТОВО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            setControlsDisable(false);
            return;
        }
        AutoTestParam param = autoQueue.poll();
        f1Field.setText(String.format(java.util.Locale.US, "%.1f", param.f1));
        f2Field.setText(String.format(java.util.Locale.US, "%.1f", param.f2));
        a1Field.setText(String.format(java.util.Locale.US, "%.1f", param.a1));
        a2Field.setText(String.format(java.util.Locale.US, "%.1f", param.a2));
        phaseField.setText(String.format(java.util.Locale.US, "%.1f", param.phase));

        updateCanvasPreview();
        runMeasurementCycle();
    }

    private void runMeasurementCycle() {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: ЗБІР ДАНИХ...");
        liveStatusLabel.setStyle("-fx-text-fill: cyan;");

        boolean isLissajous = modeBox.getValue().contains("Ліссажу");
        double f1 = Double.parseDouble(f1Field.getText());
        double f2 = Double.parseDouble(f2Field.getText());
        double a1 = Double.parseDouble(a1Field.getText());
        double a2 = Double.parseDouble(a2Field.getText());
        double phase = isLissajous ? Double.parseDouble(phaseField.getText()) : 0.0;

        canvas.setOnReadyCallback(() -> Platform.runLater(() -> finalizeMeasurement(isLissajous, f1, f2, phase)));
        canvas.startMeasurement(isLissajous ? "lissajous" : "beats", f1, f2, a1, a2, phase);
    }

    private void finalizeMeasurement(boolean isLissajous, double f1, double f2, double phase) {
        liveStatusLabel.setText("Статус: ЗАФІКСОВАНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

        Measurement meas = new Measurement(
                idCounter++,
                isLissajous ? "Ліссажу" : "Биття",
                f1, f2,
                isLissajous ? Math.round(phase * 10.0) / 10.0 : 0.0
        );
        data.add(meas);
        updateStats();

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(600); Platform.runLater(this::processNextAuto); }
                catch (InterruptedException ignored) {}
            }).start();
        } else {
            setControlsDisable(false);
        }
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

        StringBuilder analysis = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ (" + currentTaskName + "):\n");

        if (currentTaskName.contains("Биття")) {
            analysis.append("1. Теоретична частота биття: f_б = |f1 - f2|.\n");
            analysis.append("2. Амплітуда результуючого коливання періодично змінюється від 0 до 2A.\n");
            analysis.append("3. ВИСНОВОК: Візуальні спостереження на осцилограмі повністю підтверджують явище биття при додаванні паралельних коливань.");
        } else if (currentTaskName.contains("Ліссажу")) {
            analysis.append("1. При додаванні взаємно перпендикулярних коливань утворюються замкнені криві.\n");
            analysis.append("2. Відношення частот f1/f2 можна визначити за співвідношенням кількості дотиків фігури до сторін прямокутника.\n");
            analysis.append("3. ВИСНОВОК: Зміна різниці фаз Δφ змінює форму фігури (від прямої до еліпса та кола), але відношення частот залишається сталим.");
        } else {
            analysis.append("Показники успішно занесені до таблиці. Змініть режим для авто-аналізу.");
        }

        finalResultLabel.setText(analysis.toString());
    }
}