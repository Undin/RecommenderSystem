package com.ifmo.recommendersystem;

import com.ifmo.recommendersystem.utils.InstancesUtils;
import com.ifmo.recommendersystem.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import weka.attributeSelection.UnsupervisedSubsetEvaluator;
import weka.clusterers.Clusterer;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import static com.ifmo.recommendersystem.utils.JSONUtils.*;

/**
 * Created by warrior on 06.05.15.
 */
public class RecommenderSystemEvaluation extends UnsupervisedSubsetEvaluator {

    private final double[][] earrMatrix; // datasets x algorithms
    private final Instances metaFeaturesList;
    private final List<FSSAlgorithm> algorithms;
    private final List<String> datasets;
    private final int datasetSize;

    private JSONObject result;

    public RecommenderSystemEvaluation(double[][] earrMatrix, Instances metaFeaturesList, List<FSSAlgorithm> algorithms, List<String> datasets) {
        this.earrMatrix = earrMatrix;
        this.metaFeaturesList = metaFeaturesList;
        this.algorithms = algorithms;
        this.datasets = datasets;
        this.datasetSize = datasets.size();
    }

    @Override
    public void buildEvaluator(Instances metaFeatures) throws Exception {
    }

    @Override
    public double evaluateSubset(BitSet subset) throws Exception {
        BitSet inverseBitSet = new BitSet(subset.size());
        inverseBitSet.andNot(subset);
        Instances localMetaFeaturesList = InstancesUtils.removeAttributes(metaFeaturesList, inverseBitSet.stream().toArray());
        if (localMetaFeaturesList == null) {
            throw new IllegalStateException();
        }
        double meanRPR = 0;
        JSONArray separateResult = new JSONArray();
        for (int dataIndex = 0; dataIndex < datasetSize; dataIndex++) {
            double[][] matrix = new double[datasetSize - 1][];
            int k = 0;
            for (int j = 0; j < datasetSize; j++) {
                if (dataIndex != j) {
                    matrix[k] = earrMatrix[j];
                    k++;
                }
            }

            RecommenderSystemV2 system = new RecommenderSystemV2(matrix, localMetaFeaturesList, algorithms);
            Instance localMetaFeatures = localMetaFeaturesList.remove(dataIndex);
            FSSAlgorithm recAlgorithm = system.recommend(localMetaFeatures).get(0);

            double optEarr = -Double.MAX_VALUE;
            double worstEarr = Double.MAX_VALUE;
            FSSAlgorithm optAlgorithm = null;
            FSSAlgorithm worstAlgorithm = null;
            for (int algIndex = 0; algIndex < algorithms.size(); algIndex++) {
                if (earrMatrix[dataIndex][algIndex] > optEarr) {
                    optEarr = earrMatrix[dataIndex][algIndex];
                    optAlgorithm = algorithms.get(algIndex);
                }
                if (earrMatrix[dataIndex][algIndex] < worstEarr) {
                    worstEarr = earrMatrix[dataIndex][algIndex];
                    worstAlgorithm = algorithms.get(algIndex);
                }
            }
            int recAlgIndex = algorithms.indexOf(recAlgorithm);
            double rpr = earrMatrix[dataIndex][recAlgIndex] / optEarr;
            double worstRpr = worstEarr / optEarr;
            meanRPR += rpr;

            List<Pair<String, Double>> algorithmRpr = new ArrayList<>(algorithms.size());
            for (int algIndex = 0; algIndex < algorithms.size(); algIndex++) {
                String algName = algorithms.get(algIndex).getName();
                double algRpr = earrMatrix[dataIndex][algIndex] / optEarr;
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
            resultObject.put(DATA_SET_NAME, datasets.get(dataIndex))
                    .put(RECOMMENDED_ALGORITHM, recAlgorithm.getName())
                    .put(OPT_ALGORITHM, optAlgorithm.getName())
                    .put(RPR, rpr)
                    .put(RPR_LIST, rprList)
                    .put(WORST_RPR, worstRpr)
                    .put(WORST_ALGORITHM, worstAlgorithm.getName());
            separateResult.put(resultObject);
            localMetaFeaturesList.add(dataIndex, localMetaFeatures);
        }
        meanRPR /= datasetSize;

        result = new JSONObject();
        result.put(SEPARATE_RESULT, separateResult);
        result.put(MEAN_RPR, meanRPR);

        return meanRPR;
    }

    public double evaluate() {
        BitSet bitSet = new BitSet(datasetSize);
        bitSet.set(0, datasetSize, true);
        try {
            return evaluateSubset(bitSet);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public JSONObject getResult() {
        return result;
    }

    @Override
    public int getNumClusters() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public Clusterer getClusterer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setClusterer(Clusterer d) {
        throw new UnsupportedOperationException();
    }
}
