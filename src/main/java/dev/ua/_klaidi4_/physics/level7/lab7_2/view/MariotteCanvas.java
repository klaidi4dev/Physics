/*
 * Лабораторна робота № 7-2 "В'язкість газів".
 * Клас: MariotteCanvas.
 * Призначення: відповідає за графічне відображення симуляції, анімацію
 * фізичного процесу та відмальовку компонентів установки.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level7.lab7_2.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MariotteCanvas extends Canvas {

    private boolean isMeasuring = false;
    private double progress = 0.0;
    private double displayedTime = 0.0;
    private double startWaterLevel = 0.8;
    private double endWaterLevel = 0.6;
    private double dropY = 0;

    private AnimationTimer timer;
    private long lastTime = 0;

    /*
     * Лабораторна робота № 7-2 "В'язкість газів".
     * Функція: MariotteCanvas.
     * Призначення: Конструктор класу, ініціалізує початкові параметри та стан об'єкта.
     */
    public MariotteCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    /*
     * Лабораторна робота № 7-2 "В'язкість газів".
     * Функція: updateState.
     * Призначення: Оновлює графічні елементи та анімацію на основі нових даних.
     */
    public void updateState(double progress, double timeStr, double volumeDropPct) {
        this.progress = progress;
        this.displayedTime = timeStr;
        this.endWaterLevel = startWaterLevel - volumeDropPct;
        draw();
    }

    /*
     * Лабораторна робота № 7-2 "В'язкість газів".
     * Функція: setMeasuring.
     * Призначення: Встановлює фізичні параметри або обробники подій для візуалізації.
     */
    public void setMeasuring(boolean measuring) {
        this.isMeasuring = measuring;
        if (!measuring) {
            this.startWaterLevel = this.endWaterLevel;
            if (this.startWaterLevel < 0.2) this.startWaterLevel = 0.8;
        }
    }

    /*
     * Лабораторна робота № 7-2 "В'язкість газів".
     * Функція: startAnimation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    public void startAnimation() {
        if (timer != null) timer.stop();
        timer = new AnimationTimer() {
    /*
     * Лабораторна робота № 7-2 "В'язкість газів".
     * Функція: handle.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                if (isMeasuring) {
                    dropY += dt * 300;
                    if (dropY > 80) dropY = 0;
                }
                draw();
            }
        };
        timer.start();
    }

    /*
     * Лабораторна робота № 7-2 "В'язкість газів".
     * Функція: draw.
     * Призначення: Відмальовує графічні компоненти та стан симуляції на полотні.
     */
    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#e0e0e0"));
        gc.fillRect(0, 0, w, h);
        gc.setFill(Color.web("#b0bec5"));
        gc.fillRect(0, h - 80, w, 80);

        double centerX = w / 2;

        gc.setFill(Color.web("#37474f"));
        gc.fillRect(centerX - 90, 40, 10, h - 120);
        gc.fillRect(centerX - 130, h - 80, 90, 15);
        gc.fillRect(centerX - 90, 150, 45, 10);

        double flaskW = 90;
        double flaskH = 220;
        double flaskX = centerX - flaskW / 2;
        double flaskY = 60;
        double currentLvl = startWaterLevel - (progress * (startWaterLevel - endWaterLevel));
        double waterY = flaskY + flaskH * (1.0 - currentLvl);
        double waterH = flaskH * currentLvl;

        LinearGradient waterGrad = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#4fc3f7", 0.8)), new Stop(1, Color.web("#0288d1", 0.8)));
        gc.setFill(waterGrad);
        gc.fillRoundRect(flaskX, waterY, flaskW, waterH, 10, 10);

        gc.setFill(Color.web("#ffffff", 0.3));
        gc.fillRoundRect(flaskX + 5, waterY + 5, 15, waterH - 10, 5, 5);
        gc.setStroke(Color.web("#90a4ae", 0.8));
        gc.setLineWidth(3);
        gc.strokeRoundRect(flaskX, flaskY, flaskW, flaskH, 10, 10);
        gc.setFill(Color.web("#ffffff", 0.15));
        gc.fillRoundRect(flaskX, flaskY, flaskW, flaskH, 10, 10);
        gc.setStroke(Color.web("#000000", 0.6));
        gc.setLineWidth(1);
        for (int i = 0; i <= 10; i++) {
            double tickY = flaskY + (flaskH / 10.0) * i;
            gc.strokeLine(flaskX + flaskW, tickY, flaskX + flaskW + ((i % 5 == 0) ? 10 : 5), tickY);
        }

        gc.setFill(Color.web("#8d6e63"));
        gc.fillRect(centerX - 15, flaskY - 15, 30, 15);
        gc.setFill(Color.web("#cfd8dc"));
        gc.fillRect(centerX - 2, flaskY - 40, 4, 80);
        gc.setFill(Color.web("#78909c"));
        gc.fillRect(centerX - 5, flaskY + flaskH, 10, 20);
        gc.setFill(Color.web("#d32f2f"));
        gc.fillOval(centerX + 5, flaskY + flaskH + 2, 15, 15);

        double beakerW = 50;
        double beakerH = 60;
        double beakerX = centerX - beakerW / 2;
        double beakerY = h - 80 - beakerH;
        double collectedWaterH = (beakerH * 0.8) * progress;
        gc.setFill(waterGrad);
        gc.fillRect(beakerX, beakerY + beakerH - collectedWaterH, beakerW, collectedWaterH);
        gc.setStroke(Color.web("#cfd8dc", 0.8));
        gc.setLineWidth(2);
        gc.strokeRect(beakerX, beakerY, beakerW, beakerH);
        gc.setFill(Color.web("#ffffff", 0.2));
        gc.fillRect(beakerX, beakerY, beakerW, beakerH);
        if (isMeasuring) {
            gc.setFill(Color.web("#29b6f6"));
            gc.fillOval(centerX - 3, flaskY + flaskH + 20 + dropY, 6, 10);
        }

        gc.setFill(Color.web("#212121"));
        gc.fillRoundRect(w - 170, 20, 150, 60, 10, 10);
        gc.setStroke(Color.web("#616161"));
        gc.strokeRoundRect(w - 170, 20, 150, 60, 10, 10);

        gc.setFill(isMeasuring ? Color.web("#ff1744") : Color.web("#00e676"));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
        String timeFmt = String.format("%05.2f s", displayedTime);
        gc.fillText(timeFmt, w - 155, 60);
    }
}