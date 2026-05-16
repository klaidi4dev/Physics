/*
 * Лабораторна робота № 6-1 "Пробіг α-частинок".
 * Клас: AlphaDecayCanvas.
 * Призначення: відповідає за графічне відображення симуляції, анімацію
 * фізичного процесу та відмальовку компонентів установки.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level6.lab6_1.view;

import dev.ua._klaidi4_.physics.level6.lab6_1.model.Measurement;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AlphaDecayCanvas extends Canvas {

    private double distanceX = 0.0;
    private boolean isMeasuring = false;
    private List<Measurement> dataPoints = new ArrayList<>();
    private double currentR0 = 0.47;
    private double maxAxisX = 2.0;
    private AnimationTimer timer;
    private double time = 0;

    /*
     * Лабораторна робота № 6-1 "Пробіг α-частинок".
     * Функція: AlphaDecayCanvas.
     * Призначення: Конструктор класу, ініціалізує початкові параметри та стан об'єкта.
     */
    public AlphaDecayCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    /*
     * Лабораторна робота № 6-1 "Пробіг α-частинок".
     * Функція: setPhysicsParameters.
     * Призначення: Встановлює фізичні параметри або обробники подій для візуалізації.
     */
    public void setPhysicsParameters(double x, boolean measuring, List<Measurement> data, double r0) {
        this.distanceX = x;
        this.isMeasuring = measuring;
        this.dataPoints = new ArrayList<>(data);
        this.currentR0 = r0;
        this.maxAxisX = Math.max(2.0, Math.ceil(r0 * 1.5));
    }

    /*
     * Лабораторна робота № 6-1 "Пробіг α-частинок".
     * Функція: startAnimation.
     * Призначення: Запускає цикл анімації та процес візуалізації.
     */
    private void startAnimation() {
        timer = new AnimationTimer() {
            private long lastTime = 0;
    /*
     * Лабораторна робота № 6-1 "Пробіг α-частинок".
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
     * Лабораторна робота № 6-1 "Пробіг α-частинок".
     * Функція: stopAnimation.
     * Призначення: Зупиняє цикл анімації.
     */
    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    /*
     * Лабораторна робота № 6-1 "Пробіг α-частинок".
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

        double apparatusY = h * 0.35;
        double detectorX = 50.0;

        gc.setFill(Color.web("#1e293b"));
        gc.fillRect(10, apparatusY - 40, detectorX - 10, 80);

        boolean particlesReachDetector = distanceX <= currentR0 * 2.0;
        if (isMeasuring && particlesReachDetector) {
            double flash = 0.5 + 0.5 * Math.random();
            gc.setFill(Color.color(0.0, 1.0, 0.8, flash));
        } else {
            gc.setFill(Color.web("#00ffcc", 0.2));
        }
        gc.fillRect(detectorX, apparatusY - 30, 5, 60);

        double scaleX = (w - 120) / maxAxisX;
        double sourceX = detectorX + 5 + distanceX * scaleX;

        gc.setFill(Color.web("#475569"));
        gc.fillRect(sourceX + 10, apparatusY - 5, w - sourceX, 10);

        gc.setFill(Color.web("#94a3b8"));
        gc.fillRect(sourceX, apparatusY - 20, 10, 40);

        gc.setFill(Color.web("#ff007f"));
        gc.fillRect(sourceX - 2, apparatusY - 10, 2, 20);

        if (isMeasuring) {
            gc.setStroke(Color.web("#ffeb3b", 0.8));
            gc.setLineWidth(2.0);

            for (int i = 0; i < 5; i++) {
                double startY = apparatusY - 10 + Math.random() * 20;
                double maxTravelPx = (currentR0 + currentR0*0.5) * scaleX;
                double currentTravelPx = (time * 500 + i * 50) % maxTravelPx;

                double particleX = sourceX - currentTravelPx;
                double tailX = particleX + 20;

                if (particleX > detectorX && currentTravelPx < maxTravelPx) {
                    gc.strokeLine(particleX, startY, tailX > sourceX ? sourceX : tailX, startY);
                }
            }
        }

        gc.setStroke(Color.web("#ffffff", 0.5));
        gc.setLineWidth(1.0);
        gc.strokeLine(detectorX + 5, apparatusY + 60, detectorX + 5 + maxAxisX * scaleX, apparatusY + 60);

        double tickStep = maxAxisX > 5 ? 1.0 : (maxAxisX > 2 ? 0.5 : 0.2);
        for (double i = 0; i <= maxAxisX + 0.01; i += tickStep) {
            double tickX = detectorX + 5 + i * scaleX;
            gc.strokeLine(tickX, apparatusY + 60, tickX, apparatusY + 65);
            gc.setFill(Color.web("#94a3b8"));
            gc.fillText(String.format(Locale.US, "%.1f", i), tickX - 8, apparatusY + 80);
        }

        double graphX = 60.0;
        double graphY = h - 30.0;
        double graphW = w - 90.0;
        double graphH = h * 0.3;

        gc.setStroke(Color.web("#64748b"));
        gc.setLineWidth(2.0);
        gc.strokeLine(graphX, graphY, graphX + graphW, graphY);
        gc.strokeLine(graphX, graphY, graphX, graphY - graphH);

        gc.setFill(Color.web("#64748b"));
        gc.fillText("x (см)", graphX + graphW - 30, graphY + 15);
        gc.fillText("N(x)", graphX - 30, graphY - graphH + 10);

        if (!dataPoints.isEmpty()) {
            dataPoints.sort(Comparator.comparingDouble(Measurement::getX));

            gc.setStroke(Color.web("#00ffcc"));
            gc.setLineWidth(2.0);
            gc.setFill(Color.web("#ff007f"));

            int maxN = 100;
            for (Measurement m : dataPoints) {
                if (m.getCounts() > maxN) maxN = m.getCounts();
            }
            double dynamicMaxN = maxN * 1.15;

            gc.beginPath();
            boolean first = true;

            for (Measurement m : dataPoints) {
                double px = graphX + (m.getX() / maxAxisX) * graphW;
                double py = graphY - ((double)m.getCounts() / dynamicMaxN) * graphH;

                if (first) {
                    gc.moveTo(px, py);
                    first = false;
                } else {
                    gc.lineTo(px, py);
                }
            }
            gc.stroke();

            for (Measurement m : dataPoints) {
                double px = graphX + (m.getX() / maxAxisX) * graphW;
                double py = graphY - ((double)m.getCounts() / dynamicMaxN) * graphH;
                gc.fillOval(px - 3, py - 3, 6, 6);
            }
        }

        double currentPx = graphX + (distanceX / maxAxisX) * graphW;
        if(currentPx <= graphX + graphW) {
            gc.setStroke(Color.web("#ffffff", 0.3));
            gc.setLineDashes(4, 4);
            gc.strokeLine(currentPx, graphY, currentPx, graphY - graphH);
            gc.setLineDashes(null);
        }
    }
}