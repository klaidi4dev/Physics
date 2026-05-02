package dev.ua._klaidi4_.physics.level8.lab8_5.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TunnelDiodeCanvas extends Canvas {

    private double targetU = 0;
    private double currentU = 0;
    private String mode = "Прямий";
    private double currentI = 0;
    private AnimationTimer timer;

    private class Electron {
        double x, y, speed;
        double alpha = 1.0;
        Electron(double x, double y, double speed) {
            this.x = x;
            this.y = y;
            this.speed = speed;
        }
    }

    private List<Electron> electrons = new ArrayList<>();

    public TunnelDiodeCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    public void updateState(double voltage, String mode, double current) {
        this.targetU = mode.equals("Прямий") ? voltage : -voltage;
        this.mode = mode;
        this.currentI = current;
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                currentU += (targetU - currentU) * 0.1;
                updateParticles();
                drawFrame();
            }
        };
        timer.start();
    }

    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    private void updateParticles() {
        double w = getWidth();
        double h = getHeight();
        double ef_base = h / 2.0;
        double scale = 250.0;
        double p_shift = currentU * scale;

        double ev_p = ef_base - 30 + p_shift;
        double ef_p = ef_base + p_shift;

        double ec_n = ef_base + 30;
        double ef_n = ef_base;

        int spawnRate = (int) (currentI / 10.0);
        if (spawnRate > 0 && Math.random() < 0.3) {
            for (int i = 0; i < spawnRate; i++) {
                if (currentU > 0) {
                    double spawnY = ec_n + Math.random() * Math.max(0, (ef_n - ec_n));
                    if (spawnY > ev_p && spawnY < ef_p) {
                        electrons.add(new Electron(w / 2 + 40, spawnY, -3 - Math.random() * 2));
                    }
                } else if (currentU < 0) {
                    double spawnY = ef_p + Math.random() * 40;
                    if (spawnY < ec_n) continue;
                    electrons.add(new Electron(w / 2 - 40, spawnY, 3 + Math.random() * 2));
                }
            }
        }

        Iterator<Electron> it = electrons.iterator();
        while (it.hasNext()) {
            Electron e = it.next();
            e.x += e.speed;
            if (e.x < w / 2 - 80 || e.x > w / 2 + 80) {
                e.alpha -= 0.05;
                if (e.alpha <= 0) it.remove();
            }
        }
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

        double ef_base = h / 2.0;
        double scale = 250.0;
        double p_shift = currentU * scale;

        double ev_p = ef_base - 30 + p_shift;
        double ec_p = ev_p - 140;
        double ef_p = ef_base + p_shift;

        double ec_n = ef_base + 30;
        double ev_n = ec_n + 140;
        double ef_n = ef_base;

        double transWidth = 80;
        double leftX = w / 2 - transWidth / 2;
        double rightX = w / 2 + transWidth / 2;

        gc.setFill(Color.web("#1e3a8a", 0.6));
        gc.fillRect(0, ev_p, leftX, h - ev_p);
        gc.setFill(Color.web("#7f1d1d", 0.6));
        gc.fillRect(rightX, 0, w - rightX, ec_n);

        gc.setStroke(Color.web("#60a5fa"));
        gc.setLineWidth(2.0);
        gc.strokeLine(0, ev_p, leftX, ev_p);
        gc.strokeLine(0, ec_p, leftX, ec_p);

        gc.setStroke(Color.web("#f87171"));
        gc.strokeLine(rightX, ec_n, w, ec_n);
        gc.strokeLine(rightX, ev_n, w, ev_n);

        gc.beginPath();
        gc.moveTo(leftX, ev_p);
        gc.bezierCurveTo(w / 2, ev_p, w / 2, ev_n, rightX, ev_n);
        gc.setStroke(Color.WHITE);
        gc.stroke();

        gc.beginPath();
        gc.moveTo(leftX, ec_p);
        gc.bezierCurveTo(w / 2, ec_p, w / 2, ec_n, rightX, ec_n);
        gc.stroke();

        gc.setStroke(Color.web("#a3e635"));
        gc.setLineDashes(5, 5);
        gc.strokeLine(0, ef_p, leftX, ef_p);
        gc.strokeLine(rightX, ef_n, w, ef_n);
        gc.setLineDashes(null);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", 14));
        gc.fillText("p-область", 20, 30);
        gc.fillText("n-область", w - 100, 30);

        gc.setFill(Color.web("#a3e635"));
        gc.fillText("E_F", 10, ef_p - 5);
        gc.fillText("E_F", w - 30, ef_n - 5);

        gc.setFill(Color.web("#60a5fa"));
        gc.fillText("E_v", 10, ev_p + 15);
        gc.setFill(Color.web("#f87171"));
        gc.fillText("E_c", w - 30, ec_n - 5);

        for (Electron e : electrons) {
            gc.setFill(Color.color(1.0, 1.0, 0.0, Math.max(0, e.alpha)));
            gc.fillOval(e.x - 3, e.y - 3, 6, 6);
        }

        if (Math.abs(currentU) > 0.001) {
            gc.setStroke(Color.web("#ffeb3b"));
            gc.setLineWidth(1.0);
            gc.strokeLine(w / 2, ef_p, w / 2, ef_n);
            gc.fillText(String.format("eU = %.2f еВ", Math.abs(currentU)), w / 2 + 5, (ef_p + ef_n) / 2);
        }
    }
}