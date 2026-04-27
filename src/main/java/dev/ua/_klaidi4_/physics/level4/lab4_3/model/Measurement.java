package dev.ua._klaidi4_.physics.level4.lab4_3.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String mode;
    private double f1;
    private double f2;
    private double phase;
}