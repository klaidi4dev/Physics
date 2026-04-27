package dev.ua._klaidi4_.physics.level2.lab222.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level2.lab222.model.Measurement;
import dev.ua._klaidi4_.physics.level2.lab222.view.UnifiedBridgeCanvas;
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

public class LabController222 extends BaseLabController {

    private UnifiedBridgeCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private double measuredC1 = -1;
    private double measuredC2 = -1;
    private TabPane mainTabPane;
    private ComboBox<String> targetCapCombo;
    private ComboBox<String> materialCombo;
    private TextField eps0Field, refBaTiO3Field, refSaltField, refGlassField;
    private TextField lengthAField, widthBField, thicknessDField;
    private Button startPhase1Btn;
    private ComboBox<String> connectionCombo;
    private Button startPhase2Btn;
    private Label c1StatusLabel, c2StatusLabel;
    private Label liveStatusLabel, liveCapLabel;
    private AnimationTimer measurementTimer;
    private long startTime;
    private double targetCapacitance;
    private Queue<Integer> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;
    private int autoStep = 0;

    public LabController222() {
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
        leftPanel.setPrefWidth(340);
        leftPanel.setMinWidth(340);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 222)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        mainTabPane = new TabPane();
        mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab phase1Tab = new Tab("Фаза 1: C1 та C2");
        VBox phase1Box = new VBox(12);
        phase1Box.setPadding(new Insets(10));

        targetCapCombo = new ComboBox<>(FXCollections.observableArrayList("C1", "C2"));
        targetCapCombo.getSelectionModel().selectFirst();
        targetCapCombo.setOnAction(e -> updateCanvasVisuals());

        materialCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Титанат барію (BaTiO3)", "Сегнетова сіль", "Скло"
        ));
        materialCombo.getSelectionModel().selectFirst();
        materialCombo.setOnAction(e -> updateCanvasVisuals());

        lengthAField = new TextField("10.0");
        widthBField = new TextField("10.0");
        thicknessDField = new TextField("2.0");
        thicknessDField.textProperty().addListener((o, ov, nv) -> updateCanvasVisuals());

        eps0Field = new TextField("8.854e-12");
        refBaTiO3Field = new TextField("1500.0");
        refSaltField = new TextField("4000.0");
        refGlassField = new TextField("7.0");

        TitledPane constPane = new TitledPane("Константи (ε)", new VBox(5,
                createInputGroup("ε₀ (Ф/м):", eps0Field),
                createInputGroup("ε (BaTiO3):", refBaTiO3Field),
                createInputGroup("ε (Сіль):", refSaltField),
                createInputGroup("ε (Скло):", refGlassField)
        ));
        constPane.setExpanded(false);

        phase1Box.getChildren().addAll(
                createInputGroup("Налаштовуємо конденсатор:", targetCapCombo),
                createInputGroup("Матеріал діелектрика:", materialCombo),
                createInputGroup("Довжина a (мм):", lengthAField),
                createInputGroup("Ширина b (мм):", widthBField),
                createInputGroup("Товщина d (мм):", thicknessDField),
                constPane
        );

        startPhase1Btn = new Button("▶ ВИМІРЯТИ ЄМНІСТЬ");
        startPhase1Btn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startPhase1Btn.setMaxWidth(Double.MAX_VALUE);
        startPhase1Btn.setOnAction(e -> startPhase1Measurement());
        phase1Box.getChildren().add(startPhase1Btn);

        ScrollPane scrollPhase1 = new ScrollPane(phase1Box);
        scrollPhase1.setFitToWidth(true);
        scrollPhase1.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        phase1Tab.setContent(scrollPhase1);

        Tab phase2Tab = new Tab("Фаза 2: З'єднання");
        VBox phase2Box = new VBox(12);
        phase2Box.setPadding(new Insets(10));

        c1StatusLabel = new Label("C1: Не виміряно");
        c1StatusLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
        c2StatusLabel = new Label("C2: Не виміряно");
        c2StatusLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");

        connectionCombo = new ComboBox<>(FXCollections.observableArrayList("Паралельне (C1 || C2)", "Послідовне (C1 + C2)"));
        connectionCombo.getSelectionModel().selectFirst();
        connectionCombo.setOnAction(e -> updateCanvasVisuals());
        connectionCombo.setDisable(true);

        startPhase2Btn = new Button("▶ ВИМІРЯТИ ЛАНЦЮГ");
        startPhase2Btn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startPhase2Btn.setMaxWidth(Double.MAX_VALUE);
        startPhase2Btn.setDisable(true);
        startPhase2Btn.setOnAction(e -> startPhase2Measurement());

        phase2Box.getChildren().addAll(c1StatusLabel, c2StatusLabel, new Separator(),
                createInputGroup("Тип з'єднання:", connectionCombo), startPhase2Btn);

        ScrollPane scrollPhase2 = new ScrollPane(phase2Box);
        scrollPhase2.setFitToWidth(true);
        scrollPhase2.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        phase2Tab.setContent(scrollPhase2);

        mainTabPane.getTabs().addAll(phase1Tab, phase2Tab);
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> updateCanvasVisuals());

        Button autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ (ОБИДВІ ФАЗИ)");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAuto());

        Button clearBtn = new Button("🗑 ОЧИСТИТИ ВСЕ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> resetAll());

        leftPanel.getChildren().addAll(title, mainTabPane, autoBtn, clearBtn);
        VBox.setVgrow(mainTabPane, Priority.ALWAYS);
        canvas = new UnifiedBridgeCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(220, 80);
        Label dashTitle = new Label("ВИМІРЮВАЧ Р-577");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveCapLabel = new Label("C = 0.00 pF");
        liveCapLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        liveCapLabel.setStyle("-fx-text-fill: #00ff00;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveCapLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        table.getColumns().addAll(
                createCol("№", "id"),
                createCol("Етап", "phase"),
                createCol("Деталі", "details"),
                createCol("C вимір (пФ)", "cMeasured"),
                createCol("C теор (пФ)", "cTheoretical"),
                createCol("ε розрах.", "epsilon"),
                createCol("Похибка ε/С (%)", "errorPct")
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        updateCanvasVisuals();
    }

    private TableColumn<Measurement, Object> createCol(String title, String prop) {
        TableColumn<Measurement, Object> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        return col;
    }

    private void updateCanvasVisuals() {
        try {
            if (mainTabPane == null || canvas == null) return;

            if (mainTabPane.getSelectionModel().getSelectedIndex() == 1) {
                if (connectionCombo != null && !connectionCombo.isDisabled()) {
                    String mode = connectionCombo.getSelectionModel().getSelectedIndex() == 0 ? "Parallel" : "Series";
                    canvas.setMode(mode);
                }
            } else {
                if (targetCapCombo != null && materialCombo != null && thicknessDField != null) {
                    String cap = targetCapCombo.getValue();
                    double d = Double.parseDouble(thicknessDField.getText());
                    canvas.setMode(cap);
                    canvas.updateSample(d, materialCombo.getValue());
                }
            }
        } catch (Exception ignored) {}
    }

    private double getTargetEpsilon(String material) {
        try {
            if (material.contains("BaTiO3")) return Double.parseDouble(refBaTiO3Field.getText());
            if (material.contains("сіль")) return Double.parseDouble(refSaltField.getText());
            return Double.parseDouble(refGlassField.getText());
        } catch (Exception e) { return 1.0; }
    }

    private void resetAll() {
        data.clear();
        idCounter = 1;
        measuredC1 = -1;
        measuredC2 = -1;
        c1StatusLabel.setText("C1: Не виміряно");
        c1StatusLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
        c2StatusLabel.setText("C2: Не виміряно");
        c2StatusLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
        connectionCombo.setDisable(true);
        startPhase2Btn.setDisable(true);
        updateStats();
        canvas.updateDisplay("---- pF", false);
        liveCapLabel.setText("C = 0.00 pF");
    }

    private void checkPhase2Unlock() {
        if (measuredC1 > 0 && measuredC2 > 0) {
            connectionCombo.setDisable(false);
            startPhase2Btn.setDisable(false);
        }
    }


    private void startPhase1Measurement() {
        try {
            double a = Double.parseDouble(lengthAField.getText());
            double b = Double.parseDouble(widthBField.getText());
            double d = Double.parseDouble(thicknessDField.getText());
            isAutoRunning = false;
            runPhase1Simulation(a, b, d, materialCombo.getValue(), targetCapCombo.getValue());
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа в поля розмірів.");
        }
    }

    private void runPhase1Simulation(double aMm, double bMm, double dMm, String mat, String targetCap) {
        try {
            startPhase1Btn.setDisable(true);
            targetCapCombo.setDisable(true);
            liveStatusLabel.setText("Статус: ВИМІРЮВАННЯ " + targetCap);
            liveStatusLabel.setStyle("-fx-text-fill: red;");

            double S = (aMm / 1000.0) * (bMm / 1000.0);
            double eps0 = Double.parseDouble(eps0Field.getText());
            double trueEps = getTargetEpsilon(mat);
            double cPico = ((eps0 * trueEps * S) / (dMm / 1000.0)) * 1e12;
            targetCapacitance = cPico + cPico * (Math.random() * 0.04 - 0.02);

            startTime = System.nanoTime();
            measurementTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    double elapsed = (now - startTime) / 1_000_000_000.0;
                    double fluctuatingCap = targetCapacitance * (0.8 + Math.random() * 0.4);
                    canvas.updateDisplay(String.format("%.1f pF", fluctuatingCap), true);
                    liveCapLabel.setText(String.format("C = %.1f pF", fluctuatingCap));

                    if (elapsed >= 1.5) {
                        this.stop();
                        finishPhase1Measurement(aMm, bMm, dMm, S, mat, eps0, targetCap);
                    }
                }
            };
            measurementTimer.start();
        } catch (Exception e) {
            startPhase1Btn.setDisable(false);
            targetCapCombo.setDisable(false);
        }
    }

    private void finishPhase1Measurement(double a, double b, double d, double S, String mat, double eps0, String targetCap) {
        canvas.updateDisplay(String.format("%.2f pF", targetCapacitance), false);
        liveCapLabel.setText(String.format("C = %.2f pF", targetCapacitance));
        liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

        double calcEps = (targetCapacitance * 1e-12 * (d / 1000.0)) / (eps0 * S);
        double theoEps = getTargetEpsilon(mat);
        double errPct = Math.abs(calcEps - theoEps) / theoEps * 100.0;

        double theoCap = ((eps0 * theoEps * S) / (d / 1000.0)) * 1e12;

        Measurement meas = new Measurement(idCounter++, "Фаза 1 (Властивості)", targetCap + ": " + mat,
                Math.round(targetCapacitance * 100.0) / 100.0,
                Math.round(theoCap * 100.0) / 100.0,
                Math.round(calcEps),
                Math.round(errPct * 100.0) / 100.0);
        data.add(meas);

        if (targetCap.equals("C1")) {
            measuredC1 = targetCapacitance;
            c1StatusLabel.setText(String.format("C1: %.2f пФ (%s)", measuredC1, mat));
            c1StatusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        } else {
            measuredC2 = targetCapacitance;
            c2StatusLabel.setText(String.format("C2: %.2f пФ (%s)", measuredC2, mat));
            c2StatusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        }

        checkPhase2Unlock();
        updateStats();
        startPhase1Btn.setDisable(false);
        targetCapCombo.setDisable(false);

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(1000); Platform.runLater(this::processAutoStep); } catch (Exception ignored) {}
            }).start();
        }
    }


    private void startPhase2Measurement() {
        if (measuredC1 < 0 || measuredC2 < 0) return;
        isAutoRunning = false;
        runPhase2Simulation(connectionCombo.getSelectionModel().getSelectedIndex() == 0);
    }

    private void runPhase2Simulation(boolean isParallel) {
        startPhase2Btn.setDisable(true);
        connectionCombo.setDisable(true);
        liveStatusLabel.setText("Статус: ВИМІРЮВАННЯ ЛАНЦЮГА");
        liveStatusLabel.setStyle("-fx-text-fill: red;");

        double theoCombinedC;
        if (isParallel) {
            theoCombinedC = measuredC1 + measuredC2;
        } else {
            theoCombinedC = (measuredC1 * measuredC2) / (measuredC1 + measuredC2);
        }

        targetCapacitance = theoCombinedC + theoCombinedC * (Math.random() * 0.04 - 0.02);

        startTime = System.nanoTime();
        measurementTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double elapsed = (now - startTime) / 1_000_000_000.0;
                double fluctuatingCap = targetCapacitance * (0.9 + Math.random() * 0.2);
                canvas.updateDisplay(String.format("%.1f pF", fluctuatingCap), true);
                liveCapLabel.setText(String.format("C = %.1f pF", fluctuatingCap));

                if (elapsed >= 1.5) {
                    this.stop();
                    finishPhase2Measurement(theoCombinedC, isParallel);
                }
            }
        };
        measurementTimer.start();
    }

    private void finishPhase2Measurement(double theoCombinedC, boolean isParallel) {
        canvas.updateDisplay(String.format("%.2f pF", targetCapacitance), false);
        liveCapLabel.setText(String.format("C = %.2f pF", targetCapacitance));
        liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

        double errPct = Math.abs(targetCapacitance - theoCombinedC) / theoCombinedC * 100.0;
        String typeStr = isParallel ? "Паралельне (C1||C2)" : "Послідовне (C1+C2)";

        Measurement meas = new Measurement(idCounter++, "Фаза 2 (Закони)", typeStr,
                Math.round(targetCapacitance * 100.0) / 100.0,
                Math.round(theoCombinedC * 100.0) / 100.0,
                0,
                Math.round(errPct * 100.0) / 100.0);
        data.add(meas);

        updateStats();
        startPhase2Btn.setDisable(false);
        connectionCombo.setDisable(false);

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(1000); Platform.runLater(this::processAutoStep); } catch (Exception ignored) {}
            }).start();
        }
    }


    private void startAuto() {
        resetAll();
        isAutoRunning = true;
        autoStep = 0;
        processAutoStep();
    }

    private void processAutoStep() {
        if (!isAutoRunning) return;

        if (autoStep == 0) {
            mainTabPane.getSelectionModel().select(0);
            targetCapCombo.getSelectionModel().select(0);
            materialCombo.getSelectionModel().select(0);
            lengthAField.setText("10.0");
            widthBField.setText("10.0");
            thicknessDField.setText("2.0");
            updateCanvasVisuals();

            autoStep++;
            runPhase1Simulation(10.0, 10.0, 2.0, materialCombo.getValue(), "C1");

        } else if (autoStep == 1) {
            targetCapCombo.getSelectionModel().select(1);
            materialCombo.getSelectionModel().select(2);
            lengthAField.setText("15.0");
            widthBField.setText("15.0");
            thicknessDField.setText("1.0");
            updateCanvasVisuals();

            autoStep++;
            runPhase1Simulation(15.0, 15.0, 1.0, materialCombo.getValue(), "C2");

        } else if (autoStep == 2) {
            mainTabPane.getSelectionModel().select(1);
            connectionCombo.getSelectionModel().select(0);
            updateCanvasVisuals();

            autoStep++;
            runPhase2Simulation(true);

        } else if (autoStep == 3) {
            connectionCombo.getSelectionModel().select(1);
            updateCanvasVisuals();

            autoStep++;
            runPhase2Simulation(false);

        } else {
            isAutoRunning = false;
            liveStatusLabel.setText("Статус: АВТО-ЗАВЕРШЕНО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
        }
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

        StringBuilder sb = new StringBuilder();
        sb.append("--- ЧАСТИНА 1: Властивості сегнетоелектриків ---\n");

        boolean phase1Done = false;
        for (Measurement m : data) {
            if (m.getPhase().contains("Фаза 1")) {
                phase1Done = true;
                sb.append(String.format("Зразок %s:\n", m.getDetails()));
                sb.append(String.format("  - Виміряна ємність: %.2f пФ\n", m.getCMeasured()));
                sb.append(String.format("  - Розраховане ε = %.0f (Еталон: %.0f). Похибка ε: %.1f%%\n",
                        m.getEpsilon(), getTargetEpsilon(m.getDetails().split(": ")[1]), m.getErrorPct()));
            }
        }
        if (!phase1Done) sb.append("  (Дані відсутні)\n");

        sb.append("\n--- ЧАСТИНА 2: Закони з'єднання конденсаторів ---\n");
        boolean phase2Done = false;
        for (Measurement m : data) {
            if (m.getPhase().contains("Фаза 2")) {
                phase2Done = true;
                sb.append(String.format("%s:\n", m.getDetails()));
                sb.append(String.format("  - Виміряна еквівалентна ємність: %.2f пФ\n", m.getCMeasured()));
                sb.append(String.format("  - Теоретична ємність: %.2f пФ. Похибка: %.1f%%\n",
                        m.getCTheoretical(), m.getErrorPct()));
            }
        }
        if (!phase2Done) {
            sb.append("  (Для аналізу необхідно виміряти C1, C2 та їх з'єднання у другій вкладці)\n");
        } else {
            sb.append("\nЗАГАЛЬНИЙ ВИСНОВОК:\n");
            sb.append("Експериментальні дані підтверджують формулу плоского конденсатора (дозволяючи обчислити діелектричну проникність матеріалів) та закони паралельного і послідовного з'єднання ємностей. Похибки знаходяться в межах норми для місткових вимірювань.");
        }

        finalResultLabel.setText(sb.toString());
    }
}