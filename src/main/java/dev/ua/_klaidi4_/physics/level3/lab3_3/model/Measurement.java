package dev.ua._klaidi4_.physics.level3.lab3_3.model;

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
    private double expEm;
    private double theoEm;
    private double errorPercent;
}