/*
 * Лабораторна робота № 6-4 "Фотоемульсійний метод".
 * Клас: EmulsionCanvas.
 * Призначення: відповідає за графічне відображення симуляції, анімацію
 * фізичного процесу та відмальовку компонентів установки.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level6.lab6_4.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class EmulsionCanvas extends Canvas {

    private double stageX = 5.0;
    private double stageY = 5.0;
    private final double starCenterX = 5.00;
    private final double starCenterY = 5.00;
    private final double[][] tracks = {
            {6.50, 6.20},
            {3.80, 5.90},
            {4.10, 2.50},
            {7.20, 3.80},
            {5.30, 4.10}
    };

    private AnimationTimer timer;

    /*
     * Лабораторна робота № 6-4 "Фотоемульсійний метод".
     * Функція: EmulsionCanvas.
     * Призначення: Конструктор класу, ініціалізує початкові параметри та стан об'єкта.
     */
    public EmulsionCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    /*
     * Лабораторна робота № 6-4 "Фотоемульсійний метод".
     * Функція: setStageCoordinates.
     * Призначення: Встановлює фізичні параметри або обробники подій для візуалізації.
     */
    public void setStageCoordinates(double x, double y) {
        this.stageX = x;
        this.stageY = y;
    }

    /*
     * Лабораторна робота № 6-4 "Фотоемульсійний метод".
     * Функція: startAnimation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    private void startAnimation() {
        timer = new AnimationTimer() {
    /*
     * Лабораторна робота № 6-4 "Фотоемульсійний метод".
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
     * Лабораторна робота № 6-4 "Фотоемульсійний метод".
     * Функція: stopAnimation.
     * Призначення: Зупиняє цикл анімації.
     */
    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    /*
     * Лабораторна робота № 6-4 "Фотоемульсійний метод".
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

        double eyeCenterX = w / 2.0;
        double eyeCenterY = h / 2.0;
        double eyeRadius = 180.0;

        gc.setFill(Color.web("#1e293b"));
        gc.fillOval(eyeCenterX - eyeRadius - 10, eyeCenterY - eyeRadius - 10, eyeRadius * 2 + 20, eyeRadius * 2 + 20);

        gc.setFill(Color.web("#1c2331"));
        gc.fillOval(eyeCenterX - eyeRadius, eyeCenterY - eyeRadius, eyeRadius * 2, eyeRadius * 2);

        gc.save();
        gc.beginPath();
        gc.arc(eyeCenterX, eyeCenterY, eyeRadius, eyeRadius, 0, 360);
        gc.clip();

        double pixelsPerMm = 80.0;
        double drawCx = eyeCenterX + (starCenterX - stageX) * pixelsPerMm;
        double drawCy = eyeCenterY + (starCenterY - stageY) * pixelsPerMm;

        gc.setStroke(Color.web("#e2e8f0"));
        gc.setLineWidth(3.0);

        for (double[] track : tracks) {
            double drawTx = eyeCenterX + (track[0] - stageX) * pixelsPerMm;
            double drawTy = eyeCenterY + (track[1] - stageY) * pixelsPerMm;

            gc.setLineDashes(2, 3);
            gc.strokeLine(drawCx, drawCy, drawTx, drawTy);

            gc.setStroke(Color.web("#ffffff", 0.3));
            gc.setLineWidth(6.0);
            gc.strokeLine(drawCx, drawCy, drawTx, drawTy);
            gc.setLineWidth(3.0);
            gc.setStroke(Color.web("#e2e8f0"));
        }
        gc.setLineDashes(null);

        gc.setFill(Color.web("#ffffff"));
        gc.fillOval(drawCx - 6, drawCy - 6, 12, 12);
        gc.setFill(Color.web("#ff007f", 0.5));
        gc.fillOval(drawCx - 10, drawCy - 10, 20, 20);

        gc.restore();
        gc.setStroke(Color.web("#00ffcc"));
        gc.setLineWidth(1.5);
        gc.strokeLine(eyeCenterX - eyeRadius, eyeCenterY, eyeCenterX + eyeRadius, eyeCenterY);
        gc.strokeLine(eyeCenterX, eyeCenterY - eyeRadius, eyeCenterX, eyeCenterY + eyeRadius);
        gc.setStroke(Color.web("#ff007f"));
        gc.strokeOval(eyeCenterX - 4, eyeCenterY - 4, 8, 8);
    }
}