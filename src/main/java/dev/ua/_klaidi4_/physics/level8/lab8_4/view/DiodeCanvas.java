package dev.ua._klaidi4_.physics.level8.lab8_4.view;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DiodeCanvas extends Canvas {

    private double currentU = 0.0;
    private double currentI = 0.0;
    private List<Point2D> points = new ArrayList<>();

    public DiodeCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void updateLiveValues(double u, double i) {
        this.currentU = u;
        this.currentI = i;
        draw();
    }

    public void addGraphPoint(double u, double i) {
        points.add(new Point2D(u, i));
        draw();
    }

    public void clearGraph() {
        points.clear();
        draw();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#f4f7f6"));
        gc.fillRect(0, 0, w, h);
        gc.setStroke(Color.web("#e0e4e3"));
        gc.setLineWidth(1.0);
        for (int i = 0; i < w; i += 20) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 20) gc.strokeLine(0, i, w, i);

        drawCircuit(gc);
        drawGraph(gc);
    }

    private void drawCircuit(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2.5);

        gc.strokeLine(60, 100, 60, 180);
        gc.setStroke(Color.web("#d32f2f")); gc.setLineWidth(3); gc.strokeLine(45, 180, 75, 180);
        gc.setStroke(Color.web("#1976d2")); gc.setLineWidth(3); gc.strokeLine(50, 190, 70, 190);
        gc.setStroke(Color.web("#d32f2f")); gc.setLineWidth(3); gc.strokeLine(45, 200, 75, 200);
        gc.setStroke(Color.web("#1976d2")); gc.setLineWidth(3); gc.strokeLine(50, 210, 70, 210);
        gc.setStroke(Color.BLACK); gc.setLineWidth(2.5);
        gc.strokeLine(60, 210, 60, 300);

        gc.strokeLine(60, 100, 280, 100);
        gc.strokeLine(60, 300, 280, 300);

        gc.strokeLine(160, 100, 160, 130);

        gc.setFill(Color.web("#fff8e1"));
        gc.fillOval(140, 130, 40, 40);
        gc.strokeOval(140, 130, 40, 40);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("mA", 148, 155);

        gc.setFill(Color.web("#00b050"));
        gc.fillText(String.format(Locale.US, "%.3f мА", currentI), 185, 155);

        gc.strokeLine(160, 170, 160, 200);

        gc.setFill(Color.web("#333333"));
        gc.fillPolygon(new double[]{145, 175, 160}, new double[]{200, 200, 230}, 3);
        gc.strokePolygon(new double[]{145, 175, 160}, new double[]{200, 200, 230}, 3);
        gc.setLineWidth(3.5);
        gc.strokeLine(145, 230, 175, 230);
        gc.setLineWidth(2.5);
        gc.strokeLine(160, 230, 160, 300);

        gc.strokeLine(160, 185, 240, 185);
        gc.strokeLine(240, 185, 240, 210);

        gc.setFill(Color.web("#e3f2fd"));
        gc.fillOval(220, 210, 40, 40);
        gc.strokeOval(220, 210, 40, 40);
        gc.setFill(Color.BLACK);
        gc.fillText("V", 234, 236);

        gc.setFill(Color.web("#2962ff"));
        gc.fillText(String.format(Locale.US, "%.2f В", currentU), 265, 236);

        gc.strokeLine(240, 250, 240, 265);
        gc.strokeLine(240, 265, 160, 265);
    }

    private void drawGraph(GraphicsContext gc) {
        double gx0 = 530;
        double gy0 = 360;
        double scaleX = 20.0;
        double scaleY = 8.0;

        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(1.5);

        gc.strokeLine(260, gy0, getWidth() - 10, gy0);
        gc.strokeLine(gx0, 20, gx0, 420);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 11));
        gc.fillText("U (В)", getWidth() - 35, gy0 - 10);

        for (int v = -10; v <= -2; v += 2) {
            double px = gx0 + v * scaleX;
            gc.strokeLine(px, gy0 - 4, px, gy0 + 4);
            gc.fillText(String.valueOf(v), px - 10, gy0 + 18);
        }

        for (int i = 10; i <= 30; i += 10) {
            double py = gy0 - i * scaleY;
            gc.strokeLine(gx0 - 4, py, gx0 + 4, py);
            gc.fillText(String.valueOf(i), gx0 - 25, py + 4);
        }

        gc.setFill(Color.RED);
        for (Point2D p : points) {
            double px = gx0 + p.getX() * scaleX;
            double py = gy0 - p.getY() * scaleY;

            if (px >= 260 && px <= getWidth() - 10 && py >= 20 && py <= 420) {
                gc.fillOval(px - 2.5, py - 2.5, 5, 5);
            }
        }

        if (points.size() > 1) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            gc.beginPath();
            boolean first = true;
            for (Point2D p : points) {
                double px = gx0 + p.getX() * scaleX;
                double py = gy0 - p.getY() * scaleY;
                if (px >= 260 && px <= getWidth() - 10 && py >= 20 && py <= 420) {
                    if (first) { gc.moveTo(px, py); first = false; }
                    else { gc.lineTo(px, py); }
                }
            }
            gc.stroke();
        }
    }
}