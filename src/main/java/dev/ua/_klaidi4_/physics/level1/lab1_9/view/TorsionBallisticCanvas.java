package dev.ua._klaidi4_.physics.level1.lab1_9.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class TorsionBallisticCanvas extends Canvas {

    private double currentAngle = 0;
    private double maxAngle = 0;
    private double bulletX = 0;
    private boolean isShooting = false;
    private boolean isSwinging = false;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double swingTime = 0;

    private Runnable onHitCallback;

    public TorsionBallisticCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void setCallbacks(Runnable onHit) {
        this.onHitCallback = onHit;
    }

    public void startSimulation(double calculatedMaxAngleDeg) {
        this.maxAngle = Math.toRadians(calculatedMaxAngleDeg);
        this.currentAngle = 0;
        this.bulletX = 50;
        this.swingTime = 0;
        this.isShooting = true;
        this.isSwinging = false;

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
        double originX = getWidth() / 2;
        double targetRadius = 120;

        if (isShooting) {
            bulletX += 1500 * dt;
            if (bulletX >= originX - targetRadius - 10) {
                bulletX = originX - targetRadius - 10;
                isShooting = false;
                isSwinging = true;
                if (onHitCallback != null) onHitCallback.run();
            }
        } else if (isSwinging) {
            swingTime += dt;
            double omega = 3.0;
            currentAngle = maxAngle * Math.sin(omega * swingTime);
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
        double originY = h / 2;
        double targetRadius = 120;
        double weightRadius = 90;
        if (isShooting) {
            gc.setFill(Color.web("#e74c3c"));
            gc.fillOval(bulletX, originY - 5, 15, 10);
        }

        gc.save();
        gc.translate(originX, originY);
        gc.rotate(Math.toDegrees(currentAngle));
        gc.setStroke(Color.web("#7f8c8d"));
        gc.setLineWidth(6);
        gc.strokeLine(-targetRadius, 0, targetRadius, 0);
        gc.strokeLine(0, -weightRadius - 20, 0, weightRadius + 20);
        gc.setFill(Color.web("#34495e"));
        gc.fillRect(-targetRadius - 10, -15, 20, 30);
        gc.fillOval(targetRadius - 15, -15, 30, 30);

        if (isSwinging) {
            gc.setFill(Color.web("#e74c3c"));
            gc.fillOval(-targetRadius - 5, -5, 10, 10);
        }

        RadialGradient weightGrad = new RadialGradient(0, 0, 0, 0, 15,
                false, CycleMethod.NO_CYCLE, new Stop(0, Color.web("#f1c40f")), new Stop(1, Color.web("#d35400")));
        gc.setFill(weightGrad);
        gc.fillOval(-15, -weightRadius - 15, 30, 30);
        gc.fillOval(-15, weightRadius - 15, 30, 30);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeOval(-15, -weightRadius - 15, 30, 30);
        gc.strokeOval(-15, weightRadius - 15, 30, 30);
        gc.restore();
        gc.setFill(Color.BLACK);
        gc.fillOval(originX - 5, originY - 5, 10, 10);
        gc.setFill(Color.WHITE);
        gc.fillOval(originX - 2, originY - 2, 4, 4);
    }
}