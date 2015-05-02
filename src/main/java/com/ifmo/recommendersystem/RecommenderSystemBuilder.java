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
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RecommenderSystemBuilder {

    public static final String META_FEATURES_DIRECTORY = "results/test_metaFeatures";
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
        ForkJoinPool pool = new ForkJoinPool(config.getParallelism(),
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                (t, e) -> e.printStackTrace(),
                false);
        pool.invoke(new RecursiveAction() {
            @Override
            protected void compute() {
                if (config.extractMetaFeatures()) {
                    extractMetaFeatures(instancesList);
                }
                if (config.evaluatePerformance()) {
                    evaluatePerformance(instancesList);
                }
            }
        });
    }

    private void extractMetaFeatures(List<Pair<String, Instances>> instances) {
        instances.stream()
                .map(p -> config.getExtractors().stream()
                        .map(e -> new ExtractTask(p.first, p.second, e).fork())
                        .collect(Collectors.toList()))
                .reduce(new ArrayList<>(), (acc, a) -> {
                    acc.addAll(a);
                    return acc;
                })
                .stream()
                .forEach(task -> {
                    ExtractResult result = task.join();
                    File directory = new File(META_FEATURES_DIRECTORY, result.getDatasetName());
                    directory.mkdirs();
                    try (PrintWriter writer = new PrintWriter(new File(directory, result.getMetaFeatureName() + ".json"))) {
                        writer.print(result.toJSON().toString(4));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void evaluatePerformance(List<Pair<String, Instances>> instancesList) {
        instancesList.stream()
                .map(p -> {
                    Instances instances = new Instances(p.second);
                    List<ForkJoinTask<List<PerformanceResult>>> tasks = new ArrayList<>();
                    IntStream.range(0, ROUNDS)
                            .forEach(i -> {
                                instances.randomize(random);
                                IntStream.range(0, FOLDS)
                                        .forEach(j -> {
                                            int testNumber = i * FOLDS + j;
                                            Instances train = instances.trainCV(FOLDS, j);
                                            Instances test = instances.testCV(FOLDS, j);
                                            config.getAlgorithms().stream()
                                                    .map(alg -> new PerformanceTask(config.getClassifiers(),
                                                            alg,
                                                            p.first,
                                                            train,
                                                            test,
                                                            testNumber).fork())
                                                    .forEach(tasks::add);
                                        });
                                    });
                    return tasks;
                })
                .reduce(new ArrayList<>(), (acc, a) -> {
                    acc.addAll(a);
                    return acc;
                })
                .stream()
                .forEach(task -> task.join()
                        .stream()
                        .forEach(result -> {
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
                                }
                        ));
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
