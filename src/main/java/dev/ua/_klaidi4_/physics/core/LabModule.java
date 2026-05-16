/*
 * Проєкт: Лабораторний практикум з фізики.
 * Клас: LabModule.
 * Призначення: Інтерфейс для лабораторного модуля, який повинен реалізовуватись кожною лабораторною роботою.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.core;

import javafx.scene.layout.Pane;

public interface LabModule {
    Pane getRoot();
    void shutdown();
}