/*
 * Лабораторна робота № 7-4 "Теплопровідність металів".
 * Клас: HeatConductionCanvas.
 * Призначення: відповідає за графічне відображення симуляції, анімацію
 * фізичного процесу та відмальовку компонентів установки.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level7.lab7_4.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HeatConductionCanvas extends Canvas {

    private boolean isHeaterOn = false;
    private double waterFlowRate = 0.0;
    private double t1 = 20.0;
    private double t2 = 20.0;
    private double t3 = 20.0;
    private double t4 = 20.0;
    private double heaterTemp = 20.0;
    private double currentK = 390.0;
    private boolean isMeasuringVol = false;
    private double currentVolumeMl = 0.0;
    private final double MAX_VOLUME_ML = 200.0;

    private AnimationTimer timer;
    private double time = 0;
    private List<double[]> particles = new ArrayList<>();

    /*
     * Лабораторна робота № 7-4 "Теплопровідність металів".
     * Функція: HeatConductionCanvas.
     * Призначення: Конструктор класу, ініціалізує початкові параметри та стан об'єкта.
     */
    public HeatConductionCanvas(double width, double height) {
        super(width, height);
        for (int i = 0; i < 50; i++) {
            double startX = Math.random() * 280.0;
            double startY = (Math.random() * 20.0) - 10.0;
            particles.add(new double[]{startX, startY});
        }
        startAnimation();
    }

    /*
     * Лабораторна робота № 7-4 "Теплопровідність металів".
     * Функція: updateState.
     * Призначення: Оновлює графічні елементи та анімацію на основі нових даних.
     */
    public void updateState(boolean heater, double flow, double t1, double t2, double t3, double t4, double hTemp, double k) {
        this.isHeaterOn = heater;
        this.waterFlowRate = flow;
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
        this.t4 = t4;
        this.heaterTemp = hTemp;
        this.currentK = k;
    }

    /*
     * Лабораторна робота № 7-4 "Теплопровідність металів".
     * Функція: updateVolumeState.
     * Призначення: Оновлює графічні елементи та анімацію на основі нових даних.
     */
    public void updateVolumeState(boolean isMeasuring, double currentVolume) {
        this.isMeasuringVol = isMeasuring;
        this.currentVolumeMl = currentVolume;
    }

    /*
     * Лабораторна робота № 7-4 "Теплопровідність металів".
     * Функція: startAnimation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    private void startAnimation() {
        timer = new AnimationTimer() {
            private long lastTime = 0;
    /*
     * Лабораторна робота № 7-4 "Теплопровідність металів".
     * Функція: handle.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                time += dt;
                drawFrame(dt);
            }
        };
        timer.start();
    }

    /*
     * Лабораторна робота № 7-4 "Теплопровідність металів".
     * Функція: stopAnimation.
     * Призначення: Зупиняє цикл анімації.
     */
    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    /*
     * Лабораторна робота № 7-4 "Теплопровідність металів".
     * Функція: drawFrame.
     * Призначення: Відмальовує графічні компоненти та стан симуляції на полотні.
     */
    private void drawFrame(double dt) {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#0d1117"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#1e293b"));
        gc.setLineWidth(1.0);
        for (int i = 0; i < w; i += 40) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 40) gc.strokeLine(0, i, w, i);

        double centerY = h * 0.40;
        double startX = 140.0;
        double rodLength = 260.0;

        Color coldColor = getTemperatureColor(t3);
        Color hotColor = getTemperatureColor(t4);
        LinearGradient rodGradient = new LinearGradient(startX, 0, startX + rodLength, 0, false, CycleMethod.NO_CYCLE, new Stop(0, coldColor), new Stop(1, hotColor));

        gc.setFill(rodGradient);
        gc.fillRect(startX, centerY - 15, rodLength, 30);
        gc.setStroke(Color.web("#475569"));
        gc.setLineWidth(2.0);
        gc.strokeRect(startX, centerY - 15, rodLength, 30);

        if (isHeaterOn && t4 > t3 + 5.0) {
            double speed = (currentK / 100.0) * 50.0;
            gc.setFill(Color.web("#ffeb3b", 0.6));
            for (double[] particle : particles) {
                particle[0] -= speed * dt;
                if (particle[0] < 0) particle[0] = rodLength;
                gc.fillOval(startX + particle[0], centerY + particle[1], 4, 4);
            }
        }

        double heaterX = startX + rodLength - 40;
        gc.setFill(Color.web("#1e293b"));
        gc.fillRect(heaterX, centerY - 40, 80, 80);

        Color spiralColor = isHeaterOn ? Color.web("#f43f5e") : Color.web("#475569");
        if (isHeaterOn) {
            double glow = Math.min(1.0, (heaterTemp - 20) / 200.0);
            gc.setStroke(Color.color(1.0, 0.0, 0.5, glow));
            gc.setLineWidth(10.0);
            gc.strokeOval(heaterX + 20, centerY - 20, 40, 40);
        }

        gc.setStroke(spiralColor);
        gc.setLineWidth(3.0);
        for (int i = 0; i < 4; i++) {
            gc.strokeArc(heaterX + 20, centerY - 25 + i * 10, 40, 20, 0, 180, ArcType.OPEN);
        }
        gc.setFill(Color.web("#cbd5e1"));
        gc.fillText("ПІЧ", heaterX + 30, centerY + 60);

        double coolerX = startX + 20;
        gc.setFill(Color.web("#0288d1", 0.3));
        gc.fillRect(coolerX, centerY - 35, 60, 70);
        gc.setStroke(Color.web("#0ea5e9"));
        gc.setLineWidth(2.0);
        gc.strokeRect(coolerX, centerY - 35, 60, 70);

        gc.strokeLine(coolerX + 15, centerY + 35, coolerX + 15, centerY + 80);
        gc.strokeLine(coolerX + 45, centerY - 35, coolerX + 45, centerY - 80);

        double pipeDropX = coolerX - 60;
        gc.strokeLine(coolerX + 45, centerY - 80, pipeDropX, centerY - 80);
        gc.strokeLine(pipeDropX, centerY - 80, pipeDropX, centerY);

        gc.setFill(Color.web("#64748b"));
        gc.fillOval(coolerX + 35, centerY - 70, 20, 20);
        gc.setStroke(Color.web("#ff9800"));
        gc.setLineWidth(4.0);
        double angle = waterFlowRate * Math.PI / 2;
        double cx = coolerX + 45;
        double cy = centerY - 60;
        gc.strokeLine(cx - 10 * Math.cos(angle), cy - 10 * Math.sin(angle), cx + 10 * Math.cos(angle), cy + 10 * Math.sin(angle));

        if (waterFlowRate > 0.05) {
            gc.setStroke(Color.web("#00ffcc", 0.6));
            gc.setLineWidth(2.0);
            gc.setLineDashes(4, 4);

            double offset = (time * 50 * waterFlowRate) % 8;
            gc.strokeLine(coolerX + 15, centerY + 80 - offset, coolerX + 15, centerY + 35);
            gc.strokeLine(coolerX + 45, centerY - 35 - offset, coolerX + 45, centerY - 80);
            gc.strokeLine(coolerX + 45 - offset, centerY - 80, pipeDropX, centerY - 80);
            gc.strokeLine(pipeDropX, centerY - 80 + offset, pipeDropX, centerY);

            gc.setLineDashes(null);

            if ((isMeasuringVol || waterFlowRate > 0) && currentVolumeMl < MAX_VOLUME_ML) {
                double dropY = (time * 200 * waterFlowRate) % 110;
                gc.fillOval(pipeDropX - 2, centerY + dropY, 4, 8);
                gc.fillOval(pipeDropX - 2, centerY + dropY + 40, 4, 8);
            }
        }
        gc.setFill(Color.web("#cbd5e1"));
        gc.fillText("ХОЛОД.", coolerX + 5, centerY + 60);

        double beakerW = 60;
        double beakerH = 140;
        double beakerX = pipeDropX - beakerW / 2.0;
        double beakerY = centerY + 20;

        gc.setStroke(Color.web("#ffffff", 0.9));
        gc.setLineWidth(3.0);
        gc.strokeLine(beakerX, beakerY, beakerX, beakerY + beakerH);
        gc.strokeLine(beakerX + beakerW, beakerY, beakerX + beakerW, beakerY + beakerH);
        gc.strokeLine(beakerX, beakerY + beakerH, beakerX + beakerW, beakerY + beakerH);

        gc.setStroke(Color.web("#ffffff", 0.6));
        gc.setLineWidth(1.5);
        gc.setFill(Color.web("#94a3b8"));
        gc.setFont(javafx.scene.text.Font.font("System", 10));

        for(int i = 1; i <= 4; i++) {
            double tickY = beakerY + beakerH - (i * beakerH / 4.0);
            gc.strokeLine(beakerX, tickY, beakerX + 15, tickY);
            gc.fillText(String.valueOf(i * 50), beakerX + 18, tickY + 4);
        }

        if (currentVolumeMl > 0) {
            double fillRatio = Math.min(1.0, currentVolumeMl / MAX_VOLUME_ML);
            double waterH = beakerH * fillRatio;

            gc.setFill(Color.web("#0ea5e9", 0.75));
            gc.fillRect(beakerX + 2, beakerY + beakerH - waterH, beakerW - 4, waterH);

            gc.setFill(Color.web("#38bdf8", 0.9));
            gc.fillOval(beakerX + 2, beakerY + beakerH - waterH - 3, beakerW - 4, 6);
        }

        gc.setFill(Color.web("#00ffcc"));
        gc.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 12));
        gc.fillText("МЕНЗУРКА", beakerX - 5, beakerY + beakerH + 20);

        gc.setFill(Color.web("#ffffff"));
        gc.fillText(String.format(Locale.US, "V = %.1f мл", currentVolumeMl), beakerX - 5, beakerY + beakerH + 35);

        drawThermometer(gc, coolerX + 5, centerY + 50, t1, "t1");
        drawThermometer(gc, coolerX + 55, centerY - 50, t2, "t2");
        drawThermometer(gc, startX + 100, centerY - 15, t3, "t3");
        drawThermometer(gc, heaterX - 20, centerY - 15, t4, "t4");
    }

    /*
     * Лабораторна робота № 7-4 "Теплопровідність металів".
     * Функція: drawThermometer.
     * Призначення: Відмальовує графічні компоненти та стан симуляції на полотні.
     */
    private void drawThermometer(GraphicsContext gc, double x, double y, double temp, String label) {
        gc.setFill(Color.web("#cbd5e1"));
        gc.fillRoundRect(x - 4, y - 80, 8, 80, 8, 8);
        gc.setFill(Color.web("#0f172a"));
        gc.fillRect(x - 2, y - 75, 4, 70);

        double tRatio = Math.max(0, Math.min(1.0, temp / 100.0));
        double height = 70 * tRatio;

        gc.setFill(Color.web("#ef4444"));
        gc.fillRect(x - 2, y - 5 - height, 4, height);
        gc.fillOval(x - 6, y - 5, 12, 12);

        gc.setFill(Color.web("#00ffcc"));
        gc.fillText(String.format(Locale.US, "%s: %.1f°", label, temp), x - 15, y - 90);
    }

    /*
     * Лабораторна робота № 7-4 "Теплопровідність металів".
     * Функція: getTemperatureColor.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
    private Color getTemperatureColor(double temp) {
        double ratio = Math.max(0, Math.min(1.0, (temp - 20) / 80.0));
        return Color.color(0.3 + ratio * 0.7, 0.4, 0.8 - ratio * 0.6);
    }
}