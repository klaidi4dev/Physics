/*
 * Лабораторна робота № 1-1 "Машина Атвуда".
 * Клас: AtwoodCanvas.
 * Призначення: виконує графічну візуалізацію машини Атвуда,
 * анімацію руху вантажів та вимірювання часу руху.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level1.lab1_1.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;

public class AtwoodCanvas extends Canvas {

    private double s1 = 0.2;
    private double s2 = 0.25;
    private double bigM = 0.0628;
    private double smallM = 0.012;
    private double gravity = 9.81;
    private double currentY = 0;
    private double currentV = 0;
    private double measuredTime = 0;
    private boolean isRunning = false;
    private boolean isAccelerating = true;
    private boolean isFinished = false;
    private AnimationTimer timer;
    private long lastTime = 0;
    private Runnable onStage2Start;
    private Runnable onFinish;
    private final double SCALE = 600;

    public AtwoodCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    public void setParameters(double bigM, double smallM, double s1, double s2, double gravity) {
        this.bigM = bigM;
        this.smallM = smallM;
        this.s1 = s1;
        this.s2 = s2;
        this.gravity = gravity;
        resetSystem();
    }

    public void setCallbacks(Runnable onStage2Start, Runnable onFinish) {
        this.onStage2Start = onStage2Start;
        this.onFinish = onFinish;
    }

    public void resetSystem() {
        this.currentY = 0;
        this.currentV = 0;
        this.measuredTime = 0;
        this.isRunning = false;
        this.isAccelerating = true;
        this.isFinished = false;
        draw();
    }

    public void startSimulation() {
        resetSystem();
        this.isRunning = true;
    }

    public double getMeasuredTime() {
        return measuredTime;
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

                if (isRunning && !isFinished) {
                    update(dt);
                }
                draw();
            }
        };
        timer.start();
    }

    private void update(double dt) {
        if (isAccelerating) {
            double a = gravity * smallM / (2 * bigM + smallM);
            currentV += a * dt;
            currentY += currentV * dt;

            if (currentY >= s1) {
                currentY = s1;
                isAccelerating = false;
                if (onStage2Start != null) onStage2Start.run();
            }
        } else {
            currentY += currentV * dt;
            measuredTime += dt;

            if (currentY >= s1 + s2) {
                currentY = s1 + s2;
                isFinished = true;
                isRunning = false;
                if (onFinish != null) onFinish.run();
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
        double pulleyY = 60;
        double pulleyR = 25;

        LinearGradient colGrad = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ced4da")), new Stop(1, Color.web("#6c757d")));
        gc.setFill(colGrad);
        gc.fillRect(originX - 10, pulleyY, 20, h - pulleyY);
        gc.fillRect(originX - 50, h - 20, 100, 20);

        gc.setFill(Color.web("#343a40"));
        gc.fillOval(originX - pulleyR, pulleyY - pulleyR, pulleyR * 2, pulleyR * 2);
        gc.setFill(Color.web("#adb5bd"));
        gc.fillOval(originX - pulleyR * 0.8, pulleyY - pulleyR * 0.8, pulleyR * 1.6, pulleyR * 1.6);
        gc.setFill(Color.WHITE);
        gc.fillOval(originX - 3, pulleyY - 3, 6, 6);

        double leftX = originX - pulleyR;
        double rightX = originX + pulleyR;
        double startRightY = pulleyY + 50;
        double startLeftY = pulleyY + 50 + (s1 + s2) * SCALE;
        double rightY = startRightY + currentY * SCALE;
        double leftY = startLeftY - currentY * SCALE;

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.5);
        gc.strokeLine(leftX, pulleyY, leftX, leftY);
        gc.strokeLine(rightX, pulleyY, rightX, rightY);

        double boxW = 30;
        double boxH = 40;
        gc.setFill(Color.STEELBLUE);
        gc.fillRect(leftX - boxW / 2, leftY, boxW, boxH);
        gc.fillRect(rightX - boxW / 2, rightY, boxW, boxH);
        gc.setStroke(Color.web("#0a2540"));
        gc.strokeRect(leftX - boxW / 2, leftY, boxW, boxH);
        gc.strokeRect(rightX - boxW / 2, rightY, boxW, boxH);

        double ringY = startRightY + s1 * SCALE;
        gc.setStroke(Color.RED);
        gc.setLineWidth(4);
        gc.strokeLine(rightX - 30, ringY, rightX - 18, ringY);
        gc.strokeLine(rightX + 18, ringY, rightX + 30, ringY);

        double stopY = startRightY + (s1 + s2) * SCALE + boxH;
        gc.setStroke(Color.web("#2e7d32"));
        gc.setLineWidth(4);
        gc.strokeLine(rightX - 30, stopY, rightX + 30, stopY);

        double smallBoxW = 20;
        double smallBoxH = 10;
        double smallBoxY = isAccelerating ? rightY - smallBoxH : ringY - smallBoxH;
        gc.setFill(Color.CRIMSON);
        gc.fillRect(rightX - smallBoxW / 2, smallBoxY, smallBoxW, smallBoxH);
    }
}