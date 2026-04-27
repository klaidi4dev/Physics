package dev.ua._klaidi4_.physics.level1.lab1_14.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level1.lab1_14.model.Measurement;
import dev.ua._klaidi4_.physics.level1.lab1_14.view.StokesCanvas;
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

public class LabController114 extends BaseLabController {

    private StokesCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField rField;
    private TextField rhoBallField;
    private TextField rhoFluidField;
    private TextField lField;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveTimeLabel;
    private Queue<Double> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;
    private final double ETA_THEO = 1.48;

    public LabController114() {
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

        Label title = new Label("Система управління (Лаб 1-14)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри установки");
        labPane.setCollapsible(false);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        rField = new TextField("0.001");
        rhoBallField = new TextField("7800");
        rhoFluidField = new TextField("1260");
        lField = new TextField("0.30");

        paramsBox.getChildren().addAll(
                createInputGroup("Радіус кульки r (м):", rField),
                createInputGroup("Густина кульки ρ (кг/м³):", rhoBallField),
                createInputGroup("Густина рідини ρ0 (кг/м³):", rhoFluidField),
                createInputGroup("Відстань між мітками L (м):", lField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(260);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        startBtn = new Button("▶ ВИНУСТИТИ КУЛЬКУ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ СЕРІЯ ВИМІРІВ (5 кульок)");
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
        TableColumn<Measurement, Double> rCol = new TableColumn<>("r (м)");
        rCol.setCellValueFactory(new PropertyValueFactory<>("r"));
        TableColumn<Measurement, Double> tCol = new TableColumn<>("t (с)");
        tCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        TableColumn<Measurement, Double> vCol = new TableColumn<>("v (м/с)");
        vCol.setCellValueFactory(new PropertyValueFactory<>("velocity"));
        TableColumn<Measurement, Double> etaCol = new TableColumn<>("η (Па·с)");
        etaCol.setCellValueFactory(new PropertyValueFactory<>("eta"));

        table.getColumns().addAll(idCol, rCol, tCol, vCol, etaCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

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
        rField.setDisable(disable);
        rhoBallField.setDisable(disable);
        rhoFluidField.setDisable(disable);
        lField.setDisable(disable);
    }

    private void startManual() {
        try {
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
        autoQueue.add(0.0010);
        autoQueue.add(0.0015);
        autoQueue.add(0.0020);
        autoQueue.add(0.0025);
        autoQueue.add(0.0030);

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
        rField.setText(String.format(java.util.Locale.US, "%.4f", autoQueue.poll()));
        runSimulation();
    }

    private void runSimulation() {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: ПАДІННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: cyan;");

        double r = Double.parseDouble(rField.getText());
        double rhoB = Double.parseDouble(rhoBallField.getText());
        double rhoF = Double.parseDouble(rhoFluidField.getText());
        double L = Double.parseDouble(lField.getText());
        double exactV = (2.0 * r * r * 9.81 * (rhoB - rhoF)) / (9.0 * ETA_THEO);
        double exactTime = L / exactV;
        final double finalMeasuredTime = exactTime + (Math.random() - 0.5) * 0.3;

        canvas.setOnFinishCallback(() -> Platform.runLater(() -> finishMeasurement(finalMeasuredTime)));
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

    private void finishMeasurement(double measuredTime) {
        liveTimeLabel.setText(String.format("t = %.3f с", measuredTime));
        liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

        double r = Double.parseDouble(rField.getText());
        double rhoB = Double.parseDouble(rhoBallField.getText());
        double rhoF = Double.parseDouble(rhoFluidField.getText());
        double L = Double.parseDouble(lField.getText());
        double vExp = L / measuredTime;
        double etaExp = (2.0 * r * r * 9.81 * (rhoB - rhoF)) / (9.0 * vExp);

        Measurement meas = new Measurement(
                idCounter++, r,
                Math.round(measuredTime * 100.0) / 100.0,
                Math.round(vExp * 1000.0) / 1000.0,
                Math.round(etaExp * 1000.0) / 1000.0
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
        double sumEta = 0;
        for (Measurement meas : data) {
            sumEta += meas.getEta();
        }

        double avgEta = sumEta / data.size();

        double sumDelta = 0;
        for (Measurement meas : data) {
            sumDelta += Math.abs(meas.getEta() - avgEta);
        }

        double avgDelta = sumDelta / data.size();
        double eps = (avgEta != 0) ? (avgDelta / avgEta) * 100 : 0;

        String conclusion = String.format(
                "ОБРОБКА РЕЗУЛЬТАТІВ ЕКСПЕРИМЕНТУ ТА ЇХ АНАЛІЗ:\n" +
                        "1. За формулою (7) вирахувано експериментальний коефіцієнт динамічної в'язкості (η) для кожної кульки окремо (див. таблицю).\n" +
                        "2. Середнє значення в'язкості: η_ср = %.3f Па·с.\n" +
                        "3. Абсолютна похибка експерименту: Δη = %.3f Па·с. Відносна похибка: ε = %.1f %%.\n" +
                        "4. ОЦІНКА РЕЗУЛЬТАТІВ: Одержане значення відповідає табличному показнику в'язкості гліцерину при кімнатній температурі. " +
                        "Метод Стокса дає високу точність вимірювання за умови, що кульки достатньо малі і рухаються вздовж осі циліндра рівномірно.",
                avgEta, avgDelta, eps
        );

        finalResultLabel.setText(conclusion);
    }
}