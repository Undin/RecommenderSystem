package com.ifmo.recommendersystem;

import org.json.JSONObject;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

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

    @Override
    public JSONObject toJSON() {
        return JSONUtils.objectToJSON(classifier, options).put(JSONUtils.NAME, name);
    }

    public static final AbstractJSONCreator<ClassifierWrapper> JSON_CREATOR = new AbstractJSONCreator<ClassifierWrapper>() {
        @Override
        protected ClassifierWrapper throwableFromJSON(JSONObject jsonObject) throws Exception {
            String name = jsonObject.getString(JSONUtils.NAME);
            String classifierClassName = jsonObject.getString(JSONUtils.CLASS_NAME);
            String[] options = JSONUtils.readOptions(jsonObject);
            Classifier classifier = Classifier.forName(classifierClassName, options);
            return new ClassifierWrapper(name, classifier, options);
        }
    };
}
