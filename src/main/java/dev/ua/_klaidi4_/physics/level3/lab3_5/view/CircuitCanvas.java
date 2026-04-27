package dev.ua._klaidi4_.physics.level3.lab3_5.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CircuitCanvas extends Canvas {

    private double currentU = 0.0;
    private double currentI = 0.0;
    private double currentP = 0.0;
    private boolean hasCore = false;

    public CircuitCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void updateMeters(double u, double i, double p, boolean core) {
        this.currentU = u;
        this.currentI = i;
        this.currentP = p;
        this.hasCore = core;
        draw();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#2c3e50"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#bdc3c7"));
        gc.setLineWidth(4);
        gc.strokeLine(50, 150, 50, 50);
        gc.strokeLine(50, 50, 550, 50);
        gc.strokeLine(550, 50, 550, 150);
        gc.strokeLine(550, 250, 550, 320);
        gc.strokeLine(550, 320, 50, 320);
        gc.strokeLine(50, 320, 50, 250);
        gc.strokeLine(150, 50, 150, 150);
        gc.strokeLine(150, 220, 150, 320);

        gc.setFill(Color.web("#7f8c8d"));
        gc.fillRoundRect(20, 150, 60, 100, 10, 10);
        gc.setFill(Color.web("#ecf0f1"));
        gc.fillOval(35, 185, 30, 30);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeText("~", 46, 205);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.fillText("ЛАТР", 35, 145);

        double coilX = 520;
        double coilY = 150;

        if (hasCore) {
            gc.setFill(Color.web("#34495e"));
            gc.fillRect(coilX + 10, coilY - 20, 40, 140);
            gc.setFill(Color.web("#95a5a6"));
            gc.fillRect(coilX + 15, coilY - 20, 10, 140);
        } else {
            gc.setFill(Color.web("#ecf0f1", 0.5));
            gc.fillRect(coilX + 10, coilY, 40, 100);
        }

        gc.setStroke(Color.web("#d35400"));
        gc.setLineWidth(6);
        for (int i = 0; i < 10; i++) {
            gc.strokeOval(coilX, coilY + i * 10, 60, 15);
        }
        gc.setFill(Color.WHITE);
        gc.fillText("L, R", coilX + 70, coilY + 50);

        drawMeter(gc, "ВОЛЬТМЕТР (V)", 100, 150, currentU, "V", "#2980b9");
        drawMeter(gc, "АМПЕРМЕТР (A)", 260, 20, currentI, "A", "#c0392b");
        drawMeter(gc, "ВАТМЕТР (W)", 420, 20, currentP, "W", "#27ae60");
    }

    private void drawMeter(GraphicsContext gc, String title, double x, double y, double value, String unit, String colorStr) {
        gc.setFill(Color.web("#34495e"));
        gc.fillRoundRect(x, y, 100, 70, 10, 10);
        gc.setFill(Color.web(colorStr));
        gc.fillRoundRect(x, y, 100, 20, 10, 10);
        gc.fillRect(x, y + 10, 100, 10);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 10));
        gc.fillText(title, x + 5, y + 14);
        gc.setFill(Color.web("#000000"));
        gc.fillRect(x + 10, y + 30, 80, 30);
        gc.setFill(Color.web("#2ecc71"));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        String valStr = String.format("%.2f", value);
        gc.fillText(valStr, x + 15, y + 52);
        gc.setFont(Font.font("System", FontWeight.NORMAL, 12));
        gc.fillText(unit, x + 75, y + 52);
    }
}