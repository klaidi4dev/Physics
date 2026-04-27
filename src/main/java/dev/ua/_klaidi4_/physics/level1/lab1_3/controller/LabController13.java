package dev.ua._klaidi4_.physics.level1.lab1_3.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level1.lab1_3.enums.CollisionType;
import dev.ua._klaidi4_.physics.level1.lab1_3.model.Measurement;
import dev.ua._klaidi4_.physics.level1.lab1_3.view.CollisionCanvas;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class LabController13 extends BaseLabController {

    private CollisionCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> typeComboBox;
    private TextField m1Field;
    private TextField m2Field;
    private TextField lengthField;
    private TextField angleField;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label v1Label;
    private Label u1Label;
    private Label u2Label;

    private Queue<AutoTestParam> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;

    private static class AutoTestParam {
        CollisionType type;
        double m1, m2, angle;
        AutoTestParam(CollisionType type, double m1, double m2, double angle) {
            this.type = type; this.m1 = m1; this.m2 = m2; this.angle = angle;
        }
    }

    public LabController13() {
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

        Label title = new Label("Система управління (Лаб 1-3)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри вимірювання");
        labPane.setCollapsible(false);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        typeComboBox = new ComboBox<>(FXCollections.observableArrayList("Пружний (Сталь)", "Непружний (Пластилін)"));
        typeComboBox.getSelectionModel().selectFirst();
        typeComboBox.setOnAction(e -> applyPhysicsSettings());

        m1Field = new TextField("0.169");
        m2Field = new TextField("0.169");
        lengthField = new TextField("0.41");

        m1Field.textProperty().addListener((o, oldVal, newVal) -> applyPhysicsSettings());
        m2Field.textProperty().addListener((o, oldVal, newVal) -> applyPhysicsSettings());
        lengthField.textProperty().addListener((o, oldVal, newVal) -> applyPhysicsSettings());

        paramsBox.getChildren().addAll(
                createInputGroup("Тип удару:", typeComboBox),
                createInputGroup("Маса кулі 1 (кг):", m1Field),
                createInputGroup("Маса кулі 2 (кг):", m2Field),
                createInputGroup("Довжина нитки (м):", lengthField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(250);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        labPane.setContent(scrollParams);

        TitledPane physicsPane = new TitledPane();
        physicsPane.setText("Кінематика");
        VBox physBox = new VBox(5);
        physBox.setPadding(new Insets(5));

        angleField = new TextField("15.0");
        angleField.textProperty().addListener((o, oldVal, newVal) -> applyPhysicsSettings());
        physBox.getChildren().addAll(createInputGroup("Кут відхилення кулі 1 (°):", angleField));
        physicsPane.setContent(physBox);

        startBtn = new Button("▶ СИМУЛЮВАТИ УДАР");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

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

        leftPanel.getChildren().addAll(title, labPane, physicsPane, startBtn, autoBtn, clearBtn);

        canvas = new CollisionCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(240, 100);
        Label dashTitle = new Label("ДАТЧИКИ ШВИДКОСТІ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        v1Label = new Label("v1 (до) = 0.00 м/с");
        v1Label.setStyle("-fx-text-fill: #00ff00;");
        u1Label = new Label("u1 (після) = 0.00 м/с");
        u1Label.setStyle("-fx-text-fill: #00ff00;");
        u2Label = new Label("u2 (після) = 0.00 м/с");
        u2Label.setStyle("-fx-text-fill: #00ff00;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, v1Label, u1Label, u2Label);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, Double> a1Col = new TableColumn<>("α1 (°)");
        a1Col.setCellValueFactory(new PropertyValueFactory<>("alpha1"));
        TableColumn<Measurement, Double> a1pCol = new TableColumn<>("α1' (°)");
        a1pCol.setCellValueFactory(new PropertyValueFactory<>("alpha1Prime"));
        TableColumn<Measurement, Double> a2pCol = new TableColumn<>("α2' (°)");
        a2pCol.setCellValueFactory(new PropertyValueFactory<>("alpha2Prime"));
        TableColumn<Measurement, Double> tauCol = new TableColumn<>("τ (мкс)");
        tauCol.setCellValueFactory(new PropertyValueFactory<>("tau"));
        TableColumn<Measurement, Double> u1Col = new TableColumn<>("u1 (м/с)");
        u1Col.setCellValueFactory(new PropertyValueFactory<>("u1"));
        TableColumn<Measurement, Double> u2Col = new TableColumn<>("u2 (м/с)");
        u2Col.setCellValueFactory(new PropertyValueFactory<>("u2"));
        TableColumn<Measurement, Double> dwCol = new TableColumn<>("ΔW (Дж)");
        dwCol.setCellValueFactory(new PropertyValueFactory<>("deltaW"));

        table.getColumns().addAll(idCol, a1Col, a1pCol, a2pCol, tauCol, u1Col, u2Col, dwCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(130);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        canvas.setOnCollisionCallback(() -> Platform.runLater(this::onCollision));
        updateStats();
    }

    private void applyPhysicsSettings() {
        try {
            if (isAutoRunning) return;
            CollisionType type = typeComboBox.getSelectionModel().getSelectedIndex() == 0 ? CollisionType.ELASTIC : CollisionType.INELASTIC;
            double m1 = Double.parseDouble(m1Field.getText());
            double m2 = Double.parseDouble(m2Field.getText());
            double l = Double.parseDouble(lengthField.getText());
            double angle = Double.parseDouble(angleField.getText());

            canvas.setPhysicsParameters(type, l, 9.81, m1, m2, angle);

            double v1 = Math.sqrt(2 * 9.81 * l * (1 - Math.cos(Math.toRadians(angle))));
            v1Label.setText(String.format("v1 (до) = %.2f м/с", v1));
            u1Label.setText("u1 (після) = 0.00 м/с");
            u2Label.setText("u2 (після) = 0.00 м/с");
            liveStatusLabel.setText("Статус: ГОТОВО");
            liveStatusLabel.setStyle("-fx-text-fill: yellow;");
        } catch (NumberFormatException ignored) {}
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        typeComboBox.setDisable(disable);
        m1Field.setDisable(disable);
        m2Field.setDisable(disable);
        lengthField.setDisable(disable);
        angleField.setDisable(disable);
    }

    private void startManual() {
        try {
            Double.parseDouble(m1Field.getText());
            Double.parseDouble(m2Field.getText());
            Double.parseDouble(lengthField.getText());
            Double.parseDouble(angleField.getText());

            isAutoRunning = false;
            applyPhysicsSettings();
            canvas.startSimulation();
            liveStatusLabel.setText("Статус: РУХ");
            liveStatusLabel.setStyle("-fx-text-fill: red;");
            setControlsDisable(true);
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа в поля маси, довжини та кута.");
        }
    }

    private void startAuto() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        for (int i = 0; i < 10; i++) {
            autoQueue.add(new AutoTestParam(CollisionType.ELASTIC, 0.169, 0.169, 15));
        }

        isAutoRunning = true;
        processNextAuto();
    }

    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            isAutoRunning = false;
            liveStatusLabel.setText("СИСТЕМА: ГОТОВО");
            liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            setControlsDisable(false);
            applyPhysicsSettings();
            return;
        }

        setControlsDisable(true);
        AutoTestParam param = autoQueue.poll();

        typeComboBox.getSelectionModel().select(param.type == CollisionType.ELASTIC ? 0 : 1);
        m1Field.setText(String.valueOf(param.m1));
        m2Field.setText(String.valueOf(param.m2));
        angleField.setText(String.valueOf(param.angle));

        double l = Double.parseDouble(lengthField.getText());
        canvas.setPhysicsParameters(param.type, l, 9.81, param.m1, param.m2, param.angle);

        double v1 = Math.sqrt(2 * 9.81 * l * (1 - Math.cos(Math.toRadians(param.angle))));
        v1Label.setText(String.format("v1 (до) = %.2f м/с", v1));
        u1Label.setText("u1 (після) = 0.00 м/с");
        u2Label.setText("u2 (після) = 0.00 м/с");

        liveStatusLabel.setText("СИСТЕМА: АВТО-РУХ");
        liveStatusLabel.setStyle("-fx-text-fill: red;");

        canvas.startSimulation();
    }

    private void onCollision() {
        liveStatusLabel.setText("Статус: ЗІТКНЕННЯ!");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

        try {
            CollisionType type = typeComboBox.getSelectionModel().getSelectedIndex() == 0 ? CollisionType.ELASTIC : CollisionType.INELASTIC;
            String typeStr = type == CollisionType.ELASTIC ? "Пружний" : "Непружний";

            double m1 = Double.parseDouble(m1Field.getText());
            double m2 = Double.parseDouble(m2Field.getText());
            double l = Double.parseDouble(lengthField.getText());
            double angle = Double.parseDouble(angleField.getText());

            double v1 = Math.sqrt(2 * 9.81 * l * (1 - Math.cos(Math.toRadians(angle))));
            double u1, u2;

            if (type == CollisionType.ELASTIC) {
                double eCoef = 0.72;
                u1 = (m1 * v1 - m2 * eCoef * v1) / (m1 + m2);
                u2 = (m1 * v1 + m1 * eCoef * v1) / (m1 + m2);
            } else {
                u1 = (m1 * v1) / (m1 + m2);
                u2 = u1;
            }

            double a1p = 0.0;
            if (u1 > 0) a1p = Math.toDegrees(Math.acos(Math.max(0, 1 - (u1 * u1) / (2 * 9.81 * l))));
            double a2p = Math.toDegrees(Math.acos(Math.max(0, 1 - (u2 * u2) / (2 * 9.81 * l))));

            a1p += (Math.random() - 0.5) * 0.5;
            a2p += (Math.random() - 0.5) * 1.0;
            if (a1p < 0) a1p = 0;

            u1 = Math.sqrt(2 * 9.81 * l * (1 - Math.cos(Math.toRadians(a1p))));
            u2 = Math.sqrt(2 * 9.81 * l * (1 - Math.cos(Math.toRadians(a2p))));

            double tau = 18 + Math.random() * 47;
            double wBefore = (m1 * v1 * v1) / 2.0;
            double wAfter = (m1 * u1 * u1) / 2.0 + (m2 * u2 * u2) / 2.0;
            double dw = Math.abs(wBefore - wAfter);

            u1Label.setText(String.format("u1 (після) = %.2f м/с", u1));
            u2Label.setText(String.format("u2 (після) = %.2f м/с", u2));

            Measurement m = new Measurement(
                    idCounter++, typeStr,
                    Math.round(m1 * 1000.0) / 1000.0,
                    Math.round(m2 * 1000.0) / 1000.0,
                    Math.round(angle * 10.0) / 10.0,
                    Math.round(a1p * 10.0) / 10.0,
                    Math.round(a2p * 10.0) / 10.0,
                    Math.round(v1 * 100.0) / 100.0,
                    Math.round(u1 * 100.0) / 100.0,
                    Math.round(u2 * 100.0) / 100.0,
                    Math.round(tau),
                    Math.round(dw * 10000.0) / 10000.0
            );
            data.add(m);
            updateStats();

            if (isAutoRunning) {
                new Thread(() -> {
                    try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                    Platform.runLater(this::processNextAuto);
                }).start();
            } else {
                new Thread(() -> {
                    try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                    Platform.runLater(() -> {
                        setControlsDisable(false);
                        liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
                    });
                }).start();
            }
        } catch (Exception ignored) {}
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
        List<Measurement> elasticData = new ArrayList<>();
        List<Measurement> inelasticData = new ArrayList<>();

        for (Measurement m : data) {
            if (m.getType().equals("Пружний")) elasticData.add(m);
            else inelasticData.add(m);
        }

        StringBuilder resultText = new StringBuilder();

        if (!elasticData.isEmpty()) {
            Measurement last = elasticData.get(elasticData.size() - 1);
            double pBefore = last.getM1() * last.getV1();
            double pAfter = last.getM1() * last.getU1() + last.getM2() * last.getU2();
            double dp = Math.abs(pBefore - pAfter);
            double epsP = (dp / pBefore) * 100;
            double force = (last.getM1() * Math.abs(last.getV1() - last.getU1())) / (last.getTau() / 1_000_000.0);

            resultText.append("А. ПРУЖНИЙ УДАР:\n");
            resultText.append(String.format("1. Швидкості: v1 = %.2f м/с, u1 = %.2f м/с, u2 = %.2f м/с.\n", last.getV1(), last.getU1(), last.getU2()));
            resultText.append(String.format("2. Збереження: Імпульс (ΔP = %.4f кг·м/с), Втрата енергії (ΔW = %.4f Дж).\n", dp, last.getDeltaW()));
            resultText.append(String.format("3. Середня сила удару: Fср ≈ %.1f Н.\n", force));
            resultText.append(String.format("4. Похибки: Абсолютна Δp = %.4f кг·м/с, Відносна ε = %.1f %%.\n\n", dp, epsP));
        }

        if (!inelasticData.isEmpty()) {
            Measurement last = inelasticData.get(inelasticData.size() - 1);
            double pBefore = last.getM1() * last.getV1();
            double pAfter = (last.getM1() + last.getM2()) * last.getU1();
            double dp = Math.abs(pBefore - pAfter);
            double epsP = (dp / pBefore) * 100;

            resultText.append("В. НЕПРУЖНИЙ УДАР:\n");
            resultText.append(String.format("1. Швидкості: v1 = %.2f м/с, спільна U = %.2f м/с.\n", last.getV1(), last.getU1()));
            resultText.append(String.format("2. Перевірка: Закон збереження імпульсу виконується (ΔP = %.4f кг·м/с).\n", dp));
            resultText.append(String.format("3. Втрата механічної енергії: ΔW = %.4f Дж (перейшла у внутрішню).\n", last.getDeltaW()));
            resultText.append(String.format("4. Похибки експерименту: Абсолютна Δp = %.4f кг·м/с, Відносна ε = %.1f %%.\n", dp, epsP));
        }

        finalResultLabel.setText(resultText.toString());
    }
}