package dev.ua._klaidi4_.physics.level7.lab7_5.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double tk1;
    private double tk2;
    private double tkAvg;
    private double a;
    private double b;
}