package edu.upenn.cit594.util;
import java.time.LocalDate;

/*
headers:
zipcode
date: etl_timestamp
partialV: total # people received first dose in zipcode
fullyV: total # people received 2nd dose in zipcode
 */
public class CovidData {
    private final String zipCode;
    private final LocalDate date;
    private final int partialV;
    private final int fullyV;

    public CovidData(String zipCode, LocalDate date, int partialV, int fullyV) {
        this.zipCode = zipCode;
        this.date = date;
        this.partialV = partialV;
        this.fullyV = fullyV;
    }

    public String getZipCode() { return zipCode; }
    public LocalDate getDate() { return date; }
    public int getPartial() { return partialV; }
    public int getFull() { return fullyV; }
}
