package edu.upenn.cit594.datamanagement;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import edu.upenn.cit594.util.CovidData;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * JSON or CSV
 * headers:
 * zipcode: ignore entire row if the zip code is not 5 digits
 * etl_timestamp: YYYY-MM-DD hh:mm:ss ignore entire row if not this format
 * partially_vaccinated: total # people received first dose in zipcode, 0 for empty field
 * fully_vaccinated: total # people received 2nd dose in zipcode, 0 for empty field
 */
public class CovidLoader {
    private final String filename;
    private List<CovidData> data; // memoization

    public CovidLoader(String filename) {
        this.filename = filename;
    }

    public List<CovidData> loadCovidData() {
        if (data != null) return data;

        if (filename.toLowerCase().endsWith(".csv")) {
            return loadFromCSV();
        } else if (filename.toLowerCase().endsWith(".json")) {
            return loadFromJSON();
        } else {
            return Collections.emptyList();
        }
    }

    private List<CovidData> loadFromCSV() {
        List<CovidData> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String header = br.readLine();
            if (header == null) return records;

            String[] headers = header.split(",", -1);
            Map<String, Integer> index = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                index.put(headers[i].trim().toLowerCase(), i);
            }
            // DEBUG: Print column index map
            System.out.println("Column Index Map: " + index);


            int zipIdx = index.getOrDefault("zip_code", -1);
            int timeIdx = index.getOrDefault("etl_timestamp", -1);
            int partialIdx = index.getOrDefault("partially_vaccinated", -1);
            int fullIdx = index.getOrDefault("fully_vaccinated", -1);

            if (zipIdx < 0 || timeIdx < 0 || partialIdx < 0 || fullIdx < 0) return records;

            String line;
            int maxIndex = Math.max(Math.max(zipIdx, timeIdx), Math.max(partialIdx, fullIdx));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < maxIndex) continue;

                String zip = parts[zipIdx].trim();
                String timestamp = parts[timeIdx].trim();

                //debug
                System.out.println("🔍 Raw timestamp: " + timestamp);

                if (!isValidZip(zip) || !isValidTimestamp(timestamp)) continue;

                zip = parseZipSafe(zip);
                LocalDate date = parseDateSafe(timestamp, formatter);
                int partial = parseIntSafe(parts[partialIdx].trim());
                int full = parseIntSafe(parts[fullIdx].trim());

                //debug
                if (date != null && date.equals(LocalDate.of(2021, 4, 10))) {
                    System.out.println("✅ Loaded 2021-04-10: ZIP=" + zip + ", partial=" + partial + ", full=" + full);
                }

                records.add(new CovidData(zip, date, partial, full));
            }
        } catch (Exception e) {
            System.err.println("Error reading COVID CSV file: " + e.getMessage());
        }
        return records;
    }

    private List<CovidData> loadFromJSON() {
        List<CovidData> records = new ArrayList<>();
        JSONParser parser = new JSONParser();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (Reader reader = new FileReader(filename)) {
            JSONArray array = (JSONArray) parser.parse(reader);

            for (Object obj : array) {
                JSONObject json = (JSONObject) obj;

                String zip = String.valueOf(json.get("zip_code")).trim();
                String timestamp = String.valueOf(json.get("etl_timestamp")).trim();
                if (!isValidZip(zip) || !isValidTimestamp(timestamp)) continue;

                zip = parseZipSafe(zip);
                LocalDate date = parseDateSafe(timestamp, formatter);
                int partial = parseIntSafe(String.valueOf(json.get("partially_vaccinated")).trim());
                int full = parseIntSafe(String.valueOf(json.get("fully_vaccinated")).trim());

                records.add(new CovidData(zip, date, partial, full));
            }

        } catch (Exception e) {
            System.err.println("Error reading COVID JSON file: " + e.getMessage());
        }
        return records;
    }

    private boolean isValidZip(String zip) {
        return (zip != null &&
                zip.length() >= 5 &&
                zip.substring(0, 5).matches("\\d{5}"));
    }

    private boolean isValidTimestamp(String timestamp) {
        return (timestamp != null &&
                // Can further modify to set interval for valid time expression, such as HH should be 0~23
                timestamp.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    private String parseZipSafe(String raw) {
        if (raw == null) return null;
       return raw.substring(0, 5);
    }

    private LocalDate parseDateSafe(String raw, DateTimeFormatter formatter) {
        try {
            if (raw == null || formatter == null) return null;
            return  LocalDateTime.parse(raw, formatter).toLocalDate();
        } catch (Exception e) {
            return null;
        }
    }

    private int parseIntSafe(String str) {
        try {
            if (str.isEmpty() || str.equals("null") || str == null) return 0;
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }
}
