package dev.ua._klaidi4_.physics.level5.lab5_4.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String filterColor;
    private double dy;
    private double l1;
    private double x1;
    private double lambdaExp;
    private double errorPercent;
}