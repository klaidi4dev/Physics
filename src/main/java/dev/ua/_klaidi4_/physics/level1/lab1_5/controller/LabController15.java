package dev.ua._klaidi4_.physics.level1.lab1_5.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level1.lab1_5.model.Measurement;
import dev.ua._klaidi4_.physics.level1.lab1_5.view.ChronometerCanvas;
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

public class LabController15 extends BaseLabController {

    private ChronometerCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField mField;
    private TextField lField;
    private Slider angleSlider;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label vLabel;
    private Label tauLabel;
    private Queue<Double> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;
    private double currentV;
    private double currentTau;
    private double currentForce;

    public LabController15() {
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

        Label title = new Label("Система управління (Лаб 1-5)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри установки");
        labPane.setCollapsible(false);
        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        mField = new TextField("0.150");
        lField = new TextField("0.5");

        paramsBox.getChildren().addAll(
                createInputGroup("Маса кулі m (кг):", mField),
                createInputGroup("Довжина підвісу L (м):", lField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(150);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        TitledPane physicsPane = new TitledPane();
        physicsPane.setText("Кінематика");
        VBox physBox = new VBox(5);
        physBox.setPadding(new Insets(5));

        Label angleLabel = new Label("Кут відхилення α: 30°");
        angleSlider = new Slider(10, 60, 30);
        angleSlider.setShowTickMarks(true);
        angleSlider.setShowTickLabels(true);
        angleSlider.valueProperty().addListener((o, ov, nv) ->
                angleLabel.setText(String.format("Кут відхилення α: %.1f°", nv.doubleValue()))
        );
        physBox.getChildren().addAll(angleLabel, angleSlider);
        physicsPane.setContent(physBox);

        startBtn = new Button("▶ СИМУЛЮВАТИ УДАР");
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
            tauLabel.setText("τ = 0.0 мкс");
            vLabel.setText("V = 0.00 м/с");
        });

        leftPanel.getChildren().addAll(title, labPane, physicsPane, startBtn, autoBtn, clearBtn);

        canvas = new ChronometerCanvas(600, 440);

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
        Label dashTitle = new Label("ЕЛЕКТРОННИЙ БЛОК");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        tauLabel = new Label("τ = 0.0 мкс");
        tauLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        tauLabel.setStyle("-fx-text-fill: #e74c3c;");
        vLabel = new Label("V = 0.00 м/с");
        vLabel.setStyle("-fx-text-fill: #3498db;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, tauLabel, vLabel);

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
        TableColumn<Measurement, Double> lCol = new TableColumn<>("L (м)");
        lCol.setCellValueFactory(new PropertyValueFactory<>("l"));
        TableColumn<Measurement, Double> angCol = new TableColumn<>("Кут (°)");
        angCol.setCellValueFactory(new PropertyValueFactory<>("angle"));
        TableColumn<Measurement, Double> vCol = new TableColumn<>("V (м/с)");
        vCol.setCellValueFactory(new PropertyValueFactory<>("v"));
        TableColumn<Measurement, Double> tauCol = new TableColumn<>("τ (мкс)");
        tauCol.setCellValueFactory(new PropertyValueFactory<>("tau"));
        TableColumn<Measurement, Double> fCol = new TableColumn<>("F (Н)");
        fCol.setCellValueFactory(new PropertyValueFactory<>("force"));

        table.getColumns().addAll(idCol, mCol, lCol, angCol, vCol, tauCol, fCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(140);

        VBox statsBox = createStatsBox();

        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        canvas.setCallbacks(() -> Platform.runLater(this::processHit));
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        mField.setDisable(disable);
        lField.setDisable(disable);
        angleSlider.setDisable(disable);
    }

    private void startManual() {
        try {
            Double.parseDouble(mField.getText());
            Double.parseDouble(lField.getText());
            isAutoRunning = false;
            runSimulation(angleSlider.getValue());
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа в поля маси та довжини.");
        }
    }

    private void startAuto() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        autoQueue.add(20.0); autoQueue.add(25.0); autoQueue.add(30.0);
        autoQueue.add(35.0); autoQueue.add(40.0);

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
        double nextAngle = autoQueue.poll();
        angleSlider.setValue(nextAngle);
        runSimulation(nextAngle);
    }

    private void runSimulation(double angle) {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: РУХ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow;");

        double m = Double.parseDouble(mField.getText());
        double L = Double.parseDouble(lField.getText());
        double angleRad = Math.toRadians(angle);
        currentV = Math.sqrt(2 * 9.81 * L * (1 - Math.cos(angleRad)));
        double baseTau = 150.0 * Math.pow(currentV, -0.2);
        currentTau = baseTau + (Math.random() - 0.5) * 6.0;
        currentForce = (m * currentV) / (currentTau * 1e-6);
        canvas.startSimulation(angle);
    }

    private void processHit() {
        double m = Double.parseDouble(mField.getText());
        double L = Double.parseDouble(lField.getText());
        double angle = angleSlider.getValue();

        liveStatusLabel.setText("Статус: КОНТАКТ!");
        liveStatusLabel.setStyle("-fx-text-fill: #e74c3c;");
        vLabel.setText(String.format("V = %.2f м/с", currentV));
        tauLabel.setText(String.format("τ = %.1f мкс", currentTau));

        Measurement meas = new Measurement(
                idCounter++, m, L, Math.round(angle * 10.0) / 10.0,
                Math.round(currentV * 100.0) / 100.0,
                Math.round(currentTau * 10.0) / 10.0,
                Math.round(currentForce)
        );
        data.add(meas);
        updateStats();

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(1500); Platform.runLater(this::processNextAuto); }
                catch (InterruptedException ignored) {}
            }).start();
        } else {
            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> {
                    setControlsDisable(false);
                    liveStatusLabel.setText("Статус: ГОТОВО");
                    liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
                });
            }).start();
        }
    }

    private void updateStats() {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: -");
            return;
        }

        double sumV = 0, sumTau = 0, sumF = 0;
        for (Measurement m : data) {
            sumV += m.getV();
            sumTau += m.getTau();
            sumF += m.getForce();
        }

        double avgV = sumV / data.size();
        double avgTau = sumTau / data.size();
        double avgF = sumF / data.size();

        double sumDeltaTau = 0;
        for (Measurement m : data) {
            sumDeltaTau += Math.abs(m.getTau() - avgTau);
        }
        double deltaTau = sumDeltaTau / data.size();
        double epsTau = (deltaTau / avgTau) * 100;

        double m = Double.parseDouble(mField.getText());

        String conclusion = String.format(
                "ОБРОБКА РЕЗУЛЬТАТІВ ТА АНАЛІЗ:\n" +
                        "1. Середня швидкість кулі перед ударом: v_ср = %.2f м/с.\n" +
                        "2. Середній час зіткнення куль: τ_ср = %.1f мкс. Абсолютна похибка: Δτ = %.1f мкс. Відносна похибка: ε = %.1f %%.\n" +
                        "3. Маса кулі m = %.3f кг. Розрахована середня сила удару: F_ср = %.0f Н.\n\n" +
                        "ПОЯСНЕННЯ: Оскільки зіткнення сталевих куль є абсолютно пружним, час їхнього електричного контакту (τ) надзвичайно малий (мікросекунди). За законом збереження імпульсу це призводить до виникнення величезної імпульсної сили удару (тисячі Ньютонів).",
                avgV, avgTau, deltaTau, epsTau, m, avgF
        );

        finalResultLabel.setText(conclusion);
    }
}