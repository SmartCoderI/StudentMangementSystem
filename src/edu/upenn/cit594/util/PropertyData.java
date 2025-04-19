package edu.upenn.cit594.util;

/**
 * only provided in csv file, no JSON file
 * headers:
 * market_value
 * total_livable area
 * zip_code
 */
public class PropertyData {
    private final String zipCode;
    private final String marketValueRaw;
    private final String livableAreaRaw;

    public PropertyData(String zipCode, String marketValue, String totalLivableArea) {
        this.zipCode = zipCode;
        this.marketValueRaw = marketValue;
        this.livableAreaRaw = totalLivableArea;
    }

    public String getZipCode() {
        return zipCode;
    }

    /**
     * @return String-parsed-to-num value; otherwise null
     */
    public Double getParsedMarketValue() {
        try {
            return Double.parseDouble(marketValueRaw.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return String-parsed-to-num value; otherwise null
     */
    public Double getParsedLivableArea() {
        try {
            return Double.parseDouble(livableAreaRaw.trim());
        } catch (Exception e) {
            return null;
        }
    }

}
