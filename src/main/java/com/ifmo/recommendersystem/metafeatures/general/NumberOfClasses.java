package com.ifmo.recommendersystem.metafeatures.general;

import com.ifmo.recommendersystem.metafeatures.MetaFeatureExtractor;
import weka.core.Instances;

/**
 * Created by warrior on 22.03.15.
 */
public class NumberOfClasses extends MetaFeatureExtractor {

    public static final String NAME = "Number of classes";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double extractValue(Instances instances) {
        if (instances.classIndex() >= 0) {
            return instances.numClasses();
        }
        return 0;
    }
}
