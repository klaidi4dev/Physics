/*
 * Лабораторна робота № 1-1 "Машина Атвуда".
 * Клас: Measurement.
 * Призначення: зберігає дані одного експериментального вимірювання:
 * маси вантажів, шляхи руху, час та обчислене значення g.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.level1.lab1_1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    private int id;
    private double bigM;
    private double smallM;
    private double s1;
    private double s2;
    private double time;
    private double gravity;
}