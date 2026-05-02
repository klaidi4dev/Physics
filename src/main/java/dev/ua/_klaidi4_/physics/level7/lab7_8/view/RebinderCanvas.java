package dev.ua._klaidi4_.physics.level7.lab7_8.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class RebinderCanvas extends Canvas {

    private boolean isPumping = false;
    private String currentLiquid = "Дистильована вода";
    private Color liquidColor = Color.web("#4fc3f7", 0.6);
    private double currentH = 0.0;
    private double targetH = 150.0;
    private double bubbleRadius = 0.0;
    private final double MAX_BUBBLE_RADIUS = 8.0;

    private AnimationTimer timer;
    private double simSpeed = 25.0;

    private Runnable onBubblePop;

    public RebinderCanvas(double width, double height) {
        super(width, height);
        startAnimation();
    }

    public void setLiquidParams(String name, double theoreticalAlpha) {
        this.currentLiquid = name;
        this.targetH = (theoreticalAlpha / 72.8) * 150.0;

        if (name.contains("вода")) {
            this.liquidColor = Color.web("#4fc3f7", 0.6);
        } else if (name.contains("Спирт")) {
            this.liquidColor = Color.web("#e0e0e0", 0.6);
        } else if (name.contains("Гліцерин")) {
            this.liquidColor = Color.web("#ffb74d", 0.6);
        }

        this.currentH = 0;
        this.bubbleRadius = 0;
        draw();
    }

    public void setPumping(boolean pumping) {
        this.isPumping = pumping;
    }

    public double getCurrentH() {
        return currentH;
    }

    public void setOnBubblePop(Runnable callback) {
        this.onBubblePop = callback;
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }
                double dt = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;

                if (isPumping) {
                    currentH += simSpeed * dt;

                    bubbleRadius = MAX_BUBBLE_RADIUS * (currentH / targetH);

                    if (currentH >= targetH) {
                        currentH = targetH * 0.7;
                        bubbleRadius = 0;
                        if (onBubblePop != null) {
                            onBubblePop.run();
                        }
                    }
                } else {
                    if (currentH > 0) {
                        currentH -= 5.0 * dt;
                        if (currentH < 0) currentH = 0;
                        bubbleRadius = MAX_BUBBLE_RADIUS * (currentH / targetH);
                    }
                }
                draw();
            }
        };
        timer.start();
    }

    public void stopAnimation() {
        if (timer != null) timer.stop();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, w, h);

        double beakerX = 100;
        double beakerY = h - 150;
        double beakerW = 120;
        double beakerH = 100;

        gc.setFill(liquidColor);
        gc.fillRect(beakerX, beakerY + 30, beakerW, beakerH - 30);

        gc.setStroke(Color.web("#90a4ae"));
        gc.setLineWidth(3);
        gc.strokeLine(beakerX, beakerY, beakerX, beakerY + beakerH);
        gc.strokeLine(beakerX + beakerW, beakerY, beakerX + beakerW, beakerY + beakerH);
        gc.strokeLine(beakerX, beakerY + beakerH, beakerX + beakerW, beakerY + beakerH);

        double capX = beakerX + beakerW / 2;
        double capY = beakerY - 100;
        double capW = 6;
        double capH = 132;

        gc.setFill(Color.web("#cfd8dc"));
        gc.fillRect(capX - capW / 2, capY, capW, capH);
        gc.setStroke(Color.web("#607d8b"));
        gc.setLineWidth(2);
        gc.strokeLine(capX - capW / 2, capY, capX - capW / 2, capY + capH);
        gc.strokeLine(capX + capW / 2, capY, capX + capW / 2, capY + capH);

        if (bubbleRadius > 0) {
            gc.setFill(Color.web("#e0f7fa", 0.8));
            gc.setStroke(Color.web("#00bcd4"));
            gc.setLineWidth(1);
            gc.fillOval(capX - bubbleRadius, capY + capH, bubbleRadius * 2, bubbleRadius * 2);
            gc.strokeOval(capX - bubbleRadius, capY + capH, bubbleRadius * 2, bubbleRadius * 2);
        }

        double manX = 350;
        double manY = h - 50;
        double manAngle = Math.toRadians(-30);
        double manLength = 250;

        gc.save();
        gc.translate(manX, manY);
        gc.rotate(Math.toDegrees(manAngle));

        gc.setStroke(Color.web("#b0bec5"));
        gc.setLineWidth(12);
        gc.strokeLine(0, 0, manLength, 0);

        gc.setFill(Color.web("#eceff1"));
        gc.fillRoundRect(0, -4, manLength, 8, 4, 4);

        double visualLevel = Math.min(manLength - 10, currentH);
        gc.setFill(Color.web("#ef5350", 0.9));
        gc.fillRoundRect(0, -3, visualLevel, 6, 3, 3);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        for (int i = 0; i <= 200; i += 10) {
            double tickH = (i % 50 == 0) ? 8 : 4;
            gc.strokeLine(i, 6, i, 6 + tickH);
            if (i % 50 == 0) {
                gc.setFont(Font.font("System", 10));
                gc.fillText(String.valueOf(i), i - 8, 25);
            }
        }
        gc.restore();

        gc.setFill(Color.web("#37474f"));
        gc.setFont(Font.font("System", FontWeight.BOLD, 14));
        gc.fillText("Досліджувана рідина: " + currentLiquid, 20, 30);
        gc.fillText(String.format("Показник манометра h: %.1f мм", currentH), manX + 20, manY - 150);

        if (isPumping) {
            gc.setFill(Color.web("#e53935"));
            gc.setFont(Font.font("System", FontWeight.BOLD, 12));
            gc.fillText("СТАТУС: Нагнітання повітря...", 20, 50);
        }
    }
}