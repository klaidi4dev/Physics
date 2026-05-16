/*
 * Лабораторна робота № 8-2 "Ефект Холла".
 * Клас: HallEffectCanvas.
 * Призначення: відповідає за графічне відображення симуляції, анімацію
 * фізичного процесу та відмальовку компонентів установки.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level8.lab8_2.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

public class HallEffectCanvas extends Canvas {

    private double currentIMain = 0.0;
    private double currentB = 0.4;
    private boolean isPType = true;
    private boolean isPowerOn = false;

    private static class Particle {
        double x, y;
        double speedX;
        double speedY;

        Particle(double x, double y) {
            this.x = x;
            this.y = y;
            this.speedX = 0;
            this.speedY = 0;
        }
    }

    private List<Particle> particles = new ArrayList<>();

    /*
     * Лабораторна робота № 8-2 "Ефект Холла".
     * Функція: HallEffectCanvas.
     * Призначення: Конструктор класу, ініціалізує початкові параметри та стан об'єкта.
     */
    public HallEffectCanvas(double width, double height) {
        super(width, height);
        for (int i = 0; i < 25; i++) {
            particles.add(new Particle(Math.random() * 300 - 150, (Math.random() * 80) - 40));
        }
        drawFrame();
    }

    /*
     * Лабораторна робота № 8-2 "Ефект Холла".
     * Функція: updateState.
     * Призначення: Оновлює графічні елементи та анімацію на основі нових даних.
     */
    public void updateState(double iMain, double magneticField, boolean pType, boolean powerOn, double dt) {
        this.currentIMain = iMain;
        this.currentB = magneticField;
        this.isPType = pType;
        this.isPowerOn = powerOn;
        updateParticles(dt);
        drawFrame();
    }

    /*
     * Лабораторна робота № 8-2 "Ефект Холла".
     * Функція: updateParticles.
     * Призначення: Оновлює графічні елементи та анімацію на основі нових даних.
     */
    private void updateParticles(double dt) {
        if (!isPowerOn || currentIMain <= 0) {
            return;
        }

        double dir = isPType ? 1.0 : -1.0;
        double baseSpeed = (50.0 + currentIMain * 15.0) * dir;
        double deflectionForce = currentIMain * currentB * 20.0;

        for (Particle p : particles) {
            p.speedX = baseSpeed;
            p.speedY = -deflectionForce * (1.0 - (p.y + 50) / 100.0);

            p.x += p.speedX * dt;
            p.y += p.speedY * dt;

            if (p.y < -42) p.y = -42;

            if (isPType && p.x > 150) {
                p.x = -150;
                p.y = (Math.random() * 80) - 30;
            } else if (!isPType && p.x < -150) {
                p.x = 150;
                p.y = (Math.random() * 80) - 30;
            }
        }
    }

    /*
     * Лабораторна робота № 8-2 "Ефект Холла".
     * Функція: drawFrame.
     * Призначення: Відмальовує графічні компоненти та стан симуляції на полотні.
     */
    private void drawFrame() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#0f172a"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#1e293b"));
        gc.setLineWidth(1.0);
        for (int i = 0; i < w; i += 40) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 40) gc.strokeLine(0, i, w, i);

        double centerX = w / 2;
        double centerY = h / 2;

        gc.setFill(Color.web("#3b82f6", 0.5));
        gc.setStroke(Color.web("#2563eb", 0.5));
        gc.setLineWidth(1.5);
        for (int ix = -180; ix <= 180; ix += 40) {
            for (int iy = -120; iy <= 120; iy += 40) {
                gc.strokeOval(centerX + ix - 5, centerY + iy - 5, 10, 10);
                gc.fillOval(centerX + ix - 2, centerY + iy - 2, 4, 4);
            }
        }

        gc.setFill(Color.web("#60a5fa"));
        gc.setFont(Font.font("System", 14));
        gc.fillText(String.format("B (%.2f Тл)", currentB), centerX - 180, centerY - 130);

        gc.setFill(Color.web("#475569", 0.8));
        gc.fillRoundRect(centerX - 150, centerY - 50, 300, 100, 10, 10);
        gc.setStroke(Color.web("#94a3b8"));
        gc.setLineWidth(2.0);
        gc.strokeRoundRect(centerX - 150, centerY - 50, 300, 100, 10, 10);

        gc.setFill(Color.web("#f59e0b"));
        gc.fillRect(centerX - 160, centerY - 20, 10, 40);
        gc.fillRect(centerX + 150, centerY - 20, 10, 40);
        gc.setFill(Color.WHITE);
        gc.fillText("1", centerX - 175, centerY + 5);
        gc.fillText("2", centerX + 165, centerY + 5);

        gc.setFill(Color.web("#ef4444"));
        gc.fillRect(centerX - 10, centerY - 60, 20, 10);
        gc.fillRect(centerX - 10, centerY + 50, 20, 10);
        gc.setFill(Color.WHITE);
        gc.fillText("3", centerX + 15, centerY - 55);
        gc.fillText("4", centerX + 15, centerY + 65);

        if (isPowerOn) {
            Color pColor = isPType ? Color.web("#ef4444") : Color.web("#3b82f6");
            gc.setFill(pColor);

            for (Particle p : particles) {
                gc.fillOval(centerX + p.x - 4, centerY + p.y - 4, 8, 8);
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(1.5);
                gc.strokeLine(centerX + p.x - 2, centerY + p.y, centerX + p.x + 2, centerY + p.y);
                if (isPType) {
                    gc.strokeLine(centerX + p.x, centerY + p.y - 2, centerX + p.x, centerY + p.y + 2);
                }
            }

            gc.setStroke(Color.web("#22c55e"));
            gc.setLineWidth(2.0);
            drawArrow(gc, centerX, centerY, centerX, centerY - 30);
            gc.setFill(Color.web("#22c55e"));
            gc.fillText("F_л", centerX + 5, centerY - 15);

            gc.setStroke(Color.WHITE);
            drawArrow(gc, centerX, centerY, centerX + (isPType ? 40 : -40), centerY);
            gc.setFill(Color.WHITE);
            gc.fillText("v", centerX + (isPType ? 20 : -30), centerY + 15);

            gc.setStroke(Color.web("#eab308"));
            double startY = isPType ? -40 : 40;
            double endY = isPType ? 40 : -40;
            drawArrow(gc, centerX - 20, centerY + startY, centerX - 20, centerY + endY);
            gc.setFill(Color.web("#eab308"));
            gc.fillText("E_x", centerX - 45, centerY + 5);
        }
    }

    /*
     * Лабораторна робота № 8-2 "Ефект Холла".
     * Функція: drawArrow.
     * Призначення: Відмальовує графічні компоненти та стан симуляції на полотні.
     */
    private void drawArrow(GraphicsContext gc, double x1, double y1, double x2, double y2) {
        gc.strokeLine(x1, y1, x2, y2);
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double len = 6;
        gc.strokeLine(x2, y2, x2 - len * Math.cos(angle - Math.PI / 6), y2 - len * Math.sin(angle - Math.PI / 6));
        gc.strokeLine(x2, y2, x2 - len * Math.cos(angle + Math.PI / 6), y2 - len * Math.sin(angle + Math.PI / 6));
    }
}