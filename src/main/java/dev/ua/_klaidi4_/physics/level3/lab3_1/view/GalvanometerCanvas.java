package dev.ua._klaidi4_.physics.level3.lab3_1.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;

public class GalvanometerCanvas extends Canvas {

    private double l0 = 20.0;
    private double currentI = 1.5;
    private double x = 0.0;
    private double targetN = 0;
    private double currentN = 0;
    private double measuredN = 0;
    private boolean isRunning = false;
    private double animTime = 0;
    private AnimationTimer timer;
    private long lastTime = 0;
    private Runnable onPulsePeak;
    private Runnable onFinish;

    public GalvanometerCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    public void setParameters(double l0, double currentI, double x) {
        this.l0 = l0;
        this.currentI = currentI;
        this.x = x;
        resetSystem();
    }

    public void setCallbacks(Runnable onPulsePeak, Runnable onFinish) {
        this.onPulsePeak = onPulsePeak;
        this.onFinish = onFinish;
    }

    public void resetSystem() {
        this.currentN = 0;
        this.animTime = 0;
        this.isRunning = false;
        draw();
    }

    public void startSimulation() {
        resetSystem();

        double k = 1400.0;
        double xFactor = 1.0 / (1.0 + 0.08 * x * x);
        targetN = (k * currentI / l0) * xFactor;

        targetN += (Math.random() - 0.5) * 0.8;
        if (targetN < 0) targetN = Math.abs(targetN);

        this.measuredN = targetN;
        this.isRunning = true;
    }

    public double getMeasuredN() {
        return measuredN;
    }

    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                if (dt > 0.05) dt = 0.05;

                if (isRunning) {
                    update(dt);
                }
                draw();
            }
        };
        timer.start();
    }

    private void update(double dt) {
        animTime += dt;

        double tPeak = 0.4;
        currentN = targetN * (animTime / tPeak) * Math.exp(1 - animTime / tPeak);

        if (animTime >= tPeak && animTime - dt < tPeak) {
            if (onPulsePeak != null) onPulsePeak.run();
        }

        if (animTime >= 2.0) {
            currentN = 0;
            isRunning = false;
            if (onFinish != null) onFinish.run();
        }
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

        double centerX = w / 2;

        double magnetY = 90;
        LinearGradient colGrad = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ced4da")), new Stop(1, Color.web("#6c757d")));

        gc.setFill(colGrad);
        gc.fillRect(centerX - 100, 20, 200, 30);
        gc.fillRect(centerX - 100, 20, 40, magnetY);
        gc.fillRect(centerX + 60, 20, 40, magnetY);

        double visualL0 = l0 * 1.5;
        gc.fillRect(centerX - visualL0/2 - 40, magnetY + 20, 40, 30);
        gc.fillRect(centerX + visualL0/2, magnetY + 20, 40, 30);

        gc.setFill(Color.WHITE);
        gc.fillText("N", centerX - visualL0/2 - 25, magnetY + 40);
        gc.fillText("S", centerX + visualL0/2 + 15, magnetY + 40);

        double coilVisualX = centerX + (x * 12);
        gc.setFill(Color.web("#ff9800"));
        gc.fillRect(coilVisualX - 8, magnetY + 22, 16, 26);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(coilVisualX - 8, magnetY + 22, 16, 26);

        double galvY = h - 30;
        double r = 140;

        gc.setFill(Color.web("#e9ecef"));
        gc.fillArc(centerX - r, galvY - r, r * 2, r * 2, 0, 180, ArcType.CHORD);
        gc.setStroke(Color.web("#adb5bd"));
        gc.setLineWidth(3);
        gc.strokeArc(centerX - r, galvY - r, r * 2, r * 2, 0, 180, ArcType.CHORD);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        for (int i = -150; i <= 150; i += 10) {
            double angle = Math.toRadians(90 - (i * 0.4));
            double x1 = centerX + (r - 10) * Math.cos(angle);
            double y1 = galvY - (r - 10) * Math.sin(angle);
            double x2 = centerX + (r - 20) * Math.cos(angle);
            double y2 = galvY - (r - 20) * Math.sin(angle);
            gc.strokeLine(x1, y1, x2, y2);
        }

        double needleAngle = Math.toRadians(90 - (currentN * 0.4));
        double needleX = centerX + (r - 15) * Math.cos(needleAngle);
        double needleY = galvY - (r - 15) * Math.sin(needleAngle);

        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        gc.strokeLine(centerX, galvY, needleX, needleY);

        gc.setFill(Color.web("#343a40"));
        gc.fillOval(centerX - 10, galvY - 10, 20, 20);
    }
}