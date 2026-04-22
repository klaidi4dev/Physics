package dev.ua._klaidi4_.physics.level1.lab1_9.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double t1;
    private double period1;
    private double t2;
    private double period2;
    private double phi;
    private double v;
}