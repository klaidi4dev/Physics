/*
 * Лабораторна робота № 3-1 "Магнітне поле".
 * Клас: GalvanometerCanvas.
 * Призначення: відповідає за графічне відображення симуляції, анімацію
 * фізичного процесу та відмальовку компонентів установки.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level3.lab3_1.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GalvanometerCanvas extends Canvas {

    private double l0 = 20.0;
    private double xPos = 0.0;
    private double targetN = 0;
    private double currentN = 0;
    private boolean isRunning = false;
    private double animTime = 0;
    private AnimationTimer timer;
    private long lastTime = 0;
    private Runnable onFinish;

    /*
     * Лабораторна робота № 3-1 "Магнітне поле".
     * Функція: GalvanometerCanvas.
     * Призначення: Конструктор класу, ініціалізує початкові параметри та стан об'єкта.
     */
    public GalvanometerCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    /*
     * Лабораторна робота № 3-1 "Магнітне поле".
     * Функція: updatePhysics.
     * Призначення: Оновлює графічні елементи та анімацію на основі нових даних.
     */
    public void updatePhysics(double l0, double xPos) {
        this.l0 = l0;
        this.xPos = xPos;
        draw();
    }

    /*
     * Лабораторна робота № 3-1 "Магнітне поле".
     * Функція: playPulse.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
    public void playPulse(double n, Runnable onFinish) {
        this.targetN = n;
        this.onFinish = onFinish;
        this.animTime = 0;
        this.isRunning = true;
    }

    /*
     * Лабораторна робота № 3-1 "Магнітне поле".
     * Функція: stopAnimation.
     * Призначення: Зупиняє цикл анімації.
     */
    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    /*
     * Лабораторна робота № 3-1 "Магнітне поле".
     * Функція: startAnimation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    private void startAnimation() {
        timer = new AnimationTimer() {
    /*
     * Лабораторна робота № 3-1 "Магнітне поле".
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
     * Лабораторна робота № 3-1 "Магнітне поле".
     * Функція: update.
     * Призначення: Оновлює графічні елементи та анімацію на основі нових даних.
     */
    private void update(double dt) {
        animTime += dt;
        double tPeak = 0.15;

        currentN = targetN * (animTime / tPeak) * Math.exp(1 - animTime / tPeak);

        if (animTime >= 1.5) {
            currentN = 0;
            isRunning = false;
            if (onFinish != null) {
                Runnable callback = onFinish;
                onFinish = null;
                callback.run();
            }
        }
    }

    /*
     * Лабораторна робота № 3-1 "Магнітне поле".
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

        double centerX = w / 2;
        double magnetY = 90;

        LinearGradient colGrad = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ced4da")), new Stop(1, Color.web("#6c757d")));

        gc.setFill(colGrad);
        gc.fillRect(centerX - 120, 20, 240, 30);
        gc.fillRect(centerX - 120, 20, 40, magnetY);
        gc.fillRect(centerX + 80, 20, 40, magnetY);

        double visualL0 = l0 * 2.0;
        gc.setFill(Color.web("#d32f2f"));
        gc.fillRect(centerX - visualL0/2 - 40, magnetY + 20, 40, 40);
        gc.setFill(Color.web("#1976d2"));
        gc.fillRect(centerX + visualL0/2, magnetY + 20, 40, 40);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 16));
        gc.fillText("N", centerX - visualL0/2 - 25, magnetY + 45);
        gc.fillText("S", centerX + visualL0/2 + 15, magnetY + 45);

        double coilVisualX = centerX + (xPos * 15);
        gc.setFill(Color.web("#ff9800"));
        gc.fillRect(coilVisualX - 6, magnetY + 25, 12, 30);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(coilVisualX - 6, magnetY + 25, 12, 30);
        gc.strokeLine(coilVisualX, magnetY + 55, coilVisualX, h);

        double galvY = h - 30;
        double r = 130;

        gc.setFill(Color.web("#e9ecef"));
        gc.fillArc(centerX - r, galvY - r, r * 2, r * 2, 0, 180, ArcType.CHORD);
        gc.setStroke(Color.web("#adb5bd"));
        gc.setLineWidth(3);
        gc.strokeArc(centerX - r, galvY - r, r * 2, r * 2, 0, 180, ArcType.CHORD);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        for (int i = -100; i <= 100; i += 10) {
            double angle = Math.toRadians(90 - (i * 0.6));
            double x1 = centerX + (r - 10) * Math.cos(angle);
            double y1 = galvY - (r - 10) * Math.sin(angle);
            double x2 = centerX + (r - 20) * Math.cos(angle);
            double y2 = galvY - (r - 20) * Math.sin(angle);
            gc.strokeLine(x1, y1, x2, y2);
        }

        double needleAngle = Math.toRadians(90 - (currentN * 0.6));
        double needleX = centerX + (r - 15) * Math.cos(needleAngle);
        double needleY = galvY - (r - 15) * Math.sin(needleAngle);

        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        gc.strokeLine(centerX, galvY, needleX, needleY);

        gc.setFill(Color.web("#343a40"));
        gc.fillOval(centerX - 10, galvY - 10, 20, 20);
    }
}