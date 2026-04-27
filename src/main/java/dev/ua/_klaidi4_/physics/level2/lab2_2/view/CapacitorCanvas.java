package dev.ua._klaidi4_.physics.level2.lab2_2.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CapacitorCanvas extends Canvas {

    private int connectionMode = 0;
    private boolean isMeasuring = false;
    private double currentC1 = 4.7;
    private double currentC2 = 2.2;
    private double visualScale = 1.0;

    public CapacitorCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void setConnectionMode(int mode) {
        this.connectionMode = mode;
        draw();
    }

    public void updateValues(double c1, double c2) {
        this.currentC1 = c1;
        this.currentC2 = c2;
        draw();
    }

    public void startSimulation() {
        this.isMeasuring = true;
        this.visualScale = 1.1;
        draw();
    }

    public void stopSimulation() {
        this.isMeasuring = false;
        this.visualScale = 1.0;
        draw();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#e9ecef"));
        gc.setLineWidth(1);
        for (int i = 0; i < w; i += 20) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 20) gc.strokeLine(0, i, w, i);

        double devWidth = 160;
        double devHeight = 110;
        double devX = w / 2 - devWidth / 2;
        double devY = h - devHeight - 20;

        LinearGradient deviceGrad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#455a64")), new Stop(1, Color.web("#263238")));

        gc.setFill(deviceGrad);
        gc.fillRoundRect(devX, devY, devWidth, devHeight, 15, 15);
        gc.setStroke(Color.web("#1c2833"));
        gc.setLineWidth(3);
        gc.strokeRoundRect(devX, devY, devWidth, devHeight, 15, 15);

        gc.setFill(Color.web("#cfd8dc"));
        gc.fillRect(devX + 15, devY + 15, devWidth - 30, 45);
        gc.setStroke(Color.web("#90a4ae"));
        gc.setLineWidth(2);
        gc.strokeRect(devX + 15, devY + 15, devWidth - 30, 45);

        gc.setFill(isMeasuring ? Color.web("#ff1744") : Color.web("#00e676"));
        gc.fillOval(devX + devWidth / 2 - 10, devY + 75, 20, 20);

        gc.setFill(Color.web("#212121"));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        String statusText = isMeasuring ? "MEASURING" : "READY";
        gc.fillText(statusText, devX + 25, devY + 45);

        Color wireColor = isMeasuring ? Color.web("#f57c00") : Color.BLACK;
        gc.setStroke(wireColor);
        gc.setLineWidth(3);

        gc.strokeLine(devX + 30, devY, w / 2 - 100, 150);
        gc.strokeLine(devX + devWidth - 30, devY, w / 2 + 100, 150);

        gc.setStroke(Color.web("#37474f"));
        gc.setLineWidth(2.5);

        double cx = w / 2;

        if (connectionMode == 0) {
            gc.strokeLine(cx - 100, 150, cx - 20, 150);
            gc.strokeLine(cx + 20, 150, cx + 100, 150);
            drawCapacitor(gc, cx, 150, "C1 (" + currentC1 + " мкФ)", isMeasuring);
        } else if (connectionMode == 1) {
            gc.strokeLine(cx - 100, 150, cx - 20, 150);
            gc.strokeLine(cx + 20, 150, cx + 100, 150);
            drawCapacitor(gc, cx, 150, "C2 (" + currentC2 + " мкФ)", isMeasuring);
        } else if (connectionMode == 2) {
            gc.strokeLine(cx - 100, 150, cx - 60, 150);
            gc.strokeLine(cx + 60, 150, cx + 100, 150);

            gc.setFill(Color.BLACK);
            gc.fillOval(cx - 64, 146, 8, 8);
            gc.fillOval(cx + 56, 146, 8, 8);

            gc.strokeLine(cx - 60, 100, cx - 60, 200);
            gc.strokeLine(cx + 60, 100, cx + 60, 200);

            gc.strokeLine(cx - 60, 100, cx - 20, 100);
            gc.strokeLine(cx + 20, 100, cx + 60, 100);
            drawCapacitor(gc, cx, 100, "C1", isMeasuring);

            gc.strokeLine(cx - 60, 200, cx - 20, 200);
            gc.strokeLine(cx + 20, 200, cx + 60, 200);
            drawCapacitor(gc, cx, 200, "C2", isMeasuring);
        } else if (connectionMode == 3) {
            gc.strokeLine(cx - 100, 150, cx - 70, 150);
            drawCapacitor(gc, cx - 50, 150, "C1", isMeasuring);
            gc.strokeLine(cx - 30, 150, cx + 30, 150);
            drawCapacitor(gc, cx + 50, 150, "C2", isMeasuring);
            gc.strokeLine(cx + 70, 150, cx + 100, 150);
        }

        gc.setFill(Color.web("#455a64"));
        gc.setFont(Font.font("System", FontWeight.BOLD, 14));
        String modeName = "";
        if(connectionMode == 0) modeName = "Окремий конденсатор C1";
        if(connectionMode == 1) modeName = "Окремий конденсатор C2";
        if(connectionMode == 2) modeName = "Паралельне з'єднання (C = C1 + C2)";
        if(connectionMode == 3) modeName = "Послідовне з'єднання (1/C = 1/C1 + 1/C2)";
        gc.fillText("Поточний режим: " + modeName, 20, 30);
    }

    private void drawCapacitor(GraphicsContext gc, double x, double y, String label, boolean active) {
        Color capColor = active ? Color.web("#1565c0") : Color.BLACK;
        double gap = 10 * visualScale;
        double height = 25 * visualScale;

        gc.setStroke(capColor);
        gc.setLineWidth(4);
        gc.strokeLine(x - gap, y - height, x - gap, y + height);
        gc.strokeLine(x + gap, y - height, x + gap, y + height);

        if(active) {
            gc.setFill(Color.web("#e3f2fd", 0.6));
            gc.fillRect(x - gap + 2, y - height, gap * 2 - 4, height * 2);
        }

        gc.setFill(Color.web("#0d47a1"));
        gc.setFont(Font.font("System", FontWeight.BOLD, 14));
        double textOffset = label.length() * 4;
        gc.fillText(label, x - textOffset, y - height - 10);
    }
}