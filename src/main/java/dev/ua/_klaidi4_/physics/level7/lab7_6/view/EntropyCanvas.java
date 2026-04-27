package dev.ua._klaidi4_.physics.level7.lab7_6.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

import java.util.ArrayList;
import java.util.List;

public class EntropyCanvas extends Canvas {

    private boolean isHeaterOn = false;
    private double temperature = 20.0;
    private double meltProgress = 0.0;
    private double maxSimTime = 0;
    private double targetTMelt = 327.0;
    private List<Double> timeHistory = new ArrayList<>();
    private List<Double> tempHistory = new ArrayList<>();

    public EntropyCanvas(double width, double height) {
        super(width, height);
        drawFrame();
    }

    public void updateState(boolean heaterOn, double temp, double meltProg, double simTime, double tMelt) {
        this.isHeaterOn = heaterOn;
        this.temperature = temp;
        this.meltProgress = meltProg;
        this.targetTMelt = tMelt;

        if (timeHistory.isEmpty() || simTime - timeHistory.get(timeHistory.size() - 1) >= 1.0) {
            timeHistory.add(simTime);
            tempHistory.add(temp);
            maxSimTime = Math.max(maxSimTime, simTime);
        }

        drawFrame();
    }

    public void resetGraph() {
        timeHistory.clear();
        tempHistory.clear();
        maxSimTime = 0;
        drawFrame();
    }

    private void drawFrame() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#0d1117"));
        gc.fillRect(0, 0, w, h);

        drawCylindricalFurnace(gc, 30, 80, 140, 260);
        drawKSP4Recorder(gc, 200, 20, 380, 400);
    }

    private void drawCylindricalFurnace(GraphicsContext gc, double x, double y, double fw, double fh) {
        gc.setFill(Color.web("#37474f"));
        gc.fillRoundRect(x, y, fw, fh, 30, 30);
        gc.setStroke(Color.web("#263238"));
        gc.setLineWidth(4.0);
        gc.strokeRoundRect(x, y, fw, fh, 30, 30);

        gc.setFill(Color.web("#455a64"));
        gc.fillOval(x, y - 15, fw, 30);
        gc.setStroke(Color.web("#1c313a"));
        gc.strokeOval(x, y - 15, fw, 30);

        gc.setStroke(Color.web("#90a4ae"));
        gc.setLineWidth(6.0);
        gc.strokeArc(x + fw/2 - 20, y - 35, 40, 40, 0, 180, ArcType.OPEN);

        double cutX = x + 20;
        double cutY = y + 60;
        double cutW = fw - 40;
        double cutH = fh - 100;

        gc.setFill(Color.web("#1e293b"));
        gc.fillRoundRect(cutX, cutY, cutW, cutH, 15, 15);

        gc.setStroke(isHeaterOn ? Color.web("#ff3d00", 0.9) : Color.web("#455a64"));
        gc.setLineWidth(4.0);
        for (int i = 0; i <= cutH - 20; i += 15) {
            gc.strokeLine(cutX + 5, cutY + 10 + i, cutX + cutW - 5, cutY + 20 + i);
        }

        gc.setFill(Color.web("#78909c"));
        gc.fillArc(cutX + 10, cutY + cutH - 70, cutW - 20, 80, 180, 180, ArcType.ROUND);

        Color solidPb = Color.web("#37474f");
        Color liquidPb = Color.web("#b0bec5");
        double r = solidPb.getRed() + (liquidPb.getRed() - solidPb.getRed()) * meltProgress;
        double g = solidPb.getGreen() + (liquidPb.getGreen() - solidPb.getGreen()) * meltProgress;
        double b = solidPb.getBlue() + (liquidPb.getBlue() - solidPb.getBlue()) * meltProgress;
        Color currentColor = Color.color(r, g, b);

        gc.setFill(currentColor);
        gc.fillOval(cutX + 15, cutY + cutH - 40, cutW - 30, 20);

        if (meltProgress > 0) {
            gc.setFill(Color.web("#ffffff", 0.4 * meltProgress));
            gc.fillOval(cutX + 25, cutY + cutH - 35, 20, 6);
        }
        if (meltProgress < 1.0) {
            gc.setFill(Color.web("#263238", 1.0 - meltProgress));
            gc.fillRect(cutX + 30, cutY + cutH - 38, 10, 10);
            gc.fillRect(cutX + 50, cutY + cutH - 35, 12, 8);
        }

        gc.setStroke(Color.web("#cfd8dc"));
        gc.setLineWidth(3.0);
        gc.strokeLine(cutX + cutW/2, cutY + cutH - 30, cutX + cutW/2, y - 50);
        gc.strokeLine(cutX + cutW/2, y - 50, 200, y - 50);
    }

    private void drawKSP4Recorder(GraphicsContext gc, double x, double y, double w, double h) {
        gc.setFill(Color.web("#cfd8dc"));
        gc.fillRect(x, y, w, h);
        gc.setStroke(Color.web("#90a4ae"));
        gc.setLineWidth(3.0);
        gc.strokeRect(x, y, w, h);

        double dialR = 45;
        double dialX = x + 60;
        double dialY = y + 60;
        gc.setFill(Color.web("#ffffff"));
        gc.fillOval(dialX - dialR, dialY - dialR, dialR * 2, dialR * 2);
        gc.setStroke(Color.web("#37474f"));
        gc.setLineWidth(2.0);
        gc.strokeOval(dialX - dialR, dialY - dialR, dialR * 2, dialR * 2);

        double angle = Math.toRadians(-150 + (Math.min(temperature, 550.0) / 550.0) * 300);
        double nx = dialX + (dialR - 10) * Math.cos(angle);
        double ny = dialY + (dialR - 10) * Math.sin(angle);
        gc.setStroke(Color.web("#d32f2f"));
        gc.setLineWidth(3.0);
        gc.strokeLine(dialX, dialY, nx, ny);
        gc.setFill(Color.web("#000000"));
        gc.fillOval(dialX - 4, dialY - 4, 8, 8);

        gc.setFill(Color.web("#37474f"));
        gc.fillText("КСП-4", dialX + 70, dialY - 10);

        gc.fillText(String.format(java.util.Locale.US, "T = %.1f °C", temperature), dialX + 70, dialY + 15);

        double pX = x + 20;
        double pY = y + 130;
        double gW = w - 40;
        double gH = h - 150;

        gc.setFill(Color.web("#ffffff"));
        gc.fillRect(pX, pY, gW, gH);

        gc.setStroke(Color.web("#e0f2f1"));
        gc.setLineWidth(1.0);
        for(int i = 0; i <= gW; i += 20) gc.strokeLine(pX + i, pY, pX + i, pY + gH);
        for(int i = 0; i <= gH; i += 20) gc.strokeLine(pX, pY + i, pX + gW, pY + i);

        gc.setStroke(Color.web("#455a64"));
        gc.setLineWidth(2.0);
        gc.strokeLine(pX + 30, pY + 10, pX + 30, pY + gH - 20);
        gc.strokeLine(pX + 30, pY + gH - 20, pX + gW - 10, pY + gH - 20);

        gc.setFill(Color.web("#455a64"));
        gc.fillText("T, °C", pX + 5, pY + 20);
        gc.fillText("t, с", pX + gW - 25, pY + gH - 5);

        double maxT = Math.max(350.0, targetTMelt + 50.0);

        for (Double th : tempHistory) {
            if (th > maxT - 20) maxT = th + 20.0;
        }
        if (temperature > maxT - 20) maxT = temperature + 20.0;

        gc.fillText(String.valueOf((int) maxT), pX + 5, pY + 30);
        gc.fillText("0", pX + 15, pY + gH - 20);

        double graphH = gH - 40;
        double meltY = (pY + gH - 20) - (targetTMelt / maxT) * graphH;

        gc.setStroke(Color.web("#ffb300", 0.7));
        gc.setLineDashes(4, 4);
        gc.strokeLine(pX + 30, meltY, pX + gW - 10, meltY);
        gc.setLineDashes(null);
        gc.setFill(Color.web("#ffb300"));
        gc.fillText(String.valueOf((int) targetTMelt), pX + 2, meltY + 4);

        if (timeHistory.size() > 1) {
            gc.setStroke(Color.web("#d32f2f"));
            gc.setLineWidth(2.5);

            double xMax = Math.max(900.0, maxSimTime);
            double graphW = gW - 50;

            gc.beginPath();
            for (int i = 0; i < timeHistory.size(); i++) {
                double px = pX + 30 + (timeHistory.get(i) / xMax) * graphW;
                double py = (pY + gH - 20) - (tempHistory.get(i) / maxT) * graphH;
                py = Math.max(pY + 10, Math.min(py, pY + gH - 20));

                if (i == 0) gc.moveTo(px, py);
                else gc.lineTo(px, py);
            }
            gc.stroke();

            double lastX = pX + 30 + (timeHistory.get(timeHistory.size() - 1) / xMax) * graphW;
            double lastY = (pY + gH - 20) - (tempHistory.get(tempHistory.size() - 1) / maxT) * graphH;
            gc.setFill(Color.web("#1976d2"));
            gc.fillOval(lastX - 4, lastY - 4, 8, 8);
        }
    }
}