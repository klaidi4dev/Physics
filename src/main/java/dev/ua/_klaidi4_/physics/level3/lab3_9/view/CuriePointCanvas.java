/*
 * Лабораторна робота № 3-9 "Точка Кюрі".
 * Клас: CuriePointCanvas.
 * Призначення: відповідає за графічне відображення симуляції, анімацію
 * фізичного процесу та відмальовку компонентів установки.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level3.lab3_9.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CuriePointCanvas extends Canvas {

    private double temperature = 20;
    private double current = 96;
    private double curieTemp = 120;
    private boolean heating = false;
    private String material = "Феромагнетик зразок №1";

    private AnimationTimer timer;
    private long lastTime = 0;
    private double pulse = 0;

    /*
     * Лабораторна робота № 3-9 "Точка Кюрі".
     * Функція: CuriePointCanvas.
     * Призначення: Конструктор класу, ініціалізує початкові параметри та стан об'єкта.
     */
    public CuriePointCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    /*
     * Лабораторна робота № 3-9 "Точка Кюрі".
     * Функція: updateState.
     * Призначення: Оновлює графічні елементи та анімацію на основі нових даних.
     */
    public void updateState(String material, double temperature, double current, double curieTemp, boolean heating) {
        this.material = material;
        this.temperature = temperature;
        this.current = current;
        this.curieTemp = curieTemp;
        this.heating = heating;
        draw();
    }

    /*
     * Лабораторна робота № 3-9 "Точка Кюрі".
     * Функція: stopAnimation.
     * Призначення: Зупиняє цикл анімації.
     */
    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    /*
     * Лабораторна робота № 3-9 "Точка Кюрі".
     * Функція: startAnimation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    private void startAnimation() {
        timer = new AnimationTimer() {
    /*
     * Лабораторна робота № 3-9 "Точка Кюрі".
     * Функція: handle.
     * Призначення: Допоміжний метод для обробки логіки або внутрішнього стану компонента.
     */
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                if (dt > 0.05) dt = 0.05;

                pulse += dt * 3.0;
                draw();
            }
        };

        timer.start();
    }

    /*
     * Лабораторна робота № 3-9 "Точка Кюрі".
     * Функція: draw.
     * Призначення: Відмальовує графічні компоненти та стан симуляції на полотні.
     */
    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#eef2f7"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#cbd5e1"));
        gc.setLineWidth(1);
        for (int i = 0; i < w; i += 40) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 40) gc.strokeLine(0, i, w, i);

        drawFurnace(gc, 55, 65);
        drawMicroAmmeter(gc, 375, 65);
        drawScheme(gc, 85, 275);
    }

    /*
     * Лабораторна робота № 3-9 "Точка Кюрі".
     * Функція: drawFurnace.
     * Призначення: Відмальовує графічні компоненти та стан симуляції на полотні.
     */
    private void drawFurnace(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.web("#334155"));
        gc.fillRoundRect(x, y, 250, 165, 18, 18);

        gc.setFill(Color.web("#111827"));
        gc.fillRoundRect(x + 25, y + 25, 200, 115, 15, 15);

        double heatLevel = Math.min(1.0, temperature / Math.max(1.0, curieTemp + 40));
        Color heatColor = Color.color(1.0, 0.10 + heatLevel * 0.65, 0.02, 0.35 + heatLevel * 0.50);

        gc.setFill(heatColor);
        gc.fillRoundRect(x + 35, y + 35, 180, 95, 12, 12);

        if (heating) {
            gc.setFill(Color.web("#ef4444", 0.18 + 0.12 * Math.sin(pulse)));
            gc.fillOval(x + 32, y + 25, 186, 110);
        }

        gc.setFill(Color.web("#475569"));
        gc.fillRoundRect(x + 80, y + 60, 110, 45, 12, 12);

        gc.setStroke(Color.web("#f97316"));
        gc.setLineWidth(3);
        for (int i = 0; i < 8; i++) {
            double cx = x + 92 + i * 12;
            gc.strokeOval(cx, y + 62, 14, 40);
        }

        gc.setFill(temperature >= curieTemp ? Color.web("#64748b") : Color.web("#dc2626"));
        gc.fillRoundRect(x + 105, y + 70, 60, 22, 8, 8);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 14));
        gc.fillText("Електрична піч", x + 65, y - 10);

        gc.setFont(Font.font("System", FontWeight.BOLD, 13));
        gc.fillText(material, x + 35, y + 158);

        gc.setFill(Color.web("#111827"));
        gc.fillRoundRect(x + 62, y + 180, 130, 32, 8, 8);

        gc.setFill(Color.web("#22c55e"));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        gc.fillText(String.format("%.1f °C", temperature), x + 78, y + 202);

        gc.setFill(Color.web("#facc15"));
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.fillText(String.format("Tc ≈ %.0f °C", curieTemp), x + 88, y + 225);
    }

    /*
     * Лабораторна робота № 3-9 "Точка Кюрі".
     * Функція: drawMicroAmmeter.
     * Призначення: Відмальовує графічні компоненти та стан симуляції на полотні.
     */
    private void drawMicroAmmeter(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.web("#1e293b"));
        gc.fillRoundRect(x, y, 185, 165, 18, 18);

        gc.setFill(Color.web("#f8fafc"));
        gc.fillOval(x + 25, y + 25, 135, 135);

        gc.setStroke(Color.web("#0f172a"));
        gc.setLineWidth(3);
        gc.strokeOval(x + 25, y + 25, 135, 135);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("System", FontWeight.BOLD, 13));
        gc.fillText("mA", x + 82, y + 70);

        double cx = x + 92.5;
        double cy = y + 122;
        double radius = 70;

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        for (int i = 0; i <= 100; i += 10) {
            double normalized = i / 100.0;
            double angle = Math.toRadians(210 + normalized * 120);

            double x1 = cx + Math.cos(angle) * (radius - 5);
            double y1 = cy + Math.sin(angle) * (radius - 5);
            double x2 = cx + Math.cos(angle) * (radius - 17);
            double y2 = cy + Math.sin(angle) * (radius - 17);

            gc.strokeLine(x1, y1, x2, y2);

            if (i % 20 == 0) {
                gc.setFont(Font.font("System", FontWeight.NORMAL, 8));
                gc.fillText(String.valueOf(i),
                        cx + Math.cos(angle) * (radius - 34) - 8,
                        cy + Math.sin(angle) * (radius - 34) + 4);
            }
        }

        double limited = Math.max(0, Math.min(100, current));
        double needleAngle = Math.toRadians(210 + (limited / 100.0) * 120);

        gc.setStroke(Color.RED);
        gc.setLineWidth(3);
        gc.strokeLine(cx, cy, cx + Math.cos(needleAngle) * 55, cy + Math.sin(needleAngle) * 55);

        gc.setFill(Color.BLACK);
        gc.fillOval(cx - 5, cy - 5, 10, 10);

        gc.setFill(Color.web("#111827"));
        gc.fillRoundRect(x + 35, y + 132, 115, 25, 6, 6);

        gc.setFill(Color.web("#22c55e"));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        gc.fillText(String.format("I₂ = %.1f", current), x + 45, y + 150);
    }

    /*
     * Лабораторна робота № 3-9 "Точка Кюрі".
     * Функція: drawScheme.
     * Призначення: Відмальовує графічні компоненти та стан симуляції на полотні.
     */
    private void drawScheme(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.web("#ffffff"));
        gc.fillRoundRect(x, y, 440, 90, 12, 12);

        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, 440, 90, 12, 12);

        gc.setFill(Color.web("#111827"));
        gc.setFont(Font.font("System", FontWeight.BOLD, 13));
        gc.fillText("Схема: електропіч + феромагнітний зразок + вторинна обмотка", x + 25, y + 20);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(x + 40, y + 55, x + 145, y + 55);
        gc.strokeLine(x + 295, y + 55, x + 400, y + 55);

        gc.setFill(Color.web("#f97316"));
        gc.fillRoundRect(x + 145, y + 38, 150, 34, 10, 10);

        gc.setFill(temperature >= curieTemp ? Color.web("#64748b") : Color.web("#dc2626"));
        gc.fillRoundRect(x + 185, y + 47, 70, 16, 6, 6);

        gc.setStroke(Color.web("#2563eb"));
        gc.setLineWidth(3);
        for (int i = 0; i < 7; i++) {
            gc.strokeOval(x + 150 + i * 18, y + 41, 22, 28);
        }

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("System", FontWeight.BOLD, 11));
        gc.fillText("при Tc струм I₂ різко падає", x + 155, y + 84);
    }
}