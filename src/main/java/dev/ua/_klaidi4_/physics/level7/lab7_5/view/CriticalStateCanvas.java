package dev.ua._klaidi4_.physics.level7.lab7_5.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.util.Random;

public class CriticalStateCanvas extends Canvas {

    private double voltage = 0.0;
    private double currentTemp = 20.0;
    private double actualTk = 193.6;
    private double heatRateMultiplier = 1.0;
    private AnimationTimer timer;
    private final Random random = new Random();
    private double time = 0;

    public CriticalStateCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    public void setPhysicsParams(double voltage, double tk, double heatRate) {
        this.voltage = voltage;
        this.actualTk = tk;
        this.heatRateMultiplier = heatRate;
    }

    public double getCurrentTemp() {
        return currentTemp;
    }

    public void resetTemp() {
        this.currentTemp = 20.0;
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                time += dt;

                double maxPossibleTemp = actualTk + 60.0;
                double targetTemp = 20.0 + (voltage / 220.0) * (maxPossibleTemp - 20.0);
                double baseRate = (voltage > 0) ? 0.08 : 0.15;
                currentTemp += (targetTemp - currentTemp) * baseRate * heatRateMultiplier * dt;

                drawFrame();
            }
        };
        timer.start();
    }

    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    private void drawFrame() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#0d1117"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#1e293b"));
        gc.setLineWidth(1.0);
        for (int i = 0; i < w; i += 40) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 40) gc.strokeLine(0, i, w, i);

        double centerX = w / 2;
        double centerY = h / 2;

        gc.setStroke(Color.web("#475569"));
        gc.setLineWidth(4.0);
        gc.strokeRect(centerX - 120, centerY - 160, 240, 320);
        gc.setFill(Color.web("#1e293b", 0.5));
        gc.fillRect(centerX - 120, centerY - 160, 240, 320);

        double glow = (voltage / 220.0);
        gc.setStroke(Color.color(0.3 + glow * 0.7, 0.1, 0.1));
        gc.setLineWidth(4.0);
        gc.beginPath();
        for (int i = 0; i <= 200; i += 10) {
            gc.lineTo(centerX - 100 + i, centerY + 140 + (i % 20 == 0 ? 0 : -10));
        }
        gc.stroke();

        double ampW = 40.0;
        double ampH = 200.0;
        double ampX = centerX - ampW / 2;
        double ampY = centerY - ampH / 2;
        double diff = actualTk - currentTemp;
        double phaseSeparation = Math.max(0.0, Math.min(1.0, diff / 20.0));

        Color liquidColor = Color.web("#00e5ff", 0.3 + 0.5 * phaseSeparation);
        Color vaporColor = Color.web("#00e5ff", 0.8 - 0.5 * phaseSeparation);

        if (currentTemp >= actualTk) {
            gc.setFill(Color.web("#00e5ff", 0.55));
            gc.fillRoundRect(ampX, ampY, ampW, ampH, 20, 20);
        } else {
            double meniscusY = ampY + ampH / 2 + (1.0 - phaseSeparation) * 10.0;
            gc.setFill(vaporColor);
            gc.fillRoundRect(ampX, ampY, ampW, ampH, 20, 20);
            gc.setFill(liquidColor);
            gc.fillRoundRect(ampX, meniscusY, ampW, ampY + ampH - meniscusY, 0, 20);

            gc.setStroke(Color.web("#ffffff", phaseSeparation));
            gc.setLineWidth(2.0);
            gc.strokeLine(ampX, meniscusY, ampX + ampW, meniscusY);
        }

        if (Math.abs(diff) < 2.0) {
            double opalescenceIntensity = 1.0 - (Math.abs(diff) / 2.0);
            gc.setFill(Color.web("#ffffff", opalescenceIntensity * 0.4));
            for (int i = 0; i < 50; i++) {
                double rx = ampX + random.nextDouble() * ampW;
                double ry = ampY + random.nextDouble() * ampH;
                double rSize = 2 + random.nextDouble() * 6;
                gc.fillOval(rx, ry, rSize, rSize);
            }
        }

        LinearGradient glassGloss = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ffffff", 0.1)),
                new Stop(0.2, Color.web("#ffffff", 0.5)),
                new Stop(0.3, Color.web("#ffffff", 0.0)),
                new Stop(1, Color.web("#ffffff", 0.2)));
        gc.setFill(glassGloss);
        gc.fillRoundRect(ampX, ampY, ampW, ampH, 20, 20);
        gc.setStroke(Color.web("#94a3b8"));
        gc.setLineWidth(2.0);
        gc.strokeRoundRect(ampX, ampY, ampW, ampH, 20, 20);

        double thermX = centerX + 50;
        double thermY = centerY - 100;
        gc.setFill(Color.web("#e2e8f0"));
        gc.fillRect(thermX, thermY, 8, 200);

        double maxThermTemp = actualTk * 1.3;
        double thermFillH = (currentTemp / maxThermTemp) * 180.0;
        thermFillH = Math.max(0, Math.min(thermFillH, 190.0));

        gc.setFill(Color.web("#ff007f"));
        gc.fillRect(thermX + 2, thermY + 190 - thermFillH, 4, thermFillH + 10);
        gc.fillOval(thermX - 2, thermY + 190, 12, 12);

        gc.setStroke(Color.web("#000000"));
        gc.setLineWidth(1.0);
        double step = (maxThermTemp > 200) ? 50 : 20;
        for (int i = 0; i <= maxThermTemp; i += step) {
            double tickY = thermY + 190 - (i / maxThermTemp) * 180.0;
            gc.strokeLine(thermX, tickY, thermX + 4, tickY);
        }
    }
}