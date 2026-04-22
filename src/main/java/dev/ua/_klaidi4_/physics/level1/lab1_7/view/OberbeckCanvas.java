package dev.ua._klaidi4_.physics.level1.lab1_7.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class OberbeckCanvas extends Canvas {

    private double r = 0.02;
    private double bigR = 0.15;
    private double h = 1.0;

    private double currentY = 0;
    private double currentAngle = 0;

    private boolean isRunning = false;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double simTime = 0;
    private double exactTime = 1.0;

    private Runnable onFinishCallback;

    public OberbeckCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void setParameters(double r, double bigR, double h) {
        this.r = r;
        this.bigR = bigR;
        this.h = h;
        this.currentY = 0;
        this.currentAngle = 0;
        draw();
    }

    public void setOnFinishCallback(Runnable onFinishCallback) {
        this.onFinishCallback = onFinishCallback;
    }

    public void startSimulation(double calculatedTime) {
        this.exactTime = calculatedTime;
        this.simTime = 0;
        this.currentY = 0;
        this.currentAngle = 0;
        this.isRunning = true;

        if (timer != null) timer.stop();
        startAnimation();
    }

    public void stopAnimation() {
        this.isRunning = false;
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
        if (isRunning) {
            simTime += dt;

            double a = (2 * h) / (exactTime * exactTime);
            currentY = (a * simTime * simTime) / 2;

            currentAngle = currentY / r;

            if (simTime >= exactTime || currentY >= h) {
                currentY = h;
                currentAngle = h / r;
                isRunning = false;
                timer.stop();
                if (onFinishCallback != null) onFinishCallback.run();
            }
        }
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double width = getWidth();
        double height = getHeight();

        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, width, height);
        gc.setStroke(Color.web("#e9ecef"));
        gc.setLineWidth(1);
        for (int i = 0; i < width; i += 20) gc.strokeLine(i, 0, i, height);
        for (int i = 0; i < height; i += 20) gc.strokeLine(0, i, width, i);

        double originX = width / 2;
        double originY = 120;
        double scaleY = 250;
        double scaleR = 400;
        double scaledPulleyR = 15;

        gc.setFill(Color.web("#7f8c8d"));
        gc.fillRect(originX - 10, originY, 20, height - originY);
        gc.fillRect(originX - 40, height - 15, 80, 15);
        gc.save();
        gc.translate(originX, originY);
        gc.rotate(Math.toDegrees(currentAngle));

        double armLen = 0.25 * scaleR;
        gc.setStroke(Color.web("#2c3e50"));
        gc.setLineWidth(4);
        gc.strokeLine(-armLen, 0, armLen, 0);
        gc.strokeLine(0, -armLen, 0, armLen);

        double weightD = 20;
        double drawBigR = bigR * scaleR;
        gc.setFill(Color.web("#e74c3c"));
        gc.fillRect(drawBigR - weightD/2, -weightD/2, weightD, weightD);
        gc.fillRect(-drawBigR - weightD/2, -weightD/2, weightD, weightD);
        gc.fillRect(-weightD/2, drawBigR - weightD/2, weightD, weightD);
        gc.fillRect(-weightD/2, -drawBigR - weightD/2, weightD, weightD);
        gc.restore();

        RadialGradient pulleyGrad = new RadialGradient(0, 0, originX, originY, scaledPulleyR,
                false, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(1, Color.web("#34495e")));
        gc.setFill(pulleyGrad);
        gc.fillOval(originX - scaledPulleyR, originY - scaledPulleyR, scaledPulleyR * 2, scaledPulleyR * 2);

        double stringX = originX + scaledPulleyR;
        double boxY = originY + currentY * scaleY;

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.5);
        gc.strokeLine(stringX, originY, stringX, boxY);

        gc.setFill(Color.web("#2980b9"));
        gc.fillRect(stringX - 10, boxY, 20, 25);

        double finishY = originY + h * scaleY + 25;
        gc.setStroke(Color.web("#27ae60"));
        gc.setLineWidth(3);
        gc.strokeLine(stringX - 25, finishY, stringX + 25, finishY);
    }
}