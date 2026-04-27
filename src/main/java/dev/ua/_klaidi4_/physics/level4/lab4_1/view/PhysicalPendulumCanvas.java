package dev.ua._klaidi4_.physics.level4.lab4_1.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;

import java.util.function.BiConsumer;

public class PhysicalPendulumCanvas extends Canvas {

    private String axis = "N";
    private double xD_cm = 30;
    private double mRod = 1.0;
    private double mD = 1.5;

    private boolean isSwinging = false;
    private AnimationTimer timer;
    private long lastTime = 0;

    private double simTime = 0;
    private double period = 1.0;
    private int targetOscillations = 10;
    private int currentOscillations = 0;
    private double currentAngleRad = 0;
    private double maxAngleRad = Math.toRadians(8);

    private Runnable onReadyCallback;
    private BiConsumer<Double, Integer> timerUpdateCallback;

    public PhysicalPendulumCanvas(double width, double height) {
        super(width, height);
        drawIdle();
    }

    public void setOnReadyCallback(Runnable cb) {
        this.onReadyCallback = cb;
    }

    public void setTimerUpdateCallback(BiConsumer<Double, Integer> cb) {
        this.timerUpdateCallback = cb;
    }

    public void updatePreview(String axis, double xD_cm, double initAngleDeg, double mRod, double mD) {
        if (isSwinging) return;

        this.axis = axis;
        this.xD_cm = xD_cm;
        this.maxAngleRad = Math.toRadians(initAngleDeg);
        this.mRod = mRod;
        this.mD = mD;

        drawIdle();
    }

    public void startSimulation(String axis, double xD_cm, int n, double period, double initAngleDeg, double mRod, double mD) {
        this.axis = axis;
        this.xD_cm = xD_cm;
        this.targetOscillations = n;
        this.period = period;
        this.maxAngleRad = Math.toRadians(initAngleDeg);
        this.mRod = mRod;
        this.mD = mD;

        this.simTime = 0;
        this.currentOscillations = 0;
        this.currentAngleRad = maxAngleRad;
        this.isSwinging = true;

        if (timer != null) timer.stop();
        startRenderLoop();
    }

    public void stopAnimation() {
        this.isSwinging = false;
        if (timer != null) timer.stop();
        drawIdle();
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
        if (!isSwinging) return;

        simTime += dt;

        currentAngleRad = maxAngleRad * Math.cos((2 * Math.PI / period) * simTime);

        int newOsc = (int) (simTime / period);
        if (newOsc > currentOscillations && newOsc <= targetOscillations) {
            currentOscillations = newOsc;
        }

        if (timerUpdateCallback != null) {
            timerUpdateCallback.accept(simTime, currentOscillations);
        }

        if (simTime >= period * targetOscillations) {
            isSwinging = false;
            currentAngleRad = maxAngleRad;
            timer.stop();
            if (timerUpdateCallback != null) timerUpdateCallback.accept(period * targetOscillations, targetOscillations);
            if (onReadyCallback != null) onReadyCallback.run();
        }
    }

    private void drawIdle() {
        currentAngleRad = maxAngleRad;
        draw();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#eef2f5"));
        gc.fillRect(0, 0, w, h);
        gc.setStroke(Color.web("#d2dce6"));
        gc.setLineWidth(1);
        for (int i = 0; i < w; i += 40) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 40) gc.strokeLine(0, i, w, i);

        double pivotX = w / 2;
        double pivotY = 80;

        gc.setStroke(Color.web("#22c55e"));
        gc.setLineWidth(1.5);
        gc.setLineDashes(6, 6);
        gc.strokeLine(pivotX, pivotY, pivotX, h - 30);
        gc.setLineDashes(null);

        gc.setFill(Color.web("#166534"));
        gc.setFont(Font.font("System", 11));
        gc.fillText("Лінія рівноваги", pivotX + 5, h - 35);

        gc.setFill(Color.web("#475569"));
        gc.fillRoundRect(pivotX - 30, pivotY - 15, 60, 15, 5, 5);
        gc.setFill(Color.web("#94a3b8"));
        gc.fillPolygon(new double[]{pivotX - 10, pivotX + 10, pivotX}, new double[]{pivotY, pivotY, pivotY - 15}, 3);

        gc.save();
        gc.translate(pivotX, pivotY);
        gc.rotate(Math.toDegrees(currentAngleRad));

        double scale = 3.0;
        double topOffsetCm = axis.equals("N") ? -10.0 : -20.0;
        double rodLengthCm = 100.0;
        double rodY = topOffsetCm * scale;
        double rodH = rodLengthCm * scale;
        double rodW = 8 + (mRod * 4);

        LinearGradient gradRod = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#d1d5db")), new Stop(0.5, Color.web("#f3f4f6")), new Stop(1, Color.web("#9ca3af")));
        gc.setFill(gradRod);
        gc.fillRoundRect(-rodW/2, rodY, rodW, rodH, 6, 6);
        gc.setStroke(Color.web("#6b7280"));
        gc.setLineWidth(1);
        gc.strokeRoundRect(-rodW/2, rodY, rodW, rodH, 6, 6);

        gc.setStroke(Color.BLACK);
        gc.setFont(Font.font("System", 9));
        for (int i = 0; i <= 100; i += 10) {
            double tickCm = axis.equals("N") ? (i - 10) : (80 - i);
            double tickY = tickCm * scale;
            gc.strokeLine(-rodW/2, tickY, 0, tickY);
            if (i % 20 == 0) {
                gc.setFill(Color.BLACK);
                gc.fillText(String.valueOf(i), rodW/2 + 2, tickY + 3);
            }
        }

        java.util.function.Function<Double, Double> getLocalY = (cm) -> {
            return (axis.equals("N") ? (cm - 10) : (80 - cm)) * scale;
        };

        double nY = getLocalY.apply(10.0);
        gc.setFill(Color.RED);
        gc.fillPolygon(new double[]{-rodW/2-8, -rodW/2, -rodW/2-8}, new double[]{nY-6, nY, nY+6}, 3);
        gc.fillPolygon(new double[]{rodW/2+8, rodW/2, rodW/2+8}, new double[]{nY-6, nY, nY+6}, 3);
        gc.fillText("N", -25 - rodW/2, nY + 4);

        double nPrimeY = getLocalY.apply(80.0);
        gc.setFill(Color.RED);
        gc.fillPolygon(new double[]{-rodW/2-8, -rodW/2, -rodW/2-8}, new double[]{nPrimeY-6, nPrimeY, nPrimeY+6}, 3);
        gc.fillPolygon(new double[]{rodW/2+8, rodW/2, rodW/2+8}, new double[]{nPrimeY-6, nPrimeY, nPrimeY+6}, 3);
        gc.fillText("N'", -28 - rodW/2, nPrimeY + 4);

        double bY = getLocalY.apply(60.0);
        double bSize = 30;
        gc.setFill(Color.web("#3b82f6"));
        gc.fillOval(-bSize/2, bY - bSize/2, bSize, bSize);
        gc.setStroke(Color.web("#1e3a8a"));
        gc.setLineWidth(2);
        gc.strokeOval(-bSize/2, bY - bSize/2, bSize, bSize);
        gc.setFill(Color.WHITE);
        gc.fillText("B", -3, bY + 4);

        double dY = getLocalY.apply(xD_cm);
        double dSize = 15 + (mD * 10);

        gc.setFill(Color.web("#f59e0b"));
        gc.fillOval(-dSize/2, dY - dSize/2, dSize, dSize);
        gc.setStroke(Color.web("#78350f"));
        gc.setLineWidth(2);
        gc.strokeOval(-dSize/2, dY - dSize/2, dSize, dSize);
        gc.setFill(Color.BLACK);
        gc.fillText("D", -4, dY + 4);

        gc.restore();

        gc.setFill(Color.BLACK);
        gc.fillOval(pivotX - 3, pivotY - 3, 6, 6);
    }
}