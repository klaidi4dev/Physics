package dev.ua._klaidi4_.physics.level5.lab5_7.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private int order;
    private double lengthL;
    private double d;
    private double lm;
    private double lambda;
    private int resolution;
}