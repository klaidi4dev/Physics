package dev.ua._klaidi4_.physics.level1.lab1_5.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double m;
    private double l;
    private double angle;
    private double v;
    private double tau;
    private double force;
}