package dev.ua._klaidi4_.physics.level1.lab1_7.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double m;
    private double bigR;
    private double time;
    private double accel;
    private double expI;
    private double theoI;
}