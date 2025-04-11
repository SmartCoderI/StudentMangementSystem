package edu.upenn.cit594.datamanagement;

import edu.upenn.cit594.util.PropertyData;

import java.io.*;
import java.util.*;

/*
only provided in csv file, no JSON file
headers:
market_value: only use for calc if the data is parsable as numeric value, even as negative or 0
total_livable area: if parsable as a numerical value
zip_code: first 5 digits, ignore if first 5 characters are not all numerical, if zipcode less than 5 character
 */
public class PropertyLoader {
    protected String filename;

    public PropertyLoader(String filename) {
        this.filename = filename;
    }

    public List<PropertyData> loadPropertyData() {
        List<PropertyData> properties = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String header = br.readLine();
            if (header == null) return properties;
            String[] columns = header.split(",");

            int zipIndex = -1, valueIndex = -1, areaIndex = -1;
            for (int i = 0; i < columns.length; i++) {
                String col = columns[i].trim().toLowerCase();
                if (col.equals("zip_code")) zipIndex = i;
                if (col.equals("market_value")) valueIndex = i;
                if (col.equals("total_livable_area")) areaIndex = i;
            }

            if (zipIndex == -1 || valueIndex == -1 || areaIndex == -1) return properties;

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length <= Math.max(zipIndex, Math.max(valueIndex, areaIndex))) continue;

                String zip = parts[zipIndex].trim();
                if (!zip.matches("\\d{5}")) continue;
                try {
                    double value = Double.parseDouble(parts[valueIndex]);
                    double area = Double.parseDouble(parts[areaIndex]);
                    properties.add(new PropertyData(zip, value, area));
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException e) {
            System.err.println("Error reading properties file: " + e.getMessage());
        }
        return properties;
    }
}