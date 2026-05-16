/*
 * Лабораторна робота № 5-6 "Дифракційна решітка".
 * Клас: DiffractionCanvas.
 * Призначення: відповідає за графічне відображення симуляції, анімацію
 * фізичного процесу та відмальовку компонентів установки.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level5.lab5_6.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class DiffractionCanvas extends Canvas {

    private boolean isPowerOn = false;
    private double telescopeAngle = 0.0;
    private double filterWavelength = 589.0;
    private double gratingD = 10000.0;

    private AnimationTimer timer;

    /*
     * Лабораторна робота № 5-6 "Дифракційна решітка".
     * Функція: DiffractionCanvas.
     * Призначення: Конструктор класу, ініціалізує початкові параметри та стан об'єкта.
     */
    public DiffractionCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    /*
     * Лабораторна робота № 5-6 "Дифракційна решітка".
     * Функція: setPhysicsParameters.
     * Призначення: Встановлює фізичні параметри або обробники подій для візуалізації.
     */
    public void setPhysicsParameters(boolean power, double angle, double wave, double d) {
        this.isPowerOn = power;
        this.telescopeAngle = angle;
        this.filterWavelength = wave;
        this.gratingD = d;
    }

    /*
     * Лабораторна робота № 5-6 "Дифракційна решітка".
     * Функція: startAnimation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    private void startAnimation() {
        timer = new AnimationTimer() {
    /*
     * Лабораторна робота № 5-6 "Дифракційна решітка".
     * Функція: handle.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
            @Override
            public void handle(long now) {
                drawFrame();
            }
        };
        timer.start();
    }

    /*
     * Лабораторна робота № 5-6 "Дифракційна решітка".
     * Функція: stopAnimation.
     * Призначення: Зупиняє цикл анімації.
     */
    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    /*
     * Лабораторна робота № 5-6 "Дифракційна решітка".
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

        double eyeCenterX = w * 0.35;
        double eyeCenterY = h / 2;
        double eyeRadius = 140;

        gc.setFill(Color.web("#1e293b"));
        gc.fillOval(eyeCenterX - eyeRadius - 10, eyeCenterY - eyeRadius - 10, eyeRadius * 2 + 20, eyeRadius * 2 + 20);

        gc.setFill(Color.web("#000000"));
        gc.fillOval(eyeCenterX - eyeRadius, eyeCenterY - eyeRadius, eyeRadius * 2, eyeRadius * 2);

        if (isPowerOn) {
            gc.save();
            gc.beginPath();
            gc.arc(eyeCenterX, eyeCenterY, eyeRadius, eyeRadius, 0, 360);
            gc.clip();

            Color waveColor = getFilterColor(filterWavelength);

            double pixelsPerDegree = eyeRadius / 2.0;

            for (int k = -3; k <= 3; k++) {
                double targetAngleRad = Math.asin(k * filterWavelength / gratingD);
                double targetAngleDeg = Math.toDegrees(targetAngleRad);
                double angleDiff = targetAngleDeg - telescopeAngle;

                if (Math.abs(angleDiff) <= 2.0) {
                    double xPos = eyeCenterX + angleDiff * pixelsPerDegree;

                    double intensity = (k == 0) ? 1.0 : (1.0 - Math.abs(k) * 0.2);

                    gc.setStroke(Color.color(waveColor.getRed() * intensity, waveColor.getGreen() * intensity, waveColor.getBlue() * intensity, 0.9));
                    gc.setLineWidth(4.0);
                    gc.strokeLine(xPos, eyeCenterY - eyeRadius, xPos, eyeCenterY + eyeRadius);

                    gc.setStroke(Color.color(waveColor.getRed(), waveColor.getGreen(), waveColor.getBlue(), 0.3 * intensity));
                    gc.setLineWidth(12.0);
                    gc.strokeLine(xPos, eyeCenterY - eyeRadius, xPos, eyeCenterY + eyeRadius);
                }
            }
            gc.restore();
        }

        gc.setStroke(Color.web("#ffffff", 0.6));
        gc.setLineWidth(1.5);
        gc.strokeLine(eyeCenterX - eyeRadius, eyeCenterY, eyeCenterX + eyeRadius, eyeCenterY);
        gc.setStroke(Color.web("#00ffcc"));
        gc.setLineWidth(1.5);
        gc.strokeLine(eyeCenterX, eyeCenterY - eyeRadius, eyeCenterX, eyeCenterY + eyeRadius);

        double sideX = w * 0.8;
        double stageY = h * 0.55;

        gc.setStroke(Color.web("#475569"));
        gc.setLineWidth(2.0);
        gc.strokeOval(sideX - 70, stageY - 70, 140, 140);
        gc.setFill(Color.web("#1e293b"));
        gc.fillOval(sideX - 60, stageY - 60, 120, 120);

        gc.setFill(Color.web("#334155"));
        gc.fillOval(sideX - 25, stageY - 25, 50, 50);
        gc.setStroke(Color.web("#0288d1"));
        gc.setLineWidth(3.0);
        gc.strokeLine(sideX - 20, stageY, sideX + 20, stageY);

        gc.setFill(Color.web("#64748b"));
        gc.fillRect(sideX - 15, stageY + 60, 30, 60);
        if (isPowerOn) {
            Color waveColor = getFilterColor(filterWavelength);
            gc.setStroke(Color.color(waveColor.getRed(), waveColor.getGreen(), waveColor.getBlue(), 0.8));
            gc.setLineWidth(3.0);
            gc.strokeLine(sideX, stageY + 120, sideX, stageY);

            for (int k = -2; k <= 2; k++) {
                double targetAngleRad = Math.asin(k * filterWavelength / gratingD);
                double targetAngleDeg = Math.toDegrees(targetAngleRad);
                double intensity = (k == 0) ? 0.7 : 0.3;

                gc.save();
                gc.translate(sideX, stageY);
                gc.rotate(targetAngleDeg);
                gc.setStroke(Color.color(waveColor.getRed(), waveColor.getGreen(), waveColor.getBlue(), intensity));
                gc.setLineWidth(2.0);
                gc.setLineDashes(4, 4);
                gc.strokeLine(0, 0, 0, -100);
                gc.setLineDashes(null);
                gc.restore();
            }
        }

        gc.save();
        gc.translate(sideX, stageY);
        gc.rotate(telescopeAngle);

        gc.setFill(Color.web("#94a3b8"));
        gc.fillRect(-15, -140, 30, 80);
        gc.setFill(Color.web("#000000"));
        gc.fillRect(-10, -140, 20, 5);

        gc.restore();
    }

    /*
     * Лабораторна робота № 5-6 "Дифракційна решітка".
     * Функція: getFilterColor.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
    private Color getFilterColor(double wave) {
        if (wave > 550) return Color.web("#FFD700");
        if (wave > 500) return Color.web("#00FF00");
        return Color.web("#0080FF");
    }
}