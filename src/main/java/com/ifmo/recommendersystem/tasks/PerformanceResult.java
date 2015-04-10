package com.ifmo.recommendersystem.tasks;

import com.ifmo.recommendersystem.AbstractJSONCreator;
import com.ifmo.recommendersystem.JSONConverted;
import org.json.JSONObject;

import static com.ifmo.recommendersystem.utils.JSONUtils.*;

/**
 * Created by warrior on 20.11.14.
 */
public class PerformanceResult implements JSONConverted {

    public final String dataSetName;
    public final String algorithmName;
    public final String classifierName;
    public final double accuracy;
    public final double f1Measure;
    public final double runtime;
    public final double attributeNumber;
    public final int testNumber;

    public PerformanceResult(String dataSetName, String algorithmName, String classifierName, double accuracy, double f1Measure, double attributeNumber, double runtime, int testNumber) {
        this.dataSetName = dataSetName;
        this.algorithmName = algorithmName;
        this.classifierName = classifierName;
        this.accuracy = accuracy;
        this.f1Measure = f1Measure;
        this.attributeNumber = attributeNumber;
        this.runtime = runtime;
        this.testNumber = testNumber;
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put(DATA_SET_NAME, dataSetName)
                .put(ALGORITHM_NAME, algorithmName)
                .put(CLASSIFIER_NAME, classifierName)
                .put(ACCURACY, accuracy)
                .put(F1_MEASURE, f1Measure)
                .put(ATTRIBUTE_NUMBER, attributeNumber)
                .put(RUNTIME, runtime)
                .put(TEST_NUMBER, testNumber);
    }

    public static final AbstractJSONCreator<PerformanceResult> JSON_CREATOR = new AbstractJSONCreator<PerformanceResult>() {
        @Override
        protected PerformanceResult throwableFromJSON(JSONObject jsonObject) throws Exception {
            String dataSetName = jsonObject.getString(DATA_SET_NAME);
            String algorithmName = jsonObject.getString(ALGORITHM_NAME);
            String classifierName = jsonObject.getString(CLASSIFIER_NAME);
            double f1Measure = jsonObject.getDouble(F1_MEASURE);
            double accuracy = jsonObject.getDouble(ACCURACY);
            double attributeNumber = jsonObject.getDouble(ATTRIBUTE_NUMBER);
            double runtime = jsonObject.getDouble(RUNTIME);
            int testNumber = jsonObject.getInt(TEST_NUMBER);
            return new PerformanceResult(dataSetName, algorithmName, classifierName, accuracy, f1Measure, attributeNumber, runtime, testNumber);
        }
    };
}
