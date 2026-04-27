package dev.ua._klaidi4_.physics.level5.lab5_4.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class BiprismCanvas extends Canvas {

    private boolean isPowerOn = false;
    private boolean isLensInserted = false;
    private double lensX = 20.0;
    private double micrometerZ = 0.0;
    private double filterWavelength = 650.0;
    private final double L = 40.0;
    private final double l_true = 2.0;

    private AnimationTimer timer;
    private double time = 0;

    public BiprismCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    public void setPhysicsParameters(boolean power, boolean lens, double lx, double mz, double wave) {
        this.isPowerOn = power;
        this.isLensInserted = lens;
        this.lensX = lx;
        this.micrometerZ = mz;
        this.filterWavelength = wave;
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            private long lastTime = 0;
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

    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

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

        double eyeCenterX = w * 0.35;
        double eyeCenterY = h / 2;
        double eyeRadius = 140;

        gc.setFill(Color.web("#1e293b"));
        gc.fillOval(eyeCenterX - eyeRadius - 10, eyeCenterY - eyeRadius - 10, eyeRadius * 2 + 20, eyeRadius * 2 + 20);

        gc.setFill(isPowerOn ? Color.web("#f8fafc") : Color.web("#0f172a"));
        gc.fillOval(eyeCenterX - eyeRadius, eyeCenterY - eyeRadius, eyeRadius * 2, eyeRadius * 2);

        Color waveColor = getFilterColor(filterWavelength);

        if (isPowerOn) {
            gc.save();
            gc.beginPath();
            gc.arc(eyeCenterX, eyeCenterY, eyeRadius, eyeRadius, 0, 360);
            gc.clip();

            if (!isLensInserted) {
                double dy_fringe = (filterWavelength * L * 10) / (l_true * 1000000.0);
                double pixelsPerMm = eyeRadius / 2.5;

                for (int xPx = (int)(eyeCenterX - eyeRadius); xPx <= (int)(eyeCenterX + eyeRadius); xPx++) {
                    double z_mm = (xPx - eyeCenterX) / pixelsPerMm;
                    double intensity = Math.pow(Math.cos(Math.PI * z_mm / dy_fringe), 2);

                    gc.setStroke(Color.color(waveColor.getRed(), waveColor.getGreen(), waveColor.getBlue(), intensity * 0.8));
                    gc.setLineWidth(1.5);
                    gc.strokeLine(xPx, eyeCenterY - eyeRadius, xPx, eyeCenterY + eyeRadius);
                }
            } else {
                double pixelsPerMm = eyeRadius / 2.5;
                double blur = Math.abs(lensX - 20.0);

                double s1_px = eyeCenterX - (l_true / 2.0) * pixelsPerMm;
                double s2_px = eyeCenterX + (l_true / 2.0) * pixelsPerMm;

                drawSlitImage(gc, s1_px, eyeCenterY, eyeRadius, blur, waveColor);
                drawSlitImage(gc, s2_px, eyeCenterY, eyeRadius, blur, waveColor);
            }
            gc.restore();
        }

        double pixelsPerMm = eyeRadius / 2.5;
        double crosshairX = eyeCenterX + micrometerZ * pixelsPerMm;

        gc.setStroke(Color.web("#000000", 0.8));
        gc.setLineWidth(1.5);
        gc.strokeLine(eyeCenterX - eyeRadius, eyeCenterY, eyeCenterX + eyeRadius, eyeCenterY);
        gc.setStroke(Color.web("#ef4444"));
        gc.strokeLine(crosshairX, eyeCenterY - eyeRadius, crosshairX, eyeCenterY + eyeRadius);

        for (int i = -5; i <= 5; i++) {
            double tickX = eyeCenterX + i * pixelsPerMm;
            gc.setStroke(Color.web("#000000", 0.4));
            gc.strokeLine(tickX, eyeCenterY - 5, tickX, eyeCenterY + 5);
        }

        double sideX = w * 0.8;
        double stageY = h * 0.75;
        double scaleY = 10.0;

        gc.setFill(Color.web("#475569"));
        gc.fillRect(sideX - 40, stageY - (L * scaleY), 80, L * scaleY + 20);

        double topY = stageY - (L * scaleY);
        gc.setFill(Color.web("#334155"));
        gc.fillRect(sideX - 25, topY - 10, 50, 10);
        gc.setFill(Color.web("#81d4fa", 0.6));
        gc.fillPolygon(new double[]{sideX - 20, sideX + 20, sideX}, new double[]{topY, topY, topY + 15}, 3);

        if (isPowerOn) {
            gc.setStroke(Color.color(waveColor.getRed(), waveColor.getGreen(), waveColor.getBlue(), 0.4));
            gc.setLineWidth(2.0);
            gc.strokeLine(sideX, topY + 15, sideX - 15, stageY);
            gc.strokeLine(sideX, topY + 15, sideX + 15, stageY);
        }

        gc.setFill(Color.web("#94a3b8"));
        gc.fillRect(sideX - 20, stageY, 40, 20);

        if (isLensInserted) {
            double ly = stageY - ((40.0 - lensX) * scaleY);
            gc.setStroke(Color.web("#00e5ff"));
            gc.setLineWidth(3.0);
            gc.strokeLine(sideX - 20, ly, sideX + 20, ly);
            gc.strokeLine(sideX - 20, ly, sideX - 15, ly - 5);
            gc.strokeLine(sideX - 20, ly, sideX - 15, ly + 5);
            gc.strokeLine(sideX + 20, ly, sideX + 15, ly - 5);
            gc.strokeLine(sideX + 20, ly, sideX + 15, ly + 5);
        }
    }

    private void drawSlitImage(GraphicsContext gc, double x, double y, double r, double blur, Color color) {
        if (blur > 5.0) return;

        double opacity = Math.max(0.1, 1.0 - (blur / 5.0));
        double width = 3.0 + blur * 10.0;

        gc.setStroke(Color.color(color.getRed(), color.getGreen(), color.getBlue(), opacity));
        gc.setLineWidth(width);
        gc.strokeLine(x, y - r, x, y + r);
    }

    private Color getFilterColor(double wave) {
        if (wave > 600) return Color.web("#ff0033");
        if (wave > 500) return Color.web("#00ff66");
        return Color.web("#0066ff");
    }
}