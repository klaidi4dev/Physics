package dev.ua._klaidi4_.physics.core;

import javafx.scene.layout.Pane;

public interface LabModule {
    Pane getRoot();
    void shutdown();
}