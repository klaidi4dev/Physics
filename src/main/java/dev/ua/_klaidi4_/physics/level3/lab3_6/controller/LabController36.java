package dev.ua._klaidi4_.physics.level3.lab3_6.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level3.lab3_6.model.Measurement;
import dev.ua._klaidi4_.physics.level3.lab3_6.view.MutualInductionCanvas;
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

public class LabController36 extends BaseLabController {

    private MutualInductionCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField zField;
    private TextField u0Field;
    private TextField fField;
    private TextField rField;
    private TextField autoStepsField;
    private Button startBtn;
    private Button autoTask1Btn;
    private Button autoTask2Btn;
    private Button autoTask3Btn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveEpsLabel;
    private Label liveMLabel;
    private Queue<AutoTestParam> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;
    private String currentTaskName = "Ручний режим";

    private static class AutoTestParam {
        double z, u0, f;
        AutoTestParam(double z, double u0, double f) {
            this.z = z; this.u0 = u0; this.f = f;
        }
    }

    public LabController36() {
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
        leftPanel.setPrefWidth(320);
        leftPanel.setMinWidth(320);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Управління установкою (3-6)");
        title.setFont(Font.font("System", FontWeight.BOLD, 17));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри приладів");
        labPane.setCollapsible(false);

        VBox paramsBox = new VBox(10);
        paramsBox.setPadding(new Insets(5));

        zField = new TextField("0.0");
        u0Field = new TextField("2.0");
        fField = new TextField("10000");
        rField = new TextField("10000.0");

        autoStepsField = new TextField("10");
        autoStepsField.setStyle("-fx-background-color: #fff9c4; -fx-border-color: #fbc02d; -fx-border-radius: 4; -fx-padding: 6;");

        paramsBox.getChildren().addAll(
                createInputGroup("Координата рухомої котушки Z (см):", zField),
                createInputGroup("Напруга генератора U0 (В):", u0Field),
                createInputGroup("Частота генератора f (Гц):", fField),
                createInputGroup("Опір резистора R (Ом):", rField),
                createInputGroup("Кількість вимірів (для авторежиму):", autoStepsField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(280);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        startBtn = new Button("▶ ВИМІРЯТИ ЕРС (Один вимір)");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoTask1Btn = new Button("⚙ ТАБЛ. 1: Залежність M від Z");
        autoTask1Btn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold;");
        autoTask1Btn.setMaxWidth(Double.MAX_VALUE);
        autoTask1Btn.setOnAction(e -> startAutoTask(1));

        autoTask2Btn = new Button("⚙ ТАБЛ. 2: Залежність M від U0");
        autoTask2Btn.setStyle("-fx-background-color: #8e24aa; -fx-text-fill: white; -fx-font-weight: bold;");
        autoTask2Btn.setMaxWidth(Double.MAX_VALUE);
        autoTask2Btn.setOnAction(e -> startAutoTask(2));

        autoTask3Btn = new Button("⚙ ТАБЛ. 3: Залежність M від f");
        autoTask3Btn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoTask3Btn.setMaxWidth(Double.MAX_VALUE);
        autoTask3Btn.setOnAction(e -> startAutoTask(3));

        clearBtn = new Button("🗑 ОЧИСТИТИ ДАНІ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> clearTable());

        leftPanel.getChildren().addAll(title, labPane, startBtn, autoTask1Btn, autoTask2Btn, autoTask3Btn, clearBtn);

        canvas = new MutualInductionCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(10, 25, 40, 0.9); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(210, 80);
        Label dashTitle = new Label("ЕЛЕКТРОННІ ПОКАЗНИКИ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveEpsLabel = new Label("ε0 = 0.0000 В");
        liveEpsLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        liveEpsLabel.setStyle("-fx-text-fill: #00ff00;");
        liveMLabel = new Label("M = 0.00 мГн");
        liveMLabel.setStyle("-fx-text-fill: cyan; -fx-font-size: 12px;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveEpsLabel, liveMLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, Double> zCol = new TableColumn<>("Z (см)");
        zCol.setCellValueFactory(new PropertyValueFactory<>("z"));
        TableColumn<Measurement, Double> u0Col = new TableColumn<>("U0 (В)");
        u0Col.setCellValueFactory(new PropertyValueFactory<>("u0"));
        TableColumn<Measurement, Double> fCol = new TableColumn<>("f (Гц)");
        fCol.setCellValueFactory(new PropertyValueFactory<>("f"));
        TableColumn<Measurement, Double> epsCol = new TableColumn<>("ε0 (В)");
        epsCol.setCellValueFactory(new PropertyValueFactory<>("eps"));
        TableColumn<Measurement, Double> mCol = new TableColumn<>("M (мГн)");
        mCol.setCellValueFactory(new PropertyValueFactory<>("m"));

        table.getColumns().addAll(idCol, zCol, u0Col, fCol, epsCol, mCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(130);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoTask1Btn.setDisable(disable);
        autoTask2Btn.setDisable(disable);
        autoTask3Btn.setDisable(disable);
        clearBtn.setDisable(disable);
        zField.setDisable(disable);
        u0Field.setDisable(disable);
        fField.setDisable(disable);
        rField.setDisable(disable);
        autoStepsField.setDisable(disable);
    }

    private void clearTable() {
        data.clear();
        idCounter = 1;
        updateStats();
        liveEpsLabel.setText("ε0 = 0.0000 В");
        liveMLabel.setText("M = 0.00 мГн");
        canvas.resetToIdle();
    }

    private void startManual() {
        try {
            Double.parseDouble(zField.getText().replace(',', '.'));
            Double.parseDouble(u0Field.getText().replace(',', '.'));
            Double.parseDouble(fField.getText().replace(',', '.'));
            Double.parseDouble(rField.getText().replace(',', '.'));
            isAutoRunning = false;
            currentTaskName = "Ручні вимірювання";
            runMeasurementCycle();
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числові значення в поля параметрів.");
        }
    }

    private void startAutoTask(int taskNumber) {
        try {
            int steps = Integer.parseInt(autoStepsField.getText().replace(',', '.'));
            if (steps < 2) {
                showAlert("Увага", "Кількість вимірів має бути не менше 2.");
                return;
            }
            Double.parseDouble(rField.getText().replace(',', '.')); // Перевірка поля R

            clearTable();
            autoQueue.clear();
            isAutoRunning = true;

            if (taskNumber == 1) {
                currentTaskName = "Таблиця 1: Залежність M(Z)";
                double stepSize = 15.0 / (steps - 1);
                for (int i = 0; i < steps; i++) {
                    autoQueue.add(new AutoTestParam(i * stepSize, 2.0, 10000));
                }
            } else if (taskNumber == 2) {
                currentTaskName = "Таблиця 2: Залежність M(U0)";
                double stepSize = (5.0 - 1.0) / (steps - 1);
                for (int i = 0; i < steps; i++) {
                    autoQueue.add(new AutoTestParam(0.0, 1.0 + (i * stepSize), 10000));
                }
            } else if (taskNumber == 3) {
                currentTaskName = "Таблиця 3: Залежність M(f)";
                double stepSize = (20000.0 - 5000.0) / (steps - 1);
                for (int i = 0; i < steps; i++) {
                    autoQueue.add(new AutoTestParam(0.0, 2.0, 5000.0 + (i * stepSize)));
                }
            }
            processNextAuto();

        } catch (NumberFormatException ex) {
            showAlert("Помилка", "Перевірте правильність вводу кількості вимірів та опору.");
        }
    }

    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoRunning = false;
            liveStatusLabel.setText("СИСТЕМА: СЕРІЮ ЗАВЕРШЕНО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            setControlsDisable(false);
            return;
        }
        AutoTestParam param = autoQueue.poll();
        zField.setText(String.format(Locale.US, "%.1f", param.z));
        u0Field.setText(String.format(Locale.US, "%.1f", param.u0));
        fField.setText(String.format(Locale.US, "%.0f", param.f));
        runMeasurementCycle();
    }

    private void runMeasurementCycle() {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: ЗБІР ДАНИХ...");
        liveStatusLabel.setStyle("-fx-text-fill: cyan;");

        double z = Double.parseDouble(zField.getText().replace(',', '.'));
        double u0 = Double.parseDouble(u0Field.getText().replace(',', '.'));
        double f = Double.parseDouble(fField.getText().replace(',', '.'));
        double r = Double.parseDouble(rField.getText().replace(',', '.'));
        double mTheo = 0.006 * Math.exp(-0.015 * z * z);
        double exactEps = (2 * Math.PI * f * u0 * mTheo) / r;
        double noiseEps = exactEps * (Math.random() - 0.5) * 0.03;
        final double measuredEps = exactEps + noiseEps;

        canvas.setOnReadyCallback(() -> Platform.runLater(() -> finalizeMeasurement(z, u0, f, r, measuredEps)));
        canvas.animateDevices(z, u0, f, measuredEps);
    }

    private void finalizeMeasurement(double z, double u0, double f, double r, double eps) {
        liveStatusLabel.setText("Статус: ВИМІР ЗАФІКСОВАНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

        double mExpH = (eps * r) / (2 * Math.PI * f * u0);
        double mExpMH = mExpH * 1000.0;

        liveEpsLabel.setText(String.format(Locale.US, "ε0 = %.4f В", eps));
        liveMLabel.setText(String.format(Locale.US, "M = %.2f мГн", mExpMH));

        Measurement meas = new Measurement(
                idCounter++, z, u0, f,
                Math.round(eps * 10000.0) / 10000.0,
                Math.round(mExpMH * 100.0) / 100.0
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

        double sumM = 0;
        for (Measurement m : data) {
            sumM += m.getM();
        }
        double avgM = sumM / data.size();

        String analysisText;
        if (currentTaskName.contains("Таблиця 1")) {
            analysisText = "Зі збільшенням Z (віддалення котушок) магнітний потік розсіюється, тому M закономірно зменшується. Графік M(Z) має форму купола.";
        } else if (currentTaskName.contains("Таблиця 2")) {
            analysisText = "При зміні амплітуди напруги U0 генератора розрахований коефіцієнт M залишається практично незмінним. Це підтверджує теорію (M залежить лише від геометрії).";
        } else if (currentTaskName.contains("Таблиця 3")) {
            analysisText = "Зі зміною частоти f значення ЕРС зростає лінійно, проте розраховане M залишається сталим.";
        } else {
            analysisText = String.format(Locale.US, "Проміжний аналіз: M_сер = %.2f мГн.", avgM);
        }

        String conclusion = String.format(Locale.US,
                "ОБРОБКА РЕЗУЛЬТАТІВ (%s):\n" +
                        "1. Коефіцієнт взаємної індукції M розраховано за формулою (10): M = (ε0·R) / (2π·f·U0).\n" +
                        "2. Аналіз залежностей: %s\n" +
                        "3. Висновок: Явище взаємної індукції досліджено успішно. Всі виявлені закономірності повністю відповідають закону електромагнітної індукції Фарадея.",
                currentTaskName, analysisText
        );

        finalResultLabel.setText(conclusion);
    }
}