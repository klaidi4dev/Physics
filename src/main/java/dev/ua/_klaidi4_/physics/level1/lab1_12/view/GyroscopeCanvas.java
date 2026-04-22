package dev.ua._klaidi4_.physics.level1.lab1_12.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class GyroscopeCanvas extends Canvas {

    private double currentAngle = 0;
    private boolean isRunning = false;

    private AnimationTimer timer;
    private long lastTime = 0;
    private double simTime = 0;
    private double exactTime = 2.0;
    private int targetRevs = 1;

    private Runnable onFinishCallback;

    public GyroscopeCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void setOnFinishCallback(Runnable onFinishCallback) {
        this.onFinishCallback = onFinishCallback;
    }

    public void startSimulation(double calculatedTime, int revs) {
        this.exactTime = calculatedTime;
        this.targetRevs = revs;
        this.simTime = 0;
        this.currentAngle = 0;
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
            currentAngle = (simTime / exactTime) * (targetRevs * 2 * Math.PI);

            if (simTime >= exactTime) {
                currentAngle = targetRevs * 2 * Math.PI;
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
        double cy = h / 2;
        double rPath = 140;

        gc.setStroke(Color.web("#bdc3c7"));
        gc.setLineWidth(2);
        gc.setLineDashes(5);
        gc.strokeOval(cx - rPath, cy - rPath, rPath * 2, rPath * 2);
        gc.setLineDashes(null);

        gc.save();
        gc.translate(cx, cy);
        gc.rotate(Math.toDegrees(currentAngle));
        gc.setStroke(Color.web("#34495e"));
        gc.setLineWidth(8);
        gc.strokeLine(-40, 0, rPath + 20, 0);
        gc.setFill(Color.web("#2980b9", 0.9));
        gc.fillRect(rPath - 30, -40, 20, 80);
        gc.setStroke(Color.web("#2c3e50"));
        gc.setLineWidth(2);
        gc.strokeRect(rPath - 30, -40, 20, 80);

        RadialGradient weightGrad = new RadialGradient(0, 0, 0, 0, 15,
                false, CycleMethod.NO_CYCLE, new Stop(0, Color.web("#e74c3c")), new Stop(1, Color.web("#c0392b")));
        gc.setFill(weightGrad);
        gc.fillOval(rPath + 5, -15, 30, 30);
        gc.setFill(Color.WHITE);
        gc.fillText("m", rPath + 15, 4);

        gc.restore();
        gc.setFill(Color.web("#7f8c8d"));
        gc.fillOval(cx - 15, cy - 15, 30, 30);
        gc.setFill(Color.BLACK);
        gc.fillOval(cx - 4, cy - 4, 8, 8);
    }
}