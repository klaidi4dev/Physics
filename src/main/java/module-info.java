module dev.ua._klaidi4_.physics {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;

    opens dev.ua._klaidi4_.physics.level1.lab1_1.model to javafx.base;
    opens dev.ua._klaidi4_.physics.level1.lab1_2.model to javafx.base;
    opens dev.ua._klaidi4_.physics.level1.lab1_3.model to javafx.base;
    opens dev.ua._klaidi4_.physics.level1.lab1_4.model to javafx.base;
    opens dev.ua._klaidi4_.physics.level1.lab1_5.model to javafx.base;
    opens dev.ua._klaidi4_.physics.level1.lab1_6.model to javafx.base;
    opens dev.ua._klaidi4_.physics.level1.lab1_7.model to javafx.base;
    opens dev.ua._klaidi4_.physics.level1.lab1_8.model to javafx.base;
    opens dev.ua._klaidi4_.physics.level1.lab1_9.model to javafx.base;
    opens dev.ua._klaidi4_.physics.level1.lab1_10.model to javafx.base;
    opens dev.ua._klaidi4_.physics.level1.lab1_11.model to javafx.base;
    opens dev.ua._klaidi4_.physics.level1.lab1_12.model to javafx.base;
    opens dev.ua._klaidi4_.physics.level1.lab1_13.model to javafx.base;
    opens dev.ua._klaidi4_.physics.level1.lab1_14.model to javafx.base;
    opens dev.ua._klaidi4_.physics.lab2_1.model to javafx.base;
    opens dev.ua._klaidi4_.physics to javafx.fxml;
    exports dev.ua._klaidi4_.physics;
}