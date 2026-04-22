package dev.ua._klaidi4_.physics.lab2_1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double phi;
    private int segment;
    private double deltaR;
    private double deltaL;
    private double eField;
}