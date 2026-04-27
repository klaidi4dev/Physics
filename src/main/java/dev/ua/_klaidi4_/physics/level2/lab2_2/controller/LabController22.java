package dev.ua._klaidi4_.physics.level2.lab2_2.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level2.lab2_2.model.Measurement;
import dev.ua._klaidi4_.physics.level2.lab2_2.view.CapacitorCanvas;
import javafx.animation.AnimationTimer;
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

public class LabController22 extends BaseLabController {

    private CapacitorCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;

    private TextField c1Field;
    private TextField c2Field;
    private ComboBox<String> connectionCombo;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;

    private Label liveStatusLabel;
    private Label liveCapLabel;

    private Queue<Integer> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;
    private AnimationTimer measurementTimer;
    private long startTime;

    public LabController22() {
        initUI();
    }

    @Override
    public void shutdown() {
        if (measurementTimer != null) measurementTimer.stop();
        isAutoRunning = false;
        autoQueue.clear();
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(320);
        leftPanel.setMinWidth(320);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 2-2)");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));

        TitledPane paramsPane = new TitledPane();
        paramsPane.setText("Параметри конденсаторів");
        paramsPane.setCollapsible(false);
        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        c1Field = new TextField("4.7");
        c2Field = new TextField("2.2");

        c1Field.textProperty().addListener((o, old, val) -> updateCanvas());
        c2Field.textProperty().addListener((o, old, val) -> updateCanvas());

        paramsBox.getChildren().addAll(
                createInputGroup("Ємність C1 (мкФ):", c1Field),
                createInputGroup("Ємність C2 (мкФ):", c2Field)
        );
        paramsPane.setContent(paramsBox);

        TitledPane controlPane = new TitledPane();
        controlPane.setText("Управління вимірами");
        controlPane.setCollapsible(false);
        VBox controlBox = new VBox(12);
        controlBox.setPadding(new Insets(5));

        connectionCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Тільки C1", "Тільки C2", "Паралельне (C1 || C2)", "Послідовне (C1 + C2)"
        ));
        connectionCombo.getSelectionModel().selectFirst();
        connectionCombo.setMaxWidth(Double.MAX_VALUE);
        connectionCombo.setOnAction(e -> updateCanvas());

        controlBox.getChildren().add(createInputGroup("Тип з'єднання:", connectionCombo));
        controlPane.setContent(controlBox);

        startBtn = new Button("▶ ВИМІРЯТИ ЄМНІСТЬ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startMeasurement());

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
            liveCapLabel.setText("C = 0.00 мкФ");
        });

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, paramsPane, controlPane, startBtn, autoBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new CapacitorCanvas(600, 360);

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

        Label dashTitle = new Label("ВИМІРЮВАЛЬНИЙ МІСТ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ГОТОВО");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveCapLabel = new Label("C = 0.00 мкФ");
        liveCapLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        liveCapLabel.setStyle("-fx-text-fill: #00ff00;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveCapLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        centerPanel.setStyle("-fx-background-color: #ffffff;");
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);
        table.getColumns().addAll(
                createCol("№", "id"),
                createCol("З'єднання", "connectionType"),
                createCol("C вимір (мкФ)", "cMeasured"),
                createCol("C теор (мкФ)", "cTheoretical"),
                createCol("Похибка ε (%)", "errorPct")
        );
        table.setPrefHeight(150);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox statsBox = createStatsBox();

        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);
        updateCanvas();
        updateStats();
    }

    private TableColumn<Measurement, Object> createCol(String title, String property) {
        TableColumn<Measurement, Object> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }

    private void updateCanvas() {
        int mode = connectionCombo.getSelectionModel().getSelectedIndex();
        canvas.setConnectionMode(mode);
        try {
            double c1 = Double.parseDouble(c1Field.getText());
            double c2 = Double.parseDouble(c2Field.getText());
            canvas.updateValues(c1, c2);
        } catch (NumberFormatException ignored) {}
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        c1Field.setDisable(disable);
        c2Field.setDisable(disable);
        connectionCombo.setDisable(disable);
    }

    private void startMeasurement() {
        try {
            double c1 = Double.parseDouble(c1Field.getText());
            double c2 = Double.parseDouble(c2Field.getText());
            int mode = connectionCombo.getSelectionModel().getSelectedIndex();

            setControlsDisable(true);
            liveStatusLabel.setText("Статус: БАЛАНСУВАННЯ МОСТА...");
            liveStatusLabel.setStyle("-fx-text-fill: red;");
            canvas.startSimulation();

            double exactC = 0;
            if (mode == 0) exactC = c1;
            else if (mode == 1) exactC = c2;
            else if (mode == 2) exactC = c1 + c2;
            else if (mode == 3) exactC = (c1 * c2) / (c1 + c2);

            final double targetC = exactC;

            startTime = System.nanoTime();
            measurementTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    double elapsed = (now - startTime) / 1_000_000_000.0;
                    double displayC = targetC * (0.5 + Math.random());
                    liveCapLabel.setText(String.format("C = %.2f мкФ", displayC));

                    if (elapsed >= 1.5) {
                        this.stop();
                        finishMeasurement(targetC, mode, connectionCombo.getValue());
                    }
                }
            };
            measurementTimer.start();
        } catch (NumberFormatException e) {
            showAlert("Помилка", "Введіть коректні значення ємностей.");
        }
    }

    private void finishMeasurement(double exactC, int mode, String modeName) {
        canvas.stopSimulation();
        double noise = (Math.random() - 0.5) * 0.05 * exactC;
        double measuredC = exactC + noise;

        liveCapLabel.setText(String.format("C = %.2f мкФ", measuredC));
        liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

        double error = Math.abs(measuredC - exactC) / exactC * 100.0;

        Measurement m = new Measurement(idCounter++, modeName,
                Math.round(measuredC * 100.0) / 100.0,
                Math.round(exactC * 100.0) / 100.0,
                Math.round(error * 100.0) / 100.0);

        data.add(m);
        updateStats();

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(1000); } catch (Exception ignored) {}
                Platform.runLater(this::processNextAuto);
            }).start();
        } else {
            setControlsDisable(false);
        }
    }

    private void startAuto() {
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
            setControlsDisable(false);
            liveStatusLabel.setText("Статус: ГОТОВО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            return;
        }

        int mode = autoQueue.poll();
        connectionCombo.getSelectionModel().select(mode);
        updateCanvas();
        startMeasurement();
    }

    private void updateStats() {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: Очікування вимірювань...");
            return;
        }
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }
        double c1_meas = -1, c2_meas = -1, cPar_meas = -1, cSer_meas = -1;

        for (Measurement m : data) {
            if (m.getConnectionType().contains("Тільки C1")) c1_meas = m.getCMeasured();
            if (m.getConnectionType().contains("Тільки C2")) c2_meas = m.getCMeasured();
            if (m.getConnectionType().contains("Паралельне")) cPar_meas = m.getCMeasured();
            if (m.getConnectionType().contains("Послідовне")) cSer_meas = m.getCMeasured();
        }

        StringBuilder sb = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ ВИМІРЮВАННЯ І ЇХ АНАЛІЗ:\n");
        sb.append("1. Виміряні ємності:\n");
        if (c1_meas > 0) sb.append(String.format("   C1 = %.2f мкФ\n", c1_meas));
        if (c2_meas > 0) sb.append(String.format("   C2 = %.2f мкФ\n", c2_meas));
        if (cPar_meas > 0) sb.append(String.format("   C_парал = %.2f мкФ\n", cPar_meas));
        if (cSer_meas > 0) sb.append(String.format("   C_послід = %.2f мкФ\n", cSer_meas));

        if (c1_meas > 0 && c2_meas > 0 && (cPar_meas > 0 || cSer_meas > 0)) {
            sb.append("2. Перевірка законів сполучення конденсаторів:\n");

            if (cPar_meas > 0) {
                double cPar_theor = c1_meas + c2_meas;
                double errPar = Math.abs(cPar_meas - cPar_theor) / cPar_theor * 100.0;
                sb.append(String.format("   Теоретичне паралельне (C1+C2) = %.2f мкФ. Відносна похибка: %.1f%%\n", cPar_theor, errPar));
            }
            if (cSer_meas > 0) {
                double cSer_theor = (c1_meas * c2_meas) / (c1_meas + c2_meas);
                double errSer = Math.abs(cSer_meas - cSer_theor) / cSer_theor * 100.0;
                sb.append(String.format("   Теоретичне послідовне (C1*C2/(C1+C2)) = %.2f мкФ. Відносна похибка: %.1f%%\n", cSer_theor, errSer));
            }
            sb.append("3. Висновок: Теоретичні розрахунки збігаються з експериментальними даними в межах допустимої похибки. Закони сполучення підтверджено.");
        } else {
            sb.append("2. Для перевірки законів сполучення та розрахунку похибок необхідно виміряти C1, C2 та їх комбінації.");
        }

        finalResultLabel.setText(sb.toString());
    }
}