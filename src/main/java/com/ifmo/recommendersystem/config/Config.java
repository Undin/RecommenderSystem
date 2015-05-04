package com.ifmo.recommendersystem.config;

import com.ifmo.recommendersystem.ClassifierWrapper;
import com.ifmo.recommendersystem.FSSAlgorithm;
import com.ifmo.recommendersystem.JSONConverted;
import com.ifmo.recommendersystem.metafeatures.MetaFeatureExtractor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.ifmo.recommendersystem.utils.JSONUtils.*;

/**
* Created by warrior on 27.04.15.
*/
public abstract class Config {

    private static final String ALGORITHMS_FILE = "algorithms.json";
    private static final String CLASSIFIERS_FILE = "classifiers.json";

    private final List<ClassifierWrapper> classifiers;
    private final List<FSSAlgorithm> algorithms;
    private final List<String> datasets;
    private final List<MetaFeatureExtractor> extractors;
    private final String directory;
    private final boolean averageResult;

    public Config(String configFilename) {
        this(readJSONObject(configFilename));
    }

    protected Config(JSONObject jsonObject) {
        directory = jsonObject.getString(DIRECTORY);
        datasets = jsonArrayToStringList(jsonObject.getJSONArray(DATA_SETS));
        extractors = jsonArrayToStringList(jsonObject.getJSONArray(META_FEATURE_LIST)).stream()
                .map(MetaFeatureExtractor::forName)
                .collect(Collectors.toList());
        Set<String> classifierNames = new HashSet<>(jsonArrayToStringList(jsonObject.getJSONArray(CLASSIFIERS)));
        Set<String> algorithmNames = new HashSet<>(jsonArrayToStringList(jsonObject.getJSONArray(ALGORITHMS)));
        classifiers = get(CLASSIFIERS_FILE, ClassifierWrapper.JSON_CREATOR, c -> classifierNames.contains(c.getName()));
        algorithms = get(ALGORITHMS_FILE, FSSAlgorithm.JSON_CREATOR, a -> algorithmNames.contains(a.getName()));
        averageResult = jsonObject.optBoolean(AVERAGE_RESULT, false);
    }

    public List<ClassifierWrapper> getClassifiers() {
        return classifiers;
    }

    public List<FSSAlgorithm> getAlgorithms() {
        return algorithms;
    }

    public List<String> getDatasets() {
        return datasets;
    }

    public List<MetaFeatureExtractor> getExtractors() {
        return extractors;
    }

    public String getDirectory() {
        return directory;
    }

    public boolean isAverageResult() {
        return averageResult;
    }

    private static <T extends JSONConverted> List<T> get(String filename, JSONConverted.JSONCreator<T> creator, Predicate<T> predicate) {
        JSONArray array = readJSONArray(filename);
        return jsonArrayToObjectList(array, creator).stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }
}
