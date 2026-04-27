package dev.ua._klaidi4_.physics.level8.lab8_4.view;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

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

        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, w, h);
        gc.setStroke(Color.web("#e9ecef"));
        gc.setLineWidth(1);
        for (int i = 0; i < w; i += 20) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 20) gc.strokeLine(0, i, w, i);

        drawCircuit(gc);
        drawGraph(gc);
    }

    private void drawCircuit(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2.5);

        gc.strokeLine(30, 80, 30, 160);
        gc.setStroke(Color.web("#d32f2f")); gc.setLineWidth(4); gc.strokeLine(15, 160, 45, 160);
        gc.setStroke(Color.web("#1976d2")); gc.setLineWidth(2); gc.strokeLine(20, 170, 40, 170);
        gc.setStroke(Color.web("#d32f2f")); gc.setLineWidth(4); gc.strokeLine(15, 180, 45, 180);
        gc.setStroke(Color.web("#1976d2")); gc.setLineWidth(2); gc.strokeLine(20, 190, 40, 190);
        gc.setStroke(Color.BLACK); gc.setLineWidth(2.5);
        gc.strokeLine(30, 190, 30, 280);

        gc.strokeLine(30, 80, 250, 80);
        gc.strokeLine(30, 280, 250, 280);
        gc.strokeLine(130, 80, 130, 120);

        gc.setFill(Color.web("#fff3e0"));
        gc.fillOval(110, 120, 40, 40);
        gc.strokeOval(110, 120, 40, 40);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("System", FontWeight.BOLD, 14));
        gc.fillText("mA", 118, 145);

        gc.strokeLine(130, 160, 130, 190);

        gc.setFill(Color.web("#424242"));
        gc.fillPolygon(new double[]{115, 145, 130}, new double[]{190, 190, 220}, 3);
        gc.strokePolygon(new double[]{115, 145, 130}, new double[]{190, 190, 220}, 3);
        gc.setLineWidth(4);
        gc.strokeLine(115, 220, 145, 220);
        gc.setLineWidth(2.5);
        gc.strokeLine(130, 220, 130, 280);

        gc.strokeLine(130, 175, 210, 175);
        gc.strokeLine(210, 175, 210, 190);
        gc.setFill(Color.web("#e3f2fd"));
        gc.fillOval(190, 190, 40, 40);
        gc.strokeOval(190, 190, 40, 40);
        gc.setFill(Color.BLACK);
        gc.fillText("V", 205, 215);
        gc.strokeLine(210, 230, 210, 245);
        gc.strokeLine(210, 245, 130, 245);

        gc.setFill(Color.web("#00c853"));
        gc.fillText(String.format("%.3f мА", currentI), 155, 140);
        gc.setFill(Color.web("#2962ff"));
        gc.fillText(String.format("%.2f В", currentU), 235, 215);
    }

    private void drawGraph(GraphicsContext gc) {
        double gx0 = 500;
        double gy0 = 340;
        double scaleX = 25;
        double scaleY = 10;

        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(1.5);

        gc.strokeLine(230, gy0, 580, gy0);
        gc.strokeLine(gx0, 20, gx0, 400);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("System", 12));
        gc.fillText("U (В)", 550, gy0 - 10);
        gc.fillText("I (мА)", gx0 + 10, 30);

        for (int v = -10; v <= 1; v += 2) {
            double px = gx0 + v * scaleX;
            gc.strokeLine(px, gy0 - 3, px, gy0 + 3);
            if (v != 0) gc.fillText(String.valueOf(v), px - 8, gy0 + 18);
        }

        for (int i = 10; i <= 30; i += 10) {
            double py = gy0 - i * scaleY;
            gc.strokeLine(gx0 - 3, py, gx0 + 3, py);
            gc.fillText(String.valueOf(i), gx0 - 25, py + 4);
        }
        for (int i = -10; i >= -30; i -= 10) {
            double py = gy0 - i * scaleY;
            gc.strokeLine(gx0 - 3, py, gx0 + 3, py);
            gc.fillText(String.valueOf(i), gx0 - 28, py + 4);
        }

        gc.setFill(Color.RED);
        for (Point2D p : points) {
            double px = gx0 + p.getX() * scaleX;
            double py = gy0 - p.getY() * scaleY;

            if (py >= 20 && py <= 420 && px >= 230 && px <= 580) {
                gc.fillOval(px - 3, py - 3, 6, 6);
            }
        }

        if (points.size() > 1) {
            gc.setStroke(Color.web("#ff0000", 0.6));
            gc.setLineWidth(2);
            gc.beginPath();
            boolean first = true;
            for (Point2D p : points) {
                double px = gx0 + p.getX() * scaleX;
                double py = gy0 - p.getY() * scaleY;
                if (py >= 20 && py <= 420 && px >= 230 && px <= 580) {
                    if (first) { gc.moveTo(px, py); first = false; }
                    else { gc.lineTo(px, py); }
                }
            }
            gc.stroke();
        }
    }
}