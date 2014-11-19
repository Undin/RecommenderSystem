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
import java.util.concurrent.ThreadPoolExecutor;

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
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ((ThreadPoolExecutor)executor).setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
    }

    public void build() {
        long start = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch((algorithms.size() + 1) * dataSets.size());
        for (String location : dataSets) {
            ExtractTask extractTask = new ExtractTask(location);
            extractTask.setLatch(latch);
            executor.submit(extractTask);
            for (FSSAlgorithm algorithm : algorithms) {
                PerformanceTask performanceTask = new PerformanceTask(location, algorithm, classifier);
                performanceTask.setLatch(latch);
                executor.submit(performanceTask);
            }
        }
        try {
            latch.await();
            executor.shutdown();
            long end = System.currentTimeMillis();
            System.out.println("FINISH. time = " + (end - start));
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
