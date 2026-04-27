package dev.ua._klaidi4_.physics.level4.lab4_1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String axis;
    private double xD;
    private int n;
    private double time;
    private double period;
}