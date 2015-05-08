package com.ifmo.recommendersystem.evaluation;

import com.ifmo.recommendersystem.FSSAlgorithm;
import org.json.JSONObject;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Created by warrior on 08.05.15.
 */
public class RecommenderSystemMeanEvaluator extends AbstractRecommenderSystemEvaluator {

    private final List<double[][]> matrices;
    private final Instances metaFeaturesList;
    private final List<FSSAlgorithm> algorithms;
    private final List<String> datasets;

    private List<JSONObject> results;

    public RecommenderSystemMeanEvaluator(List<double[][]> matrices, Instances metaFeaturesList, List<FSSAlgorithm> algorithms, List<String> datasets) {
        this.matrices = matrices;
        this.metaFeaturesList = metaFeaturesList;
        this.algorithms = algorithms;
        this.datasets = datasets;
    }

    @Override
    public double evaluateSubset(BitSet subset) throws Exception {
        double meanRPR = 0;
        results = new ArrayList<>();
        for (double[][] matrix : matrices) {
            RecommenderSystemEvaluation evaluation = new RecommenderSystemEvaluation(matrix, metaFeaturesList, algorithms, datasets);
            meanRPR += evaluation.evaluateSubset(subset);
            results.add(evaluation.getResult());
        }
        return meanRPR / matrices.size();
    }

    public List<JSONObject> getResults() {
        return results;
    }
}
