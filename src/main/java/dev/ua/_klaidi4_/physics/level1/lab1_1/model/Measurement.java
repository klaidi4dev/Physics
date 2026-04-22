package dev.ua._klaidi4_.physics.level1.lab1_1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double bigM;
    private double smallM;
    private double s1;
    private double s2;
    private double time;
    private double accel;
    private double gravity;
}