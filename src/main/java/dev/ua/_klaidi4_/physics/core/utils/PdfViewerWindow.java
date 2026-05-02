package dev.ua._klaidi4_.physics.core.utils;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PdfViewerWindow {

    public static void showPdf(String titleText, InputStream pdfStream) {
        try {
            PDDocument document = PDDocument.load(pdfStream);
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(titleText);
            stage.setWidth(950);
            stage.setHeight(800);

            VBox pageContainer = new VBox(15);
            pageContainer.setAlignment(Pos.TOP_CENTER);
            pageContainer.setPadding(new Insets(20));
            pageContainer.setStyle("-fx-background-color: #94a3b8;");

            ScrollPane scrollPane = new ScrollPane(pageContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent;");
            scrollPane.getContent().setOnScroll(event -> {
                double deltaY = event.getDeltaY() * 1.2;
                double contentHeight = scrollPane.getContent().getBoundsInLocal().getHeight();
                double viewportHeight = scrollPane.getViewportBounds().getHeight();

                if (contentHeight > viewportHeight) {
                    double vvalue = scrollPane.getVvalue();
                    double scrollStep = -deltaY / (contentHeight - viewportHeight);
                    double newVvalue = Math.max(0.0, Math.min(1.0, vvalue + scrollStep));
                    scrollPane.setVvalue(newVvalue);
                }
            });

            int numPages = document.getNumberOfPages();

            ExecutorService executor = Executors.newSingleThreadExecutor();

            for (int i = 0; i < numPages; i++) {
                final int pageIndex = i;

                StackPane placeholder = new StackPane();
                placeholder.setPrefSize(880, 1100);
                placeholder.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4);");

                Label loadingLabel = new Label("Завантаження сторінки " + (pageIndex + 1) + "...");
                loadingLabel.setFont(Font.font("Segoe UI", 16));
                loadingLabel.setStyle("-fx-text-fill: #64748b;");
                placeholder.getChildren().add(loadingLabel);
                pageContainer.getChildren().add(placeholder);

                executor.submit(() -> {
                    try {
                        BufferedImage bim = pdfRenderer.renderImageWithDPI(pageIndex, 120);
                        Image fxImage = convertToFxImage(bim);

                        Platform.runLater(() -> {
                            ImageView imageView = new ImageView(fxImage);
                            imageView.setPreserveRatio(true);
                            imageView.setFitWidth(880);

                            StackPane imageWrapper = new StackPane(imageView);
                            imageWrapper.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4);");

                            pageContainer.getChildren().set(pageIndex, imageWrapper);
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> loadingLabel.setText("Помилка завантаження сторінки " + (pageIndex + 1)));
                    }
                });
            }

            stage.setOnHidden(e -> {
                try {
                    executor.shutdownNow();
                    document.close();
                } catch (Exception ignored) {}
            });

            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: #f8fafc;");

            Label title = new Label(titleText);
            title.setFont(Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 20));
            title.setStyle("-fx-text-fill: #1e293b;");

            VBox topBox = new VBox(title);
            topBox.setAlignment(Pos.CENTER);
            topBox.setPadding(new Insets(15));
            topBox.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

            root.setTop(topBox);
            root.setCenter(scrollPane);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Image convertToFxImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        WritableImage wr = new WritableImage(width, height);
        PixelWriter pw = wr.getPixelWriter();

        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        pw.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        return wr;
    }
}