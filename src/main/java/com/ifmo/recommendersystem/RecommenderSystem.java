package com.ifmo.recommendersystem;

import org.json.JSONObject;
import weka.core.Instances;
import weka.core.matrix.Matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ifmo.recommendersystem.JSONUtils.*;

public class RecommenderSystem {

    private static final int RECOMMEND_RESULT_SIZE = 1;
    private static final int NEAREST_DATA_SET_NUMBER = 3;

    private Matrix EARRMatrix;
    private List<FSSAlgorithm> algorithms;
    private List<DataSet> dataSets;
    private double[] minAttributeValues;
    private double[] maxAttributeValues;

    public RecommenderSystem(Matrix EARRMatrix, List<FSSAlgorithm> algorithms, List<DataSet> dataSets) {
        this.EARRMatrix = EARRMatrix;
        this.algorithms = algorithms;
        this.dataSets = dataSets;
        minAttributeValues = new double[MetaFeatures.META_FEATURE_NUMBER];
        maxAttributeValues = new double[MetaFeatures.META_FEATURE_NUMBER];
        Arrays.fill(minAttributeValues, Double.MAX_VALUE);
        Arrays.fill(maxAttributeValues, -Double.MAX_VALUE);
        for (DataSet dataSet : dataSets) {
            for (int i = 0; i < dataSet.getMetaFeatures().numAttributes(); i++) {
                minAttributeValues[i] = Math.min(minAttributeValues[i], dataSet.getMetaFeatures().value(i));
                maxAttributeValues[i] = Math.max(maxAttributeValues[i], dataSet.getMetaFeatures().value(i));
            }
        }
    }

    public List<FSSAlgorithm> recommend(Instances dataSet) {
        int number = Math.min(NEAREST_DATA_SET_NUMBER, dataSets.size());
        MetaFeatures metaFeatures = MetaFeatures.extractMetaFeature(dataSet);
        double[] dist = dist(metaFeatures);
        Integer[] indexes = new Integer[dataSets.size()];
        Arrays.setAll(indexes, i -> i);
        Arrays.sort(indexes, (o1, o2) -> Double.compare(dist[o1], dist[o2]));
        double[] inverseDistances = Arrays.stream(dist, 0, number).map(d -> 1 / d).toArray();
        double inverseDistanceSum = Arrays.stream(inverseDistances).sum();
        double[] earrCoef = new double[algorithms.size()];
        for (int i = 0; i < algorithms.size(); i++) {
            for (int j : indexes) {
                earrCoef[i] += inverseDistances[j] * EARRMatrix.get(i, j);
            }
            earrCoef[i] /= inverseDistanceSum;
        }
        indexes = new Integer[algorithms.size()];
        Arrays.setAll(indexes, i -> i);
        Arrays.sort(indexes, (o1, o2) -> Double.compare(earrCoef[o2], earrCoef[o1]));
        List<FSSAlgorithm> result = new ArrayList<>();
        for (int i = 0; i < Math.min(RECOMMEND_RESULT_SIZE, algorithms.size()); i++) {
            result.add(algorithms.get(indexes[i]));
        }
        return result;
    }

    private double[] dist(MetaFeatures metaFeatures) {
        List<MetaFeatures> normalizedMetaFeaturesList = normalizeMetaFeatures(metaFeatures);
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

    private List<MetaFeatures> normalizeMetaFeatures(MetaFeatures metaFeatures) {
        double[] minValues = new double[MetaFeatures.META_FEATURE_NUMBER];
        double[] maxValues = new double[MetaFeatures.META_FEATURE_NUMBER];
        for (int i = 0; i < MetaFeatures.META_FEATURE_NUMBER; i++) {
            minValues[i] = Math.min(minAttributeValues[i], metaFeatures.value(i));
            maxValues[i] = Math.max(maxAttributeValues[i], metaFeatures.value(i));
        }
        List<MetaFeatures> normalizedMetaFeatures = new ArrayList<>(dataSets.size() + 1);

        for (DataSet dataSet : dataSets) {
            double[] values = new double[MetaFeatures.META_FEATURE_NUMBER];
            for (int i = 0; i < MetaFeatures.META_FEATURE_NUMBER; i++) {
                values[i] = (dataSet.getMetaFeatures().value(i) - minValues[i]) / (maxValues[i] - minValues[i]);
            }
            normalizedMetaFeatures.add(new MetaFeatures(values));
        }
        double[] values = new double[MetaFeatures.META_FEATURE_NUMBER];
        for (int i = 0; i < MetaFeatures.META_FEATURE_NUMBER; i++) {
            values[i] = (metaFeatures.value(i) - minValues[i]) / (maxValues[i] - minValues[i]);
        }
        normalizedMetaFeatures.add(new MetaFeatures(values));
        return normalizedMetaFeatures;
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
}
