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
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class RecommenderSystemBuilder {

    public static final String META_FEATURES_DIRECTORY = "results/metaFeatures";
    public static final String AVERAGE_PERFORMANCE_DIRECTORY = "results/performanceAverage";
    public static final String PERFORMANCE_DIRECTORY = "results/performance";

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
        ExecutorService executor = Executors.newFixedThreadPool(config.getParallelism());
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
        List<Future<List<PerformanceResult>>> futurePerformanceResults = new ArrayList<>();
        for (Pair<String, Instances> p : instancesList) {
            Instances instances = new Instances(p.second);
            for (int i = 0; i < ROUNDS; i++) {
                instances.randomize(random);
                for (int j = 0; j < FOLDS; j++) {
                    int testNumber = i * FOLDS + j;
                    Instances train = instances.trainCV(FOLDS, j);
                    Instances test = instances.testCV(FOLDS, j);
                    config.getAlgorithms().stream()
                                    .map(alg -> executor.submit(new PerformanceTask(config.getClassifiers(), alg, p.first, train, test, testNumber)))
                            .forEach(futurePerformanceResults::add);
                }
            }
            printPerformanceResults(futurePerformanceResults);
        }
    }

    private void printPerformanceResults(List<Future<List<PerformanceResult>>> futurePerformanceResults) {
        if (config.isAverageResult()) {
            Map<Pair<String, String>, List<PerformanceResult>> separatedResults = new HashMap<>();
            for (Future<List<PerformanceResult>> future : futurePerformanceResults) {
                try {
                    List<PerformanceResult> results = future.get();
                    for (PerformanceResult result : results) {
                        List<PerformanceResult> mapList = separatedResults.get(Pair.of(result.classifierName, result.algorithmName));
                        if (mapList == null) {
                            mapList = new ArrayList<>();
                            separatedResults.put(Pair.of(result.classifierName, result.algorithmName), mapList);
                        }
                        mapList.add(result);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            for (List<PerformanceResult> results : separatedResults.values()) {
                PerformanceResult average = PerformanceResult.average(results);
                printResult(average);
            }
        } else {
            for (Future<List<PerformanceResult>> future : futurePerformanceResults) {
                try {
                    List<PerformanceResult> results = future.get();
                    results.forEach(this::printResult);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        futurePerformanceResults.clear();
    }

    private void printResult(PerformanceResult result) {
        String perfirmanceDirectory = config.isAverageResult() ? AVERAGE_PERFORMANCE_DIRECTORY : PERFORMANCE_DIRECTORY;
        String baseFilename = PathUtils.createName(result.classifierName, result.dataSetName, result.algorithmName);
        String filename = config.isAverageResult() ? baseFilename : PathUtils.createName(baseFilename, String.valueOf(result.testNumber));
        File directory = new File(PathUtils.createPath(perfirmanceDirectory,
                result.classifierName,
                result.dataSetName,
                result.algorithmName));
        directory.mkdirs();
        try (PrintWriter writer = new PrintWriter(new File(directory, filename + ".json"))) {
            writer.print(result.toJSON().toString(4));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
