package dev.ua._klaidi4_.physics.level7.lab7_3.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LiquidType {
    GLYCERIN("Гліцерин", 1260, 1.2, "#a0e0ff"),
    CASTOR_OIL("Касторова олія", 960, 0.98, "#fada5e");

    private final String name;
    private final double density;
    private final double theoreticalViscosity;
    private final String colorHex;

    @Override
    public String toString() { return name + " (ρ=" + density + " кг/м³)"; }
}