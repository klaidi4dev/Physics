package dev.ua._klaidi4_.physics.core.brigade;

import java.util.*;

public class BrigadeConfig {
    private static final Map<String, List<String>> BRIGADES = new LinkedHashMap<>();

    static {
        BRIGADES.put("Бригада 1", Arrays.asList(
                "1-1", "2-1", "3-1", "4-1", "5-1"
        ));
        BRIGADES.put("Бригада 2", Arrays.asList(
                "1-2", "1-4", "1-8", "1-13", "2-2", "2-6", "3-3", "4-2", "5-3", "6-2", "7-2"
        ));
        BRIGADES.put("Бригада 3", Arrays.asList(
                "1-5", "1-6", "1-9", "1-14", "2-3", "3-4", "4-3", "5-4", "6-3", "7-3"
        ));

        BRIGADES.put("Всі роботи (Адмін)", Arrays.asList(
                "1-1", "1-2", "1-3", "1-4", "1-5", "1-6", "1-7", "1-8", "1-9", "1-10", "1-11", "1-12", "1-13", "1-14",
                "222", "2-1", "2-2", "2-3", "2-4", "2-5", "2-6",
                "3-1", "3-2", "3-3", "3-4", "3-5", "3-6",
                "4-1", "4-2", "4-3", "4-4", "4-5", "4-6",
                "5-1", "5-3", "5-4", "5-5", "5-6", "5-7",
                "6-1", "6-2", "6-3", "6-4", "6-5",
                "7-1", "7-2", "7-3", "7-4", "7-5", "7-6", "7-7", "7-8",
                "8-4"
        ));
    }

    public static List<String> getBrigades() {
        return new ArrayList<>(BRIGADES.keySet());
    }

    public static List<String> getAllowedLabs(String brigade) {
        return BRIGADES.getOrDefault(brigade, new ArrayList<>());
    }
}