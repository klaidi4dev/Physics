package dev.ua._klaidi4_.physics.level6.lab6_2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String sampleType;
    private int timeSec;
    private int counts;
    private double activity;
}