package com.ifmo.recommendersystem;

import com.ifmo.recommendersystem.metafeatures.MetaFeatureExtractor;
import com.ifmo.recommendersystem.utils.Pair;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Created by warrior on 06.05.15.
 */
public class RecommenderSystem {

    private static final int RECOMMEND_RESULT_SIZE = 1;
    private static final int NEAREST_DATA_SET_NUMBER = 3;

    private final double[][] earrMatrix; // dataset x algorithm
    private final List<FSSAlgorithm> algorithms;
    private final Instances metaFeaturesList;

    public RecommenderSystem(double[][] earrMatrix, Instances metaFeaturesList, List<FSSAlgorithm> algorithms) {
        this.earrMatrix = earrMatrix;
        this.algorithms = algorithms;
        this.metaFeaturesList = metaFeaturesList;
    }

    public List<FSSAlgorithm> recommend(Instances dataSet, List<MetaFeatureExtractor> extractors) {
        double[] values = extractors.stream()
                .mapToDouble(e -> e.extract(dataSet).getValue())
                .toArray();
        Instance instance = new DenseInstance(1, values);
        return recommend(instance);
    }

    public List<FSSAlgorithm> recommend(Instance metaFeatures) {
        double[] dist = dist(metaFeaturesList, metaFeatures);
        int nearestDataSetNumber = Math.min(NEAREST_DATA_SET_NUMBER, metaFeaturesList.size());
        int[] sortedIndexes = IntStream.range(0, metaFeaturesList.size())
                .boxed()
                .sorted((o1, o2) -> Double.compare(dist[o1], dist[o2]))
                .limit(nearestDataSetNumber)
                .mapToInt(i -> i)
                .toArray();
        double[] inverseDistances = IntStream.of(sortedIndexes)
                .mapToDouble(i -> 1 / dist[i])
                .toArray();
        double inverseDistanceSum = DoubleStream.of(inverseDistances).sum();

        double[] earrCoef = new double[algorithms.size()];
        for (int i = 0; i < algorithms.size(); i++) {
            for (int k = 0; k < nearestDataSetNumber; k++) {
                int j = sortedIndexes[k]; // dataset index
                earrCoef[i] += inverseDistances[k] * earrMatrix[j][i];
            }
            earrCoef[i] /= inverseDistanceSum;
        }
        return IntStream.range(0, algorithms.size())
                .boxed()
                .sorted((o1, o2) -> Double.compare(earrCoef[o2], earrCoef[o1]))
                .map(algorithms::get)
                .limit(Math.min(RECOMMEND_RESULT_SIZE, algorithms.size()))
                .collect(Collectors.toList());
    }

    private static double[] dist(Instances metaFeaturesList, Instance metaFeatures) {
        Pair<Instances, Instance> normalized = normalizeMetaFeatures(metaFeaturesList, metaFeatures);
        Instances normalizedMetaFeaturesList = normalized.first;
        Instance normalizedMetaFeatures = normalized.second;
        return normalizedMetaFeaturesList.stream()
                .mapToDouble(m -> dist(m, normalizedMetaFeatures))
                .toArray();
    }

    private static double dist(Instance first, Instance second) {
        if (first.numAttributes() != second.numAttributes()) {
            throw new IllegalArgumentException("first.numAttributes() != second.numAttributes()");
        }
        double dist = 0;
        for (int i = 0; i < first.numAttributes(); i++) {
            dist += Math.abs(first.value(i) - second.value(i));
        }
        return dist;
    }

    private static Pair<Instances, Instance> normalizeMetaFeatures(Instances metaFeaturesList, Instance metaFeatures) {
        double[] minValues = metaFeatures.toDoubleArray();
        double[] maxValues = metaFeatures.toDoubleArray();
        for (Instance instance : metaFeaturesList) {
            for (int i = 0; i < instance.numAttributes(); i++) {
                minValues[i] = Math.min(minValues[i], instance.value(i));
                maxValues[i] = Math.max(maxValues[i], instance.value(i));
            }
        }
        Instances normalizedMetaFeaturesList = new Instances(metaFeaturesList, 0, 0);
        for (Instance instance : metaFeaturesList) {
            normalizedMetaFeaturesList.add(normalizeMetaFeature(instance, minValues, maxValues));
        }
        Instance normalizedMetaFeatures = normalizeMetaFeature(metaFeatures, minValues, maxValues);
        return Pair.of(normalizedMetaFeaturesList, normalizedMetaFeatures);
    }

    private static Instance normalizeMetaFeature(Instance metaFeatures, double[] minValues, double[] maxValues) {
        double[] values = metaFeatures.toDoubleArray();
        double[] normalizedValues = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            normalizedValues[i] = (values[i] - minValues[i]) / (maxValues[i] - minValues[i]);
        }
        return new DenseInstance(metaFeatures.weight(), normalizedValues);
    }
}
