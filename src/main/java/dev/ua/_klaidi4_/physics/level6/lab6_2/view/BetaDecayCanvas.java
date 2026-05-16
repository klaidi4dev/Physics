/*
 * Лабораторна робота № 6-2 "Активність β-джерела".
 * Клас: BetaDecayCanvas.
 * Призначення: відповідає за графічне відображення симуляції, анімацію
 * фізичного процесу та відмальовку компонентів установки.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level6.lab6_2.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class BetaDecayCanvas extends Canvas {

    private boolean isMeasuring = false;
    private int sampleIndex = 0;

    private AnimationTimer timer;
    private double time = 0;

    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: BetaDecayCanvas.
     * Призначення: Конструктор класу, ініціалізує початкові параметри та стан об'єкта.
     */
    public BetaDecayCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: setPhysicsParameters.
     * Призначення: Встановлює фізичні параметри або обробники подій для візуалізації.
     */
    public void setPhysicsParameters(boolean measuring, int sampleType) {
        this.isMeasuring = measuring;
        this.sampleIndex = sampleType;
    }

    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: startAnimation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    private void startAnimation() {
        timer = new AnimationTimer() {
            private long lastTime = 0;
    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: handle.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                time += dt;
                drawFrame();
            }
        };
        timer.start();
    }

    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
     * Функція: stopAnimation.
     * Призначення: Зупиняє цикл анімації.
     */
    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    /*
     * Лабораторна робота № 6-2 "Активність β-джерела".
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

        double centerX = w / 2;
        double centerY = h / 2 - 20;

        gc.setFill(Color.web("#334155"));
        gc.fillRect(centerX - 80, centerY - 100, 160, 180);
        gc.setFill(Color.web("#0d1117"));
        gc.fillRect(centerX - 60, centerY - 80, 120, 140);

        gc.setFill(Color.web("#94a3b8"));
        gc.fillRect(centerX - 25, centerY - 70, 50, 80);

        gc.setFill(Color.web("#00ffcc", 0.3));
        gc.fillRect(centerX - 20, centerY + 10, 40, 5);

        gc.setStroke(Color.web("#475569"));
        gc.setLineWidth(4.0);
        gc.strokeLine(centerX, centerY - 70, centerX, centerY - 150);
        gc.strokeLine(centerX, centerY - 150, centerX + 150, centerY - 150);

        gc.setFill(Color.web("#475569"));
        gc.fillRect(centerX - 40, centerY + 40, 80, 10);
        if (sampleIndex == 1) {
            gc.setFill(Color.web("#00e5ff"));
            gc.fillRect(centerX - 15, centerY + 35, 30, 5);
        } else if (sampleIndex == 2) {
            gc.setFill(Color.web("#ff007f"));
            gc.fillRect(centerX - 15, centerY + 35, 30, 5);
        }

        if (isMeasuring) {
            gc.setStroke(Color.web("#00e5ff", 0.8));
            gc.setLineWidth(2.0);

            int numParticles = (sampleIndex == 0) ? 2 : (sampleIndex == 1) ? 8 : 12;

            for (int i = 0; i < numParticles; i++) {
                double speed = 150.0 + (i * 20);
                double currentTravel = (time * speed + i * 40) % 120;

                double startX, startY;
                double angle;

                if (sampleIndex == 0) {
                    startX = centerX - 60 + Math.random() * 120;
                    startY = centerY - 80 + Math.random() * 140;
                    angle = Math.random() * Math.PI * 2;
                } else {
                    startX = centerX - 10 + (i % 3) * 10;
                    startY = centerY + 35;
                    angle = -Math.PI / 2 + (Math.random() - 0.5) * 0.5;
                }

                double px = startX + Math.cos(angle) * currentTravel;
                double py = startY + Math.sin(angle) * currentTravel;
                double tailX = px - Math.cos(angle) * 10;
                double tailY = py - Math.sin(angle) * 10;

                if (px > centerX - 60 && px < centerX + 60 && py > centerY - 80 && py < centerY + 60) {
                    gc.strokeLine(px, py, tailX, tailY);

                    if (py < centerY + 15 && py > centerY + 5 && px > centerX - 25 && px < centerX + 25) {
                        gc.setFill(Color.web("#00ffcc", 0.6));
                        gc.fillRect(centerX - 20, centerY + 10, 40, 5);
                    }
                }
            }
        }
    }
}