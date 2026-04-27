package dev.ua._klaidi4_.physics.level4.lab4_2.view;

import dev.ua._klaidi4_.physics.level4.lab4_2.enums.MaterialType;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class MathPendulumCanvas extends Canvas {

    private double length = 0.5;
    private double gravity = 9.81;
    private MaterialType material = MaterialType.STEEL;
    private double startAngle = Math.PI / 18;
    private double currentAngle = startAngle;
    private double angularVelocity = 0;
    private boolean isRunning = false;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double elapsedTime = 0;
    private int halfSwings = 0;
    private double lastVelocity = 0;

    private Runnable onOscillationCompleted;

    public MathPendulumCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    public void setPhysicsParameters(MaterialType material, double l, double angleDeg) {
        this.material = material;
        this.length = l;
        this.startAngle = Math.toRadians(angleDeg);
        resetSystem();
    }

    public void setOnOscillationCompleted(Runnable callback) {
        this.onOscillationCompleted = callback;
    }

    public void resetSystem() {
        this.currentAngle = startAngle;
        this.angularVelocity = 0;
        this.halfSwings = 0;
        this.lastVelocity = 0;
        this.elapsedTime = 0;
        this.isRunning = false;
        draw();
    }

    public void startSimulation() {
        resetSystem();
        this.isRunning = true;
    }

    public void stopSimulation() {
        this.isRunning = false;
    }

    public double getElapsedTime() { return elapsedTime; }

    public int getFullOscillations() {
        return halfSwings / 2;
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
        double angularAcceleration = -(gravity / length) * Math.sin(currentAngle);
        angularVelocity += angularAcceleration * dt;
        currentAngle += angularVelocity * dt;
        elapsedTime += dt;

        if (lastVelocity * angularVelocity < 0) {
            halfSwings++;
            if (halfSwings % 2 == 0 && onOscillationCompleted != null) {
                onOscillationCompleted.run();
            }
        }
        lastVelocity = angularVelocity;
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

        double originX = w / 2;
        double originY = 50;

        gc.setFill(Color.DARKGRAY);
        gc.fillRect(originX - 40, originY - 10, 80, 10);
        gc.setStroke(Color.GRAY);
        gc.setLineDashes(5, 5);
        gc.strokeLine(originX, originY, originX, h - 20);
        gc.setLineDashes(null);

        double visualLength = length * 500;
        double bobX = originX + visualLength * Math.sin(currentAngle);
        double bobY = originY + visualLength * Math.cos(currentAngle);

        gc.setStroke(Color.web("#343a40"));
        gc.setLineWidth(1.5);
        gc.strokeLine(originX, originY, bobX, bobY);

        double r = 16;
        if (material == MaterialType.WOOD) r = 22;
        else if (material == MaterialType.ALUMINUM) r = 18;

        RadialGradient grad = new RadialGradient(0, 0, bobX - r*0.3, bobY - r*0.3, r,
                false, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(1, Color.web(material.getColorHex())));
        gc.setFill(grad);
        gc.fillOval(bobX - r, bobY - r, r * 2, r * 2);

        if (isRunning) {
            gc.setFill(Color.BLACK);
            gc.fillText(String.format("Поточний кут: %.1f°", Math.toDegrees(currentAngle)), 10, 20);
        }
    }
}