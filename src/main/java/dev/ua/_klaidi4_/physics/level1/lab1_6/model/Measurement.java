package dev.ua._klaidi4_.physics.level1.lab1_6.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String config;
    private double mTotal;
    private int n;
    private double time;
    private double period;
    private double inertia;
}