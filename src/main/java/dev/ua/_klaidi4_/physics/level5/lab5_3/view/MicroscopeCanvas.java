package dev.ua._klaidi4_.physics.level5.lab5_3.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class MicroscopeCanvas extends Canvas {

    private boolean isPowerOn = false;
    private double focusZ = 0.0;
    private double plateD = 4.0;
    private double trueN = 1.51;
    private final double topZ = 3.0;

    private AnimationTimer timer;
    private double time = 0;

    public MicroscopeCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    public void setPhysicsParameters(boolean power, double focusZ, double plateD, double trueN) {
        this.isPowerOn = power;
        this.focusZ = focusZ;
        this.plateD = plateD;
        this.trueN = trueN;
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

        if (isPowerOn) {
            gc.setFill(Color.web("#f8fafc"));
        } else {
            gc.setFill(Color.web("#0f172a"));
        }
        gc.fillOval(eyeCenterX - eyeRadius, eyeCenterY - eyeRadius, eyeRadius * 2, eyeRadius * 2);

        if (isPowerOn) {
            double apparentBottomZ = topZ + (plateD / trueN);
            drawScratch(gc, eyeCenterX, eyeCenterY, eyeRadius, focusZ - apparentBottomZ, Color.web("#1976d2"), true);
            drawScratch(gc, eyeCenterX, eyeCenterY, eyeRadius, focusZ - topZ, Color.web("#d32f2f"), false);
        }

        gc.setStroke(Color.web("#000000", 0.8));
        gc.setLineWidth(1.5);
        gc.strokeLine(eyeCenterX - eyeRadius, eyeCenterY, eyeCenterX + eyeRadius, eyeCenterY);
        gc.strokeLine(eyeCenterX, eyeCenterY - eyeRadius, eyeCenterX, eyeCenterY + eyeRadius);

        double sideX = w * 0.8;
        double stageY = h * 0.75;
        double scale = 15.0;

        gc.setFill(Color.web("#475569"));
        gc.fillRect(sideX - 50, stageY, 100, 15);

        double plateHPx = plateD * scale;
        gc.setFill(Color.web("#81d4fa", 0.4));
        gc.fillRect(sideX - 35, stageY - plateHPx, 70, plateHPx);
        gc.setStroke(Color.web("#0288d1"));
        gc.setLineWidth(2);
        gc.strokeRect(sideX - 35, stageY - plateHPx, 70, plateHPx);

        gc.setStroke(Color.web("#d32f2f"));
        gc.setLineWidth(3);
        gc.strokeLine(sideX - 10, stageY - plateHPx, sideX + 10, stageY - plateHPx);
        gc.setStroke(Color.web("#1976d2"));
        gc.strokeLine(sideX - 10, stageY, sideX + 10, stageY);

        double objY = stageY - plateHPx - ((10.0 - focusZ) * scale) - 20;

        gc.setFill(Color.web("#94a3b8"));
        gc.fillRect(sideX - 15, objY - 60, 30, 60);
        gc.setFill(Color.web("#64748b"));
        gc.fillPolygon(
                new double[]{sideX - 15, sideX + 15, sideX + 8, sideX - 8},
                new double[]{objY, objY, objY + 15, objY + 15},
                4
        );

        if (isPowerOn) {
            gc.setStroke(Color.web("#ffeb3b", 0.6));
            gc.setLineDashes(4, 4);
            gc.strokeLine(sideX, stageY + 15, sideX, objY + 15);
            gc.setLineDashes(null);
        }
    }

    private void drawScratch(GraphicsContext gc, double cx, double cy, double r, double diff, Color color, boolean rotate) {
        double blur = Math.abs(diff);
        if (blur > 1.5) return;

        double opacity = Math.max(0.1, 1.0 - (blur / 1.5));
        double width = 2.0 + blur * 15.0;
        double len = r * 0.5;

        gc.setStroke(Color.color(color.getRed(), color.getGreen(), color.getBlue(), opacity));
        gc.setLineWidth(width);

        if (rotate) {
            double offset = len * 0.707;
            gc.strokeLine(cx - offset, cy - offset, cx + offset, cy + offset);
            gc.strokeLine(cx - offset, cy + offset, cx + offset, cy - offset);
        } else {
            gc.strokeLine(cx - len, cy, cx + len, cy);
            gc.strokeLine(cx, cy - len, cx, cy + len);
        }
    }
}