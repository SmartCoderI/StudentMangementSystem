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
    private final Map<String, Integer> populationMap;
    private final List<PropertyData> properties;
    private final List<CovidData> covidData;

    // For memorization
    private final Map<String, Double> avgMarketValueCache = new HashMap<>();
    private final Map<String, Double> avgLivableAreaCache = new HashMap<>();
    private final Map<String, Integer> marketValuePerCapitaCache = new HashMap<>();
    private final Map<String, Integer> marketValuePerSqFtCache = new HashMap<>();
    private final Map<String, Map<LocalDate, Double>> vaccinationCache = new HashMap<>();

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

    public int getTotalPopulation() {
        return populationMap.values().stream().mapToInt(Integer::intValue).sum();
    }

    //3
    public Map<String, Double> getVaccinationPerCapita(String type, LocalDate date) {
        // Use inner map for date-specific memoization
        Map<LocalDate, Double> zipCache = vaccinationCache.computeIfAbsent(type, k -> new HashMap<>());
        Map<String, Double> result = new TreeMap<>();

        for (CovidData record : covidData) {
            if (!record.getDate().equals(date)) continue;
            String zip = record.getZipCode();
            int vaccinated = type.equals("partial") ? record.getPartial() : record.getFull();
            int population = populationMap.getOrDefault(zip, 0);

            if (vaccinated > 0 && population > 0) {
                double perCapita = Math.round(((double) vaccinated / population) * 10000.0) / 10000.0;
                result.put(zip, perCapita);
            }
        }
        // Cache the result for future calls
        zipCache.put(date, 1.0); // You can choose to store each zip individually if needed
        return result;
    }

    //4
    public int getAvgMarketValue(String zip, PropertyFunction strategy) {
        if (strategy instanceof AvgMktValue && avgMarketValueCache.containsKey(zip)) {
            return (int) (double) avgMarketValueCache.get(zip);
        } else if (strategy instanceof AvgLivableArea && avgLivableAreaCache.containsKey(zip)) {
            return (int) (double) avgLivableAreaCache.get(zip);
        }

        List<PropertyData> filtered = properties.stream()
                .filter(p -> zip.equals(p.getZipCode()))
                .collect(Collectors.toList());

        System.out.println("filter size:" + filtered.size());

        double result = strategy.compute(filtered);

        if (strategy instanceof AvgMktValue) {
            avgMarketValueCache.put(zip, result);
        } else if (strategy instanceof AvgLivableArea) {
            avgLivableAreaCache.put(zip, result);
        }

        return (int) result;
    }

    //6
    public int getMarketValuePerCapita(String zip) {
        if (marketValuePerCapitaCache.containsKey(zip)) {
            return marketValuePerCapitaCache.get(zip);
        }

        double totalValue = properties.stream()
                .filter(p -> zip.equals(p.getZipCode()))
                .map(PropertyData::getParsedMarketValue)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        int population = populationMap.getOrDefault(zip, 0);
        int result = (population == 0 || totalValue == 0) ? 0 : (int) (totalValue / population);

        marketValuePerCapitaCache.put(zip, result);
        return result;
    }


    //custom method: total market value divided by total livable area
    public int getMarketValuePerSqFt(String zip) {
        if (marketValuePerSqFtCache.containsKey(zip)) {
            return marketValuePerSqFtCache.get(zip);
        }

        double totalMarketValue = 0;
        double totalLivableArea = 0;

        for (PropertyData p : properties) {
            if (p == null || !zip.equals(p.getZipCode())) continue;

            Double marketValue = p.getParsedMarketValue();
            Double livableArea = p.getParsedLivableArea();

            if (marketValue != null && livableArea != null && livableArea > 0) {
                totalMarketValue += marketValue;
                totalLivableArea += livableArea;
            }
        }

        int result = (totalLivableArea == 0) ? 0 : (int) (totalMarketValue / totalLivableArea);
        marketValuePerSqFtCache.put(zip, result);
        return result;
    }

    public void clearCache() {
        avgMarketValueCache.clear();
        avgLivableAreaCache.clear();
    }

}
