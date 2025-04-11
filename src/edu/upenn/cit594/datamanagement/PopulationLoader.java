package edu.upenn.cit594.datamanagement;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/*
only in CSV
headers:
population: ignore entire row if data is not parsable as an integer
zipcode: ignore entire row if zipcode is not a parsable 5 digits number
 */
public class PopulationLoader {
    protected String filename;

    public PopulationLoader(String filename) {
        this.filename = filename;
    }

    public Map<String, Integer> loadPopulationData() {
        Map<String, Integer> populationMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.replaceAll("\"", "").split(",");
                if (parts.length != 2) continue;
                String zip = parts[0].trim();
                String popStr = parts[1].trim();
                if (!zip.matches("\\d{5}")) continue;
                try {
                    int population = Integer.parseInt(popStr);
                    populationMap.put(zip, population);
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException e) {
            System.err.println("Error reading population file: " + e.getMessage());
        }
        return populationMap;
    }
}
