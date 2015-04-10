package com.ifmo.recommendersystem;

import com.ifmo.recommendersystem.utils.JSONUtils;
import org.json.JSONObject;
import weka.core.Instances;

public class DataSet implements JSONConverted {

    private final String name;

    private MetaFeatures metaFeatures;

    private DataSet(String name, MetaFeatures metaFeatures) {
        this.name = name;
        this.metaFeatures = metaFeatures;
    }

    public MetaFeatures getMetaFeatures() {
        return metaFeatures;
    }

    public String getName() {
        return name;
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject().put(JSONUtils.DATA_SET_NAME, name).put(JSONUtils.META_FEATURES, metaFeatures.toJSON());
    }

    public static DataSet fromInstances(String name, Instances instances, MetaFeatures.Set metaFeatureSet) {
        MetaFeatures metaFeatures = MetaFeatures.extractMetaFeature(metaFeatureSet, instances);
        return new DataSet(name, metaFeatures);
    }

    public static final AbstractJSONCreator<DataSet> JSON_CREATOR = new AbstractJSONCreator<DataSet>() {
        @Override
        protected DataSet throwableFromJSON(JSONObject jsonObject) throws Exception {
            String name = jsonObject.getString(JSONUtils.DATA_SET_NAME);
            JSONObject metaFeatureObject = jsonObject.getJSONObject(JSONUtils.META_FEATURES);
            MetaFeatures metaFeatures = MetaFeatures.JSON_CREATOR.fromJSON(metaFeatureObject);
            return new DataSet(name, metaFeatures);
        }
    };
}
