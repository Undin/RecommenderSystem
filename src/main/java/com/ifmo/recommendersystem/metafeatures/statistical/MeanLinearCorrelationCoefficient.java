package com.ifmo.recommendersystem.metafeatures.statistical;

import com.ifmo.recommendersystem.utils.StatisticalUtils;
import weka.core.Instances;

/**
 * Created by warrior on 22.03.15.
 */
public class MeanLinearCorrelationCoefficient extends AbstractStatisticalExtractor {

    private static final String NAME = "Mean absolute linear correlation coefficient of all possible pairs of features";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double extractValue(Instances instances) {
        double sum = 0;
        int count = 0;
        for (int i = 0; i < instances.numAttributes(); i++) {
            if (isNonClassNumericalAttribute(instances, i)) {
                double[] values1 = instances.attributeToDoubleArray(i);
                for (int j = i + 1; j < instances.numAttributes(); j++) {
                    if (isNonClassNumericalAttribute(instances, j)) {
                        double[] values2 = instances.attributeToDoubleArray(j);
                        double linearCorrelationCoefficient = StatisticalUtils.linearCorrelationCoefficient(values1, values2);
                        if (Double.isFinite(linearCorrelationCoefficient)) {
                            sum += linearCorrelationCoefficient;
                            count++;
                        }
                    }
                }
            }
        }
        double result = sum / count;
        return Double.isFinite(result) ? result : 0;
    }
}
