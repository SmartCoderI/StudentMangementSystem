package edu.upenn.cit594.processor;

import edu.upenn.cit594.util.PropertyData;

import java.util.List;

public interface PropertyFunction {
    int computeAverage(List<PropertyData> properties);
}
