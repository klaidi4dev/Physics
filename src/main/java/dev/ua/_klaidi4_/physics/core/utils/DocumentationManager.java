package dev.ua._klaidi4_.physics.core.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DocumentationManager {

    private static final String DOCS_TABLE_CSV_URL =
            "https://docs.google.com/spreadsheets/d/1S_KFKmbLnZJ9RoNiwH97MyFv2RO_W-OzopiwIQoywM0/export?format=csv";

    public static void openInstruction(String labId) {
        if (labId == null || labId.trim().isEmpty()) return;

        Thread thread = new Thread(() -> {
            try {
                Map<String, OnlineInstruction> instructions = loadInstructionsFromGoogleTable();

                OnlineInstruction instruction = instructions.get(labId.trim());

                if (instruction == null) {
                    Platform.runLater(() -> showInstructionNotFoundAlert(labId));
                    return;
                }

                if (isBlank(instruction.url)) {
                    Platform.runLater(() -> showInstructionNotFoundAlert(labId));
                    return;
                }

                String title = "Інструкція: Лабораторна робота № " + labId;

                if (!isBlank(instruction.title)) {
                    title += " — " + instruction.title;
                }

                String finalTitle = title;
                String finalUrl = normalizeGoogleDriveUrl(instruction.url);

                Platform.runLater(() -> WebInstructionWindow.showUrl(finalTitle, finalUrl));

            } catch (IOException e) {
                Platform.runLater(DocumentationManager::showNoInternetAlert);
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showErrorAlert(
                        "Помилка документації",
                        "Не вдалося прочитати Google Таблицю. Перевірте структуру таблиці."
                ));
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private static Map<String, OnlineInstruction> loadInstructionsFromGoogleTable() throws IOException {
        String csvText = loadTextFromUrl(DOCS_TABLE_CSV_URL);

        List<List<String>> rows = parseCsv(csvText);

        int headerRowIndex = findHeaderRowIndex(rows);

        if (headerRowIndex == -1) {
            throw new IOException("Не знайдено рядок із колонками labId, title, url");
        }

        List<String> headers = rows.get(headerRowIndex);
        Map<String, Integer> columnIndexes = buildColumnIndexes(headers);

        Map<String, OnlineInstruction> result = new HashMap<>();

        for (int i = headerRowIndex + 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);

            String labId = getCell(row, columnIndexes, "labid");
            String title = getCell(row, columnIndexes, "title");
            String url = getCell(row, columnIndexes, "url");

            if (isBlank(labId)) continue;

            result.put(
                    labId.trim(),
                    new OnlineInstruction(labId.trim(), title.trim(), url.trim())
            );
        }

        return result;
    }

    private static String loadTextFromUrl(String urlString) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(6000);
        connection.setReadTimeout(6000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "PhysicsPractice/1.0");

        int code = connection.getResponseCode();

        if (code < 200 || code >= 300) {
            throw new IOException("HTTP error: " + code);
        }

        StringBuilder builder = new StringBuilder();

        try (InputStream inputStream = connection.getInputStream();
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8)
             )) {

            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
        }

        String text = builder.toString();

        if (text.toLowerCase(Locale.ROOT).contains("<html")) {
            throw new IOException("Google Table returned HTML instead of CSV");
        }

        return text;
    }

    private static int findHeaderRowIndex(List<List<String>> rows) {
        for (int i = 0; i < rows.size(); i++) {
            Map<String, Integer> indexes = buildColumnIndexes(rows.get(i));

            if (indexes.containsKey("labid")
                    && indexes.containsKey("title")
                    && indexes.containsKey("url")) {
                return i;
            }
        }

        return -1;
    }

    private static Map<String, Integer> buildColumnIndexes(List<String> headers) {
        Map<String, Integer> indexes = new HashMap<>();

        for (int i = 0; i < headers.size(); i++) {
            String header = normalizeHeader(headers.get(i));
            indexes.put(header, i);
        }

        return indexes;
    }

    private static String normalizeHeader(String value) {
        if (value == null) return "";

        return value
                .replace("\uFEFF", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private static String getCell(List<String> row, Map<String, Integer> indexes, String columnName) {
        Integer index = indexes.get(columnName.toLowerCase(Locale.ROOT));

        if (index == null) return "";
        if (index < 0 || index >= row.size()) return "";

        return row.get(index) == null ? "" : row.get(index);
    }

    private static List<List<String>> parseCsv(String csv) {
        List<List<String>> rows = new ArrayList<>();
        List<String> currentRow = new ArrayList<>();
        StringBuilder currentCell = new StringBuilder();

        boolean insideQuotes = false;

        for (int i = 0; i < csv.length(); i++) {
            char ch = csv.charAt(i);

            if (ch == '"') {
                if (insideQuotes && i + 1 < csv.length() && csv.charAt(i + 1) == '"') {
                    currentCell.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (ch == ',' && !insideQuotes) {
                currentRow.add(currentCell.toString());
                currentCell.setLength(0);
            } else if ((ch == '\n' || ch == '\r') && !insideQuotes) {
                if (ch == '\r' && i + 1 < csv.length() && csv.charAt(i + 1) == '\n') {
                    i++;
                }

                currentRow.add(currentCell.toString());
                currentCell.setLength(0);

                rows.add(currentRow);
                currentRow = new ArrayList<>();
            } else {
                currentCell.append(ch);
            }
        }

        if (currentCell.length() > 0 || !currentRow.isEmpty()) {
            currentRow.add(currentCell.toString());
            rows.add(currentRow);
        }

        return rows;
    }

    private static String normalizeGoogleDriveUrl(String url) {
        if (isBlank(url)) return url;

        String trimmedUrl = url.trim();

        if (!trimmedUrl.contains("drive.google.com")) {
            return trimmedUrl;
        }

        String fileId = extractGoogleDriveFileId(trimmedUrl);

        if (isBlank(fileId)) {
            return trimmedUrl;
        }

        return "https://drive.google.com/file/d/" + fileId + "/preview";
    }

    private static String extractGoogleDriveFileId(String url) {
        String marker = "/d/";

        int startIndex = url.indexOf(marker);

        if (startIndex != -1) {
            startIndex += marker.length();

            int endIndex = url.indexOf("/", startIndex);

            if (endIndex == -1) {
                endIndex = url.indexOf("?", startIndex);
            }

            if (endIndex == -1) {
                endIndex = url.length();
            }

            return url.substring(startIndex, endIndex);
        }

        String idMarker = "id=";

        startIndex = url.indexOf(idMarker);

        if (startIndex != -1) {
            startIndex += idMarker.length();

            int endIndex = url.indexOf("&", startIndex);

            if (endIndex == -1) {
                endIndex = url.length();
            }

            return url.substring(startIndex, endIndex);
        }

        return "";
    }

    private static void showNoInternetAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Немає підключення");
        alert.setHeaderText("Немає підключення до інтернету.");
        alert.setContentText("Інструкцію неможливо відкрити, тому що програма не може отримати дані з Google Таблиці.");
        alert.showAndWait();
    }

    private static void showInstructionNotFoundAlert(String labId) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Документація відсутня");
        alert.setHeaderText("Інструкція для лабораторної роботи № " + labId + " не знайдена.");
        alert.setContentText("Перевірте, чи є цей labId у Google Таблиці та чи заповнена колонка url.");
        alert.showAndWait();
    }

    private static void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(content);
        alert.showAndWait();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class OnlineInstruction {
        private final String labId;
        private final String title;
        private final String url;

        private OnlineInstruction(String labId, String title, String url) {
            this.labId = labId;
            this.title = title;
            this.url = url;
        }
    }
}