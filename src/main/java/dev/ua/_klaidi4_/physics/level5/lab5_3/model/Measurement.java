package dev.ua._klaidi4_.physics.level5.lab5_3.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String plate;
    private double d;
    private double z1;
    private double z2;
    private double h;
    private double nExp;
    private double errorPercent;
}