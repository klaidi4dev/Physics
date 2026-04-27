package dev.ua._klaidi4_.physics.level2.lab2_5.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String source;
    private double length;
    private double currentG;
}