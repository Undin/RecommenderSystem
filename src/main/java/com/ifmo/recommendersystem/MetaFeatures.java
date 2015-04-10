package com.ifmo.recommendersystem;

import com.ifmo.recommendersystem.metafeatures.MetaFeatureExtractor;
import com.ifmo.recommendersystem.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import weka.core.Instances;

import java.util.Objects;

public class MetaFeatures implements JSONConverted {

    public static enum Set {
        DEFAULT(new String[]{
                        "com.ifmo.recommendersystem.metafeatures.general.NumberOfInstances",
                        "com.ifmo.recommendersystem.metafeatures.general.NumberOfFeatures",
                        "com.ifmo.recommendersystem.metafeatures.general.NumberOfClasses",
                        "com.ifmo.recommendersystem.metafeatures.general.DataSetDimensionality",
                        "com.ifmo.recommendersystem.metafeatures.statistical.MeanLinearCorrelationCoefficient",
                        "com.ifmo.recommendersystem.metafeatures.statistical.MeanSkewness",
                        "com.ifmo.recommendersystem.metafeatures.statistical.MeanKurtosis",
                        "com.ifmo.recommendersystem.metafeatures.informationtheoretic.NormalizedClassEntropy",
                        "com.ifmo.recommendersystem.metafeatures.informationtheoretic.MeanNormalizedFeatureEntropy",
                        "com.ifmo.recommendersystem.metafeatures.informationtheoretic.MeanMutualInformation",
                        "com.ifmo.recommendersystem.metafeatures.informationtheoretic.MaxMutualInformation",
                        "com.ifmo.recommendersystem.metafeatures.informationtheoretic.EquivalentNumberOfFeatures",
                        "com.ifmo.recommendersystem.metafeatures.informationtheoretic.NoiseSignalRatio",
                });

        private final String[] extractors;

        private Set(String[] extractors) {
            this.extractors = extractors;
        }

        public String[] getExtractors() {
            return extractors;
        }
    }

    private final Set metaFeatureSet;
    private final double[] values;

    public MetaFeatures(Set metaFeatureSet, double[] values) {
        this.metaFeatureSet = Objects.requireNonNull(metaFeatureSet);
        this.values = Objects.requireNonNull(values);
    }

    public double value(int i) {
        return values[i];
    }

    public int numAttributes() {
        return values.length;
    }

    public Set getMetaFeatureSet() {
        return metaFeatureSet;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < metaFeatureSet.getExtractors().length; i++) {
            builder.append(metaFeatureSet.getExtractors()[i]).append(" : ").append(values[i]).append("\n");
        }
        return builder.toString();
    }

    @Override
    public JSONObject toJSON() {
        JSONArray valuesArray = new JSONArray(values);
        return new JSONObject()
                .put(JSONUtils.SET_NAME, metaFeatureSet.name())
                .put(JSONUtils.VALUES, valuesArray);
    }

    public static MetaFeatures extractMetaFeature(Set metaFeatureSet, Instances dataSet) {
        MetaFeatureExtractor[] extractors = new MetaFeatureExtractor[metaFeatureSet.getExtractors().length];
        for (int i = 0; i < extractors.length; i++) {
            extractors[i] = MetaFeatureExtractor.forName(metaFeatureSet.getExtractors()[i]);
        }
        double[] values = new double[extractors.length];
        for (int i = 0; i < extractors.length; i++) {
            values[i] = extractors[i].extract(dataSet);
        }
        return new MetaFeatures(metaFeatureSet, values);
    }

    public static final AbstractJSONCreator<MetaFeatures> JSON_CREATOR = new AbstractJSONCreator<MetaFeatures>() {
        @Override
        protected MetaFeatures throwableFromJSON(JSONObject jsonObject) {
            String name = jsonObject.getString(JSONUtils.SET_NAME);
            JSONArray valuesArray = jsonObject.getJSONArray(JSONUtils.VALUES);
            double[] values = JSONUtils.jsonArrayToDoubleArray(valuesArray);
            return new MetaFeatures(Set.valueOf(name), values);
        }
    };
}
