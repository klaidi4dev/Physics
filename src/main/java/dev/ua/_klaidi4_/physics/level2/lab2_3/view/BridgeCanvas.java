package dev.ua._klaidi4_.physics.level2.lab2_3.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class BridgeCanvas extends Canvas {

    private String currentDisplay = "---- pF";
    private double sampleThickness = 2.0;
    private Color sampleColor = Color.web("#80cbc4");
    private boolean isMeasuring = false;

    public BridgeCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void updateDisplay(String text, boolean measuring) {
        this.currentDisplay = text;
        this.isMeasuring = measuring;
        draw();
    }

    public void updateSample(double thicknessMm, String materialType) {
        this.sampleThickness = thicknessMm;
        switch (materialType) {
            case "Титанат барію (BaTiO3)": this.sampleColor = Color.web("#9ccc65"); break;
            case "Сегнетова сіль": this.sampleColor = Color.web("#ce93d8"); break;
            case "Скло": this.sampleColor = Color.web("#90caf9"); break;
            default: this.sampleColor = Color.GRAY;
        }
        draw();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#eceff1"));
        gc.fillRect(0, 0, w, h);
        gc.setStroke(Color.web("#cfd8dc"));
        gc.setLineWidth(1);
        for (int i = 0; i < w; i += 20) gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += 20) gc.strokeLine(0, i, w, i);

        double devWidth = 260;
        double devHeight = 160;
        double devX = w / 2 - devWidth / 2;
        double devY = h - devHeight - 30;

        LinearGradient deviceGrad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#cfd8dc")), new Stop(1, Color.web("#78909c")));
        gc.setFill(deviceGrad);
        gc.fillRoundRect(devX, devY, devWidth, devHeight, 15, 15);
        gc.setStroke(Color.web("#455a64"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(devX, devY, devWidth, devHeight, 15, 15);
        gc.setFill(Color.web("#1c2833"));
        gc.fillRect(devX + 30, devY + 20, 200, 60);
        gc.setStroke(Color.web("#17202a"));
        gc.strokeRect(devX + 30, devY + 20, 200, 60);
        gc.setFill(isMeasuring ? Color.web("#aed6f1") : Color.web("#00ff00"));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 26));
        gc.fillText(currentDisplay, devX + 45, devY + 60);
        gc.setFill(Color.web("#37474f"));
        gc.fillOval(devX + 50, devY + 100, 40, 40);
        gc.fillOval(devX + 170, devY + 100, 40, 40);
        gc.setFill(Color.web("#b0bec5"));
        gc.fillOval(devX + 60, devY + 110, 20, 20);
        gc.fillOval(devX + 180, devY + 110, 20, 20);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.fillText("Р-577", devX + 115, devY + 125);

        double capY = 60;
        gc.setStroke(Color.web("#d32f2f"));
        gc.setLineWidth(3);
        gc.strokeLine(devX + 80, devY, w / 2 - 50, capY + 40);
        gc.setStroke(Color.web("#1976d2"));
        gc.strokeLine(devX + 180, devY, w / 2 + 50, capY + 40);

        double visualThickness = Math.max(10, sampleThickness * 10);
        double plateWidth = 160;

        gc.setFill(sampleColor);
        gc.fillRect(w / 2 - plateWidth / 2, capY, plateWidth, visualThickness);

        LinearGradient plateGrad = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#b0bec5")), new Stop(0.5, Color.WHITE), new Stop(1, Color.web("#90a4ae")));
        gc.setFill(plateGrad);
        gc.fillRect(w / 2 - plateWidth / 2, capY - 10, plateWidth, 10);
        gc.fillRect(w / 2 - plateWidth / 2, capY + visualThickness, plateWidth, 10);

        if (!isMeasuring) {
            gc.setStroke(Color.web("#ffffff", 0.6));
            gc.setLineWidth(1.5);
            for (int i = -60; i <= 60; i += 25) {
                gc.strokeLine(w / 2 + i, capY + 5, w / 2 + i, capY + visualThickness - 5);
                gc.strokeLine(w / 2 + i, capY + 5, w / 2 + i - 3, capY + 10);
                gc.strokeLine(w / 2 + i, capY + 5, w / 2 + i + 3, capY + 10);
            }
        }

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("System", FontWeight.NORMAL, 14));
        gc.fillText("Зразок між обкладками", w / 2 - 75, capY - 20);
    }
}