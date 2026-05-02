package dev.ua._klaidi4_.physics.core.utils;

import javafx.scene.control.Alert;
import java.io.InputStream;

public class DocumentationManager {

    public static void openInstruction(String labId) {
        if (labId == null || labId.isEmpty()) return;

        String filePath = "/docs/lab_" + labId + ".pdf";
        InputStream pdfStream = DocumentationManager.class.getResourceAsStream(filePath);

        if (pdfStream != null) {
            String title = "Інструкція: Лабораторна робота № " + labId;
            PdfViewerWindow.showPdf(title, pdfStream);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Документація відсутня");
            alert.setHeaderText("Інструкція для лабораторної роботи № " + labId + " не знайдена.");
            alert.showAndWait();
        }
    }
}