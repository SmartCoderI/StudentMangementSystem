package edu.upenn.cit594.datamanagement;

import edu.upenn.cit594.logging.Logger;

import java.io.IOException;
import java.util.Arrays;
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

        try (CharacterReader charReader = new CharacterReader(filename)) {
            CSVReader csvReader = new CSVReader(charReader);
            String[] headerLine = csvReader.readRow();

            if (headerLine == null) return zipToPopulation;

            int zipIndex = -1, popIndex = -1;

            // Determine the indices of zip_code and population
            for (int i = 0; i < headerLine.length; i++) {
                String header = headerLine[i].trim().toLowerCase();
                if (header.equals("zip_code")) zipIndex = i;
                else if (header.equals("population")) popIndex = i;
            }

            if (zipIndex == -1 || popIndex == -1) return zipToPopulation;

            String[] line;
            int maxIndex = Math.max(zipIndex, popIndex);
            while ((line = csvReader.readRow()) != null) {
                if (Arrays.stream(line).count() <= maxIndex) continue;

                String zip = line[zipIndex].trim();
                String popStr = line[popIndex].trim();

                if (validateZip(zip)) {
                    try {
                        zipToPopulation.put(zip, Integer.parseInt(popStr));
                    } catch (NumberFormatException e) {
                        //Unparseable population data, skip the entry
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading population file: " + e.getMessage());
        } catch (CSVFormatException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }

        return zipToPopulation;
    }

    private boolean validateZip(String raw) {
        return raw.matches("^[0-9]{5}$");
    }
}
