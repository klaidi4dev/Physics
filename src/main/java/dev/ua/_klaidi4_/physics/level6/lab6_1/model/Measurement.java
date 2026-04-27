package dev.ua._klaidi4_.physics.level6.lab6_1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String sourceName;
    private double pressure;
    private double x;
    private int counts;
}