package com.ifmo.recommendersystem.evaluation;

import weka.attributeSelection.UnsupervisedSubsetEvaluator;
import weka.clusterers.Clusterer;
import weka.core.Instances;

import java.util.BitSet;
import java.util.stream.IntStream;

/**
 * Created by warrior on 08.05.15.
 */
public abstract class AbstractRecommenderSystemEvaluator extends UnsupervisedSubsetEvaluator {

    public double evaluateSubset(int[] subset) throws Exception {
        BitSet bitSet = new BitSet();
        IntStream.of(subset).forEach(bitSet::set);
        return evaluateSubset(bitSet);
    }

    @Override
    public void buildEvaluator(Instances data) throws Exception {
    }

    @Override
    public int getNumClusters() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public Clusterer getClusterer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setClusterer(Clusterer d) {
        throw new UnsupportedOperationException();
    }
}
