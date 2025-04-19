package edu.upenn.cit594.datamanagement;

import edu.upenn.cit594.logging.Logger;
import edu.upenn.cit594.util.PropertyData;

import java.io.IOException;
import java.util.*;


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

        try (CharacterReader charReader = new CharacterReader(filename)) {
            CSVReader csvReader = new CSVReader(charReader);

            String[] headers = csvReader.readRow();
            if (headers == null) return properties;

            Map<String, Integer> colIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                colIndex.put(headers[i].trim().toLowerCase(), i);
            }

            Integer zipIndex = colIndex.get("zip_code");
            Integer valueIndex = colIndex.get("market_value");
            Integer areaIndex = colIndex.get("total_livable_area");

            if (zipIndex == null || valueIndex == null || areaIndex == null) {
                System.err.println("Missing required columns.");
                return properties;
            }

            String[] row;

            while ((row = csvReader.readRow()) != null) {
                if (Arrays.stream(row).count() <= zipIndex) continue;

                /* Validate zip code:
                1. at least 5 digits
                2. Ignore the entire data for following cases
                2.1 First 5 characters are not all numerical: e.g., 191043, 19104a, 19a04
                2.2 Less than 5 character. E.g. 1910
                */
                String validatedZip = validateZip(row[zipIndex].trim());
                if (validatedZip == null) continue;
                // Keep market value and livable area as are
                String marketValue = row[valueIndex];
                String livableArea = row[areaIndex];

                // Keep all valid zip entries
                properties.add(new PropertyData(validatedZip, marketValue, livableArea));
            }

        } catch (IOException e) {
            System.err.println("Error reading properties file: " + e.getMessage());
        } catch (CSVFormatException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
        return properties;
    }

    private String validateZip(String raw) {
        if (raw == null || raw.length() < 5) return null;

        if (raw.substring(0, 5).matches("^[0-9]{5}$")) {
            String zip = raw.substring(0, 5);
            return zip;
        }
        return null;
    }
}