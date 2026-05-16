/*
 * Лабораторна робота № 4-6 "Поперечні коливання струни".
 * Клас: StringResonanceCanvas.
 * Призначення: відповідає за графічне відображення симуляції, анімацію
 * фізичного процесу та відмальовку компонентів установки.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level4.lab4_6.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

public class StringResonanceCanvas extends Canvas {

    private boolean isGenerating = false;
    private double currentTime = 0;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double length = 1.0;
    private double diameter = 0.001;
    private double rho = 7800.0;
    private double tension = 20.0;
    private double generatorFreq = 30.0;
    private int activeHarmonic = 0;
    private double currentAmp = 0;

    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: StringResonanceCanvas.
     * Призначення: Конструктор класу, ініціалізує початкові параметри та стан об'єкта.
     */
    public StringResonanceCanvas(double width, double height) {
        super(width, height);
        drawFrame();
    }

    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: setPhysicsParameters.
     * Призначення: Встановлює фізичні параметри або обробники подій для візуалізації.
     */
    public void setPhysicsParameters(double l, double d, double rho, double f, double genFreq) {
        this.length = l;
        this.diameter = d;
        this.rho = rho;
        this.tension = f;
        this.generatorFreq = genFreq;
        calculateResonance();
        if (!isGenerating) drawFrame();
    }

    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: toggleGenerator.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
    public void toggleGenerator(boolean state) {
        this.isGenerating = state;
        if (state) {
            if (timer != null) timer.stop();
            startAnimation();
        } else {
            if (timer != null) timer.stop();
            drawFrame();
        }
    }

    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: calculateResonance.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
    private void calculateResonance() {
        double maxAmp = 0;
        int bestN = 0;

        double v1 = (1.0 / (length * diameter)) * Math.sqrt(tension / (Math.PI * rho));
        for (int i = 1; i <= 5; i += 2) {
            double vi = i * v1;
            double diff = Math.abs(generatorFreq - vi);
            double a = 60.0 * Math.exp(-(diff * diff) / 2.0);

            if (a > maxAmp) {
                maxAmp = a;
                bestN = i;
            }
        }
        this.activeHarmonic = bestN;
        this.currentAmp = maxAmp;
    }

    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: startAnimation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    private void startAnimation() {
        lastTime = System.nanoTime();
        timer = new AnimationTimer() {
    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: handle.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
            @Override
            public void handle(long now) {
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                currentTime += dt;
                drawFrame();
            }
        };
        timer.start();
    }

    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: stopAnimation.
     * Призначення: Зупиняє цикл анімації.
     */
    public void stopAnimation() {
        if (timer != null) timer.stop();
        isGenerating = false;
        drawFrame();
    }

    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: getCurrentAmp.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
    public double getCurrentAmp() {
        return currentAmp;
    }

    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
     * Функція: getActiveHarmonic.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
    public int getActiveHarmonic() {
        return activeHarmonic;
    }

    /*
     * Лабораторна робота № 4-6 "Поперечні коливання струни".
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

        double centerY = h / 2;
        double margin = 50;
        double stringLengthPx = w - 2 * margin;
        double magnetWidth = 60;
        double magnetHeight = 120;
        gc.setFill(new LinearGradient(0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#d32f2f")), new Stop(1, Color.web("#1976d2"))));
        gc.fillRoundRect(w / 2 - magnetWidth / 2, centerY - magnetHeight / 2, magnetWidth, magnetHeight, 15, 15);

        gc.setFill(Color.WHITE);
        gc.fillText("N", w / 2 - 20, centerY - 40);
        gc.fillText("S", w / 2 + 10, centerY - 40);

        gc.setFill(Color.web("#94a3b8"));
        gc.fillOval(margin - 8, centerY - 8, 16, 16);
        gc.fillOval(w - margin - 8, centerY - 8, 16, 16);

        gc.setStroke(Color.web("#00ffcc"));
        gc.setLineWidth(4.0);
        gc.beginPath();
        gc.moveTo(margin, centerY);

        for (int i = 0; i <= 200; i++) {
            double xFraction = i / 200.0;
            double yOffset = 0;

            if (isGenerating) {
                if (currentAmp > 5.0) {
                    yOffset = currentAmp * Math.sin(activeHarmonic * Math.PI * xFraction) * Math.cos(currentTime * generatorFreq);
                } else {
                    yOffset = (Math.random() - 0.5) * 4.0 * Math.cos(currentTime * generatorFreq);
                }
            }
            gc.lineTo(margin + xFraction * stringLengthPx, centerY - yOffset);
        }
        gc.stroke();

        if (isGenerating && currentAmp > 15.0) {
            gc.setFill(Color.web("#ff007f"));
            for (int i = 0; i <= activeHarmonic; i++) {
                double nx = margin + (i / (double) activeHarmonic) * stringLengthPx;
                gc.fillOval(nx - 6, centerY - 6, 12, 12);
            }
        }
    }
}