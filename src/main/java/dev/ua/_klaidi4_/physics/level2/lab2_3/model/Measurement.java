package dev.ua._klaidi4_.physics.level2.lab2_3.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String material;
    private double a;
    private double b;
    private double d;
    private double area;
    private double capacitance;
    private double epsilon;
}