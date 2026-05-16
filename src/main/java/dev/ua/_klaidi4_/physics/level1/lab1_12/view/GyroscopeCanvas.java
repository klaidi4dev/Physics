/*
 * Лабораторна робота № 1-12 "Гіроскоп".
 * Клас: GyroscopeCanvas.
 * Призначення: відповідає за графічне відображення симуляції, анімацію
 * фізичного процесу та відмальовку компонентів установки.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level1.lab1_12.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GyroscopeCanvas extends Canvas {

    private double currentAngleDeg = 0;
    private boolean isRunning = false;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double simTime = 0;
    private double exactTime = 2.0;
    private double targetAngleDeg = 30.0;
    private double speedMult = 1.0;

    private Runnable onFinishCallback;

    /*
     * Лабораторна робота № 1-12 "Гіроскоп".
     * Функція: GyroscopeCanvas.
     * Призначення: Конструктор класу, ініціалізує початкові параметри та стан об'єкта.
     */
    public GyroscopeCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    /*
     * Лабораторна робота № 1-12 "Гіроскоп".
     * Функція: setOnFinishCallback.
     * Призначення: Встановлює фізичні параметри або обробники подій для візуалізації.
     */
    public void setOnFinishCallback(Runnable onFinishCallback) {
        this.onFinishCallback = onFinishCallback;
    }

    /*
     * Лабораторна робота № 1-12 "Гіроскоп".
     * Функція: startSimulation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    public void startSimulation(double calculatedTime, double angleDeg, double speedMultiplier) {
        this.exactTime = calculatedTime;
        this.targetAngleDeg = angleDeg;
        this.speedMult = speedMultiplier;
        this.simTime = 0;
        this.currentAngleDeg = 0;
        this.isRunning = true;

        if (timer != null) timer.stop();
        startAnimation();
    }

    /*
     * Лабораторна робота № 1-12 "Гіроскоп".
     * Функція: stopAnimation.
     * Призначення: Зупиняє цикл анімації.
     */
    public void stopAnimation() {
        this.isRunning = false;
        if (timer != null) timer.stop();
    }

    /*
     * Лабораторна робота № 1-12 "Гіроскоп".
     * Функція: startAnimation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    private void startAnimation() {
        timer = new AnimationTimer() {
    /*
     * Лабораторна робота № 1-12 "Гіроскоп".
     * Функція: handle.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
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

    /*
     * Лабораторна робота № 1-12 "Гіроскоп".
     * Функція: update.
     * Призначення: Оновлює графічні елементи та анімацію на основі нових даних.
     */
    private void update(double dt) {
        if (isRunning) {
            simTime += dt * speedMult;
            currentAngleDeg = (simTime / exactTime) * targetAngleDeg;

            if (simTime >= exactTime) {
                currentAngleDeg = targetAngleDeg;
                isRunning = false;
                timer.stop();
                if (onFinishCallback != null) onFinishCallback.run();
            }
        }
    }

    /*
     * Лабораторна робота № 1-12 "Гіроскоп".
     * Функція: draw.
     * Призначення: Відмальовує графічні компоненти та стан симуляції на полотні.
     */
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
        gc.rotate(currentAngleDeg);
        gc.setStroke(Color.web("#34495e"));
        gc.setLineWidth(8);
        gc.strokeLine(-80, 0, rPath + 40, 0);
        gc.setFill(Color.web("#2980b9", 0.9));
        gc.fillRect(-20, -50, 40, 100);
        gc.setStroke(Color.web("#2c3e50"));
        gc.setLineWidth(2);
        gc.strokeRect(-20, -50, 40, 100);
        gc.setFill(Color.web("#e74c3c"));
        gc.fillOval(rPath - 15, -15, 30, 30);
        gc.setFill(Color.WHITE);
        gc.fillText("m", rPath - 5, 5);
        gc.restore();
        gc.setFill(Color.web("#7f8c8d"));
        gc.fillOval(cx - 15, cy - 15, 30, 30);
        gc.setFill(Color.BLACK);
        gc.fillOval(cx - 4, cy - 4, 8, 8);
    }
}