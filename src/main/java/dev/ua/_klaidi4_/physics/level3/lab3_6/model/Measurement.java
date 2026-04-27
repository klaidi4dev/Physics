package dev.ua._klaidi4_.physics.level3.lab3_6.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double z;
    private double u0;
    private double f;
    private double eps;
    private double m;
}