package dev.ua._klaidi4_.physics.level1.lab1_4.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double m;
    private double mPend;
    private double length;
    private double dx;
    private double v;
}