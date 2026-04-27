package dev.ua._klaidi4_.physics.level7.lab7_2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double volume;
    private double h1;
    private double h2;
    private double deltaP;
    private double tau;
    private double temp;
    private double eta;
    private double lambda;
}