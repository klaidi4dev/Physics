package dev.ua._klaidi4_.physics.level2.lab2_6.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ResistanceCanvas extends Canvas {

    private int activeMode = 1;
    private double targetCurrent = 0.0;
    private double displayedCurrent = 0.0;
    private double sliderPosition = 500.0;
    private String activeResistor = "R1";
    private double targetTemp = 20.0;
    private double displayedTemp = 20.0;
    private boolean isHeating = false;

    private AnimationTimer timer;
    private long lastTime = 0;

    public ResistanceCanvas(double width, double height) {
        super(width, height);
        startSimulation();
    }

    public void setMode(int mode) {
        this.activeMode = mode;
        this.isHeating = false;
        draw();
    }

    public void updateBridge(double sliderPos, double currentG, String resistor) {
        this.sliderPosition = sliderPos;
        this.targetCurrent = currentG;
        this.activeResistor = resistor;
    }

    public void updateThermostat(double temp, boolean heating) {
        this.targetTemp = temp;
        this.isHeating = heating;
    }

    private void startSimulation() {
        if (timer != null) timer.stop();
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                lastTime = now;

                if (activeMode == 1) {
                    if (Math.abs(displayedCurrent - targetCurrent) > 0.01) {
                        displayedCurrent += (targetCurrent - displayedCurrent) * 0.15;
                    } else {
                        displayedCurrent = targetCurrent;
                    }
                    if (Math.abs(targetCurrent) > 0.5) {
                        displayedCurrent += (Math.random() - 0.5) * 0.2;
                    }
                } else {
                    if (Math.abs(displayedTemp - targetTemp) > 0.1) {
                        displayedTemp += (targetTemp - displayedTemp) * 0.1;
                    } else {
                        displayedTemp = targetTemp;
                    }
                }
                draw();
            }
        };
        timer.start();
    }

    public void stopSimulation() {
        if (timer != null) timer.stop();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#f8fafc"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#e2e8f0"));
        gc.setLineWidth(1);
        for (int i = 0; i < w; i += 20) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 20) gc.strokeLine(0, i, w, i);

        if (activeMode == 1) {
            drawBridge(gc, w, h);
        } else {
            drawThermostat(gc, w, h);
        }
    }

    private void drawBridge(GraphicsContext gc, double w, double h) {
        double startX = 50;
        double endX = w - 50;
        double topY = 60;
        double midY = 140;
        double botY = 220;

        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(2);

        gc.strokeLine(startX, midY, startX, topY);
        gc.strokeLine(endX, midY, endX, topY);
        gc.strokeLine(startX, topY, w/2 - 15, topY);
        gc.strokeLine(w/2 + 15, topY, endX, topY);

        gc.setLineWidth(4);
        gc.strokeLine(w/2 - 15, topY - 10, w/2 - 15, topY + 10);
        gc.setLineWidth(2);
        gc.strokeLine(w/2 + 5, topY - 20, w/2 + 5, topY + 20);

        gc.strokeLine(startX, midY, endX, midY);

        double rxX = w/4;
        gc.setFill(Color.WHITE);
        gc.fillRect(rxX - 25, midY - 10, 50, 20);
        gc.strokeRect(rxX - 25, midY - 10, 50, 20);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.fillText(activeResistor, rxX - 10, midY + 4);

        double retX = 3 * w / 4;
        gc.setFill(Color.WHITE);
        gc.fillRect(retX - 25, midY - 10, 50, 20);
        gc.strokeRect(retX - 25, midY - 10, 50, 20);
        gc.setFill(Color.BLACK);
        gc.fillText("Rет", retX - 10, midY + 4);

        gc.strokeLine(startX, midY, startX, botY);
        gc.strokeLine(endX, midY, endX, botY);
        gc.setStroke(Color.web("#b45309"));
        gc.setLineWidth(6);
        gc.strokeLine(startX, botY, endX, botY);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        for (int i = 0; i <= 10; i++) {
            double tickX = startX + i * ((endX - startX) / 10);
            gc.strokeLine(tickX, botY + 5, tickX, botY + 15);
        }

        double sliderPx = startX + (sliderPosition / 1000.0) * (endX - startX);
        gc.setFill(Color.web("#ef4444"));
        gc.fillPolygon(new double[]{sliderPx - 8, sliderPx + 8, sliderPx},
                new double[]{botY + 45, botY + 45, botY + 5}, 3);

        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(2);
        gc.strokeLine(w/2, midY, w/2, botY);
        gc.setFill(Color.WHITE);
        gc.fillOval(w/2 - 25, (midY + botY)/2 - 25, 50, 50);
        gc.strokeOval(w/2 - 25, (midY + botY)/2 - 25, 50, 50);

        double deflection = (displayedCurrent / 50.0) * 35.0;
        if (deflection > 35) deflection = 35;
        if (deflection < -35) deflection = -35;
        double needleRad = Math.toRadians(deflection - 90);
        double gCy = (midY + botY)/2 + 10;
        double nX = w/2 + 20 * Math.cos(needleRad);
        double nY = gCy + 20 * Math.sin(needleRad);

        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        gc.strokeLine(w/2, gCy, nX, nY);
        gc.setFill(Color.BLACK);
        gc.fillOval(w/2 - 3, gCy - 3, 6, 6);
    }

    private void drawThermostat(GraphicsContext gc, double w, double h) {
        double cx = w / 2 - 40;
        double cy = h / 2 + 10;

        double ovenW = 140;
        double ovenH = 160;
        double ovenX = cx - ovenW / 2;
        double ovenY = cy - ovenH / 2;

        gc.setFill(Color.web("#607d8b"));
        gc.fillRoundRect(ovenX, ovenY, ovenW, ovenH, 15, 15);
        gc.setStroke(Color.web("#37474f"));
        gc.setLineWidth(3);
        gc.strokeRoundRect(ovenX, ovenY, ovenW, ovenH, 15, 15);

        Color heatColor = isHeating ? Color.web("#ff5722") : Color.web("#90a4ae");
        gc.setStroke(heatColor);
        gc.setLineWidth(5);
        for (int i = 0; i < 4; i++) {
            double hy = ovenY + 30 + i * 35;
            gc.strokeLine(ovenX + 15, hy, ovenX + ovenW - 15, hy);
        }

        gc.setStroke(Color.web("#d35400"));
        gc.setLineWidth(6);
        for (int i = 0; i < 6; i++) {
            gc.strokeOval(cx - 20, cy - 40 + i * 12, 40, 15);
        }

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(cx - 20, cy - 40, cx - 20, ovenY - 30);
        gc.strokeLine(cx + 20, cy + 32, cx + 20, ovenY - 30);

        gc.setFill(Color.web("#cfd8dc"));
        gc.fillRect(cx - 40, ovenY - 70, 80, 40);
        gc.strokeRect(cx - 40, ovenY - 70, 80, 40);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.fillText("Мідь", cx - 18, ovenY - 45);

        double tempRatio = (displayedTemp - 20) / 80.0;
        if (tempRatio > 1.0) tempRatio = 1.0;
        if (tempRatio < 0) tempRatio = 0;
        Color tempColor = Color.web("#4fc3f7").interpolate(Color.web("#d32f2f"), tempRatio);

        double thermX = cx + ovenW / 2 + 20;
        double thermY = cy - 60;
        gc.setFill(Color.WHITE);
        gc.fillRoundRect(thermX, thermY, 12, 120, 6, 6);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRoundRect(thermX, thermY, 12, 120, 6, 6);

        gc.setFill(tempColor);
        double fillH = 20 + 90 * tempRatio;
        gc.fillRect(thermX + 2, thermY + 120 - fillH, 8, fillH);
        gc.fillOval(thermX - 3, thermY + 115, 18, 18);
    }
}