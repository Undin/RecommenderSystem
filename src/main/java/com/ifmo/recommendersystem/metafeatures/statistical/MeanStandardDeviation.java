package com.ifmo.recommendersystem.metafeatures.statistical;

import com.ifmo.recommendersystem.utils.StatisticalUtils;
import weka.core.Instances;

import java.util.stream.IntStream;

/**
 * Created by warrior on 16.05.15.
 */
public class MeanStandardDeviation extends AbstractStatisticalExtractor {

    public static final String NAME = "Mean standard deviation";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected double extractValue(Instances instances) {
        return IntStream.range(0, instances.numAttributes())
                .filter(i -> isNonClassNumericalAttribute(instances, i))
                .mapToDouble(i -> Math.sqrt(StatisticalUtils.variance(instances.attributeToDoubleArray(i))))
                .average()
                .getAsDouble();
    }
}
