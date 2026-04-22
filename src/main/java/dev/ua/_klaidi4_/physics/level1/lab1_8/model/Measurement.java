package dev.ua._klaidi4_.physics.level1.lab1_8.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String config;
    private int n;
    private double time;
    private double period;
    private double expI;
    private double theoI;
}