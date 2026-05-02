package dev.ua._klaidi4_.physics.level8.lab8_3.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Locale;

public class PhotoresistorCanvas extends Canvas {

    private double currentU = 0.0;
    private double currentR = 20.0;
    private double currentI = 0.0;
    private double targetU = 0.0;
    private double targetR = 20.0;
    private AnimationTimer timer;
    private double timeOffset = 0.0;

    public PhotoresistorCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    public void setTarget(double u, double r) {
        this.targetU = u;
        this.targetR = r;
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                timeOffset += 0.1;

                currentU += (targetU - currentU) * 0.1;
                currentR += (targetR - currentR) * 0.1;

                calculatePhysicsForDisplay();
                drawFrame();
            }
        };
        timer.start();
    }

    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    private void calculatePhysicsForDisplay() {
        if (currentR <= 0 || currentU <= 0.5) {
            currentI = 0;
            return;
        }
        currentI = (currentU / 10.0) * Math.pow(10.0 / currentR, 1.8) * 0.1;
    }

    private void drawFrame() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#0f172a"));
        gc.fillRect(0, 0, w, h);

        gc.setFill(Color.web("#1e293b"));
        gc.fillRect(0, h - 140, w, 140);
        gc.setStroke(Color.web("#020617"));
        gc.setLineWidth(3);
        gc.strokeLine(0, h - 140, w, h - 140);

        double railY = h - 110;
        LinearGradient railGrad = new LinearGradient(0, railY, 0, railY + 20, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#94a3b8")), new Stop(1, Color.web("#475569")));
        gc.setFill(railGrad);
        gc.fillRect(30, railY, w - 60, 20);

        gc.setStroke(Color.web("#0f172a"));
        gc.setLineWidth(1.5);
        for (int i = 50; i < w - 40; i += 10) {
            double tickH = ((i - 50) % 50 == 0) ? 10 : 5;
            gc.strokeLine(i, railY + 20, i, railY + 20 - tickH);
        }

        double sensorX = 100;
        double lampX = sensorX + (currentR * 10.0);

        if (currentU > 1.0) {
            double spread = 25 + (currentR * 0.6);
            double intensity = Math.min(0.9, (currentU / 150.0) * (20.0 / currentR));
            double flicker = 1.0 + Math.sin(timeOffset * 2.5) * 0.03;

            gc.setFill(Color.web("#fef08a", Math.max(0.05, intensity * flicker)));
            gc.fillPolygon(
                    new double[]{lampX - 25, lampX - 25, sensorX + 8, sensorX + 8},
                    new double[]{railY - 50, railY - 30, railY - 40 + spread/2, railY - 40 - spread/2},
                    4
            );
        }

        drawMount(gc, sensorX, railY);
        gc.setFill(Color.web("#020617"));
        gc.fillRect(sensorX - 10, railY - 60, 20, 40);
        gc.setFill(Color.web("#ef4444"));
        gc.fillRect(sensorX, railY - 50, 4, 20);

        drawMount(gc, lampX, railY);
        gc.setFill(Color.web("#334155"));
        gc.fillRoundRect(lampX - 25, railY - 65, 40, 50, 5, 5);
        gc.setFill(Color.web("#000000"));
        gc.fillRect(lampX - 30, railY - 55, 5, 30);

        if (currentU > 1.0) {
            gc.setFill(Color.web("#fef08a", 0.9));
            gc.fillRect(lampX - 32, railY - 53, 2, 26);
        }

        drawDigitalDisplay(gc, 60, 30, "Джерело U (В)", String.format(Locale.US, "%.1f", currentU), "#3b82f6");
        drawDigitalDisplay(gc, w - 240, 30, "Мікроамперметр (мА)", String.format(Locale.US, "%.3f", currentI), "#ef4444");

        gc.setStroke(Color.web("#64748b"));
        gc.setLineWidth(2);
        gc.strokeLine(sensorX, railY - 60, sensorX, 85);
        gc.strokeLine(sensorX, 85, w - 160, 85);
        gc.strokeLine(lampX + 10, railY - 65, lampX + 10, 60);
        gc.strokeLine(lampX + 10, 60, 140, 60);
    }

    private void drawMount(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.web("#64748b"));
        gc.fillPolygon(
                new double[]{x - 20, x + 20, x + 12, x - 12},
                new double[]{y + 20, y + 20, y - 5, y - 5},
                4
        );
        gc.setFill(Color.web("#cbd5e1"));
        gc.fillRect(x - 3, y - 40, 6, 40);
    }

    private void drawDigitalDisplay(GraphicsContext gc, double x, double y, String label, String value, String ledColor) {
        gc.setFill(Color.web("#1e293b"));
        gc.fillRoundRect(x, y, 180, 65, 8, 8);
        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, 180, 65, 8, 8);

        gc.setFill(Color.web("#94a3b8"));
        gc.setFont(Font.font("Consolas", FontWeight.NORMAL, 12));
        gc.fillText(label, x + 10, y + 20);

        gc.setFill(Color.web(ledColor));
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 24));
        gc.fillText(value, x + 15, y + 50);
    }
}