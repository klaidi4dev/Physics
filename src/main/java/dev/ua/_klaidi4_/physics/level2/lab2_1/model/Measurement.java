package dev.ua._klaidi4_.physics.level2.lab2_1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double phi1;
    private double phi2;
    private double dPhi;
    private double x1;
    private double x2;
    private double dX;
    private double eField;
}