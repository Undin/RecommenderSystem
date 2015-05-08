package com.ifmo.recommendersystem.metafeatures;

import com.ifmo.recommendersystem.RecommenderSystemBuilder;
import com.ifmo.recommendersystem.config.Config;
import com.ifmo.recommendersystem.config.EvaluationConfig;
import com.ifmo.recommendersystem.tasks.ExtractResult;
import com.ifmo.recommendersystem.utils.JSONUtils;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by warrior on 06.05.15.
 */
public class MetaFeatureConverter {

    public static final String META_FEATURE_RELATION_NAME = "metaFeatures";

    public static void main(String[] args) {
        EvaluationConfig config = new EvaluationConfig("evaluationConfig.json");
        toArff(config, RecommenderSystemBuilder.META_FEATURES_DIRECTORY, META_FEATURE_RELATION_NAME);
    }

    public static void toArff(Config config, String sourceDirectory, String relationName) {
        Instances instances = createInstances(config, sourceDirectory, relationName);
        try (PrintWriter writer = new PrintWriter("metaFeatures.arff")) {
            writer.println(instances.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Instances createInstances(Config config, String sourceDirectory, String relationName) {
        ArrayList<Attribute> attributes = config.getExtractors().stream()
                .map(e -> new Attribute(e.getClass().getCanonicalName()))
                .collect(Collectors.toCollection(ArrayList::new));
        int capacity = config.getDatasets().size();
        Instances instances = new Instances(relationName, attributes, capacity);

        File file = new File(sourceDirectory);
        for (String dataset : config.getDatasets()) {
            File datasetDir = new File(file, dataset);

            double[] values = config.getExtractors().stream()
                    .mapToDouble(e -> {
                        File metaFeatureFile = new File(datasetDir, e.getClass().getCanonicalName() + ".json");
                        ExtractResult result = ExtractResult.JSON_CREATOR.fromJSON(JSONUtils.readJSONObject(metaFeatureFile.getAbsolutePath()));
                        return result.getMetaFeature().getValue();
                    })
                    .toArray();
            instances.add(new DenseInstance(1, values));
        }
        return instances;
    }
}
