/*
 * Лабораторна робота № 4-2 "Математичний маятник".
 * Клас: LabController42.
 * Призначення: керує інтерфейсом лабораторної роботи, проведенням дослідів
 * з вивчення законів коливання математичного маятника.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level4.lab4_2.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level4.lab4_2.enums.MaterialType;
import dev.ua._klaidi4_.physics.level4.lab4_2.model.Measurement;
import dev.ua._klaidi4_.physics.level4.lab4_2.view.MathPendulumCanvas;
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
import javafx.animation.AnimationTimer;

import java.util.LinkedList;
import java.util.Queue;

public class LabController42 extends BaseLabController {

    private MathPendulumCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<MaterialType> materialComboBox;
    private TextField lengthField, nField;
    private Slider angleSlider;
    private Button startBtn, autoBtn, clearBtn;
    private Label liveStatusLabel, liveTimeLabel, liveOscLabel;

    private Queue<AutoTestParam> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;
    private int targetN = 10;
    private AnimationTimer uiTimer;

    private static class AutoTestParam {
        MaterialType mat; double l, angle; int n;
        AutoTestParam(MaterialType mat, double l, double angle, int n) {
            this.mat = mat; this.l = l; this.angle = angle; this.n = n;
        }
    }

    /*
     * Лабораторна робота № 4-2 "Математичний маятник".
     * Функція: LabController42.
     * Призначення: Конструктор класу, ініціалізує інтерфейс.
     */
    public LabController42() {
        initUI();
        applyPhysicsSettings();
    }

    /*
     * Лабораторна робота № 4-2 "Математичний маятник".
     * Функція: shutdown.
     * Призначення: Зупиняє анімацію при закритті модуля.
     */
    @Override
    public void shutdown() {
        if(canvas != null) canvas.stopSimulation();
        if(uiTimer != null) uiTimer.stop();
        isAutoRunning = false;
        autoQueue.clear();
    }

    /*
     * Лабораторна робота № 4-2 "Математичний маятник".
     * Функція: initUI.
     * Призначення: Створює графічний інтерфейс: панель параметрів, таблицю та візуалізацію коливань маятника.
     */
    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 4-2)");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри маятника");
        labPane.setCollapsible(false);
        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        materialComboBox = new ComboBox<>(FXCollections.observableArrayList(MaterialType.values()));
        materialComboBox.getSelectionModel().selectFirst();
        materialComboBox.setOnAction(e -> applyPhysicsSettings());

        lengthField = new TextField("0.5");
        lengthField.textProperty().addListener((o, oldVal, newVal) -> applyPhysicsSettings());

        nField = new TextField("10");

        paramsBox.getChildren().addAll(
                createInputGroup("Матеріал кульки:", materialComboBox),
                createInputGroup("Довжина нитки l (м):", lengthField),
                createInputGroup("Кіл-ть коливань n:", nField)
        );
        labPane.setContent(paramsBox);

        TitledPane physicsPane = new TitledPane();
        physicsPane.setText("Кінематика");
        physicsPane.setCollapsible(false);
        VBox physBox = new VBox(5);
        physBox.setPadding(new Insets(5));

        Label angleLabel = new Label("Кут відхилення: 5.0°");
        angleSlider = new Slider(2, 30, 5);
        angleSlider.setShowTickMarks(true);
        angleSlider.setShowTickLabels(true);
        angleSlider.valueProperty().addListener((o, oldVal, newVal) -> {
            angleLabel.setText(String.format("Кут відхилення: %.1f°", newVal.doubleValue()));
            applyPhysicsSettings();
        });
        physBox.getChildren().addAll(angleLabel, angleSlider);
        physicsPane.setContent(physBox);

        startBtn = new Button("▶ ПОЧАТИ ВИМІРЮВАННЯ");
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

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, labPane, physicsPane, startBtn, autoBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new MathPendulumCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));
        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(200, 80);
        Label dashTitle = new Label("СЕНСОРНА ПАНЕЛЬ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveTimeLabel = new Label("t = 0.00 с");
        liveTimeLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        liveTimeLabel.setStyle("-fx-text-fill: #00ff00;");
        liveOscLabel = new Label("n = 0");
        liveOscLabel.setStyle("-fx-text-fill: #00ff00;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveTimeLabel, liveOscLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);
        String[] cols = {"№", "Матеріал", "l (м)", "Кут (°)", "m (кг)", "n", "t (с)", "Tексп (с)", "Tтеор (с)"};
        String[] props = {"id", "material", "length", "angle", "mass", "oscillations", "time", "periodExp", "periodTheor"};
        for (int i = 0; i < cols.length; i++) {
            TableColumn<Measurement, Object> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(new PropertyValueFactory<>(props[i]));
            table.getColumns().add(col);
        }
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();

        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        uiTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (liveStatusLabel.getText().contains("ВИМІРЮВАННЯ")) {
                    liveTimeLabel.setText(String.format("t = %.2f с", canvas.getElapsedTime()));
                    liveOscLabel.setText(String.format("n = %d", canvas.getFullOscillations()));
                }
            }
        };
        uiTimer.start();
        canvas.setOnOscillationCompleted(this::onOscillationTick);
    }

    /*
     * Лабораторна робота № 4-2 "Математичний маятник".
     * Функція: applyPhysicsSettings.
     * Призначення: Передає актуальні параметри (довжина, матеріал) до візуальної моделі.
     */
    private void applyPhysicsSettings() {
        if (isAutoRunning) return;
        try {
            MaterialType mat = materialComboBox.getValue();
            double l = Double.parseDouble(lengthField.getText());
            double angle = angleSlider.getValue();
            canvas.setPhysicsParameters(mat, l, angle);
            liveTimeLabel.setText("t = 0.00 с");
            liveOscLabel.setText("n = 0");
        } catch (NumberFormatException ignored) {}
    }

    /*
     * Лабораторна робота № 4-2 "Математичний маятник".
     * Функція: setControlsDisable.
     * Призначення: Керує доступністю полів введення під час коливань.
     */
    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        materialComboBox.setDisable(disable);
        lengthField.setDisable(disable);
        nField.setDisable(disable);
        angleSlider.setDisable(disable);
    }

    /*
     * Лабораторна робота № 4-2 "Математичний маятник".
     * Функція: startManual.
     * Призначення: Запускає поодиноке вимірювання періоду коливань вручну.
     */
    private void startManual() {
        try {
            targetN = Integer.parseInt(nField.getText());
            Double.parseDouble(lengthField.getText());
            isAutoRunning = false;
            applyPhysicsSettings();
            setControlsDisable(true);
            liveStatusLabel.setText("СИСТЕМА: ВИМІРЮВАННЯ");
            liveStatusLabel.setStyle("-fx-text-fill: red;");
            canvas.startSimulation();
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа.");
        }
    }

    /*
     * Лабораторна робота № 4-2 "Математичний маятник".
     * Функція: startAuto.
     * Призначення: Ініціює автоматичну серію вимірювань для різних довжин нитки.
     */
    private void startAuto() {
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        autoQueue.add(new AutoTestParam(MaterialType.STEEL, 0.5, 5.0, 10));
        autoQueue.add(new AutoTestParam(MaterialType.STEEL, 0.5, 25.0, 10));
        autoQueue.add(new AutoTestParam(MaterialType.WOOD, 0.5, 5.0, 10));
        autoQueue.add(new AutoTestParam(MaterialType.STEEL, 0.25, 5.0, 10));

        isAutoRunning = true;
        processNextAuto();
    }

    /*
     * Лабораторна робота № 4-2 "Математичний маятник".
     * Функція: processNextAuto.
     * Призначення: Виконує наступний крок в автоматичному режимі.
     */
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
        materialComboBox.setValue(param.mat);
        lengthField.setText(String.valueOf(param.l));
        angleSlider.setValue(param.angle);
        nField.setText(String.valueOf(param.n));
        targetN = param.n;

        canvas.setPhysicsParameters(param.mat, param.l, param.angle);
        liveStatusLabel.setText("СИСТЕМА: ВИМІРЮВАННЯ (АВТО)");
        liveStatusLabel.setStyle("-fx-text-fill: red;");
        canvas.startSimulation();
    }

    /*
     * Лабораторна робота № 4-2 "Математичний маятник".
     * Функція: onOscillationTick.
     * Призначення: Обробляє завершення вимірювального циклу, фіксує час та додає дані до таблиці.
     */
    private void onOscillationTick() {
        int currentN = canvas.getFullOscillations();
        if (currentN >= targetN) {
            canvas.stopSimulation();
            liveTimeLabel.setText(String.format("t = %.2f с", canvas.getElapsedTime()));
            liveOscLabel.setText(String.format("n = %d", currentN));
            liveStatusLabel.setText("СИСТЕМА: ЗАПИС");
            liveStatusLabel.setStyle("-fx-text-fill: yellow;");

            double finalTime = canvas.getElapsedTime();
            double expPeriod = finalTime / targetN;
            MaterialType mat = materialComboBox.getValue();
            double l = Double.parseDouble(lengthField.getText());
            double angle = angleSlider.getValue();
            double theorPeriod = 2 * Math.PI * Math.sqrt(l / 9.81);

            Measurement m = new Measurement(idCounter++, mat.getName(), l, angle, mat.getMass(), targetN,
                    Math.round(finalTime * 100.0) / 100.0, Math.round(expPeriod * 1000.0) / 1000.0, Math.round(theorPeriod * 1000.0) / 1000.0);
            data.add(m);
            updateStats();

            if (isAutoRunning) {
                new Thread(() -> {
                    try { Thread.sleep(1000); } catch (Exception ignored) {}
                    Platform.runLater(this::processNextAuto);
                }).start();
            } else {
                setControlsDisable(false);
                liveStatusLabel.setText("СИСТЕМА: ГОТОВО");
                liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");
            }
        }
    }

    /*
     * Лабораторна робота № 4-2 "Математичний маятник".
     * Функція: updateStats.
     * Призначення: Проводить статистичну обробку та перевірку законів коливань.
     */
    private void updateStats() {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: -");
            return;
        }
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }
        StringBuilder sb = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ:\n");

        Measurement baseMeas = data.get(0);
        double t1 = baseMeas.getPeriodExp();
        sb.append(String.format("1. Базовий період (T1): %.2f с.\n", t1));

        boolean angleChecked = false;
        boolean massChecked = false;
        boolean lengthChecked = false;

        for (Measurement m : data) {
            if (m.getId() == baseMeas.getId()) continue;

            if (m.getLength() == baseMeas.getLength() && m.getMass() == baseMeas.getMass() && m.getAngle() > baseMeas.getAngle()) {
                sb.append(String.format("2-3. Кут %.1f°: T = %.2f с. Висновок: %s.\n",
                        m.getAngle(), m.getPeriodExp(),
                        (Math.abs(m.getPeriodExp() - t1) > 0.05 ? "При великих кутах період зростає (неізохронність)" : "Період не залежить від кута")));
                angleChecked = true;
            }

            if (m.getLength() == baseMeas.getLength() && m.getAngle() == baseMeas.getAngle() && m.getMass() != baseMeas.getMass()) {
                sb.append(String.format("4-5. Зміна маси (%.2f кг): T = %.2f с. Висновок: %s.\n",
                        m.getMass(), m.getPeriodExp(),
                        (Math.abs(m.getPeriodExp() - t1) < 0.05 ? "Період не залежить від маси" : "Період залежить від маси (помилка)")));
                massChecked = true;
            }

            if (m.getLength() != baseMeas.getLength() && m.getAngle() == baseMeas.getAngle() && m.getMass() == baseMeas.getMass()) {
                double ratioT = t1 / m.getPeriodExp();
                double ratioL = Math.sqrt(baseMeas.getLength() / m.getLength());
                sb.append(String.format("6-7. Зміна довжини (l=%.2f м): T = %.2f с.\n   Перевірка: T1/T2 = %.2f, √(l1/l2) = %.2f. %s.\n",
                        m.getLength(), m.getPeriodExp(), ratioT, ratioL,
                        (Math.abs(ratioT - ratioL) < 0.05 ? "Співвідношення виконується" : "Співвідношення не виконується")));
                lengthChecked = true;
            }
        }

        sb.append("8. ЗАГАЛЬНИЙ ВИСНОВОК: ");
        if (angleChecked && massChecked && lengthChecked) {
            sb.append("Закони коливання математичного маятника підтверджено експериментально.");
        } else {
            sb.append("Для повного аналізу проведіть вимірювання зі зміною кута, маси та довжини.");
        }

        finalResultLabel.setText(sb.toString());
    }
}