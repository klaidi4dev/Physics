package dev.ua._klaidi4_.physics.level1.lab1_2.view;

import dev.ua._klaidi4_.physics.level1.lab1_2.enums.PendulumType;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class PendulumCanvas extends Canvas {

    private PendulumType type = PendulumType.MATHEMATICAL;
    private double length = 250;
    private double currentAngle = Math.PI / 6;
    private double startAngle = Math.PI / 6;
    private double angularVelocity = 0;
    private double gravity = 9.81;
    private double airFriction = 0.0;
    private double bobRadius = 15;
    private AnimationTimer timer;
    private long lastTime = 0;
    private boolean laserBroken = false;
    private double sensorFlashTimer = 0;

    public PendulumCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    public void setPhysicsParameters(PendulumType type, double physicalLength, double gravity, double angleDeg, double friction, double radius) {
        this.type = type;
        this.length = type == PendulumType.MATHEMATICAL ? physicalLength * 400 : physicalLength * 450;
        this.gravity = gravity;
        this.startAngle = Math.toRadians(angleDeg);
        this.airFriction = friction;
        this.bobRadius = radius;
        resetAngle();
    }

    public void resetAngle() {
        this.currentAngle = startAngle;
        this.angularVelocity = 0;
        this.laserBroken = false;
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                update(dt);
                draw();
            }
        };
        timer.start();
    }

    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    private void update(double dt) {
        double oldAngle = currentAngle;

        double angularAcceleration = -(gravity / (length / 100.0)) * Math.sin(currentAngle) - (airFriction * angularVelocity);
        angularVelocity += angularAcceleration * dt;
        currentAngle += angularVelocity * dt;

        if ((oldAngle > 0 && currentAngle <= 0) || (oldAngle < 0 && currentAngle >= 0)) {
            laserBroken = true;
            sensorFlashTimer = 0.15;
        }

        if (laserBroken) {
            sensorFlashTimer -= dt;
            if (sensorFlashTimer <= 0) laserBroken = false;
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
        double originY = 60;

        gc.setStroke(Color.web("#adb5bd"));
        gc.setLineWidth(1.5);
        gc.setLineDashes(10, 10);
        gc.strokeLine(originX, originY, originX, h - 20);
        gc.setLineDashes(null);

        double sensorY = originY + length;
        double sensorWidth = 100;

        LinearGradient sensorGrad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#495057")), new Stop(1, Color.web("#212529")));
        gc.setFill(sensorGrad);
        gc.fillRoundRect(originX - sensorWidth/2 - 20, sensorY - 15, 30, 30, 10, 10);
        gc.fillRoundRect(originX + sensorWidth/2 - 10, sensorY - 15, 30, 30, 10, 10);

        if (laserBroken) {
            gc.setStroke(Color.web("#ff0000", 0.2));
            gc.setFill(Color.LIMEGREEN);
        } else {
            gc.setStroke(Color.web("#ff0000", 0.8));
            gc.setFill(Color.RED);
        }
        gc.setLineWidth(4);
        gc.strokeLine(originX - sensorWidth/2 + 10, sensorY, originX + sensorWidth/2 - 10, sensorY);
        gc.fillOval(originX - sensorWidth/2 - 10, sensorY - 5, 10, 10);

        double bobX = originX + length * Math.sin(currentAngle);
        double bobY = originY + length * Math.cos(currentAngle);

        LinearGradient mountGrad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ced4da")), new Stop(1, Color.web("#6c757d")));
        gc.setFill(mountGrad);
        gc.fillRoundRect(originX - 60, originY - 10, 120, 20, 10, 10);
        gc.setFill(Color.BLACK);
        gc.fillOval(originX - 4, originY - 4, 8, 8);

        if (type == PendulumType.MATHEMATICAL) {
            gc.setStroke(Color.web("#343a40"));
            gc.setLineWidth(2);
            gc.strokeLine(originX, originY, bobX, bobY);

            RadialGradient ballGrad = new RadialGradient(0, 0, bobX - bobRadius*0.3, bobY - bobRadius*0.3, bobRadius,
                    false, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(0.3, Color.STEELBLUE), new Stop(1, Color.web("#0a2540")));
            gc.setFill(ballGrad);
            gc.fillOval(bobX - bobRadius, bobY - bobRadius, bobRadius * 2, bobRadius * 2);

        } else {
            gc.save();
            gc.translate(originX, originY);
            gc.rotate(Math.toDegrees(-currentAngle));

            LinearGradient rodGrad = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#6c757d")), new Stop(0.5, Color.WHITE), new Stop(1, Color.web("#495057")));
            gc.setFill(rodGrad);
            gc.fillRect(-8, -40, 16, length + 80);

            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            for(int i = 0; i < length; i+=20) {
                gc.strokeLine(-8, i, 0, i);
            }

            gc.setFill(Color.web("#8b0000"));
            gc.fillPolygon(new double[]{-20, 20, 0}, new double[]{0, 0, 15}, 3);
            gc.fillPolygon(new double[]{-20, 20, 0}, new double[]{length, length, length - 15}, 3);
            LinearGradient weightGrad = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#b8860b")), new Stop(0.5, Color.web("#ffd700")), new Stop(1, Color.web("#8b6508")));
            gc.setFill(weightGrad);
            gc.fillRoundRect(-25, length * 0.6, 50, 30, 5, 5);
            gc.setStroke(Color.BLACK);
            gc.strokeRoundRect(-25, length * 0.6, 50, 30, 5, 5);
            gc.restore();
        }
    }
}