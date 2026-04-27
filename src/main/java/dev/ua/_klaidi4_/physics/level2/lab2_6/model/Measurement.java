package dev.ua._klaidi4_.physics.level2.lab2_6.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String mode;
    private String target;
    private double temperature;
    private double l1;
    private double rEtalon;
    private double resistance;
}