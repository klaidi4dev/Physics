package dev.ua._klaidi4_.physics.level4.lab4_5.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class StandingWaveCanvas extends Canvas {

    private boolean isGenerating = false;
    private double currentTime = 0;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double currentF = 0.0;
    private double tau = 0.002;
    private double cosPhi = 0.5;
    private double lActive = 1.0;
    private double targetFreq = 50.0;
    private int activeN = 0;
    private double currentAmp = 0;

    public StandingWaveCanvas(double width, double height) {
        super(width, height);
        drawFrame();
    }

    public void setPhysicsParameters(double f, double tau, double cosPhi, double lActive, double targetFreq) {
        this.currentF = f;
        this.tau = tau;
        this.cosPhi = cosPhi;
        this.lActive = lActive;
        this.targetFreq = targetFreq;
        calculateResonance();
        if (!isGenerating) drawFrame();
    }

    public void toggleGenerator(boolean state) {
        this.isGenerating = state;
        if (state) {
            if (timer != null) timer.stop();
            startAnimation();
        } else {
            if (timer != null) timer.stop();
            drawFrame();
        }
    }

    private void calculateResonance() {
        int bestN = 0;
        double maxAmp = 0;

        for (int i = 1; i <= 10; i++) {
            double fn = tau * (1 + cosPhi) * Math.pow((2 * lActive * targetFreq) / i, 2);
            double diffRatio = Math.abs(currentF - fn) / fn;
            double a = 60.0 * Math.exp(-diffRatio * 15.0);

            if (a > maxAmp) {
                maxAmp = a;
                bestN = i;
            }
        }
        this.activeN = bestN;
        this.currentAmp = maxAmp;
    }

    private void startAnimation() {
        lastTime = System.nanoTime();
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                currentTime += dt;
                drawFrame();
            }
        };
        timer.start();
    }

    public void stopAnimation() {
        if (timer != null) timer.stop();
        isGenerating = false;
        drawFrame();
    }

    public double getCurrentAmp() {
        return currentAmp;
    }

    public int getActiveN() {
        return activeN;
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
        double margin = 40;
        double stringLengthPx = w - 2 * margin;

        gc.setFill(Color.web("#475569"));
        gc.fillRect(margin - 10, centerY - 20, 10, 40);
        gc.fillOval(w - margin, centerY - 10, 20, 20);
        gc.setStroke(Color.web("#00e5ff"));
        gc.setLineWidth(3.5);
        gc.beginPath();
        gc.moveTo(margin, centerY);

        for (int i = 0; i <= 200; i++) {
            double xFraction = i / 200.0;
            double yOffset = 0;

            if (isGenerating) {
                if (currentAmp > 5.0) {
                    yOffset = currentAmp * Math.sin(activeN * Math.PI * xFraction) * Math.cos(currentTime * 30.0);
                } else {
                    yOffset = (Math.random() - 0.5) * 3.0 * Math.cos(currentTime * 30.0);
                }
            }
            gc.lineTo(margin + xFraction * stringLengthPx, centerY - yOffset);
        }
        gc.stroke();

        if (isGenerating && currentAmp > 15.0) {
            gc.setFill(Color.web("#ff007f"));
            for (int i = 0; i <= activeN; i++) {
                double nx = margin + (i / (double) activeN) * stringLengthPx;
                gc.fillOval(nx - 5, centerY - 5, 10, 10);
            }
        }
    }
}