package edu.upenn.cit594.datamanagement;

import edu.upenn.cit594.logging.Logger;
import edu.upenn.cit594.util.PropertyData;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * only provided in csv file, no JSON file
 * headers:
 * market_value: only use for calc if the data is parsable as numeric value, even as negative or 0
 * total_livable area: if parsable as a numerical value
 * zip_code: first 5 digits, ignore if first 5 characters are not all numerical, if zipcode less than 5 character
 */
public class PropertyLoader {
    private final String filename;
    private List<PropertyData> properties; // Memoization

    public PropertyLoader(String filename) {
        this.filename = filename;
    }

    /**
     * Loads and parses the property data CSV.
     * Skips rows with bad ZIPs, and handles missing or malformed fields.
     *
     * @return List of PropertyData
     */
    public List<PropertyData> loadPropertyData() {
        if (properties != null) {
            return properties;
        }

        properties = new ArrayList<>();

        Logger.getInstance().log(filename);

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String header = br.readLine();
            if (header == null) return properties;

            // Map column names to indices
            String[] columns = header.split(",", -1);
            Map<String, Integer> colIndex = new HashMap<>();
            for (int i = 0; i < columns.length; i++) {
                colIndex.put(columns[i].trim().toLowerCase(), i);
            }

            Integer zipIndex = colIndex.get("zip_code");
            Integer valueIndex = colIndex.get("market_value");
            Integer areaIndex = colIndex.get("total_livable_area");

            if (zipIndex == null || valueIndex == null || areaIndex == null) {
                System.err.println("Missing required columns in header.");
                return properties;
            }

            // Start processing meaningful entries
            String line;
            int maxIndex = Math.max(zipIndex, Math.max(valueIndex, areaIndex));
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                // Row have enough fields to safely access indices
                if (parts.length <= maxIndex) continue;

                String zipRaw = parts[zipIndex].trim();
                String zip = extractFiveDigitZip(zipRaw);
                if (zip == null) continue;

                if (zip.length() < 5 || !zip.substring(0, 5).matches("\\d{5}")) continue;

                Double marketValue = parseDoubleSafe(parts[valueIndex].trim());
                Double livableArea = parseDoubleSafe(parts[areaIndex].trim());

                PropertyData property = new PropertyData(zip, marketValue, livableArea);
                properties.add(property);
            }

        } catch (IOException e) {
            System.err.println("Error reading properties file: " + e.getMessage());
        }
        return properties;
    }

    private String extractFiveDigitZip(String raw) {
        if (raw == null) return null;

        if (raw.length() >= 5 && raw.substring(0, 5).matches("\\d{5}")) {
            String zip = raw.substring(0, 5);
            return zip;
        }
        return null;
    }

    private Double parseDoubleSafe(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return null; // treat malformed values as missing
        }
    }
}