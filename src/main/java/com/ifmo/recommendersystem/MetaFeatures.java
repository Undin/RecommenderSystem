package com.ifmo.recommendersystem;

import com.ifmo.recommendersystem.metafeatures.MetaFeatureExtractor;
import org.json.JSONArray;
import org.json.JSONObject;
import weka.core.Instances;

import java.util.List;
import java.util.Objects;

public class MetaFeatures implements JSONConverted {

    private final String[] extractorClassNames;
    private final double[] values;

    public MetaFeatures(String[] extractorClassNames, double[] values) {
        this.extractorClassNames = Objects.requireNonNull(extractorClassNames);
        this.values = Objects.requireNonNull(values);
    }

    public double value(int i) {
        return values[i];
    }

    public int numAttributes() {
        return values.length;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < extractorClassNames.length; i++) {
            builder.append(extractorClassNames[i]).append(" : ").append(values[i]).append("\n");
        }
        return builder.toString();
    }

    @Override
    public JSONObject toJSON() {
        JSONArray extractors = new JSONArray(extractorClassNames);
        JSONArray valuesArray = new JSONArray(values);
        return new JSONObject()
                .put(JSONUtils.EXTRACTORS, extractors)
                .put(JSONUtils.VALUES, valuesArray);
    }

    public static MetaFeatures extractMetaFeature(String[] extractorClassNames, Instances dataSet) {
        MetaFeatureExtractor[] extractors = new MetaFeatureExtractor[extractorClassNames.length];
        for (int i = 0; i < extractors.length; i++) {
            extractors[i] = MetaFeatureExtractor.forName(extractorClassNames[i]);
        }
        double[] values = new double[extractors.length];
        for (int i = 0; i < extractors.length; i++) {
            values[i] = extractors[i].extract(dataSet);
        }
        return new MetaFeatures(extractorClassNames, values);
    }

    public static final AbstractJSONCreator<MetaFeatures> JSON_CREATOR = new AbstractJSONCreator<MetaFeatures>() {
        @Override
        protected MetaFeatures throwableFromJSON(JSONObject jsonObject) {
            JSONArray extractorsArray = jsonObject.getJSONArray(JSONUtils.EXTRACTORS);
            JSONArray valuesArray = jsonObject.getJSONArray(JSONUtils.VALUES);
            List<String> extractors = JSONUtils.jsonArrayToStringList(extractorsArray);
            double[] values = JSONUtils.jsonArrayToDoubleArray(valuesArray);
            return new MetaFeatures(extractors.toArray(new String[extractors.size()]), values);
        }
    };
}
