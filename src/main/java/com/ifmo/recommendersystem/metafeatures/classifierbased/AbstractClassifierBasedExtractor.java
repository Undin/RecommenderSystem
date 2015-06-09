package com.ifmo.recommendersystem.metafeatures.classifierbased;

import com.ifmo.recommendersystem.metafeatures.MetaFeatureExtractor;
import weka.core.Instances;

/**
 * Created by warrior on 04.06.15.
 */
public abstract class AbstractClassifierBasedExtractor extends MetaFeatureExtractor {

    private static final int ROUNDS = 20;

    private final Extractor extractor;
    private final Transform transform;
    private final Aggregator aggregator;
    private final int rounds;

    public AbstractClassifierBasedExtractor(Extractor extractor, Transform transform, Aggregator aggregator, int rounds) {
        this.extractor = extractor;
        this.transform = transform;
        this.aggregator = aggregator;
        this.rounds = rounds;
    }

    public AbstractClassifierBasedExtractor(Extractor extractor, Transform transform, Aggregator aggregator) {
        this(extractor, transform, aggregator, ROUNDS);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    protected double extractValue(Instances instances) {
        String extractorName = getClass().getSimpleName();
        double[] values = new double[rounds];
        for (int i = 0; i < rounds; i++) {
            System.out.format("%s %d\n", extractorName, i);
            Instances subspace = transform.transform(instances);
            values[i] = extractor.extract(subspace);
        }
        return aggregator.aggregate(values);
    }
}
