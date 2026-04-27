package dev.ua._klaidi4_.physics.level4.lab4_4.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double rd;
    private int n;
    private double nDivs;
    private double um;
    private double umNext;
    private double period;
    private double decrement;
}