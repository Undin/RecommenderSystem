package com.ifmo.recommendersystem.config;

import com.ifmo.recommendersystem.utils.JSONUtils;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static com.ifmo.recommendersystem.utils.JSONUtils.EVALUATE_PERFORMANCE;
import static com.ifmo.recommendersystem.utils.JSONUtils.EXTRACT_META_FEATURES;
import static com.ifmo.recommendersystem.utils.JSONUtils.PARALLELISM;

/**
 * Created by warrior on 27.04.15.
 */
public class BuildConfig extends Config {

    private final boolean extractMetaFeatures;
    private final boolean evaluatePerformance;
    private final int parallelism;
    private final List<String> datasetsPaths;

    public BuildConfig(String configFilename) {
        this(JSONUtils.readJSONObject(configFilename));
    }

    protected BuildConfig(JSONObject jsonObject) {
        super(jsonObject);
        extractMetaFeatures = jsonObject.getBoolean(EXTRACT_META_FEATURES);
        evaluatePerformance = jsonObject.getBoolean(EVALUATE_PERFORMANCE);
        parallelism = jsonObject.optInt(PARALLELISM, Runtime.getRuntime().availableProcessors());
        datasetsPaths = getDatasets().stream()
                .map(this::createPath)
                .collect(Collectors.toList());
    }

    public boolean extractMetaFeatures() {
        return extractMetaFeatures;
    }

    public boolean evaluatePerformance() {
        return evaluatePerformance;
    }

    public List<String> getDatasetsPaths() {
        return datasetsPaths;
    }

    public String createPath(String datasetName) {
        return getDirectory() + File.separator + datasetName + ".arff";
    }

    public int getParallelism() {
        return parallelism;
    }
}
