package dev.ua._klaidi4_.physics.level2.lab2_2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String connectionType;
    private double cMeasured;
    private double cTheoretical;
    private double errorPct;
}