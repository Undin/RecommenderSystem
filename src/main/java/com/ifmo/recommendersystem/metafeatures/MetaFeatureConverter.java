package com.ifmo.recommendersystem.metafeatures;

import com.ifmo.recommendersystem.ClassifierWrapper;
import com.ifmo.recommendersystem.FSSAlgorithm;
import com.ifmo.recommendersystem.RecommenderSystemBuilder;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * Created by warrior on 06.05.15.
 */
public class MetaFeatureConverter {

    public static final String META_FEATURE_RELATION_NAME = "metaFeatures";

    public static void main(String[] args) {
        EvaluationConfig config = new EvaluationConfig("evaluationConfig.json");
        toArff(config, RecommenderSystemBuilder.META_FEATURES_DIRECTORY, META_FEATURE_RELATION_NAME);
    }

    public static void toArff(EvaluationConfig config, String sourceDirectory, String relationName) {
        Instances instances = createInstances(config, sourceDirectory, relationName);
        try (PrintWriter writer = new PrintWriter("metaFeatures.arff")) {
            writer.println(instances.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void toArff(EvaluationConfig config, String sourceDirectory, String relationName, ClassifierWrapper classifier) {
        Instances instances = createInstances(config, sourceDirectory, relationName, classifier);
        try (PrintWriter writer = new PrintWriter("metaFeatures.arff")) {
            writer.println(instances.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Instances createInstances(EvaluationConfig config, String sourceDirectory, String relationName) {
        return createInstances(config, sourceDirectory, relationName, null);
    }

    public static Instances createInstances(EvaluationConfig config, String sourceDirectory, String relationName, ClassifierWrapper classifier) {
        ArrayList<Attribute> attributes = config.getExtractors().stream()
                .map(e -> new Attribute(e.getClass().getCanonicalName()))
                .collect(Collectors.toCollection(ArrayList::new));
        List<String> algorithmNames = null;
        double[] classIndexes = null;
        if (classifier != null) {
            algorithmNames = config.getAlgorithms().stream()
                    .map(FSSAlgorithm::getName)
                    .collect(Collectors.toList());
            Attribute classAttribute = new Attribute("class", algorithmNames);
            attributes.add(classAttribute);
            double[][] matrix = config.createEarrMatrix(classifier);
            classIndexes = Arrays.stream(matrix)
                    .mapToDouble(doubles -> {
                        int index = 0;
                        for (int i = 1; i < doubles.length; i++) {
                            if (doubles[i] > doubles[index]) {
                                index = i;
                            }
                        }
                        return index;
                    })
                    .toArray();
        }
        int capacity = config.getDatasets().size();
        Instances instances = new Instances(relationName, attributes, capacity);
        if (classifier != null) {
            instances.setClassIndex(attributes.size() - 1);
        }

        File file = new File(sourceDirectory);
        for (int i = 0; i < config.getDatasets().size(); i++) {
            String dataset = config.getDatasets().get(i);
            File datasetDir = new File(file, dataset);

            DoubleStream stream = config.getExtractors().stream()
                    .mapToDouble(e -> {
                        File metaFeatureFile = new File(datasetDir, e.getClass().getCanonicalName() + ".json");
                        ExtractResult result = ExtractResult.JSON_CREATOR.fromJSON(JSONUtils.readJSONObject(metaFeatureFile.getAbsolutePath()));
                        return result.getMetaFeature().getValue();
                    });
            if (classifier != null) {
                stream = DoubleStream.concat(stream, DoubleStream.of(classIndexes[i]));
            }
            double[] values = stream.toArray();
            instances.add(new DenseInstance(1, values));
        }
        return instances;
    }
}
