package dev.ua._klaidi4_.physics.level8.lab8_2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double iMain;
    private double iHall;
    private double uHall;
    private double rHall;
    private double n;
}