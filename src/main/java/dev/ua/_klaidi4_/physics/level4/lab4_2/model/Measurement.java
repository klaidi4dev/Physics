package dev.ua._klaidi4_.physics.level4.lab4_2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String material;
    private double length;
    private double angle;
    private double mass;
    private int oscillations;
    private double time;
    private double periodExp;
    private double periodTheor;
}