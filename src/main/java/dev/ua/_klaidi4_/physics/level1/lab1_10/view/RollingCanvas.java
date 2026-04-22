package dev.ua._klaidi4_.physics.level1.lab1_10.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class RollingCanvas extends Canvas {

    private double angleDeg = 15;
    private int bodyTypeIndex = 0;
    private double currentDistance = 0;
    private double maxDistance = 400;
    private double currentRotation = 0;
    private boolean isRolling = false;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double simTime = 0;
    private double exactTime = 2.0;

    private Runnable onFinishCallback;

    public RollingCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void setParameters(int bodyTypeIndex, double angleDeg) {
        this.bodyTypeIndex = bodyTypeIndex;
        this.angleDeg = angleDeg;
        this.currentDistance = 0;
        this.currentRotation = 0;
        draw();
    }

    public void setOnFinishCallback(Runnable onFinishCallback) {
        this.onFinishCallback = onFinishCallback;
    }

    public void startSimulation(double calculatedTime) {
        this.exactTime = calculatedTime;
        this.simTime = 0;
        this.currentDistance = 0;
        this.currentRotation = 0;
        this.isRolling = true;

        if (timer != null) timer.stop();
        startAnimation();
    }

    public void stopAnimation() {
        this.isRolling = false;
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
        if (isRolling) {
            simTime += dt;

            double a = (2 * maxDistance) / (exactTime * exactTime);
            currentDistance = (a * simTime * simTime) / 2;

            double radiusPx = 25;
            currentRotation = currentDistance / radiusPx;

            if (simTime >= exactTime || currentDistance >= maxDistance) {
                currentDistance = maxDistance;
                isRolling = false;
                timer.stop();
                if (onFinishCallback != null) onFinishCallback.run();
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

        double startX = 50;
        double endX = startX + maxDistance * Math.cos(Math.toRadians(angleDeg)) + 50;
        double groundY = 350;

        double slopeStartY = groundY - (endX - startX) * Math.tan(Math.toRadians(angleDeg));
        gc.setFill(Color.web("#bdc3c7"));
        gc.fillRect(0, groundY, w, h - groundY);
        gc.setStroke(Color.web("#7f8c8d"));
        gc.setLineWidth(3);
        gc.strokeLine(0, groundY, w, groundY);
        gc.setFill(Color.web("#34495e"));
        gc.fillPolygon(new double[]{startX, endX, endX}, new double[]{slopeStartY, groundY, slopeStartY}, 3);
        gc.setStroke(Color.web("#2c3e50"));
        gc.setLineWidth(4);
        gc.strokeLine(startX, slopeStartY, endX, groundY);

        double radiusPx = 25;
        double bodyX = startX + currentDistance * Math.cos(Math.toRadians(angleDeg));
        double bodyY = slopeStartY + currentDistance * Math.sin(Math.toRadians(angleDeg)) - radiusPx;

        gc.save();
        gc.translate(bodyX, bodyY);
        gc.rotate(Math.toDegrees(currentRotation));

        if (bodyTypeIndex == 0) {
            RadialGradient grad = new RadialGradient(0, 0, 0, 0, radiusPx, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#95a5a6")), new Stop(1, Color.web("#2c3e50")));
            gc.setFill(grad);
            gc.fillOval(-radiusPx, -radiusPx, radiusPx*2, radiusPx*2);
            gc.setFill(Color.WHITE);
            gc.fillOval(radiusPx*0.4, -5, 10, 10);

        } else if (bodyTypeIndex == 1) {
            RadialGradient grad = new RadialGradient(0, 0, -10, -10, radiusPx, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.WHITE), new Stop(0.3, Color.web("#e74c3c")), new Stop(1, Color.web("#c0392b")));
            gc.setFill(grad);
            gc.fillOval(-radiusPx, -radiusPx, radiusPx*2, radiusPx*2);
            gc.setFill(Color.BLACK);
            gc.fillOval(-2, -2, 4, 4);

        } else if (bodyTypeIndex == 2) {
            gc.setStroke(Color.web("#d35400"));
            gc.setLineWidth(10);
            gc.strokeOval(-radiusPx+5, -radiusPx+5, radiusPx*2-10, radiusPx*2-10);
            gc.setStroke(Color.web("#7f8c8d"));
            gc.setLineWidth(2);
            gc.strokeLine(-radiusPx+5, 0, radiusPx-5, 0);
            gc.strokeLine(0, -radiusPx+5, 0, radiusPx-5);
        }

        gc.restore();
        gc.setStroke(Color.web("#27ae60"));
        gc.setLineWidth(2);
        gc.setLineDashes(5);
        gc.strokeLine(startX, slopeStartY - 40, startX, slopeStartY);
        gc.strokeLine(startX + maxDistance * Math.cos(Math.toRadians(angleDeg)), groundY - 40, startX + maxDistance * Math.cos(Math.toRadians(angleDeg)), groundY);
        gc.setLineDashes(null);
    }
}