package dev.ua._klaidi4_.physics.level6.lab6_5.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MagnetronCanvas extends Canvas {

    private double currentIc = 0.0;
    private double currentUa = 6.3;
    private double currentRatio = 0.0;
    private AnimationTimer timer;
    private List<Electron> electrons = new ArrayList<>();

    private class Electron {
        double x, y, vx, vy;
        public Electron(double angle) {
            this.x = 2 * Math.cos(angle);
            this.y = 2 * Math.sin(angle);
            this.vx = 0.5 * Math.cos(angle);
            this.vy = 0.5 * Math.sin(angle);
        }
    }

    public MagnetronCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    public void updatePhysicsParameters(double ratio, double ua, double ic) {
        this.currentRatio = ratio;
        this.currentUa = ua;
        this.currentIc = ic;
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updatePhysics();
                draw();
            }
        };
        timer.start();
    }

    private void updatePhysics() {
        for (int i = 0; i < 5; i++) {
            electrons.add(new Electron(Math.random() * Math.PI * 2));
        }

        Iterator<Electron> it = electrons.iterator();

        double visualBkr = 0.13 * Math.sqrt(currentUa / 6.3);
        double B_visual = currentRatio * visualBkr;

        while (it.hasNext()) {
            Electron e = it.next();
            double r = Math.hypot(e.x, e.y);

            if (r >= 98 || (r < 1.5 && Math.hypot(e.vx, e.vy) > 1.0)) {
                it.remove();
                continue;
            }

            double Er = (150.0 * (currentUa / 6.3)) / Math.max(r, 1.0);
            double ax = Er * (e.x / r) - e.vy * B_visual;
            double ay = Er * (e.y / r) + e.vx * B_visual;
            double dt = 0.08;

            e.vx += ax * dt;
            e.vy += ay * dt;
            e.x += e.vx * dt;
            e.y += e.vy * dt;
        }
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();
        double cx = w / 2;
        double cy = h / 2;

        gc.setFill(Color.web("#1e272e", 0.4));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#0fbcf9", 0.25));
        gc.setLineWidth(1);
        int density = (int) (currentRatio * 15);
        if (density > 0 && density < 50) {
            double step = 220.0 / density;
            for (int i = 0; i < density; i++) {
                for (int j = 0; j < density; j++) {
                    double fx = cx - 110 + i * step;
                    double fy = cy - 110 + j * step;
                    if (Math.hypot(fx - cx, fy - cy) < 95) {
                        gc.strokeLine(fx - 2, fy - 2, fx + 2, fy + 2);
                        gc.strokeLine(fx - 2, fy + 2, fx + 2, fy - 2);
                    }
                }
            }
        }

        gc.setStroke(Color.web("#95a5a6"));
        gc.setLineWidth(6);
        gc.strokeOval(cx - 100, cy - 100, 200, 200);

        gc.setFill(Color.web("#ff4757"));
        gc.fillOval(cx - 4, cy - 4, 8, 8);

        gc.setFill(Color.web("#f1c40f"));
        for (Electron e : electrons) {
            gc.fillOval(cx + e.x - 1.5, cy + e.y - 1.5, 3, 3);
        }

        gc.setFill(Color.WHITE);
        gc.fillText(String.format(Locale.US, "Напруга U_a = %.1f В", currentUa), 10, 20);
        gc.fillText(String.format(Locale.US, "Струм соленоїда I_c = %.3f А", currentIc), 10, 40);

        if (currentRatio > 1.0) {
            gc.setFill(Color.web("#ff4757"));
            gc.fillText("РЕЖИМ ВІДСІЧІ: Хмара просторового заряду", 10, 60);
        }
    }
    public void stopAnimation() {
        if (timer != null) {
            timer.stop();
        }
    }
}