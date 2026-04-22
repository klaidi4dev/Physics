package dev.ua._klaidi4_.physics.core.controller;

import dev.ua._klaidi4_.physics.core.LabModule;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public abstract class BaseLabController extends BorderPane implements LabModule {

    protected VBox leftPanel;
    protected Label finalResultLabel;

    @Override
    public Pane getRoot() {
        return this;
    }

    protected void toggleSidebar(Button btn) {
        if (leftPanel == null) return;

        Timeline timeline = new Timeline();
        if (leftPanel.getPrefWidth() > 0) {
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(250),
                            new KeyValue(leftPanel.prefWidthProperty(), 0),
                            new KeyValue(leftPanel.minWidthProperty(), 0),
                            new KeyValue(leftPanel.opacityProperty(), 0)
                    )
            );
            timeline.setOnFinished(e -> this.setLeft(null));
            btn.setText("☰ Показати панель");
        } else {
            this.setLeft(leftPanel);
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(250),
                            new KeyValue(leftPanel.prefWidthProperty(), 310),
                            new KeyValue(leftPanel.minWidthProperty(), 310),
                            new KeyValue(leftPanel.opacityProperty(), 1)
                    )
            );
            btn.setText("◀ Приховати панель");
        }
        timeline.play();
    }

    protected VBox createStatsBox() {
        VBox statsBox = new VBox(8);
        statsBox.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 12; -fx-border-color: #90caf9; -fx-border-width: 2; -fx-border-radius: 5;");

        finalResultLabel = new Label("Обробка результатів: -");
        finalResultLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        finalResultLabel.setWrapText(true);
        finalResultLabel.setMaxWidth(Double.MAX_VALUE);

        statsBox.getChildren().add(finalResultLabel);
        return statsBox;
    }

    protected VBox createInputGroup(String labelText, javafx.scene.control.Control control) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        label.setStyle("-fx-text-fill: #475569;");
        label.setWrapText(true);

        control.setMaxWidth(Double.MAX_VALUE);

        if (control instanceof javafx.scene.control.TextField) {
            control.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-padding: 6;");
        }

        return new VBox(4, label, control);
    }
    protected void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}