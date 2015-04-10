package com.ifmo.recommendersystem.tasks;

import com.ifmo.recommendersystem.ClassifierWrapper;
import com.ifmo.recommendersystem.FSSAlgorithm;
import com.ifmo.recommendersystem.utils.InstancesUtils;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static java.util.Objects.requireNonNull;

/**
 * Created by warrior on 18.11.14.
 */
public class PerformanceTask implements Callable<List<PerformanceResult>> {

    private final String datasetName;
    private final List<FSSAlgorithm> algorithms;
    private final ClassifierWrapper classifier;
    private final int testNumber;
    private final Instances train;
    private final Instances test;

    public PerformanceTask(String datasetName, int testNumber, Instances train, Instances test, List<FSSAlgorithm> algorithms, ClassifierWrapper classifier) {
        this.datasetName = datasetName;
        this.train = requireNonNull(train);
        this.test = requireNonNull(test);
        this.algorithms = requireNonNull(algorithms);
        this.classifier = requireNonNull(classifier);
        this.testNumber = testNumber;
    }

    @Override
    public List<PerformanceResult> call() {
        List<PerformanceResult> results = new ArrayList<>();
        for (FSSAlgorithm algorithm : algorithms) {
            FSSAlgorithm.Result result = algorithm.subsetSelection(train);
            Instances selectedTrain = InstancesUtils.removeAttributes(train, result.instances);
            Instances selectedTest = InstancesUtils.removeAttributes(test, result.instances);
            long runtime = result.runtime;
            int resultAttributeNumber = selectedTrain.numAttributes() - 1;
            double f1Measure = classifier.getF1Measure(selectedTrain, selectedTest);
            results.add(new PerformanceResult(datasetName,
                    algorithm.getName(),
                    classifier.getName(),
                    f1Measure,
                    resultAttributeNumber,
                    runtime,
                    testNumber));

        }
        return results;
    }
}
