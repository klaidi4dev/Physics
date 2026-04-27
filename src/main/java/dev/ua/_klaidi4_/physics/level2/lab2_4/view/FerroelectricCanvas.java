package dev.ua._klaidi4_.physics.level2.lab2_4.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class FerroelectricCanvas extends Canvas {

    private double targetTemperature = 20.0;
    private double displayedTemperature = 20.0;
    private boolean isHeating = false;
    private AnimationTimer timer;
    private long lastTime = 0;

    public FerroelectricCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void updateTemperature(double temp) {
        this.targetTemperature = temp;
    }

    public void startSimulation() {
        this.isHeating = true;
        if (timer != null) timer.stop();
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                lastTime = now;

                if (Math.abs(displayedTemperature - targetTemperature) > 0.1) {
                    displayedTemperature += (targetTemperature - displayedTemperature) * 0.1;
                } else {
                    displayedTemperature = targetTemperature;
                }
                draw();
            }
        };
        timer.start();
    }

    public void stopSimulation() {
        this.isHeating = false;
        if (timer != null) timer.stop();
        this.displayedTemperature = targetTemperature;
        draw();
    }

    public void resetSystem() {
        this.isHeating = false;
        this.targetTemperature = 20.0;
        this.displayedTemperature = 20.0;
        if (timer != null) timer.stop();
        draw();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#eceff1"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#cfd8dc"));
        gc.setLineWidth(1);
        for (int i = 0; i < w; i += 20) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 20) gc.strokeLine(0, i, w, i);

        double cx = w / 2 - 30;
        double cy = h / 2 + 10;

        double ovenW = 120;
        double ovenH = 140;
        double ovenX = cx - ovenW / 2;
        double ovenY = cy - ovenH / 2;

        gc.setFill(Color.web("#607d8b"));
        gc.fillRoundRect(ovenX, ovenY, ovenW, ovenH, 10, 10);
        gc.setStroke(Color.web("#37474f"));
        gc.setLineWidth(3);
        gc.strokeRoundRect(ovenX, ovenY, ovenW, ovenH, 10, 10);

        Color heatColor = isHeating ? Color.web("#ff5722") : Color.web("#90a4ae");
        gc.setStroke(heatColor);
        gc.setLineWidth(4);
        for (int i = 0; i < 4; i++) {
            double hy = ovenY + 25 + i * 30;
            gc.strokeLine(ovenX + 10, hy, ovenX + ovenW - 10, hy);
        }

        double crystalW = 40;
        double crystalH = 20;
        gc.setFill(Color.web("#80cbc4"));
        gc.fillRect(cx - crystalW / 2, cy - crystalH / 2, crystalW, crystalH);

        gc.setFill(Color.web("#cfd8dc"));
        gc.fillRect(cx - crystalW / 2, cy - crystalH / 2 - 5, crystalW, 5);
        gc.fillRect(cx - crystalW / 2, cy + crystalH / 2, crystalW, 5);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(cx, cy - crystalH / 2 - 5, cx, ovenY - 20);
        gc.strokeLine(cx, ovenY - 20, 60, ovenY - 20);

        gc.strokeLine(cx, cy + crystalH / 2 + 5, cx, ovenY + ovenH + 20);
        gc.strokeLine(cx, ovenY + ovenH + 20, 60, ovenY + ovenH + 20);

        gc.setStroke(Color.web("#d32f2f"));
        gc.setLineWidth(1.5);
        gc.strokeLine(cx + crystalW / 2, cy, cx + ovenW / 2 + 30, cy);
        gc.strokeLine(cx + ovenW / 2 + 30, cy, cx + ovenW / 2 + 30, h - 30);

        gc.setFill(Color.web("#455a64"));
        gc.fillRect(cx + ovenW / 2 + 10, h - 45, 40, 30);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 10));
        gc.fillText("mV", cx + ovenW / 2 + 18, h - 25);

        double tempRatio = (displayedTemperature - 20) / 120.0;
        if (tempRatio > 1.0) tempRatio = 1.0;
        if (tempRatio < 0) tempRatio = 0;

        Color tempColor = Color.web("#4fc3f7").interpolate(Color.web("#d32f2f"), tempRatio);

        double thermX = cx - ovenW / 2 - 30;
        double thermY = cy - 50;
        gc.setFill(Color.WHITE);
        gc.fillRoundRect(thermX, thermY, 10, 100, 5, 5);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRoundRect(thermX, thermY, 10, 100, 5, 5);

        gc.setFill(tempColor);
        double fillH = 20 + 70 * tempRatio;
        gc.fillRect(thermX + 2, thermY + 100 - fillH, 6, fillH);
        gc.fillOval(thermX - 2, thermY + 95, 14, 14);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("System", FontWeight.BOLD, 14));
        gc.fillText("BaTiO3", cx - 22, cy - 25);
    }
}