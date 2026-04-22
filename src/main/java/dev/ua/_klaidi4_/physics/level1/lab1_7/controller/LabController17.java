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
    private TextField m1Field;
    private TextField bigRField;
    private TextField rField;
    private TextField hField;
    private TextField i0Field;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveTimeLabel;
    private Queue<AutoTestParam> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;
    private static class AutoTestParam {
        double m, bigR;
        AutoTestParam(double m, double bigR) {
            this.m = m; this.bigR = bigR;
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

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри маятника");
        labPane.setCollapsible(true);
        labPane.setExpanded(true);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        mField = new TextField("0.050");
        m1Field = new TextField("0.150");
        bigRField = new TextField("0.10");
        rField = new TextField("0.02");
        hField = new TextField("1.0");
        i0Field = new TextField("0.001");

        mField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        m1Field.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        bigRField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        rField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        hField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());

        paramsBox.getChildren().addAll(
                createInputGroup("Маса падаючого вантажу m (кг):", mField),
                createInputGroup("Маса 1 тягарця на хрестовині m1 (кг):", m1Field),
                createInputGroup("Відстань тягарців від осі R (м):", bigRField),
                createInputGroup("Радіус шківа r (м):", rField),
                createInputGroup("Висота падіння h (м):", hField),
                createInputGroup("Момент інерції порожнього I0 (кг·м²):", i0Field)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(280);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        startBtn = new Button("▶ ВІДПУСТИТИ ВАНТАЖ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ (5 дослідів)");
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

        leftPanel.getChildren().addAll(title, labPane, startBtn, autoBtn, clearBtn);

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
        TableColumn<Measurement, Double> rCol = new TableColumn<>("R (м)");
        rCol.setCellValueFactory(new PropertyValueFactory<>("bigR"));
        TableColumn<Measurement, Double> tCol = new TableColumn<>("t (с)");
        tCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        TableColumn<Measurement, Double> aCol = new TableColumn<>("a (м/с²)");
        aCol.setCellValueFactory(new PropertyValueFactory<>("accel"));
        TableColumn<Measurement, Double> expCol = new TableColumn<>("I експ (кг·м²)");
        expCol.setCellValueFactory(new PropertyValueFactory<>("expI"));
        TableColumn<Measurement, Double> theoCol = new TableColumn<>("I теор (кг·м²)");
        theoCol.setCellValueFactory(new PropertyValueFactory<>("theoI"));

        table.getColumns().addAll(idCol, mCol, rCol, tCol, aCol, expCol, theoCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(130);

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
            double bigR = Double.parseDouble(bigRField.getText());
            double h = Double.parseDouble(hField.getText());
            canvas.setParameters(r, bigR, h);
        } catch (NumberFormatException ignored) {}
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        mField.setDisable(disable);
        m1Field.setDisable(disable);
        bigRField.setDisable(disable);
        rField.setDisable(disable);
        hField.setDisable(disable);
        i0Field.setDisable(disable);
    }

    private void startManual() {
        try {
            Double.parseDouble(mField.getText());
            Double.parseDouble(bigRField.getText());
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

        autoQueue.add(new AutoTestParam(0.05, 0.10));
        autoQueue.add(new AutoTestParam(0.05, 0.15));
        autoQueue.add(new AutoTestParam(0.08, 0.15));
        autoQueue.add(new AutoTestParam(0.10, 0.15));
        autoQueue.add(new AutoTestParam(0.10, 0.20));

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
        bigRField.setText(String.valueOf(param.bigR));
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
        double m1 = Double.parseDouble(m1Field.getText());
        double R = Double.parseDouble(bigRField.getText());
        double r = Double.parseDouble(rField.getText());
        double h = Double.parseDouble(hField.getText());
        double I0 = Double.parseDouble(i0Field.getText());

        currentTheoI = I0 + 4 * m1 * R * R;
        double a = (m * 9.81) / (m + currentTheoI / (r * r));

        currentExactTime = Math.sqrt(2 * h / a);
        canvas.startSimulation(currentExactTime);

        final double exactFinalTime = currentExactTime;
        new Thread(() -> {
            long start = System.currentTimeMillis();
            long targetMs = (long) (exactFinalTime * 1000);

            while (System.currentTimeMillis() - start < targetMs) {
                long elapsed = System.currentTimeMillis() - start;
                double seconds = elapsed / 1000.0;
                Platform.runLater(() -> liveTimeLabel.setText(String.format("t = %.3f с", seconds)));
                try { Thread.sleep(30); } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    private void finishMeasurement() {
        double m = Double.parseDouble(mField.getText());
        double R = Double.parseDouble(bigRField.getText());
        double r = Double.parseDouble(rField.getText());
        double h = Double.parseDouble(hField.getText());
        double measuredTime = currentExactTime + (Math.random() - 0.5) * 0.08;
        if (measuredTime <= 0) measuredTime = 0.001;

        liveTimeLabel.setText(String.format("t = %.3f с", measuredTime));
        liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

        double expA = (2 * h) / (measuredTime * measuredTime);
        double expI = m * r * r * ((9.81 / expA) - 1);

        Measurement meas = new Measurement(
                idCounter++, m, R,
                Math.round(measuredTime * 1000.0) / 1000.0,
                Math.round(expA * 1000.0) / 1000.0,
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
                        "1. Експериментальні моменти інерції маятника I_експ для різних мас m та відстаней R розраховані за формулою (4) і занесені до таблиці.\n" +
                        "2. Середня абсолютна похибка експерименту: ΔI = %.5f кг·м². Відносна похибка: ε = %.1f %%.\n" +
                        "3. Теоретичні значення моменту інерції I_теор розраховані за формулою (7) і подані в останній колонці таблиці.\n" +
                        "4. ПОРІВНЯННЯ ТА ВИСНОВОК: Результати, одержані експериментальним та теоретичним шляхом, збігаються. Незначні відхилення зумовлені тертям у підшипниках осі та опором повітря, якими нехтує ідеальна модель.",
                avgDelta, avgEps
        );

        finalResultLabel.setText(conclusion);
    }
}