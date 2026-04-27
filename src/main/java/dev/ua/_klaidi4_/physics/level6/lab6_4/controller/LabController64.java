package dev.ua._klaidi4_.physics.level6.lab6_4.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level6.lab6_4.model.Measurement;
import dev.ua._klaidi4_.physics.level6.lab6_4.view.EmulsionCanvas;
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

public class LabController64 extends BaseLabController {

    private EmulsionCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private int trackCounter = 1;
    private Slider xSlider;
    private Slider ySlider;
    private Label xValueLabel;
    private Label yValueLabel;
    private Button recordCenterBtn;
    private Button recordTrackBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStepLabel;
    private Label x0y0Label;
    private Double x0 = null;
    private Double y0 = null;

    private AnimationTimer autoTimer;
    private Queue<double[]> autoQueue = new LinkedList<>();

    public LabController64() {
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

        Label title = new Label("Система управління (Лаб 6-4)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane configPane = new TitledPane();
        configPane.setText("Мікрометричні гвинти столика");
        configPane.setCollapsible(false);

        VBox configBox = new VBox(12);
        configBox.setPadding(new Insets(5));

        xValueLabel = new Label("Координата X: 2.00 мм");
        xSlider = new Slider(0.0, 10.0, 2.0);
        xSlider.setShowTickMarks(true);
        xSlider.setMajorTickUnit(2.0);
        xSlider.setMinorTickCount(10);
        xSlider.valueProperty().addListener((o, ov, nv) -> {
            xValueLabel.setText(String.format(Locale.US, "Координата X: %.2f мм", nv.doubleValue()));
            applyPhysicsSettings();
        });

        yValueLabel = new Label("Координата Y: 2.00 мм");
        ySlider = new Slider(0.0, 10.0, 2.0);
        ySlider.setShowTickMarks(true);
        ySlider.setMajorTickUnit(2.0);
        ySlider.setMinorTickCount(10);
        ySlider.valueProperty().addListener((o, ov, nv) -> {
            yValueLabel.setText(String.format(Locale.US, "Координата Y: %.2f мм", nv.doubleValue()));
            applyPhysicsSettings();
        });

        configBox.getChildren().addAll(xValueLabel, xSlider, yValueLabel, ySlider);

        ScrollPane scrollParams = new ScrollPane(configBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(140);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        configPane.setContent(scrollParams);

        recordCenterBtn = new Button("🔴 ЗАФІКСУВАТИ ЦЕНТР (x0, y0)");
        recordCenterBtn.setStyle("-fx-background-color: #ff007f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
        recordCenterBtn.setMaxWidth(Double.MAX_VALUE);
        recordCenterBtn.setOnAction(e -> handleRecordCenter());

        recordTrackBtn = new Button("🔵 ЗАФІКСУВАТИ КІНЕЦЬ ТРЕКУ");
        recordTrackBtn.setStyle("-fx-background-color: #00bcd4; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 10;");
        recordTrackBtn.setMaxWidth(Double.MAX_VALUE);
        recordTrackBtn.setDisable(true);
        recordTrackBtn.setOnAction(e -> handleRecordTrack());

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
            trackCounter = 1;
            x0 = null;
            y0 = null;
            recordCenterBtn.setDisable(false);
            recordTrackBtn.setDisable(true);
            x0y0Label.setText("x0: ---, y0: ---");
            liveStepLabel.setText("Статус: Знайдіть центр 'зірочки'");
            liveStepLabel.setStyle("-fx-text-fill: #ff007f; -fx-font-weight: bold;");
            updateStats();
        });

        leftPanel.getChildren().addAll(title, configPane, recordCenterBtn, recordTrackBtn, autoBtn, clearBtn);

        canvas = new EmulsionCanvas(600, 440);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPickOnBounds(false);

        VBox dash = new VBox(4);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-text-fill: #00e5ff; -fx-padding: 10; -fx-border-color: #00e5ff; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(260, 100);

        Label dashTitle = new Label("ДАНІ ВИМІРЮВАНЬ");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        liveStepLabel = new Label("Статус: Знайдіть центр 'зірочки'");
        liveStepLabel.setStyle("-fx-text-fill: #ff007f; -fx-font-weight: bold;");

        x0y0Label = new Label("x0: ---, y0: ---");
        x0y0Label.setStyle("-fx-text-fill: #00ffcc; -fx-font-size: 13px;");

        dash.getChildren().addAll(dashTitle, liveStepLabel, new Separator(), x0y0Label);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #000000;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, String> nameCol = new TableColumn<>("Точка");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("trackName"));
        TableColumn<Measurement, Double> xCol = new TableColumn<>("x (мм)");
        xCol.setCellValueFactory(new PropertyValueFactory<>("x"));
        TableColumn<Measurement, Double> yCol = new TableColumn<>("y (мм)");
        yCol.setCellValueFactory(new PropertyValueFactory<>("y"));
        TableColumn<Measurement, Double> rCol = new TableColumn<>("R (мкм)");
        rCol.setCellValueFactory(new PropertyValueFactory<>("rMicrons"));
        TableColumn<Measurement, Double> eCol = new TableColumn<>("E (МеВ)");
        eCol.setCellValueFactory(new PropertyValueFactory<>("energyMeV"));

        table.getColumns().addAll(idCol, nameCol, xCol, yCol, rCol, eCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(150);

        VBox statsBox = createStatsBox();
        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        updateStats();
    }

    private void applyPhysicsSettings() {
        canvas.setStageCoordinates(xSlider.getValue(), ySlider.getValue());
    }

    private void handleRecordCenter() {
        double curX = Math.round(xSlider.getValue() * 100.0) / 100.0;
        double curY = Math.round(ySlider.getValue() * 100.0) / 100.0;

        if (Math.abs(curX - 5.0) > 0.1 || Math.abs(curY - 5.0) > 0.1) {
            showAlert("Похибка наведення", "Візирне перехрестя не знаходиться в центрі зірочки!");
            return;
        }

        x0 = curX;
        y0 = curY;

        x0y0Label.setText(String.format(Locale.US, "x0: %.2f мм, y0: %.2f мм", x0, y0));
        data.add(new Measurement(idCounter++, "Центр", x0, y0, 0.0, 0.0));

        recordCenterBtn.setDisable(true);
        recordTrackBtn.setDisable(false);

        liveStepLabel.setText("Статус: Наведіться на кінець треку");
        liveStepLabel.setStyle("-fx-text-fill: #00ffcc; -fx-font-weight: bold;");
    }

    private void handleRecordTrack() {
        if (x0 == null || y0 == null) return;

        double curX = Math.round(xSlider.getValue() * 100.0) / 100.0;
        double curY = Math.round(ySlider.getValue() * 100.0) / 100.0;
        double dx = curX - x0;
        double dy = curY - y0;
        double rMm = Math.sqrt(dx * dx + dy * dy);
        double rMicrons = rMm * 1000.0;

        double energyMeV = 0.0;
        if (rMicrons >= 100.0) {
            energyMeV = 0.251 * Math.pow(rMicrons, 0.58);
        }

        data.add(new Measurement(
                idCounter++,
                "Трек " + trackCounter++,
                curX, curY,
                Math.round(rMicrons * 10.0) / 10.0,
                Math.round(energyMeV * 100.0) / 100.0
        ));

        updateStats();
    }

    private void startAutoMode() {
        data.clear();
        idCounter = 1;
        trackCounter = 1;
        x0 = null;
        y0 = null;
        recordCenterBtn.setDisable(false);
        recordTrackBtn.setDisable(true);
        autoQueue.clear();

        autoQueue.add(new double[]{5.00, 5.00});
        autoQueue.add(new double[]{6.50, 6.20});
        autoQueue.add(new double[]{3.80, 5.90});
        autoQueue.add(new double[]{4.10, 2.50});
        autoQueue.add(new double[]{7.20, 3.80});
        autoQueue.add(new double[]{5.30, 4.10});

        processNextAuto();
    }

    private void processNextAuto() {
        if (autoQueue.isEmpty()) {
            liveStepLabel.setText("АВТОПРОХОДЖЕННЯ ЗАВЕРШЕНО");
            liveStepLabel.setStyle("-fx-text-fill: #a3e635; -fx-font-weight: bold;");
            return;
        }

        double[] target = autoQueue.poll();

        autoTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double cx = xSlider.getValue();
                double cy = ySlider.getValue();

                double dx = target[0] - cx;
                double dy = target[1] - cy;

                if (Math.abs(dx) < 0.02 && Math.abs(dy) < 0.02) {
                    xSlider.setValue(target[0]);
                    ySlider.setValue(target[1]);
                    this.stop();

                    Platform.runLater(() -> {
                        if (x0 == null) handleRecordCenter();
                        else handleRecordTrack();

                        new Thread(() -> {
                            try { Thread.sleep(800); Platform.runLater(() -> processNextAuto()); }
                            catch (InterruptedException ignored) {}
                        }).start();
                    });
                } else {
                    xSlider.setValue(cx + dx * 0.1);
                    ySlider.setValue(cy + dy * 0.1);
                }
            }
        };
        autoTimer.start();
    }

    private void updateStats() {
        if (data.isEmpty() || trackCounter <= 1) {
            finalResultLabel.setText("Обробка результатів: -");
            return;
        }

        if (!showCalculations) {
            finalResultLabel.setText("Обробка результатів: [Приховано для самостійного розрахунку]");
            return;
        }

        double maxE = 0;
        for (Measurement m : data) {
            if (m.getEnergyMeV() > maxE) maxE = m.getEnergyMeV();
        }

        String conclusion = String.format(Locale.US,
                "ОБРОБКА РЕЗУЛЬТАТІВ:\n" +
                        "1. Метод: R = sqrt((x_i - x_0)^2 + (y_i - y_0)^2) · 10³.\n" +
                        "2. Енергія розрахована за формулою: E = 0.251 · R^0.58.\n" +
                        "3. Максимальна зареєстрована енергія частинки уламка: E_max = %.2f МеВ.\n" +
                        "ВИСНОВОК: Товстошарові фотоемульсії дозволяють точно визначати пробіги та енергії заряджених частинок, що виникають при ядерних реакціях.",
                maxE
        );
        finalResultLabel.setText(conclusion);
    }
}