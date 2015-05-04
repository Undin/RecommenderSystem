package com.ifmo.recommendersystem.tasks;

import com.ifmo.recommendersystem.AbstractJSONCreator;
import com.ifmo.recommendersystem.ClassifierWrapper;
import com.ifmo.recommendersystem.FSSAlgorithm;
import com.ifmo.recommendersystem.JSONConverted;
import org.json.JSONObject;

import java.util.List;

import static com.ifmo.recommendersystem.utils.JSONUtils.*;

/**
 * Created by warrior on 20.11.14.
 */
public class PerformanceResult implements JSONConverted {

    public static final int MEAN = -1;

    public final String dataSetName;
    public final String algorithmName;
    public final String classifierName;
    public final double accuracy;
    public final double f1Measure;
    public final double runtime;
    public final double attributeNumber;
    public final int testNumber;

    PerformanceResult(String dataSetName, int testNumber, FSSAlgorithm.Result fssResult, ClassifierWrapper.Result classifierResult) {
        this.dataSetName = dataSetName;
        this.testNumber = testNumber;
        this.algorithmName = fssResult.algorithmName;
        this.attributeNumber = fssResult.instances.numAttributes();
        this.runtime = fssResult.runtime;
        this.classifierName = classifierResult.classifierName;
        this.accuracy = classifierResult.accuracy;
        this.f1Measure = classifierResult.f1Measure;
    }

    private PerformanceResult(String dataSetName, String algorithmName, String classifierName, double accuracy, double f1Measure, double runtime, double attributeNumber, int testNumber) {
        this.dataSetName = dataSetName;
        this.algorithmName = algorithmName;
        this.classifierName = classifierName;
        this.accuracy = accuracy;
        this.f1Measure = f1Measure;
        this.runtime = runtime;
        this.attributeNumber = attributeNumber;
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

    public static PerformanceResult average(List<PerformanceResult> results) {
        if (results == null || results.size() == 0) {
            throw new IllegalArgumentException();
        }
        PerformanceResult r = results.get(0);
        String dataset = r.dataSetName;
        String algorithm = r.algorithmName;
        String classifier = r.classifierName;
        double accuracy = 0;
        double f1Measure = 0;
        double attributeNumber = 0;
        double runtime = 0;
        for (PerformanceResult result : results) {
            if (!same(r, result)) {
                throw new IllegalArgumentException();
            }
            accuracy += result.accuracy;
            f1Measure += result.f1Measure;
            attributeNumber += result.attributeNumber;
            runtime += result.runtime;
        }
        int size = results.size();
        return new PerformanceResult(dataset, algorithm, classifier,
                accuracy / size, f1Measure / size, runtime / size, attributeNumber / size, MEAN);
    }

    public static boolean same(PerformanceResult r1, PerformanceResult r2) {
        return r1.dataSetName.equals(r2.dataSetName) &&
               r1.classifierName.equals(r2.classifierName) &&
               r1.algorithmName.equals(r2.algorithmName);
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
            return new PerformanceResult(dataSetName, algorithmName, classifierName, accuracy, f1Measure, runtime, attributeNumber, testNumber);
        }
    };
}
