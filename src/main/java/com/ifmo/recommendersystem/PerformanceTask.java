package com.ifmo.recommendersystem;

import org.json.JSONObject;
import weka.core.Instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

import static com.ifmo.recommendersystem.JSONUtils.*;

/**
 * Created by warrior on 18.11.14.
 */
public class PerformanceTask extends AbstractTask {

    private static final String PERFORMANCE_DIRECTORY = "performance";

    private final static int ROUNDS = 5;
    private final static int FOLDS = 10;

    private final String name;

    private final FSSAlgorithm algorithm;
    private final ClassifierWrapper classifier;

    public PerformanceTask(String datasetPath, FSSAlgorithm algorithm, ClassifierWrapper classifier) {
        super(datasetPath);
        this.algorithm = algorithm;
        this.classifier = classifier;
        this.name = Utils.createName(classifier.getName(), algorithm.getName(), datasetPath);
    }

    @Override
    protected void runInternal() {
        try {
            Instances instances = InstancesUtils.createInstances(datasetPath, true);
            Random random = new Random();
            double runtime = 0;
            double accuracy = 0;
            double resultAttributeNumber = 0;
            for (int i = 0; i < ROUNDS; i++) {
                instances.randomize(random);
                for (int j = 0; j < FOLDS; j++) {
                    Instances train = instances.trainCV(FOLDS, j);
                    Instances test = instances.testCV(FOLDS, j);
                    FSSAlgorithm.Result result = algorithm.subsetSelection(train);
                    Instances selectedTrain = InstancesUtils.removeAttributes(train, result.instances);
                    Instances selectedTest = InstancesUtils.removeAttributes(test, result.instances);
                    runtime += result.runtime;
                    resultAttributeNumber += selectedTrain.numAttributes() - 1;
                    accuracy += classifier.getAccuracy(selectedTrain, selectedTest);
                }
            }

            runtime /= ROUNDS * FOLDS;
            accuracy /= ROUNDS * FOLDS;
            resultAttributeNumber /= ROUNDS * FOLDS;

            JSONObject result = new JSONObject().
                    put(DATA_SET_NAME, instances.relationName()).
                    put(ALGORITHM_NAME, algorithm.getName()).
                    put(CLASS_NAME, classifier.getName()).
                    put(MEAN_RUNTIME, runtime).
                    put(MEAN_ACCURACY, accuracy).
                    put(MEAN_ATTRIBUTE_NUMBER, resultAttributeNumber);

            String directoryPath = Utils.createPath(RESULT_DIRECTORY,
                    PERFORMANCE_DIRECTORY,
                    classifier.getName(),
                    instances.relationName(),
                    algorithm.getName());
            String fileName = Utils.createName(classifier.getName(), instances.relationName(), algorithm.getName());
            File directory = new File(directoryPath);
            directory.mkdirs();
            try (PrintWriter writer = new PrintWriter(new File(directory, fileName + ".json"))) {
                writer.print(result.toString(4));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println(">> " + datasetPath + " <<");
            e.printStackTrace();
        }
    }

    @Override
    protected String getTaskName() {
        return name;
    }

}
