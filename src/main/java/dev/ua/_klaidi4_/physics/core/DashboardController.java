package dev.ua._klaidi4_.physics.core;

import dev.ua._klaidi4_.physics.lab2_1.controller.LabController21;
import dev.ua._klaidi4_.physics.level1.lab1_1.controller.LabController11;
import dev.ua._klaidi4_.physics.level1.lab1_10.controller.LabController110;
import dev.ua._klaidi4_.physics.level1.lab1_11.controller.LabController111;
import dev.ua._klaidi4_.physics.level1.lab1_12.controller.LabController112;
import dev.ua._klaidi4_.physics.level1.lab1_13.controller.LabController113;
import dev.ua._klaidi4_.physics.level1.lab1_14.controller.LabController114;
import dev.ua._klaidi4_.physics.level1.lab1_2.controller.LabController12;
import dev.ua._klaidi4_.physics.level1.lab1_3.controller.LabController13;
import dev.ua._klaidi4_.physics.level1.lab1_4.controller.LabController14;
import dev.ua._klaidi4_.physics.level1.lab1_5.controller.LabController15;
import dev.ua._klaidi4_.physics.level1.lab1_6.controller.LabController16;
import dev.ua._klaidi4_.physics.level1.lab1_7.controller.LabController17;
import dev.ua._klaidi4_.physics.level1.lab1_8.controller.LabController18;
import dev.ua._klaidi4_.physics.level1.lab1_9.controller.LabController19;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.function.Supplier;

public class DashboardController extends BorderPane {

    private ScrollPane menuScrollPane;
    private LabModule currentLab = null;

    public DashboardController() {
        initUI();
    }

    private void initUI() {
        HBox navBar = new HBox(15);
        navBar.setPadding(new Insets(12, 20, 12, 20));
        navBar.setStyle("-fx-background-color: #1e293b; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);");
        navBar.setAlignment(Pos.CENTER_LEFT);

        Button homeBtn = new Button("🏠 Головне меню");
        homeBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        homeBtn.setCursor(Cursor.HAND);
        homeBtn.setOnAction(e -> openMainMenu());

        Label appTitle = new Label("Лабораторний практикум з фізики");
        appTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        appTitle.setStyle("-fx-text-fill: #f8fafc;");

        navBar.getChildren().addAll(homeBtn, appTitle);
        this.setTop(navBar);

        VBox menuContainer = new VBox(20);
        menuContainer.setAlignment(Pos.TOP_CENTER);
        menuContainer.setPadding(new Insets(40, 40, 60, 40));
        menuContainer.setStyle("-fx-background-color: #f1f5f9;");

        Label selectTitle = new Label("Доступні лабораторні роботи");
        selectTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        selectTitle.setStyle("-fx-text-fill: #0f172a; -fx-padding: 0 0 20 0;");

        TitledPane section1 = createCategorySection("Частина 1: Фізичні основи механіки", true,
                createLabCard("1-1", "Машина Атвуда", "Дослідження прямолінійного руху в полі тяжіння та визначення прискорення вільного падіння.", LabController11::new),
                createLabCard("1-2", "Прискорення вільного падіння", "Визначення g за допомогою математичного та оборотного маятників.", LabController12::new),
                createLabCard("1-3", "Центральний удар куль", "Вивчення законів збереження імпульсу та енергії при пружному і непружному ударах.", LabController13::new),
                createLabCard("1-4", "Балістичний маятник", "Визначення швидкості кулі з допомогою балістичного маятника.", LabController14::new),
                createLabCard("1-5", "Абсолютно пружний удар", "Вивчення удару з допомогою конденсаторного хронометра.", LabController15::new),
                createLabCard("1-6", "Моменти інерції тіл", "Визначення моментів інерції тіл та перевірка теореми Штейнера з допомогою трифілярного підвісу.", LabController16::new),
                createLabCard("1-7", "Маятник Обербека", "Визначення моменту інерції хрестоподібного маятника та перевірка основного закону динаміки.", LabController17::new),
                createLabCard("1-8", "Еліпсоїд інерції", "Визначення головних моментів інерції тіла за допомогою крутильного маятника.", LabController18::new),
                createLabCard("1-9", "Крутильний балістичний маятник", "Визначення швидкості кулі з допомогою крутильного балістичного маятника.", LabController19::new),
                createLabCard("1-10", "Момент інерції: Скочування тіл", "Визначення моменту інерції тіл методом скочування з похилої площини.", LabController110::new),
                createLabCard("1-11", "Маятник Максвелла", "Визначення моменту інерції маятника та перевірка закону збереження енергії.", LabController111::new),
                createLabCard("1-12", "Гіроскоп", "Визначення швидкості прецесії та моменту інерції гіроскопа.", LabController112::new),
                createLabCard("1-13", "Тертя кочення", "Визначення коефіцієнта тертя кочення методом похилої площини.", LabController113::new),
                createLabCard("1-14", "В'язкість рідини", "Визначення коефіцієнта динамічної в'язкості рідини методом Стокса.", LabController114::new)
        );

        TitledPane section2 = createCategorySection("Частина 2: Молекулярна фізика та термодинаміка", true,
                createLabCard("2-1", "Електростатичне поле", "Дослідження електростатичного поля методом моделювання на провідному папері.", LabController21::new));
        TitledPane section3 = createCategorySection("Частина 3: Електростатика та постійний струм", false);
        TitledPane section4 = createCategorySection("Частина 4: Електромагнетизм", false);
        TitledPane section5 = createCategorySection("Частина 5: Оптика", false);
        TitledPane section6 = createCategorySection("Частина 6: Квантова та атомна фізика", false);

        menuContainer.getChildren().addAll(selectTitle, section1, section2, section3, section4, section5, section6);

        menuScrollPane = new ScrollPane(menuContainer);
        menuScrollPane.setFitToWidth(true);
        menuScrollPane.setStyle("-fx-background-color: transparent; -fx-control-inner-background: #f1f5f9;");
        menuScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        openMainMenu();
    }

    private TitledPane createCategorySection(String title, boolean isExpanded, VBox... cards) {
        FlowPane cardsGrid = new FlowPane();
        cardsGrid.setVgap(25);
        cardsGrid.setHgap(25);
        cardsGrid.setPadding(new Insets(20));
        cardsGrid.setAlignment(Pos.CENTER_LEFT);

        if (cards != null && cards.length > 0) {
            cardsGrid.getChildren().addAll(cards);
        } else {
            Label emptyLabel = new Label("Лабораторні роботи в розробці...");
            emptyLabel.setFont(Font.font("Segoe UI", javafx.scene.text.FontPosture.ITALIC, 14));
            emptyLabel.setStyle("-fx-text-fill: #94a3b8;");
            cardsGrid.getChildren().add(emptyLabel);
        }

        TitledPane pane = new TitledPane(title, cardsGrid);
        pane.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        pane.setExpanded(isExpanded);
        pane.setAnimated(true);
        pane.setStyle("-fx-text-fill: #1e293b;");
        return pane;
    }

    private VBox createLabCard(String number, String title, String description, Supplier<LabModule> moduleInstantiator) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setPrefSize(320, 200);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;"
        );
        card.setCursor(Cursor.HAND);

        Label numLabel = new Label("№ " + number);
        numLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        numLabel.setStyle("-fx-text-fill: #3b82f6; -fx-background-color: #eff6ff; -fx-padding: 4 12 4 12; -fx-background-radius: 20;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: #1e293b;");
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Segoe UI", 13));
        descLabel.setStyle("-fx-text-fill: #64748b;");
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(TextAlignment.CENTER);
        VBox.setVgrow(descLabel, Priority.ALWAYS);

        card.getChildren().addAll(numLabel, titleLabel, descLabel);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #f8fafc; -fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(59,130,246,0.3), 15, 0, 0, 5);" +
                        "-fx-border-color: #cbd5e1; -fx-border-radius: 12; -fx-border-width: 1; -fx-translate-y: -3;"
        ));

        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);" +
                        "-fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-border-width: 1; -fx-translate-y: 0;"
        ));

        card.setOnMouseClicked(e -> launchLab(moduleInstantiator.get()));
        return card;
    }

    private void openMainMenu() {
        if (currentLab != null) {
            currentLab.shutdown();
            currentLab = null;
        }
        this.setCenter(menuScrollPane);
    }

    private void launchLab(LabModule lab) {
        if (currentLab != null) currentLab.shutdown();
        currentLab = lab;
        this.setCenter(lab.getRoot());
    }
}