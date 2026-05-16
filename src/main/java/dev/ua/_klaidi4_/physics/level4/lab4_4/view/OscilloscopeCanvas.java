/*
 * Лабораторна робота № 4-4 "Згасаючі коливання".
 * Клас: OscilloscopeCanvas.
 * Призначення: відповідає за графічне відображення симуляції, анімацію
 * фізичного процесу та відмальовку компонентів установки.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level4.lab4_4.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class OscilloscopeCanvas extends Canvas {

    private double l = 0.05;
    private double c = 5e-7;
    private double rTotal = 10.0;
    private double timeScale = 0.001;

    private boolean isRunning = false;
    private double currentTime = 0;
    private AnimationTimer timer;
    private long lastTime = 0;
    private final double U0 = 100.0;

    /*
     * Лабораторна робота № 4-4 "Згасаючі коливання".
     * Функція: OscilloscopeCanvas.
     * Призначення: Конструктор класу, ініціалізує початкові параметри та стан об'єкта.
     */
    public OscilloscopeCanvas(double width, double height) {
        super(width, height);
        drawGrid();
    }

    /*
     * Лабораторна робота № 4-4 "Згасаючі коливання".
     * Функція: setSetupParameters.
     * Призначення: Встановлює фізичні параметри або обробники подій для візуалізації.
     */
    public void setSetupParameters(double l, double c, double rTotal, double timeScale) {
        this.l = l;
        this.c = c;
        this.rTotal = rTotal;
        this.timeScale = timeScale;
        if (!isRunning) drawGrid();
    }

    /*
     * Лабораторна робота № 4-4 "Згасаючі коливання".
     * Функція: startSimulation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    public void startSimulation() {
        this.currentTime = 0;
        this.isRunning = true;
        if (timer != null) timer.stop();

        drawGrid();

        timer = new AnimationTimer() {
    /*
     * Лабораторна робота № 4-4 "Згасаючі коливання".
     * Функція: handle.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                currentTime += dt * 0.005;

                drawTrace();

                if (currentTime > timeScale * 10) {
                    isRunning = false;
                    this.stop();
                    lastTime = 0;
                }
            }
        };
        timer.start();
    }

    /*
     * Лабораторна робота № 4-4 "Згасаючі коливання".
     * Функція: stopAnimation.
     * Призначення: Зупиняє цикл анімації.
     */
    public void stopAnimation() {
        if (timer != null) timer.stop();
        isRunning = false;
    }

    /*
     * Лабораторна робота № 4-4 "Згасаючі коливання".
     * Функція: drawGrid.
     * Призначення: Відмальовує графічні компоненти та стан симуляції на полотні.
     */
    private void drawGrid() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#0d1117"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#1e293b"));
        gc.setLineWidth(1.0);

        double divWidth = w / 10;
        double divHeight = h / 8;

        for (int i = 0; i <= 10; i++) gc.strokeLine(i * divWidth, 0, i * divWidth, h);
        for (int i = 0; i <= 8; i++) gc.strokeLine(0, i * divHeight, w, i * divHeight);

        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(2.0);
        gc.strokeLine(0, h / 2, w, h / 2);
        gc.strokeLine(w / 2, 0, w / 2, h);
    }

    /*
     * Лабораторна робота № 4-4 "Згасаючі коливання".
     * Функція: drawTrace.
     * Призначення: Відмальовує графічні компоненти та стан симуляції на полотні.
     */
    private void drawTrace() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();
        double divWidth = w / 10;
        double divHeight = h / 8;
        double centerY = h / 2;

        drawGrid();

        gc.setStroke(Color.web("#A155FF"));
        gc.setLineWidth(2.5);
        gc.beginPath();
        gc.moveTo(0, centerY - (U0 / 25.0) * divHeight);

        double beta = rTotal / (2 * l);
        double omega0Sq = 1.0 / (l * c);
        boolean isOscillating = omega0Sq > (beta * beta);

        double omega = isOscillating ? Math.sqrt(omega0Sq - beta * beta) : 0;
        double lambda1 = !isOscillating ? beta - Math.sqrt(beta * beta - omega0Sq) : 0;
        double lambda2 = !isOscillating ? beta + Math.sqrt(beta * beta - omega0Sq) : 0;

        int steps = 500;
        double tMax = Math.min(currentTime, timeScale * 10);

        for (int i = 0; i <= steps; i++) {
            double t = (i / (double) steps) * tMax;
            double u;

            if (isOscillating) {
                u = U0 * Math.exp(-beta * t) * Math.cos(omega * t);
            } else {
                if (lambda1 == lambda2) {
                    u = U0 * Math.exp(-beta * t) * (1 + beta * t);
                } else {
                    double c1 = U0 * lambda2 / (lambda2 - lambda1);
                    double c2 = -U0 * lambda1 / (lambda2 - lambda1);
                    u = c1 * Math.exp(-lambda1 * t) + c2 * Math.exp(-lambda2 * t);
                }
            }

            double xPx = (t / timeScale) * divWidth;
            double yPx = centerY - (u / 25.0) * divHeight;
            gc.lineTo(xPx, yPx);
        }
        gc.stroke();
    }
}