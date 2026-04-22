package dev.ua._klaidi4_.physics.level1.lab1_14.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double r;
    private double time;
    private double velocity;
    private double eta;
}