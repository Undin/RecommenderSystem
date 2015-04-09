package com.ifmo.recommendersystem.tasks;

import com.ifmo.recommendersystem.*;
import weka.core.Instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Created by warrior on 18.11.14.
 */
public class PerformanceTask extends AbstractTask {

    public static final String PERFORMANCE_DIRECTORY = "performance";

    private final Instances train;
    private final Instances test;
    private final List<FSSAlgorithm> algorithms;
    private final ClassifierWrapper classifier;
    private final int testNumber;

    public PerformanceTask(String datasetName, int testNumber, Instances train, Instances test, List<FSSAlgorithm> algorithms, ClassifierWrapper classifier) {
        super(datasetName);
        this.train = requireNonNull(train);
        this.test = requireNonNull(test);
        this.algorithms = requireNonNull(algorithms);
        this.classifier = requireNonNull(classifier);
        this.testNumber = testNumber;
    }

    @Override
    protected void runInternal() {
        for (FSSAlgorithm algorithm : algorithms) {
            FSSAlgorithm.Result result = algorithm.subsetSelection(train);
            Instances selectedTrain = InstancesUtils.removeAttributes(train, result.instances);
            Instances selectedTest = InstancesUtils.removeAttributes(test, result.instances);
            long runtime = result.runtime;
            int resultAttributeNumber = selectedTrain.numAttributes() - 1;
            double accuracy = classifier.getAccuracy(selectedTrain, selectedTest);
            PerformanceResult performanceResult = new PerformanceResult(datasetName,
                    algorithm.getName(),
                    classifier.getName(),
                    accuracy,
                    resultAttributeNumber,
                    runtime,
                    testNumber);
            String directoryPath = Utils.createPath(RESULT_DIRECTORY,
                    PERFORMANCE_DIRECTORY,
                    classifier.getName(),
                    datasetName,
                    algorithm.getName());
            String fileName = Utils.createName(classifier.getName(), datasetName, algorithm.getName(), String.valueOf(testNumber));
            File directory = new File(directoryPath);
            directory.mkdirs();
            try (PrintWriter writer = new PrintWriter(new File(directory, fileName + ".json"))) {
                writer.print(performanceResult.toJSON().toString(4));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected String getTaskName() {
        return "performance " + datasetName + " " + testNumber;
    }

}
