package dev.ua._klaidi4_.physics.level6.lab6_3.view;

import dev.ua._klaidi4_.physics.level6.lab6_3.model.Measurement;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GammaAttenuationCanvas extends Canvas {

    private double thicknessX = 0.0;
    private double trueMu = 0.65;
    private boolean isMeasuring = false;
    private List<Measurement> dataPoints = new ArrayList<>();
    private AnimationTimer timer;
    private double time = 0;

    public GammaAttenuationCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    public void setPhysicsParameters(double x, double mu, boolean measuring, List<Measurement> data) {
        this.thicknessX = x;
        this.trueMu = mu;
        this.isMeasuring = measuring;
        this.dataPoints = new ArrayList<>(data);
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

        double setupY = h * 0.35;
        double centerX = w / 2;

        double containerX = 50;
        gc.setFill(Color.web("#334155"));
        gc.fillRoundRect(containerX, setupY - 40, 80, 80, 10, 10);

        gc.setFill(Color.web("#000000"));
        gc.fillRect(containerX + 60, setupY - 10, 20, 20);

        gc.setFill(Color.web("#00e5ff"));
        gc.fillOval(containerX + 30, setupY - 10, 20, 20);

        double detectorX = w - 100;
        gc.setFill(Color.web("#1e293b"));
        gc.fillRect(detectorX, setupY - 30, 80, 60);
        gc.setStroke(Color.web("#475569"));
        gc.setLineWidth(3.0);
        gc.strokeRect(detectorX, setupY - 30, 80, 60);

        double filterStartX = containerX + 120;
        int numPlates = (int) Math.round(thicknessX * 10);

        for (int i = 0; i < numPlates; i++) {
            gc.setFill(Color.web("#64748b"));
            gc.fillRect(filterStartX + i * 8, setupY - 35, 6, 70);
        }

        if (isMeasuring) {
            gc.setStroke(Color.web("#00e5ff", 0.6));
            gc.setLineWidth(2.0);

            double transmissionProb = Math.exp(-trueMu * thicknessX);

            for (int i = 0; i < 8; i++) {
                double speed = 400.0 + i * 50;
                double maxTravel = detectorX - (containerX + 80);
                double currentTravel = (time * speed + i * 70) % maxTravel;
                double px = containerX + 80 + currentTravel;
                double py = setupY - 5 + Math.random() * 10;
                double tailX = px - 15;

                if (px > filterStartX && px < filterStartX + numPlates * 8) {
                    if (Math.random() > transmissionProb) {
                        continue;
                    }
                }

                if (currentTravel < maxTravel) {
                    gc.setLineDashes(4, 4);
                    gc.strokeLine(px, py, tailX, py);
                    gc.setLineDashes(null);
                }

                if (px >= detectorX - 10 && px <= detectorX && Math.random() < transmissionProb) {
                    gc.setFill(Color.color(0.0, 1.0, 0.8, 0.7));
                    gc.fillOval(detectorX - 5, py - 5, 10, 10);
                }
            }
        }

        double graphX = 60.0;
        double graphY = h - 30.0;
        double graphW = w - 90.0;
        double graphH = h * 0.35;

        gc.setStroke(Color.web("#64748b"));
        gc.setLineWidth(2.0);
        gc.strokeLine(graphX, graphY, graphX + graphW, graphY);
        gc.strokeLine(graphX, graphY, graphX, graphY - graphH);

        gc.setFill(Color.web("#64748b"));
        gc.fillText("Товщина x (см)", graphX + graphW - 40, graphY + 15);
        gc.fillText("ln N(x)", graphX - 45, graphY - graphH + 10);

        if (!dataPoints.isEmpty()) {
            dataPoints.sort(Comparator.comparingDouble(Measurement::getX));

            gc.setStroke(Color.web("#ff007f"));
            gc.setLineWidth(2.0);
            gc.setFill(Color.web("#00ffcc"));

            double maxLnN = 0;
            double minLnN = Double.MAX_VALUE;
            for (Measurement m : dataPoints) {
                if (m.getLnCounts() > maxLnN) maxLnN = m.getLnCounts();
                if (m.getLnCounts() < minLnN) minLnN = m.getLnCounts();
            }

            maxLnN += 0.5;
            minLnN = Math.max(0, minLnN - 0.5);
            double rangeLnN = maxLnN - minLnN;
            if(rangeLnN == 0) rangeLnN = 1.0;

            gc.beginPath();
            boolean first = true;

            for (Measurement m : dataPoints) {
                double px = graphX + (m.getX() / 1.0) * graphW;
                double py = graphY - ((m.getLnCounts() - minLnN) / rangeLnN) * graphH;

                if (first) {
                    gc.moveTo(px, py);
                    first = false;
                } else {
                    gc.lineTo(px, py);
                }
            }
            gc.stroke();

            for (Measurement m : dataPoints) {
                double px = graphX + (m.getX() / 1.0) * graphW;
                double py = graphY - ((m.getLnCounts() - minLnN) / rangeLnN) * graphH;
                gc.fillOval(px - 4, py - 4, 8, 8);
            }
        }

        double currentPx = graphX + (thicknessX / 1.0) * graphW;
        gc.setStroke(Color.web("#ffffff", 0.3));
        gc.setLineDashes(4, 4);
        gc.strokeLine(currentPx, graphY, currentPx, graphY - graphH);
        gc.setLineDashes(null);
    }
}