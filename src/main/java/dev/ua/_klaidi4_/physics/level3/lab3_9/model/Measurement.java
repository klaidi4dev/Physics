package dev.ua._klaidi4_.physics.level3.lab3_9.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String material;
    private double temperature;
    private double current;
}