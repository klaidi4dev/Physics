package dev.ua._klaidi4_.physics.level1.lab1_11.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level1.lab1_11.model.Measurement;
import dev.ua._klaidi4_.physics.level1.lab1_11.view.MaxwellCanvas;
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

public class LabController111 extends BaseLabController {

    private MaxwellCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField mField;
    private TextField rAxisField;
    private TextField rDiskField;
    private TextField hField;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveTimeLabel;
    private Queue<Integer> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    public LabController111() {
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
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 1-11)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри установки");
        labPane.setCollapsible(false);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        mField = new TextField("0.400");
        rAxisField = new TextField("0.005");
        rDiskField = new TextField("0.05");
        hField = new TextField("0.50");

        paramsBox.getChildren().addAll(
                createInputGroup("Маса маятника m (кг):", mField),
                createInputGroup("Радіус осі r (м):", rAxisField),
                createInputGroup("Радіус диска R (м):", rDiskField),
                createInputGroup("Висота падіння h (м):", hField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(260);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        startBtn = new Button("▶ ВІДПУСТИТИ МАЯТНИК");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ СЕРІЯ ВИМІРІВ (5 разів)");
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
            liveTimeLabel.setText("t = 0.00 с");
        });

        leftPanel.getChildren().addAll(title, labPane, startBtn, autoBtn, clearBtn);

        canvas = new MaxwellCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(220, 60);
        Label dashTitle = new Label("СЕКУНДОМІР");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveTimeLabel = new Label("t = 0.00 с");
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
        TableColumn<Measurement, Double> hCol = new TableColumn<>("h (м)");
        hCol.setCellValueFactory(new PropertyValueFactory<>("h"));
        TableColumn<Measurement, Double> tCol = new TableColumn<>("t (с)");
        tCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        TableColumn<Measurement, Double> expCol = new TableColumn<>("I експ (кг·м²)");
        expCol.setCellValueFactory(new PropertyValueFactory<>("expI"));
        TableColumn<Measurement, Double> theoCol = new TableColumn<>("I теор (кг·м²)");
        theoCol.setCellValueFactory(new PropertyValueFactory<>("theoI"));

        table.getColumns().addAll(idCol, mCol, hCol, tCol, expCol, theoCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(140);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        mField.setDisable(disable);
        rAxisField.setDisable(disable);
        rDiskField.setDisable(disable);
        hField.setDisable(disable);
    }

    private void startManual() {
        try {
            Double.parseDouble(mField.getText());
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

        for (int i = 0; i < 5; i++) autoQueue.add(1);

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
        autoQueue.poll();
        runSimulation();
    }

    private void runSimulation() {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: ПАДІННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: cyan;");

        double m = Double.parseDouble(mField.getText());
        double r = Double.parseDouble(rAxisField.getText());
        double R = Double.parseDouble(rDiskField.getText());
        double h = Double.parseDouble(hField.getText());
        double iTheory = 0.5 * m * R * R;
        double a = (m * 9.81) / (m + iTheory / (r * r));
        double exactTime = Math.sqrt(2 * h / a);
        final double finalMeasuredTime = exactTime + (Math.random() - 0.5) * 0.16;
        final double finalITheory = iTheory;

        canvas.setOnFinishCallback(() -> Platform.runLater(() -> finishMeasurement(finalMeasuredTime, finalITheory)));
        canvas.startSimulation(finalMeasuredTime);
        new Thread(() -> {
            long start = System.currentTimeMillis();
            long targetMs = (long) (finalMeasuredTime * 1000);

            while (System.currentTimeMillis() - start < targetMs) {
                long elapsed = System.currentTimeMillis() - start;
                double seconds = elapsed / 1000.0;
                Platform.runLater(() -> liveTimeLabel.setText(String.format("t = %.3f с", seconds)));
                try { Thread.sleep(30); } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    private void finishMeasurement(double measuredTime, double iTheory) {
        liveTimeLabel.setText(String.format("t = %.3f с", measuredTime));
        liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

        double m = Double.parseDouble(mField.getText());
        double r = Double.parseDouble(rAxisField.getText());
        double h = Double.parseDouble(hField.getText());

        double expA = (2 * h) / (measuredTime * measuredTime);
        double iExp = m * r * r * ((9.81 / expA) - 1);

        Measurement meas = new Measurement(
                idCounter++, m, h,
                Math.round(measuredTime * 1000.0) / 1000.0,
                Math.round(iExp * 1000000.0) / 1000000.0,
                Math.round(iTheory * 1000000.0) / 1000000.0
        );
        data.add(meas);
        updateStats();

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(1200); Platform.runLater(this::processNextAuto); }
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
        double sumT = 0;
        for (Measurement meas : data) {
            sumT += meas.getTime();
        }

        double avgT = sumT / data.size();
        double m = Double.parseDouble(mField.getText());
        double r = Double.parseDouble(rAxisField.getText());
        double h = Double.parseDouble(hField.getText());
        double avgExpA = (2 * h) / (avgT * avgT);
        double avgExpI = m * r * r * ((9.81 / avgExpA) - 1);
        double theoI = data.get(0).getTheoI();
        double deltaI = Math.abs(avgExpI - theoI);
        double epsI = (deltaI / theoI) * 100;

        String conclusion = String.format(
                "ОБРОБКА РЕЗУЛЬТАТІВ ЕКСПЕРИМЕНТУ ТА ЇХ АНАЛІЗ:\n" +
                        "1. Середнє значення часу падіння маятника: t_ср = %.3f с.\n" +
                        "2. Експериментальний момент інерції (за ф. 10): I_експ = %.6f кг·м².\n" +
                        "3. Теоретичний момент інерції (за ф. 11): I_теор = %.6f кг·м².\n" +
                        "4. Похибки вимірювань: абсолютна ΔI = %.6f кг·м², відносна ε = %.1f %%.\n" +
                        "5. ВИСНОВОК: Одержане експериментальне значення моменту інерції добре збігається з теоретичним. " +
                        "Це експериментально підтверджує закон збереження механічної енергії при складному русі тіла.",
                avgT, avgExpI, theoI, deltaI, epsI
        );

        finalResultLabel.setText(conclusion);
    }
}