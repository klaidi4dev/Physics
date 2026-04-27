package dev.ua._klaidi4_.physics.level4.lab4_5.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double f;
    private int n;
    private double nuExp;
    private double errorPercent;
}