package dev.ua._klaidi4_.physics.level1.lab1_3.view;

import dev.ua._klaidi4_.physics.level1.lab1_3.enums.CollisionType;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class CollisionCanvas extends Canvas {

    private CollisionType type = CollisionType.ELASTIC;
    private double length = 205;
    private double gravity = 9.81;
    private double m1 = 0.169;
    private double m2 = 0.169;
    private double r1 = 20;
    private double r2 = 20;
    private double startAngle1 = -Math.toRadians(15);
    private double currentAngle1 = -Math.toRadians(15);
    private double currentAngle2 = 0;
    private double av1 = 0;
    private double av2 = 0;
    private boolean isRunning = false;
    private boolean collisionOccurred = false;
    private AnimationTimer timer;
    private long lastTime = 0;

    private Runnable onCollisionCallback;

    public CollisionCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    public void setPhysicsParameters(CollisionType type, double l, double g, double mass1, double mass2, double angleDeg) {
        this.type = type;
        this.length = l * 500;
        this.gravity = g;
        this.m1 = mass1;
        this.m2 = mass2;

        this.r1 = 15 + mass1 * 30;
        this.r2 = 15 + mass2 * 30;

        this.startAngle1 = -Math.toRadians(angleDeg);
        resetSystem();
    }

    public void setOnCollisionCallback(Runnable callback) {
        this.onCollisionCallback = callback;
    }

    public void resetSystem() {
        this.currentAngle1 = startAngle1;
        this.currentAngle2 = 0;
        this.av1 = 0;
        this.av2 = 0;
        this.isRunning = false;
        this.collisionOccurred = false;
        draw();
    }

    public void startSimulation() {
        resetSystem();
        this.isRunning = true;
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

    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    private void update(double dt) {
        double aa1 = -(gravity / (length / 100.0)) * Math.sin(currentAngle1);
        double aa2 = -(gravity / (length / 100.0)) * Math.sin(currentAngle2);

        av1 += aa1 * dt;
        av2 += aa2 * dt;

        currentAngle1 += av1 * dt;
        currentAngle2 += av2 * dt;

        if (!collisionOccurred && currentAngle1 >= currentAngle2 && av1 > av2) {
            collisionOccurred = true;

            double v1 = av1 * length;
            double v2 = av2 * length;

            double u1, u2;

            if (type == CollisionType.ELASTIC) {
                double eCoef = 0.72;
                u1 = (m1 * v1 + m2 * v2 - m2 * eCoef * (v1 - v2)) / (m1 + m2);
                u2 = (m1 * v1 + m2 * v2 + m1 * eCoef * (v1 - v2)) / (m1 + m2);
            } else {
                u1 = (m1 * v1 + m2 * v2) / (m1 + m2);
                u2 = u1;
            }

            av1 = u1 / length;
            av2 = u2 / length;
            currentAngle1 = currentAngle2;

            if (onCollisionCallback != null) {
                onCollisionCallback.run();
            }
        }

        if (type == CollisionType.INELASTIC && collisionOccurred) {
            double avgAv = (av1 + av2) / 2.0;
            av1 = avgAv;
            av2 = avgAv;
            currentAngle1 = currentAngle2;
        }
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#e9ecef"));
        gc.setLineWidth(1);
        for (int i = 0; i < w; i += 20) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 20) gc.strokeLine(0, i, w, i);

        double originX1 = w / 2 - r1;
        double originX2 = w / 2 + r2;
        double originY = 60;

        gc.setFill(Color.DARKGRAY);
        gc.fillRect(w / 2 - 50, originY - 10, 100, 10);

        double bobX1 = originX1 + length * Math.sin(currentAngle1);
        double bobY1 = originY + length * Math.cos(currentAngle1);

        double bobX2 = originX2 + length * Math.sin(currentAngle2);
        double bobY2 = originY + length * Math.cos(currentAngle2);

        gc.setStroke(Color.web("#343a40"));
        gc.setLineWidth(1.5);
        gc.strokeLine(originX1, originY, bobX1, bobY1);
        gc.strokeLine(originX2, originY, bobX2, bobY2);

        Color color1 = type == CollisionType.ELASTIC ? Color.STEELBLUE : Color.DARKOLIVEGREEN;
        Color color2 = type == CollisionType.ELASTIC ? Color.INDIANRED : Color.DARKOLIVEGREEN;

        drawBob(gc, bobX1, bobY1, r1, color1);
        drawBob(gc, bobX2, bobY2, r2, color2);
    }

    private void drawBob(GraphicsContext gc, double x, double y, double r, Color baseColor) {
        RadialGradient grad = new RadialGradient(0, 0, x - r*0.3, y - r*0.3, r,
                false, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(1, baseColor));
        gc.setFill(grad);
        gc.fillOval(x - r, y - r, r * 2, r * 2);
    }
}