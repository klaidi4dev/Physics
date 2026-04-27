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

public class LabController62 extends BaseLabController {

    private BetaDecayCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> sampleComboBox;
    private Slider timeSlider;
    private Slider refActivitySlider;
    private Slider unkActivitySlider;
    private Slider efficiencySlider;
    private Slider bgRateSlider;
    private Label timeValueLabel;
    private Label refActivityLabel;
    private Label unkActivityLabel;
    private Label efficiencyLabel;
    private Label bgRateLabel;
    private Button measureBtn;
    private Button calcBtn;
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

    public LabController62() {
        initUI();
        applyPhysicsSettings();
    }

    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        if (measureTimer != null) measureTimer.stop();
        if (autoTimer != null) autoTimer.stop();
        autoQueue.clear();
    }

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

        timeValueLabel = new Label("Час вимірювання t: 100 с");
        timeSlider = new Slider(10.0, 300.0, 100.0);
        timeSlider.setShowTickMarks(true);
        timeSlider.setMajorTickUnit(50.0);
        timeSlider.valueProperty().addListener((o, ov, nv) -> {
            timeValueLabel.setText(String.format(Locale.US, "Час вимірювання t: %.0f с", nv.doubleValue()));
            if (!isMeasuring) {
                liveTimerLabel.setText(String.format(Locale.US, "Таймер: 0 / %.0f с", nv.doubleValue()));
            }
        });

        Label envSettingsLabel = new Label("Налаштування середовища:");
        envSettingsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b; -fx-padding: 10 0 0 0;");

        refActivityLabel = new Label("Активність Еталону (A_ет): 2000 Бк");
        refActivitySlider = new Slider(500.0, 5000.0, 2000.0);
        refActivitySlider.setShowTickMarks(true);
        refActivitySlider.valueProperty().addListener((o, ov, nv) -> {
            refActivityLabel.setText(String.format(Locale.US, "Активність Еталону (A_ет): %.0f Бк", nv.doubleValue()));
            resetVariables(); updateStats();
        });

        unkActivityLabel = new Label("Активність Досліду (Справжня): 3850 Бк");
        unkActivitySlider = new Slider(1000.0, 10000.0, 3850.0);
        unkActivitySlider.setShowTickMarks(true);
        unkActivitySlider.valueProperty().addListener((o, ov, nv) -> {
            unkActivityLabel.setText(String.format(Locale.US, "Активність Досліду (Справжня): %.0f Бк", nv.doubleValue()));
            resetVariables(); updateStats();
        });

        efficiencyLabel = new Label("Ефективність лічильника: 1.5 %");
        efficiencySlider = new Slider(0.1, 10.0, 1.5);
        efficiencySlider.setShowTickMarks(true);
        efficiencySlider.valueProperty().addListener((o, ov, nv) -> {
            efficiencyLabel.setText(String.format(Locale.US, "Ефективність лічильника: %.1f %%", nv.doubleValue()));
            resetVariables(); updateStats();
        });

        bgRateLabel = new Label("Радіаційний фон: 1.2 імп/с");
        bgRateSlider = new Slider(0.1, 5.0, 1.2);
        bgRateSlider.setShowTickMarks(true);
        bgRateSlider.valueProperty().addListener((o, ov, nv) -> {
            bgRateLabel.setText(String.format(Locale.US, "Радіаційний фон: %.1f імп/с", nv.doubleValue()));
            resetVariables(); updateStats();
        });

        configBox.getChildren().addAll(
                studentLabel, createInputGroup("Об'єкт:", sampleComboBox), timeValueLabel, timeSlider,
                new Separator(),
                envSettingsLabel, refActivityLabel, refActivitySlider, unkActivityLabel, unkActivitySlider,
                efficiencyLabel, efficiencySlider, bgRateLabel, bgRateSlider
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

        calcBtn = new Button("📝 ОБЧИСЛИТИ АКТИВНІСТЬ");
        calcBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold;");
        calcBtn.setMaxWidth(Double.MAX_VALUE);
        calcBtn.setDisable(true);
        calcBtn.setOnAction(e -> executeCalculation());

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

        leftPanel.getChildren().addAll(title, configPane, measureBtn, calcBtn, autoBtn, clearBtn);

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
        TableColumn<Measurement, String> typeCol = new TableColumn<>("Об'єкт вимірювання");
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

    private void applyPhysicsSettings() {
        int sampleType = sampleComboBox.getSelectionModel().getSelectedIndex();
        canvas.setPhysicsParameters(isMeasuring, sampleType);
    }

    private void resetVariables() {
        nBg = null; nRef = null; nUnk = null;
        bgCountLabel.setText("N_ф (Фон): ---");
        refCountLabel.setText("N_ет (Еталон): ---");
        unkCountLabel.setText("N_д (Дослід): ---");
        calcBtn.setDisable(true);
    }

    private void setControlsDisable(boolean disable) {
        sampleComboBox.setDisable(disable);
        timeSlider.setDisable(disable);
        refActivitySlider.setDisable(disable);
        unkActivitySlider.setDisable(disable);
        efficiencySlider.setDisable(disable);
        bgRateSlider.setDisable(disable);
        measureBtn.setDisable(disable);
        calcBtn.setDisable(disable || (nBg == null || nRef == null || nUnk == null));
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
    }

    private void startSingleMeasurement() {
        double targetTime = timeSlider.getValue();
        int sampleType = sampleComboBox.getSelectionModel().getSelectedIndex();

        isMeasuring = true;
        setControlsDisable(true);
        liveStepLabel.setText("Статус: ВИМІРЮВАННЯ...");
        liveStepLabel.setStyle("-fx-text-fill: #ff007f; -fx-font-weight: bold;");
        applyPhysicsSettings();

        measureTimer = new AnimationTimer() {
            long start = System.nanoTime();
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

    private int generateCount(int sampleType, double timeSec) {
        double bgRate = bgRateSlider.getValue();
        double eff = efficiencySlider.getValue() / 100.0;
        double ratePerSec = bgRate;

        if (sampleType == 1) {
            ratePerSec += refActivitySlider.getValue() * eff;
        } else if (sampleType == 2) {
            ratePerSec += unkActivitySlider.getValue() * eff;
        }

        double expectedTotal = ratePerSec * timeSec;
        double noise = (Math.random() - 0.5) * Math.sqrt(expectedTotal) * 2;

        return Math.max(0, (int) Math.round(expectedTotal + noise));
    }

    private void completeMeasurement(int sampleType, double timeSec) {
        isMeasuring = false;
        liveStepLabel.setText("Статус: ГОТОВО");
        liveStepLabel.setStyle("-fx-text-fill: #00ffcc; -fx-font-weight: bold;");
        liveTimerLabel.setText(String.format(Locale.US, "Таймер: %.0f / %.0f с", timeSec, timeSec));

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

        double actToShow = (sampleType == 1) ? refActivitySlider.getValue() : 0.0;
        data.add(new Measurement(idCounter++, typeName, (int)timeSec, finalCount, actToShow));

        updateStats();
        applyPhysicsSettings();
        setControlsDisable(false);
    }

    private void executeCalculation() {
        if (nBg == null || nRef == null || nUnk == null) {
            showAlert("Помилка", "Для розрахунку необхідні всі три значення: N_ф, N_ет, N_д.");
            return;
        }

        double aRef = refActivitySlider.getValue();
        double aUnkTrue = unkActivitySlider.getValue();

        double num = nUnk - nBg;
        double den = nRef - nBg;

        if (den <= 0) {
            showAlert("Помилка", "Некоректні дані: еталон не перевищує фон.");
            return;
        }

        double calculatedActivity = (num / den) * aRef;
        double error = Math.abs(calculatedActivity - aUnkTrue) / aUnkTrue * 100.0;

        for (int i = data.size() - 1; i >= 0; i--) {
            Measurement m = data.get(i);
            if (m.getSampleType().equals("Досліджуваний")) {
                m.setActivity(Math.round(calculatedActivity * 10.0) / 10.0);
                table.refresh();
                break;
            }
        }

        String conclusion = String.format(Locale.US,
                "ОБРОБКА РЕЗУЛЬТАТІВ:\n" +
                        "1. Робоча формула: A_д = ((N_д - N_ф) / (N_ет - N_ф)) · A_ет.\n" +
                        "2. Справжня активність (з налаштувань): %.1f Бк.\n" +
                        "3. Розрахована активність: A_д = %.1f Бк.\n" +
                        "ВИСНОВОК: Відносна похибка становить ε = %.1f %%. Вона залежить від тривалості вимірювання (чим більше t, тим менший вплив шуму).",
                aUnkTrue, calculatedActivity, error
        );
        finalResultLabel.setText(conclusion);

        calcBtn.setDisable(true);
    }

    private void startAutoMode() {
        data.clear();
        idCounter = 1;
        resetVariables();
        updateStats();
        autoQueue.clear();

        autoQueue.add(0);
        autoQueue.add(1);
        autoQueue.add(2);

        processNextAuto();
    }

    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            liveStepLabel.setText("АВТОПРОХОДЖЕННЯ ЗАВЕРШЕНО");
            liveStepLabel.setStyle("-fx-text-fill: #a3e635; -fx-font-weight: bold;");

            Platform.runLater(this::executeCalculation);
            setControlsDisable(false);
            return;
        }

        int nextSample = autoQueue.poll();
        sampleComboBox.getSelectionModel().select(nextSample);
        timeSlider.setValue(100.0);

        Platform.runLater(this::startSingleMeasurement);

        autoTimer = new AnimationTimer() {
            long start = System.nanoTime();
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

    private void updateStats() {
        if (data.isEmpty()) {
            finalResultLabel.setText("Обробка результатів: -");
            return;
        }
        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }
        if (nBg == null || nRef == null || nUnk == null) {
            finalResultLabel.setText("Обробка результатів: [Зберіть усі три показники та натисніть 'ОБЧИСЛИТИ АКТИВНІСТЬ']");
        }
    }
}