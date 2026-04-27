package dev.ua._klaidi4_.physics.level7.lab7_8.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String liquidName;
    private double h;
    private double alpha;
}