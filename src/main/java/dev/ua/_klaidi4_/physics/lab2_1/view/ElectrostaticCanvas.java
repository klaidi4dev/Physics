package dev.ua._klaidi4_.physics.lab2_1.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class ElectrostaticCanvas extends Canvas {

    private boolean isScanning = false;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double simTime = 0;

    private double currentAngleRad = 0;
    private double equipotentialRadius = 50;

    private List<Point> drawnPoints = new ArrayList<>();
    private Runnable onFinishCallback;

    private static class Point {
        double x, y;
        Point(double x, double y) { this.x = x; this.y = y; }
    }

    public ElectrostaticCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void setOnFinishCallback(Runnable cb) {
        this.onFinishCallback = cb;
    }

    public void startSimulation(double phi, double U) {
        // Радіус залежить від різниці потенціалів
        double deltaPhi = U - phi;
        this.equipotentialRadius = 25 + deltaPhi * 12;

        this.currentAngleRad = 0;
        this.simTime = 0;
        this.isScanning = true;

        if (timer != null) timer.stop();
        startAnimation();
    }

    // --- ОСЬ ЦЕЙ МЕТОД БРАКУВАЛО ---
    public void stopAnimation() {
        this.isScanning = false;
        if (timer != null) timer.stop();
    }
    // -------------------------------

    public void clearCanvas() {
        drawnPoints.clear();
        draw();
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                if (dt > 0.05) dt = 0.05;

                update(dt);
                draw();
            }
        };
        timer.start();
    }

    private void update(double dt) {
        if (isScanning) {
            simTime += dt;

            // Зонд робить коло за 2 секунди
            currentAngleRad = (simTime / 2.0) * (2 * Math.PI);

            if (simTime >= 2.0) {
                isScanning = false;
                timer.stop();
                if (onFinishCallback != null) onFinishCallback.run();
            }
        }
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        // Папір
        gc.setFill(Color.web("#ecf0f1"));
        gc.fillRect(0, 0, w, h);
        gc.setStroke(Color.web("#bdc3c7"));
        gc.setLineWidth(1);
        for (int i = 0; i < w; i += 20) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 20) gc.strokeLine(0, i, w, i);

        double cx = w / 2;
        double cy = h / 2;
        double electrodeDist = 100;
        double rPx = 15;

        // Лівий електрод (+)
        gc.setFill(Color.web("#e74c3c"));
        gc.fillOval(cx - electrodeDist - rPx, cy - rPx, rPx*2, rPx*2);
        gc.setFill(Color.WHITE);
        gc.fillText("+", cx - electrodeDist - 4, cy + 4);

        // Правий електрод (-)
        gc.setFill(Color.web("#3498db"));
        gc.fillOval(cx + electrodeDist - rPx, cy - rPx, rPx*2, rPx*2);
        gc.setFill(Color.WHITE);
        gc.fillText("-", cx + electrodeDist - 2, cy + 4);

        // Раніше знайдені точки
        gc.setFill(Color.web("#2c3e50"));
        for (Point p : drawnPoints) {
            gc.fillOval(p.x - 3, p.y - 3, 6, 6);
        }

        // Анімація зонда
        if (isScanning) {
            double probeX = cx - electrodeDist + equipotentialRadius * Math.cos(currentAngleRad);
            double probeY = cy + equipotentialRadius * Math.sin(currentAngleRad);

            // Дріт зонда
            gc.setStroke(Color.web("#f1c40f"));
            gc.setLineWidth(2);
            gc.strokeLine(cx - electrodeDist, cy, probeX, probeY);

            // Зонд
            gc.setFill(Color.web("#f39c12"));
            gc.fillOval(probeX - 5, probeY - 5, 10, 10);

            // Залишаємо точки кожні ~45 градусів
            if (drawnPoints.isEmpty() || Math.hypot(drawnPoints.get(drawnPoints.size()-1).x - probeX, drawnPoints.get(drawnPoints.size()-1).y - probeY) > 25) {
                drawnPoints.add(new Point(probeX, probeY));
            }
        }
    }
}