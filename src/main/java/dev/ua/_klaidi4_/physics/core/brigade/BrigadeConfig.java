package dev.ua._klaidi4_.physics.core.brigade;

import java.util.*;

public class BrigadeConfig {
    private static final Map<String, List<String>> BRIGADES = new LinkedHashMap<>();

    static {
        BRIGADES.put("Бригада 1", Arrays.asList(
                "1-1", "2-1", "3-1", "4-1", "5-1", "6-1", "7-1", "8-1"
        ));
        BRIGADES.put("Бригада 2", Arrays.asList(
                "1-2", "2-2", "3-2", "4-2", "5-3", "6-2", "7-2", "8-2"
        ));
        BRIGADES.put("Бригада 3", Arrays.asList(
                "1-3", "2-4", "3-3", "4-3", "5-4", "6-3", "7-3", "8-3"
        ));
        BRIGADES.put("Бригада 4", Arrays.asList(
                "1-4", "2-5", "3-4", "4-4", "5-5", "6-4", "7-4", "8-4"
        ));
        BRIGADES.put("Бригада 5", Arrays.asList(
                "1-5", "2-6", "3-5", "4-5", "5-6", "6-5", "7-5", "8-5"
        ));
        BRIGADES.put("Бригада 6", Arrays.asList(
                "1-6", "2-1", "3-6", "4-6", "5-7", "6-1", "7-6", "8-1"
        ));
        BRIGADES.put("Бригада 7", Arrays.asList(
                "1-7", "2-2", "3-1", "4-1", "5-1", "6-2", "7-7", "8-2"
        ));
        BRIGADES.put("Бригада 8", Arrays.asList(
                "1-8", "2-4", "3-2", "4-2", "5-3", "6-3", "7-8", "8-3"
        ));
        BRIGADES.put("Бригада 9", Arrays.asList(
                "1-9", "2-5", "3-3", "4-3", "5-4", "6-4", "7-1", "8-4"
        ));
        BRIGADES.put("Бригада 10", Arrays.asList(
                "1-10", "2-6", "3-4", "4-4", "5-5", "6-5", "7-2", "8-5"
        ));
        BRIGADES.put("Бригада 11", Arrays.asList(
                "1-11", "2-1", "3-5", "4-5", "5-6", "6-1", "7-3", "8-1"
        ));
        BRIGADES.put("Бригада 12", Arrays.asList(
                "1-12", "2-2", "3-6", "4-6", "5-7", "6-2", "7-4", "8-2"
        ));

        BRIGADES.put("Всі роботи (Адмін)", Arrays.asList(
                "1-1", "1-2", "1-3", "1-4", "1-5", "1-6", "1-7", "1-8", "1-9", "1-10", "1-11", "1-12", "1-13", "1-14",
                "2-1", "2-2", "2-4", "2-5", "2-6",
                "3-1", "3-2", "3-3", "3-4", "3-5", "3-6", "3-7", "3-8", "3-9",
                "4-1", "4-2", "4-3", "4-4", "4-5", "4-6",
                "5-1", "5-3", "5-4", "5-5", "5-6", "5-7",
                "6-1", "6-2", "6-3", "6-4", "6-5",
                "7-1", "7-2", "7-3", "7-4", "7-5", "7-6", "7-7", "7-8",
                "8-1", "8-2", "8-3", "8-4", "8-5"
        ));
    }

    public static List<String> getBrigades() {
        return new ArrayList<>(BRIGADES.keySet());
    }

    public static List<String> getAllowedLabs(String brigade) {
        return BRIGADES.getOrDefault(brigade, new ArrayList<>());
    }
}