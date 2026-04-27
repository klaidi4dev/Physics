package dev.ua._klaidi4_.physics.level3.lab3_2.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class TangentGalvanometerCanvas extends Canvas {

    private double earthH0 = 16.5;
    private int turnsN = 100;
    private double currentI = 0.0;
    private double radiusR = 0.1;
    private double targetAngle = 0;
    private double currentAngle = 0;
    private double needleVelocity = 0;
    private double measuredAngle = 0;

    private boolean isRunning = false;

    private AnimationTimer timer;
    private long lastTime = 0;
    private Runnable onFinish;

    public TangentGalvanometerCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void setEarthField(double h0) {
        this.earthH0 = h0;
    }

    public void setParameters(int n, double current, double r, int polarity) {
        this.turnsN = n;
        this.currentI = current * polarity;
        this.radiusR = r;
        if (!isRunning) draw();
    }

    public void setCallbacks(Runnable onFinish) {
        this.onFinish = onFinish;
    }

    public void startSimulation() {
        double Hm = (turnsN * currentI) / (2.0 * radiusR);
        targetAngle = Math.toDegrees(Math.atan2(Hm, earthH0));
        double frictionNoise = (Math.random() - 0.5) * 0.6;
        targetAngle += frictionNoise;
        this.measuredAngle = targetAngle;
        needleVelocity += (Math.random() - 0.5) * 40.0;

        this.isRunning = true;
        this.lastTime = 0;

        if (timer != null) timer.stop();
        startAnimation();
    }

    public double getMeasuredAngle() {
        return measuredAngle;
    }

    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                if (dt > 0.05) dt = 0.05;
                if (isRunning) updatePhysics(dt);
                draw();
            }
        };
        timer.start();
    }

    private void updatePhysics(double dt) {
        double springConstant = 40.0;
        double damping = 3.5;

        double acceleration = springConstant * (targetAngle - currentAngle) - damping * needleVelocity;
        needleVelocity += acceleration * dt;
        currentAngle += needleVelocity * dt;

        if (Math.abs(targetAngle - currentAngle) < 0.1 && Math.abs(needleVelocity) < 0.1) {
            currentAngle = targetAngle;
            isRunning = false;
            if (onFinish != null) onFinish.run();
            timer.stop();
        }
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#e0e0e0"));
        gc.fillRect(0, 0, w, h);

        double centerX = w / 2;
        double centerY = h / 2;

        double coilVisualRadius = 160;
        gc.setStroke(Color.web("#cddc39"));
        gc.setLineWidth(12);
        gc.strokeOval(centerX - coilVisualRadius, centerY - coilVisualRadius, coilVisualRadius * 2, coilVisualRadius * 2);

        gc.setStroke(Color.web("#757575"));
        gc.setLineWidth(4);
        gc.strokeOval(centerX - coilVisualRadius - 8, centerY - coilVisualRadius - 8, (coilVisualRadius + 8) * 2, (coilVisualRadius + 8) * 2);
        gc.strokeOval(centerX - coilVisualRadius + 8, centerY - coilVisualRadius + 8, (coilVisualRadius - 8) * 2, (coilVisualRadius - 8) * 2);

        double compassRadius = 100;
        gc.setFill(Color.WHITE);
        gc.fillOval(centerX - compassRadius, centerY - compassRadius, compassRadius * 2, compassRadius * 2);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeOval(centerX - compassRadius, centerY - compassRadius, compassRadius * 2, compassRadius * 2);

        gc.setFont(Font.font("System", FontWeight.BOLD, 14));
        gc.setTextAlign(TextAlignment.CENTER);

        for (int i = 0; i < 360; i += 5) {
            double angleRad = Math.toRadians(i - 90);
            double length = (i % 30 == 0) ? 10 : (i % 10 == 0) ? 7 : 4;

            double x1 = centerX + compassRadius * Math.cos(angleRad);
            double y1 = centerY + compassRadius * Math.sin(angleRad);
            double x2 = centerX + (compassRadius - length) * Math.cos(angleRad);
            double y2 = centerY + (compassRadius - length) * Math.sin(angleRad);

            gc.setStroke((i == 0 || i == 180) ? Color.RED : Color.BLACK);
            gc.setLineWidth((i % 30 == 0) ? 2 : 1);
            gc.strokeLine(x1, y1, x2, y2);

            if (i % 30 == 0) {
                int labelVal = i <= 90 ? i : i <= 180 ? 180 - i : i <= 270 ? i - 180 : 360 - i;
                double tx = centerX + (compassRadius - 22) * Math.cos(angleRad);
                double ty = centerY + (compassRadius - 22) * Math.sin(angleRad) + 5;
                gc.setFill(Color.BLACK);
                gc.fillText(String.valueOf(labelVal), tx, ty);
            }
        }

        gc.setFill(Color.RED);
        gc.fillText("Пн (С)", centerX, centerY - compassRadius - 5);

        gc.save();
        gc.translate(centerX, centerY);
        gc.rotate(currentAngle);

        gc.setFill(Color.RED);
        gc.fillPolygon(new double[]{-4, 4, 0}, new double[]{0, 0, -compassRadius + 30}, 3);
        gc.setFill(Color.BLUE);
        gc.fillPolygon(new double[]{-4, 4, 0}, new double[]{0, 0, compassRadius - 30}, 3);

        gc.setFill(Color.GOLD);
        gc.fillOval(-4, -4, 8, 8);

        gc.restore();
    }
}