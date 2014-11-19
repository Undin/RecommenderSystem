package com.ifmo.recommendersystem;

import org.json.JSONObject;

import static com.ifmo.recommendersystem.JSONUtils.*;

/**
 * Created by warrior on 20.11.14.
 */
public class PerformanceResult implements JSONConverted {

    public final String dataSetName;
    public final String algorithmName;
    public final String classifierName;
    public final double meanAccuracy;
    public final double meanRuntime;
    public final double meanAttributeNumber;

    public PerformanceResult(String dataSetName, String algorithmName, String classifierName, double meanAccuracy, double meanAttributeNumber, double meanRuntime) {
        this.dataSetName = dataSetName;
        this.algorithmName = algorithmName;
        this.classifierName = classifierName;
        this.meanAccuracy = meanAccuracy;
        this.meanAttributeNumber = meanAttributeNumber;
        this.meanRuntime = meanRuntime;
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject().
                put(DATA_SET_NAME, dataSetName).
                put(ALGORITHM_NAME, algorithmName).
                put(CLASSIFIER_NAME, classifierName).
                put(MEAN_ACCURACY, meanAccuracy).
                put(MEAN_ATTRIBUTE_NUMBER, meanAttributeNumber).
                put(MEAN_RUNTIME, meanRuntime);
    }

    public static final AbstractJSONCreator<PerformanceResult> JSON_CREATOR = new AbstractJSONCreator<PerformanceResult>() {
        @Override
        protected PerformanceResult throwableFromJSON(JSONObject jsonObject) throws Exception {
            String dataSetName = jsonObject.getString(DATA_SET_NAME);
            String algorithmName = jsonObject.getString(ALGORITHM_NAME);
            String classifierName = jsonObject.getString(CLASSIFIER_NAME);
            double meanAccuracy = jsonObject.getDouble(MEAN_ACCURACY);
            double meanAttributeNumber = jsonObject.getDouble(MEAN_ATTRIBUTE_NUMBER);
            double meanRuntime = jsonObject.getDouble(MEAN_RUNTIME);
            return new PerformanceResult(dataSetName, algorithmName, classifierName, meanAccuracy, meanAttributeNumber, meanRuntime);
        }
    };
}
