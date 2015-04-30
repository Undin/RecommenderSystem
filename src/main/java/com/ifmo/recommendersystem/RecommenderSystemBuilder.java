package com.ifmo.recommendersystem;

import com.ifmo.recommendersystem.config.BuildConfig;
import com.ifmo.recommendersystem.tasks.ExtractResult;
import com.ifmo.recommendersystem.tasks.ExtractTask;
import com.ifmo.recommendersystem.tasks.PerformanceResult;
import com.ifmo.recommendersystem.tasks.PerformanceTask;
import com.ifmo.recommendersystem.utils.InstancesUtils;
import com.ifmo.recommendersystem.utils.Pair;
import com.ifmo.recommendersystem.utils.PathUtils;
import weka.core.Instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class RecommenderSystemBuilder {

    public static final String META_FEATURES_DIRECTORY = "results/metaFeatures";
    public static final String PERFORMANCE_DIRECTORY = "results/test_performance";

    public final static int ROUNDS = 5;
    public final static int FOLDS = 10;

    private final BuildConfig config;

    private final Random random = new Random();

    public RecommenderSystemBuilder(BuildConfig config) {
        this.config = config;
    }

    public void build() {
        List<Pair<String, Instances>> instancesList = new ArrayList<>(config.getDatasets().size());
        for (String datasetName : config.getDatasets()) {
            String path = config.createPath(datasetName);
            try {
                Instances instances = InstancesUtils.createInstances(path, InstancesUtils.REMOVE_STRING_ATTRIBUTES |
                        InstancesUtils.REMOVE_UNINFORMATIVE_ATTRIBUTES);
                instancesList.add(Pair.of(datasetName, instances));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ((ThreadPoolExecutor) executor).setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        if (config.extractMetaFeatures()) {
            extractMetaFeatures(executor, instancesList);
        }
        if (config.evaluatePerformance()) {
            evaluatePerformance(executor, instancesList);
        }
        executor.shutdown();
    }

    private void extractMetaFeatures(ExecutorService executor, List<Pair<String, Instances>> instances) {
        List<Future<ExtractResult>> futureExtractResults = instances.stream()
                .map(p -> config.getExtractors().stream()
                            .map(e -> executor.submit(new ExtractTask(p.first, p.second, e)))
                            .collect(Collectors.toList()))
                .reduce(new ArrayList<>(), (acc, a) -> {
                    acc.addAll(a);
                    return acc;
                });
        for (Future<ExtractResult> future : futureExtractResults) {
            try {
                ExtractResult result = future.get();
                File directory = new File(META_FEATURES_DIRECTORY, result.getDatasetName());
                directory.mkdirs();
                try (PrintWriter writer = new PrintWriter(new File(directory, result.getMetaFeatureName() + ".json"))) {
                    writer.print(result.toJSON().toString(4));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void evaluatePerformance(ExecutorService executor, List<Pair<String, Instances>> instancesList) {
        List<Future<PerformanceResult>> futurePerformanceResults = new ArrayList<>();
        for (Pair<String, Instances> p : instancesList) {
            Instances instances = new Instances(p.second);
            for (int i = 0; i < ROUNDS; i++) {
                instances.randomize(random);
                for (int j = 0; j < FOLDS; j++) {
                    int testNumber = i * FOLDS + j;
                    Instances train = instances.trainCV(FOLDS, j);
                    Instances test = instances.testCV(FOLDS, j);
                    config.getClassifiers().stream()
                            .forEach(classifier -> config.getAlgorithms().stream()
                                    .map(alg -> executor.submit(new PerformanceTask(p.first, testNumber, train, test, alg, classifier)))
                                    .forEach(futurePerformanceResults::add));

                }
            }
        }
        for (Future<PerformanceResult> future : futurePerformanceResults) {
            try {
                PerformanceResult result = future.get();
                File directory = new File(PathUtils.createPath(PERFORMANCE_DIRECTORY,
                        result.classifierName,
                        result.dataSetName,
                        result.algorithmName));
                directory.mkdirs();
                String fileName = PathUtils.createName(result.classifierName, result.dataSetName, result.algorithmName, String.valueOf(result.testNumber));
                try (PrintWriter writer = new PrintWriter(new File(directory, fileName + ".json"))) {
                    writer.print(result.toJSON().toString(4));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static RecommenderSystemBuilder createFromConfig(String configFilename) throws Exception {
        return new RecommenderSystemBuilder(new BuildConfig(configFilename));
    }

    private static final String CONFIG_FILE_NAME = "config.json";

    public static void main(String[] args) throws Exception {
        RecommenderSystemBuilder builder = RecommenderSystemBuilder.createFromConfig(CONFIG_FILE_NAME);
        builder.build();
    }
}
