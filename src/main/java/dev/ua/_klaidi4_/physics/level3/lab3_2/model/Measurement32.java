package dev.ua._klaidi4_.physics.level3.lab3_2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement32 {
    private int id;
    private int n;
    private double r;
    private double currentI;
    private String polarity;
    private double angle;
    private double h0;
}