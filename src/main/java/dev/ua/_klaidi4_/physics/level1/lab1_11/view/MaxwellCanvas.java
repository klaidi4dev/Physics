package dev.ua._klaidi4_.physics.level1.lab1_11.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class MaxwellCanvas extends Canvas {

    private double currentY = 0;
    private double maxH = 300;
    private boolean isRunning = false;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double simTime = 0;
    private double exactTime = 2.0;

    private Runnable onFinishCallback;

    public MaxwellCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void setOnFinishCallback(Runnable onFinishCallback) {
        this.onFinishCallback = onFinishCallback;
    }

    public void startSimulation(double calculatedTime) {
        this.exactTime = calculatedTime;
        this.simTime = 0;
        this.currentY = 0;
        this.isRunning = true;

        if (timer != null) timer.stop();
        startAnimation();
    }

    public void stopAnimation() {
        this.isRunning = false;
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

                update(dt);
                draw();
            }
        };
        timer.start();
    }

    private void update(double dt) {
        if (isRunning) {
            simTime += dt;

            double a = (2 * maxH) / (exactTime * exactTime);
            currentY = (a * simTime * simTime) / 2;

            if (simTime >= exactTime || currentY >= maxH) {
                currentY = maxH;
                isRunning = false;
                timer.stop();
                if (onFinishCallback != null) onFinishCallback.run();
            }
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
        double cx = w / 2;
        double startY = 50;
        double r = 40;
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(cx - 60, startY - 10, 120, 10);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(cx - 15, startY, cx - 15, startY + currentY);
        gc.strokeLine(cx + 15, startY, cx + 15, startY + currentY);
        RadialGradient grad = new RadialGradient(0, 0, 0, 0, r, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#e74c3c")), new Stop(1, Color.web("#c0392b")));
        gc.setFill(grad);
        gc.fillOval(cx - r, startY + currentY - r, r*2, r*2);
        gc.setFill(Color.web("#7f8c8d"));
        gc.fillOval(cx - 15, startY + currentY - 15, 30, 30);
        gc.setFill(Color.BLACK);
        gc.fillOval(cx - 3, startY + currentY - 3, 6, 6);
        gc.setStroke(Color.web("#27ae60"));
        gc.setLineDashes(5);
        gc.strokeLine(cx - 50, startY + maxH + r, cx + 50, startY + maxH + r);
        gc.setLineDashes(null);
    }
}