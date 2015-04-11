package com.ifmo.recommendersystem.tasks;

import com.ifmo.recommendersystem.AbstractJSONCreator;
import com.ifmo.recommendersystem.JSONConverted;
import com.ifmo.recommendersystem.utils.JSONUtils;
import org.json.JSONObject;

/**
 * Created by warrior on 11.04.15.
 */
public class MetaFeature implements JSONConverted {

    private final String extractorClassName;
    private final double value;

    public MetaFeature(String extractorClassName, double value) {
        this.extractorClassName = extractorClassName;
        this.value = value;
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put(JSONUtils.CLASS_NAME, extractorClassName)
                .put(JSONUtils.VALUE, value);
    }

    public static final AbstractJSONCreator<MetaFeature> JSON_CREATOR = new AbstractJSONCreator<MetaFeature>() {
        @Override
        protected MetaFeature throwableFromJSON(JSONObject jsonObject) throws Exception {
            return new MetaFeature(jsonObject.getString(JSONUtils.CLASS_NAME), jsonObject.getDouble(JSONUtils.VALUE));
        }
    };
}
