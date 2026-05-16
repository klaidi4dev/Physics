/*
 * Лабораторна робота № 3-7 "Магнітна проникність".
 * Клас: Measurement.
 * Призначення: зберігає дані вимірювань, параметри фізичної моделі та
 * результати обчислень для лабораторної роботи.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level3.lab3_7.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String toroidName;
    private double voltage;
    private int n;
    private double l1;
    private double l2;
    private double d1;
    private double d2;
    private double h;
    private double rx;
    private double lx;
    private double mu;
}