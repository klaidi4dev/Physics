package dev.ua._klaidi4_.physics.level1.lab1_14.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

public class StokesCanvas extends Canvas {

    private double currentY = 0;
    private double maxH = 300;
    private boolean isRunning = false;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double simTime = 0;
    private double exactTime = 2.0;

    private Runnable onFinishCallback;

    public StokesCanvas(double width, double height) {
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

            double v = maxH / exactTime;
            currentY = v * simTime;

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
        double startY = 60;
        double flaskWidth = 100;
        double r = 8;

        LinearGradient fluidGrad = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#f39c12", 0.3)), new Stop(0.5, Color.web("#f1c40f", 0.5)), new Stop(1, Color.web("#f39c12", 0.3)));
        gc.setFill(fluidGrad);
        gc.fillRect(cx - flaskWidth/2, startY - 20, flaskWidth, maxH + 60);
        gc.setStroke(Color.web("#7f8c8d"));
        gc.setLineWidth(4);
        gc.strokeLine(cx - flaskWidth/2, startY - 30, cx - flaskWidth/2, startY + maxH + 40);
        gc.strokeLine(cx + flaskWidth/2, startY - 30, cx + flaskWidth/2, startY + maxH + 40);
        gc.strokeLine(cx - flaskWidth/2 - 2, startY + maxH + 40, cx + flaskWidth/2 + 2, startY + maxH + 40);
        gc.setStroke(Color.web("#c0392b"));
        gc.setLineWidth(2);
        gc.strokeLine(cx - flaskWidth/2 - 10, startY, cx - flaskWidth/2 + 10, startY);
        gc.strokeLine(cx - flaskWidth/2 - 10, startY + maxH, cx - flaskWidth/2 + 10, startY + maxH);
        gc.setFill(Color.web("#c0392b"));
        gc.fillText("Початок відліку", cx - flaskWidth/2 - 100, startY + 5);
        gc.fillText("Кінець відліку", cx - flaskWidth/2 - 100, startY + maxH + 5);
        gc.setFill(Color.web("#2c3e50"));
        gc.fillOval(cx - r, startY + currentY - r, r*2, r*2);
        gc.setFill(Color.WHITE);
        gc.fillOval(cx - r + 3, startY + currentY - r + 3, 4, 4);
    }
}