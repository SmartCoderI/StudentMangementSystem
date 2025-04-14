package edu.upenn.cit594.processor;


import edu.upenn.cit594.util.PropertyData;

import java.util.List;
import java.util.stream.Collectors;

public class DataProcessor {
    private List<PropertyData> properties;

    public DataProcessor(List<PropertyData> properties) {
        this.properties = properties;
    }

    public double computeByZip(String zip, PropertyFunction strategy) {
        List<PropertyData> filtered = properties.stream()
                .filter(p -> zip.equals(p.getZipCode()))
                .collect(Collectors.toList());

        return strategy.compute(filtered);
    }
}
