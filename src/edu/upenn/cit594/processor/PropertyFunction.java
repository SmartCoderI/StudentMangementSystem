package edu.upenn.cit594.processor;

import java.util.List;

import edu.upenn.cit594.util.PropertyData;

public interface PropertyFunction {
    int compute(List<PropertyData> properties);
}
