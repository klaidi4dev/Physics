/*
 * Лабораторна робота № 5-5 "Кільця Ньютона".
 * Клас: NewtonRingsCanvas.
 * Призначення: відповідає за графічне відображення симуляції, анімацію
 * фізичного процесу та відмальовку компонентів установки.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level5.lab5_5.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class NewtonRingsCanvas extends Canvas {

    private boolean isPowerOn = false;
    private double micrometerZ = 0.0;
    private double filterWavelength = 650.0;
    private double lensR = 1000.0;
    private double zoom = 1.0;

    private AnimationTimer timer;

    /*
     * Лабораторна робота № 5-5 "Кільця Ньютона".
     * Функція: NewtonRingsCanvas.
     * Призначення: Конструктор класу, ініціалізує початкові параметри та стан об'єкта.
     */
    public NewtonRingsCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    /*
     * Лабораторна робота № 5-5 "Кільця Ньютона".
     * Функція: setPhysicsParameters.
     * Призначення: Встановлює фізичні параметри або обробники подій для візуалізації.
     */
    public void setPhysicsParameters(boolean power, double mz, double wave, double r, double zoom) {
        this.isPowerOn = power;
        this.micrometerZ = mz;
        this.filterWavelength = wave;
        this.lensR = r;
        this.zoom = zoom;
    }

    /*
     * Лабораторна робота № 5-5 "Кільця Ньютона".
     * Функція: startAnimation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    private void startAnimation() {
        timer = new AnimationTimer() {
            private long lastTime = 0;
    /*
     * Лабораторна робота № 5-5 "Кільця Ньютона".
     * Функція: handle.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                drawFrame();
            }
        };
        timer.start();
    }

    /*
     * Лабораторна робота № 5-5 "Кільця Ньютона".
     * Функція: stopAnimation.
     * Призначення: Зупиняє цикл анімації.
     */
    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    /*
     * Лабораторна робота № 5-5 "Кільця Ньютона".
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

            double lambda_mm = filterWavelength / 1_000_000.0;
            double pixelsPerMm = (eyeRadius / 3.5) * zoom;

            Color waveColor = getFilterColor(filterWavelength);
            double step_px = 1.0;
            gc.setLineWidth(step_px + 0.5);

            for (double r_px = eyeRadius; r_px >= 0; r_px -= step_px) {
                double r_mm = r_px / pixelsPerMm;

                double intensity = Math.pow(Math.sin(Math.PI * r_mm * r_mm / (lambda_mm * lensR)), 2);

                double red = waveColor.getRed() * intensity;
                double green = waveColor.getGreen() * intensity;
                double blue = waveColor.getBlue() * intensity;

                gc.setStroke(Color.color(red, green, blue, 1.0));
                gc.strokeOval(eyeCenterX - r_px, eyeCenterY - r_px, r_px * 2, r_px * 2);
            }
            gc.restore();
        }

        double pixelsPerMm = (eyeRadius / 3.5) * zoom;
        double crosshairX = eyeCenterX + micrometerZ * pixelsPerMm;

        gc.setStroke(Color.web("#ffffff", 0.6));
        gc.setLineWidth(1.5);
        gc.strokeLine(eyeCenterX - eyeRadius, eyeCenterY, eyeCenterX + eyeRadius, eyeCenterY);
        gc.setStroke(Color.web("#00ffcc"));
        gc.strokeLine(crosshairX, eyeCenterY - eyeRadius, crosshairX, eyeCenterY + eyeRadius);

        for (double i = -3.5; i <= 3.5; i += 0.5) {
            double tickX = eyeCenterX + i * pixelsPerMm;
            if (tickX >= eyeCenterX - eyeRadius && tickX <= eyeCenterX + eyeRadius) {
                gc.setStroke(Color.web("#ffffff", 0.4));
                gc.strokeLine(tickX, eyeCenterY - 5, tickX, eyeCenterY + 5);
            }
        }

        double sideX = w * 0.8;
        double stageY = h * 0.8;
        double mirrorY = stageY - 100;

        gc.setFill(Color.web("#81d4fa", 0.4));
        gc.fillRect(sideX - 40, stageY, 80, 10);
        gc.setStroke(Color.web("#0288d1"));
        gc.setLineWidth(2.0);
        gc.strokeRect(sideX - 40, stageY, 80, 10);

        gc.setFill(Color.web("#81d4fa", 0.6));
        gc.beginPath();
        gc.moveTo(sideX - 30, stageY);
        gc.quadraticCurveTo(sideX, stageY - 20, sideX + 30, stageY);
        gc.closePath();
        gc.fill();
        gc.stroke();

        gc.setStroke(Color.web("#94a3b8"));
        gc.setLineWidth(3.0);
        gc.strokeLine(sideX - 25, mirrorY + 25, sideX + 25, mirrorY - 25);

        gc.setFill(Color.web("#334155"));
        gc.fillRect(sideX - 120, mirrorY - 10, 20, 20);
        gc.setStroke(Color.web("#00e5ff"));
        gc.setLineWidth(2.0);
        gc.strokeOval(sideX - 70, mirrorY - 15, 10, 30);

        gc.setFill(Color.web("#475569"));
        gc.fillRect(sideX - 15, mirrorY - 80, 30, 40);

        if (isPowerOn) {
            Color waveColor = getFilterColor(filterWavelength);
            gc.setStroke(Color.color(waveColor.getRed(), waveColor.getGreen(), waveColor.getBlue(), 0.6));
            gc.setLineWidth(2.0);
            gc.setLineDashes(4, 4);
            gc.strokeLine(sideX - 100, mirrorY, sideX, mirrorY);
            gc.strokeLine(sideX, mirrorY, sideX, stageY);
            gc.strokeLine(sideX - 5, stageY, sideX - 5, mirrorY - 80);
            gc.strokeLine(sideX + 5, stageY, sideX + 5, mirrorY - 80);
            gc.setLineDashes(null);
        }
    }

    /*
     * Лабораторна робота № 5-5 "Кільця Ньютона".
     * Функція: getFilterColor.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
    private Color getFilterColor(double wave) {
        if (wave > 600) return Color.web("#ff0033");
        if (wave > 500) return Color.web("#00ff66");
        return Color.web("#0066ff");
    }
}