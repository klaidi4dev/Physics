package dev.ua._klaidi4_.physics.level6.lab6_4.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String trackName;
    private double x;
    private double y;
    private double rMicrons;
    private double energyMeV;
}