/*
 * Лабораторна робота № 1-3 "Центральний удар куль".
 * Клас: Measurement.
 * Призначення: зберігає дані вимірювань, параметри фізичної моделі та
 * результати обчислень для лабораторної роботи.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level1.lab1_3.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String type;
    private double m1;
    private double m2;
    private double alpha1;
    private double alpha1Prime;
    private double alpha2Prime;
    private double v1;
    private double u1;
    private double u2;
    private double tau;
    private double deltaW;
}