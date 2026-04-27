package dev.ua._klaidi4_.physics.level5.lab5_1.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level5.lab5_1.model.Measurement;
import dev.ua._klaidi4_.physics.level5.lab5_1.view.OpticsCanvas;
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

public class LabController51 extends BaseLabController {

    private OpticsCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> modeComboBox;
    private Slider sourceSlider;
    private Slider screenSlider;
    private Slider lensSlider;
    private Label sourceValueLabel;
    private Label screenValueLabel;
    private Label lensValueLabel;
    private Button togglePowerBtn;
    private Button recordBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label livePowerLabel;
    private Label liveSharpnessLabel;
    private Label liveStepLabel;
    private boolean isIlluminatorOn = false;
    private Double firstX = null;
    private boolean isWaitingForSecondX = false;
    private AnimationTimer autoTimer;
    private Queue<Integer> autoQueue = new LinkedList<>();

    private final double F1 = 12.0;
    private final double F2 = 18.0;

    public LabController51() {
        initUI();
        applyPhysicsSettings();
    }

    @Override
    public void shutdown() {
        if (canvas != null) canvas.stopAnimation();
        if (autoTimer != null) autoTimer.stop();
        autoQueue.clear();
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 5-1)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane configPane = new TitledPane();
        configPane.setText("Оптична лава");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(12);
        configBox.setPadding(new Insets(5));

        modeComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "Лінза 1 (+12 см)",
                "Лінза 2 (+18 см)",
                "Система: Збир + Збир"
        ));
        modeComboBox.getSelectionModel().selectFirst();
        modeComboBox.setMaxWidth(Double.MAX_VALUE);
        modeComboBox.setOnAction(e -> {
            resetMeasurementState();
            applyPhysicsSettings();
        });

        sourceValueLabel = new Label("Позиція джерела X_source: 0.0 см");
        sourceSlider = new Slider(0.0, 40.0, 0.0);
        sourceSlider.setShowTickMarks(true);
        sourceSlider.setMajorTickUnit(10.0);
        sourceSlider.valueProperty().addListener((o, oldV, newV) -> {
            sourceValueLabel.setText(String.format(Locale.US, "Позиція джерела X_source: %.1f см", newV.doubleValue()));
            applyPhysicsSettings();
        });

        screenValueLabel = new Label("Позиція екрану X_screen: 100.0 см");
        screenSlider = new Slider(50.0, 120.0, 100.0);
        screenSlider.setShowTickMarks(true);
        screenSlider.setMajorTickUnit(10.0);
        screenSlider.valueProperty().addListener((o, oldV, newV) -> {
            screenValueLabel.setText(String.format(Locale.US, "Позиція екрану X_screen: %.1f см", newV.doubleValue()));
            applyPhysicsSettings();
        });

        lensValueLabel = new Label("Позиція лінзи X_lens: 50.0 см");
        lensSlider = new Slider(0.0, 120.0, 50.0);
        lensSlider.setShowTickMarks(true);
        lensSlider.setMajorTickUnit(20.0);
        lensSlider.valueProperty().addListener((o, oldV, newV) -> {
            lensValueLabel.setText(String.format(Locale.US, "Позиція лінзи X_lens: %.1f см", newV.doubleValue()));
            applyPhysicsSettings();
        });

        configBox.getChildren().addAll(
                createInputGroup("Оптична система:", modeComboBox),
                sourceValueLabel, sourceSlider,
                screenValueLabel, screenSlider,
                lensValueLabel, lensSlider
        );

        ScrollPane scrollParams = new ScrollPane(configBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(220);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        configPane.setContent(scrollParams);

        togglePowerBtn = new Button("⚡ УВІМКНУТИ ОСВІТЛЮВАЧ");
        togglePowerBtn.setStyle("-fx-background-color: #A155FF; -fx-text-fill: white; -fx-font-weight: bold;");
        togglePowerBtn.setMaxWidth(Double.MAX_VALUE);
        togglePowerBtn.setOnAction(e -> handleTogglePower());

        recordBtn = new Button("📝 ЗАФІКСУВАТИ x1 (ЗБІЛЬШЕНЕ)");
        recordBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        recordBtn.setMaxWidth(Double.MAX_VALUE);
        recordBtn.setOnAction(e -> handleRecordPosition());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAutoMode());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            data.clear();
            idCounter = 1;
            resetMeasurementState();
            updateStats();
        });

        leftPanel.getChildren().addAll(title, configPane, togglePowerBtn, recordBtn, autoBtn, clearBtn);

        canvas = new OpticsCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(4);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-text-fill: #A155FF; -fx-padding: 10; -fx-border-color: #A155FF; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(240, 80);
        Label dashTitle = new Label("ОПТИЧНИЙ СЕНСОР ЕКРАНУ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        livePowerLabel = new Label("Живлення: ВИМКНЕНО");
        livePowerLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
        liveSharpnessLabel = new Label("Різкість: 0%");
        liveSharpnessLabel.setStyle("-fx-text-fill: yellow;");
        liveStepLabel = new Label("Крок: Очікування");
        liveStepLabel.setStyle("-fx-text-fill: #ff007f; -fx-font-weight: bold;");

        dash.getChildren().addAll(dashTitle, livePowerLabel, liveSharpnessLabel, liveStepLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, String> modeCol = new TableColumn<>("Система");
        modeCol.setCellValueFactory(new PropertyValueFactory<>("systemName"));
        TableColumn<Measurement, Double> dCol = new TableColumn<>("D (см)");
        dCol.setCellValueFactory(new PropertyValueFactory<>("d"));
        TableColumn<Measurement, Double> x1Col = new TableColumn<>("x1 (см)");
        x1Col.setCellValueFactory(new PropertyValueFactory<>("x1"));
        TableColumn<Measurement, Double> x2Col = new TableColumn<>("x2 (см)");
        x2Col.setCellValueFactory(new PropertyValueFactory<>("x2"));
        TableColumn<Measurement, Double> aCol = new TableColumn<>("a (см)");
        aCol.setCellValueFactory(new PropertyValueFactory<>("a"));
        TableColumn<Measurement, Double> fExpCol = new TableColumn<>("F експ");
        fExpCol.setCellValueFactory(new PropertyValueFactory<>("fExp"));
        TableColumn<Measurement, Double> errCol = new TableColumn<>("ε (%)");
        errCol.setCellValueFactory(new PropertyValueFactory<>("errorPercent"));

        table.getColumns().addAll(idCol, modeCol, dCol, x1Col, x2Col, aCol, fExpCol, errCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(130);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);
    }

    private double getTheoreticalF() {
        int index = modeComboBox.getSelectionModel().getSelectedIndex();
        if (index == 0) return F1;
        if (index == 1) return F2;
        return (F1 * F2) / (F1 + F2);
    }

    private void handleTogglePower() {
        isIlluminatorOn = !isIlluminatorOn;
        if (isIlluminatorOn) {
            togglePowerBtn.setText("⏹ ВИМКНУТИ ОСВІТЛЮВАЧ");
            togglePowerBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold;");
            livePowerLabel.setText("Живлення: УВІМКНЕНО");
            livePowerLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
            resetMeasurementState();
        } else {
            togglePowerBtn.setText("⚡ УВІМКНУТИ ОСВІТЛЮВАЧ");
            togglePowerBtn.setStyle("-fx-background-color: #A155FF; -fx-text-fill: white; -fx-font-weight: bold;");
            livePowerLabel.setText("Живлення: ВИМКНЕНО");
            livePowerLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
            liveSharpnessLabel.setText("Різкість: 0%");
            liveStepLabel.setText("Крок: Очікування");
        }
        applyPhysicsSettings();
    }

    private void applyPhysicsSettings() {
        double xSrc = sourceSlider.getValue();
        double xScr = screenSlider.getValue();
        double xLens = lensSlider.getValue();
        double f = getTheoreticalF();

        canvas.setPhysicsParameters(isIlluminatorOn, xSrc, xScr, xLens, f);
        updateDashboard(xSrc, xScr, f);
    }

    private void updateDashboard(double xSrc, double xScr, double f) {
        if (!isIlluminatorOn) return;

        double d = xScr - xSrc;
        if (d < 4 * f) {
            liveSharpnessLabel.setText("Різкість: НЕМОЖЛИВО (D < 4F)");
            liveSharpnessLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        double error = canvas.getSharpnessError();
        if (error < 0.5) {
            liveSharpnessLabel.setText("Різкість: ІДЕАЛЬНО (100%)");
            liveSharpnessLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
        } else if (error < 2.0) {
            double percent = 100.0 - (error * 10);
            liveSharpnessLabel.setText(String.format(Locale.US, "Різкість: ДОБРЕ (%.0f%%)", percent));
            liveSharpnessLabel.setStyle("-fx-text-fill: #a3e635;");
        } else {
            liveSharpnessLabel.setText("Різкість: РОЗМИТО");
            liveSharpnessLabel.setStyle("-fx-text-fill: yellow;");
        }
    }

    private void resetMeasurementState() {
        firstX = null;
        isWaitingForSecondX = false;
        if (isIlluminatorOn) {
            recordBtn.setText("📝 ЗАФІКСУВАТИ x1 (ЗБІЛЬШЕНЕ)");
            recordBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
            liveStepLabel.setText("Крок: Шукайте збільшене зобр.");
        }
    }

    private void handleRecordPosition() {
        if (!isIlluminatorOn) {
            showAlert("Помилка", "Увімкніть освітлювач.");
            return;
        }

        double d = screenSlider.getValue() - sourceSlider.getValue();
        double f = getTheoreticalF();

        if (d <= 4 * f) {
            showAlert("Помилка", "Відстань D = X_screen - X_source має бути більшою за 4F.");
            return;
        }

        if (canvas.getSharpnessError() > 1.5) {
            showAlert("Помилка", "Зображення розмите! Знайдіть точний фокус на екрані.");
            return;
        }

        double currentX = Math.round(lensSlider.getValue() * 10.0) / 10.0;

        if (!isWaitingForSecondX) {
            firstX = currentX;
            isWaitingForSecondX = true;
            recordBtn.setText("💾 ЗАФІКСУВАТИ x2 ТА РОЗРАХУВАТИ");
            recordBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold;");
            liveStepLabel.setText("Крок: Шукайте зменшене зобр.");
        } else {
            if (Math.abs(firstX - currentX) < 2.0) {
                showAlert("Помилка", "Ви зафіксували те ж саме положення.");
                return;
            }
            double a = Math.abs(firstX - currentX);
            executeMeasurement(d, firstX, currentX, a, f);
            resetMeasurementState();
        }
    }

    private void startAutoMode() {
        if (!isIlluminatorOn) handleTogglePower();
        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        autoQueue.add(0);
        autoQueue.add(1);
        autoQueue.add(2);

        processNextAuto();
    }

    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            liveStepLabel.setText("АВТО: ГОТОВО");
            return;
        }

        int mode = autoQueue.poll();
        modeComboBox.getSelectionModel().select(mode);
        sourceSlider.setValue(10.0);
        screenSlider.setValue(110.0);

        double d = 100.0;
        double f = getTheoreticalF();

        double discriminant = d * d - 4 * f * d;
        double shift = sourceSlider.getValue();
        double targetX1 = shift + (d - Math.sqrt(discriminant)) / 2.0;
        double targetX2 = shift + (d + Math.sqrt(discriminant)) / 2.0;
        double targetA = Math.abs(targetX1 - targetX2);

        liveStepLabel.setText("АВТО: Симуляція...");

        autoTimer = new AnimationTimer() {
            long start = System.nanoTime();
            @Override
            public void handle(long now) {
                double elapsed = (now - start) / 1_000_000_000.0;
                if (elapsed < 1.0) {
                    lensSlider.setValue(targetX1);
                } else if (elapsed > 1.5 && elapsed < 2.5) {
                    lensSlider.setValue(targetX1 + (targetX2 - targetX1) * (elapsed - 1.5));
                } else if (elapsed > 3.0) {
                    this.stop();
                    double simulatedA = targetA + (Math.random() - 0.5) * 0.4;
                    executeMeasurement(d, targetX1, targetX2, simulatedA, f);

                    new Thread(() -> {
                        try { Thread.sleep(1000); Platform.runLater(() -> processNextAuto()); }
                        catch (InterruptedException ignored) {}
                    }).start();
                }
            }
        };
        autoTimer.start();
    }

    private void executeMeasurement(double d, double x1, double x2, double a, double fTheo) {
        try {
            double fExp = (d * d - a * a) / (4 * d);
            double error = Math.abs(fExp - fTheo) / Math.abs(fTheo) * 100.0;
            String sysName = modeComboBox.getSelectionModel().getSelectedItem().substring(0, 7);

            Measurement m = new Measurement(
                    idCounter++, sysName,
                    Math.round(d * 10.0) / 10.0,
                    Math.round(x1 * 10.0) / 10.0,
                    Math.round(x2 * 10.0) / 10.0,
                    Math.round(a * 10.0) / 10.0,
                    Math.round(fExp * 100.0) / 100.0,
                    Math.round(error * 10.0) / 10.0
            );

            data.add(m);
            updateStats();

        } catch (Exception e) {
            showAlert("Помилка", "Сталася помилка при обчисленні.");
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

        double maxError = 0;
        for (Measurement m : data) {
            if (m.getErrorPercent() > maxError) maxError = m.getErrorPercent();
        }

        String conclusion = String.format(Locale.US,
                "ОБРОБКА РЕЗУЛЬТАТІВ:\n" +
                        "1. Головна фокусна відстань розрахована за формулою Бесселя: F = (D² - a²) / 4D.\n" +
                        "2. Максимальна відносна похибка: ε_max = %.1f %%.\n" +
                        "ВИСНОВОК: Двоетапний метод дозволяє точно визначити фокус без знання точного положення оптичного центра лінзи.",
                maxError
        );

        finalResultLabel.setText(conclusion);
    }
}