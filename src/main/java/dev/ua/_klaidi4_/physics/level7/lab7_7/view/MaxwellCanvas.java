package dev.ua._klaidi4_.physics.level7.lab7_7.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MaxwellCanvas extends Canvas {

    private double currentVoltage = 0.0;
    private boolean isPowerOn = false;
    private double cathodeTemp = 2000.0;
    private double emissionIntensity = 1.0;
    private double simSpeed = 1.0;

    private AnimationTimer timer;
    private List<Electron> electrons = new ArrayList<>();
    private Random random = new Random();

    private class Electron {
        double x, y;
        double energy;
        boolean isStopped = false;

        Electron(double startX, double startY, double energy) {
            this.x = startX;
            this.y = startY;
            this.energy = energy;
        }
    }

    public MaxwellCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    public void updateState(boolean power, double voltage) {
        this.isPowerOn = power;
        this.currentVoltage = voltage;
    }

    public void setAdvancedParams(double temp, double emission, double speed) {
        this.cathodeTemp = temp;
        this.emissionIntensity = emission;
        this.simSpeed = speed;
    }

    public double getCurrentIa() {
        if (!isPowerOn) return 0.0;

        double maxCurrent = 200.0 * emissionIntensity;


        double effectiveKt = 2.5 * (cathodeTemp / 2000.0);
        double ratio = currentVoltage / effectiveKt;

        double current = maxCurrent * (1.0 + ratio) * Math.exp(-ratio);
        current += (random.nextDouble() - 0.5) * 1.5;

        return Math.max(0.1, current);
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate > 16_000_000) {
                    updateElectrons();
                    drawFrame();
                    lastUpdate = now;
                }
            }
        };
        timer.start();
    }

    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    private void updateElectrons() {
        if (!isPowerOn) {
            electrons.clear();
            return;
        }

        double w = getWidth();
        double h = getHeight();
        double cathodeX = 150;
        double anodeX = w - 150;
        double grid1X = 220;
        double grid2X = 300;

        int spawnRate = (int) ((15 - (currentVoltage / 15.0) * 10) * emissionIntensity * simSpeed);
        spawnRate = Math.max(1, Math.min(spawnRate, 30));

        for (int i = 0; i < spawnRate; i++) {
            double startY = h / 2 - 80 + random.nextDouble() * 160;
            double energyScale = cathodeTemp / 2000.0;
            double energy = Math.abs(random.nextGaussian()) * 3.0 * energyScale + 0.5;
            electrons.add(new Electron(cathodeX, startY, energy));
        }


        Iterator<Electron> it = electrons.iterator();
        while (it.hasNext()) {
            Electron e = it.next();

            if (e.isStopped) {
                e.x -= 2.0 * simSpeed;
                e.y += (random.nextDouble() - 0.5) * 2 * simSpeed;
                if (e.x < cathodeX) it.remove();
                continue;
            }

            e.x += (e.energy * 2.0 + 1.0) * simSpeed;

            if (e.x > grid1X && e.x < grid2X) {
                double effectiveBarrier = currentVoltage * 0.4;
                if (e.energy < effectiveBarrier && e.x > (grid1X + grid2X)/2) {
                    e.isStopped = true;
                }
            }

            if (e.x > anodeX) {
                it.remove();
            }
        }
    }

    private void drawFrame() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#0d1117"));
        gc.fillRect(0, 0, w, h);

        double centerY = h / 2;

        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(4.0);
        gc.strokeRoundRect(80, centerY - 120, w - 160, 240, 60, 60);
        gc.setFill(Color.web("#1e293b", 0.3));
        gc.fillRoundRect(80, centerY - 120, w - 160, 240, 60, 60);

        double cathodeX = 150;
        if (isPowerOn) {
            double glowSize = 100 * (cathodeTemp / 2000.0);
            Color glowColor = cathodeTemp > 2200 ? Color.web("#fff59d") : Color.web("#ffb300");

            RadialGradient glow = new RadialGradient(0, 0, cathodeX, centerY, glowSize, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 0.6)),
                    new Stop(1, Color.color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 0.0)));
            gc.setFill(glow);
            gc.fillOval(cathodeX - glowSize/2, centerY - glowSize, glowSize, glowSize*2);

            gc.setFill(glowColor);
        } else {
            gc.setFill(Color.web("#94a3b8"));
        }
        gc.fillRect(cathodeX - 10, centerY - 80, 20, 160);
        gc.setFill(Color.WHITE);
        gc.fillText("K (Катод)", cathodeX - 25, centerY + 100);

        double grid1X = 220;
        gc.setStroke(Color.web("#cfd8dc"));
        gc.setLineWidth(2.0);
        gc.setLineDashes(4, 4);
        gc.strokeLine(grid1X, centerY - 90, grid1X, centerY + 90);
        gc.setLineDashes(null);
        gc.fillText("q1", grid1X - 5, centerY + 110);

        double grid2X = 300;
        gc.setStroke(Color.web("#ef5350"));
        gc.setLineWidth(2.0);
        gc.setLineDashes(4, 4);
        gc.strokeLine(grid2X, centerY - 90, grid2X, centerY + 90);
        gc.setLineDashes(null);
        gc.setFill(Color.web("#ef5350"));
        gc.fillText("q2 (Стримує)", grid2X - 30, centerY + 110);

        double anodeX = w - 150;
        gc.setFill(Color.web("#90a4ae"));
        gc.fillRect(anodeX - 10, centerY - 80, 20, 160);
        gc.setFill(Color.WHITE);
        gc.fillText("А (Анод)", anodeX - 20, centerY + 100);

        if (isPowerOn && currentVoltage > 0) {
            gc.setFill(Color.web("#ef5350", 0.1 + (currentVoltage/15.0)*0.2));
            gc.fillRect(grid1X, centerY - 90, grid2X - grid1X, 180);
        }

        for (Electron e : electrons) {
            if (e.isStopped) {
                gc.setFill(Color.web("#ffeb3b", 0.5));
            } else {
                gc.setFill(Color.web("#00e5ff"));
            }
            gc.fillOval(e.x - 2, e.y - 2, 4, 4);
        }

        gc.setFill(Color.web("#475569"));
        gc.fillRect(20, 20, 180, 50);
        gc.fillRect(w - 200, 20, 180, 50);

        gc.setFill(Color.WHITE);
        gc.fillText(String.format(java.util.Locale.US, "Вольтметр V1: %.1f В", currentVoltage), 30, 50);

        double current = getCurrentIa();
        gc.fillText(String.format(java.util.Locale.US, "Струм I_a: %.1f мкА", current), w - 190, 50);
    }
}