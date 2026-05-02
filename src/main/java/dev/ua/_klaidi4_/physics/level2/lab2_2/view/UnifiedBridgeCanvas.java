package dev.ua._klaidi4_.physics.level2.lab2_2.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class UnifiedBridgeCanvas extends Canvas {

    private String currentDisplay = "---- pF";
    private double sampleThickness = 2.0;
    private Color sampleColor = Color.web("#80cbc4");
    private boolean isMeasuring = false;
    private String currentMode = "C1"; // "C1", "C2", "Parallel", "Series"

    public UnifiedBridgeCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void updateDisplay(String text, boolean measuring) {
        this.currentDisplay = text;
        this.isMeasuring = measuring;
        draw();
    }

    public void setMode(String mode) {
        this.currentMode = mode;
        draw();
    }

    public void updateSample(double thicknessMm, String materialType) {
        this.sampleThickness = thicknessMm;
        if (materialType != null) {
            switch (materialType) {
                case "Титанат барію (BaTiO3)": this.sampleColor = Color.web("#9ccc65"); break;
                case "Сегнетова сіль": this.sampleColor = Color.web("#ce93d8"); break;
                case "Скло": this.sampleColor = Color.web("#90caf9"); break;
                default: this.sampleColor = Color.GRAY;
            }
        }
        draw();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        // Background
        gc.setFill(Color.web("#eceff1"));
        gc.fillRect(0, 0, w, h);
        gc.setStroke(Color.web("#cfd8dc"));
        gc.setLineWidth(1);
        for (int i = 0; i < w; i += 20) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 20) gc.strokeLine(0, i, w, i);

        // Measuring Device (P-577 Bridge)
        double devWidth = 260;
        double devHeight = 160;
        double devX = w / 2 - devWidth / 2;
        double devY = h - devHeight - 30;

        LinearGradient deviceGrad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#cfd8dc")), new Stop(1, Color.web("#78909c")));
        gc.setFill(deviceGrad);
        gc.fillRoundRect(devX, devY, devWidth, devHeight, 15, 15);
        gc.setStroke(Color.web("#455a64"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(devX, devY, devWidth, devHeight, 15, 15);

        // Display Screen
        gc.setFill(Color.web("#1c2833"));
        gc.fillRect(devX + 30, devY + 20, 200, 60);
        gc.setStroke(Color.web("#17202a"));
        gc.strokeRect(devX + 30, devY + 20, 200, 60);

        // Display Text
        gc.setFill(isMeasuring ? Color.web("#aed6f1") : Color.web("#00ff00"));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 26));
        gc.fillText(currentDisplay, devX + 45, devY + 60);

        // Dials
        gc.setFill(Color.web("#37474f"));
        gc.fillOval(devX + 50, devY + 100, 40, 40);
        gc.fillOval(devX + 170, devY + 100, 40, 40);
        gc.setFill(Color.web("#b0bec5"));
        gc.fillOval(devX + 60, devY + 110, 20, 20);
        gc.fillOval(devX + 180, devY + 110, 20, 20);

        // Label
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.fillText("Міст Р-577", devX + 105, devY + 125);

        double capY = 60;

        if (currentMode.equals("C1") || currentMode.equals("C2")) {
            // Draw Single Capacitor with Dielectric (Phase 1)
            gc.setStroke(Color.web("#d32f2f"));
            gc.setLineWidth(3);
            gc.strokeLine(devX + 80, devY, w / 2 - 50, capY + 40);
            gc.setStroke(Color.web("#1976d2"));
            gc.strokeLine(devX + 180, devY, w / 2 + 50, capY + 40);

            double visualThickness = Math.max(10, sampleThickness * 10);
            double plateWidth = 160;

            gc.setFill(sampleColor);
            gc.fillRect(w / 2 - plateWidth / 2, capY, plateWidth, visualThickness);

            LinearGradient plateGrad = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#b0bec5")), new Stop(0.5, Color.WHITE), new Stop(1, Color.web("#90a4ae")));
            gc.setFill(plateGrad);
            gc.fillRect(w / 2 - plateWidth / 2, capY - 10, plateWidth, 10);
            gc.fillRect(w / 2 - plateWidth / 2, capY + visualThickness, plateWidth, 10);

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("System", FontWeight.BOLD, 14));
            gc.fillText("Вимірювання " + currentMode, w / 2 - 60, capY - 20);

        } else {
            // Draw Circuit (Phase 2)
            double cx = w / 2;
            gc.setStroke(Color.web("#d32f2f"));
            gc.setLineWidth(3);
            gc.strokeLine(devX + 80, devY, cx - 80, 150);
            gc.setStroke(Color.web("#1976d2"));
            gc.strokeLine(devX + 180, devY, cx + 80, 150);

            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2.5);

            if (currentMode.equals("Parallel")) {
                gc.strokeLine(cx - 80, 150, cx - 40, 150);
                gc.strokeLine(cx + 40, 150, cx + 80, 150);

                gc.setFill(Color.BLACK);
                gc.fillOval(cx - 44, 146, 8, 8);
                gc.fillOval(cx + 36, 146, 8, 8);

                gc.strokeLine(cx - 40, 100, cx - 40, 200);
                gc.strokeLine(cx + 40, 100, cx + 40, 200);

                gc.strokeLine(cx - 40, 100, cx - 15, 100);
                gc.strokeLine(cx + 15, 100, cx + 40, 100);
                drawSimpleCapacitor(gc, cx, 100, "C1");

                gc.strokeLine(cx - 40, 200, cx - 15, 200);
                gc.strokeLine(cx + 15, 200, cx + 40, 200);
                drawSimpleCapacitor(gc, cx, 200, "C2");

                gc.setFill(Color.BLACK);
                gc.setFont(Font.font("System", FontWeight.BOLD, 14));
                gc.fillText("Паралельне з'єднання", cx - 80, 50);

            } else if (currentMode.equals("Series")) {
                gc.strokeLine(cx - 80, 150, cx - 50, 150);
                drawSimpleCapacitor(gc, cx - 35, 150, "C1");
                gc.strokeLine(cx - 20, 150, cx + 20, 150);
                drawSimpleCapacitor(gc, cx + 35, 150, "C2");
                gc.strokeLine(cx + 50, 150, cx + 80, 150);

                gc.setFill(Color.BLACK);
                gc.setFont(Font.font("System", FontWeight.BOLD, 14));
                gc.fillText("Послідовне з'єднання", cx - 80, 50);
            }
        }
    }

    private void drawSimpleCapacitor(GraphicsContext gc, double x, double y, String label) {
        Color capColor = isMeasuring ? Color.web("#1565c0") : Color.BLACK;
        double gap = 8;
        double height = 20;

        gc.setStroke(capColor);
        gc.setLineWidth(4);
        gc.strokeLine(x - gap, y - height, x - gap, y + height);
        gc.strokeLine(x + gap, y - height, x + gap, y + height);

        gc.setFill(Color.web("#0d47a1"));
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.fillText(label, x - 10, y - height - 5);
    }
}