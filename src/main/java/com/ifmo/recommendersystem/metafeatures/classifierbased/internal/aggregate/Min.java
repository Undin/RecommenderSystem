package com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate;

import com.ifmo.recommendersystem.metafeatures.classifierbased.Aggregator;

import java.util.stream.DoubleStream;

/**
 * Created by warrior on 04.06.15.
 */
public class Min implements Aggregator {
    @Override
    public double aggregate(double[] values) {
        return DoubleStream.of(values)
                .min()
                .getAsDouble();
    }
}
