package com.ifmo.recommendersystem;

import org.json.JSONArray;
import org.json.JSONObject;
import weka.core.Instances;
import weka.core.matrix.Matrix;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.ifmo.recommendersystem.JSONUtils.*;

public class RecommenderSystem {

    private static final int RECOMMEND_RESULT_SIZE = 1;
    private static final int NEAREST_DATA_SET_NUMBER = 3;
    private static final double THRESHOLD = 0.91;

    private final Matrix earrMatrix;
    private final List<String> algorithms;
    private final List<DataSet> dataSets;

    public RecommenderSystem(Matrix earrMatrix, List<String> algorithms, List<DataSet> dataSets) {
        this.earrMatrix = earrMatrix;
        this.algorithms = algorithms;
        this.dataSets = dataSets;
    }

    public List<String> recommend(Instances dataSet) {
        MetaFeatures metaFeatures = MetaFeatures.extractMetaFeature(dataSet);
        List<Integer> indexes = new ArrayList<>(dataSets.size());
        for (int i = 0; i < dataSets.size(); i++) {
            indexes.add(i);
        }
        return recommendInternal(indexes, metaFeatures, RECOMMEND_RESULT_SIZE);
    }

    private List<String> recommendInternal(List<Integer> dataSetIndexes, MetaFeatures metaFeatures, int expectedResultSize) {
        List<MetaFeatures> metaFeaturesList = dataSetIndexes.stream().map(i -> dataSets.get(i).getMetaFeatures()).collect(Collectors.toList());
        double[] dist = dist(metaFeaturesList, metaFeatures);
        int nearestDataSetNumber = Math.min(NEAREST_DATA_SET_NUMBER, metaFeaturesList.size());
        Integer[] indexes = new Integer[metaFeaturesList.size()];
        Arrays.setAll(indexes, i -> i);
        Arrays.sort(indexes, (o1, o2) -> Double.compare(dist[o1], dist[o2]));

        double[] inverseDistances = new double[nearestDataSetNumber];
        for (int i = 0; i < nearestDataSetNumber; i++) {
            inverseDistances[i] = 1 / dist[indexes[i]];
        }
        double inverseDistanceSum = Arrays.stream(inverseDistances).sum();
        double[] earrCoef = new double[algorithms.size()];
        for (int i = 0; i < algorithms.size(); i++) {
            for (int k = 0; k < nearestDataSetNumber; k++) {
                int j = dataSetIndexes.get(indexes[k]);
                earrCoef[i] += inverseDistances[k] * earrMatrix.get(i, j);
            }
            earrCoef[i] /= inverseDistanceSum;
        }
        indexes = new Integer[algorithms.size()];
        Arrays.setAll(indexes, i -> i);
        Arrays.sort(indexes, (o1, o2) -> Double.compare(earrCoef[o2], earrCoef[o1]));
        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(expectedResultSize, algorithms.size()); i++) {
            result.add(algorithms.get(indexes[i]));
        }
        return result;
    }

    public JSONObject evaluate() {
        JSONObject result = new JSONObject();
        double meanHitRatio = 0;
        double meanRPR = 0;
        List<Integer> indexes = new ArrayList<>(dataSets.size());
        for (int i = 0; i < dataSets.size(); i++) {
            indexes.add(i);
        }
        JSONArray separateResult = new JSONArray();
        for (int dataIndex = 0; dataIndex < dataSets.size(); dataIndex++) {
            double optEarr = -Double.MAX_VALUE;
            double worstEarr = Double.MAX_VALUE;
            String optAlgorithm = null;
            String worstAlgorithm = null;
            for (int algIndex = 0; algIndex < algorithms.size(); algIndex++) {
                if (earrMatrix.get(algIndex, dataIndex) > optEarr) {
                    optEarr = earrMatrix.get(algIndex, dataIndex);
                    optAlgorithm = algorithms.get(algIndex);
                }
                if (earrMatrix.get(algIndex, dataIndex) < worstEarr) {
                    worstEarr = earrMatrix.get(algIndex, dataIndex);
                    worstAlgorithm = algorithms.get(algIndex);
                }
            }
            List<String> optSet = new ArrayList<>();
            for (int algIndex = 0; algIndex < algorithms.size(); algIndex++) {
                if (earrMatrix.get(algIndex, dataIndex) / optEarr >= THRESHOLD) {
                    optSet.add(algorithms.get(algIndex));
                }
            }
            indexes.remove(Integer.valueOf(dataIndex));
            String recAlgorithm = recommendInternal(indexes, dataSets.get(dataIndex).getMetaFeatures(), 1).get(0);
            int hitRatio = 0;
            if (optSet.contains(recAlgorithm)) {
                hitRatio = 1;
            }
            int recAlgIndex = algorithms.indexOf(recAlgorithm);
            double rpr = earrMatrix.get(recAlgIndex, dataIndex) / optEarr;
            double worstRpr = worstEarr / optEarr;
            meanHitRatio += hitRatio;
            meanRPR += rpr;
            JSONObject resultObject = new JSONObject();
            resultObject.put(CLASS_NAME, dataSets.get(dataIndex).getName())
                    .put(RECOMMENDED_ALGORITHM, recAlgorithm)
                    .put(OPT_ALGORITHM, optAlgorithm)
                    .put(OPT_ALGORITHM_SET, new JSONArray(optSet))
                    .put(HIT_RATIO, hitRatio)
                    .put(RPR, rpr)
                    .put(WORST_RPR, worstRpr)
                    .put(WORST_ALGORITHM, worstAlgorithm);
            separateResult.put(resultObject);
            indexes.add(dataIndex);
        }
        meanHitRatio /= dataSets.size();
        meanRPR /= dataSets.size();
        result.put(SEPARATE_RESULT, separateResult);
        result.put(MEAN_HIT_RATIO, meanHitRatio);
        result.put(MEAN_RPR, meanRPR);
        return result;
    }

    private double[] dist(List<MetaFeatures> metaFeaturesList, MetaFeatures metaFeatures) {
        List<MetaFeatures> normalizedMetaFeaturesList = normalizeMetaFeatures(metaFeaturesList, metaFeatures);
        metaFeatures = normalizedMetaFeaturesList.remove(normalizedMetaFeaturesList.size() - 1);
        double[] dist = new double[normalizedMetaFeaturesList.size()];
        for (int i = 0; i < normalizedMetaFeaturesList.size(); i++) {
            MetaFeatures otherMetaFeatures = normalizedMetaFeaturesList.get(i);
            for (int j = 0; j < metaFeatures.numAttributes(); j++) {
                dist[i] += Math.abs(metaFeatures.value(j) - otherMetaFeatures.value(j));
            }
        }
        return dist;
    }

    private List<MetaFeatures> normalizeMetaFeatures(List<MetaFeatures> metaFeaturesList, MetaFeatures metaFeatures) {
        double[] minValues = new double[MetaFeatures.META_FEATURE_NUMBER];
        double[] maxValues = new double[MetaFeatures.META_FEATURE_NUMBER];
        Arrays.setAll(minValues, metaFeatures::value);
        Arrays.setAll(maxValues, metaFeatures::value);
        for (MetaFeatures mf : metaFeaturesList) {
            for (int i = 0; i < MetaFeatures.META_FEATURE_NUMBER; i++) {
                minValues[i] = Math.min(minValues[i], mf.value(i));
                maxValues[i] = Math.max(maxValues[i], mf.value(i));
            }
        }
        List<MetaFeatures> normalizedMetaFeatures = new ArrayList<>(metaFeaturesList.size() + 1);
        normalizedMetaFeatures.addAll(metaFeaturesList.stream().map(mf -> normalizeMetaFeature(mf, minValues, maxValues)).collect(Collectors.toList()));
        normalizedMetaFeatures.add(normalizeMetaFeature(metaFeatures, minValues, maxValues));
        return normalizedMetaFeatures;
    }

    private MetaFeatures normalizeMetaFeature(MetaFeatures metaFeatures, double[] minValues, double[] maxValues) {
        double[] values = new double[MetaFeatures.META_FEATURE_NUMBER];
        for (int i = 0; i < MetaFeatures.META_FEATURE_NUMBER; i++) {
            double tmp = maxValues[i] - minValues[i];
            values[i] = tmp == 0 ? 0 : (metaFeatures.value(i) - minValues[i]) / tmp;
        }
        return new MetaFeatures(values);
    }

    public static RecommenderSystem createFromConfig(String filename) {
        JSONObject jsonObject = JSONUtils.readJSONObject(filename);

        List<String> algorithms = jsonArrayToStringList(jsonObject.getJSONArray(ALGORITHMS));
        List<String> dataSetsName = jsonArrayToStringList(jsonObject.getJSONArray(DATA_SETS));
        String classifierName = jsonObject.getString(CLASSIFIER_NAME);
        String directory = jsonObject.getString(DIRECTORY);
        double alpha = jsonObject.getDouble(ALPHA);
        double betta = jsonObject.getDouble(BETTA);

        Matrix matrix = new Matrix(algorithms.size(), dataSetsName.size());
        List<DataSet> dataSets = new ArrayList<>();

        String metaFeatureDirectory = Utils.createPath(directory, ExtractTask.META_FEATURES_DIRECTORY);
        String performanceDirectory = Utils.createPath(directory, PerformanceTask.PERFORMANCE_DIRECTORY, classifierName);

        double[] accuracy = new double[algorithms.size()];
        double[] attributeNumber = new double[algorithms.size()];
        double[] runtime = new double[algorithms.size()];
        for (int j = 0; j < dataSetsName.size(); j++) {
            String dataSetName = dataSetsName.get(j);
            String dataSetPerformanceDir = Utils.createPath(performanceDirectory, dataSetName);
            dataSets.add(DataSet.JSON_CREATOR.fromJSON(JSONUtils.readJSONObject(Utils.createPath(metaFeatureDirectory, dataSetName) + ".json")));

            for (int i = 0; i < algorithms.size(); i++) {
                String algorithmName = algorithms.get(i);
                String resultFilename = Utils.createName(classifierName, dataSetName, algorithmName) + ".json";
                String resultFilePath = Utils.createPath(dataSetPerformanceDir, algorithmName, resultFilename);
                PerformanceResult performanceResult = PerformanceResult.JSON_CREATOR.fromJSON(JSONUtils.readJSONObject(resultFilePath));
                accuracy[i] = performanceResult.meanAccuracy;
                attributeNumber[i] = performanceResult.meanAttributeNumber;
                runtime[i] = performanceResult.meanRuntime;
            }

            double[] earrCoef = calculateEARRCoefs(alpha, betta, accuracy, attributeNumber, runtime);
            for (int i = 0; i < earrCoef.length; i++) {
                matrix.set(i, j, earrCoef[i]);
            }
        }

        return new RecommenderSystem(matrix, algorithms, dataSets);
    }

    private static double[] calculateEARRCoefs(double alpha, double betta, double[] accuracy, double[] attributeNumber, double[] runtime) {
        if (alpha < 0 || betta < 0) {
            throw new IllegalArgumentException("alpha must be >= 0 && betta must be >= 0");
        }
        if (accuracy == null || runtime == null || attributeNumber == null) {
            throw new IllegalArgumentException("arguments must be not null");
        }
        if (accuracy.length != runtime.length || runtime.length != attributeNumber.length) {
            throw new IllegalArgumentException("arguments must have same length");
        }
        int len = accuracy.length;
        double[] eaarCoefs = new double[len];
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                if (i != j) {
                    eaarCoefs[i] += (accuracy[i] / accuracy[j]) /
                            (1 + alpha * Math.log(runtime[i] / runtime[j]) + betta * Math.log(attributeNumber[i] / attributeNumber[j]));
                }
            }
            eaarCoefs[i] /= len - 1;
        }
        return eaarCoefs;
    }

    private static final String EVALUATION_CONFIG = "evaluationConfig.json";
    private static final String EVALUATION_RESULT_FILE_NAME = "evaluationResult.json";

    public static void main(String[] args) throws IOException {
        RecommenderSystem system = createFromConfig(EVALUATION_CONFIG);
        JSONObject result = system.evaluate();
        try (PrintWriter writer = new PrintWriter(EVALUATION_RESULT_FILE_NAME)) {
            writer.println(result.toString(4));
        }
    }
}
