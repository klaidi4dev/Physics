package dev.ua._klaidi4_.physics.level7.lab7_3.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String liquid;
    private double radius;
    private double l;
    private double time;
    private double expViscosity;
    private double theorViscosity;
}