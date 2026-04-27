package dev.ua._klaidi4_.physics.level3.lab3_3.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class FocusingCanvas extends Canvas {

    private double voltage = 1000.0;
    private double current = 0.0;
    private double nTurns = 2000.0;
    private double length = 0.15;
    private final double EM_THEORY = 1.7588e11;
    private final double MU_0 = 4 * Math.PI * 1e-7;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double phase = 0;

    public FocusingCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    public void setPhysicsParameters(double voltage, double current, double nTurns, double length) {
        this.voltage = voltage;
        this.current = current;
        this.nTurns = nTurns;
        this.length = length;
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                phase += dt * 50;
                draw();
            }
        };
        timer.start();
    }

    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#051e05"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#0a4a0a"));
        gc.setLineWidth(1);
        for (int i = 0; i < w; i += 30) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 30) gc.strokeLine(0, i, w, i);

        double originX = w / 2;
        double originY = h / 2;
        double radius = Math.min(w, h) / 2 - 20;

        gc.setStroke(Color.web("#1e5631"));
        gc.setLineWidth(3);
        gc.strokeOval(originX - radius, originY - radius, radius * 2, radius * 2);

        double B = MU_0 * nTurns * current;
        double velocity = Math.sqrt(2 * voltage * EM_THEORY);
        double tFlight = length / velocity;
        double omegaC = B * EM_THEORY;

        double theta = omegaC * tFlight;

        double scale;
        if (Math.abs(theta) < 1e-6) {
            scale = 1.0;
        } else {
            scale = Math.sin(theta / 2.0) / (theta / 2.0);
        }

        double rotationAngle = theta / 2.0;

        gc.save();
        gc.translate(originX, originY);
        gc.rotate(Math.toDegrees(rotationAngle));

        double baseLength = radius * 0.7;
        double currentLength = baseLength * Math.abs(scale);

        gc.setStroke(Color.web("#00ff00"));
        gc.setLineWidth(3);

        for (int i = 0; i < 5; i++) {
            gc.setGlobalAlpha(0.2 + i * 0.15);
            gc.setLineWidth(5 - i);
            gc.strokeLine(0, -currentLength, 0, currentLength);
        }

        gc.setGlobalAlpha(1.0);
        gc.setFill(Color.web("#ccffcc"));
        gc.fillOval(-2, -2, 4, 4);

        gc.restore();
    }
}