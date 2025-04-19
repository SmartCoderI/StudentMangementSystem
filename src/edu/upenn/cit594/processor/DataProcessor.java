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
    private final Map<String, String> customFeatureCache = new HashMap<>();
    private final Map<String, Map<LocalDate, Map<String, Double>>> vaccinationCache = new HashMap<>();

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

    // Feature 2: total population for all Zip codes
    public int getTotalPopulation() {
        return populationMap.values().stream().mapToInt(Integer::intValue).sum();
    }

    // Feature 3: total specified vaccinations per capital for each Zip code for the input date
    public Map<String, Double> getVaccinationPerCapita(String type, LocalDate date) {
        Map<LocalDate, Map<String, Double>> innerCache = vaccinationCache.computeIfAbsent(type, k -> new HashMap<>());

        if (innerCache.containsKey(date)) {
            return innerCache.get(date);
        }

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
        innerCache.put(date, result);
        return result;
    }

    // Feature 4 (avg market value given Zip)
    // and Feature 5 (avg livable area given Zip)
    // Depending on input PropertyFunction strategy
    public int getPropertyAvg(String zip, PropertyFunction strategy) {
        if (strategy instanceof AvgMktValue && avgMarketValueCache.containsKey(zip)) {
            return (int) (double) avgMarketValueCache.get(zip);
        } else if (strategy instanceof AvgLivableArea && avgLivableAreaCache.containsKey(zip)) {
            return (int) (double) avgLivableAreaCache.get(zip);
        }

        List<PropertyData> filteredByZip = properties.stream()
                .filter(p -> zip.equals(p.getZipCode()))
                .collect(Collectors.toList());

        double result;

        if (filteredByZip.size() > 0) {
            result = strategy.computeAverage(filteredByZip);
        } else {
            result = 0.0;
        }

        if (strategy instanceof AvgMktValue) {
            avgMarketValueCache.put(zip, result);
        } else if (strategy instanceof AvgLivableArea) {
            avgLivableAreaCache.put(zip, result);
        }

        return (int) result;
    }

    // Feature 6: total mkt value of properties, per capita, given Zip
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

    // Feature 7: custom method, given zip input, output corresponding value per area unit and latest available vac (half and fully) ratio
    public String getCustom(String zip) {
        if (customFeatureCache.containsKey(zip)) {
            return customFeatureCache.get(zip);
        }

        // Value per area unit
        List<PropertyData> valueFilteredByZip = properties.stream()
                .filter(p -> zip.equals(p.getZipCode()))
                .collect(Collectors.toList());

        double totalMarketValue = 0;
        double totalLivableArea = 0;

        for (PropertyData p : valueFilteredByZip) {
            if (p == null) continue;

            Double marketValue = p.getParsedMarketValue();
            Double livableArea = p.getParsedLivableArea();

            if (marketValue != null && livableArea != null && livableArea > 0) {
                totalMarketValue += marketValue;
                totalLivableArea += livableArea;
            }
        }

        int unitValue = (totalLivableArea == 0) ? 0 : (int) (totalMarketValue / totalLivableArea);

        // Latest vaccination record
        CovidData mostRecentCovidData = covidData.stream()
                .filter(c -> zip.equals(c.getZipCode()))
                .max(Comparator.comparing(CovidData::getDate))
                .orElse(null);

        int totalPopulation = populationMap.getOrDefault(zip, 0);

        String unitValueResult = (unitValue == 0) ?
                "No property data for the area " + zip + ". "
                : zip + " area has an average livable area market price of $" +
                unitValue + " per area unit. ";

        String covidResult;

        if (mostRecentCovidData != null) {
            double vacRatioRaw = (double) (mostRecentCovidData.getFull() + mostRecentCovidData.getPartial()) / totalPopulation;
            int vacRatioPercent = (int) Math.round(vacRatioRaw * 100);
            covidResult = "This area has the latest vaccination ratio of "
                    + vacRatioPercent
                    + "% as of "
                    + mostRecentCovidData.getDate().toString() + ".";
        } else {
            covidResult = "No vaccination for the area.";
        }

        String result = unitValueResult + covidResult;

        customFeatureCache.put(zip, result);
        return result;
    }
}
