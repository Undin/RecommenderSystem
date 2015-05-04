package com.ifmo.recommendersystem.utils;

import com.ifmo.recommendersystem.JSONConverted;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class JSONUtils {

    public static final String ACCURACY = "accuracy";
    public static final String ALGORITHM_NAME = "algorithmName";
    public static final String ALGORITHMS = "algorithms";
    public static final String ALPHA = "alpha";
    public static final String ATTRIBUTE_NUMBER = "attributeNumber";
    public static final String AVERAGE_RESULT = "averageResult";
    public static final String BETTA = "betta";
    public static final String CLASS_NAME = "className";
    public static final String CLASSIFIERS = "classifiers";
    public static final String CLASSIFIER_NAME = "classifierName";
    public static final String DATA_SET_NAME = "dataSetName";
    public static final String DATA_SETS = "dataSets";
    public static final String DIRECTORY = "directory";
    public static final String EVALUATE_PERFORMANCE = "evaluatePerformance";
    public static final String EVALUATION = "evaluation";
    public static final String EXTRACT_META_FEATURES = "extractMetaFeatures";
    public static final String F1_MEASURE = "f1Measure";
    public static final String HIT_RATIO = "hitRatio";
    public static final String LIST = "list";
    public static final String MEAN_HIT_RATIO = "meanHitRatio";
    public static final String META_FEATURE = "metaFeature";
    public static final String META_FEATURE_LIST = "metaFeatureList";
    public static final String MEAN_RPR = "meanRPR";
    public static final String OPT_ALGORITHM = "optAlgorithm";
    public static final String OPT_ALGORITHM_SET = "optAlgorithmSet";
    public static final String OPTIONS = "options";
    public static final String PARALLELISM = "parallelism";
    public static final String RECOMMENDED_ALGORITHM = "recommendedAlgorithm";
    public static final String RPR = "RPR";
    public static final String RPR_LIST = "prpList";
    public static final String RUNTIME = "runtime";
    public static final String SEARCH = "search";
    public static final String SEPARATE_RESULT = "separateResult";
    public static final String TEST_NUMBER = "testNumber";
    public static final String VALUE = "value";
    public static final String VALUES = "values";
    public static final String WORST_ALGORITHM = "worstAlgorithm";
    public static final String WORST_RPR = "worstRPR";

    public static String[] readOptions(JSONObject jsonObject) {
        if (jsonObject.has(OPTIONS)) {
            JSONArray optionsArray = jsonObject.getJSONArray(OPTIONS);
            String[] options = new String[optionsArray.length()];
            for (int i = 0; i < optionsArray.length(); i++) {
                options[i] = optionsArray.getString(i);
            }
            return options;
        }
        return null;
    }

    public static JSONObject objectToJSON(Object object, String[] options) {
        return new JSONObject().
                put(CLASS_NAME, object.getClass().getCanonicalName()).
                put(OPTIONS, options == null ? Collections.emptyList() : Arrays.asList(options));
    }

    public static JSONArray collectionToJSONArray(Collection<? extends JSONConverted> collection) {
        List<JSONObject> objectList = new ArrayList<>(collection.size());
        objectList.addAll(collection.stream().map(JSONConverted::toJSON).collect(Collectors.toList()));
        return new JSONArray(objectList);
    }

    public static <T> List<T> jsonArrayToObjectList(JSONArray jsonArray, JSONConverted.JSONCreator<T> creator) {
        List<T> objects = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            objects.add(creator.fromJSON(jsonArray.getJSONObject(i)));
        }
        return objects;
    }

    public static List<String> jsonArrayToStringList(JSONArray jsonArray) {
        List<String> objects = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            objects.add(jsonArray.getString(i));
        }
        return objects;
    }

    public static double[] jsonArrayToDoubleArray(JSONArray jsonArray) {
        double[] array = new double[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            array[i] = jsonArray.getDouble(i);
        }
        return array;
    }

    public static JSONObject readJSONObject(String filename) {
        JSONObject result = null;
        try (InputStream inputStream = new FileInputStream(filename)) {
            String config = IOUtils.toString(inputStream);
            result = new JSONObject(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static JSONArray readJSONArray(String filename) {
        JSONArray result = null;
        try (InputStream inputStream = new FileInputStream(filename)) {
            String config = IOUtils.toString(inputStream);
            result = new JSONArray(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
