package edu.upenn.cit594.processor;


import edu.upenn.cit594.util.PropertyData;

import java.util.List;

public class AvgMktValue implements PropertyFunction {
    @Override
    public int compute(List<PropertyData> properties) {
        double sum = 0;
        int count = 0;
        for (PropertyData p : properties) {
            Double value = p.getParsedMarketValue();
            if (value != null) {
                sum += value;
                count++;

                //debug
//                System.out.println(" Used MV: " + value + " for ZIP: " + p.getZipCode());
            }
        }

        //debug
//        if (!properties.isEmpty()) {
//            System.out.println(" Total count used for ZIP " + properties.get(0).getZipCode() + ": " + count);
//            System.out.println(" Total sum for ZIP " + properties.get(0).getZipCode() + ": " + sum);
//        }
        System.out.println(sum);
        System.out.println(count);
        return count == 0 ? 0 : (int) (sum / count);


    }
}

