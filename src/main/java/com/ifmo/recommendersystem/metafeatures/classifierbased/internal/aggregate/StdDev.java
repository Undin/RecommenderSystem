package com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate;

import com.ifmo.recommendersystem.metafeatures.classifierbased.Aggregator;
import com.ifmo.recommendersystem.utils.StatisticalUtils;

/**
 * Created by warrior on 04.06.15.
 */
public class StdDev implements Aggregator {
    @Override
    public double aggregate(double[] values) {
        return StatisticalUtils.variance(values);
    }
}
