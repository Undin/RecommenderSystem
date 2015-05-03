package com.ifmo.recommendersystem;

import com.ifmo.recommendersystem.utils.JSONUtils;
import com.ifmo.recommendersystem.utils.Pair;
import org.json.JSONObject;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Arrays;

public class ClassifierWrapper implements JSONConverted {

    public static class Result {
        public final String classifierName;
        public final double accuracy;
        public final double f1Measure;

        public Result(String classifierName, double accuracy, double f1Measure) {
            this.classifierName = classifierName;
            this.accuracy = accuracy;
            this.f1Measure = f1Measure;
        }

        public Result(String classifierName, Pair<Double, Double> accuracyAndF1measure) {
            this.classifierName = classifierName;
            this.accuracy = accuracyAndF1measure.first;
            this.f1Measure = accuracyAndF1measure.second;
        }
    }

    private final String name;
    private final Classifier classifier;
    private final String[] options;

    private ClassifierWrapper(String name, Classifier classifier, String[] options) {
        this.name = name;
        this.classifier = classifier;
        this.options = options;
    }

    public String getName() {
        return name;
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public String[] getOptions() {
        return options;
    }

    @Override
    public JSONObject toJSON() {
        return JSONUtils.objectToJSON(classifier, options).put(JSONUtils.CLASSIFIER_NAME, name);
    }

    public Result computeAccuracyAndF1Measure(Instances train, Instances test) {
        int[][] confusionMatrix = new int[train.numClasses()][train.numClasses()];
        double correct = 0;
        int sum = 0;
        try {
            Classifier localClassifier = AbstractClassifier.makeCopy(classifier);
            localClassifier.buildClassifier(train);
            for (int i = 0; i < test.numInstances(); i++) {
                Instance instance = test.instance(i);
                if (!instance.classIsMissing()) {
                    sum++;
                    int estimatedClassIndex = (int) localClassifier.classifyInstance(instance);
                    int expectedClassIndex = (int) instance.classValue();
                    if (estimatedClassIndex == expectedClassIndex) {
                        correct++;
                    }
                    confusionMatrix[estimatedClassIndex][expectedClassIndex]++;
                }
            }
            return new Result(name, correct / sum, computeF1Measure(confusionMatrix));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(name, 0D, 0D);
    }

    public static double computeF1Measure(int[][] confusionMatrix) {
        if (confusionMatrix == null) {
            throw new IllegalArgumentException("confusionMatrix must be not null");
        }
        if (confusionMatrix.length != confusionMatrix[0].length) {
            throw new IllegalArgumentException("confusionMatrix must be square");
        }
        int size = confusionMatrix.length;
        int precisionCount = 0;
        int recallCount = 0;
        double precision = 0;
        double recall = 0;
        for (int i = 0; i < size; i++) {
            int sum = 0;
            for (int j = 0; j < size; j++) {
                sum += confusionMatrix[i][j];
            }
            if (sum != 0) {
                precision += confusionMatrix[i][i] / (double) sum;
                precisionCount++;
            }
            sum = 0;
            for (int j = 0; j < size; j++) {
                sum += confusionMatrix[j][i];
            }
            if (sum != 0) {
                recall += confusionMatrix[i][i] / (double) sum;
                recallCount++;
            }

        }
        if (precisionCount != 0) {
            precision /= precisionCount;
        }
        if (recallCount != 0) {
            recall /= recallCount;
        }


        if (precision + recall == 0) {
            return 0;
        }
        return 2 * precision * recall / (precision + recall);

    }

    public static final AbstractJSONCreator<ClassifierWrapper> JSON_CREATOR = new AbstractJSONCreator<ClassifierWrapper>() {
        @Override
        protected ClassifierWrapper throwableFromJSON(JSONObject jsonObject) throws Exception {
            String name = jsonObject.getString(JSONUtils.CLASSIFIER_NAME);
            String classifierClassName = jsonObject.getString(JSONUtils.CLASS_NAME);
            String[] options = JSONUtils.readOptions(jsonObject);
            Classifier classifier = AbstractClassifier.forName(classifierClassName, Arrays.copyOf(options, options.length));
            return new ClassifierWrapper(name, classifier, options);
        }
    };
}
