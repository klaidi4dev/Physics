package dev.ua._klaidi4_.physics.level5.lab5_1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String systemName;
    private double d;
    private double x1;
    private double x2;
    private double a;
    private double fExp;
    private double errorPercent;
}