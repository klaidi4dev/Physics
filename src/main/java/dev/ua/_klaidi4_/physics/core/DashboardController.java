/*
 * Проєкт: Лабораторний практикум з фізики.
 * Клас: DashboardController.
 * Призначення: Головне меню програми, яке відповідає за навігацію між лабораторними роботами.
 *
 * Автор: Остапенко Максим (_Klaidi4_)
 * Copyright (c) 2026 Maksym Ostapenko (_Klaidi4_)
 */
package dev.ua._klaidi4_.physics.core;

import dev.ua._klaidi4_.physics.core.brigade.BrigadeConfig;
import dev.ua._klaidi4_.physics.core.utils.DocumentationManager;
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

import dev.ua._klaidi4_.physics.level2.lab2_2.controller.LabController222;
import dev.ua._klaidi4_.physics.level2.lab2_1.controller.LabController21;
import dev.ua._klaidi4_.physics.level2.lab2_4.controller.LabController24;
import dev.ua._klaidi4_.physics.level2.lab2_5.controller.LabController25;
import dev.ua._klaidi4_.physics.level2.lab2_6.controller.LabController26;

import dev.ua._klaidi4_.physics.level3.lab3_1.controller.LabController31;
import dev.ua._klaidi4_.physics.level3.lab3_2.controller.LabController32;
import dev.ua._klaidi4_.physics.level3.lab3_3.controller.LabController33;
import dev.ua._klaidi4_.physics.level3.lab3_4.controller.LabController34;
import dev.ua._klaidi4_.physics.level3.lab3_5.controller.LabController35;
import dev.ua._klaidi4_.physics.level3.lab3_6.controller.LabController36;

import dev.ua._klaidi4_.physics.level3.lab3_7.controller.LabController37;
import dev.ua._klaidi4_.physics.level3.lab3_8.controller.LabController38;
import dev.ua._klaidi4_.physics.level3.lab3_9.controller.LabController39;
import dev.ua._klaidi4_.physics.level4.lab4_1.controller.LabController41;
import dev.ua._klaidi4_.physics.level4.lab4_2.controller.LabController42;
import dev.ua._klaidi4_.physics.level4.lab4_3.controller.LabController43;
import dev.ua._klaidi4_.physics.level4.lab4_4.controller.LabController44;
import dev.ua._klaidi4_.physics.level4.lab4_5.controller.LabController45;
import dev.ua._klaidi4_.physics.level4.lab4_6.controller.LabController46;

import dev.ua._klaidi4_.physics.level5.lab5_1.controller.LabController51;
import dev.ua._klaidi4_.physics.level5.lab5_3.controller.LabController53;
import dev.ua._klaidi4_.physics.level5.lab5_4.controller.LabController54;
import dev.ua._klaidi4_.physics.level5.lab5_5.controller.LabController55;
import dev.ua._klaidi4_.physics.level5.lab5_6.controller.LabController56;
import dev.ua._klaidi4_.physics.level5.lab5_7.controller.LabController57;

import dev.ua._klaidi4_.physics.level6.lab6_1.controller.LabController61;
import dev.ua._klaidi4_.physics.level6.lab6_2.controller.LabController62;
import dev.ua._klaidi4_.physics.level6.lab6_3.controller.LabController63;
import dev.ua._klaidi4_.physics.level6.lab6_4.controller.LabController64;
import dev.ua._klaidi4_.physics.level6.lab6_5.controller.LabController65;

import dev.ua._klaidi4_.physics.level7.lab7_1.controller.LabController71;
import dev.ua._klaidi4_.physics.level7.lab7_2.controller.LabController72;
import dev.ua._klaidi4_.physics.level7.lab7_3.controller.LabController73;
import dev.ua._klaidi4_.physics.level7.lab7_4.controller.LabController74;
import dev.ua._klaidi4_.physics.level7.lab7_5.controller.LabController75;
import dev.ua._klaidi4_.physics.level7.lab7_6.controller.LabController76;
import dev.ua._klaidi4_.physics.level7.lab7_7.controller.LabController77;
import dev.ua._klaidi4_.physics.level7.lab7_8.controller.LabController78;

import dev.ua._klaidi4_.physics.level8.lab8_1.controller.LabController81;
import dev.ua._klaidi4_.physics.level8.lab8_2.controller.LabController82;
import dev.ua._klaidi4_.physics.level8.lab8_3.controller.LabController83;
import dev.ua._klaidi4_.physics.level8.lab8_4.controller.LabController84;

import dev.ua._klaidi4_.physics.level8.lab8_5.controller.LabController85;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DashboardController extends BorderPane {

    private static String currentBrigade = "";

    private BorderPane mainDashboardView;
    private VBox sidebar;
    private ScrollPane contentScrollPane;
    private VBox contentArea;
    private LabModule currentLab = null;
    private String currentLabId = null;
    private List<String> allowedLabs;
    private List<Button> categoryButtons = new ArrayList<>();
    private final Runnable onChangeBrigade;

    private Button instructionBtn;

    public static void setCurrentBrigade(String brigade) {
        currentBrigade = brigade;
    }

    public DashboardController(Runnable onChangeBrigade) {
        this.onChangeBrigade = onChangeBrigade;
        this.allowedLabs = BrigadeConfig.getAllowedLabs(currentBrigade);
        initUI();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void initUI() {
        HBox navBar = new HBox(15);
        navBar.setPadding(new Insets(12, 25, 12, 25));
        navBar.setStyle("-fx-background-color: #1e293b; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);");
        navBar.setAlignment(Pos.CENTER_LEFT);

        Button homeBtn = new Button("🏠 Головне меню");
        homeBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        homeBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20 10 20;");
        homeBtn.setCursor(Cursor.HAND);
        homeBtn.setOnAction(e -> openMainMenu());

        homeBtn.setOnMouseEntered(e -> homeBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20 10 20;"));
        homeBtn.setOnMouseExited(e -> homeBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20 10 20;"));

        Label appTitle = new Label("Лабораторний практикум | " + currentBrigade);
        appTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        appTitle.setStyle("-fx-text-fill: #f8fafc; -fx-padding: 0 0 0 15;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        instructionBtn = new Button("📖 Інструкція");
        instructionBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        instructionBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16 8 16;");
        instructionBtn.setCursor(Cursor.HAND);
        instructionBtn.setVisible(false);
        instructionBtn.setManaged(false);

        instructionBtn.setOnMouseEntered(e -> instructionBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16 8 16;"));
        instructionBtn.setOnMouseExited(e -> instructionBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16 8 16;"));

        instructionBtn.setOnAction(e -> DocumentationManager.openInstruction(currentLabId));

        Button changeBrigadeBtn = new Button("🔄 Змінити бригаду");
        changeBrigadeBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        changeBrigadeBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16 8 16;");
        changeBrigadeBtn.setCursor(Cursor.HAND);
        changeBrigadeBtn.setOnAction(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> onChangeBrigade.run());
            fadeOut.play();
        });

        changeBrigadeBtn.setOnMouseEntered(e -> changeBrigadeBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16 8 16;"));
        changeBrigadeBtn.setOnMouseExited(e -> changeBrigadeBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16 8 16;"));

        navBar.getChildren().addAll(homeBtn, appTitle, spacer, instructionBtn, changeBrigadeBtn);
        this.setTop(navBar);

        mainDashboardView = new BorderPane();

        sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 1 0 0;");

        Label sidebarTitle = new Label("Розділи");
        sidebarTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        sidebarTitle.setStyle("-fx-text-fill: #64748b; -fx-padding: 0 0 10 10;");
        sidebar.getChildren().add(sidebarTitle);

        contentArea = new VBox(20);
        contentArea.setPadding(new Insets(30, 40, 60, 40));
        contentArea.setStyle("-fx-background-color: #f1f5f9;");

        contentScrollPane = new ScrollPane(contentArea);
        contentScrollPane.setFitToWidth(true);
        contentScrollPane.setStyle("-fx-background-color: transparent; -fx-control-inner-background: #f1f5f9;");
        contentScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        mainDashboardView.setLeft(sidebar);
        mainDashboardView.setCenter(contentScrollPane);

        registerCategory("Частина 1: Механіка",
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

        registerCategory("Частина 2: Електрика",
                createLabCard("2-1", "Електростатичне поле", "Дослідження електростатичного поля методом моделювання на провідному папері.", LabController21::new),
                createLabCard("2-2", "Ємність та сегнетоелектрики", "Комплексна робота: вимірювання ємності конденсаторів, перевірка законів їх з'єднання та визначення діелектричної проникності.", LabController222::new),
                createLabCard("2-4", "Точка Кюрі", "Дослідження температурної залежності властивостей сегнетоелектриків.", LabController24::new),
                createLabCard("2-5", "Визначення ЕРС", "Вимірювання електрорушійної сили джерел струму компенсаційним методом.", LabController25::new),
                createLabCard("2-6", "Опір та температура", "Вимірювання опорів (Міст Уітстона) і залежності опору металу від температури.", LabController26::new)
        );

        registerCategory("Частина 3: Магнетизм",
                createLabCard("3-1", "Магнітне поле", "Визначення індукції магнетного поля за допомогою балістичного гальванометра.", LabController31::new),
                createLabCard("3-2", "Магнітне поле Землі", "Визначення горизонтальної складової напруженості магнітного поля Землі.", LabController32::new),
                createLabCard("3-3", "Питомий заряд електрона", "Визначення питомого заряду електрона методом магнетного фокусування розбіжного пучка.", LabController33::new),
                createLabCard("3-4", "Поле на осі соленоїда", "Визначення напруженості магнетного поля в різних точках вздовж осі соленоїда.", LabController34::new),
                createLabCard("3-5", "Індуктивність соленоїда", "Визначення індуктивності соленоїда за допомогою ЛАТР.", LabController35::new),
                createLabCard("3-6", "Взаємна індукція", "Дослідження явища взаємної індукції двох коаксіально розміщених котушок.", LabController36::new),
                createLabCard("3-7", "Магнітна проникність", "Визначення відносної магнетної проникності магнетиків з допомогою містка Максвелла.", LabController37::new),
                createLabCard("3-8", "Петля гістерезису", "Дослідження кривих намагнечування та петель гістерезису феромагнетиків з допомогою осцилографа.", LabController38::new),
                createLabCard("3-9", "Точка Кюрі", "Визначення точки Кюрі феромагнетика.", LabController39::new)
        );

        registerCategory("Частина 4: Коливання",
                createLabCard("4-1", "Фізичний маятник", "Вивчення коливань фізичного (оборотного) маятника та визначення прискорення вільного падіння g.", LabController41::new),
                createLabCard("4-2", "Математичний маятник", "Вивчення законів коливання математичного маятника.", LabController42::new),
                createLabCard("4-3", "Додавання коливань", "Вивчення явища додавання гармонічних коливань (биття та фігури Ліссажу).", LabController43::new),
                createLabCard("4-4", "Згасаючі коливання", "Вивчення і перевірка законів згасаючих електромагнітних коливань у RLC-контурі.", LabController44::new),
                createLabCard("4-5", "Частота мультивібратора", "Вивчення стоячих хвиль в натягнутій струні та визначення частоти коливань мультивібратора.", LabController45::new),
                createLabCard("4-6", "Поперечні коливання струни", "Вимірювання власних частот (гармонік) поперечних коливань струни із закріпленими кінцями.", LabController46::new)
        );

        registerCategory("Частина 5: Оптика",
                createLabCard("5-1", "Фокусна віддаль лінз", "Визначення головної фокусної віддалі збірної та розсіювальної лінз методом Гауса-Бесселя.", LabController51::new),
                createLabCard("5-3", "Показник заломлення", "Визначення показника заломлення скляної пластинки за допомогою мікроскопа методом уявної товщини.", LabController53::new),
                createLabCard("5-4", "Біпризма Френеля", "Визначення довжини світлової хвилі за допомогою біпризми Френеля.", LabController54::new),
                createLabCard("5-5", "Кільця Ньютона", "Визначення довжини світлової хвилі за допомогою кілець Ньютона.", LabController55::new),
                createLabCard("5-6", "Дифракційна решітка", "Визначення довжини світлової хвилі за допомогою дифракційної решітки.", LabController56::new),
                createLabCard("5-7", "Дифракція Фраунгофера", "Дослідження дифракції на решітці за допомогою лазера.", LabController57::new)
        );

        registerCategory("Частина 6: Атомна фізика",
                createLabCard("6-1", "Пробіг α-частинок", "Визначення втрат енергії α-частинок за довжиною вільного пробігу в повітрі.", LabController61::new),
                createLabCard("6-2", "Активність β-джерела", "Визначення активності джерела β-випромінювання методом порівняння з еталоном.", LabController62::new),
                createLabCard("6-3", "Гамма-ослаблення", "Визначення лінійного коефіцієнта ослаблення γ-квантів у свинці.", LabController63::new),
                createLabCard("6-4", "Фотоемульсійний метод", "Вивчення іонізуючого випромінювання фотоемульсійним методом.", LabController64::new),
                createLabCard("6-5", "Питомий заряд електрона", "Визначення питомого заряду електрона методом магнетрона.", LabController65::new)
        );

        registerCategory("Частина 7: Молекулярна",
                createLabCard("7-1", "Теплоємність газу", "Визначення відношення теплоємностей газу методом Клемана-Дезорма.", LabController71::new),
                createLabCard("7-2", "В'язкість газів", "В'язкість та довжина вільного пробігу молекул повітря.", LabController72::new),
                createLabCard("7-3", "Метод Стокса", "Визначення коефіцієнта в'язкості рідини методом Стокса.", LabController73::new),
                createLabCard("7-4", "Теплопровідність металів", "Визначення коефіцієнта теплопровідності міді.", LabController74::new),
                createLabCard("7-5", "Критичний стан", "Дослідження критичних явищ в системі рідина-пара (поправки Ван-дер-Ваальса).", LabController75::new),
                createLabCard("7-6", "Зміна ентропії", "Визначення приросту ентропії при нагріванні і плавленні свинцю (фазовий перехід І роду).", LabController76::new),
                createLabCard("7-7", "Розподіл Максвелла", "Вивчення розподілу Максвелла за швидкостями для термоелектронів.", LabController77::new),
                createLabCard("7-8", "Поверхневий натяг", "Визначення коефіцієнта поверхневого натягу рідини методом Ребіндера.", LabController78::new)
        );

        registerCategory("Частина 8: Тверде тіло",
                createLabCard("8-1", "Енергія активації напівпровідників", "Дослідження температурної залежності електропровідності та визначення енергії активації.", LabController81::new),
                createLabCard("8-2", "Ефект Холла", "Вивчення ефекту Холла в напівпровідниках та визначення концентрації вільних носіїв струму.", LabController82::new),
                createLabCard("8-3", "Фотоелектричні явища", "Вивчення законів внутрішнього фотоефекту та зняття характеристик напівпровідникового фотоопору.", LabController83::new),
                createLabCard("8-4", "Дослідження p-n-переходу", "Вивчення вольт-амперної характеристики напівпровідникового діода.", LabController84::new),
                createLabCard("8-5", "Тунельний діод", "Принцип роботи та вольт-амперна характеристика тунельного діода.", LabController85::new)
        );
        Region spacerBottom = new Region();
        VBox.setVgrow(spacerBottom, Priority.ALWAYS);

        Label authorsLabel = new Label("Created by _Klaidi4_, Ankai, 7ei");
        authorsLabel.setFont(Font.font("Segoe UI", 12));
        authorsLabel.setStyle("-fx-text-fill: #94a3b8;");
        authorsLabel.setWrapText(true);
        authorsLabel.setAlignment(Pos.CENTER);
        authorsLabel.setMaxWidth(Double.MAX_VALUE);
        authorsLabel.setPadding(new Insets(20, 10, 10, 10));
        sidebar.getChildren().addAll(spacerBottom, authorsLabel);
        if (!categoryButtons.isEmpty()) {
            categoryButtons.get(0).fire();
        } else {
            showEmptyState();
        }

        openMainMenu();
    }

    private void registerCategory(String title, VBox... cards) {
        List<VBox> validCards = new ArrayList<>();
        if (cards != null) {
            for (VBox card : cards) {
                if (card != null) validCards.add(card);
            }
        }

        if (validCards.isEmpty()) return;

        Button navButton = new Button(title);
        navButton.setMaxWidth(Double.MAX_VALUE);
        navButton.setAlignment(Pos.CENTER_LEFT);
        navButton.setPadding(new Insets(10, 15, 10, 15));
        navButton.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        navButton.setCursor(Cursor.HAND);
        navButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #1e293b; -fx-background-radius: 8;");

        navButton.setOnAction(e -> {
            setActiveCategoryButton(navButton);
            showCategoryContent(title, validCards);
        });

        navButton.setOnMouseEntered(e -> {
            if (!navButton.getStyleClass().contains("active-btn")) {
                navButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #1e293b; -fx-background-radius: 8;");
            }
        });
        navButton.setOnMouseExited(e -> {
            if (!navButton.getStyleClass().contains("active-btn")) {
                navButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #1e293b; -fx-background-radius: 8;");
            }
        });

        categoryButtons.add(navButton);
        sidebar.getChildren().add(navButton);
    }

    private void setActiveCategoryButton(Button activeBtn) {
        for (Button btn : categoryButtons) {
            btn.getStyleClass().remove("active-btn");
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1e293b; -fx-background-radius: 8;");
        }
        activeBtn.getStyleClass().add("active-btn");
        activeBtn.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #2563eb; -fx-background-radius: 8; -fx-font-weight: bold;");
    }

    private void showCategoryContent(String title, List<VBox> cards) {
        contentArea.getChildren().clear();

        Label sectionTitle = new Label(title);
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        sectionTitle.setStyle("-fx-text-fill: #0f172a; -fx-padding: 0 0 20 0;");

        FlowPane cardsGrid = new FlowPane();
        cardsGrid.setVgap(25);
        cardsGrid.setHgap(25);
        cardsGrid.setAlignment(Pos.TOP_LEFT);
        cardsGrid.getChildren().addAll(cards);

        contentArea.getChildren().addAll(sectionTitle, cardsGrid);
        contentScrollPane.setVvalue(0);
    }

    private void showEmptyState() {
        contentArea.getChildren().clear();
        Label emptyLabel = new Label("Для цієї бригади немає доступних лабораторних робіт.");
        emptyLabel.setFont(Font.font("Segoe UI", 16));
        emptyLabel.setStyle("-fx-text-fill: #64748b;");
        contentArea.getChildren().add(emptyLabel);
    }

    private VBox createLabCard(String number, String title, String description, Supplier<LabModule> moduleInstantiator) {
        if (!allowedLabs.contains(number)) {
            return null;
        }

        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setPrefSize(340, 210);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 12, 0, 0, 4);" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1.5;"
        );
        card.setCursor(Cursor.HAND);

        Label numLabel = new Label("№ " + number);
        numLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
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
                        "-fx-effect: dropshadow(three-pass-box, rgba(59,130,246,0.25), 20, 0, 0, 6);" +
                        "-fx-border-color: #cbd5e1; -fx-border-radius: 12; -fx-border-width: 1.5; -fx-translate-y: -4;"
        ));

        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 12, 0, 0, 4);" +
                        "-fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-border-width: 1.5; -fx-translate-y: 0;"
        ));

        card.setOnMouseClicked(e -> {
            LabModule module = moduleInstantiator.get();
            if (module != null) {
                launchLab(module, number);
            }
        });
        return card;
    }

    private void openMainMenu() {
        if (currentLab != null) {
            currentLab.shutdown();
            currentLab = null;
        }
        currentLabId = null;

        instructionBtn.setVisible(false);
        instructionBtn.setManaged(false);

        this.setCenter(mainDashboardView);
    }

    private void launchLab(LabModule lab, String labId) {
        if (currentLab != null) currentLab.shutdown();
        currentLab = lab;
        currentLabId = labId;

        instructionBtn.setVisible(true);
        instructionBtn.setManaged(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), lab.getRoot());
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        this.setCenter(lab.getRoot());
        fadeIn.play();
    }
}