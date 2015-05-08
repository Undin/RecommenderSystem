package com.ifmo.recommendersystem.tasks;

import com.ifmo.recommendersystem.AbstractJSONCreator;
import com.ifmo.recommendersystem.JSONConverted;
import com.ifmo.recommendersystem.metafeatures.MetaFeature;
import com.ifmo.recommendersystem.metafeatures.MetaFeatureExtractor;
import com.ifmo.recommendersystem.utils.JSONUtils;
import org.json.JSONObject;
import weka.core.Instances;

public class ExtractResult implements JSONConverted {

    private final String datasetName;

    private MetaFeature metaFeature;

    private ExtractResult(String datasetName, MetaFeature metaFeature) {
        this.datasetName = datasetName;
        this.metaFeature = metaFeature;
    }

    public MetaFeature getMetaFeature() {
        return metaFeature;
    }

    public String getMetaFeatureName() {
        return metaFeature.getExtractorClassName();
    }

    public String getDatasetName() {
        return datasetName;
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put(JSONUtils.DATA_SET_NAME, datasetName)
                .put(JSONUtils.META_FEATURE, metaFeature.toJSON());
    }

    public static ExtractResult fromInstances(String name, Instances instances, MetaFeatureExtractor extractor) {
        return new ExtractResult(name, extractor.extract(instances));
    }

    public static final AbstractJSONCreator<ExtractResult> JSON_CREATOR = new AbstractJSONCreator<ExtractResult>() {
        @Override
        protected ExtractResult throwableFromJSON(JSONObject jsonObject) throws Exception {
            String name = jsonObject.getString(JSONUtils.DATA_SET_NAME);
            JSONObject metaFeatureObject = jsonObject.getJSONObject(JSONUtils.META_FEATURE);
            MetaFeature metaFeature = MetaFeature.JSON_CREATOR.fromJSON(metaFeatureObject);
            return new ExtractResult(name, metaFeature);
        }
    };
}
