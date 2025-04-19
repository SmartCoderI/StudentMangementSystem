package edu.upenn.cit594.datamanagement;

import edu.upenn.cit594.logging.Logger;
import edu.upenn.cit594.util.CovidData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

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
            return Collections.emptyList();//or throw error here?
        }
    }

    private List<CovidData> loadFromCSV() {
        Logger.getInstance().log(filename);
        List<CovidData> records = new ArrayList<>();
        try (CharacterReader charReader = new CharacterReader(filename)) {
            CSVReader csvReader = new CSVReader(charReader);
            String[] headers = csvReader.readRow();

            if (headers == null) return records;

            Map<String, Integer> index = new HashMap<>();

            for (int i = 0; i < headers.length; i++) {
                index.put(headers[i].trim().toLowerCase(), i);
            }

            int zipIdx = index.getOrDefault("zip_code", -1);
            int timeIdx = index.getOrDefault("etl_timestamp", -1);
            int partialIdx = index.getOrDefault("partially_vaccinated", -1);
            int fullIdx = index.getOrDefault("fully_vaccinated", -1);

            if (zipIdx < 0 || timeIdx < 0 || partialIdx < 0 || fullIdx < 0) return records;

            String[] line;
            int maxIndex = Math.max(Math.max(zipIdx, timeIdx), Math.max(partialIdx, fullIdx));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            while ((line = csvReader.readRow()) != null) {
                if (Arrays.stream(line).count() <= maxIndex) continue;

                String zip = line[zipIdx].trim();
                String timestamp = line[timeIdx].trim();
                timestamp = timestamp.replaceAll("^\"|\"$", "").trim();
                LocalDate date = parseDateSafe(timestamp, formatter);

                if (!isValidZip(zip) || date == null) continue;

                int partial = parseIntSafe(line[partialIdx].trim());
                int full = parseIntSafe(line[fullIdx].trim());

                records.add(new CovidData(zip, date, partial, full));
            }
        } catch (Exception e) {
            System.err.println("Error reading COVID CSV file: " + e.getMessage());
        }
        this.data = records; // for memorization
        return records;
    }

    private List<CovidData> loadFromJSON() {
        Logger.getInstance().log(filename);
        List<CovidData> records = new ArrayList<>();
        JSONParser parser = new JSONParser();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (Reader reader = new FileReader(filename)) {
            JSONArray array = (JSONArray) parser.parse(reader);

            for (Object obj : array) {
                JSONObject json = (JSONObject) obj;

                String zip = String.valueOf(json.get("zip_code")).trim();
                String timestamp = String.valueOf(json.get("etl_timestamp")).trim();
                timestamp = timestamp.replaceAll("^\"|\"$", "").trim();

                LocalDate date = parseDateSafe(timestamp, formatter);

                if (!isValidZip(zip) || date == null) continue;

                int partial = parseIntSafe(String.valueOf(json.get("partially_vaccinated")).trim());
                int full = parseIntSafe(String.valueOf(json.get("fully_vaccinated")).trim());

                records.add(new CovidData(zip, date, partial, full));
            }

        } catch (Exception e) {
            System.err.println("Error reading COVID JSON file: " + e.getMessage());
        }
        this.data = records; // for memorization
        return records;
    }

    private boolean isValidZip(String raw) {
        return raw.matches("^[0-9]{5}$");
    }

    private LocalDate parseDateSafe(String raw, DateTimeFormatter formatter) {
        try {
            if (raw == null || formatter == null) return null;
            return LocalDateTime.parse(raw, formatter).toLocalDate();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private int parseIntSafe(String str) {
        try {
            if (str.isEmpty() || str.equals("null") || str == null || str.equals("")) return 0;
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }
}
