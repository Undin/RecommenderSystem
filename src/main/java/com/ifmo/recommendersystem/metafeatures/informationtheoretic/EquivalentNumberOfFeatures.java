package com.ifmo.recommendersystem.metafeatures.informationtheoretic;

import weka.core.Instances;

import static com.ifmo.recommendersystem.utils.InformationTheoreticUtils.*;

/**
 * Created by warrior on 23.03.15.
 */
public class EquivalentNumberOfFeatures extends AbstractDiscretizeExtractor {

    public static final String NAME = "Equivalent number of features";

    private double meanMutualInformation;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double extractValue(Instances instances) {
        meanMutualInformation = new MeanMutualInformation().extractValue(instances);
        return super.extractValue(instances);
    }

    @Override
    protected double extractValueInternal(Instances instances) {
        int classIndex = instances.classIndex();
        if (classIndex < 0) {
            throw new IllegalArgumentException("dataset hasn't class attribute");
        }
        double[] values = instances.attributeToDoubleArray(classIndex);
        EntropyResult result = entropy(values, instances.classAttribute().numValues());
        return result.entropy / meanMutualInformation;
    }
}
