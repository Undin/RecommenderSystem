package com.ifmo.recommendersystem.config;

import com.ifmo.recommendersystem.ClassifierWrapper;
import com.ifmo.recommendersystem.FSSAlgorithm;
import com.ifmo.recommendersystem.metafeatures.MetaFeatureExtractor;
import com.ifmo.recommendersystem.utils.JSONUtils;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

import static com.ifmo.recommendersystem.utils.JSONUtils.*;
import static com.ifmo.recommendersystem.utils.JSONUtils.META_FEATURE_LIST;

/**
* Created by warrior on 27.04.15.
*/
public abstract class Config {

    private final ClassifierWrapper classifier;
    private final List<FSSAlgorithm> algorithms;
    private final List<String> datasets;
    private final List<MetaFeatureExtractor> extractors;
    private final String directory;

    public Config(String configFilename) {
        this(JSONUtils.readJSONObject(configFilename));
    }

    protected Config(JSONObject jsonObject) {
        directory = jsonObject.getString(DIRECTORY);
        classifier = ClassifierWrapper.JSON_CREATOR.fromJSON(jsonObject.getJSONObject(CLASSIFIER));
        algorithms = jsonArrayToObjectList(jsonObject.getJSONArray(ALGORITHMS), FSSAlgorithm.JSON_CREATOR);
        datasets = jsonArrayToStringList(jsonObject.getJSONArray(DATA_SETS));
        extractors = jsonArrayToStringList(jsonObject.getJSONArray(META_FEATURE_LIST)).stream()
                .map(MetaFeatureExtractor::forName)
                .collect(Collectors.toList());
    }

    public ClassifierWrapper getClassifier() {
        return classifier;
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
}
