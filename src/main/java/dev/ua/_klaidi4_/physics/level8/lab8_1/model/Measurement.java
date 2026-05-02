package dev.ua._klaidi4_.physics.level8.lab8_1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double tC;
    private double tK;
    private double invT;
    private double rKohm;
    private double lnR;
}