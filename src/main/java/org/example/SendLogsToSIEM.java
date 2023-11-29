package org.example;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SendLogsToSIEM {

    public static void main(String[] args) {

        String csvFilePath = "/var/log/super_mega_critical.csv";

        String siemUrl = "http://siem.yandex.ru/input";

        try {

            List<String[]> logsList = readCsvFile(csvFilePath);


            String jsonLogs = convertToJSON(logsList);


            HttpResponse<JsonNode> response = sendLogsToSIEM(siemUrl, jsonLogs);


            if (response.isSuccess()) {
                System.out.println("Логи успешно отправлены на SIEM.");
            } else {
                System.out.println("Ошибка при отправке логов на SIEM.");
                System.out.println("Код ошибки: " + response.getStatus());
                System.out.println(response.getBody());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String[]> readCsvFile(String filePath) throws IOException {
        List<String[]> logsList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                logsList.add(fields);
            }
        }

        return logsList;
    }

    private static String convertToJSON(List<String[]> logsList) {
        StringBuilder jsonLogs = new StringBuilder("[");
        for (String[] fields : logsList) {
            jsonLogs.append("{");
            for (int i = 0; i < fields.length; i++) {
                jsonLogs.append("\"").append(fields[i]).append("\": \"").append(fields[i]).append("\"");
                if (i < fields.length - 1) {
                    jsonLogs.append(",");
                }
            }
            jsonLogs.append("},");
        }
        jsonLogs.deleteCharAt(jsonLogs.length() - 1);
        jsonLogs.append("]");

        return jsonLogs.toString();
    }

    private static HttpResponse<JsonNode> sendLogsToSIEM(String siemUrl, String jsonLogs) {
        return Unirest.post(siemUrl)
                .header("Content-Type", "application/json")
                .body(jsonLogs)
                .asJson();
    }
}
