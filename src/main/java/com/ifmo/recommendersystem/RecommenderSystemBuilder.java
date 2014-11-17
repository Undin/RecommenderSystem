package com.ifmo.recommendersystem;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import weka.core.Instances;
import weka.core.matrix.Matrix;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ifmo.recommendersystem.JSONUtils.*;

public class RecommenderSystemBuilder {

    private final double alpha;
    private final double betta;

    private final List<FSSAlgorithm> algorithms;
    private final List<Instances> dataSets;
    private final ClassifierWrapper classifier;

    private final ExecutorService executor;
    private final DataSet[] dataSetArray;
    private final Matrix earrMatrix;

    private JSONObject result;

    private RecommenderSystemBuilder(double alpha, double betta, List<FSSAlgorithm> algorithms, ClassifierWrapper classifier, List<Instances> dataSets) {
        this.alpha = alpha;
        this.betta = betta;
        this.algorithms = algorithms;
        this.dataSets = dataSets;
        this.classifier = classifier;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.dataSetArray = new DataSet[dataSets.size()];
        this.earrMatrix = new Matrix(algorithms.size(), dataSets.size());
    }

    public void build() {
        CountDownLatch latch = new CountDownLatch(dataSets.size() * 2);
        for (int i = 0; i < dataSets.size(); i++) {
            executor.submit(new PerformanceEvaluationTask(latch, alpha, betta, i, dataSets.get(i), algorithms, classifier, earrMatrix));
            executor.submit(new ExtractMetaFeaturesTask(latch, dataSets.get(i), i, dataSetArray));
        }
        try {
            latch.await();
            executor.shutdown();
            System.out.println("end!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getResultAsJSON() {
        if (result == null) {
            result = new JSONObject().
                    put(JSONUtils.ALPHA, alpha).
                    put(JSONUtils.BETTA, betta).
                    put(JSONUtils.CLASSIFIER, classifier.toJSON()).
                    put(JSONUtils.ALGORITHMS, collectionToJSONArray(algorithms)).
                    put(JSONUtils.DATA_SETS, collectionToJSONArray(Arrays.asList(dataSetArray))).
                    put(JSONUtils.EARR_MATRIX, matrixToJSONArray(earrMatrix));
        }
        return result;
    }

    public RecommenderSystem getResultAsRecommenderSystem() {
        return new RecommenderSystem(earrMatrix, algorithms, Arrays.asList(dataSetArray));
    }

    private abstract static class CountDownTask implements Runnable {

        private final CountDownLatch latch;

        public CountDownTask(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void run() {
            internalRun();
            latch.countDown();
        }

        protected abstract void internalRun();
    }

    private static class ExtractMetaFeaturesTask extends CountDownTask {

        private final Instances dataSet;
        private final int dataSetIndex;
        private final DataSet[] dataSets;

        private ExtractMetaFeaturesTask(CountDownLatch latch, Instances dataSet, int dataSetIndex, DataSet[] dataSets) {
            super(latch);
            System.out.println("new ExtractMetaFeaturesTask");
            this.dataSet = dataSet;
            this.dataSetIndex = dataSetIndex;
            this.dataSets = dataSets;
        }

        @Override
        protected void internalRun() {
            System.out.println("start ExtractMetaFeaturesTask");
            dataSets[dataSetIndex] = new DataSet(dataSet);
            System.out.println("end ExtractMetaFeaturesTask");
        }
    }

    private static class PerformanceEvaluationTask extends CountDownTask {

        private final static int ROUNDS = 5;
        private final static int FOLDS = 10;

        private final double alpha;
        private final double betta;
        private final int dataSetNumber;
        private final Instances dataSet;
        private final int algorithmNumber;
        private final List<FSSAlgorithm> algorithms;
        private final ClassifierWrapper classifier;

        private final Matrix EARRMatrix;

        private final Random random = new Random();

        private PerformanceEvaluationTask(CountDownLatch latch,
                                          double alpha, double betta,
                                          int dataSetNumber, Instances dataSet,
                                          List<FSSAlgorithm> algorithms,
                                          ClassifierWrapper classifier, Matrix EARRMatrix) {
            super(latch);
            System.out.println("new PerformanceEvaluationTask");
            this.alpha = alpha;
            this.betta = betta;
            this.dataSetNumber = dataSetNumber;
            this.dataSet = dataSet;
            this.algorithmNumber = algorithms.size();
            this.algorithms = algorithms;
            this.classifier = classifier;
            this.EARRMatrix = EARRMatrix;
        }

        @Override
        protected void internalRun() {
            System.out.println("start PerformanceEvaluationTask");
            double[] earrCoefs = new double[algorithmNumber];
            double[] runtime = new double[algorithmNumber];
            double[] accuracy = new double[algorithmNumber];
            double[] number = new double[algorithmNumber];
            for (int i = 0; i < ROUNDS; i++) {
                dataSet.randomize(random);
                for (int j = 0; j < FOLDS; j++) {
                    for (int k = 0; k < algorithmNumber; k++) {
                        FSSAlgorithm.Result result = algorithms.get(k).subsetSelection(dataSet);
                        runtime[k] = result.runtime;
                        number[k] = result.instances.numAttributes();
                        Instances train = result.instances.trainCV(FOLDS, j);
                        Instances test = result.instances.testCV(FOLDS, j);
                        accuracy[k] = classifier.getAccuracy(train, test);
                    }
                }
                double[] currentEARRCoefs = calculateEARRCoefs(alpha, betta, accuracy, runtime, number);
                for (int j = 0; j < algorithmNumber; j++) {
                    earrCoefs[j] += currentEARRCoefs[j];
                }
            }
            for (int i = 0; i < algorithmNumber; i++) {
                EARRMatrix.set(i, dataSetNumber, earrCoefs[i] / (ROUNDS * FOLDS));
            }
            System.out.println("end PerformanceEvaluationTask");
        }
    }

    private static double[] calculateEARRCoefs(double alpha, double betta, double[] accuracy, double[] runtime, double[] number) {
        if (alpha < 0 || betta < 0) {
            throw new IllegalArgumentException("alpha must be >= 0 && betta must be >= 0");
        }
        if (accuracy == null || runtime == null || number == null) {
            throw new IllegalArgumentException("arguments must be not null");
        }
        if (accuracy.length != runtime.length || runtime.length != number.length) {
            throw new IllegalArgumentException("arguments must have same length");
        }
        int len = accuracy.length;
        double[] eaarCoefs = new double[len];
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                if (i != j) {
                    eaarCoefs[i] += (accuracy[i] / accuracy[j]) /
                            (1 + alpha * Math.log(runtime[i] / runtime[j]) + betta * Math.log(number[i] / number[j]));
                }
            }
            eaarCoefs[i] /= len - 1;
        }
        return eaarCoefs;
    }

    public static RecommenderSystemBuilder createFromConfig(String configFilename) throws Exception {
        InputStream inputStream = new FileInputStream(configFilename);
        String config = IOUtils.toString(inputStream);
        JSONObject jsonObject = new JSONObject(config);
        double alpha = jsonObject.optDouble(ALPHA, 0);
        double betta = jsonObject.optDouble(BETTA, 0);
        ClassifierWrapper classifier = ClassifierWrapper.JSON_CREATOR.fromJSON(jsonObject.getJSONObject(CLASSIFIER));
        List<FSSAlgorithm> algorithms = jsonArrayToObjectList(jsonObject.getJSONArray(ALGORITHMS), FSSAlgorithm.JSON_CREATOR);
        List<Instances> dataSets = createInstances(jsonObject.getJSONArray(DATA_SETS));
        return new RecommenderSystemBuilder(alpha, betta, algorithms, classifier, dataSets);
    }

    private static List<Instances> createInstances(JSONArray jsonArray) throws Exception {
        List<Instances> dataSets = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            dataSets.add(InstancesUtils.createInstances(jsonArray.getString(i), true));
        }
        return dataSets;
    }

    private static final String CONFIG_FILE_NAME = "config.json";
    private static final String RESULT_FILE_NAME = "result.json";

    public static void main(String[] args) throws Exception {
        RecommenderSystemBuilder builder = RecommenderSystemBuilder.createFromConfig(CONFIG_FILE_NAME);
        builder.build();
        try (PrintWriter writer = new PrintWriter(RESULT_FILE_NAME)) {
            writer.println(builder.getResultAsJSON().toString(4));
        }
    }
}
