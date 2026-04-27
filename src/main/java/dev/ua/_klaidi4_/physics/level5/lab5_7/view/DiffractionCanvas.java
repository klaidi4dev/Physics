package dev.ua._klaidi4_.physics.level5.lab5_7.view;

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

public class DiffractionCanvas extends Canvas {

    private double distanceL = 1.0;
    private double gratingD = 10.0;
    private int targetM = 1;
    private double currentLambda = 632.8e-9;
    private boolean isMeasuring = false;
    private double scanProgress = 0.0;
    private AnimationTimer timer;
    private long lastTime = 0;
    private double pixelsPerMm = 0.33;

    public DiffractionCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    public void setPhysicsParameters(double L, double d, int m, double lambdaNm) {
        this.distanceL = L;
        this.gratingD = d;
        this.targetM = m;
        this.currentLambda = lambdaNm * 1e-9;

        double sinTheta3 = (3 * currentLambda) / (gratingD * 1e-6);
        double l3_mm = distanceL * Math.tan(Math.asin(sinTheta3)) * 1000;
        double availableWidth = getWidth() / 2.0 - 50;
        this.pixelsPerMm = availableWidth / l3_mm;

        this.isMeasuring = false;
        this.scanProgress = 0.0;
        draw();
    }

    public void startMeasurementScan() {
        this.isMeasuring = true;
        this.scanProgress = 0.0;
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                if (isMeasuring) {
                    scanProgress += dt * 1.5;
                    if (scanProgress > 1.0) { scanProgress = 1.0; isMeasuring = false; }
                }
                draw();
            }
        };
        timer.start();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();
        double halfH = h / 2;

        gc.setFill(Color.web("#1e272e"));
        gc.fillRect(0, 0, w, halfH);
        gc.setFill(Color.web("#485460"));
        gc.fillRect(10, halfH - 20, w - 20, 10);

        double laserX = 30; double laserY = halfH - 70;
        gc.setFill(Color.web("#2f3640"));
        gc.fillRect(laserX + 25, laserY + 30, 10, 20);

        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.web("#d2dae2")), new Stop(1, Color.web("#808e9b"))));
        gc.fillRect(laserX, laserY, 60, 30);
        gc.setFill(Color.RED);
        gc.fillRect(laserX + 60, laserY + 10, 5, 10);

        double gratingX = 140;
        gc.setFill(Color.web("#2f3640"));
        gc.fillRect(gratingX - 2, laserY + 40, 9, 10);
        gc.setFill(Color.web("#0fbcf9", 0.5));
        gc.fillRect(gratingX, laserY - 10, 5, 50);

        double screenX = gratingX + 100 + (distanceL * 80);
        gc.setFill(Color.web("#d1d8e0"));
        gc.fillRect(screenX, 10, 10, halfH - 20);

        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        gc.strokeLine(laserX + 65, laserY + 15, gratingX, laserY + 15);

        for (int m = -3; m <= 3; m++) {
            double sinTheta = (m * currentLambda) / (gratingD * 1e-6);
            if (Math.abs(sinTheta) <= 1.0) {
                double theta = Math.asin(sinTheta);
                double rayY = laserY + 15 + Math.tan(theta) * (screenX - gratingX) * 0.35;
                gc.setStroke(Color.web("#ff0000", Math.max(0.1, 1.0 - Math.abs(m) * 0.2)));
                gc.strokeLine(gratingX + 5, laserY + 15, screenX, rayY);
            }
        }

        gc.setFill(Color.BLACK);
        gc.fillRect(0, halfH, w, halfH);
        double centerY = halfH + halfH / 2;
        double centerX = w / 2;

        gc.setStroke(Color.web("#ffffff", 0.5));
        gc.setLineWidth(1);
        gc.strokeLine(20, centerY + 20, w - 20, centerY + 20);

        double stepMm = (pixelsPerMm < 0.1) ? 100 : ((pixelsPerMm < 0.5) ? 50 : 10);

        for (int i = -1000; i <= 1000; i += 10) {
            double tickX = centerX + i * pixelsPerMm;
            if (tickX < 20 || tickX > w - 20) continue;

            double tickH = (i % 100 == 0) ? 12 : ((i % 50 == 0) ? 8 : 4);
            gc.strokeLine(tickX, centerY + 20, tickX, centerY + 20 + tickH);

            if (i % 100 == 0 && i != 0) {
                gc.setFill(Color.web("#ffffff", 0.7));
                gc.setFont(Font.font("System", 10));
                gc.fillText(String.valueOf(Math.abs(i)), tickX - 10, centerY + 45);
            }
        }
        gc.setFill(Color.WHITE);
        gc.fillText("0", centerX - 3, centerY + 45);

        for (int m = -3; m <= 3; m++) {
            double sinTheta = (m * currentLambda) / (gratingD * 1e-6);
            if (Math.abs(sinTheta) <= 1.0) {
                double theta = Math.asin(sinTheta);
                double dotX = centerX + (distanceL * Math.tan(theta) * 1000) * pixelsPerMm;
                if (dotX < 0 || dotX > w) continue;

                double radius = 9 - Math.abs(m);
                double alpha = 1.0 - Math.abs(m) * 0.2;
                gc.setFill(new RadialGradient(0, 0, dotX, centerY, radius * 2, false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#ffffff", alpha)),
                        new Stop(0.2, Color.web("#ff0000", alpha)),
                        new Stop(1, Color.web("#ff0000", 0.0))));
                gc.fillOval(dotX - radius*2, centerY - radius*2, radius*4, radius*4);
            }
        }

        if (scanProgress > 0) {
            double targetX = centerX + (distanceL * Math.tan(Math.asin((targetM * currentLambda) / (gratingD * 1e-6))) * 1000) * pixelsPerMm;
            double currentScanX = centerX + (targetX - centerX) * scanProgress;
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(2);
            gc.strokeLine(centerX, centerY - 25, currentScanX, centerY - 25);
            gc.strokeRect(currentScanX - 10, centerY - 10, 20, 20);
            if (scanProgress >= 1.0) {
                gc.setFill(Color.YELLOW);
                gc.setFont(Font.font("System", FontWeight.BOLD, 12));
                gc.fillText("l_" + targetM, currentScanX - 10, centerY - 35);
            }
        }
    }
}