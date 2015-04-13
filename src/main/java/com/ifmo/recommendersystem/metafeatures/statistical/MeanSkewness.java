package com.ifmo.recommendersystem.metafeatures.statistical;

import com.ifmo.recommendersystem.utils.StatisticalUtils;
import weka.core.Instances;

/**
 * Created by warrior on 22.03.15.
 */
public class MeanSkewness extends AbstractStatisticalExtractor {

    public static final String NAME = "Mean skewness";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double extractValue(Instances instances) {
        int count = 0;
        double sum = 0;
        for (int i = 0; i < instances.numAttributes(); i++) {
            if (isNonClassNumericalAttribute(instances, i)) {
                count++;
                double[] values = instances.attributeToDoubleArray(i);
                double mean = StatisticalUtils.mean(values);
                double variance = StatisticalUtils.variance(values, mean);
                sum += StatisticalUtils.centralMoment(values, 3, mean) / Math.pow(variance, 1.5);
            }
        }
        return sum / count;
    }
}
