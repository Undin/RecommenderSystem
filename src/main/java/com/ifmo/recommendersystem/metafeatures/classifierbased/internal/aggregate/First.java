package com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate;

import com.ifmo.recommendersystem.metafeatures.classifierbased.Aggregator;

/**
 * Created by warrior on 07.06.15.
 */
public class First implements Aggregator {
    @Override
    public double aggregate(double[] values) {
        return values[0];
    }
}
