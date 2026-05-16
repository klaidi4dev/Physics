/*
 * Лабораторна робота № 5-6 "Дифракційна решітка".
 * Клас: Measurement.
 * Призначення: зберігає дані вимірювань, параметри фізичної моделі та
 * результати обчислень для лабораторної роботи.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level5.lab5_6.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String filterColor;
    private double dGrating;
    private double phi1;
    private double phi2;
    private double lambdaExp;
    private double errorPercent;
}