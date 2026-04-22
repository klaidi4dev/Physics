package dev.ua._klaidi4_.physics.level1.lab1_13.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double beta;
    private double a0;
    private int n;
    private double an;
    private double f;
}