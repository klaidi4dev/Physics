package dev.ua._klaidi4_.physics.level1.lab1_5.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class ChronometerCanvas extends Canvas {

    private double length = 250;
    private double startAngle = Math.PI / 4;
    private double currentAngle = Math.PI / 4;
    private boolean isSwinging = false;
    private boolean isHit = false;
    private double sparkTimer = 0;
    private AnimationTimer timer;
    private long lastTime = 0;

    private Runnable onHitCallback;

    public ChronometerCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void setCallbacks(Runnable onHit) {
        this.onHitCallback = onHit;
    }

    public void startSimulation(double angleDeg) {
        this.startAngle = Math.toRadians(angleDeg);
        this.currentAngle = startAngle;
        this.isSwinging = true;
        this.isHit = false;
        this.sparkTimer = 0;

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
        if (isSwinging) {
            double omega = 3.5;
            currentAngle -= omega * dt * (currentAngle + 0.1);

            if (currentAngle <= 0) {
                currentAngle = 0;
                isSwinging = false;
                isHit = true;
                sparkTimer = 0.2; // Іскра світиться 0.2 секунди
                if (onHitCallback != null) onHitCallback.run();
                timer.stop();
            }
        }

        if (isHit && sparkTimer > 0) {
            sparkTimer -= dt;
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
        double originY = 80;
        double radius = 20;

        gc.setFill(Color.web("#2c3e50"));
        gc.fillRoundRect(20, 20, 120, 60, 10, 10);
        gc.setFill(Color.BLACK);
        gc.fillRoundRect(30, 35, 100, 30, 5, 5);
        if (isHit) {
            gc.setFill(Color.web("#00ff00"));
            gc.setFont(javafx.scene.text.Font.font("Monospaced", 18));
            gc.fillText(String.format("τ %.1f", 125.4), 35, 55);
        }

        gc.setStroke(Color.web("#d35400"));
        gc.setLineWidth(2);
        gc.strokeLine(140, 50, originX - 10, originY);
        gc.strokeLine(140, 60, originX + 10, originY);
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(originX - 40, originY - 10, 80, 10);

        double rightX = originX + radius;
        double rightY = originY + length;
        gc.setStroke(Color.BLACK);
        gc.strokeLine(originX + 10, originY, rightX, rightY);
        drawSteelBall(gc, rightX, rightY, radius);
        double leftOriginX = originX - 10;
        double leftX = leftOriginX - length * Math.sin(currentAngle) - radius + 10;
        double leftY = originY + length * Math.cos(currentAngle);

        gc.setStroke(Color.BLACK);
        gc.strokeLine(leftOriginX, originY, leftX, leftY);
        drawSteelBall(gc, leftX, leftY, radius);

        if (isHit && sparkTimer > 0) {
            gc.setFill(Color.web("#f1c40f", 0.8));
            gc.fillOval(originX - 15, rightY - 15, 30, 30);
            gc.setFill(Color.web("#ffffff", 0.9));
            gc.fillOval(originX - 5, rightY - 5, 10, 10);
        }
    }

    private void drawSteelBall(GraphicsContext gc, double x, double y, double r) {
        RadialGradient ballGrad = new RadialGradient(0, 0, x - r*0.3, y - r*0.3, r,
                false, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(0.4, Color.web("#95a5a6")), new Stop(1, Color.web("#2c3e50")));
        gc.setFill(ballGrad);
        gc.fillOval(x - r, y - r, r * 2, r * 2);
    }
}