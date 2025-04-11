package edu.upenn.cit594.util;
/*
only in CSV
headers:
population: ignore entire row if data is not parsable as an integer
zipcode: ignore entire row if zipcode is not a parsable 5 digits number
 */
public class PopulationData {
    private final String zipCode;
    private final int population;

    public PopulationData(String zipCode, int population) {
        this.zipCode = zipCode;
        this.population = population;
    }

    public String getZipCode() { return zipCode; }
    public int getPopulation() { return population; }
}
