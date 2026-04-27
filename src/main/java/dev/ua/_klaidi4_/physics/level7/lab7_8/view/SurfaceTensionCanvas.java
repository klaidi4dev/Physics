package dev.ua._klaidi4_.physics.level7.lab7_8.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;

public class SurfaceTensionCanvas extends Canvas {

    private int liquidIndex = 0;
    private double currentH = 0.0;
    private double maxH = 100.0;

    private double temperature = 20.0;
    private double capillaryRadius = 0.20;

    private final Color[] LIQUID_COLORS = {
            Color.web("#2196f3", 0.6), // Вода
            Color.web("#ffca28", 0.6), // Спирт
            Color.web("#26a69a", 0.6)  // Гліцерин
    };

    private final Color[] BUBBLE_COLORS = {
            Color.web("#90caf9", 0.8),
            Color.web("#ffe082", 0.8),
            Color.web("#80cbc4", 0.8)
    };

    public SurfaceTensionCanvas(double width, double height) {
        super(width, height);
        drawFrame();
    }

    public void updateState(int liquidIdx, double h, double maxTargetH, double temp, double capRadius) {
        this.liquidIndex = liquidIdx;
        this.currentH = h;
        this.maxH = maxTargetH;
        this.temperature = temp;
        this.capillaryRadius = capRadius;
        drawFrame();
    }

    private void drawFrame() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        // Фон
        gc.setFill(Color.web("#0d1117"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#1e293b"));
        gc.setLineWidth(1.0);
        for (int i = 0; i < w; i += 40) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 40) gc.strokeLine(0, i, w, i);

        drawThermometer(gc, 20, 100);
        drawReservoir(gc, 90, 150, 160, 200);
        drawManometer(gc, 330, 350, 220, -200);
    }

    private void drawThermometer(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.web("#cbd5e1"));
        gc.fillRoundRect(x, y, 10, 150, 5, 5);
        gc.fillOval(x - 5, y + 140, 20, 20);

        double tempPerc = (temperature - 10) / 70.0;
        double fillH = tempPerc * 130;

        gc.setFill(Color.web("#ef5350"));
        gc.fillOval(x - 3, y + 142, 16, 16);
        gc.fillRect(x + 2, y + 140 - fillH, 6, fillH);

        gc.setFill(Color.WHITE);
        gc.fillText(String.format("%.0f °C", temperature), x - 5, y - 5);
    }

    private void drawReservoir(GraphicsContext gc, double x, double y, double fw, double fh) {
        // Посудина
        gc.setFill(Color.web("#ffffff", 0.1));
        gc.fillRoundRect(x, y, fw, fh, 20, 20);
        gc.setStroke(Color.web("#94a3b8"));
        gc.setLineWidth(3.0);
        gc.strokeRoundRect(x, y, fw, fh, 20, 20);

        // Рідина
        double liquidLevel = y + 80;
        gc.setFill(LIQUID_COLORS[liquidIndex]);
        gc.fillRoundRect(x + 3, liquidLevel, fw - 6, fh - 83, 15, 15);

        gc.setFill(LIQUID_COLORS[liquidIndex].deriveColor(0, 1, 1.2, 1));
        gc.fillOval(x + 3, liquidLevel - 10, fw - 6, 20);

        // Пара, якщо температура висока
        if (temperature > 40) {
            gc.setFill(Color.web("#ffffff", 0.15));
            gc.fillOval(x + 20, liquidLevel - 30, 40, 10);
            gc.fillOval(x + 80, liquidLevel - 45, 50, 15);
        }

        // Капіляр
        double capW = 10 + (capillaryRadius - 0.1) * 30; // Змінюється товщина
        double capX = x + fw / 2 - capW / 2;
        double capY = y - 50;
        double capH = liquidLevel - capY + 5;

        gc.setFill(Color.web("#e2e8f0"));
        gc.fillRect(capX, capY, capW, capH);

        gc.setStroke(Color.web("#cbd5e1"));
        gc.setLineWidth(2.0);
        gc.strokeRect(capX, capY, capW, capH);

        gc.setStroke(Color.web("#455a64"));
        gc.setLineWidth(12.0);
        gc.strokeLine(capX + capW/2, capY, capX + capW/2, capY - 40);
        gc.strokeLine(capX + capW/2, capY - 40, x + fw + 80, capY - 40);

        // Бульбашка
        double progress = currentH / maxH;
        if (progress > 0) {
            double rMax = capW * 0.9;
            double r = rMax * Math.min(1.0, progress * 1.2);

            double bubX = capX + capW / 2;
            double bubY = capY + capH;

            gc.setFill(BUBBLE_COLORS[liquidIndex]);
            gc.setStroke(Color.web("#ffffff", 0.8));
            gc.setLineWidth(1.5);

            double stretchY = (progress > 0.85) ? (progress - 0.85) * 20 : 0;

            gc.fillOval(bubX - r, bubY - r/2, r * 2, r * 2 + stretchY);
            gc.strokeOval(bubX - r, bubY - r/2, r * 2, r * 2 + stretchY);
        }

        // Відірвані бульбашки спливають
        if (progress < 0.2 && progress > 0.05) {
            double bubX = capX + capW / 2;
            gc.setFill(BUBBLE_COLORS[liquidIndex]);
            gc.setStroke(Color.web("#ffffff", 0.5));
            gc.fillOval(bubX - 15, capY + capH + 20, 30, 30);
            gc.strokeOval(bubX - 15, capY + capH + 20, 30, 30);
        }
    }

    private void drawManometer(GraphicsContext gc, double startX, double startY, double dx, double dy) {
        gc.setFill(Color.web("#1e293b"));

        gc.save();
        gc.translate(startX, startY);
        double angle = Math.atan2(dy, dx);
        gc.rotate(Math.toDegrees(angle));

        double length = Math.hypot(dx, dy);
        gc.fillRoundRect(-20, -30, length + 40, 60, 10, 10);
        gc.setStroke(Color.web("#475569"));
        gc.setLineWidth(4.0);
        gc.strokeRoundRect(-20, -30, length + 40, 60, 10, 10);

        gc.setStroke(Color.web("#94a3b8"));
        gc.setLineWidth(1.0);
        for (int i = 0; i <= length; i += 10) {
            double tickLength = (i % 50 == 0) ? 15 : 8;
            gc.strokeLine(i, -10, i, -10 - tickLength);
        }

        gc.setFill(Color.web("#ffffff", 0.2));
        gc.fillRoundRect(0, -5, length, 10, 5, 5);
        gc.setStroke(Color.web("#cbd5e1"));
        gc.setLineWidth(2.0);
        gc.strokeRoundRect(0, -5, length, 10, 5, 5);

        double fillLength = (currentH / 250.0) * length;
        fillLength = Math.max(0, Math.min(fillLength, length));

        LinearGradient liquidGrad = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#d32f2f")),
                new Stop(1, Color.web("#ff5252")));

        gc.setFill(liquidGrad);
        gc.fillRoundRect(2, -3, fillLength, 6, 3, 3);

        gc.restore();

        gc.setStroke(Color.web("#455a64"));
        gc.setLineWidth(12.0);
        gc.strokeLine(startX - 20, startY + 50, startX, startY);
    }
}