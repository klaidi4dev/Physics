package dev.ua._klaidi4_.physics.level1.lab1_12.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double m;
    private double time;
    private double omegaP;
    private double torque;
    private double momentum;
    private double expI;
    private double theoI;
}