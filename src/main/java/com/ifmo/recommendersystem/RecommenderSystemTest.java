package com.ifmo.recommendersystem;

import com.ifmo.recommendersystem.config.EvaluationConfig;
import com.ifmo.recommendersystem.evaluation.RecommenderSystemEvaluation;
import com.ifmo.recommendersystem.metafeatures.MetaFeatureConverter;
import com.ifmo.recommendersystem.utils.InstancesUtils;
import com.ifmo.recommendersystem.utils.PathUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import weka.attributeSelection.GeneticSearch;
import weka.core.Instances;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by warrior on 08.05.15.
 */
public class RecommenderSystemTest {
    private static final String EVALUATION_CONFIG = "evaluationConfig.json";
    private static final String EVALUATION_RESULT_DIRECTORY = "evaluationResults/geneticSearchSeparatedGeneral";

    public static void main(String[] args) throws IOException {
        EvaluationConfig config = new EvaluationConfig(EVALUATION_CONFIG);
        Instances metaFeaturesList = MetaFeatureConverter.createInstances(config, RecommenderSystemBuilder.META_FEATURES_DIRECTORY, "metaFeatures");
        File file = new File(EVALUATION_RESULT_DIRECTORY);
        file.mkdirs();


        for (ClassifierWrapper classifier : config.getClassifiers()) {
            double[][] matrix = config.createEarrMatrix(classifier);
            RecommenderSystemEvaluation evaluation = new RecommenderSystemEvaluation(matrix, metaFeaturesList, config.getAlgorithms(), config.getDatasets());
            GeneticSearch search = new GeneticSearch();
            try {
                int[] selected = search.search(evaluation, metaFeaturesList);
                Instances selectedMetaFeaturesList = InstancesUtils.removeAttributes(metaFeaturesList, selected, true);
                RecommenderSystemEvaluation eval = new RecommenderSystemEvaluation(matrix, selectedMetaFeaturesList, config.getAlgorithms(), config.getDatasets());
                eval.evaluate();
                JSONArray selectedMetaFeatures = new JSONArray();
                for (int i = 0; i < selectedMetaFeaturesList.numAttributes(); i++) {
                    selectedMetaFeatures.put(selectedMetaFeaturesList.attribute(i).name());
                }
                JSONObject result = eval.getResult();
                result.put("selectedMetaFeatures", selectedMetaFeatures);
                try (PrintWriter writer = new PrintWriter(PathUtils.createPath(EVALUATION_RESULT_DIRECTORY, classifier.getName() + ".json"))) {
                    writer.println(eval.getResult().toString(4));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}