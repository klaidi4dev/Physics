package dev.ua._klaidi4_.physics.level4.lab4_6.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double tension;
    private int n;
    private double fExp;
    private double nuTheo;
    private double errorPercent;
}