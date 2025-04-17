package edu.upenn.cit594.processor;


import edu.upenn.cit594.util.PropertyData;

import java.util.List;

public class AvgLivableArea implements PropertyFunction {
    @Override
    public int compute(List<PropertyData> properties) {
        double sum = 0;
        int count = 0;
        for (PropertyData p : properties) {
            Double area = p.getTotalLivableArea();
            if (area != null) {
                sum += area;
                count++;
            }
        }
        return count == 0 ? 0 : (int)(sum / count);
    }
}

