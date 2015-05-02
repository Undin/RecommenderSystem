package com.ifmo.recommendersystem.tasks;

import com.ifmo.recommendersystem.ClassifierWrapper;
import com.ifmo.recommendersystem.FSSAlgorithm;
import com.ifmo.recommendersystem.utils.InstancesUtils;
import weka.core.Instances;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Created by warrior on 18.11.14.
 */
public class PerformanceTask extends RecursiveTask<List<PerformanceResult>> {

    private final String datasetName;
    private final FSSAlgorithm algorithm;
    private final List<ClassifierWrapper> classifiers;
    private final int testNumber;
    private final Instances train;
    private final Instances test;

    public PerformanceTask(List<ClassifierWrapper> classifiers, FSSAlgorithm algorithm, String datasetName, Instances train, Instances test, int testNumber) {
        this.datasetName = datasetName;
        this.train = requireNonNull(train);
        this.test = requireNonNull(test);
        this.algorithm = requireNonNull(algorithm);
        this.classifiers = requireNonNull(classifiers);
        this.testNumber = testNumber;
    }

    @Override
    protected List<PerformanceResult> compute() {
        Instant start = Instant.now();
        System.out.format(">> [%s] %s-%d %s\n",
                start.atZone(ZoneId.of("UTC+3")).toLocalTime().toString(),
                datasetName,
                testNumber,
                algorithm.getName());

        FSSAlgorithm.Result fssResult = algorithm.subsetSelection(train);
        Instances selectedTrain = InstancesUtils.removeAttributes(train, fssResult.instances);
        Instances selectedTest = InstancesUtils.removeAttributes(test, fssResult.instances);

        List<RecursiveTask<ClassifierWrapper.Result>> subTasks = classifiers.stream()
                .map(classifier -> {
                    ClassifierTask task = new ClassifierTask(classifier, selectedTrain, selectedTest);
                    task.fork();
                    return task;
                })
                .collect(Collectors.toList());
        List<PerformanceResult> results = subTasks.stream()
                .map(ForkJoinTask::join)
                .map(classifierResult -> new PerformanceResult(datasetName, testNumber, fssResult, classifierResult))
                .collect(Collectors.toList());

        Instant end = Instant.now();
        System.out.format("<< [%s] %s-%d %s. exec time: %d\n",
                end.atZone(ZoneId.of("UTC+3")).toLocalTime().toString(),
                datasetName,
                testNumber,
                algorithm.getName(),
                Duration.between(start, end).toMillis());

        return results;
    }

    private static class ClassifierTask extends RecursiveTask<ClassifierWrapper.Result> {

        private final ClassifierWrapper classifier;
        private final Instances train;
        private final Instances test;

        public ClassifierTask(ClassifierWrapper classifier, Instances train, Instances test) {
            this.classifier = classifier;
            this.train = train;
            this.test = test;
        }

        @Override
        protected ClassifierWrapper.Result compute() {
            return classifier.computeAccuracyAndF1Measure(train, test);
        }
    }
}
