package edu.upenn.cit594.datamanagement;

import edu.upenn.cit594.logging.Logger;

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
     * Uses memoization to avoid reloading.
     *
     * @return Map of ZIP Code to population
     */
    public Map<String, Integer> loadPopulationData() {
        if (zipToPopulation != null) return zipToPopulation;

        zipToPopulation = new HashMap<>();

        // Log file name when open the file
        Logger.getInstance().log(filename);

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String headerLine = br.readLine();
            if (headerLine == null) return zipToPopulation;

            String[] headers = headerLine.replaceAll("\"", "").split(",");
            int zipIndex = -1, popIndex = -1;

            // Determine the indices of zip_code and population
            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].trim().toLowerCase();
                if (header.equals("zip_code")) zipIndex = i;
                else if (header.equals("population")) popIndex = i;
            }

            if (zipIndex == -1 || popIndex == -1) return zipToPopulation;

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.replaceAll("\"", "").split(",");

                if (parts.length <= Math.max(zipIndex, popIndex)) continue;

                String zip = parts[zipIndex].trim();
                String popStr = parts[popIndex].trim();

                if (zip.matches("\\d{5}") && popStr.matches("\\d+")) {
                    try {
                        zipToPopulation.put(zip, Integer.parseInt(popStr));
                    } catch (NumberFormatException ignored) {
                        // Skip line
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading population file: " + e.getMessage());
        }

        return zipToPopulation;
    }
}
