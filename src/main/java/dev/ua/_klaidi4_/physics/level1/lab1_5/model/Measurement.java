package dev.ua._klaidi4_.physics.level1.lab1_5.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double c;
    private double r;
    private double n0;
    private double n;
    private double dh;
    private double radius;
    private double tau;
    private double v;
    private double force;
}