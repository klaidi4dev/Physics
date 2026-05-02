package dev.ua._klaidi4_.physics.level2.lab2_5.controller;

import dev.ua._klaidi4_.physics.core.controller.BaseLabController;
import dev.ua._klaidi4_.physics.level2.lab2_5.model.Measurement;
import dev.ua._klaidi4_.physics.level2.lab2_5.view.CompensationCanvas;
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

public class LabController25 extends BaseLabController {

    private CompensationCanvas canvas;
    private TableView<Measurement> table;
    private ObservableList<Measurement> data;
    private int idCounter = 1;
    private TextField fieldE1, fieldEn, fieldLmax, fieldRg;
    private RadioButton rbEx, rbEn;
    private TextField lengthField;
    private Button recordBtn, autoBtn, clearBtn;
    private Label liveStatusLabel, liveSourceLabel, liveIgLabel;

    private double currentE1 = 2.0;
    private double currentEn = 1.0186;
    private double currentLmax = 1000.0;
    private double Ex;
    private double currentIg = 0.0;

    private boolean isAutoRunning = false;
    private Queue<Integer> autoQueue = new LinkedList<>();

    public LabController25() {
        generateUnknownEx();
        initUI();
    }

    @Override
    public void shutdown() {
        isAutoRunning = false;
        autoQueue.clear();
        if (canvas != null) canvas.stopSimulation();
    }

    private void generateUnknownEx() {
        double rawEx = 1.2 + Math.random() * 0.4;
        Ex = Math.round(rawEx * 10000.0) / 10000.0;
    }

    private void initUI() {
        leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(330);
        leftPanel.setMinWidth(330);
        leftPanel.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #cfd8dc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Система управління (Лаб 2-5)");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        TitledPane paramsPane = new TitledPane();
        paramsPane.setText("Параметри установки");
        paramsPane.setCollapsible(false);
        VBox paramsBox = new VBox(10);
        paramsBox.setPadding(new Insets(5));

        fieldE1 = new TextField("2.0");
        fieldEn = new TextField("1.0186");
        fieldLmax = new TextField("1000.0");
        fieldRg = new TextField("100.0");

        fieldE1.textProperty().addListener((o, old, val) -> updatePhysics());
        fieldEn.textProperty().addListener((o, old, val) -> updatePhysics());
        fieldLmax.textProperty().addListener((o, old, val) -> updatePhysics());
        fieldRg.textProperty().addListener((o, old, val) -> updatePhysics());

        paramsBox.getChildren().addAll(
                createInputGroup("ЕРС робочої батареї E1 (В):", fieldE1),
                createInputGroup("ЕРС еталона En (В):", fieldEn),
                createInputGroup("Довжина реохорда L_max (мм):", fieldLmax),
                createInputGroup("Опір гальванометра Rg (Ом):", fieldRg)
        );
        paramsPane.setContent(paramsBox);

        TitledPane controlPane = new TitledPane();
        controlPane.setText("Управління компенсацією");
        controlPane.setCollapsible(false);
        VBox controlBox = new VBox(12);
        controlBox.setPadding(new Insets(5));

        ToggleGroup group = new ToggleGroup();
        rbEx = new RadioButton("Підключити Ex (Невідомий)");
        rbEx.setToggleGroup(group);
        rbEx.setSelected(true);
        rbEn = new RadioButton("Підключити En (Еталон)");
        rbEn.setToggleGroup(group);
        group.selectedToggleProperty().addListener((o, old, val) -> updatePhysics());

        lengthField = new TextField("500.0");
        lengthField.textProperty().addListener((o, old, val) -> {
            if (lengthField.isFocused()) {
                updatePhysics();
            }
        });

        controlBox.getChildren().addAll(
                rbEx, rbEn,
                createInputGroup("Точка контакту l (мм):", lengthField)
        );
        controlPane.setContent(controlBox);

        recordBtn = new Button("✍ ЗАПИСАТИ ПОКАЗНИКИ");
        recordBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        recordBtn.setMaxWidth(Double.MAX_VALUE);
        recordBtn.setOnAction(e -> recordMeasurement());

        autoBtn = new Button("⚙ АВТОПРОХОДЖЕННЯ");
        autoBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setOnAction(e -> startAuto());

        clearBtn = new Button("🗑 ОЧИСТИТИ ТАБЛИЦЮ");
        clearBtn.setStyle("-fx-background-color: #ef6c00; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> resetAll());

        ScrollPane scrollLeft = new ScrollPane(new VBox(10, title, paramsPane, controlPane, recordBtn, autoBtn, clearBtn));
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        leftPanel.getChildren().add(scrollLeft);

        canvas = new CompensationCanvas(600, 350);

        Button toggleSidebarBtn = new Button("◀ Приховати панель");
        toggleSidebarBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar(toggleSidebarBtn));

        HBox topBar = new HBox(toggleSidebarBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);

        VBox dash = new VBox(2);
        dash.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-radius: 5;");
        dash.setMaxSize(200, 80);

        Label dashTitle = new Label("ДАТЧИК ГАЛЬВАНОМЕТРА");
        dashTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        liveStatusLabel = new Label("Статус: ОЧІКУВАННЯ");
        liveStatusLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 11px;");
        liveSourceLabel = new Label("Підключено: Ex");
        liveSourceLabel.setStyle("-fx-text-fill: #00ff00;");
        liveIgLabel = new Label("Ig = 0.0 мкА");
        liveIgLabel.setStyle("-fx-text-fill: #ff3333; -fx-font-weight: bold;");

        dash.getChildren().addAll(dashTitle, liveStatusLabel, liveSourceLabel, liveIgLabel);

        StackPane canvasStack = new StackPane(canvas, topBar, dash);
        canvasStack.setStyle("-fx-background-color: #ffffff;");
        StackPane.setAlignment(dash, Pos.TOP_RIGHT);
        StackPane.setMargin(dash, new Insets(10));

        VBox centerTopPanel = new VBox(canvasStack);
        VBox.setVgrow(canvasStack, Priority.ALWAYS);

        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        String[] cols = {"№", "Джерело", "Довжина l (мм)", "Струм Ig (мкА)"};
        String[] props = {"id", "source", "length", "currentG"};

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
        this.setCenter(centerTopPanel);
        this.setBottom(bottomPanel);

        canvas.startSimulation();
        updatePhysics();
    }

    private void updatePhysics() {
        if (isAutoRunning) return;

        double Rg = 100.0;
        try {
            currentE1 = Double.parseDouble(fieldE1.getText().replace(',', '.'));
            currentEn = Double.parseDouble(fieldEn.getText().replace(',', '.'));
            currentLmax = Double.parseDouble(fieldLmax.getText().replace(',', '.'));
            Rg = Double.parseDouble(fieldRg.getText().replace(',', '.'));
            if (Rg <= 0) Rg = 100.0;
        } catch (Exception ignored) {}

        double l = 500.0;
        try {
            l = Double.parseDouble(lengthField.getText().replace(',', '.'));
            if (l < 0) l = 0;
            if (l > currentLmax) l = currentLmax;
        } catch (Exception ignored) {}

        boolean isEx = rbEx.isSelected();
        double activeE = isEx ? Ex : currentEn;
        String sourceName = isEx ? "Ex" : "En";

        double Ul = currentE1 * (l / currentLmax);
        double igAmpere = (activeE - Ul) / Rg;
        currentIg = igAmpere * 1e6;

        final double finalL = l;
        Platform.runLater(() -> {
            liveSourceLabel.setText("Підключено: " + sourceName);
            if (Math.abs(currentIg) < 1.0) {
                liveIgLabel.setText(String.format(Locale.US, "Ig = %.1f мкА", currentIg));
                liveIgLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
                liveStatusLabel.setText("Статус: СКОМПЕНСОВАНО!");
            } else {
                liveIgLabel.setText(String.format(Locale.US, "Ig = %.1f мкА", currentIg));
                liveIgLabel.setStyle("-fx-text-fill: #ff3333; -fx-font-weight: bold;");
                liveStatusLabel.setText("Статус: НАЛАШТУВАННЯ...");
            }
            canvas.updatePhysics(finalL, currentLmax, currentIg, sourceName, currentE1);
        });
    }

    private void setControlsDisable(boolean disable) {
        fieldE1.setDisable(disable);
        fieldEn.setDisable(disable);
        fieldLmax.setDisable(disable);
        fieldRg.setDisable(disable);
        rbEx.setDisable(disable);
        rbEn.setDisable(disable);
        lengthField.setDisable(disable);
        recordBtn.setDisable(disable);
        clearBtn.setDisable(disable);
        autoBtn.setDisable(disable);
    }

    private void startAuto() {
        try {
            currentE1 = Double.parseDouble(fieldE1.getText().replace(',', '.'));
            currentEn = Double.parseDouble(fieldEn.getText().replace(',', '.'));
            currentLmax = Double.parseDouble(fieldLmax.getText().replace(',', '.'));
        } catch (Exception e) {
            showAlert("Помилка", "Перевірте правильність введених параметрів.");
            return;
        }

        if (currentE1 < Ex || currentE1 < currentEn) {
            showAlert("Фізична помилка",
                    String.format("ЕРС робочої батареї E1 (%.2f В) менша за еталонні джерела! Компенсація в принципі неможлива. Збільшіть E1.", currentE1));
            return;
        }

        data.clear();
        idCounter = 1;
        isAutoRunning = true;
        setControlsDisable(true);

        final double speed = 300.0;

        new Thread(() -> {
            Platform.runLater(() -> rbEx.setSelected(true));
            try { Thread.sleep(500); } catch (Exception ignored) {}

            double humanErrorX = (Math.random() - 0.5) * 1.6;
            double targetLx = (Ex / currentE1) * currentLmax + humanErrorX;
            animateTextTo(targetLx, Ex, "Ex", speed);

            Platform.runLater(this::recordMeasurement);
            try { Thread.sleep(1500); } catch (Exception ignored) {}

            Platform.runLater(() -> rbEn.setSelected(true));
            try { Thread.sleep(500); } catch (Exception ignored) {}

            double humanErrorN = (Math.random() - 0.5) * 1.6;
            double targetLn = (currentEn / currentE1) * currentLmax + humanErrorN;
            animateTextTo(targetLn, currentEn, "En", speed);

            Platform.runLater(this::recordMeasurement);
            try { Thread.sleep(1000); } catch (Exception ignored) {}

            Platform.runLater(() -> {
                isAutoRunning = false;
                setControlsDisable(false);
                liveStatusLabel.setText("Статус: АВТО ЗАВЕРШЕНО");
            });
        }).start();
    }

    private void animateTextTo(double targetValue, double activeE, String sourceName, double speedFactor) {
        double current = 0;
        try {
            current = Double.parseDouble(lengthField.getText().replace(',', '.'));
        } catch (Exception ignored) {}

        double step = (targetValue > current) ? speedFactor * 0.05 : -speedFactor * 0.05;

        Platform.runLater(() -> liveStatusLabel.setText("Статус: АВТОПОШУК " + sourceName + "..."));

        double Rg = 100.0;
        try { Rg = Double.parseDouble(fieldRg.getText().replace(',', '.')); } catch (Exception ignored) {}

        while (Math.abs(targetValue - current) > Math.abs(step) && isAutoRunning) {
            current += step;
            final double val = current;

            double Ul = currentE1 * (val / currentLmax);
            currentIg = ((activeE - Ul) / Rg) * 1e6;

            Platform.runLater(() -> {
                lengthField.setText(String.format(Locale.US, "%.1f", val));
                liveSourceLabel.setText("Підключено: " + sourceName);
                liveIgLabel.setText(String.format(Locale.US, "Ig = %.1f мкА", currentIg));
                canvas.updatePhysics(val, currentLmax, currentIg, sourceName, currentE1);
            });
            try { Thread.sleep(50); } catch (Exception ignored) {}
        }

        double finalUl = currentE1 * (targetValue / currentLmax);
        currentIg = ((activeE - finalUl) / Rg) * 1e6;

        Platform.runLater(() -> {
            lengthField.setText(String.format(Locale.US, "%.1f", targetValue));
            liveIgLabel.setText(String.format(Locale.US, "Ig = %.1f мкА", currentIg));

            if (Math.abs(currentIg) < 5.0) {
                liveIgLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
                liveStatusLabel.setText("Статус: СКОМПЕНСОВАНО!");
            } else {
                liveIgLabel.setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
                liveStatusLabel.setText("Статус: МАЙЖЕ СКОМПЕНСОВАНО");
            }

            canvas.updatePhysics(targetValue, currentLmax, currentIg, sourceName, currentE1);
        });
    }

    private void recordMeasurement() {
        String sourceName = rbEx.isSelected() ? "Ex" : "En";
        double l = 0;
        try { l = Double.parseDouble(lengthField.getText().replace(',', '.')); } catch (Exception ignored) {}

        l = Math.round(l * 10.0) / 10.0;
        double ig = Math.round(currentIg * 10.0) / 10.0;

        Measurement m = new Measurement(idCounter++, sourceName, l, ig);
        data.add(m);
        updateStats();
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

        double lx = -1;
        double ln = -1;
        double maxIgErr = 0;

        for (Measurement m : data) {
            if (m.getSource().equals("Ex")) {
                lx = m.getLength();
                if (Math.abs(m.getCurrentG()) > maxIgErr) maxIgErr = Math.abs(m.getCurrentG());
            }
            if (m.getSource().equals("En")) {
                ln = m.getLength();
                if (Math.abs(m.getCurrentG()) > maxIgErr) maxIgErr = Math.abs(m.getCurrentG());
            }
        }

        StringBuilder sb = new StringBuilder("ОБРОБКА РЕЗУЛЬТАТІВ:\n");

        if (lx != -1) {
            sb.append(String.format(Locale.US, "1. Довжина компенсації для невідомого джерела Ex: l1 = %.1f мм.\n", lx));
        } else {
            sb.append("1. Очікується вимірювання для невідомого джерела Ex.\n");
        }

        if (ln != -1) {
            sb.append(String.format(Locale.US, "2. Довжина компенсації для еталона En: l2 = %.1f мм.\n", ln));
        } else {
            sb.append("2. Очікується вимірювання для еталона En.\n");
        }

        if (lx != -1 && ln != -1) {
            double calcEx = currentEn * (lx / ln);
            double relErr = Math.abs(calcEx - Ex) / Ex * 100.0;

            sb.append(String.format(Locale.US, "3. Розрахунок за формулою Ex = En * (l1 / l2): Ex = %.4f * (%.1f / %.1f) = %.4f В.\n", currentEn, lx, ln, calcEx));

            if (maxIgErr > 15.0) {
                sb.append(String.format(Locale.US, "УВАГА: Вимірювання проведені дуже неточно (залишковий струм G: %.1f мкА).\n", maxIgErr));
                sb.append(String.format(Locale.US, "4. Відносна похибка симуляції: %.2f %%.\n", relErr));
            } else {
                sb.append(String.format(Locale.US, "4. Відносна похибка розрахунку (завдяки похибці реохорда): %.2f %%.\n", relErr));
                sb.append("5. Висновок: Компенсаційний метод дозволяє точно виміряти ЕРС без споживання струму від досліджуваного джерела.");
            }
        }

        finalResultLabel.setText(sb.toString());
    }

    private void resetAll() {
        data.clear();
        idCounter = 1;
        generateUnknownEx();
        lengthField.setText(String.format(Locale.US, "%.1f", currentLmax / 2.0));
        rbEx.setSelected(true);
        finalResultLabel.setText("Обробка результатів: Очікування вимірювань...");
        updatePhysics();
    }
}