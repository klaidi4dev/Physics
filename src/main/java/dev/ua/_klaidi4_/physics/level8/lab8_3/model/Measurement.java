package dev.ua._klaidi4_.physics.level8.lab8_3.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double voltage;
    private double distance;
    private double illuminance;
    private double current;
}