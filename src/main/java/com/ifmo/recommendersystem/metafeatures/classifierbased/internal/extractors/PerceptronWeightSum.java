package com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors;

import com.ifmo.recommendersystem.metafeatures.classifierbased.Extractor;
import weka.core.Instances;

/**
 * Created by warrior on 07.06.15.
 */
public class PerceptronWeightSum implements Extractor {
    @Override
    public double extract(Instances instances) {
        MultilayerPerceptron perceptron = new MultilayerPerceptron();
        int nodes = Math.min(20, (instances.numAttributes() + instances.numClasses()) / 2);
        try {
            perceptron.setOptions(new String[]{"-N", "250", "-H", String.valueOf(nodes)});
            perceptron.buildClassifier(instances);
            return perceptron.weightSum();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
