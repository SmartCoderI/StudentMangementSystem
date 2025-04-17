package edu.upenn.cit594.util;

/*
only provided in csv file, no JSON file
headers:
market_value
total_livable area
zip_code
 */
public class PropertyData {
    private final String zipCode;
    private final Double marketValue;
    private final Double totalLivableArea;

    public PropertyData(String zipCode, Double marketValue, Double totalLivableArea) {
        this.zipCode = zipCode;
        this.marketValue = marketValue;
        this.totalLivableArea = totalLivableArea;
    }

    public String getZipCode() {
        return zipCode;
    }

    public Double getMarketValue() {
        return marketValue;
    }

    public Double getTotalLivableArea() {
        return totalLivableArea;
    }
}
