package dev.ua._klaidi4_.physics.level5.lab5_5.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String filterColor;
    private double rCurvature;
    private double r5;
    private double r11;
    private double lambdaExp;
    private double errorPercent;
}