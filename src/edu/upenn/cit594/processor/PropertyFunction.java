package edu.upenn.cit594.processor;

import java.util.List;

import edu.upenn.cit594.util.PropertyData;

public interface PropertyFunction {
    double compute(List<PropertyData> properties);
}
