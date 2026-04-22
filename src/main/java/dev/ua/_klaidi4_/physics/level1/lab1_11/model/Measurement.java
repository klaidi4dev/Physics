package dev.ua._klaidi4_.physics.level1.lab1_11.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double m;
    private double h;
    private double time;
    private double expI;
    private double theoI;
}