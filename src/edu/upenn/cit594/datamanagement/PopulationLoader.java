package edu.upenn.cit594.datamanagement;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * only in CSV
 * headers:
 * population: ignore entire row if data is not parsable as an integer
 * zipcode: ignore entire row if zipcode is not a parsable 5 digits number
 */
public class PopulationLoader {
    private final String filename;
    private Map<String, Integer> zipToPopulation; // Memoization

    public PopulationLoader(String filename) {
        this.filename = filename;
    }

    /**
     * Loads and parses the population CSV file.
     * @return Map of ZIP Code to population
     */
    public Map<String, Integer> loadPopulationData() {
        if (zipToPopulation != null) return zipToPopulation;

        zipToPopulation = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // skip header
            if (line == null) return zipToPopulation;

            while ((line = br.readLine()) != null) {
                String[] parts = line.replaceAll("\"", "").split(",");
                if (parts.length != 2) continue;

                String zip = parts[0].trim();
                String popStr = parts[1].trim();

                if (zip.matches("\\d{5}") && popStr.matches("\\d+")) {
                    zipToPopulation.put(zip, Integer.parseInt(popStr));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading population file: " + e.getMessage());
        }
        return zipToPopulation;
    }
}
