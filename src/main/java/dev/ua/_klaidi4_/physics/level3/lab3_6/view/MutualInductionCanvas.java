package dev.ua._klaidi4_.physics.level3.lab3_6.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MutualInductionCanvas extends Canvas {

    private double currentZ = 0;
    private double targetZ = 0;
    private double currentU0 = 0;
    private double currentF = 0;
    private double currentEpsAmp = 0;
    private boolean isAnimating = false;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double oscPhase = 0;
    private double measureDelay = 0;

    private Runnable onReadyCallback;

    public MutualInductionCanvas(double width, double height) {
        super(width, height);
        startRenderLoop();
    }

    public void setOnReadyCallback(Runnable cb) {
        this.onReadyCallback = cb;
    }

    public void animateDevices(double zCm, double u0, double f, double eps) {
        this.targetZ = zCm;
        this.currentU0 = u0;
        this.currentF = f;
        this.currentEpsAmp = eps;
        this.measureDelay = 1.0;
        this.isAnimating = true;
    }

    public void resetToIdle() {
        this.currentEpsAmp = 0;
        this.currentU0 = 0;
        this.currentF = 0;
        this.targetZ = 0;
        this.currentZ = 0;
        this.isAnimating = false;
    }

    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    private void startRenderLoop() {
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
        double speed = (currentF > 0) ? (5 + (currentF / 2000.0)) : 0;
        oscPhase += dt * speed;

        if (isAnimating) {
            double dz = targetZ - currentZ;
            currentZ += dz * dt * 4;

            measureDelay -= dt;

            if (Math.abs(dz) < 0.05 && measureDelay <= 0) {
                currentZ = targetZ;
                isAnimating = false;
                if (onReadyCallback != null) {
                    onReadyCallback.run();
                    onReadyCallback = null;
                }
            }
        }
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#e0e6ed"));
        gc.fillRect(0, 0, w, h);
        gc.setStroke(Color.web("#cbd5e1"));
        gc.setLineWidth(1);
        for (int i = 0; i < w; i += 40) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 40) gc.strokeLine(0, i, w, i);

        drawAudioGenerator(gc, 20, 20);
        drawOscilloscope(gc, 360, 20);
        drawWires(gc);
        drawCoilsAndRuler(gc, w / 2, 320);
    }

    private void drawAudioGenerator(GraphicsContext gc, double x, double y) {
        double gw = 200;
        double gh = 120;

        gc.setFill(Color.web("#374151"));
        gc.fillRoundRect(x, y, gw, gh, 8, 8);
        gc.setFill(Color.web("#9ca3af"));
        gc.fillRect(x + 5, y + 25, gw - 10, gh - 30);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText("ГЕНЕРАТОР ЗЧ Г3-111", x + 30, y + 18);

        gc.setFill(Color.BLACK);
        gc.fillRect(x + 20, y + 40, 70, 30);
        gc.setFill(Color.RED);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        gc.fillText(String.format("%.1f V", currentU0), x + 25, y + 62);

        gc.setFill(Color.BLACK);
        gc.fillRect(x + 100, y + 40, 80, 30);
        gc.setFill(Color.RED);
        gc.fillText(String.format("%.0f Hz", currentF), x + 105, y + 62);

        gc.setFill(Color.web("#4b5563"));
        gc.fillOval(x + 35, y + 85, 25, 25);
        gc.fillOval(x + 125, y + 85, 25, 25);
        gc.setStroke(Color.WHITE);
        gc.strokeLine(x + 47.5, y + 85, x + 47.5, y + 97.5);
        gc.strokeLine(x + 137.5, y + 85, x + 137.5, y + 97.5);
    }

    private void drawOscilloscope(GraphicsContext gc, double x, double y) {
        double ow = 220;
        double oh = 160;

        gc.setFill(Color.web("#1e293b"));
        gc.fillRoundRect(x, y, ow, oh, 10, 10);

        double screenX = x + 15;
        double screenY = y + 15;
        double screenW = ow - 30;
        double screenH = oh - 45;

        gc.setFill(Color.web("#064e3b"));
        gc.fillRect(screenX, screenY, screenW, screenH);

        gc.setStroke(Color.web("#047857"));
        gc.setLineWidth(1);
        for(int i = 10; i < screenW; i += 20) gc.strokeLine(screenX + i, screenY, screenX + i, screenY + screenH);
        for(int i = 10; i < screenH; i += 20) gc.strokeLine(screenX, screenY + i, screenX + screenW, screenY + i);

        gc.setStroke(Color.web("#10b981"));
        gc.setLineWidth(2);
        gc.strokeLine(screenX, screenY + screenH/2, screenX + screenW, screenY + screenH/2);

        gc.setStroke(Color.web("#34d399"));
        gc.setLineWidth(2);
        gc.beginPath();

        double maxVisualAmp = (screenH / 2) - 5;
        double visualAmp = Math.min((currentEpsAmp / 0.1) * maxVisualAmp, maxVisualAmp);

        for (double sx = 0; sx < screenW; sx += 2) {
            double sy = screenY + screenH/2 - visualAmp * Math.sin(oscPhase + sx * 0.08);
            if (sx == 0) gc.moveTo(screenX + sx, sy);
            else gc.lineTo(screenX + sx, sy);
        }
        gc.stroke();

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText("ОСЦИЛОГРАФ С1-83", x + 50, y + oh - 10);
    }

    private void drawCoilsAndRuler(GraphicsContext gc, double centerX, double centerY) {
        double rulerWidth = 400;
        double rulerX = centerX - rulerWidth / 2;
        double rulerY = centerY + 40;

        gc.setFill(Color.web("#fef08a"));
        gc.fillRect(rulerX, rulerY, rulerWidth, 20);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.5);
        gc.strokeRect(rulerX, rulerY, rulerWidth, 20);

        gc.setFont(Font.font("System", 10));
        gc.setFill(Color.BLACK);
        for (int i = -15; i <= 15; i++) {
            double tickX = centerX + (i * 12);
            double tickLen = (i % 5 == 0) ? 8 : 4;
            gc.strokeLine(tickX, rulerY, tickX, rulerY + tickLen);
            if (i >= 0 && i % 5 == 0) {
                gc.fillText(String.valueOf(i), tickX - 3, rulerY + 18);
            }
        }
        gc.fillText("Z, см", centerX + (16 * 12), rulerY + 14);

        double l1Width = 360;
        double l1Height = 30;
        LinearGradient gradL1 = new LinearGradient(0, centerY - l1Height/2, 0, centerY + l1Height/2, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#d97706")), new Stop(0.5, Color.web("#f59e0b")), new Stop(1, Color.web("#b45309")));
        gc.setFill(gradL1);
        gc.fillRoundRect(centerX - l1Width/2, centerY - l1Height/2, l1Width, l1Height, 5, 5);

        gc.setStroke(Color.web("#78350f"));
        gc.setLineWidth(1.5);
        for (double x = centerX - l1Width/2 + 5; x < centerX + l1Width/2; x += 6) {
            gc.strokeLine(x, centerY - l1Height/2, x, centerY + l1Height/2);
        }

        double l2Width = 60;
        double l2Height = 45;
        double zPx = currentZ * 12;
        double l2X = centerX - l2Width/2 + zPx;

        LinearGradient gradL2 = new LinearGradient(0, centerY - l2Height/2, 0, centerY + l2Height/2, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#2563eb")), new Stop(0.5, Color.web("#60a5fa")), new Stop(1, Color.web("#1e3a8a")));
        gc.setFill(gradL2);
        gc.fillRoundRect(l2X, centerY - l2Height/2, l2Width, l2Height, 10, 10);

        gc.setStroke(Color.web("#172554"));
        gc.setLineWidth(2.5);
        for (double x = l2X + 8; x < l2X + l2Width - 5; x += 10) {
            gc.strokeLine(x, centerY - l2Height/2, x, centerY + l2Height/2);
        }

        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        gc.strokeLine(l2X + l2Width/2, centerY + l2Height/2, l2X + l2Width/2, rulerY);
    }

    private void drawWires(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(3);

        gc.beginPath();
        gc.moveTo(120, 140);
        gc.quadraticCurveTo(120, 320, 140, 320);
        gc.stroke();

        gc.setStroke(Color.web("#1e40af"));
        double l2X = (getWidth() / 2) + (currentZ * 12);
        gc.beginPath();
        gc.moveTo(470, 180);
        gc.quadraticCurveTo(470, 250, l2X, 295);
        gc.stroke();
    }
}