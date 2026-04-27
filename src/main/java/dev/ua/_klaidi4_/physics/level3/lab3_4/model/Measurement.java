package dev.ua._klaidi4_.physics.level3.lab3_4.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double x;
    private double current;
    private double alpha;
    private double expH;
    private double theoH;
    private double errorPercent;
}