package dev.ua._klaidi4_.physics.level7.lab7_4.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String metalName;
    private double t1;
    private double t2;
    private double t3;
    private double t4;
    private double volumeMl;
    private double timeSec;
    private double kValue;
}