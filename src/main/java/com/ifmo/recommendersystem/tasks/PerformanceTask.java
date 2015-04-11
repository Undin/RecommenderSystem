package com.ifmo.recommendersystem.tasks;

import com.ifmo.recommendersystem.ClassifierWrapper;
import com.ifmo.recommendersystem.FSSAlgorithm;
import com.ifmo.recommendersystem.utils.InstancesUtils;
import com.ifmo.recommendersystem.utils.Pair;
import weka.core.Instances;

import java.util.concurrent.Callable;

import static java.util.Objects.requireNonNull;

/**
 * Created by warrior on 18.11.14.
 */
public class PerformanceTask implements Callable<PerformanceResult> {

    private final String datasetName;
    private final FSSAlgorithm algorithm;
    private final ClassifierWrapper classifier;
    private final int testNumber;
    private final Instances train;
    private final Instances test;

    public PerformanceTask(String datasetName, int testNumber, Instances train, Instances test, FSSAlgorithm algorithm, ClassifierWrapper classifier) {
        this.datasetName = datasetName;
        this.train = requireNonNull(train);
        this.test = requireNonNull(test);
        this.algorithm = requireNonNull(algorithm);
        this.classifier = requireNonNull(classifier);
        this.testNumber = testNumber;
    }

    @Override
    public PerformanceResult call() {
        FSSAlgorithm.Result result = algorithm.subsetSelection(train);
        Instances selectedTrain = InstancesUtils.removeAttributes(train, result.instances);
        Instances selectedTest = InstancesUtils.removeAttributes(test, result.instances);
        long runtime = result.runtime;
        int resultAttributeNumber = selectedTrain.numAttributes() - 1;
        Pair<Double, Double> effectiveness = classifier.computeAccuracyAndF1Measure(selectedTrain, selectedTest);
        return new PerformanceResult(datasetName,
                algorithm.getName(),
                classifier.getName(),
                effectiveness.first,
                effectiveness.second,
                resultAttributeNumber,
                runtime,
                testNumber);
    }
}

