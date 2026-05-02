package dev.ua._klaidi4_.physics.level8.lab8_1.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class SemiconductorCanvas extends Canvas {

    private double currentTempC = 20.0;
    private double maxTempC = 85.0;
    private boolean isHeating = false;

    public SemiconductorCanvas(double width, double height) {
        super(width, height);
        drawFrame();
    }

    public void updateState(double tempC, boolean heating, double maxTemp) {
        this.currentTempC = tempC;
        this.isHeating = heating;
        this.maxTempC = maxTemp;
        drawFrame();
    }

    private void drawFrame() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#0f172a"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#1e293b"));
        gc.setLineWidth(1.0);
        for (int i = 0; i < w; i += 40) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 40) gc.strokeLine(0, i, w, i);

        double centerX = w / 2;
        double centerY = h / 2;

        gc.setFill(Color.web("#334155"));
        gc.fillRoundRect(centerX - 120, centerY - 140, 240, 280, 20, 20);
        double tempRange = maxTempC - 20.0;
        if (tempRange < 1.0) tempRange = 1.0;

        double heatRatio = (currentTempC - 20.0) / tempRange;
        if (heatRatio < 0) heatRatio = 0;
        if (heatRatio > 1) heatRatio = 1;

        Color innerColor = Color.color(0.1 + heatRatio * 0.4, 0.15, 0.2);
        gc.setFill(innerColor);
        gc.fillRoundRect(centerX - 100, centerY - 120, 200, 240, 15, 15);

        Color coilColor = isHeating ? Color.color(1.0, 0.3 + heatRatio * 0.4, 0.0) : Color.web("#475569");
        gc.setStroke(coilColor);
        gc.setLineWidth(6.0);
        for (int i = -80; i <= 80; i += 40) {
            gc.strokeLine(centerX - 80, centerY + i, centerX + 80, centerY + i);
        }

        if (isHeating && heatRatio > 0.1) {
            gc.setFill(Color.color(1.0, 0.4, 0.0, 0.15 * heatRatio));
            gc.fillRoundRect(centerX - 100, centerY - 120, 200, 240, 15, 15);
        }

        gc.setFill(Color.web("#94a3b8"));
        gc.fillRect(centerX - 10, centerY - 120, 20, 100);

        gc.setFill(Color.web("#10b981"));
        gc.fillRoundRect(centerX - 25, centerY - 20, 50, 40, 8, 8);
        gc.setStroke(Color.web("#047857"));
        gc.setLineWidth(2.0);
        gc.strokeRoundRect(centerX - 25, centerY - 20, 50, 40, 8, 8);

        gc.setStroke(Color.web("#cbd5e1"));
        gc.setLineWidth(3.0);
        gc.strokeLine(centerX - 15, centerY + 20, centerX - 15, centerY + 140);
        gc.strokeLine(centerX + 15, centerY + 20, centerX + 15, centerY + 140);

        gc.setStroke(Color.web("#eab308"));
        gc.setLineWidth(4.0);
        gc.strokeLine(centerX + 40, centerY - 120, centerX + 40, centerY);
        gc.setFill(Color.web("#b45309"));
        gc.fillOval(centerX + 35, centerY, 10, 10);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", 16));
        gc.fillText(String.format("T: %.1f °C", currentTempC), centerX - 40, centerY - 150);
    }
}