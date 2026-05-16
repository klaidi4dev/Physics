/*
 * Лабораторна робота № 1-13 "Тертя кочення".
 * Клас: InclinedPendulumCanvas.
 * Призначення: відповідає за графічне відображення симуляції, анімацію
 * фізичного процесу та відмальовку компонентів установки.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level1.lab1_13.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class InclinedPendulumCanvas extends Canvas {

    private double currentAngleRad = 0;
    private double maxAngleRad = 0;
    private double dampingFactor = 0;
    private boolean isSwinging = false;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double simTime = 0;
    private double exactPeriod = 1.5;
    private int targetOscillations = 10;

    private Runnable onFinishCallback;

    /*
     * Лабораторна робота № 1-13 "Тертя кочення".
     * Функція: InclinedPendulumCanvas.
     * Призначення: Конструктор класу, ініціалізує початкові параметри та стан об'єкта.
     */
    public InclinedPendulumCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    /*
     * Лабораторна робота № 1-13 "Тертя кочення".
     * Функція: setOnFinishCallback.
     * Призначення: Встановлює фізичні параметри або обробники подій для візуалізації.
     */
    public void setOnFinishCallback(Runnable cb) {
        this.onFinishCallback = cb;
    }

    /*
     * Лабораторна робота № 1-13 "Тертя кочення".
     * Функція: startSimulation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    public void startSimulation(double startAngleDeg, double endAngleDeg, int n) {
        this.maxAngleRad = Math.toRadians(startAngleDeg);
        this.currentAngleRad = this.maxAngleRad;
        this.targetOscillations = n;
        this.simTime = 0;

        double endAngleRad = Math.toRadians(endAngleDeg);
        double totalTime = n * exactPeriod;
        this.dampingFactor = (this.maxAngleRad - endAngleRad) / totalTime;

        this.isSwinging = true;

        if (timer != null) timer.stop();
        startAnimation();
    }

    /*
     * Лабораторна робота № 1-13 "Тертя кочення".
     * Функція: stopAnimation.
     * Призначення: Зупиняє цикл анімації.
     */
    public void stopAnimation() {
        this.isSwinging = false;
        if (timer != null) timer.stop();
    }

    /*
     * Лабораторна робота № 1-13 "Тертя кочення".
     * Функція: startAnimation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    private void startAnimation() {
        timer = new AnimationTimer() {
    /*
     * Лабораторна робота № 1-13 "Тертя кочення".
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
     * Лабораторна робота № 1-13 "Тертя кочення".
     * Функція: update.
     * Призначення: Оновлює графічні елементи та анімацію на основі нових даних.
     */
    private void update(double dt) {
        if (isSwinging) {
            simTime += dt;

            double currentAmplitude = maxAngleRad - dampingFactor * simTime;
            if (currentAmplitude < 0) currentAmplitude = 0;

            currentAngleRad = currentAmplitude * Math.cos((2 * Math.PI / exactPeriod) * simTime);

            if (simTime >= targetOscillations * exactPeriod) {
                isSwinging = false;
                timer.stop();
                if (onFinishCallback != null) onFinishCallback.run();
            }
        }
    }

    /*
     * Лабораторна робота № 1-13 "Тертя кочення".
     * Функція: draw.
     * Призначення: Відмальовує графічні компоненти та стан симуляції на полотні.
     */
    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#ecf0f1"));
        gc.fillRect(0, 0, w, h);
        gc.setStroke(Color.web("#bdc3c7"));
        gc.setLineWidth(1);
        for (int i = 0; i < w; i += 40) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 40) gc.strokeLine(0, i, w, i);

        double originX = w / 2;
        double originY = 20;
        double pendulumLength = 300;
        double rPx = 20;

        if (maxAngleRad != 0) {
            gc.setStroke(Color.web("#7f8c8d"));
            gc.setLineWidth(1.5);
            gc.setLineDashes(5);
            double startLineX = originX + pendulumLength * Math.sin(maxAngleRad);
            double startLineY = originY + pendulumLength * Math.cos(maxAngleRad);
            gc.strokeLine(originX, originY, startLineX, startLineY);
            gc.setLineDashes(null);
            gc.setFill(Color.web("#7f8c8d"));
            gc.fillText("Старт", startLineX + 10, startLineY - 10);
        }

        double ballX = originX + pendulumLength * Math.sin(currentAngleRad);
        double ballY = originY + pendulumLength * Math.cos(currentAngleRad);

        gc.setFill(Color.web("#2c3e50"));
        gc.fillOval(originX - 5, originY - 5, 10, 10);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.5);
        gc.strokeLine(originX, originY, ballX, ballY);
        RadialGradient grad = new RadialGradient(0, 0, ballX - rPx/3, ballY - rPx/3, rPx,
                false, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(0.4, Color.web("#95a5a6")), new Stop(1, Color.web("#34495e")));
        gc.setFill(grad);
        gc.fillOval(ballX - rPx, ballY - rPx, rPx * 2, rPx * 2);
    }
}