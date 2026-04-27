package dev.ua._klaidi4_.physics.level8.lab8_4.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DiodeType {
    SILICON("Кремнієвий (Si)"),
    GERMANIUM("Германієвий (Ge)");

    private final String name;

    @Override
    public String toString() { return name; }
}