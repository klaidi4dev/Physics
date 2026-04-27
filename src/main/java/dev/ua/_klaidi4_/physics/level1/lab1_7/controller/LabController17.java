package dev.ua._klaidi4_.physics.level1.lab1_7.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level1.lab1_7.model.Measurement;
import dev.ua._klaidi4_.physics.level1.lab1_7.view.OberbeckCanvas;
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

public class LabController17 extends BaseLabController {

    private OberbeckCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField mField;
    private TextField hField;
    private TextField rField;
    private TextField lField;
    private TextField dField;
    private TextField m1Field;
    private TextField l0Field;
    private TextField r0Field;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveTimeLabel;
    private Queue<AutoTestParam> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    private static class AutoTestParam {
        double m, r;
        AutoTestParam(double m, double r) {
            this.m = m; this.r = r;
        }
    }

    public LabController17() {
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

        Label title = new Label("Система управління (Лаб 1-7)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane labPane1 = new TitledPane();
        labPane1.setText("Таблиця 1: Падіння вантажу");
        labPane1.setCollapsible(true);
        labPane1.setExpanded(true);
        VBox paramsBox1 = new VBox(12);
        paramsBox1.setPadding(new Insets(5));

        mField = new TextField("0.045");
        hField = new TextField("0.45");
        rField = new TextField("0.042");

        paramsBox1.getChildren().addAll(
                createInputGroup("Маса вантажу m (кг):", mField),
                createInputGroup("Висота падіння H (м):", hField),
                createInputGroup("Радіус диска r (м):", rField)
        );
        labPane1.setContent(paramsBox1);

        TitledPane labPane2 = new TitledPane();
        labPane2.setText("Таблиця 2: Геометрія хрестовини");
        labPane2.setCollapsible(true);
        labPane2.setExpanded(false);
        VBox paramsBox2 = new VBox(12);
        paramsBox2.setPadding(new Insets(5));

        lField = new TextField("0.25");
        dField = new TextField("0.005");
        m1Field = new TextField("0.2");
        l0Field = new TextField("0.035");
        r0Field = new TextField("0.215");

        paramsBox2.getChildren().addAll(
                createInputGroup("Довжина стержня l (м):", lField),
                createInputGroup("Діаметр стержня D (м):", dField),
                createInputGroup("Маса тягарця m1 (кг):", m1Field),
                createInputGroup("Довжина тягарця l0 (м):", l0Field),
                createInputGroup("Відстань R0 (м):", r0Field)
        );
        labPane2.setContent(paramsBox2);

        mField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        hField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        rField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        lField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        r0Field.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        l0Field.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());

        startBtn = new Button("▶ ВІДПУСТИТИ ВАНТАЖ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ (6 дослідів)");
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
            liveTimeLabel.setText("t = 0.000 с");
        });

        ScrollPane leftScroll = new ScrollPane(new VBox(8, title, labPane1, labPane2, startBtn, autoBtn, clearBtn));
        leftScroll.setFitToWidth(true);
        leftScroll.setStyle("-fx-background-color: transparent;");

        leftPanel.getChildren().add(leftScroll);

        canvas = new OberbeckCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(200, 70);
        Label dashTitle = new Label("СЕКУНДОМІР");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveTimeLabel = new Label("t = 0.000 с");
        liveTimeLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        liveTimeLabel.setStyle("-fx-text-fill: #00ff00;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveTimeLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, Double> mCol = new TableColumn<>("m (кг)");
        mCol.setCellValueFactory(new PropertyValueFactory<>("m"));
        TableColumn<Measurement, Double> rCol = new TableColumn<>("r (м)");
        rCol.setCellValueFactory(new PropertyValueFactory<>("r"));
        TableColumn<Measurement, Double> hCol = new TableColumn<>("H (м)");
        hCol.setCellValueFactory(new PropertyValueFactory<>("h"));
        TableColumn<Measurement, Double> tCol = new TableColumn<>("t (с)");
        tCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        TableColumn<Measurement, Double> expCol = new TableColumn<>("I експ (кг·м²)");
        expCol.setCellValueFactory(new PropertyValueFactory<>("expI"));
        TableColumn<Measurement, Double> theoCol = new TableColumn<>("I теор (кг·м²)");
        theoCol.setCellValueFactory(new PropertyValueFactory<>("theoI"));

        table.getColumns().addAll(idCol, mCol, rCol, hCol, tCol, expCol, theoCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        canvas.setOnFinishCallback(() -> Platform.runLater(this::finishMeasurement));
    }

    private void applyPhysicsSettings() {
        try {
            double r = Double.parseDouble(rField.getText());
            double R0 = Double.parseDouble(r0Field.getText());
            double l0 = Double.parseDouble(l0Field.getText());
            double bigR = R0 + l0/2; // Формула 9
            double h = Double.parseDouble(hField.getText());
            canvas.setParameters(r, bigR, h);
        } catch (NumberFormatException ignored) {}
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        mField.setDisable(disable);
        hField.setDisable(disable);
        rField.setDisable(disable);
        lField.setDisable(disable);
        dField.setDisable(disable);
        m1Field.setDisable(disable);
        l0Field.setDisable(disable);
        r0Field.setDisable(disable);
    }

    private void startManual() {
        try {
            Double.parseDouble(mField.getText());
            Double.parseDouble(hField.getText());
            Double.parseDouble(rField.getText());
            isAutoRunning = false;
            runSimulation();
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа в поля.");
        }
    }

    private void startAuto() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();
        autoQueue.add(new AutoTestParam(0.045, 0.042));
        autoQueue.add(new AutoTestParam(0.045, 0.042));
        autoQueue.add(new AutoTestParam(0.045, 0.042));
        autoQueue.add(new AutoTestParam(0.065, 0.021));
        autoQueue.add(new AutoTestParam(0.065, 0.021));
        autoQueue.add(new AutoTestParam(0.065, 0.021));

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
        AutoTestParam param = autoQueue.poll();
        mField.setText(String.valueOf(param.m));
        rField.setText(String.valueOf(param.r));
        applyPhysicsSettings();
        runSimulation();
    }

    private double currentExactTime;
    private double currentTheoI;

    private void runSimulation() {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: РУХ ВАНТАЖУ");
        liveStatusLabel.setStyle("-fx-text-fill: cyan;");

        double m = Double.parseDouble(mField.getText());
        double h = Double.parseDouble(hField.getText());
        double r = Double.parseDouble(rField.getText());
        double l = Double.parseDouble(lField.getText());
        double D = Double.parseDouble(dField.getText());
        double m1 = Double.parseDouble(m1Field.getText());
        double l0 = Double.parseDouble(l0Field.getText());
        double R0 = Double.parseDouble(r0Field.getText());
        double rho = 7800;
        double V = (Math.PI * D * D * l) / 4.0;
        double m2 = rho * V;

        double R_dist = R0 + l0 / 2.0;

        currentTheoI = 4 * (m2 * l * l / 3.0) + 4 * (m1 * R_dist * R_dist);
        double a = (m * 9.81) / (m + currentTheoI / (r * r));
        currentExactTime = Math.sqrt(2 * h / a);

        double speedMult = 1.0;
        if(currentExactTime > 5.0) speedMult = currentExactTime / 3.0;

        canvas.startSimulation(currentExactTime, speedMult);

        final double exactFinalTime = currentExactTime;
        final double mult = speedMult;
        new Thread(() -> {
            long start = System.currentTimeMillis();
            long targetMs = (long) ((exactFinalTime / mult) * 1000);

            while (System.currentTimeMillis() - start < targetMs) {
                long elapsed = System.currentTimeMillis() - start;
                double seconds = (elapsed / 1000.0) * mult;
                Platform.runLater(() -> liveTimeLabel.setText(String.format("t = %.3f с", seconds)));
                try { Thread.sleep(30); } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    private void finishMeasurement() {
        double m = Double.parseDouble(mField.getText());
        double h = Double.parseDouble(hField.getText());
        double r = Double.parseDouble(rField.getText());
        double measuredTime = currentExactTime + (Math.random() - 0.5) * 0.16;
        if (measuredTime <= 0) measuredTime = 0.001;

        liveTimeLabel.setText(String.format("t = %.3f с", measuredTime));
        liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

        double expI = m * r * r * ((9.81 * measuredTime * measuredTime) / (2 * h) - 1);

        Measurement meas = new Measurement(
                idCounter++, m, r, h,
                Math.round(measuredTime * 100.0) / 100.0,
                Math.round(expI * 100000.0) / 100000.0,
                Math.round(currentTheoI * 100000.0) / 100000.0
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
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: -");
            return;
        }
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }
        double sumEps = 0;
        double sumDelta = 0;

        for (Measurement meas : data) {
            double delta = Math.abs(meas.getExpI() - meas.getTheoI());
            sumDelta += delta;
            sumEps += (delta / meas.getTheoI()) * 100;
        }

        double avgDelta = sumDelta / data.size();
        double avgEps = sumEps / data.size();

        String conclusion = String.format(
                "ОБРОБКА РЕЗУЛЬТАТІВ ЕКСПЕРИМЕНТУ ТА ЇХ АНАЛІЗ:\n" +
                        "1. Експериментальні моменти інерції I_експ розраховані за формулою (4) і занесені до таблиці.\n" +
                        "2. Теоретичний момент інерції маятника I_теор розрахований за формулою (7).\n" +
                        "3. Середня абсолютна похибка: ΔI = %.5f кг·м². Середня відносна похибка: ε = %.1f %%.\n\n" +
                        "ПОРІВНЯННЯ ТА ВИСНОВОК: Результати, одержані експериментальним та теоретичним шляхами, " +
                        "збігаються в межах похибки. Відхилення зумовлені тертям у підшипниках осі хрестовини, тертям нитки та опором повітря.",
                avgDelta, avgEps
        );

        finalResultLabel.setText(conclusion);
    }
}