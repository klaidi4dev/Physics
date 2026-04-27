package dev.ua._klaidi4_.physics.level6.lab6_3.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String isotope;
    private double x;
    private int timeSec;
    private int counts;
    private double lnCounts;
}