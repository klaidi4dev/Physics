package dev.ua._klaidi4_.physics.level8.lab8_4.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String type;
    private double u;
    private double i;
    private String rStat;
}