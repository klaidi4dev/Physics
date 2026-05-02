package dev.ua._klaidi4_.physics.level2.lab2_1.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ElectrostaticCanvas extends Canvas {

    private double currentU = 20.0;
    private double currentD = 20.0;
    private double currentR = 1.0;
    private int currentN = 4;

    private boolean isScanning = false;
    private double scanProgress = 0.0;
    private AnimationTimer timer;
    private long lastTime = 0;

    private Runnable onFinishCallback;

    public ElectrostaticCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void setOnFinishCallback(Runnable callback) {
        this.onFinishCallback = callback;
    }

    public void updatePhysicsParameters(double u, double d, double r, int n) {
        this.currentU = u;
        this.currentD = d;
        this.currentR = r;
        this.currentN = n;
        if (!isScanning) {
            draw();
        }
    }

    public void resetSystem() {
        this.isScanning = false;
        this.scanProgress = 0.0;
        if (timer != null) timer.stop();
        draw();
    }

    public void startSimulation() {
        resetSystem();
        this.isScanning = true;
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                scanProgress += dt * 0.4;
                if (scanProgress >= 1.0) {
                    scanProgress = 1.0;
                    isScanning = false;
                    this.stop();
                    if (onFinishCallback != null) onFinishCallback.run();
                }
                draw();
            }
        };
        timer.start();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#e0e0e0"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#9e9e9e"));
        gc.setLineWidth(1);
        for (int i = 0; i < w; i += 20) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 20) gc.strokeLine(0, i, w, i);

        double cx = w / 2;
        double cy = h / 2;
        double scale = 15.0;

        double dPx = currentD * scale;
        double rPx = currentR * scale;

        double x1 = cx - dPx / 2;
        double x2 = cx + dPx / 2;

        if (scanProgress > 0) {
            gc.setStroke(Color.web("#1976d2", 0.7));
            gc.setLineWidth(2);
            int linesCount = (int) (currentN * scanProgress);
            double step = (currentU / 2.0) / currentN;

            for (int i = 1; i <= linesCount; i++) {
                double targetV = i * step;

                double k = Math.exp((2 * targetV * Math.log(currentD / currentR)) / currentU);
                double a = currentD / 2.0;
                double xc = a * (k + 1) / (k - 1);
                double R = 2 * a * Math.sqrt(k) / Math.abs(k - 1);

                double drawXc1 = cx + xc * scale;
                double drawR1 = R * scale;
                gc.strokeOval(drawXc1 - drawR1, cy - drawR1, drawR1 * 2, drawR1 * 2);

                double drawXc2 = cx - xc * scale;
                gc.strokeOval(drawXc2 - drawR1, cy - drawR1, drawR1 * 2, drawR1 * 2);
            }
        }

        if (scanProgress >= 1.0) {
            gc.setStroke(Color.web("#d32f2f", 0.6));
            gc.setLineWidth(1.5);
            for (int i = -5; i <= 5; i++) {
                if (i == 0) {
                    gc.strokeLine(x1, cy, x2, cy);
                } else {
                    double arcR = Math.abs(i * 25);
                    double arcCy = cy + (i > 0 ? arcR : -arcR);
                    double angle = Math.atan2(dPx / 2, arcR);
                    gc.beginPath();
                    gc.arc(cx, arcCy, Math.sqrt((dPx / 2) * (dPx / 2) + arcR * arcR), Math.sqrt((dPx / 2) * (dPx / 2) + arcR * arcR),
                            (i > 0 ? 180 + Math.toDegrees(angle) : 180 - Math.toDegrees(angle)),
                            (i > 0 ? 180 - 2 * Math.toDegrees(angle) : -180 + 2 * Math.toDegrees(angle)));
                    gc.stroke();
                }
            }
        }

        gc.setFill(Color.web("#424242"));
        gc.fillOval(x1 - rPx, cy - rPx, rPx * 2, rPx * 2);
        gc.fillOval(x2 - rPx, cy - rPx, rPx * 2, rPx * 2);

        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 12));
        gc.fillText("-V", x1 - 7, cy + 4);
        gc.fillText("+V", x2 - 7, cy + 4);
    }
}