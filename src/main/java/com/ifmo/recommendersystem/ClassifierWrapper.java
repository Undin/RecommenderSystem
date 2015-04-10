package com.ifmo.recommendersystem;

import com.ifmo.recommendersystem.utils.JSONUtils;
import org.json.JSONObject;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Arrays;

public class ClassifierWrapper implements JSONConverted {

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

    public double getAccuracy(Instances train, Instances test) {
        try {
            Classifier localClassifier = Classifier.makeCopy(classifier);
            localClassifier.buildClassifier(train);
            double accuracy = 0;
            for (int i = 0; i < test.numInstances(); i++) {
                Instance instance = test.instance(i);
                double classIndex = localClassifier.classifyInstance(instance);
                if (classIndex == instance.classValue()) {
                    accuracy += 1;
                }
            }
            return accuracy / test.numInstances();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getF1Measure(Instances train, Instances test) {
        int[][] confusionMatrix = new int[train.numClasses()][train.numClasses()];
        try {
            Classifier localClassifier = Classifier.makeCopy(classifier);
            localClassifier.buildClassifier(train);
            for (int i = 0; i < test.numInstances(); i++) {
                Instance instance = test.instance(i);
                if (!instance.classIsMissing()) {
                    int classIndex = (int) localClassifier.classifyInstance(instance);
                    confusionMatrix[classIndex][(int) instance.classValue()]++;
                }
            }
            return computeF1Measure(confusionMatrix);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
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
            Classifier classifier = Classifier.forName(classifierClassName, Arrays.copyOf(options, options.length));
            return new ClassifierWrapper(name, classifier, options);
        }
    };
}
