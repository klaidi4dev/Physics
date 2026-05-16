/*
 * Лабораторна робота № 6-3 "Гамма-ослаблення".
 * Клас: Measurement.
 * Призначення: зберігає дані вимірювань, параметри фізичної моделі та
 * результати обчислень для лабораторної роботи.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level6.lab6_3.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private String isotope;
    private double x;
    private int timeSec;
    private int counts;
    private double lnCounts;
}