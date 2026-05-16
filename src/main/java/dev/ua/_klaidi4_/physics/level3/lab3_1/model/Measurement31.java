/*
 * Лабораторна робота № 3-1 "Магнітне поле".
 * Клас: Measurement31.
 * Призначення: зберігає дані вимірювань, параметри фізичної моделі та
 * результати обчислень для лабораторної роботи.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level3.lab3_1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement31 {
    private int id;
    private double l0;
    private double currentI;
    private double x;
    private double n;
    private double b;
}