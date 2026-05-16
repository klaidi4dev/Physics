/*
 * Лабораторна робота № 6-2 "Активність β-джерела".
 * Клас: LabController62.
 * Призначення: керує інтерфейсом лабораторної роботи, запуском досліду,
 * введенням параметрів, таблицею результатів та обробкою вимірювань.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level6.lab6_2.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level6.lab6_2.model.Measurement;
import dev.ua._klaidi4_.physics.level6.lab6_2.view.BetaDecayCanvas;
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
import java.util.Locale;
import java.util.Queue;
import java.util.function.Consumer;

public class LabController62 extends BaseLabController {

    private BetaDecayCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> sampleComboBox;
    private TextField timeField;
    private TextField refActivityField;
    private TextField unkActivityField;
    private TextField efficiencyField;
    private TextField bgRateField;
    private double currentTimeValue = 100.0;
    private double currentRefActivityValue = 2000.0;
    private double currentUnkActivityValue = 3850.0;
    private double currentEfficiencyValue = 1.5;
    private double currentBgRateValue = 1.2;
    private Button measureBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStepLabel;
    private Label liveTimerLabel;
    private Label liveCountLabel;
    private Label bgCountLabel;
    private Label refCountLabel;
    private Label unkCountLabel;
    private boolean isMeasuring = false;
    private Double nBg = null;
    private Double nRef = null;
    private Double nUnk = null;
    private AnimationTimer measureTimer;
    private AnimationTimer autoTimer;
    private Queue<Integer> autoQueue = new LinkedList<>();

    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: LabController62.
     * Призначення: Конструктор класу, ініціалізує інтерфейс та налаштування.
     */
    public LabController62() {
        initUI();
        applyPhysicsSettings();
    }

    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: shutdown.
     * Призначення: Зупиняє процеси та очищує ресурси при закритті вікна.
     */
    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        if (measureTimer != null) measureTimer.stop();
        if (autoTimer != null) autoTimer.stop();
        autoQueue.clear();
    }

    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: initUI.
     * Призначення: Ініціалізує графічний інтерфейс користувача, створює панелі управління та графіки.
     */
    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 6-2)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane configPane = new TitledPane();
        configPane.setText("Параметри установки");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(12);
        configBox.setPadding(new Insets(5));

        Label studentLabel = new Label("Робочі параметри:");
        studentLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

        sampleComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "Без препарату (Радіаційний фон)",
                "Еталонний препарат (Sr-90)",
                "Досліджуваний препарат (Sr-90)"
        ));
        sampleComboBox.getSelectionModel().selectFirst();
        sampleComboBox.setMaxWidth(Double.MAX_VALUE);
        sampleComboBox.setOnAction(e -> applyPhysicsSettings());

        timeField = createNumberField(currentTimeValue, val -> {
            currentTimeValue = val;
            if (!isMeasuring) {
                liveTimerLabel.setText(String.format(Locale.US, "Таймер: 0 / %.0f с", currentTimeValue));
            }
        });

        Label envSettingsLabel = new Label("Налаштування середовища:");
        envSettingsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b; -fx-padding: 10 0 0 0;");

        refActivityField = createNumberField(currentRefActivityValue, val -> {
            currentRefActivityValue = val;
            resetVariables(); updateStats();
        });

        unkActivityField = createNumberField(currentUnkActivityValue, val -> {
            currentUnkActivityValue = val;
            resetVariables(); updateStats();
        });

        efficiencyField = createNumberField(currentEfficiencyValue, val -> {
            currentEfficiencyValue = val;
            resetVariables(); updateStats();
        });

        bgRateField = createNumberField(currentBgRateValue, val -> {
            currentBgRateValue = val;
            resetVariables(); updateStats();
        });

        configBox.getChildren().addAll(
                studentLabel,
                createInputGroup("Об'єкт:", sampleComboBox),
                createInputGroup("Час вимірювання t (с):", timeField),
                new Separator(),
                envSettingsLabel,
                createInputGroup("Активність Еталону (Бк):", refActivityField),
                createInputGroup("Активність Досліду (Бк):", unkActivityField),
                createInputGroup("Ефективність лічильника (%):", efficiencyField),
                createInputGroup("Радіаційний фон (імп/с):", bgRateField)
        );

        ScrollPane scrollParams = new ScrollPane(configBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(350);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        configPane.setContent(scrollParams);

        measureBtn = new Button("🔴 ПОЧАТИ ВИМІРЮВАННЯ");
        measureBtn.setStyle("-fx-background-color: #ff007f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
        measureBtn.setMaxWidth(Double.MAX_VALUE);
        measureBtn.setOnAction(e -> startSingleMeasurement());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ ЛАБИ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAutoMode());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear();
            idCounter = 1;
            resetVariables();
            updateStats();
        });

        leftPanel.getChildren().addAll(title, configPane, measureBtn, autoBtn, clearBtn);

        canvas = new BetaDecayCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(4);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-text-fill: #00e5ff; -fx-padding: 10; -fx-border-color: #00e5ff; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(240, 160);

        Label dashTitle = new Label("ПРИЛАД ПСО-2,4");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        liveStepLabel = new Label("Статус: Очікування");
        liveStepLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");

        liveTimerLabel = new Label("Таймер: 0 / 100 с");
        liveTimerLabel.setStyle("-fx-text-fill: yellow;");

        liveCountLabel = new Label("Імпульси N: ---");
        liveCountLabel.setStyle("-fx-text-fill: #00ffcc; -fx-font-size: 14px; -fx-font-weight: bold;");

        bgCountLabel = new Label("N_ф (Фон): ---");
        bgCountLabel.setStyle("-fx-text-fill: #94a3b8;");

        refCountLabel = new Label("N_ет (Еталон): ---");
        refCountLabel.setStyle("-fx-text-fill: #00e5ff;");

        unkCountLabel = new Label("N_д (Дослід): ---");
        unkCountLabel.setStyle("-fx-text-fill: #ff007f;");

        dash.getChildren().addAll(dashTitle, liveStepLabel, liveTimerLabel, liveCountLabel, new Separator(), bgCountLabel, refCountLabel, unkCountLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, String> typeCol = new TableColumn<>("Об'єкт");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("sampleType"));
        TableColumn<Measurement, Integer> timeCol = new TableColumn<>("Час t (с)");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("timeSec"));
        TableColumn<Measurement, Integer> countCol = new TableColumn<>("Імпульси N");
        countCol.setCellValueFactory(new PropertyValueFactory<>("counts"));
        TableColumn<Measurement, Double> actCol = new TableColumn<>("Активність А (Бк)");
        actCol.setCellValueFactory(new PropertyValueFactory<>("activity"));

        table.getColumns().addAll(idCol, typeCol, timeCol, countCol, actCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(130);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        updateStats();
    }

    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: createNumberField.
     * Призначення: Створює і повертає новий елемент інтерфейсу або об'єкт.
     */
    private TextField createNumberField(double initialValue, Consumer<Double> onChange) {
        TextField field = new TextField(String.valueOf(initialValue));
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                field.setText(newValue.replaceAll("[^\\d.]", ""));
            } else if (!newValue.isEmpty() && !newValue.equals(".")) {
                try {
                    double val = Double.parseDouble(newValue);
                    onChange.accept(val);
                } catch (NumberFormatException ignored) {}
            }
        });
        return field;
    }

    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: applyPhysicsSettings.
     * Призначення: Застосовує поточні налаштування до симуляції.
     */
    private void applyPhysicsSettings() {
        int sampleType = sampleComboBox.getSelectionModel().getSelectedIndex();
        canvas.setPhysicsParameters(isMeasuring, sampleType);
    }

    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: resetVariables.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void resetVariables() {
        nBg = null; nRef = null; nUnk = null;
        bgCountLabel.setText("N_ф (Фон): ---");
        refCountLabel.setText("N_ет (Еталон): ---");
        unkCountLabel.setText("N_д (Дослід): ---");
    }

    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: setControlsDisable.
     * Призначення: Встановлює нове значення для вказаного параметра.
     */
    private void setControlsDisable(boolean disable) {
        sampleComboBox.setDisable(disable);
        timeField.setDisable(disable);
        refActivityField.setDisable(disable);
        unkActivityField.setDisable(disable);
        efficiencyField.setDisable(disable);
        bgRateField.setDisable(disable);
        measureBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
    }

    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: startSingleMeasurement.
     * Призначення: Запускає процес симуляції або відповідний режим роботи.
     */
    private void startSingleMeasurement() {
        if (currentTimeValue <= 0) {
            showAlert("Помилка", "Час вимірювання має бути більшим за 0.");
            return;
        }

        double targetTime = currentTimeValue;
        int sampleType = sampleComboBox.getSelectionModel().getSelectedIndex();

        isMeasuring = true;
        setControlsDisable(true);
        liveStepLabel.setText("Статус: ВИМІРЮВАННЯ...");
        liveStepLabel.setStyle("-fx-text-fill: #ff007f; -fx-font-weight: bold;");
        applyPhysicsSettings();

        measureTimer = new AnimationTimer() {
            long start = System.nanoTime();
    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: handle.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
            @Override
            public void handle(long now) {
                double elapsed = (now - start) / 1_000_000_000.0;
                double simRate = targetTime / 1.5;
                int simulatedSeconds = (int) (elapsed * simRate);

                if (simulatedSeconds < targetTime) {
                    liveTimerLabel.setText(String.format(Locale.US, "Таймер: %d / %.0f с", simulatedSeconds, targetTime));
                    int currentCount = (int) ((generateCount(sampleType, targetTime) / targetTime) * simulatedSeconds);
                    liveCountLabel.setText(String.format("Імпульси N: %d", currentCount));
                } else {
                    this.stop();
                    completeMeasurement(sampleType, targetTime);
                }
            }
        };
        measureTimer.start();
    }

    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: generateCount.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private int generateCount(int sampleType, double timeSec) {
        double ratePerSec = currentBgRateValue;
        double eff = currentEfficiencyValue / 100.0;
        if (sampleType == 1) ratePerSec += currentRefActivityValue * eff;
        else if (sampleType == 2) ratePerSec += currentUnkActivityValue * eff;

        double expectedTotal = ratePerSec * timeSec;
        double noise = (Math.random() - 0.5) * Math.sqrt(expectedTotal) * 2;
        return Math.max(0, (int) Math.round(expectedTotal + noise));
    }

    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: completeMeasurement.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void completeMeasurement(int sampleType, double timeSec) {
        isMeasuring = false;
        liveStepLabel.setText("Статус: ГОТОВО");
        liveStepLabel.setStyle("-fx-text-fill: #00ffcc; -fx-font-weight: bold;");

        int finalCount = generateCount(sampleType, timeSec);
        liveCountLabel.setText(String.format("Імпульси N: %d", finalCount));

        String typeName = sampleComboBox.getValue().split(" ")[0];
        double normalizedCount = finalCount * (100.0 / timeSec);

        if (sampleType == 0) {
            nBg = normalizedCount;
            bgCountLabel.setText(String.format(Locale.US, "N_ф (Фон): %d", finalCount));
        } else if (sampleType == 1) {
            nRef = normalizedCount;
            refCountLabel.setText(String.format(Locale.US, "N_ет (Еталон): %d", finalCount));
        } else if (sampleType == 2) {
            nUnk = normalizedCount;
            unkCountLabel.setText(String.format(Locale.US, "N_д (Дослід): %d", finalCount));
        }

        double actToShow = (sampleType == 1) ? currentRefActivityValue : 0.0;
        data.add(new Measurement(idCounter++, typeName, (int)timeSec, finalCount, actToShow));

        if (nBg != null && nRef != null && nUnk != null) {
            performAutomaticCalculation();
        }

        updateStats();
        applyPhysicsSettings();
        setControlsDisable(false);
    }

    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: performAutomaticCalculation.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
    private void performAutomaticCalculation() {
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }

        double num = nUnk - nBg;
        double den = nRef - nBg;

        if (den <= 1) return;

        double calculatedActivity = (num / den) * currentRefActivityValue;
        double error = Math.abs(calculatedActivity - currentUnkActivityValue) / currentUnkActivityValue * 100.0;

        for (int i = data.size() - 1; i >= 0; i--) {
            Measurement m = data.get(i);
            if (m.getSampleType().contains("Дослід")) {
                m.setActivity(Math.round(calculatedActivity * 10.0) / 10.0);
                table.refresh();
                break;
            }
        }

        String conclusion = String.format(Locale.US,
                "АВТОМАТИЧНИЙ РОЗРАХУНОК:\n" +
                        "A_д = ((N_д - N_ф) / (N_ет - N_ф)) · A_ет = %.1f Бк.\n" +
                        "Справжнє значення: %.1f Бк. Похибка: %.1f %%.",
                calculatedActivity, currentUnkActivityValue, error
        );
        finalResultLabel.setText(conclusion);
    }

    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: startAutoMode.
     * Призначення: Запускає процес симуляції або відповідний режим роботи.
     */
    private void startAutoMode() {
        data.clear();
        idCounter = 1;
        resetVariables();
        autoQueue.clear();
        autoQueue.add(0);
        autoQueue.add(1);
        autoQueue.add(2);
        timeField.setText("100");
        processNextAuto();
    }

    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: processNextAuto.
     * Призначення: Обробляє поточний крок симуляції або дію користувача.
     */
    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            liveStepLabel.setText("АВТОПРОХОДЖЕННЯ ЗАВЕРШЕНО");
            setControlsDisable(false);
            return;
        }
        int nextSample = autoQueue.poll();
        sampleComboBox.getSelectionModel().select(nextSample);
        Platform.runLater(this::startSingleMeasurement);
        autoTimer = new AnimationTimer() {
            long start = System.nanoTime();
    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: handle.
     * Призначення: Виконує обробку відповідних параметрів та логіки лабораторної роботи.
     */
            @Override
            public void handle(long now) {
                if (!isMeasuring && ((now - start) / 1_000_000_000.0) > 1.8) {
                    this.stop();
                    processNextAuto();
                }
            }
        };
        autoTimer.start();
    }


    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: updateStats.
     * Призначення: Розраховує середні значення та похибки на основі даних у таблиці.
     */
    private void updateStats() {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: -");
        } else if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
        } else if (nBg == null || nRef == null || nUnk == null) {
            finalResultLabel.setText("Зберіть усі три показники для автоматичного розрахунку.");
        }
    }
}