package dev.ua._klaidi4_.physics.level7.lab7_7.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double voltage;
    private double current;
    private double sqrtVoltage;
    private double u;
    private double fU;
}