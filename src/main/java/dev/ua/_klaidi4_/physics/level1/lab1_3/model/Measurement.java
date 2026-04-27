package dev.ua._klaidi4_.physics.level1.lab1_3.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String type;
    private double m1;
    private double m2;
    private double alpha1;
    private double alpha1Prime;
    private double alpha2Prime;
    private double v1;
    private double u1;
    private double u2;
    private double tau;
    private double deltaW;
}