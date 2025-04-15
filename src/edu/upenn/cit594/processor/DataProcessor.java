package edu.upenn.cit594.processor;


import edu.upenn.cit594.datamanagement.CovidLoader;
import edu.upenn.cit594.datamanagement.PopulationLoader;
import edu.upenn.cit594.datamanagement.PropertyLoader;
import edu.upenn.cit594.util.CovidData;
import edu.upenn.cit594.util.PropertyData;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class DataProcessor {
    //private List<PropertyData> properties;
    private final Map<String, Integer> populationMap;
    private final List<PropertyData> properties;
    private final List<CovidData> covidData;

//    public DataProcessor(List<PropertyData> properties) {
//        this.properties = properties;
//    }
public DataProcessor(String populationFile, String propertyFile, String covidFile) {
    if (populationFile != null) {
        populationMap = new PopulationLoader(populationFile).loadPopulationData();
    } else {
        populationMap = new HashMap<>();
    }

    if (propertyFile != null) {
        properties = new PropertyLoader(propertyFile).loadPropertyData();
    } else {
        properties = new ArrayList<>();
    }

    if (covidFile != null) {
        covidData = new CovidLoader(covidFile).loadCovidData();
    } else {
        covidData = new ArrayList<>();
    }
}

    public double computeByZip(String zip, PropertyFunction strategy) {
        List<PropertyData> filtered = properties.stream()
                .filter(p -> zip.equals(p.getZipCode()))
                .collect(Collectors.toList());

        return strategy.compute(filtered);
    }

    //a lot more to be added
    public int getTotalPopulation() {
        return populationMap.values().stream().mapToInt(Integer::intValue).sum();
    }
    public Map<String, Double> getVaccinationPerCapita(String type, LocalDate date) {
        Map<String, Integer> zipToVaccination = new HashMap<>();

        for (CovidData record : covidData) {
            if (record.getDate().equals(date)) {
                int count = type.equals("partial") ? record.getPartial() : record.getFull();
                if (count > 0 && populationMap.containsKey(record.getZipCode())) {
                    zipToVaccination.put(record.getZipCode(), count);
                }
            }
        }

        Map<String, Double> result = new TreeMap<>();
        for (Map.Entry<String, Integer> entry : zipToVaccination.entrySet()) {
            String zip = entry.getKey();
            int vaccinated = entry.getValue();
            int population = populationMap.getOrDefault(zip, 0);
            if (population > 0) {
                double perCapita = (double) vaccinated / population;
                result.put(zip, Math.round(perCapita * 10000.0) / 10000.0);
            }
        }
        return result;
    }
////
    public int getZipStatistic(String zip, PropertyFunction strategy) {
        List<PropertyData> filtered = properties.stream()
                .filter(p -> p.getZipCode().equals(zip))
                .collect(Collectors.toList());
        return (int)strategy.compute(filtered);
    }

    public int getMarketValuePerCapita(String zip) {
        double totalValue = properties.stream()
                .filter(p -> p.getZipCode().equals(zip))
                .mapToDouble(PropertyData::getMarketValue)
                .sum();
        int population = populationMap.getOrDefault(zip, 0);
        if (population == 0 || totalValue == 0) return 0;
        return (int) (totalValue / population);
    }
}
