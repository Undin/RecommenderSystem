package com.ifmo.recommendersystem.config;

import com.ifmo.recommendersystem.ClassifierWrapper;
import com.ifmo.recommendersystem.RecommenderSystemBuilder;
import com.ifmo.recommendersystem.tasks.PerformanceResult;
import com.ifmo.recommendersystem.utils.PathUtils;
import org.json.JSONObject;

import java.util.Arrays;

import static com.ifmo.recommendersystem.utils.JSONUtils.ALPHA;
import static com.ifmo.recommendersystem.utils.JSONUtils.BETTA;
import static com.ifmo.recommendersystem.utils.JSONUtils.readJSONObject;

/**
 * Created by warrior on 27.04.15.
 */
public class EvaluationConfig extends Config {

    private final double alpha;
    private final double betta;

    public EvaluationConfig(String configFilename) {
        this(readJSONObject(configFilename));
    }

    protected EvaluationConfig(JSONObject jsonObject) {
        super(jsonObject);
        alpha = jsonObject.getDouble(ALPHA);
        betta = jsonObject.getDouble(BETTA);
    }

    public double getAlpha() {
        return alpha;
    }

    public double getBetta() {
        return betta;
    }

    public double[][] createEarrMatrix(ClassifierWrapper classifier) {
        int algorithmNumber = getAlgorithms().size();
        int datasetNumber = getDatasets().size();
        int testNumber = RecommenderSystemBuilder.FOLDS * RecommenderSystemBuilder.ROUNDS;
        String classifierName = classifier.getName();

        double[] f1Measure = new double[algorithmNumber];
        double[] attributeNumber = new double[algorithmNumber];
        double[] runtime = new double[algorithmNumber];

        double[][] matrix = new double[datasetNumber][algorithmNumber]; // datasets x alrogithms

        for (int i = 0; i < datasetNumber; i++) {
            Arrays.fill(f1Measure, 0);
            Arrays.fill(attributeNumber, 0);
            Arrays.fill(runtime, 0);
            String datasetName = getDatasets().get(i);
            for (int j = 0; j < algorithmNumber; j++) {
                String algorithmName = getAlgorithms().get(j).getName();
                if (isAverageResult()) {
                    String resultFullPath = PathUtils.createPath(RecommenderSystemBuilder.AVERAGE_PERFORMANCE_DIRECTORY,
                            classifierName,
                            datasetName,
                            PathUtils.createName(classifierName, datasetName, algorithmName) + ".json");
                    PerformanceResult average = PerformanceResult.JSON_CREATOR.fromJSON(readJSONObject(resultFullPath));
                    f1Measure[j] = average.f1Measure;
                    attributeNumber[j] = average.attributeNumber;
                    runtime[j] = average.runtime;
                } else {
                    String directoryName = PathUtils.createPath(RecommenderSystemBuilder.PERFORMANCE_DIRECTORY,
                            classifierName, datasetName, algorithmName);
                    for (int k = 0; k < testNumber; k++) {
                        String resultFilename = PathUtils.createName(classifierName, datasetName, algorithmName, String.valueOf(k)) + ".json";
                        String resultFullPath = PathUtils.createPath(directoryName, resultFilename);
                        PerformanceResult result = PerformanceResult.JSON_CREATOR.fromJSON(readJSONObject(resultFullPath));
                        f1Measure[j] += result.f1Measure;
                        attributeNumber[j] += result.attributeNumber;
                        runtime[j] += result.runtime;
                    }
                    f1Measure[j] /= testNumber;
                    attributeNumber[j] /= testNumber;
                    runtime[j] /= testNumber;
                }
            }
            matrix[i] = calculateEARRCoefs(getAlpha(), getBetta(), f1Measure, attributeNumber, runtime);
        }
        return matrix;
    }

    private static double[] calculateEARRCoefs(double alpha, double betta, double[] f1Measure, double[] attributeNumber, double[] runtime) {
        if (alpha < 0 || betta < 0) {
            throw new IllegalArgumentException("alpha must be >= 0 && betta must be >= 0");
        }
        if (f1Measure == null || runtime == null || attributeNumber == null) {
            throw new IllegalArgumentException("arguments must be not null");
        }
        if (f1Measure.length != runtime.length || runtime.length != attributeNumber.length) {
            throw new IllegalArgumentException("arguments must have same length");
        }
        int len = f1Measure.length;
        double[] eaarCoefs = new double[len];
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                if (i != j) {
                    eaarCoefs[i] += (f1Measure[i] / f1Measure[j]); //
                    //(1 + alpha * Math.log(runtime[i] / runtime[j]) + betta * Math.log(attributeNumber[i] / attributeNumber[j]));
                }
            }
            eaarCoefs[i] /= len - 1;
        }
        return eaarCoefs;
    }
}
