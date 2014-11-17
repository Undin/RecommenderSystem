package com.ifmo.recommendersystem;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import weka.core.Instances;
import weka.core.matrix.Matrix;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.ifmo.recommendersystem.JSONUtils.*;

public class RecommenderSystem {

    private static final String DATA_SET_NAME = "dataSetName";
    private static final String OPT_ALGORITHM = "optAlgorithm";
    private static final String OPT_ALGORITHM_SET = "optAlgorithmSet";
    private static final String HIT_RATIO = "hitRatio";
    private static final String RPR = "RPR";
    private static final String MEAN_HIT_RATIO = "meanHitRatio";
    private static final String MEAN_RPR = "meanRPR";
    private static final String SEPARATE_RESULT = "separateResult";

    private static final String DATA_FILE_NAME = "result.json";
    private static final String EVALUATE_RESULT_FILE_NAME = "evaluateResult.json";

    private static final int RECOMMEND_RESULT_SIZE = 1;
    private static final int NEAREST_DATA_SET_NUMBER = 3;
    private static final double THRESHOLD = 0.9;

    private final Matrix earrMatrix;
    private final List<FSSAlgorithm> algorithms;
    private final List<DataSet> dataSets;

    public RecommenderSystem(Matrix earrMatrix, List<FSSAlgorithm> algorithms, List<DataSet> dataSets) {
        this.earrMatrix = earrMatrix;
        this.algorithms = algorithms;
        this.dataSets = dataSets;
    }

    public List<FSSAlgorithm> recommend(Instances dataSet) {
        MetaFeatures metaFeatures = MetaFeatures.extractMetaFeature(dataSet);
        List<Integer> indexes = new ArrayList<>(dataSets.size());
        for (int i = 0; i < dataSets.size(); i++) {
            indexes.add(i);
        }
        return recommendInternal(indexes, metaFeatures, RECOMMEND_RESULT_SIZE);
    }

    private List<FSSAlgorithm> recommendInternal(List<Integer> dataSetIndexes, MetaFeatures metaFeatures, int expectedResultSize) {
        List<MetaFeatures> metaFeaturesList = dataSetIndexes.stream().map(i -> dataSets.get(i).getMetaFeatures()).collect(Collectors.toList());
        double[] dist = dist(metaFeaturesList, metaFeatures);
        int nearestDataSetNumber = Math.min(NEAREST_DATA_SET_NUMBER, metaFeaturesList.size());
        Integer[] indexes = new Integer[metaFeaturesList.size()];
        Arrays.setAll(indexes, i -> i);
        Arrays.sort(indexes, (o1, o2) -> Double.compare(dist[o1], dist[o2]));
        double[] inverseDistances = Arrays.stream(dist, 0, nearestDataSetNumber).map(d -> 1 / d).toArray();
        double inverseDistanceSum = Arrays.stream(inverseDistances).sum();
        double[] earrCoef = new double[algorithms.size()];
        for (int i = 0; i < algorithms.size(); i++) {
            for (int k : indexes) {
                int j = dataSetIndexes.get(k);
                earrCoef[i] += inverseDistances[k] * earrMatrix.get(i, j);
            }
            earrCoef[i] /= inverseDistanceSum;
        }
        indexes = new Integer[algorithms.size()];
        Arrays.setAll(indexes, i -> i);
        Arrays.sort(indexes, (o1, o2) -> Double.compare(earrCoef[o2], earrCoef[o1]));
        List<FSSAlgorithm> result = new ArrayList<>();
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
            FSSAlgorithm optAlgorithm = null;
            for (int algIndex = 0; algIndex < algorithms.size(); algIndex++) {
                if (earrMatrix.get(algIndex, dataIndex) > optEarr) {
                    optEarr = earrMatrix.get(algIndex, dataIndex);
                    optAlgorithm = algorithms.get(algIndex);
                }
            }
            List<FSSAlgorithm> optSet = new ArrayList<>();
            for (int algIndex = 0; algIndex < algorithms.size(); algIndex++) {
                if (earrMatrix.get(algIndex, dataIndex) / optEarr >= THRESHOLD) {
                    optSet.add(algorithms.get(algIndex));
                }
            }
            indexes.remove(Integer.valueOf(dataIndex));
            FSSAlgorithm recAlgorithm = recommendInternal(indexes, dataSets.get(dataIndex).getMetaFeatures(), 1).get(0);
            int hitRatio = 0;
            if (optSet.contains(recAlgorithm)) {
                hitRatio = 1;
            }
            int recAlgIndex = algorithms.indexOf(recAlgorithm);
            double rpr = earrMatrix.get(recAlgIndex, dataIndex) / optEarr;
            meanHitRatio += hitRatio;
            meanRPR += rpr;
            JSONObject resultObject = new JSONObject();
            resultObject.put(DATA_SET_NAME, dataSets.get(dataIndex).getName())
                        .put(OPT_ALGORITHM, optAlgorithm.toJSON())
                        .put(OPT_ALGORITHM_SET, JSONUtils.collectionToJSONArray(optSet))
                        .put(HIT_RATIO, hitRatio)
                        .put(RPR, rpr);
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
            values[i] = (metaFeatures.value(i) - minValues[i]) / (maxValues[i] - minValues[i]);
        }
        return new MetaFeatures(values);
    }

    public static final AbstractJSONCreator<RecommenderSystem> JSON_CREATOR = new AbstractJSONCreator<RecommenderSystem>() {
        @Override
        protected RecommenderSystem throwableFromJSON(JSONObject jsonObject) throws Exception {
            List<FSSAlgorithm> algorithms = jsonArrayToObjectList(jsonObject.getJSONArray(ALGORITHMS), FSSAlgorithm.JSON_CREATOR);
            List<DataSet> dataSets = jsonArrayToObjectList(jsonObject.getJSONArray(DATA_SETS), DataSet.JSON_CREATOR);
            Matrix matrix  = jsonArrayToMatrix(jsonObject.getJSONArray(EARR_MATRIX));
            return new RecommenderSystem(matrix, algorithms, dataSets);
        }
    };

    public static void main(String[] args) throws IOException {
        String str = IOUtils.toString(new FileInputStream(DATA_FILE_NAME));
        RecommenderSystem system = JSON_CREATOR.fromJSON(new JSONObject(str));
        JSONObject result = system.evaluate();
        try (PrintWriter writer = new PrintWriter(EVALUATE_RESULT_FILE_NAME)) {
            writer.println(result.toString(4));
        }
    }
}
