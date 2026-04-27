package dev.ua._klaidi4_.physics.level2.lab2_4.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double temperature;
    private double currentIc;
    private double epsilon;
    private double ratio;
}