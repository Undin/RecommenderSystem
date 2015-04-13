package com.ifmo.recommendersystem.metafeatures.general;

import com.ifmo.recommendersystem.metafeatures.MetaFeatureExtractor;
import weka.core.Instances;

/**
 * Created by warrior on 22.03.15.
 */
public class DataSetDimensionality extends MetaFeatureExtractor {

    public static final String NAME = "Data set dimensionality";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double extractValue(Instances instances) {
        int instanceNumber = instances.numInstances();
        int attributeNumber = instances.classIndex() >= 0 ? instances.numAttributes() - 1 : instances.numAttributes();
        return (double) instanceNumber / attributeNumber;
    }
}
