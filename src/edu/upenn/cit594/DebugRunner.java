package edu.upenn.cit594;

import edu.upenn.cit594.processor.AvgMktValue;
import edu.upenn.cit594.processor.DataProcessor;
import edu.upenn.cit594.processor.PropertyFunction;

import java.util.Scanner;

public class DebugRunner {
    public static void main(String[] args) {
        // You can update this path to test your own files
        //--population=population.csv --covid=covid_data.csv --properties=simpleProperty1.csv --log=events.log
        // String covidFile = "covid_data.csv";
        // String populationFile = "population.csv";
        String covidFile = null;
        String populationFile = null;
        String propertyFile = "properties.csv";

//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Enter 5-digit ZIP Code to debug:");
//        System.out.print("> ");
        String zip = "19127";

        // Create a real processor — no test logic
        DataProcessor processor = new DataProcessor(populationFile, propertyFile, covidFile);

        // Use real strategy
        PropertyFunction strategy = new AvgMktValue();

        // Call real method (the one used by your Menu)
        int result = processor.getAvgMarketValue(zip, strategy);

        System.out.println("Average Market Value for ZIP " + zip + ": " + result);
    }
}
