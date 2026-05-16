/*
 * Лабораторна робота № 4-5 "Частота мультивібратора".
 * Клас: Measurement.
 * Призначення: зберігає дані вимірювань, параметри фізичної моделі та
 * результати обчислень для лабораторної роботи.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level4.lab4_5.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double f;
    private int n;
    private double nuExp;
    private double errorPercent;
}