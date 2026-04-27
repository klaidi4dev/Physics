package dev.ua._klaidi4_.physics.level1.lab1_4.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

public class BallisticCanvas extends Canvas {

    private double length = 300;
    private double currentAngle = 0;
    private double maxAngle = 0;
    private double bulletX = 50;
    private double bulletSpeed = 2000;
    private boolean isShooting = false;
    private boolean isSwinging = false;
    private boolean isFinished = false;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double swingTime = 0;
    private Runnable onHitCallback;
    private Runnable onFinishCallback;

    public BallisticCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void setCallbacks(Runnable onHit, Runnable onFinish) {
        this.onHitCallback = onHit;
        this.onFinishCallback = onFinish;
    }

    public void startSimulation(double calculatedMaxAngle) {
        this.maxAngle = calculatedMaxAngle;
        this.currentAngle = 0;
        this.bulletX = 20;
        this.swingTime = 0;
        this.isShooting = true;
        this.isSwinging = false;
        this.isFinished = false;

        if (timer != null) timer.stop();
        startAnimation();
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

                update(dt);
                draw();
            }
        };
        timer.start();
    }

    private void update(double dt) {
        double w = getWidth();
        double originX = w / 2;

        if (isShooting) {
            bulletX += bulletSpeed * dt;
            if (bulletX >= originX - 15) {
                bulletX = originX - 15;
                isShooting = false;
                isSwinging = true;
                if (onHitCallback != null) onHitCallback.run();
            }
        } else if (isSwinging) {
            swingTime += dt;
            double omega = 3.0;
            currentAngle = maxAngle * Math.sin(omega * swingTime);

            if (omega * swingTime >= Math.PI / 2) {
                currentAngle = maxAngle;
                isSwinging = false;
                isFinished = true;
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

        double originX = w / 2;
        double originY = 40;

        gc.setFill(Color.DARKGRAY);
        gc.fillRect(originX - 30, originY - 10, 60, 10);

        gc.setFill(Color.web("#34495e"));
        gc.fillRect(10, originY + length - 15, 60, 30);
        gc.setFill(Color.web("#7f8c8d"));
        gc.fillRect(70, originY + length - 5, 30, 10);

        if (isShooting) {
            gc.setFill(Color.web("#e74c3c"));
            gc.fillOval(bulletX, originY + length - 5, 15, 10);
        }

        double pendX = originX + length * Math.sin(currentAngle);
        double pendY = originY + length * Math.cos(currentAngle);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(originX, originY, pendX, pendY);

        gc.save();
        gc.translate(pendX, pendY);
        gc.rotate(Math.toDegrees(-currentAngle));

        LinearGradient blockGrad = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#bdc3c7")), new Stop(1, Color.web("#7f8c8d")));
        gc.setFill(blockGrad);
        gc.fillRect(-25, -20, 50, 40);
        gc.setStroke(Color.web("#2c3e50"));
        gc.setLineWidth(2);
        gc.strokeRect(-25, -20, 50, 40);

        if (isSwinging || isFinished) {
            gc.setFill(Color.web("#e74c3c"));
            gc.fillOval(-10, -5, 15, 10);
        }

        gc.restore();

        if (isFinished) {
            gc.setStroke(Color.web("#3498db"));
            gc.setLineWidth(1.5);
            gc.setLineDashes(5);
            gc.strokeLine(originX, originY + length + 30, pendX, originY + length + 30);
            gc.strokeLine(originX, originY + length, originX, originY + length + 40);
            gc.strokeLine(pendX, pendY, pendX, originY + length + 40);
            gc.setLineDashes(null);

            gc.setFill(Color.web("#2980b9"));
            gc.fillText("S", originX + (pendX - originX) / 2 - 5, originY + length + 25);
        }
    }
}