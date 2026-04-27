package dev.ua._klaidi4_.physics.level3.lab3_5.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String state;
    private double u;
    private double i;
    private double p;
}