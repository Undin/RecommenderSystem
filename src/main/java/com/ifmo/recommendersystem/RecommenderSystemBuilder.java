package com.ifmo.recommendersystem;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ifmo.recommendersystem.JSONUtils.*;

public class RecommenderSystemBuilder {

    private final List<FSSAlgorithm> algorithms;
    private final List<String> dataSets;
    private final ClassifierWrapper classifier;

    private final ExecutorService executor;

    private RecommenderSystemBuilder(List<FSSAlgorithm> algorithms, ClassifierWrapper classifier, List<String> dataSets) {
        this.algorithms = algorithms;
        this.dataSets = dataSets;
        this.classifier = classifier;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void build() {
        CountDownLatch latch = new CountDownLatch(dataSets.size() * 2);
        for (String location : dataSets) {
            ExtractTask extractTask = new ExtractTask(location);
            PerformanceTask performanceTask = new PerformanceTask(location, algorithms, classifier);
            extractTask.setLatch(latch);
            performanceTask.setLatch(latch);
            executor.submit(extractTask);
            executor.submit(performanceTask);
        }
        try {
            latch.await();
            executor.shutdown();
            System.out.println("end!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static RecommenderSystemBuilder createFromConfig(String configFilename) throws Exception {
        InputStream inputStream = new FileInputStream(configFilename);
        String config = IOUtils.toString(inputStream);
        JSONObject jsonObject = new JSONObject(config);
        ClassifierWrapper classifier = ClassifierWrapper.JSON_CREATOR.fromJSON(jsonObject.getJSONObject(CLASSIFIER));
        List<FSSAlgorithm> algorithms = jsonArrayToObjectList(jsonObject.getJSONArray(ALGORITHMS), FSSAlgorithm.JSON_CREATOR);

        List<String> dataSetsLocation = createInstances(jsonObject.getJSONObject(DATA_SETS));
        return new RecommenderSystemBuilder(algorithms, classifier, dataSetsLocation);
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
