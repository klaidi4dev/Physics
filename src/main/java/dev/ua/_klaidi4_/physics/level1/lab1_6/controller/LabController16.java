package dev.ua._klaidi4_.physics.level1.lab1_6.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level1.lab1_6.model.Measurement;
import dev.ua._klaidi4_.physics.level1.lab1_6.view.TrifilarCanvas;
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

public class LabController16 extends BaseLabController {

    private TrifilarCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private ComboBox<String> configCombo;
    private TextField m0Field;
    private TextField m1Field;
    private TextField rField;
    private TextField r1Field;
    private TextField dField;
    private TextField lField;
    private TextField oscField;
    private Button startBtn;
    private Button autoBtn;
    private Button clearBtn;
    private Label liveStatusLabel;
    private Label liveOscLabel;
    private Label liveTimeLabel;
    private Queue<Integer> autoQueue = new LinkedList<>();
    private boolean isAutoRunning = false;
    private int targetOscillations = 10;

    public LabController16() {
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

        Label title = new Label("Система управління (Лаб 1-6)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane labPane = new TitledPane();
        labPane.setText("Параметри установки");
        labPane.setCollapsible(true);
        labPane.setExpanded(true);

        VBox paramsBox = new VBox(12);
        paramsBox.setPadding(new Insets(5));

        configCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Порожня платформа",
                "Платформа + 1 диск (центр)",
                "Платформа + 2 диски (зміщені)"
        ));
        configCombo.getSelectionModel().selectFirst();

        m0Field = new TextField("0.500");
        m1Field = new TextField("0.200");
        rField = new TextField("0.12");
        r1Field = new TextField("0.04");
        lField = new TextField("1.0");
        dField = new TextField("0.06");
        oscField = new TextField("10");

        configCombo.setOnAction(e -> applyPhysicsSettings());
        m0Field.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        rField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        r1Field.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        dField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());
        oscField.textProperty().addListener((o, ov, nv) -> applyPhysicsSettings());

        paramsBox.getChildren().addAll(
                createInputGroup("Конфігурація:", configCombo),
                createInputGroup("Маса платформи m0 (кг):", m0Field),
                createInputGroup("Маса 1 диска m1 (кг):", m1Field),
                createInputGroup("Радіус платформи R (м):", rField),
                createInputGroup("Радіус диска r1 (м):", r1Field),
                createInputGroup("Довжина ниток L (м):", lField),
                createInputGroup("Зміщення O' d (м):", dField),
                createInputGroup("Кількість коливань n:", oscField)
        );

        ScrollPane scrollParams = new ScrollPane(paramsBox);
        scrollParams.setFitToWidth(true);
        scrollParams.setPrefViewportHeight(280);
        scrollParams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");

        labPane.setContent(scrollParams);

        startBtn = new Button("▶ ЗАПУСТИТИ КОЛИВАННЯ");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startManual());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ (3 етапи)");
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

        leftPanel.getChildren().addAll(title, labPane, startBtn, autoBtn, clearBtn);

        canvas = new TrifilarCanvas(600, 440);

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
        Label dashTitle = new Label("СЕКУНДОМІР");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveOscLabel = new Label("Коливання: 0");
        liveOscLabel.setStyle("-fx-text-fill: #3498db;");
        liveTimeLabel = new Label("t = 0.00 с");
        liveTimeLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        liveTimeLabel.setStyle("-fx-text-fill: #00ff00;");
        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveOscLabel, liveTimeLabel);

        StackPane centerPanel = new StackPane(canvas, topBar, dash);
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));
        centerPanel.setStyle("-fx-background-color: #ffffff;");

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Measurement, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Measurement, String> confCol = new TableColumn<>("Конфігурація");
        confCol.setCellValueFactory(new PropertyValueFactory<>("config"));
        TableColumn<Measurement, Double> mCol = new TableColumn<>("m заг (кг)");
        mCol.setCellValueFactory(new PropertyValueFactory<>("mTotal"));
        TableColumn<Measurement, Integer> nCol = new TableColumn<>("n");
        nCol.setCellValueFactory(new PropertyValueFactory<>("n"));
        TableColumn<Measurement, Double> tCol = new TableColumn<>("t (с)");
        tCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        TableColumn<Measurement, Double> pCol = new TableColumn<>("T (с)");
        pCol.setCellValueFactory(new PropertyValueFactory<>("period"));
        TableColumn<Measurement, Double> iCol = new TableColumn<>("I (кг·м²)");
        iCol.setCellValueFactory(new PropertyValueFactory<>("inertia"));

        table.getColumns().addAll(idCol, confCol, mCol, nCol, tCol, pCol, iCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(130);

        VBox statsBox = createStatsBox();

        VBox bottomPanel = new VBox(5, table, statsBox);
        bottomPanel.setPadding(new Insets(5));

        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);
    }

    private void applyPhysicsSettings() {
        try {
            int conf = configCombo.getSelectionModel().getSelectedIndex();
            double R = Double.parseDouble(rField.getText());
            double r1 = Double.parseDouble(r1Field.getText());
            double d = Double.parseDouble(dField.getText());
            canvas.setParameters(conf, R, r1, d);
        } catch (NumberFormatException ignored) {}
    }

    private void setControlsDisable(boolean disable) {
        startBtn.setDisable(disable);
        autoBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        configCombo.setDisable(disable);
        m0Field.setDisable(disable);
        m1Field.setDisable(disable);
        rField.setDisable(disable);
        r1Field.setDisable(disable);
        dField.setDisable(disable);
        lField.setDisable(disable);
        oscField.setDisable(disable);
    }

    private void startManual() {
        try {
            Double.parseDouble(m0Field.getText());
            targetOscillations = Integer.parseInt(oscField.getText());
            if (targetOscillations <= 0) throw new NumberFormatException();

            isAutoRunning = false;
            runSimulation(configCombo.getSelectionModel().getSelectedIndex());
        } catch (Exception e) {
            showAlert("Помилка", "Введіть коректні числа в поля.");
        }
    }

    private void startAuto() {
        try {
            targetOscillations = Integer.parseInt(oscField.getText());
            if (targetOscillations <= 0) {
                targetOscillations = 10;
                oscField.setText("10");
            }
        } catch (Exception e) {
            targetOscillations = 10;
            oscField.setText("10");
        }

        data.clear();
        idCounter = 1;
        updateStats();
        autoQueue.clear();

        autoQueue.add(0);
        autoQueue.add(1);
        autoQueue.add(2);

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
        int nextConfig = autoQueue.poll();
        configCombo.getSelectionModel().select(nextConfig);
        applyPhysicsSettings();
        runSimulation(nextConfig);
    }

    private void runSimulation(int configIndex) {
        setControlsDisable(true);
        liveStatusLabel.setText("Статус: КОЛИВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: cyan;");

        double m0 = Double.parseDouble(m0Field.getText());
        double m1 = Double.parseDouble(m1Field.getText());
        double R = Double.parseDouble(rField.getText());
        double r1 = Double.parseDouble(r1Field.getText());
        double d = Double.parseDouble(dField.getText());
        double L = Double.parseDouble(lField.getText());

        double iTheory = 0;
        double tempMTotal = m0;
        double i0 = 0.5 * m0 * R * R;

        if (configIndex == 0) {
            iTheory = i0;
        } else if (configIndex == 1) {
            tempMTotal = m0 + m1;
            double iDisk = 0.5 * m1 * r1 * r1;
            iTheory = i0 + iDisk;
        } else if (configIndex == 2) {
            tempMTotal = m0 + 2 * m1;
            double iDiskCenter = 0.5 * m1 * r1 * r1;
            double iDiskShifted = iDiskCenter + m1 * d * d;
            iTheory = i0 + 2 * iDiskShifted;
        }

        final double finalMTotal = tempMTotal;
        final double exactPeriod = 2 * Math.PI * Math.sqrt((iTheory * L) / (finalMTotal * 9.81 * R * R));
        final double finalTotalTime = (exactPeriod * targetOscillations) + (Math.random() - 0.5) * 0.2;
        final double finalMeasuredPeriod = finalTotalTime / targetOscillations;

        canvas.startSimulation(finalMeasuredPeriod);

        new Thread(() -> {
            long start = System.currentTimeMillis();
            long targetMs = (long) (finalTotalTime * 1000);

            while (System.currentTimeMillis() - start < targetMs) {
                long elapsed = System.currentTimeMillis() - start;
                double seconds = elapsed / 1000.0;
                int osc = (int) (seconds / finalMeasuredPeriod);

                Platform.runLater(() -> {
                    liveTimeLabel.setText(String.format("t = %.2f с", seconds));
                    liveOscLabel.setText("Коливання: " + osc);
                });

                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }

            Platform.runLater(() -> finishMeasurement(configIndex, finalMTotal, finalTotalTime, finalMeasuredPeriod));
        }).start();
    }

    private void finishMeasurement(int configIndex, double mTotal, double totalTime, double measuredPeriod) {
        canvas.stopAnimation();
        liveOscLabel.setText("Коливання: " + targetOscillations);
        liveTimeLabel.setText(String.format("t = %.2f с", totalTime));
        liveStatusLabel.setText("Статус: ЗАВЕРШЕНО");
        liveStatusLabel.setStyle("-fx-text-fill: #00ff00;");

        double R = Double.parseDouble(rField.getText());
        double L = Double.parseDouble(lField.getText());
        double experimentalInertia = (mTotal * 9.81 * R * R * measuredPeriod * measuredPeriod) / (4 * Math.PI * Math.PI * L);

        String configName = configCombo.getItems().get(configIndex);

        Measurement meas = new Measurement(
                idCounter++, configName, mTotal, targetOscillations,
                Math.round(totalTime * 100.0) / 100.0,
                Math.round(measuredPeriod * 1000.0) / 1000.0,
                Math.round(experimentalInertia * 100000.0) / 100000.0
        );
        data.add(meas);
        updateStats();

        if (isAutoRunning) {
            new Thread(() -> {
                try { Thread.sleep(1500); Platform.runLater(this::processNextAuto); }
                catch (InterruptedException ignored) {}
            }).start();
        } else {
            setControlsDisable(false);
        }
    }

    private void updateStats() {
        if (data.size() < 3) {
            finalResultLabel.setText("Зробіть заміри для всіх трьох конфігурацій (або натисніть 'Автопроходження'), щоб побачити повний аналіз.");
            return;
        }

        Measurement m0 = data.get(data.size() - 3);
        Measurement m1 = data.get(data.size() - 2);
        Measurement m2 = data.get(data.size() - 1);

        double I0 = m0.getInertia();
        double I1 = m1.getInertia();
        double I2 = m2.getInertia();
        double mass1 = Double.parseDouble(m1Field.getText());
        double r1 = Double.parseDouble(r1Field.getText());
        double d = Double.parseDouble(dField.getText());

        double Id_exp = I1 - I0;
        double Id_theory = 0.5 * mass1 * r1 * r1;
        double I_shifted_exp = (I2 - I0) / 2.0;
        double I_shifted_theory = Id_theory + mass1 * d * d;
        double error1 = Math.abs(Id_exp - Id_theory);
        double eps1 = (error1 / Id_theory) * 100;
        double error2 = Math.abs(I_shifted_exp - I_shifted_theory);
        double eps2 = (error2 / I_shifted_theory) * 100;

        String conclusion = String.format(
                "ОБРОБКА РЕЗУЛЬТАТІВ ТА АНАЛІЗ (Теорема Штейнера):\n" +
                        "1. I0 (порожня платформа) = %.5f кг·м² (при T0 = %.3f с).\n" +
                        "2. I1 (платф. + 1 диск) = %.5f кг·м² (при T1 = %.3f с).\n" +
                        "3. I_д (експер., диск по центру) = I1 - I0 = %.5f кг·м².\n" +
                        "4. I_д' (теоретично) = 0.5*m1*r1² = %.5f кг·м².\n" +
                        "5. I2 (платф. + 2 зміщені диски) = %.5f кг·м² (при T2 = %.3f с).\n" +
                        "6. I_зміщ (експер., зміщений диск) = (I2 - I0)/2 = %.5f кг·м².\n" +
                        "7. I_зміщ' (теорема Штейнера) = I_д' + m1*d² = %.5f кг·м².\n" +
                        "8. Похибка перевірки теореми Штейнера: абсолютна ΔI = %.5f кг·м², відносна ε = %.1f %%.\n" +
                        "ВИСНОВОК: Дані підтверджують справедливість теореми Штейнера в межах експериментальної похибки.",
                I0, m0.getPeriod(), I1, m1.getPeriod(), Id_exp, Id_theory,
                I2, m2.getPeriod(), I_shifted_exp, I_shifted_theory, error2, eps2
        );

        finalResultLabel.setText(conclusion);
    }
}