/*
 * Лабораторна робота № 7-3 "Метод Стокса".
 * Клас: StokesCanvas.
 * Призначення: відповідає за графічне відображення симуляції, анімацію
 * фізичного процесу та відмальовку компонентів установки.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level7.lab7_3.view;

import dev.ua._klaidi4_.physics.level7.lab7_3.enums.LiquidType;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class StokesCanvas extends Canvas {

    private LiquidType liquid = LiquidType.GLYCERIN;
    private double ballRadius = 0.002;
    private double ballDensity = 7800;
    private double distance = 0.3;
    private double positionY = 0;
    private double velocity = 0;
    private double upperMark = 0.1;
    private boolean isRunning = false;
    private boolean timerStarted = false;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double elapsedTime = 0;
    private Runnable onMeasurementCompleted;

    /*
     * Лабораторна робота № 7-3 "Метод Стокса".
     * Функція: StokesCanvas.
     * Призначення: Конструктор класу, ініціалізує початкові параметри та стан об'єкта.
     */
    public StokesCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    /*
     * Лабораторна робота № 7-3 "Метод Стокса".
     * Функція: setPhysicsParameters.
     * Призначення: Встановлює фізичні параметри або обробники подій для візуалізації.
     */
    public void setPhysicsParameters(LiquidType liquid, double rMm, double distanceM, double bDensity) {
        this.liquid = liquid;
        this.ballRadius = rMm / 1000.0;
        this.distance = distanceM;
        this.ballDensity = bDensity;
        resetSystem();
    }

    /*
     * Лабораторна робота № 7-3 "Метод Стокса".
     * Функція: setOnMeasurementCompleted.
     * Призначення: Встановлює фізичні параметри або обробники подій для візуалізації.
     */
    public void setOnMeasurementCompleted(Runnable callback) {
        this.onMeasurementCompleted = callback;
    }

    /*
     * Лабораторна робота № 7-3 "Метод Стокса".
     * Функція: resetSystem.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
    public void resetSystem() {
        this.positionY = 0;
        this.velocity = 0;
        this.elapsedTime = 0;
        this.isRunning = false;
        this.timerStarted = false;
        draw();
    }

    /*
     * Лабораторна робота № 7-3 "Метод Стокса".
     * Функція: startSimulation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    public void startSimulation() {
        resetSystem();
        this.isRunning = true;
    }

    public double getElapsedTime() { return elapsedTime; }

    public boolean isTimerStarted() { return timerStarted; }

    /*
     * Лабораторна робота № 7-3 "Метод Стокса".
     * Функція: startAnimation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    private void startAnimation() {
        timer = new AnimationTimer() {
    /*
     * Лабораторна робота № 7-3 "Метод Стокса".
     * Функція: handle.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
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

    /*
     * Лабораторна робота № 7-3 "Метод Стокса".
     * Функція: update.
     * Призначення: Оновлює графічні елементи та анімацію на основі нових даних.
     */
    private void update(double dt) {
        double buoyantFactor = 1.0 - liquid.getDensity() / ballDensity;
        double dragFactor = (9.0 * liquid.getTheoreticalViscosity()) / (2.0 * ballRadius * ballRadius * ballDensity);
        double a = 9.81 * buoyantFactor - dragFactor * velocity;
        velocity += a * dt;
        positionY += velocity * dt;

        double lowerMark = upperMark + distance;

        if (!timerStarted && positionY >= upperMark && positionY < lowerMark) {
            timerStarted = true;
            double overshoot = positionY - upperMark;
            elapsedTime = overshoot / velocity;
        }
        else if (timerStarted && positionY >= lowerMark) {
            double overshoot = positionY - lowerMark;
            elapsedTime += dt - (overshoot / velocity);

            elapsedTime += (Math.random() - 0.5) * 0.1;

            timerStarted = false;
            isRunning = false;
            if (onMeasurementCompleted != null) {
                onMeasurementCompleted.run();
            }
        }
        else if (timerStarted) {
            elapsedTime += dt;
        }
    }

    /*
     * Лабораторна робота № 7-3 "Метод Стокса".
     * Функція: draw.
     * Призначення: Відмальовує графічні компоненти та стан симуляції на полотні.
     */
    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();
        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, w, h);

        double originX = w / 2;
        double scale = (h - 60) / (upperMark + distance + 0.05);
        double cylWidth = 100;
        double cylTop = 20;
        double cylBottom = 20 + (upperMark + distance + 0.05) * scale;

        gc.setFill(Color.web(liquid.getColorHex(), 0.6));
        gc.fillRect(originX - cylWidth/2, cylTop, cylWidth, cylBottom - cylTop);
        gc.setStroke(Color.web("#adb5bd"));
        gc.setLineWidth(3);
        gc.strokeRect(originX - cylWidth/2, cylTop, cylWidth, cylBottom - cylTop);

        double mark1Y = 20 + upperMark * scale;
        double mark2Y = 20 + (upperMark + distance) * scale;
        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        gc.strokeLine(originX - cylWidth/2 - 15, mark1Y, originX + cylWidth/2 + 15, mark1Y);
        gc.strokeLine(originX - cylWidth/2 - 15, mark2Y, originX + cylWidth/2 + 15, mark2Y);
        gc.setFill(Color.BLACK);
        gc.fillText("m1 (Старт)", originX + cylWidth/2 + 20, mark1Y + 4);
        gc.fillText("m2 (Стоп)", originX + cylWidth/2 + 20, mark2Y + 4);

        double visualR = ballRadius * 1000 * 3;
        if (visualR < 6) visualR = 6;

        double bobY = 20 + positionY * scale;
        if (bobY > cylBottom - visualR) bobY = cylBottom - visualR;

        RadialGradient grad = new RadialGradient(0, 0, originX - visualR*0.3, bobY - visualR*0.3, visualR,
                false, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(1, Color.web("#343a40")));
        gc.setFill(grad);
        gc.fillOval(originX - visualR, bobY - visualR, visualR * 2, visualR * 2);
    }
}