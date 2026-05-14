package dev.ua._klaidi4_.physics.level3.lab3_8.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

public class OscilloscopeCanvas extends Canvas {

    private double amplitude = 100.0;
    private String material = "М'який (Трансформаторна сталь)";
    private double mouseNx = 0;
    private double mouseNy = 0;
    private boolean mouseInCanvas = false;
    private final double DIV_SIZE = 40.0;
    private final int X_DIVS = 10;
    private final int Y_DIVS = 8;

    public OscilloscopeCanvas(double width, double height) {
        super(width, height);

        this.setOnMouseMoved(e -> {
            mouseInCanvas = true;
            mouseNx = (e.getX() - getWidth() / 2) / DIV_SIZE;
            mouseNy = -(e.getY() - getHeight() / 2) / DIV_SIZE;
            draw();
        });

        this.setOnMouseExited(e -> {
            mouseInCanvas = false;
            draw();
        });

        startRenderLoop();
    }

    public void setParameters(double amplitude, String material) {
        this.amplitude = amplitude;
        this.material = material;
    }

    public double getMouseNx() { return mouseNx; }
    public double getMouseNy() { return mouseNy; }

    public double getPeakNx() {
        return calculateCurvePoints().stream().mapToDouble(p -> p[0]).max().orElse(0);
    }

    public double getPeakNy() {
        return calculateCurvePoints().stream().mapToDouble(p -> p[1]).max().orElse(0);
    }

    public List<double[]> calculateCurvePoints() {
        List<double[]> points = new ArrayList<>();
        if (amplitude <= 0) {
            points.add(new double[]{0, 0});
            return points;
        }

        double saturationY = material.contains("М'який") ? 3.0 : 2.5;
        double coercivity = material.contains("М'який") ? 0.4 : 1.5;
        double steepness = material.contains("М'який") ? 1.2 : 2.0;
        double scale = amplitude / 100.0;
        double currentMaxX = 4.5 * scale;

        for (double t = 0; t <= 2 * Math.PI; t += 0.05) {
            double x = currentMaxX * Math.cos(t);
            double y = saturationY * Math.tanh(x / steepness) + coercivity * Math.sin(t) * Math.sqrt(scale);
            points.add(new double[]{x, y});
        }
        return points;
    }

    private void startRenderLoop() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                draw();
            }
        };
        timer.start();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();
        double cx = w / 2;
        double cy = h / 2;

        gc.setFill(Color.web("#071a0f"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#1b5e20", 0.6));
        gc.setLineWidth(1);

        for (int i = 0; i <= w / DIV_SIZE; i++) {
            gc.strokeLine(cx + i * DIV_SIZE, 0, cx + i * DIV_SIZE, h);
            gc.strokeLine(cx - i * DIV_SIZE, 0, cx - i * DIV_SIZE, h);
        }
        for (int i = 0; i <= h / DIV_SIZE; i++) {
            gc.strokeLine(0, cy + i * DIV_SIZE, w, cy + i * DIV_SIZE);
            gc.strokeLine(0, cy - i * DIV_SIZE, w, cy - i * DIV_SIZE);
        }

        gc.setStroke(Color.web("#4caf50", 0.8));
        gc.setLineWidth(2);
        gc.strokeLine(0, cy, w, cy);
        gc.strokeLine(cx, 0, cx, h);

        List<double[]> points = calculateCurvePoints();
        if (!points.isEmpty()) {
            for (int glow = 3; glow >= 1; glow--) {
                gc.setStroke(Color.web("#aeea00", 0.3 / glow));
                gc.setLineWidth(glow * 3);
                gc.beginPath();
                boolean first = true;
                for (double[] p : points) {
                    double screenX = cx + p[0] * DIV_SIZE;
                    double screenY = cy - p[1] * DIV_SIZE;
                    if (first) {
                        gc.moveTo(screenX, screenY);
                        first = false;
                    } else {
                        gc.lineTo(screenX, screenY);
                    }
                }
                gc.closePath();
                gc.stroke();
            }

            gc.setStroke(Color.web("#c6ff00"));
            gc.setLineWidth(1.5);
            gc.beginPath();
            boolean first = true;
            for (double[] p : points) {
                double screenX = cx + p[0] * DIV_SIZE;
                double screenY = cy - p[1] * DIV_SIZE;
                if (first) {
                    gc.moveTo(screenX, screenY);
                    first = false;
                } else {
                    gc.lineTo(screenX, screenY);
                }
            }
            gc.closePath();
            gc.stroke();
        }

        if (mouseInCanvas) {
            double sx = cx + mouseNx * DIV_SIZE;
            double sy = cy - mouseNy * DIV_SIZE;

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1);
            gc.strokeLine(sx - 10, sy, sx + 10, sy);
            gc.strokeLine(sx, sy - 10, sx, sy + 10);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Monospaced", 12));
            gc.fillText(String.format(java.util.Locale.US, "nx:%.2f ny:%.2f", mouseNx, mouseNy), sx + 10, sy - 10);
        }
    }
}