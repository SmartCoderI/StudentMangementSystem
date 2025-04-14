package edu.upenn.cit594.util;

/*
only provided in csv file, no JSON file
headers:
market_value
total_livable area
zip_code
 */
public class PropertyData {
    protected final String zipCode;
    protected final double marketValue;
    protected final double totalLivableArea;

    public PropertyData(String zipCode, double marketValue, double totalLivableArea) {
        this.zipCode = zipCode;
        this.marketValue = marketValue;
        this.totalLivableArea = totalLivableArea;
    }

    public String getZipCode() {
        return zipCode;
    }

    public double getMarketValue() {
        return marketValue;
    }

    public double getTotalLivableArea() {
        return totalLivableArea;
    }
}
