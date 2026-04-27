package dev.ua._klaidi4_.physics.level4.lab4_2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MaterialType {
    STEEL("Сталь", 0.05, "#434b4d"),
    ALUMINUM("Алюміній", 0.02, "#d7d7d9"),
    WOOD("Дерево", 0.01, "#8b5a2b");

    private final String name;
    private final double mass;
    private final String colorHex;

    @Override
    public String toString() { return name + " (" + mass + " кг)"; }
}