package dev.ua._klaidi4_.physics.level1.lab1_2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String type;
    private double length;
    private int oscillations;
    private double time;
    private double period;
    private double gravity;
}