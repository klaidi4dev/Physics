package dev.ua._klaidi4_.physics.level1.lab1_5.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ChronometerCanvas extends Canvas {

    private double length = 200;
    private double maxAngle = 0;
    private double currentAngle = 0;
    private double sphereRadius = 15;
    private double n0Value = 100;
    private double nValue = 100;
    private double currentN = 100;
    private boolean isSwinging = false;
    private boolean isHit = false;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double swingTime = 0;
    private Runnable onHitCallback;
    private Runnable onFinishCallback;

    public ChronometerCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void setCallbacks(Runnable onHit, Runnable onFinish) {
        this.onHitCallback = onHit;
        this.onFinishCallback = onFinish;
    }

    public void startSimulation(double dh, double n0, double n) {
        double h = dh * 500;
        if (h > length) h = length;
        this.maxAngle = -Math.acos(1.0 - (h / length));
        this.currentAngle = maxAngle;
        this.n0Value = n0;
        this.nValue = n;
        this.currentN = n0;
        this.swingTime = 0;
        this.isSwinging = true;
        this.isHit = false;

        if (timer != null) timer.stop();
        startAnimation();
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

                update(dt);
                draw();
            }
        };
        timer.start();
    }

    private void update(double dt) {
        if (isSwinging) {
            swingTime += dt;
            double omega = Math.sqrt(9.81 / (length / 100.0));
            currentAngle = maxAngle * Math.cos(omega * swingTime);

            if (currentAngle >= 0) {
                currentAngle = 0;
                isSwinging = false;
                isHit = true;
                if (onHitCallback != null) onHitCallback.run();
            }
        } else if (isHit) {
            if (currentN > nValue) {
                currentN -= 50 * dt;
                if (currentN <= nValue) {
                    currentN = nValue;
                    timer.stop();
                    if (onFinishCallback != null) onFinishCallback.run();
                }
            }
        }
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, w, h);
        gc.setStroke(Color.web("#e9ecef"));
        gc.setLineWidth(1);
        for (int i = 0; i < w; i += 20) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 20) gc.strokeLine(0, i, w, i);

        double originX = w / 2 - 40;
        double originY = 50;

        gc.setFill(Color.DARKGRAY);
        gc.fillRect(originX - 30, originY - 10, 100, 10);

        double rightBobX = originX + sphereRadius;
        double rightBobY = originY + length;
        gc.setStroke(Color.web("#34495e"));
        gc.setLineWidth(2);
        gc.strokeLine(originX + sphereRadius, originY, rightBobX, rightBobY);
        drawSphere(gc, rightBobX, rightBobY, sphereRadius);

        double leftBobX = originX - sphereRadius + length * Math.sin(currentAngle);
        double leftBobY = originY + length * Math.cos(currentAngle);
        gc.strokeLine(originX - sphereRadius, originY, leftBobX, leftBobY);
        drawSphere(gc, leftBobX, leftBobY, sphereRadius);

        if (isHit && currentN > nValue) {
            gc.setStroke(Color.web("#f1c40f"));
            gc.setLineWidth(3);
            gc.strokeLine(leftBobX + sphereRadius, leftBobY, rightBobX - sphereRadius, rightBobY);
            gc.setFill(Color.ORANGE);
            gc.fillOval(originX - 5, rightBobY - 5, 10, 10);
        }

        drawGalvanometer(gc, w - 180, h - 160);
    }

    private void drawSphere(GraphicsContext gc, double x, double y, double r) {
        RadialGradient grad = new RadialGradient(0, 0, x - r * 0.3, y - r * 0.3, r,
                false, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(1, Color.web("#7f8c8d")));
        gc.setFill(grad);
        gc.fillOval(x - r, y - r, r * 2, r * 2);
    }

    private void drawGalvanometer(GraphicsContext gc, double x, double y) {
        double width = 150;
        double height = 120;

        LinearGradient bg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ecf0f1")), new Stop(1, Color.web("#bdc3c7")));
        gc.setFill(bg);
        gc.fillRoundRect(x, y, width, height, 15, 15);
        gc.setStroke(Color.web("#95a5a6"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, width, height, 15, 15);

        gc.setFill(Color.WHITE);
        gc.fillRect(x + 10, y + 10, width - 20, 60);
        gc.strokeRect(x + 10, y + 10, width - 20, 60);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.fillText("ГАЛЬВАНОМЕТР", x + 25, y + 90);
        gc.setFont(Font.font("System", 10));
        gc.fillText("n: " + String.format("%.1f", currentN), x + 50, y + 105);

        double cx = x + width / 2;
        double cy = y + 65;
        double needleLength = 45;

        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);
        for (int i = 0; i <= 150; i += 30) {
            double angle = Math.PI - (i / 150.0) * Math.PI;
            double px1 = cx + (needleLength - 5) * Math.cos(angle);
            double py1 = cy - (needleLength - 5) * Math.sin(angle);
            double px2 = cx + needleLength * Math.cos(angle);
            double py2 = cy - needleLength * Math.sin(angle);
            gc.strokeLine(px1, py1, px2, py2);
        }

        double angle = Math.PI - (currentN / 150.0) * Math.PI;
        double nx = cx + needleLength * Math.cos(angle);
        double ny = cy - needleLength * Math.sin(angle);

        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        gc.strokeLine(cx, cy, nx, ny);
        gc.setFill(Color.BLACK);
        gc.fillOval(cx - 3, cy - 3, 6, 6);
    }
}