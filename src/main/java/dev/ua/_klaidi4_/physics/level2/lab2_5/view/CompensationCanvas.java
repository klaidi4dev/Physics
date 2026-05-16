/*
 * Лабораторна робота № 2-5 "Визначення ЕРС".
 * Клас: CompensationCanvas.
 * Призначення: відповідає за графічне відображення симуляції, анімацію
 * фізичного процесу та відмальовку компонентів установки.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level2.lab2_5.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CompensationCanvas extends Canvas {

    private double targetCurrent = 0.0;
    private double displayedCurrent = 0.0;
    private double sliderPosition = 500.0;
    private double maxSliderPosition = 1000.0;
    private String activeSource = "Ex";
    private double currentE1 = 2.0;

    private AnimationTimer timer;
    private long lastTime = 0;

    /*
     * Лабораторна робота № 2-5 "Визначення ЕРС".
     * Функція: CompensationCanvas.
     * Призначення: Конструктор класу, ініціалізує початкові параметри та стан об'єкта.
     */
    public CompensationCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    /*
     * Лабораторна робота № 2-5 "Визначення ЕРС".
     * Функція: updatePhysics.
     * Призначення: Оновлює графічні елементи та анімацію на основі нових даних.
     */
    public void updatePhysics(double sliderPos, double maxPos, double currentG, String source, double e1) {
        this.sliderPosition = sliderPos;
        this.maxSliderPosition = maxPos;
        this.targetCurrent = currentG;
        this.activeSource = source;
        this.currentE1 = e1;
    }

    /*
     * Лабораторна робота № 2-5 "Визначення ЕРС".
     * Функція: startSimulation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    public void startSimulation() {
        if (timer != null) timer.stop();
        timer = new AnimationTimer() {
    /*
     * Лабораторна робота № 2-5 "Визначення ЕРС".
     * Функція: handle.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                lastTime = now;

                if (Math.abs(displayedCurrent - targetCurrent) > 0.01) {
                    displayedCurrent += (targetCurrent - displayedCurrent) * 0.15;
                } else {
                    displayedCurrent = targetCurrent;
                }

                if (Math.abs(targetCurrent) > 0.5) {
                    displayedCurrent += (Math.random() - 0.5) * 0.2;
                }
                draw();
            }
        };
        timer.start();
    }

    /*
     * Лабораторна робота № 2-5 "Визначення ЕРС".
     * Функція: resetSystem.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
    public void resetSystem() {
        this.targetCurrent = 0.0;
        this.displayedCurrent = 0.0;
        this.sliderPosition = 500.0;
        this.activeSource = "Ex";
        draw();
    }

    /*
     * Лабораторна робота № 2-5 "Визначення ЕРС".
     * Функція: stopSimulation.
     * Призначення: Зупиняє цикл анімації.
     */
    public void stopSimulation() {
        if (timer != null) timer.stop();
    }

    /*
     * Лабораторна робота № 2-5 "Визначення ЕРС".
     * Функція: draw.
     * Призначення: Відмальовує графічні компоненти та стан симуляції на полотні.
     */
    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#f8fafc"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#e2e8f0"));
        gc.setLineWidth(1);
        for (int i = 0; i < w; i += 20) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 20) gc.strokeLine(0, i, w, i);

        double startX = 50;
        double endX = w - 50;
        double rheocordY = 100;
        double batteryY = 40;
        double galvY = 220;

        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(2);

        gc.strokeLine(startX, rheocordY, startX, batteryY);
        gc.strokeLine(endX, rheocordY, endX, batteryY);
        gc.strokeLine(startX, batteryY, w/2 - 20, batteryY);
        gc.strokeLine(w/2 + 20, batteryY, endX, batteryY);

        gc.setLineWidth(4);
        gc.strokeLine(w/2 - 20, batteryY - 10, w/2 - 20, batteryY + 10);
        gc.setLineWidth(2);
        gc.strokeLine(w/2, batteryY - 20, w/2, batteryY + 20);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));

        String e1Text = String.format("E1 (%.1f В)", currentE1);
        gc.fillText(e1Text, w/2 - 25, batteryY - 25);

        gc.setStroke(Color.web("#b45309"));
        gc.setLineWidth(6);
        gc.strokeLine(startX, rheocordY, endX, rheocordY);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        for (int i = 0; i <= 10; i++) {
            double tickX = startX + i * ((endX - startX) / 10);
            gc.strokeLine(tickX, rheocordY + 5, tickX, rheocordY + 15);

            if (i % 2 == 0 || i == 10) {
                gc.setFont(Font.font("System", 10));
                double labelVal = (maxSliderPosition / 10) * i;
                gc.fillText(String.format("%.0f", labelVal), tickX - 10, rheocordY + 28);
            }
        }

        double sliderPx = startX + (sliderPosition / maxSliderPosition) * (endX - startX);
        if(sliderPx < startX) sliderPx = startX;
        if(sliderPx > endX) sliderPx = endX;

        gc.setFill(Color.web("#ef4444"));
        gc.fillPolygon(new double[]{sliderPx - 8, sliderPx + 8, sliderPx},
                new double[]{rheocordY + 45, rheocordY + 45, rheocordY + 5}, 3);

        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(2);
        gc.strokeLine(startX, rheocordY, startX, galvY);
        gc.strokeLine(startX, galvY, w/2 - 50, galvY);
        gc.strokeLine(sliderPx, rheocordY + 45, sliderPx, galvY);
        gc.strokeLine(w/2 + 50, galvY, sliderPx, galvY);

        double elemX = w/2 - 120;
        gc.setFill(Color.WHITE);
        gc.fillRect(elemX - 25, galvY - 15, 50, 30);
        gc.strokeRect(elemX - 25, galvY - 15, 50, 30);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.fillText(activeSource, elemX - 8, galvY + 4);

        double gX = w/2;
        double gY = galvY;
        gc.setFill(Color.WHITE);
        gc.fillOval(gX - 40, gY - 40, 80, 80);
        gc.strokeOval(gX - 40, gY - 40, 80, 80);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("System", FontWeight.BOLD, 16));
        gc.fillText("G", gX - 6, gY - 20);

        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);
        for(int angle = -40; angle <= 40; angle += 10) {
            double rad = Math.toRadians(angle - 90);
            double x1 = gX + 30 * Math.cos(rad);
            double y1 = gY + 30 * Math.sin(rad);
            double x2 = gX + 38 * Math.cos(rad);
            double y2 = gY + 38 * Math.sin(rad);
            gc.strokeLine(x1, y1, x2, y2);
        }

        double deflection = (displayedCurrent / 50.0) * 45.0;
        if (deflection > 45) deflection = 45;
        if (deflection < -45) deflection = -45;

        double needleRad = Math.toRadians(deflection - 90);
        double nX = gX + 35 * Math.cos(needleRad);
        double nY = gY + 35 * Math.sin(needleRad);

        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        gc.strokeLine(gX, gY + 15, nX, nY);
        gc.fillOval(gX - 3, gY + 12, 6, 6);
    }
}