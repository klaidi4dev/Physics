/*
 * Лабораторна робота № 3-3 "Питомий заряд електрона".
 * Клас: FocusingCanvas.
 * Призначення: відповідає за графічне відображення симуляції, анімацію
 * фізичного процесу та відмальовку компонентів установки.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level3.lab3_3.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class FocusingCanvas extends Canvas {

    private double voltage = 1000.0;
    private double current = 0.0;
    private double turnsPerMeter = 2000.0;
    private double length = 0.15;
    private static final double EM_THEORY = 1.7588e11;
    private static final double MU_0 = 4 * Math.PI * 1e-7;
    private static final double FOCUS_ZONE = 0.35;
    private static final double FOCUS_TOLERANCE = 0.025;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double phase = 0.0;
    private double focusAnimation = 0.0;

    /*
     * Лабораторна робота № 3-3 "Питомий заряд електрона".
     * Функція: FocusingCanvas.
     * Призначення: Конструктор класу, ініціалізує полотно та запускає анімацію.
     */
    public FocusingCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    /*
     * Лабораторна робота № 3-3 "Питомий заряд електрона".
     * Функція: setPhysicsParameters.
     * Призначення: Встановлює параметри установки для візуалізації процесу фокусування.
     */
    public void setPhysicsParameters(double voltage, double current, double turnsPerMeter, double length) {
        this.voltage = voltage;
        this.current = current;
        this.turnsPerMeter = turnsPerMeter;
        this.length = length;
    }

    /*
     * Лабораторна робота № 3-3 "Питомий заряд електрона".
     * Функція: getFocusCurrent.
     * Призначення: Розраховує фокусний струм за теоретичною формулою для внутрішньої перевірки симуляції.
     */
    public double getFocusCurrent() {
        if (voltage <= 0 || turnsPerMeter <= 0 || length <= 0) {
            return 0.0;
        }

        double numerator = 8.0 * Math.PI * Math.PI * voltage;
        double denominator = MU_0 * MU_0 * turnsPerMeter * turnsPerMeter * length * length * EM_THEORY;

        if (denominator <= 0) {
            return 0.0;
        }

        return Math.sqrt(numerator / denominator);
    }

    /*
     * Лабораторна робота № 3-3 "Питомий заряд електрона".
     * Функція: getFocusErrorPercent.
     * Призначення: Визначає відносне відхилення поточного струму від фокусного струму.
     */
    public double getFocusErrorPercent() {
        double focusCurrent = getFocusCurrent();

        if (focusCurrent <= 0) {
            return 100.0;
        }

        return Math.abs(current - focusCurrent) / focusCurrent * 100.0;
    }

    /*
     * Лабораторна робота № 3-3 "Питомий заряд електрона".
     * Функція: isBeamFocused.
     * Призначення: Перевіряє, чи зведена світла смужка в чітку точку.
     */
    public boolean isBeamFocused() {
        double focusCurrent = getFocusCurrent();

        if (focusCurrent <= 0 || current <= 0) {
            return false;
        }

        double relativeError = Math.abs(current - focusCurrent) / focusCurrent;
        return relativeError <= FOCUS_TOLERANCE;
    }

    /*
     * Лабораторна робота № 3-3 "Питомий заряд електрона".
     * Функція: startAnimation.
     * Призначення: Запускає цикл анімації для постійного оновлення візуалізації.
     */
    private void startAnimation() {
        timer = new AnimationTimer() {
            /*
             * Лабораторна робота № 3-3 "Питомий заряд електрона".
             * Функція: handle.
             * Призначення: Оновлює часовий стан симуляції та перемальовує полотно.
             */
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                phase += dt;
                draw();
            }
        };

        timer.start();
    }

    /*
     * Лабораторна робота № 3-3 "Питомий заряд електрона".
     * Функція: stopAnimation.
     * Призначення: Зупиняє цикл анімації при закритті лабораторної роботи.
     */
    public void stopAnimation() {
        if (timer != null) {
            timer.stop();
        }
    }

    /*
     * Лабораторна робота № 3-3 "Питомий заряд електрона".
     * Функція: draw.
     * Призначення: Відмальовує фон, установку, екран, електронний пучок та інформаційні дані.
     */
    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();

        double w = getWidth();
        double h = getHeight();

        drawBackground(gc, w, h);
        drawTube(gc, w, h);
        drawScreen(gc, w, h);
        drawElectronBeam(gc, w, h);
        drawInfo(gc, w, h);
    }

    /*
     * Лабораторна робота № 3-3 "Питомий заряд електрона".
     * Функція: drawBackground.
     * Призначення: Відмальовує темний фон та координатну сітку.
     */
    private void drawBackground(GraphicsContext gc, double w, double h) {
        gc.setFill(Color.web("#031403"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#0b3f0b"));
        gc.setLineWidth(1);

        for (int x = 0; x < w; x += 30) {
            gc.strokeLine(x, 0, x, h);
        }

        for (int y = 0; y < h; y += 30) {
            gc.strokeLine(0, y, w, y);
        }
    }

    /*
     * Лабораторна робота № 3-3 "Питомий заряд електрона".
     * Функція: drawTube.
     * Призначення: Відмальовує умовну електронно-променеву трубку, катод, анод та вісь пучка.
     */
    private void drawTube(GraphicsContext gc, double w, double h) {
        double centerY = h / 2.0;
        double tubeX = 55;
        double tubeW = w - 110;
        double tubeH = 120;

        gc.setFill(Color.rgb(20, 70, 30, 0.22));
        gc.fillRoundRect(tubeX, centerY - tubeH / 2.0, tubeW, tubeH, 35, 35);

        gc.setStroke(Color.web("#1e6b35"));
        gc.setLineWidth(3);
        gc.strokeRoundRect(tubeX, centerY - tubeH / 2.0, tubeW, tubeH, 35, 35);

        gc.setStroke(Color.web("#66ff66"));
        gc.setLineWidth(2);
        gc.strokeLine(tubeX + 40, centerY, tubeX + tubeW - 40, centerY);

        gc.setFill(Color.web("#ffcc66"));
        gc.fillOval(tubeX + 18, centerY - 12, 24, 24);

        gc.setFill(Color.web("#d8ffd8"));
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.fillText("К", tubeX + 24, centerY - 20);
        gc.fillText("A", tubeX + 85, centerY - 20);
        gc.fillText("Екран", tubeX + tubeW - 88, centerY - 75);
    }

    /*
     * Лабораторна робота № 3-3 "Питомий заряд електрона".
     * Функція: drawScreen.
     * Призначення: Відмальовує флуоресціюючий екран електронно-променевої трубки.
     */
    private void drawScreen(GraphicsContext gc, double w, double h) {
        double screenRadius = Math.min(w, h) * 0.23;
        double screenX = w / 2.0;
        double screenY = h / 2.0;

        gc.setFill(Color.rgb(0, 30, 0, 0.65));
        gc.fillOval(screenX - screenRadius, screenY - screenRadius, screenRadius * 2, screenRadius * 2);

        gc.setStroke(Color.web("#2d8a42"));
        gc.setLineWidth(4);
        gc.strokeOval(screenX - screenRadius, screenY - screenRadius, screenRadius * 2, screenRadius * 2);

        gc.setStroke(Color.rgb(60, 255, 90, 0.18));
        gc.setLineWidth(1);

        for (int i = 1; i <= 3; i++) {
            double r = screenRadius * i / 4.0;
            gc.strokeOval(screenX - r, screenY - r, r * 2, r * 2);
        }
    }

    /*
     * Лабораторна робота № 3-3 "Питомий заряд електрона".
     * Функція: drawElectronBeam.
     * Призначення: Відмальовує світлу смужку, розмиту пляму або чітку точку залежно від підібраного струму.
     */
    private void drawElectronBeam(GraphicsContext gc, double w, double h) {
        double screenX = w / 2.0;
        double screenY = h / 2.0;

        double focusCurrent = getFocusCurrent();

        double relativeError;
        if (focusCurrent <= 0 || current <= 0) {
            relativeError = 1.0;
        } else {
            relativeError = Math.abs(current - focusCurrent) / focusCurrent;
        }

        double targetFocus = clamp(1.0 - relativeError / FOCUS_ZONE, 0.0, 1.0);

        if (!isBeamFocused()) {
            targetFocus = Math.min(targetFocus, 0.88);
        }

        focusAnimation += (targetFocus - focusAnimation) * 0.12;

        double currentRatio = focusCurrent <= 0 ? 0.0 : clamp(current / focusCurrent, 0.0, 1.8);
        double brightness = clamp(0.30 + currentRatio * 0.45, 0.30, 1.0);

        if (current <= 0.0001) {
            drawUnfocusedLine(gc, screenX, screenY, 105, 25, 20, brightness, 0);
            return;
        }

        if (isBeamFocused()) {
            drawFocusedPoint(gc, screenX, screenY, brightness);
            return;
        }

        double lineLength = lerp(105, 20, focusAnimation);
        double lineThickness = lerp(26, 7, focusAnimation);
        double blur = lerp(20, 5, focusAnimation);

        double angle = current < focusCurrent ? 0 : 90;
        angle += Math.sin(phase * 2.5) * 3.0;

        drawUnfocusedLine(gc, screenX, screenY, lineLength, lineThickness, blur, brightness, angle);
    }

    /*
     * Лабораторна робота № 3-3 "Питомий заряд електрона".
     * Функція: drawUnfocusedLine.
     * Призначення: Відмальовує світлу розмиту смужку, коли струм ще не підібраний.
     */
    private void drawUnfocusedLine(
            GraphicsContext gc,
            double x,
            double y,
            double halfLength,
            double thickness,
            double blur,
            double brightness,
            double angle
    ) {
        gc.save();
        gc.translate(x, y);
        gc.rotate(angle);

        gc.setEffect(new GaussianBlur(blur));
        gc.setFill(Color.rgb(80, 255, 80, 0.34 * brightness));
        gc.fillOval(-halfLength, -thickness / 2.0, halfLength * 2.0, thickness);

        gc.setEffect(null);
        gc.setStroke(Color.rgb(130, 255, 130, 0.62 * brightness));
        gc.setLineWidth(2);
        gc.strokeLine(-halfLength * 0.85, 0, halfLength * 0.85, 0);

        gc.restore();
    }

    /*
     * Лабораторна робота № 3-3 "Питомий заряд електрона".
     * Функція: drawFocusedPoint.
     * Призначення: Відмальовує чітку точку при правильному струмі в соленоїді.
     */
    private void drawFocusedPoint(GraphicsContext gc, double x, double y, double brightness) {
        double pulse = 1.0 + Math.sin(phase * 8.0) * 0.08;

        gc.setEffect(new GaussianBlur(12));
        gc.setFill(Color.rgb(80, 255, 80, 0.45 * brightness));
        gc.fillOval(x - 22 * pulse, y - 22 * pulse, 44 * pulse, 44 * pulse);

        gc.setEffect(new GaussianBlur(4));
        gc.setFill(Color.rgb(120, 255, 120, 0.75 * brightness));
        gc.fillOval(x - 8, y - 8, 16, 16);

        gc.setEffect(null);
        gc.setFill(Color.web("#eaffea"));
        gc.fillOval(x - 3.2, y - 3.2, 6.4, 6.4);
    }

    /*
     * Лабораторна робота № 3-3 "Питомий заряд електрона".
     * Функція: drawInfo.
     * Призначення: Виводить на полотно поточний струм та стан світлої смужки.
     */
    private void drawInfo(GraphicsContext gc, double w, double h) {
        gc.setFill(Color.rgb(0, 0, 0, 0.55));
        gc.fillRoundRect(15, h - 82, 345, 62, 12, 12);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 13));

        gc.setFill(Color.web("#aaffaa"));
        gc.fillText(String.format("I поточний: %.3f А", current), 28, h - 58);

        gc.setFill(Color.web("#66ccff"));
        gc.fillText("Підбирайте струм I реостатом / повзунком", 28, h - 38);

        if (isBeamFocused()) {
            gc.setFill(Color.web("#00ff66"));
            gc.fillText("Стан екрана: смужка зведена в точку", 28, h - 20);
        } else {
            gc.setFill(Color.web("#ffcc66"));
            gc.fillText("Стан екрана: світла смужка", 28, h - 20);
        }
    }

    /*
     * Лабораторна робота № 3-3 "Питомий заряд електрона".
     * Функція: lerp.
     * Призначення: Виконує лінійну інтерполяцію між двома значеннями.
     */
    private double lerp(double a, double b, double t) {
        return a + (b - a) * clamp(t, 0.0, 1.0);
    }

    /*
     * Лабораторна робота № 3-3 "Питомий заряд електрона".
     * Функція: clamp.
     * Призначення: Обмежує значення в заданих межах.
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}