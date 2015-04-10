package com.ifmo.recommendersystem;

import com.ifmo.recommendersystem.tasks.ExtractTask;
import com.ifmo.recommendersystem.tasks.PerformanceResult;
import com.ifmo.recommendersystem.tasks.PerformanceTask;
import com.ifmo.recommendersystem.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import rx.Observable;
import rx.schedulers.Schedulers;
import weka.core.Instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.ifmo.recommendersystem.JSONUtils.*;

public class RecommenderSystemBuilder {

    public static final String META_FEATURES_DIRECTORY = "results/metaFeatures";
    public static final String PERFORMANCE_DIRECTORY = "results/performance";

    public final static int ROUNDS = 5;
    public final static int FOLDS = 10;

    private final List<FSSAlgorithm> algorithms;
    private final List<String> dataSets;
    private final ClassifierWrapper classifier;
    private final MetaFeatures.Set meteFeatureSet;
    private final boolean extractMetaFeatures;
    private final boolean evaluatePerformance;

    private final Random random = new Random();

    private RecommenderSystemBuilder(List<FSSAlgorithm> algorithms,
                                     ClassifierWrapper classifier,
                                     List<String> dataSets,
                                     MetaFeatures.Set meteFeatureSet,
                                     boolean extractMetaFeatures,
                                     boolean evaluatePerformance) {
        this.algorithms = algorithms;
        this.dataSets = dataSets;
        this.classifier = classifier;
        this.meteFeatureSet = meteFeatureSet;
        this.extractMetaFeatures = extractMetaFeatures;
        this.evaluatePerformance = evaluatePerformance;
    }

    public void build() {
        List<Pair<String, Instances>> instancesList = new ArrayList<>(dataSets.size());
        for (String path : dataSets) {
            String datasetName = getDatasetName(path);
            try {
                Instances instances = InstancesUtils.createInstances(path, InstancesUtils.REMOVE_STRING_ATTRIBUTES |
                        InstancesUtils.REMOVE_UNINFORMATIVE_ATTRIBUTES);
                instancesList.add(Pair.of(datasetName, instances));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (extractMetaFeatures) {
            extractMetaFeatures(instancesList);
        }
        if (evaluatePerformance) {
            evaluatePerformance(instancesList);
        }
    }

    private void extractMetaFeatures(List<Pair<String, Instances>> instances) {
        List<ExtractTask> extractTasks = instances.stream()
                .map(p -> new ExtractTask(p.first, p.second, meteFeatureSet))
                .collect(Collectors.toList());
        Observable.from(extractTasks)
                .observeOn(Schedulers.computation())
                .map(ExtractTask::call)
                .toBlocking()
                .forEach(dataset -> {
                    File directory = new File(META_FEATURES_DIRECTORY, dataset.getMetaFeatures().getMetaFeatureSet().name());
                    directory.mkdirs();
                    try (PrintWriter writer = new PrintWriter(new File(directory, dataset.getName() + ".json"))) {
                        writer.print(dataset.toJSON().toString(4));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                });

    }

    private void evaluatePerformance(List<Pair<String, Instances>> instancesList) {
        List<PerformanceTask> performanceTasks = new ArrayList<>();
        for (Pair<String, Instances> p : instancesList) {
            Instances instances = new Instances(p.second);
            for (int i = 0; i < ROUNDS; i++) {
                instances.randomize(random);
                for (int j = 0; j < FOLDS; j++) {
                    Instances train = instances.trainCV(FOLDS, j);
                    Instances test = instances.testCV(FOLDS, j);
                    performanceTasks.add(new PerformanceTask(p.first, i * FOLDS + j, train, test, algorithms, classifier));
                }
            }
        }

        Observable.from(performanceTasks)
                .observeOn(Schedulers.computation())
                .map(PerformanceTask::call)
                .toBlocking()
                .forEach(resultList -> {
                    for (PerformanceResult result : resultList) {
                        File directory = new File(Utils.createPath(PERFORMANCE_DIRECTORY,
                                result.classifierName,
                                result.dataSetName,
                                result.algorithmName));
                        directory.mkdirs();
                        String fileName = Utils.createName(result.classifierName, result.dataSetName, result.algorithmName, String.valueOf(result.testNumber));
                        try (PrintWriter writer = new PrintWriter(new File(directory, fileName + ".json"))) {
                            writer.print(result.toJSON().toString(4));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private String getDatasetName(String path) {
        int startIdx = path.lastIndexOf(File.separatorChar);
        int endIdx = path.lastIndexOf('.');
        return path.substring(startIdx + 1, endIdx);
    }

    public static RecommenderSystemBuilder createFromConfig(String configFilename) throws Exception {
        JSONObject jsonObject = JSONUtils.readJSONObject(configFilename);
        boolean extractMetaFeatures = jsonObject.getBoolean(EXTRACT_META_FEATURES);
        boolean evaluatePerformance = jsonObject.getBoolean(EVALUATE_PERFORMANCE);
        String metaFeatureSetName = jsonObject.getString(SET_NAME);
        ClassifierWrapper classifier = ClassifierWrapper.JSON_CREATOR.fromJSON(jsonObject.getJSONObject(CLASSIFIER));
        List<FSSAlgorithm> algorithms = jsonArrayToObjectList(jsonObject.getJSONArray(ALGORITHMS), FSSAlgorithm.JSON_CREATOR);

        List<String> dataSetsLocation = createInstances(jsonObject.getJSONObject(DATA_SETS));
        return new RecommenderSystemBuilder(algorithms,
                classifier,
                dataSetsLocation,
                MetaFeatures.Set.valueOf(metaFeatureSetName),
                extractMetaFeatures,
                evaluatePerformance);
    }

    private static List<String> createInstances(JSONObject jsonObject) throws Exception {
        String directory = jsonObject.getString(JSONUtils.DIRECTORY);
        JSONArray jsonArray = jsonObject.getJSONArray(JSONUtils.LIST);
        List<String> dataSets = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            dataSets.add(directory + File.separator + jsonArray.getString(i));
        }
        return dataSets;
    }

    private static final String CONFIG_FILE_NAME = "config.json";

    public static void main(String[] args) throws Exception {
        RecommenderSystemBuilder builder = RecommenderSystemBuilder.createFromConfig(CONFIG_FILE_NAME);
        builder.build();
    }
}
