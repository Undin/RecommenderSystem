package com.ifmo.recommendersystem.metafeatures.classifierbased;

import com.ifmo.recommendersystem.metafeatures.MetaFeatureExtractor;
import weka.core.Instances;

/**
 * Created by warrior on 04.06.15.
 */
public abstract class AbstractClassifierBasedExtractor extends MetaFeatureExtractor {

    private static final int ROUNDS = 50;

    private final Extractor extractor;
    private final Transform transform;
    private final Aggregator aggregator;

    public AbstractClassifierBasedExtractor(Extractor extractor, Transform transform, Aggregator aggregator) {
        this.extractor = extractor;
        this.transform = transform;
        this.aggregator = aggregator;
    }

    @Override
    protected double extractValue(Instances instances) {
        double[] values = new double[ROUNDS];
        for (int i = 0; i < ROUNDS; i++) {
            Instances subspace = transform.transform(instances);
            values[i] = extractor.extract(subspace);
        }
        return aggregator.aggregate(values);
    }
}
