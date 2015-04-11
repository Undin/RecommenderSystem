package com.ifmo.recommendersystem.tasks;

import com.ifmo.recommendersystem.AbstractJSONCreator;
import com.ifmo.recommendersystem.JSONConverted;
import com.ifmo.recommendersystem.metafeatures.MetaFeatureExtractor;
import com.ifmo.recommendersystem.utils.JSONUtils;
import org.json.JSONObject;

/**
 * Created by warrior on 11.04.15.
 */
public class MetaFeature implements JSONConverted {

    private final String name;
    private final String extractorClassName;
    private final double value;

    public MetaFeature(MetaFeatureExtractor extractor, double value) {
        this.name = extractor.getClass().getSimpleName();
        this.extractorClassName = extractor.getClass().getCanonicalName();
        this.value = value;
    }

    public MetaFeature(String name, String extractorClassName, double value) {
        this.name = name;
        this.extractorClassName = extractorClassName;
        this.value = value;
    }

    public String getExtractorClassName() {
        return extractorClassName;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put(JSONUtils.META_FEATURE_NAME, name)
                .put(JSONUtils.CLASS_NAME, extractorClassName)
                .put(JSONUtils.VALUE, value);
    }

    public static final AbstractJSONCreator<MetaFeature> JSON_CREATOR = new AbstractJSONCreator<MetaFeature>() {
        @Override
        protected MetaFeature throwableFromJSON(JSONObject jsonObject) throws Exception {
            return new MetaFeature(jsonObject.getString(JSONUtils.META_FEATURE_NAME),
                    jsonObject.getString(JSONUtils.CLASS_NAME),
                    jsonObject.getDouble(JSONUtils.VALUE));
        }
    };
}
