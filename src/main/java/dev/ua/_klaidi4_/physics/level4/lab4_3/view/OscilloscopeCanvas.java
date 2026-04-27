package dev.ua._klaidi4_.physics.level4.lab4_3.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class OscilloscopeCanvas extends Canvas {

    private String mode = "beats";
    private double f1 = 40.0;
    private double f2 = 42.0;
    private double a1 = 1.0;
    private double a2 = 1.0;
    private double phaseDeg = 0.0;
    private boolean isMeasuring = false;
    private double measureDelay = 0;
    private Runnable onReadyCallback;

    private AnimationTimer timer;
    private double timeOffset = 0;

    public OscilloscopeCanvas(double width, double height) {
        super(width, height);
        startRenderLoop();
    }

    public void setOnReadyCallback(Runnable cb) {
        this.onReadyCallback = cb;
    }

    // Миттєве оновлення з урахуванням амплітуд
    public void updatePreview(String mode, double f1, double f2, double a1, double a2, double phaseDeg) {
        if (isMeasuring) return;
        this.mode = mode;
        this.f1 = f1;
        this.f2 = f2;
        this.a1 = a1;
        this.a2 = a2;
        this.phaseDeg = phaseDeg;
    }

    public void startMeasurement(String mode, double f1, double f2, double a1, double a2, double phaseDeg) {
        this.mode = mode;
        this.f1 = f1;
        this.f2 = f2;
        this.a1 = a1;
        this.a2 = a2;
        this.phaseDeg = phaseDeg;

        this.isMeasuring = true;
        this.measureDelay = 1.2;
    }

    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    private void startRenderLoop() {
        timer = new AnimationTimer() {
            private long lastTime = 0;

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
        timeOffset += dt * 0.5;

        if (isMeasuring) {
            measureDelay -= dt;
            if (measureDelay <= 0) {
                isMeasuring = false;
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

        gc.setFill(Color.web("#eef2f5"));
        gc.fillRect(0, 0, w, h);

        double oscX = 30;
        double oscY = 30;
        double oscW = w - 60;
        double oscH = h - 60;

        gc.setFill(Color.web("#2c3e50"));
        gc.fillRoundRect(oscX, oscY, oscW, oscH, 15, 15);
        gc.setFill(Color.web("#34495e"));
        gc.fillRoundRect(oscX + oscW - 120, oscY, 120, oscH, 15, 15);
        gc.setFill(Color.web("#7f8c8d"));
        gc.fillOval(oscX + oscW - 90, oscY + 50, 40, 40);
        gc.fillOval(oscX + oscW - 90, oscY + 120, 40, 40);
        gc.fillOval(oscX + oscW - 90, oscY + 190, 40, 40);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", 10));
        gc.fillText("VOLTS/DIV", oscX + oscW - 95, oscY + 40);
        gc.fillText("TIME/DIV", oscX + oscW - 93, oscY + 110);
        gc.fillText("POSITION", oscX + oscW - 95, oscY + 180);

        double screenX = oscX + 20;
        double screenY = oscY + 20;
        double screenW = oscW - 160;
        double screenH = oscH - 40;

        gc.setFill(Color.web("#001a00"));
        gc.fillRect(screenX, screenY, screenW, screenH);

        gc.setStroke(Color.web("#004d00"));
        gc.setLineWidth(1);
        for(int i = 0; i < screenW; i += 30) gc.strokeLine(screenX + i, screenY, screenX + i, screenY + screenH);
        for(int i = 0; i < screenH; i += 30) gc.strokeLine(screenX, screenY + i, screenX + screenW, screenY + i);

        gc.setStroke(Color.web("#008000"));
        gc.setLineWidth(2);
        gc.strokeLine(screenX, screenY + screenH/2, screenX + screenW, screenY + screenH/2);
        gc.strokeLine(screenX + screenW/2, screenY, screenX + screenW/2, screenY + screenH);

        gc.setStroke(Color.web("#00ff00"));
        gc.setLineWidth(2.5);
        gc.beginPath();

        if (mode.equals("beats")) {
            drawBeats(gc, screenX, screenY, screenW, screenH);
        } else {
            drawLissajous(gc, screenX, screenY, screenW, screenH);
        }

        gc.stroke();

        if (isMeasuring) {
            double scanX = screenX + (1.2 - measureDelay) / 1.2 * screenW;
            if (scanX > screenX && scanX < screenX + screenW) {
                gc.setStroke(Color.rgb(255, 255, 255, 0.5));
                gc.setLineWidth(4);
                gc.strokeLine(scanX, screenY, scanX, screenY + screenH);
            }
        }

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText(mode.equals("beats") ? "РЕЖИМ: БИТТЯ (Y = Y1 + Y2)" : "РЕЖИМ: ФІГУРИ ЛІССАЖУ", screenX + 10, screenY + 20);
    }

    private void drawBeats(GraphicsContext gc, double sx, double sy, double sw, double sh) {
        double fb = Math.abs(f1 - f2);
        double windowTime = (fb > 0.5) ? (2.0 / fb) : 1.0;

        double midY = sy + sh / 2;
        double maxAmp = Math.max(0.1, Math.abs(a1) + Math.abs(a2));
        double ampPx = (sh / 2.2) / maxAmp;

        for (double px = 0; px <= sw; px += 1) {
            double t = (px / sw) * windowTime + timeOffset;
            double yVal = a1 * Math.sin(2 * Math.PI * f1 * t) + a2 * Math.sin(2 * Math.PI * f2 * t);
            double py = midY - (yVal * ampPx);

            if (px == 0) gc.moveTo(sx + px, py);
            else gc.lineTo(sx + px, py);
        }
    }

    private void drawLissajous(GraphicsContext gc, double sx, double sy, double sw, double sh) {
        double midX = sx + sw / 2;
        double midY = sy + sh / 2;

        double basePx = Math.min(sw, sh) / 2.5;
        double maxAmp = Math.max(0.1, Math.max(Math.abs(a1), Math.abs(a2)));
        double scalePx = basePx / maxAmp;

        double phaseRad = Math.toRadians(phaseDeg);
        int steps = 1000;
        double maxTime = 1.0;

        for (int i = 0; i <= steps; i++) {
            double t = (i / (double) steps) * maxTime;

            double xVal = a1 * Math.sin(2 * Math.PI * f1 * t);
            double yVal = a2 * Math.sin(2 * Math.PI * f2 * t + phaseRad);
            double px = midX + (xVal * scalePx);
            double py = midY - (yVal * scalePx);

            if (i == 0) gc.moveTo(px, py);
            else gc.lineTo(px, py);
        }
    }
}