package dev.ua._klaidi4_.physics.level5.lab5_6.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String filterColor;
    private double dGrating;
    private double phi1;
    private double phi2;
    private double lambdaExp;
    private double errorPercent;
}