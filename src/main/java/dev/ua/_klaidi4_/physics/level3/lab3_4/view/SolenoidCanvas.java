package dev.ua._klaidi4_.physics.level3.lab3_4.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SolenoidCanvas extends Canvas {

    private double length = 0.40;
    private double radius = 0.05;
    private double probePos = 0.0;
    private double currentAlpha = 0.0;
    private double targetAlpha = 0.0;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double elapsedMeasureTime = 0;
    private boolean isMeasuring = false;

    public SolenoidCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void setSetupParameters(double length, double radius, double probePos) {
        this.length = length;
        this.radius = radius;
        this.probePos = probePos;
        if (!isMeasuring) {
            draw();
        }
    }

    public void triggerMeasurement(double maxAlpha) {
        this.targetAlpha = maxAlpha;
        this.currentAlpha = 0;
        this.elapsedMeasureTime = 0;
        this.isMeasuring = true;

        if (timer != null) timer.stop();
        startAnimation();
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                elapsedMeasureTime += dt;

                double kickTime = 0.2;
                if (elapsedMeasureTime < kickTime) {
                    currentAlpha = targetAlpha * Math.sin((Math.PI / 2) * (elapsedMeasureTime / kickTime));
                } else {
                    currentAlpha = targetAlpha * Math.exp(-(elapsedMeasureTime - kickTime) * 1.5);
                }

                if (elapsedMeasureTime > 3.0) {
                    isMeasuring = false;
                    currentAlpha = 0;
                    this.stop();
                    lastTime = 0;
                }
                draw();
            }
        };
        timer.start();
    }

    public void stopAnimation() {
        if (timer != null) timer.stop();
        isMeasuring = false;
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

        double originX = w / 2;
        double originY = h / 2 - 40;
        double scale = 600;

        gc.setStroke(Color.web("#adb5bd"));
        gc.setLineWidth(1.5);
        gc.setLineDashes(10, 10);
        gc.strokeLine(20, originY, w - 20, originY);
        gc.setLineDashes(null);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("System", FontWeight.NORMAL, 12));
        gc.fillText("Ось X", w - 45, originY - 10);
        gc.fillText("x=0", originX - 10, originY + 90);
        gc.strokeLine(originX, originY + 10, originX, originY + 75);

        double drawL = length * scale;
        double drawR = radius * scale;

        LinearGradient coreGrad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ced4da")), new Stop(0.5, Color.WHITE), new Stop(1, Color.web("#6c757d")));
        gc.setFill(coreGrad);
        gc.fillRect(originX - drawL / 2, originY - drawR, drawL, drawR * 2);

        gc.setStroke(Color.web("#b87333"));
        gc.setLineWidth(2);
        int visualTurns = (int) (length * 100);
        if (visualTurns < 10) visualTurns = 10;
        if (visualTurns > 100) visualTurns = 100;

        for (int i = 0; i <= visualTurns; i++) {
            double wireX = originX - drawL / 2 + i * (drawL / visualTurns);
            gc.strokeLine(wireX, originY - drawR, wireX, originY + drawR);
        }

        double drawProbeX = originX + probePos * scale;
        gc.setFill(Color.web("#e74c3c"));
        gc.fillRoundRect(drawProbeX - 5, originY - drawR * 0.8, 10, drawR * 1.6, 5, 5);
        gc.setStroke(Color.web("#c0392b"));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(drawProbeX - 5, originY - drawR * 0.8, 10, drawR * 1.6, 5, 5);

        gc.setStroke(Color.web("#34495e"));
        gc.setLineWidth(1.5);
        gc.strokeLine(drawProbeX, originY + drawR * 0.8, drawProbeX, originY + drawR + 20);
        gc.strokeLine(drawProbeX, originY + drawR + 20, originX, originY + drawR + 40);

        drawGalvanometer(gc, w / 2, h - 60);
    }

    private void drawGalvanometer(GraphicsContext gc, double cx, double cy) {
        gc.setFill(Color.web("#2c3e50"));
        gc.fillRoundRect(cx - 120, cy - 40, 240, 80, 10, 10);
        gc.setFill(Color.web("#ecf0f1"));
        gc.fillRoundRect(cx - 110, cy - 30, 220, 60, 5, 5);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("System", FontWeight.BOLD, 10));
        gc.fillText("БАЛІСТИЧНИЙ ГАЛЬВАНОМЕТР", cx - 80, cy - 15);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeLine(cx - 95, cy + 10, cx + 95, cy + 10);
        for (int i = -60; i <= 60; i += 10) {
            double tickX = cx + i * 1.5;
            gc.strokeLine(tickX, cy + 10, tickX, cy + (i == 0 ? 0 : 5));
        }

        double indicatorX = cx + currentAlpha * 1.5;
        gc.setFill(Color.web("#e74c3c", 0.8));
        gc.fillOval(indicatorX - 3, cy + 5, 6, 10);

        gc.setFill(Color.web("#e74c3c"));
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.fillText(String.format("α = %.1f", currentAlpha), cx - 25, cy + 25);
    }
}