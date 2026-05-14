package dev.ua._klaidi4_.physics.level3.lab3_7.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MaxwellBridgeCanvas extends Canvas {

    private double slider = 50;
    private double targetSlider = 50;
    private double indicator = 1;
    private double voltage = 1;
    private boolean balancing = false;

    private AnimationTimer timer;
    private long lastTime = 0;
    private Runnable onFinish;

    public MaxwellBridgeCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void animateBalance(double l1, double l2, double voltage, Runnable onFinish) {
        this.targetSlider = 100.0 * l1 / (l1 + l2);
        this.voltage = voltage;
        this.indicator = 1;
        this.balancing = true;
        this.onFinish = onFinish;

        if (timer != null) timer.stop();
        startAnimation();
    }

    public void reset() {
        slider = 50;
        targetSlider = 50;
        indicator = 1;
        voltage = 0;
        balancing = false;
        draw();
    }

    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    private void startAnimation() {
        lastTime = 0;

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                if (dt > 0.05) dt = 0.05;

                if (balancing) {
                    double diff = targetSlider - slider;
                    slider += diff * dt * 3.5;
                    indicator = Math.min(1.0, Math.abs(diff) / 50.0);

                    if (Math.abs(diff) < 0.15) {
                        slider = targetSlider;
                        indicator = 0;
                        balancing = false;

                        if (onFinish != null) {
                            Runnable callback = onFinish;
                            onFinish = null;
                            callback.run();
                        }
                    }
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

        gc.setFill(Color.web("#eef2f7"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#cbd5e1"));
        gc.setLineWidth(1);
        for (int i = 0; i < w; i += 40) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 40) gc.strokeLine(0, i, w, i);

        gc.setFont(Font.font("System", FontWeight.BOLD, 18));
        gc.setFill(Color.web("#1e293b"));
        gc.fillText("Міст Максвелла", 220, 35);

        double cx = w / 2;
        double cy = h / 2;

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(4);

        gc.strokeLine(cx, 80, 120, cy);
        gc.strokeLine(cx, 80, 480, cy);
        gc.strokeLine(120, cy, cx, 360);
        gc.strokeLine(480, cy, cx, 360);

        drawBlock(gc, "L, R", 210, 105, "#2563eb");
        drawBlock(gc, "Lx, Rx", 360, 105, "#dc2626");
        drawReochord(gc, 150, 330, 300);

        double sliderX = 150 + slider * 3;

        gc.setStroke(Color.web("#16a34a"));
        gc.setLineWidth(3);
        gc.strokeLine(sliderX, 330, cx, cy);

        gc.setFill(Color.web("#16a34a"));
        gc.fillOval(sliderX - 7, 323, 14, 14);

        drawIndicator(gc, cx - 45, cy - 35);
        drawTransformer(gc, 45, cy - 45);
        drawToroid(gc, 500, 290);
    }

    private void drawBlock(GraphicsContext gc, String text, double x, double y, String color) {
        gc.setFill(Color.web(color));
        gc.fillRoundRect(x, y, 80, 55, 10, 10);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 14));
        gc.fillText(text, x + 17, y + 33);
    }

    private void drawReochord(GraphicsContext gc, double x, double y, double width) {
        gc.setFill(Color.web("#facc15"));
        gc.fillRoundRect(x, y - 8, width, 16, 8, 8);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y - 8, width, 16, 8, 8);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("System", FontWeight.NORMAL, 12));
        gc.fillText("A", x - 18, y + 5);
        gc.fillText("B", x + width + 10, y + 5);
        gc.setFill(Color.WHITE);
        gc.fillRoundRect(x + 88, y + 18, 120, 28, 8, 8);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("System", FontWeight.BOLD, 15));
        gc.fillText(String.format("D = %.1f под.", slider), x + 95, y + 38);
    }

    private void drawIndicator(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.web("#111827"));
        gc.fillRoundRect(x, y, 90, 70, 10, 10);

        gc.setFill(Color.web("#dcfce7"));
        gc.fillRect(x + 12, y + 12, 66, 34);

        gc.setStroke(indicator < 0.02 ? Color.web("#16a34a") : Color.RED);
        gc.setLineWidth(3);

        double centerX = x + 45;
        gc.strokeLine(centerX, y + 46, centerX + indicator * 28, y + 18);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 10));
        gc.fillText("ІН", x + 38, y + 62);
    }

    private void drawTransformer(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.web("#334155"));
        gc.fillRoundRect(x, y, 105, 90, 10, 10);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 11));
        gc.fillText("Трансформатор", x + 8, y + 20);

        gc.setFill(Color.web("#020617"));
        gc.fillRect(x + 18, y + 35, 70, 28);

        gc.setFill(Color.web("#22c55e"));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        gc.fillText(String.format("%.1f V", voltage), x + 25, y + 55);
    }

    private void drawToroid(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.web("#0f172a"));
        gc.fillOval(x - 45, y - 45, 90, 90);

        gc.setFill(Color.web("#eef2f7"));
        gc.fillOval(x - 22, y - 22, 44, 44);

        gc.setStroke(Color.web("#f97316"));
        gc.setLineWidth(3);
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30);
            double x1 = x + Math.cos(angle) * 28;
            double y1 = y + Math.sin(angle) * 28;
            double x2 = x + Math.cos(angle) * 48;
            double y2 = y + Math.sin(angle) * 48;
            gc.strokeLine(x1, y1, x2, y2);
        }

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.fillText("Тороїд", x - 22, y + 65);
    }
}