package dev.ua._klaidi4_.physics.level5.lab5_1.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class OpticsCanvas extends Canvas {

    private boolean isPowerOn = false;
    private double xSource = 0.0;
    private double xScreen = 100.0;
    private double xLens = 50.0;
    private double focalLength = 12.0;
    private double imagePosAbsolute = 0.0;
    private double sharpnessError = 0.0;

    private AnimationTimer timer;
    private double time = 0;

    public OpticsCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    public void setPhysicsParameters(boolean power, double xSrc, double xScr, double xLns, double f) {
        this.isPowerOn = power;
        this.xSource = xSrc;
        this.xScreen = xScr;
        this.xLens = xLns;
        this.focalLength = f;
        calculateOptics();
    }

    private void calculateOptics() {
        if (!isPowerOn) return;

        double dObject = xLens - xSource;
        if (dObject <= 0) {
            sharpnessError = 1000;
            return;
        }

        if (Math.abs(dObject - focalLength) < 0.1) {
            imagePosAbsolute = 10000;
        } else {
            double fImage = (dObject * focalLength) / (dObject - focalLength);
            imagePosAbsolute = xLens + fImage;
        }

        sharpnessError = Math.abs(imagePosAbsolute - xScreen);
    }

    public double getSharpnessError() {
        return sharpnessError;
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

        double centerY = h / 2;
        double scale = (w - 60) / 130.0;
        double startX = 30.0;

        gc.setStroke(Color.web("#475569"));
        gc.setLineWidth(2.0);
        gc.setLineDashes(5, 5);
        gc.strokeLine(startX, centerY, startX + 130 * scale, centerY);
        gc.setLineDashes(null);

        double pxSrc = startX + xSource * scale;
        double pxScr = startX + xScreen * scale;
        double pxLns = startX + xLens * scale;

        gc.setFill(Color.web("#334155"));
        gc.fillRect(pxSrc - 10, centerY - 15, 10, 30);
        double objH = 25.0;

        if (isPowerOn) {
            gc.setStroke(Color.web("#00ffcc"));
            gc.setLineWidth(3.0);
            gc.strokeLine(pxSrc, centerY, pxSrc, centerY - objH);
            gc.strokeLine(pxSrc, centerY - objH, pxSrc - 5, centerY - objH + 8);
            gc.strokeLine(pxSrc, centerY - objH, pxSrc + 5, centerY - objH + 8);
        }

        gc.setFill(Color.web("#e2e8f0"));
        gc.fillRect(pxScr - 3, centerY - 80, 6, 160);

        gc.setStroke(Color.web("#A155FF"));
        gc.setLineWidth(3.0);
        gc.strokeLine(pxLns, centerY - 60, pxLns, centerY + 60);
        gc.strokeLine(pxLns, centerY - 60, pxLns - 8, centerY - 50);
        gc.strokeLine(pxLns, centerY - 60, pxLns + 8, centerY - 50);
        gc.strokeLine(pxLns, centerY + 60, pxLns - 8, centerY + 50);
        gc.strokeLine(pxLns, centerY + 60, pxLns + 8, centerY + 50);

        if (isPowerOn && xLens > xSource + focalLength) {
            gc.setStroke(Color.web("#00e5ff", 0.6));
            gc.setLineWidth(1.5);

            double pxImg = startX + imagePosAbsolute * scale;
            double mag = -(imagePosAbsolute - xLens) / (xLens - xSource);
            double imgY = centerY - objH * mag;

            double hitY = centerY - objH;

            gc.strokeLine(pxSrc, hitY, pxLns, hitY);
            gc.strokeLine(pxSrc, hitY, pxLns, centerY);

            double slope1 = (imgY - hitY) / (pxImg - pxLns);
            double screenY1 = hitY + slope1 * (pxScr - pxLns);
            double slope2 = (imgY - centerY) / (pxImg - pxLns);
            double screenY2 = centerY + slope2 * (pxScr - pxLns);

            gc.strokeLine(pxLns, hitY, pxScr, screenY1);
            gc.strokeLine(pxLns, centerY, pxScr, screenY2);

            if (pxImg < pxScr) {
                gc.setStroke(Color.web("#ff007f", 0.4));
                gc.setLineDashes(3, 3);
                gc.strokeLine(pxImg, centerY, pxImg, imgY);
                gc.setLineDashes(null);
            }

            if (sharpnessError < 2.0) {
                gc.setStroke(Color.web("#00ffcc"));
                gc.setLineWidth(3.0);
                gc.strokeLine(pxScr, centerY, pxScr, imgY);
                gc.setFill(Color.web("#00ffcc", 0.7 + 0.3 * Math.sin(time * 15)));
                gc.fillOval(pxScr - 4, imgY - 4, 8, 8);
            } else {
                double spread = Math.abs(screenY1 - screenY2);
                if (spread < 80) {
                    double opacity = Math.max(0.1, 1.0 - (spread / 80.0));
                    gc.setFill(Color.web("#00e5ff", opacity * 0.6));
                    double midY = (screenY1 + screenY2) / 2.0;
                    gc.fillOval(pxScr - 3, midY - spread / 2.0, 6, spread);
                }
            }
        } else if (isPowerOn) {
            gc.setStroke(Color.web("#ff0000", 0.6));
            gc.strokeLine(pxSrc, centerY - objH, pxLns, centerY - objH);
            gc.strokeLine(pxSrc, centerY - objH, pxLns, centerY);
            gc.strokeLine(pxLns, centerY - objH, pxScr, centerY - objH - 20);
            gc.strokeLine(pxLns, centerY, pxScr, centerY + 20);
        }
    }
}