package dev.ua._klaidi4_.physics.level1.lab1_8.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class TorsionCanvas extends Canvas {

    private int configIndex = 0;
    private double currentAngle = 0;
    private double maxAngle = Math.PI / 2;
    private double r0 = 0.05;
    private double a = 0.1;
    private double b = 0.06;
    private double c = 0.04;

    private boolean isSwinging = false;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double swingTime = 0;
    private double exactPeriod = 2.0;

    public TorsionCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void setParameters(int configIndex, double r0, double a, double b, double c) {
        this.configIndex = configIndex;
        this.r0 = r0;
        this.a = a;
        this.b = b;
        this.c = c;
        this.currentAngle = 0;
        draw();
    }

    public void startSimulation(double period) {
        this.exactPeriod = period;
        this.swingTime = 0;
        this.isSwinging = true;

        if (timer != null) timer.stop();
        startAnimation();
    }

    public void stopAnimation() {
        this.isSwinging = false;
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
            currentAngle = maxAngle * Math.cos((2 * Math.PI / exactPeriod) * swingTime);
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

        double originX = w / 2;
        double originY = h / 2;
        double scale = 1200;

        gc.save();
        gc.translate(originX, originY);
        gc.rotate(Math.toDegrees(currentAngle));

        if (configIndex == 0) {
            double radius = r0 * scale;
            RadialGradient cylGrad = new RadialGradient(0, 0, 0, 0, radius,
                    false, CycleMethod.NO_CYCLE, new Stop(0, Color.web("#bdc3c7")), new Stop(1, Color.web("#7f8c8d")));
            gc.setFill(cylGrad);
            gc.fillOval(-radius, -radius, radius * 2, radius * 2);
            gc.setStroke(Color.web("#2c3e50"));
            gc.setLineWidth(3);
            gc.strokeOval(-radius, -radius, radius * 2, radius * 2);
        } else {
            double widthParam = 0;
            double heightParam = 0;
            if (configIndex == 1) { widthParam = b; heightParam = c; }
            else if (configIndex == 2) { widthParam = a; heightParam = c; }
            else if (configIndex == 3) { widthParam = a; heightParam = b; }

            double drawW = widthParam * scale;
            double drawH = heightParam * scale;

            gc.setFill(Color.web("#e67e22"));
            gc.fillRect(-drawW / 2, -drawH / 2, drawW, drawH);
            gc.setStroke(Color.web("#d35400"));
            gc.setLineWidth(3);
            gc.strokeRect(-drawW / 2, -drawH / 2, drawW, drawH);
        }

        gc.restore();
        gc.setFill(Color.BLACK);
        gc.fillOval(originX - 4, originY - 4, 8, 8);
        gc.setFill(Color.WHITE);
        gc.fillOval(originX - 1.5, originY - 1.5, 3, 3);
    }
}