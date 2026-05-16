/*
 * Лабораторна робота № 1-6 "Моменти інерції тіл".
 * Клас: TrifilarCanvas.
 * Призначення: відповідає за графічне відображення симуляції, анімацію
 * фізичного процесу та відмальовку компонентів установки.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level1.lab1_6.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class TrifilarCanvas extends Canvas {

    private int configIndex = 0;
    private double currentAngle = 0;
    private double maxAngle = Math.PI / 3;
    private double platformRadius = 120;
    private double diskRadius = 40;
    private double displacement = 60;
    private boolean isSwinging = false;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double swingTime = 0;
    private double exactPeriod = 2.0;
    private double simSpeed = 1.0;

    /*
     * Лабораторна робота № 1-6 "Моменти інерції тіл".
     * Функція: TrifilarCanvas.
     * Призначення: Конструктор класу, ініціалізує початкові параметри та стан об'єкта.
     */
    public TrifilarCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    /*
     * Лабораторна робота № 1-6 "Моменти інерції тіл".
     * Функція: setParameters.
     * Призначення: Встановлює фізичні параметри або обробники подій для візуалізації.
     */
    public void setParameters(int configIndex, double rPlatform, double rDisk, double a) {
        this.configIndex = configIndex;
        this.platformRadius = rPlatform * 750;
        this.diskRadius = rDisk * 750;
        this.displacement = a * 750;
        this.currentAngle = 0;
        draw();
    }

    /*
     * Лабораторна робота № 1-6 "Моменти інерції тіл".
     * Функція: startSimulation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    public void startSimulation(double period, double speedMultiplier) {
        this.exactPeriod = period;
        this.simSpeed = speedMultiplier;
        this.swingTime = 0;
        this.isSwinging = true;

        if (timer != null) timer.stop();
        startAnimation();
    }

    /*
     * Лабораторна робота № 1-6 "Моменти інерції тіл".
     * Функція: stopAnimation.
     * Призначення: Зупиняє цикл анімації.
     */
    public void stopAnimation() {
        this.isSwinging = false;
        if (timer != null) timer.stop();
    }

    /*
     * Лабораторна робота № 1-6 "Моменти інерції тіл".
     * Функція: startAnimation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    private void startAnimation() {
        timer = new AnimationTimer() {
    /*
     * Лабораторна робота № 1-6 "Моменти інерції тіл".
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
     * Лабораторна робота № 1-6 "Моменти інерції тіл".
     * Функція: update.
     * Призначення: Оновлює графічні елементи та анімацію на основі нових даних.
     */
    private void update(double dt) {
        if (isSwinging) {
            swingTime += dt * simSpeed;
            currentAngle = maxAngle * Math.cos((2 * Math.PI / exactPeriod) * swingTime);
        }
    }

    /*
     * Лабораторна робота № 1-6 "Моменти інерції тіл".
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

        double originX = w / 2;
        double originY = h / 2;

        gc.save();
        gc.translate(originX, originY);
        gc.rotate(Math.toDegrees(currentAngle));

        RadialGradient platGrad = new RadialGradient(0, 0, 0, 0, platformRadius,
                false, CycleMethod.NO_CYCLE, new Stop(0, Color.web("#ecf0f1")), new Stop(1, Color.web("#bdc3c7")));
        gc.setFill(platGrad);
        gc.fillOval(-platformRadius, -platformRadius, platformRadius * 2, platformRadius * 2);
        gc.setStroke(Color.web("#7f8c8d"));
        gc.setLineWidth(4);
        gc.strokeOval(-platformRadius, -platformRadius, platformRadius * 2, platformRadius * 2);

        gc.setFill(Color.web("#c0392b"));
        for (int i = 0; i < 3; i++) {
            double angle = i * (2 * Math.PI / 3);
            double px = platformRadius * 0.9 * Math.cos(angle);
            double py = platformRadius * 0.9 * Math.sin(angle);
            gc.fillOval(px - 5, py - 5, 10, 10);
        }

        RadialGradient diskGrad = new RadialGradient(0, 0, -diskRadius*0.2, -diskRadius*0.2, diskRadius,
                false, CycleMethod.NO_CYCLE, new Stop(0, Color.web("#f1c40f")), new Stop(1, Color.web("#d35400")));

        if (configIndex == 1) {
            gc.setFill(diskGrad);
            gc.fillOval(-diskRadius, -diskRadius, diskRadius * 2, diskRadius * 2);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeOval(-diskRadius, -diskRadius, diskRadius * 2, diskRadius * 2);
            gc.setFill(Color.BLACK);
            gc.fillOval(-3, -3, 6, 6);
        } else if (configIndex == 2) {
            gc.setFill(diskGrad);
            gc.fillOval(-displacement - diskRadius, -diskRadius, diskRadius * 2, diskRadius * 2);
            gc.strokeOval(-displacement - diskRadius, -diskRadius, diskRadius * 2, diskRadius * 2);
            gc.setFill(Color.BLACK);
            gc.fillOval(-displacement - 3, -3, 6, 6);

            gc.setFill(diskGrad);
            gc.fillOval(displacement - diskRadius, -diskRadius, diskRadius * 2, diskRadius * 2);
            gc.strokeOval(displacement - diskRadius, -diskRadius, diskRadius * 2, diskRadius * 2);
            gc.setFill(Color.BLACK);
            gc.fillOval(displacement - 3, -3, 6, 6);
        }

        gc.restore();

        gc.setFill(Color.web("#2980b9", 0.5));
        gc.fillOval(originX - 4, originY - 4, 8, 8);
    }
}