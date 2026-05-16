/*
 * Лабораторна робота № 3-2 "Магнітне поле Землі".
 * Клас: Measurement32.
 * Призначення: зберігає дані вимірювань, параметри фізичної моделі та
 * результати обчислень для лабораторної роботи.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level3.lab3_2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement32 {
    private int id;
    private int n;
    private double r;
    private double currentI;
    private String polarity;
    private double angle;
    private double h0;
}