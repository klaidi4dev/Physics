package dev.ua._klaidi4_.physics.level1.lab1_8.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level1.lab1_8.model.Measurement;
import dev.ua._klaidi4_.physics.level1.lab1_8.view.TorsionCanvas;
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

public class LabController18 extends BaseLabController {

    private TorsionCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> configCombo;
    private TextField m0Field;
    private TextField a0Field;
    private TextField mField;
    private TextField aField;
    private TextField bField;
    private TextField cField;
    private TextField oscField;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveOscLabel;
    private Label liveTimeLabel;
    private Queue<Integer> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;
    private int targetOscillations = 10;
    private final double TORSION_CONSTANT_D = 0.008;

    public LabController18() {
        initUI();
        applyPhysicsSettings();
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
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 1-8)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри маятника");
        labPane.setCollapsible(true);
        labPane.setExpanded(true);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        configCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Еталон (куб)",
                "Брусок (вісь X)",
                "Брусок (вісь Y)",
                "Брусок (вісь Z)"
        ));
        configCombo.getSelectionModel().selectFirst();

        m0Field = new TextField("0.600");
        a0Field = new TextField("0.10");
        mField = new TextField("0.800");
        aField = new TextField("0.12");
        bField = new TextField("0.08");
        cField = new TextField("0.04");
        oscField = new TextField("10");

        configCombo.setOnAction(e -> applyPhysicsSettings());
        a0Field.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        aField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        bField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        cField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());

        paramsBox.getChildren().addAll(
                createInputGroup("Конфігурація тіла:", configCombo),
                createInputGroup("Маса куба m0 (кг):", m0Field),
                createInputGroup("Сторона куба a0 (м):", a0Field),
                createInputGroup("Маса бруска m (кг):", mField),
                createInputGroup("Довжина бруска a (м):", aField),
                createInputGroup("Ширина бруска b (м):", bField),
                createInputGroup("Висота бруска c (м):", cField),
                createInputGroup("Кількість коливань n:", oscField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(280);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        startBtn = new Button("▶ ЗАПУСТИТИ КОЛИВАННЯ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ (4 осі)");
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

        leftPanel.getChildren().addAll(title, labPane, startBtn, autoBtn, clearBtn);

        canvas = new TorsionCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(220, 80);
        Label dashTitle = new Label("СЕКУНДОМІР");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveOscLabel = new Label("Коливання: 0");
        liveOscLabel.setStyle("-fx-text-fill: #3498db;");
        liveTimeLabel = new Label("t = 0.00 с");
        liveTimeLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        liveTimeLabel.setStyle("-fx-text-fill: #00ff00;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveOscLabel, liveTimeLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, String> confCol = new TableColumn<>("Конфігурація");
        confCol.setCellValueFactory(new PropertyValueFactory<>("config"));
        TableColumn<Measurement, Integer> nCol = new TableColumn<>("n");
        nCol.setCellValueFactory(new PropertyValueFactory<>("n"));
        TableColumn<Measurement, Double> tCol = new TableColumn<>("t (с)");
        tCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        TableColumn<Measurement, Double> pCol = new TableColumn<>("T (с)");
        pCol.setCellValueFactory(new PropertyValueFactory<>("period"));
        TableColumn<Measurement, Double> eCol = new TableColumn<>("I експ");
        eCol.setCellValueFactory(new PropertyValueFactory<>("expI"));
        TableColumn<Measurement, Double> tICol = new TableColumn<>("I теор");
        tICol.setCellValueFactory(new PropertyValueFactory<>("theoI"));

        table.getColumns().addAll(idCol, confCol, nCol, tCol, pCol, eCol, tICol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();

        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);
    }

    private void applyPhysicsSettings() {
        try {
            int conf = configCombo.getSelectionModel().getSelectedIndex();
            double a0 = Double.parseDouble(a0Field.getText());
            double a = Double.parseDouble(aField.getText());
            double b = Double.parseDouble(bField.getText());
            double c = Double.parseDouble(cField.getText());
            canvas.setParameters(conf, a0/2, a, b, c);
        } catch (NumberFormatException ignored) {}
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        configCombo.setDisable(disable);
        m0Field.setDisable(disable);
        a0Field.setDisable(disable);
        mField.setDisable(disable);
        aField.setDisable(disable);
        bField.setDisable(disable);
        cField.setDisable(disable);
        oscField.setDisable(disable);
    }

    private void startManual() {
        try {
            Double.parseDouble(m0Field.getText());
            targetOscillations = Integer.parseInt(oscField.getText());
            if (targetOscillations <= 0) throw new Exception();

            isAutoRunning = false;
            runSimulation(configCombo.getSelectionModel().getSelectedIndex());
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа в поля.");
        }
    }

    private void startAuto() {
        try {
            targetOscillations = Integer.parseInt(oscField.getText());
            if (targetOscillations <= 0) targetOscillations = 10;
        } catch (Exception e) { targetOscillations = 10; }

        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();
        autoQueue.add(0);
        autoQueue.add(1);
        autoQueue.add(2);
        autoQueue.add(3);

        isAutoRunning = true;
        processNextAuto();
    }

    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoRunning = false;
            liveStatusLabel.setText("СИСТЕМА: ГОТОВО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            setControlsDisable(false);
            return;
        }
        int nextConfig = autoQueue.poll();
        configCombo.getSelectionModel().select(nextConfig);
        applyPhysicsSettings();
        runSimulation(nextConfig);
    }

    private void runSimulation(int configIndex) {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: КОЛИВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: cyan;");

        double m0 = Double.parseDouble(m0Field.getText());
        double a0 = Double.parseDouble(a0Field.getText());
        double m = Double.parseDouble(mField.getText());
        double a = Double.parseDouble(aField.getText());
        double b = Double.parseDouble(bField.getText());
        double c = Double.parseDouble(cField.getText());

        double iTheory = 0;

        if (configIndex == 0) {
            iTheory = (m0 * (a0 * a0 + a0 * a0)) / 12.0;
        } else if (configIndex == 1) {
            iTheory = (m * (b * b + c * c)) / 12.0;
        } else if (configIndex == 2) {
            iTheory = (m * (a * a + c * c)) / 12.0;
        } else if (configIndex == 3) {
            iTheory = (m * (a * a + b * b)) / 12.0;
        }

        final double exactPeriod = 2 * Math.PI * Math.sqrt(iTheory / TORSION_CONSTANT_D);
        final double finalTotalTime = (exactPeriod * targetOscillations) + (Math.random() - 0.5) * 0.15;
        final double finalMeasuredPeriod = finalTotalTime / targetOscillations;
        final double finalITheory = iTheory;

        canvas.startSimulation(finalMeasuredPeriod);

        new Thread(() -> {
            long start = System.currentTimeMillis();
            long targetMs = (long) (finalTotalTime * 1000);

            while (System.currentTimeMillis() - start < targetMs) {
                long elapsed = System.currentTimeMillis() - start;
                double seconds = elapsed / 1000.0;
                int osc = (int) (seconds / finalMeasuredPeriod);

                Platform.runLater(() -> {
                    liveTimeLabel.setText(String.format("t = %.2f с", seconds));
                    liveOscLabel.setText("Коливання: " + osc);
                });

                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }

            Platform.runLater(() -> finishMeasurement(configIndex, finalTotalTime, finalMeasuredPeriod, finalITheory));
        }).start();
    }

    private void finishMeasurement(int configIndex, double totalTime, double measuredPeriod, double iTheory) {
        canvas.stopAnimation();
        liveOscLabel.setText("Коливання: " + targetOscillations);
        liveTimeLabel.setText(String.format("t = %.2f с", totalTime));
        liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

        String configName = configCombo.getItems().get(configIndex);

        double iExp = 0;
        if (configIndex == 0) {
            iExp = iTheory;
        } else {
            Measurement reference = data.stream().filter(m -> m.getConfig().contains("Еталон")).findFirst().orElse(null);
            if (reference != null) {
                double T0 = reference.getPeriod();
                double I0 = reference.getExpI();
                iExp = I0 * (measuredPeriod * measuredPeriod) / (T0 * T0);
            } else {
                showAlert("Помилка обробки", "Щоб вирахувати момент інерції бруска, необхідно спочатку виміряти еталонний куб!");
            }
        }

        Measurement meas = new Measurement(
                idCounter++, configName, targetOscillations,
                Math.round(totalTime * 1000.0) / 1000.0,
                Math.round(measuredPeriod * 10000.0) / 10000.0,
                Math.round(iExp * 1000000.0) / 1000000.0,
                Math.round(iTheory * 1000000.0) / 1000000.0
        );
        data.add(meas);
        updateStats();

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(1500); Platform.runLater(this::processNextAuto); }
                catch (InterruptedException ignored) {}
            }).start();
        } else {
            setControlsDisable(false);
        }
    }

    private void updateStats() {
        if (data.size() < 4) {
            finalResultLabel.setText("Для повного аналізу та побудови еліпсоїда інерції потрібні всі 4 виміри (Еталон та 3 осі).");
            return;
        }
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }
        Measurement m0 = data.get(0);
        Measurement mx = data.get(1);
        Measurement my = data.get(2);
        Measurement mz = data.get(3);

        double ixExp = mx.getExpI();
        double iyExp = my.getExpI();
        double izExp = mz.getExpI();

        double ixTheo = mx.getTheoI();
        double iyTheo = my.getTheoI();
        double izTheo = mz.getTheoI();

        double epsX = Math.abs(ixExp - ixTheo) / ixTheo * 100;
        double epsY = Math.abs(iyExp - iyTheo) / iyTheo * 100;
        double epsZ = Math.abs(izExp - izTheo) / izTheo * 100;

        String conclusion = String.format(
                "ОБРОБКА РЕЗУЛЬТАТІВ ТА АНАЛІЗ:\n" +
                        "1. Періоди коливань: еталона (куб) T0 = %.3f с, бруска Tx = %.3f с, Ty = %.3f с, Tz = %.3f с.\n" +
                        "2. Головні експериментальні моменти інерції (ф.16): Ix = %.5f, Iy = %.5f, Iz = %.5f (кг·м²).\n" +
                        "3. Рівняння еліпсоїда інерції: %.5f X² + %.5f Y² + %.5f Z² = 1.\n" +
                        "4-5. Теоретичні значення та похибки:\n" +
                        "   Ix_теор = %.5f (ε = %.1f%%), Iy_теор = %.5f (ε = %.1f%%), Iz_теор = %.5f (ε = %.1f%%).\n" +
                        "6. ВИСНОВОК: Експериментально знайдені головні моменти інерції бруска співпадають з теоретичними " +
                        "в межах розрахованої похибки. Еліпсоїд інерції побудовано успішно.",
                m0.getPeriod(), mx.getPeriod(), my.getPeriod(), mz.getPeriod(),
                ixExp, iyExp, izExp,
                ixExp, iyExp, izExp,
                ixTheo, epsX, iyTheo, epsY, izTheo, epsZ
        );

        finalResultLabel.setText(conclusion);
    }
}