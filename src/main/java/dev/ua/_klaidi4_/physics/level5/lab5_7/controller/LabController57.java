/*
 * Лабораторна робота № 5-7 "Дифракція Фраунгофера".
 * Клас: LabController57.
 * Призначення: керує інтерфейсом лабораторної роботи, запуском досліду,
 * введенням параметрів, таблицею результатів та обробкою вимірювань.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level5.lab5_7.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level5.lab5_7.model.Measurement;
import dev.ua._klaidi4_.physics.level5.lab5_7.view.DiffractionCanvas;
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

public class LabController57 extends BaseLabController {

    private DiffractionCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;

    private TextField fieldDistanceL;
    private TextField fieldGratingD;
    private TextField fieldLambdaNm;
    private ComboBox<Integer> orderCombo;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;

    private Label liveStatusLabel;
    private Label liveOrderLabel;

    private Queue<Integer> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    /*
     * Лабораторна робота № 5-7 "Дифракція Фраунгофера".
     * Функція: LabController57.
     * Призначення: Конструктор класу, ініціалізує інтерфейс та налаштування.
     */
    public LabController57() {
        initUI();
    }

    /*
     * Лабораторна робота № 5-7 "Дифракція Фраунгофера".
     * Функція: shutdown.
     * Призначення: Зупиняє процеси та очищує ресурси при закритті вікна.
     */
    @Override
    public void shutdown() {
        isAutoRunning = false;
        autoQueue.clear();
    }

    /*
     * Лабораторна робота № 5-7 "Дифракція Фраунгофера".
     * Функція: initUI.
     * Призначення: Ініціалізує графічний інтерфейс користувача, створює панелі управління та графіки.
     */
    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 5-7)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane paramsPane = new TitledPane();
        paramsPane.setText("Параметри установки");
        paramsPane.setCollapsible(false);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        fieldDistanceL = new TextField("1.0");
        fieldGratingD = new TextField("10.0");
        fieldLambdaNm = new TextField("632.8");

        fieldDistanceL.textProperty().addListener((o, old, val) -> applyPhysicsSettings());
        fieldGratingD.textProperty().addListener((o, old, val) -> applyPhysicsSettings());
        fieldLambdaNm.textProperty().addListener((o, old, val) -> applyPhysicsSettings());

        paramsBox.getChildren().addAll(
                createInputGroup("Відстань до екрану L (м):", fieldDistanceL),
                createInputGroup("Період решітки d (мкм):", fieldGratingD),
                createInputGroup("Еталонна довжина хвилі λ (нм):", fieldLambdaNm)
        );
        paramsPane.setContent(paramsBox);

        TitledPane controlPane = new TitledPane();
        controlPane.setText("Управління вимірами");
        controlPane.setCollapsible(false);

        VBox controlBox = new VBox(12);
        controlBox.setPadding(new Insets(5));

        orderCombo = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3));
        orderCombo.getSelectionModel().selectFirst();
        orderCombo.setMaxWidth(Double.MAX_VALUE);
        orderCombo.setOnAction(e -> applyPhysicsSettings());

        controlBox.getChildren().add(createInputGroup("Порядок дифракції m:", orderCombo));
        controlPane.setContent(controlBox);

        startBtn = new Button("▶ ВИМІРЯТИ ВІДСТАНЬ l_m");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startScan());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ ЛАБИ");
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

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, paramsPane, controlPane, startBtn, autoBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new DiffractionCanvas(600, 360);

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

        Label dashTitle = new Label("СЕНСОРНА ПАНЕЛЬ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveOrderLabel = new Label("Порядок: m = 1");
        liveOrderLabel.setStyle("-fx-text-fill: #00ff00;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveOrderLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        centerPanel.setStyle("-fx-background-color: #ffffff;");
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);
        table.getColumns().addAll(
                createCol("Порядок m", "order"),
                createCol("L (м)", "lengthL"),
                createCol("d (мкм)", "d"),
                createCol("l_m (мм)", "lm"),
                createCol("λ обчисл. (нм)", "lambda"),
                createCol("R (роздільна)", "resolution")
        );
        table.setPrefHeight(130);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox statsBox = createStatsBox();

        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);
        applyPhysicsSettings();
        updateStats();
    }

    private TableColumn<Measurement, Object> createCol(String title, String property) {
        TableColumn<Measurement, Object> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }

    /*
     * Лабораторна робота № 5-7 "Дифракція Фраунгофера".
     * Функція: applyPhysicsSettings.
     * Призначення: Застосовує поточні налаштування до симуляції.
     */
    private void applyPhysicsSettings() {
        try {
            double L = Double.parseDouble(fieldDistanceL.getText());
            double d = Double.parseDouble(fieldGratingD.getText());
            double lambda = Double.parseDouble(fieldLambdaNm.getText());
            int m = orderCombo.getValue();
            canvas.setPhysicsParameters(L, d, m, lambda);
            liveOrderLabel.setText("Порядок: m = " + m);
        } catch (NumberFormatException ignored) {}
    }

    /*
     * Лабораторна робота № 5-7 "Дифракція Фраунгофера".
     * Функція: setControlsDisable.
     * Призначення: Встановлює нове значення для вказаного параметра.
     */
    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        fieldDistanceL.setDisable(disable);
        fieldGratingD.setDisable(disable);
        fieldLambdaNm.setDisable(disable);
        orderCombo.setDisable(disable);
    }

    /*
     * Лабораторна робота № 5-7 "Дифракція Фраунгофера".
     * Функція: startScan.
     * Призначення: Запускає процес симуляції або відповідний режим роботи.
     */
    private void startScan() {
        try {
            isAutoRunning = false;
            setControlsDisable(true);
            liveStatusLabel.setText("Статус: СКАНУВАННЯ...");
            liveStatusLabel.setStyle("-fx-text-fill: red;");

            double L = Double.parseDouble(fieldDistanceL.getText());
            double d = Double.parseDouble(fieldGratingD.getText());
            double lambda = Double.parseDouble(fieldLambdaNm.getText());
            int m = orderCombo.getValue();

            canvas.startMeasurementScan();

            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> {
                    recordMeasurement(L, d, m, lambda);
                    setControlsDisable(false);
                    liveStatusLabel.setText("Статус: ГОТОВО");
                    liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
                });
            }).start();
        } catch (Exception ignored) {}
    }

    /*
     * Лабораторна робота № 5-7 "Дифракція Фраунгофера".
     * Функція: startAuto.
     * Призначення: Запускає процес симуляції або відповідний режим роботи.
     */
    private void startAuto() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();
        autoQueue.add(1);
        autoQueue.add(2);
        autoQueue.add(3);

        isAutoRunning = true;
        processNextAuto();
    }

    /*
     * Лабораторна робота № 5-7 "Дифракція Фраунгофера".
     * Функція: processNextAuto.
     * Призначення: Обробляє поточний крок симуляції або дію користувача.
     */
    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoRunning = false;
            setControlsDisable(false);
            liveStatusLabel.setText("Статус: ГОТОВО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            return;
        }

        setControlsDisable(true);
        int m = autoQueue.poll();
        orderCombo.setValue(m);
        applyPhysicsSettings();

        liveStatusLabel.setText("Статус: АВТО-СКАНУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: red;");

        double L = Double.parseDouble(fieldDistanceL.getText());
        double d = Double.parseDouble(fieldGratingD.getText());
        double lambda = Double.parseDouble(fieldLambdaNm.getText());

        canvas.startMeasurementScan();

        new Thread(() -> {
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> {
                recordMeasurement(L, d, m, lambda);
                if (isAutoRunning) {
                    new Thread(() -> {
                        try { Thread.sleep(800); } catch (InterruptedException ignored) {}
                        Platform.runLater(this::processNextAuto);
                    }).start();
                }
            });
        }).start();
    }

    /*
     * Лабораторна робота № 5-7 "Дифракція Фраунгофера".
     * Функція: recordMeasurement.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void recordMeasurement(double L, double d, int m, double lambdaTrue) {
        double lmExact = L * Math.tan(Math.asin((m * lambdaTrue * 1e-9) / (d * 1e-6))) * 1000;
        double noise = (Math.random() - 0.5) * 0.4;
        double lmMeasured = lmExact + noise;
        double lambdaCalc = (d * 1e-6 * Math.sin(Math.atan(lmMeasured / 1000.0 / L))) / m * 1e9;

        int totalSlitsN = 300;
        int resolution = m * totalSlitsN;

        Measurement meas = new Measurement(
                idCounter++, m, L, d,
                Math.round(lmMeasured * 100) / 100.0,
                Math.round(lambdaCalc * 10) / 10.0,
                resolution
        );
        data.add(meas);
        updateStats();
    }

    /*
     * Лабораторна робота № 5-7 "Дифракція Фраунгофера".
     * Функція: updateStats.
     * Призначення: Розраховує середні значення та похибки на основі даних у таблиці.
     */
    private void updateStats() {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: Очікування вимірювань...");
            return;
        }
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }
        double sumLambda = 0;
        for (Measurement m : data) {
            sumLambda += m.getLambda();
        }
        double avgLambda = sumLambda / data.size();
        double trueLambda = Double.parseDouble(fieldLambdaNm.getText());

        double absErr = Math.abs(avgLambda - trueLambda);
        double relErr = (absErr / trueLambda) * 100.0;

        StringBuilder sb = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ:\n");
        sb.append(String.format("1. Обчислені довжини хвиль: Середня λ = %.2f нм.\n", avgLambda));

        sb.append("2. Роздільна здатність R (при діаметрі пучка D=3 мм, d=10 мкм, N=300): ");
        for (Measurement m : data) {
            sb.append(String.format("m=%d -> R=%d; ", m.getOrder(), m.getResolution()));
        }
        sb.append("\n");

        sb.append("3. Аналіз: Зі збільшенням порядку m роздільна здатність R лінійно зростає.\n");
        sb.append(String.format("4. Похибки: Абсолютна Δλ = %.2f нм, Відносна ε = %.1f %%.\n", absErr, relErr));
        sb.append("5. Висновок: Метод дифракційної решітки дозволяє з високою точністю визначати довжину хвилі світла.");

        finalResultLabel.setText(sb.toString());
    }
}