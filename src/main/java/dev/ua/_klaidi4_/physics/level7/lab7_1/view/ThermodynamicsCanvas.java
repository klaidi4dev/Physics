/*
 * Лабораторна робота № 7-1 "Теплоємність газу".
 * Клас: ThermodynamicsCanvas.
 * Призначення: відповідає за графічне відображення симуляції, анімацію
 * фізичного процесу та відмальовку компонентів установки.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level7.lab7_1.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

public class ThermodynamicsCanvas extends Canvas {

    private double targetPressureDiff = 0.0;
    private double currentPressureDiff = 0.0;
    private boolean isValveOpen = false;
    private double heatRate = 1.0;
    private AnimationTimer timer;

    /*
     * Лабораторна робота № 7-1 "Теплоємність газу".
     * Функція: ThermodynamicsCanvas.
     * Призначення: Конструктор класу, ініціалізує початкові параметри та стан об'єкта.
     */
    public ThermodynamicsCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    /*
     * Лабораторна робота № 7-1 "Теплоємність газу".
     * Функція: updateState.
     * Призначення: Оновлює графічні елементи та анімацію на основі нових даних.
     */
    public void updateState(double targetH, boolean valveOpen, double heatRate) {
        this.targetPressureDiff = targetH;
        this.isValveOpen = valveOpen;
        this.heatRate = heatRate;
    }

    /*
     * Лабораторна робота № 7-1 "Теплоємність газу".
     * Функція: getCurrentPressure.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
    public double getCurrentPressure() {
        return currentPressureDiff;
    }

    /*
     * Лабораторна робота № 7-1 "Теплоємність газу".
     * Функція: startAnimation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    private void startAnimation() {
        timer = new AnimationTimer() {
    /*
     * Лабораторна робота № 7-1 "Теплоємність газу".
     * Функція: handle.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
            @Override
            public void handle(long now) {
                double diff = targetPressureDiff - currentPressureDiff;
                if (isValveOpen) {
                    currentPressureDiff += diff * 0.2;
                } else {
                    currentPressureDiff += diff * 0.03 * heatRate;
                }

                if (Math.abs(currentPressureDiff - targetPressureDiff) < 0.1) {
                    currentPressureDiff = targetPressureDiff;
                }
                drawFrame();
            }
        };
        timer.start();
    }

    /*
     * Лабораторна робота № 7-1 "Теплоємність газу".
     * Функція: stopAnimation.
     * Призначення: Зупиняє цикл анімації.
     */
    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    /*
     * Лабораторна робота № 7-1 "Теплоємність газу".
     * Функція: drawFrame.
     * Призначення: Відмальовує графічні компоненти та стан симуляції на полотні.
     */
    private void drawFrame() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#0d1117"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#1e293b"));
        gc.setLineWidth(1.0);
        for (int i = 0; i < w; i += 40) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 40) gc.strokeLine(0, i, w, i);

        double centerX = w * 0.4;
        double centerY = h * 0.5;

        gc.setFill(Color.web("#1e293b"));
        gc.fillOval(centerX - 80, centerY - 60, 160, 160);
        gc.setStroke(Color.web("#475569"));
        gc.setLineWidth(4.0);
        gc.strokeOval(centerX - 80, centerY - 60, 160, 160);

        double intensity = Math.min(1.0, currentPressureDiff / 150.0);
        gc.setFill(Color.color(0.2 + intensity * 0.2, 0.4 + intensity * 0.1, 0.6, 0.4));
        gc.fillOval(centerX - 76, centerY - 56, 152, 152);

        gc.setFill(Color.web("#1e293b"));
        gc.fillRect(centerX - 20, centerY - 100, 40, 45);
        gc.strokeLine(centerX - 20, centerY - 100, centerX - 20, centerY - 60);
        gc.strokeLine(centerX + 20, centerY - 100, centerX + 20, centerY - 60);

        gc.setStroke(Color.web("#94a3b8"));
        gc.setLineWidth(10.0);

        gc.strokeLine(centerX, centerY - 100, centerX, centerY - 160);
        gc.strokeLine(centerX, centerY - 150, centerX - 120, centerY - 150);
        gc.strokeLine(centerX, centerY - 150, centerX + 100, centerY - 150);
        gc.strokeLine(centerX + 60, centerY - 150, centerX + 60, centerY - 20);

        gc.setStroke(Color.web("#0d1117"));
        gc.setLineWidth(6.0);
        gc.strokeLine(centerX, centerY - 100, centerX, centerY - 160);
        gc.strokeLine(centerX, centerY - 150, centerX - 120, centerY - 150);
        gc.strokeLine(centerX, centerY - 150, centerX + 100, centerY - 150);
        gc.strokeLine(centerX + 60, centerY - 150, centerX + 60, centerY - 20);

        gc.setFill(Color.web("#64748b"));
        gc.fillOval(centerX + 90, centerY - 165, 30, 30);

        gc.setStroke(Color.web(isValveOpen ? "#00ffcc" : "#ff007f"));
        gc.setLineWidth(4.0);
        if (isValveOpen) {
            gc.strokeLine(centerX + 95, centerY - 150, centerX + 115, centerY - 150);
        } else {
            gc.strokeLine(centerX + 105, centerY - 160, centerX + 105, centerY - 140);
        }

        if (isValveOpen && currentPressureDiff > 2.0) {
            gc.setStroke(Color.web("#00ffcc", 0.6));
            gc.setLineWidth(2.0);
            gc.setLineDashes(4, 4);
            double offset = (System.currentTimeMillis() % 1000) / 100.0;
            gc.strokeLine(centerX + 125 + offset, centerY - 155, centerX + 160 + offset, centerY - 170);
            gc.strokeLine(centerX + 125 + offset, centerY - 150, centerX + 160 + offset, centerY - 150);
            gc.strokeLine(centerX + 125 + offset, centerY - 145, centerX + 160 + offset, centerY - 130);
            gc.setLineDashes(null);
        }

        gc.setFill(Color.web("#475569"));
        gc.fillRect(centerX - 160, centerY - 170, 40, 40);
        gc.setFill(Color.web("#0288d1"));
        gc.fillRect(centerX - 180, centerY - 155, 20, 10);

        double manoX = centerX + 60;
        double manoY = centerY - 20;

        gc.setStroke(Color.web("#94a3b8", 0.6));
        gc.setLineWidth(14.0);
        gc.strokeLine(manoX, manoY, manoX, manoY + 120);
        gc.strokeLine(manoX + 40, manoY, manoX + 40, manoY + 120);
        gc.strokeArc(manoX, manoY + 100, 40, 40, 180, 180, ArcType.OPEN);

        gc.setFill(Color.web("#ffffff"));
        gc.fillRect(manoX + 18, manoY - 10, 4, 140);
        gc.setStroke(Color.web("#ffffff"));
        gc.setLineWidth(1.0);
        for (int i = 0; i <= 140; i += 10) {
            gc.strokeLine(manoX + 15, manoY - 10 + i, manoX + 25, manoY - 10 + i);
        }

        double baseLevelY = manoY + 60;
        double pxDiff = currentPressureDiff / 2.0;

        double leftLevelY = baseLevelY + pxDiff;
        double rightLevelY = baseLevelY - pxDiff;

        gc.setStroke(Color.web("#00bcd4"));
        gc.setLineWidth(10.0);

        gc.strokeLine(manoX, leftLevelY, manoX, manoY + 120);
        gc.strokeLine(manoX + 40, rightLevelY, manoX + 40, manoY + 120);
        gc.strokeArc(manoX, manoY + 100, 40, 40, 180, 180, ArcType.OPEN);

        gc.setFill(Color.web("#00ffcc"));
        gc.fillText(String.format("h = %.1f", currentPressureDiff), manoX + 50, manoY - 10);
    }
}