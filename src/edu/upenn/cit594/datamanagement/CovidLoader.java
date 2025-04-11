package edu.upenn.cit594.datamanagement;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

import edu.upenn.cit594.util.CovidData;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/*
JSON or CSV
headers:
zipcode: ignore entire row if the zip code is not 5 digits
etl_timestamp: YYYY-MM-DD hh:mm:ss ignore entire row if not this format
partially_vaccinated: total # people received first dose in zipcode, 0 for empty field
fully_vaccinated: total # people received 2nd dose in zipcode, 0 for empty field
 */
public class CovidLoader {
    protected String filename;

    public CovidLoader(String filename) {
        this.filename = filename;
    }

    public List<CovidData> loadCovidData() {
        if (filename.toLowerCase().endsWith(".csv")) {
            return loadFromCSV();
        } else if (filename.toLowerCase().endsWith(".json")) {
            return loadFromJSON();
        }
        return Collections.emptyList();
    }

    private List<CovidData> loadFromCSV() {
        List<CovidData> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 9) continue;

                String zip = parts[0].trim();
                String partialStr = parts[1].trim();
                String fullStr = parts[2].trim();
                String timestamp = parts[8].trim();

                if (!zip.matches("\\d{5}")) continue;
                try {
                    LocalDate date = LocalDate.parse(timestamp.split(" ")[0]);
                    int partialV = partialStr.isEmpty() ? 0 : Integer.parseInt(partialStr);
                    int fullyV = fullStr.isEmpty() ? 0 : Integer.parseInt(fullStr);
                    records.add(new CovidData(zip, date, partialV, fullyV));
                } catch (Exception ignored) {}
            }
        } catch (IOException e) {
            System.err.println("Error reading COVID CSV file: " + e.getMessage());
        }
        return records;
    }

    private List<CovidData> loadFromJSON() {
        List<CovidData> records = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try (Reader reader = new FileReader(filename)) {
            JSONArray array = (JSONArray) parser.parse(reader);
            for (Object obj : array) {
                JSONObject json = (JSONObject) obj;

                String zip = String.valueOf(json.get("zip_code")).trim();
                String partialStr = String.valueOf(json.get("partially_vaccinated")).trim();
                String fullStr = String.valueOf(json.get("fully_vaccinated")).trim();
                String timestamp = String.valueOf(json.get("etl_timestamp")).trim();

                if (!zip.matches("\\d{5}")) continue;
                try {
                    LocalDate date = LocalDate.parse(timestamp.split(" ")[0]);
                    int partial = partialStr.isEmpty() || partialStr.equals("null") ? 0 : Integer.parseInt(partialStr);
                    int full = fullStr.isEmpty() || fullStr.equals("null") ? 0 : Integer.parseInt(fullStr);
                    records.add(new CovidData(zip, date, partial, full));
                } catch (Exception ignored) {}
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error reading COVID JSON file: " + e.getMessage());
        }
        return records;
    }
}
