package com.ifmo.recommendersystem;

import com.ifmo.recommendersystem.config.EvaluationConfig;
import com.ifmo.recommendersystem.tasks.ExtractResult;
import com.ifmo.recommendersystem.tasks.MetaFeature;
import com.ifmo.recommendersystem.tasks.PerformanceResult;
import com.ifmo.recommendersystem.utils.Pair;
import com.ifmo.recommendersystem.utils.PathUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import weka.core.Instances;
import weka.core.matrix.Matrix;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.ifmo.recommendersystem.utils.JSONUtils.*;

public class RecommenderSystem {

    private static final int RECOMMEND_RESULT_SIZE = 1;
    private static final int NEAREST_DATA_SET_NUMBER = 3;

    private final EvaluationConfig config;

    private final List<String> algorithms;
    private final List<List<MetaFeature>> metaFeaturesList;
    private final Matrix earrMatrix;

    public RecommenderSystem(EvaluationConfig config, List<List<MetaFeature>> metaFeaturesList, Matrix earrMatrix) {
        this.config = config;
        this.algorithms = config.getAlgorithms()
                .stream()
                .map(FSSAlgorithm::getName)
                .collect(Collectors.toList());
        this.metaFeaturesList = metaFeaturesList;
        this.earrMatrix = earrMatrix;
    }

    public List<String> recommend(Instances dataSet) {
        List<MetaFeature> metaFeatures = config.getExtractors().stream()
                .map(e -> e.extract(dataSet))
                .collect(Collectors.toList());
        ArrayList<Integer> indexes = new ArrayList<>(metaFeaturesList.size());
        for (int i = 0; i < metaFeaturesList.size(); i++) {
            indexes.add(i);
        }
        return recommendInternal(indexes, metaFeatures, RECOMMEND_RESULT_SIZE);
    }

    private List<String> recommendInternal(List<Integer> dataSetIndexes, List<MetaFeature> metaFeatures, int expectedResultSize) {
        List<Double> dist = dist(metaFeaturesList, metaFeatures);
        int nearestDataSetNumber = Math.min(NEAREST_DATA_SET_NUMBER, dataSetIndexes.size());
        Integer[] indexes = dataSetIndexes.stream()
                .sorted((o1, o2) -> Double.compare(dist.get(o1), dist.get(o2)))
                .toArray(Integer[]::new);
        double[] inverseDistances = new double[nearestDataSetNumber];
        for (int i = 0; i < nearestDataSetNumber; i++) {
            inverseDistances[i] = 1 / dist.get(indexes[i]);
        }
        double inverseDistanceSum = Arrays.stream(inverseDistances).sum();
        double[] earrCoef = new double[algorithms.size()];
        for (int i = 0; i < algorithms.size(); i++) {
            for (int k = 0; k < nearestDataSetNumber; k++) {
                int j = indexes[k];
                earrCoef[i] += inverseDistances[k] * earrMatrix.get(i, j);
            }
            earrCoef[i] /= inverseDistanceSum;
        }
        Integer[] algorithmIndexes = new Integer[algorithms.size()];
        Arrays.setAll(algorithmIndexes, i -> i);
        Arrays.sort(algorithmIndexes, (o1, o2) -> Double.compare(earrCoef[o2], earrCoef[o1]));
        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(expectedResultSize, algorithms.size()); i++) {
            result.add(algorithms.get(algorithmIndexes[i]));
        }
        return result;
    }

    public JSONObject evaluate() {
        JSONObject result = new JSONObject();
        double meanRPR = 0;
        List<Integer> indexes = new ArrayList<>(config.getDatasets().size());
        for (int i = 0; i < config.getDatasets().size(); i++) {
            indexes.add(i);
        }
        JSONArray separateResult = new JSONArray();
        for (int dataIndex = 0; dataIndex < config.getDatasets().size(); dataIndex++) {
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
            indexes.remove(Integer.valueOf(dataIndex));
            String recAlgorithm = recommendInternal(indexes, metaFeaturesList.get(dataIndex), 1).get(0);
            int recAlgIndex = algorithms.indexOf(recAlgorithm);
            double rpr = earrMatrix.get(recAlgIndex, dataIndex) / optEarr;
            double worstRpr = worstEarr / optEarr;
            meanRPR += rpr;

            List<Pair<String, Double>> algorithmRpr = new ArrayList<>(algorithms.size());
            for (int algIndex = 0; algIndex < algorithms.size(); algIndex++) {
                String algName = algorithms.get(algIndex);
                double algRpr = earrMatrix.get(algIndex, dataIndex) / optEarr;
                algorithmRpr.add(Pair.of(algName, algRpr));
            }
            Collections.sort(algorithmRpr, (p1, p2) -> Double.compare(p2.second, p1.second));
            JSONArray rprList = new JSONArray();
            algorithmRpr.stream()
                    .map(p -> new JSONObject()
                            .put(ALGORITHM_NAME, p.first)
                            .put(RPR, p.second))
                    .sequential()
                    .forEach(rprList::put);
            JSONObject resultObject = new JSONObject();
            resultObject.put(DATA_SET_NAME, config.getDatasets().get(dataIndex))
                    .put(RECOMMENDED_ALGORITHM, recAlgorithm)
                    .put(OPT_ALGORITHM, optAlgorithm)
                    .put(RPR, rpr)
                    .put(RPR_LIST, rprList)
                    .put(WORST_RPR, worstRpr)
                    .put(WORST_ALGORITHM, worstAlgorithm);
            separateResult.put(resultObject);
            indexes.add(dataIndex);
        }
        meanRPR /= metaFeaturesList.size();
        result.put(SEPARATE_RESULT, separateResult);
        result.put(MEAN_RPR, meanRPR);
        return result;
    }

    private List<Double> dist(List<List<MetaFeature>> metaFeaturesList, List<MetaFeature> metaFeatures) {
        List<List<MetaFeature>> normalizedMetaFeaturesList = normalizeMetaFeatures(metaFeaturesList, metaFeatures);
        List<MetaFeature> normalizedMetaFeatures = normalizedMetaFeaturesList.remove(normalizedMetaFeaturesList.size() - 1);
        return normalizedMetaFeaturesList.stream()
                .map(m -> {
                    double dist = 0;
                    for (int i = 0; i < m.size(); i++) {
                        dist += Math.abs(m.get(i).getValue() - normalizedMetaFeatures.get(i).getValue());
                    }
                    return dist;
                })
                .collect(Collectors.toList());
    }

    private List<List<MetaFeature>> normalizeMetaFeatures(List<List<MetaFeature>> metaFeaturesList, List<MetaFeature> metaFeatures) {
        List<Double> minValues = metaFeatures.stream()
                .map(MetaFeature::getValue)
                .collect(Collectors.toList());
        List<Double> maxValues = new ArrayList<>(minValues);
        for (List<MetaFeature> list : metaFeaturesList) {
            for (int i = 0; i < list.size(); i++) {
                minValues.set(i, Math.min(minValues.get(i), list.get(i).getValue()));
                maxValues.set(i, Math.max(minValues.get(i), list.get(i).getValue()));
            }
        }
        List<List<MetaFeature>> normalizedMetaFeaturesList = metaFeaturesList.stream()
                .map(l -> normalizeMetaFeature(l, minValues, maxValues))
                .collect(Collectors.toList());
        normalizedMetaFeaturesList.add(normalizeMetaFeature(metaFeatures, minValues, maxValues));
        return normalizedMetaFeaturesList;
    }

    private List<MetaFeature> normalizeMetaFeature(List<MetaFeature> metaFeatures, List<Double> minValues, List<Double> maxValues) {
        List<MetaFeature> normalizedMetaFeatures = new ArrayList<>(metaFeatures.size());
        for (int i = 0; i < metaFeatures.size(); i++) {
            MetaFeature metaFeature = metaFeatures.get(i);
            double minValue = minValues.get(i);
            double maxValue = maxValues.get(i);
            double value = metaFeature.getValue();
            MetaFeature normalizedMetaFeature = new MetaFeature(metaFeature.getExtractorClassName(), (value - minValue) / (maxValue - minValue));
            normalizedMetaFeatures.add(normalizedMetaFeature);
        }
        return normalizedMetaFeatures;
    }

    public static RecommenderSystem createFromConfig(String filename) {
        EvaluationConfig config = new EvaluationConfig(filename);

        int algorithmNumber = config.getAlgorithms().size();
        String classifierName = config.getClassifier().getName();

        Matrix matrix = new Matrix(algorithmNumber, config.getDatasets().size());

        double[] f1Measure = new double[algorithmNumber];
        double[] attributeNumber = new double[algorithmNumber];
        double[] runtime = new double[algorithmNumber];
        int testNumber = RecommenderSystemBuilder.FOLDS * RecommenderSystemBuilder.ROUNDS;
        for (int i = 0; i < config.getDatasets().size(); i++) {
            Arrays.fill(f1Measure, 0);
            Arrays.fill(attributeNumber, 0);
            Arrays.fill(runtime, 0);
            String datasetName = config.getDatasets().get(i);
            for (int j = 0; j < algorithmNumber; j++) {
                String algorithmName = config.getAlgorithms().get(j).getName();
                String directoryName = PathUtils.createPath(RecommenderSystemBuilder.PERFORMANCE_DIRECTORY,
                        classifierName, datasetName, algorithmName);
                for (int k = 0; k < testNumber; k++) {
                    String resultFilename = PathUtils.createName(classifierName, datasetName, algorithmName, String.valueOf(k)) + ".json";
                    String resultFullPath = PathUtils.createPath(directoryName, resultFilename);
                    PerformanceResult result = PerformanceResult.JSON_CREATOR.fromJSON(readJSONObject(resultFullPath));
                    f1Measure[j] += result.f1Measure;
                    attributeNumber[j] += result.attributeNumber;
                    runtime[j] += result.runtime;
                }
                f1Measure[j] /= testNumber;
                attributeNumber[j] /= testNumber;
                runtime[j] /= testNumber;
            }
            double[] earrCoef = calculateEARRCoefs(config.getAlpha(), config.getBetta(), f1Measure, attributeNumber, runtime);
            for (int j = 0; j < earrCoef.length; j++) {
                matrix.set(j, i, earrCoef[j]);
            }
        }

        List<List<MetaFeature>> metaFeaturesList = config.getDatasets().stream()
                .map(name -> config.getExtractors().stream()
                        .map(extractor -> {
                            String path = PathUtils.createPath(RecommenderSystemBuilder.META_FEATURES_DIRECTORY,
                                    name, extractor.getClass().getCanonicalName()) + ".json";
                            return ExtractResult.JSON_CREATOR.fromJSON(readJSONObject(path)).getMetaFeature();
                        })
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        return new RecommenderSystem(config, metaFeaturesList, matrix);
    }

    private static double[] calculateEARRCoefs(double alpha, double betta, double[] f1Measure, double[] attributeNumber, double[] runtime) {
        if (alpha < 0 || betta < 0) {
            throw new IllegalArgumentException("alpha must be >= 0 && betta must be >= 0");
        }
        if (f1Measure == null || runtime == null || attributeNumber == null) {
            throw new IllegalArgumentException("arguments must be not null");
        }
        if (f1Measure.length != runtime.length || runtime.length != attributeNumber.length) {
            throw new IllegalArgumentException("arguments must have same length");
        }
        int len = f1Measure.length;
        double[] eaarCoefs = new double[len];
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                if (i != j) {
                    eaarCoefs[i] += (f1Measure[i] / f1Measure[j]); //
                            //(1 + alpha * Math.log(runtime[i] / runtime[j]) + betta * Math.log(attributeNumber[i] / attributeNumber[j]));
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
