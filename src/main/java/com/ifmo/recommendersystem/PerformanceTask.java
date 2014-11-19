package com.ifmo.recommendersystem;

import org.json.JSONArray;
import org.json.JSONObject;
import weka.core.Instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

import static com.ifmo.recommendersystem.JSONUtils.*;

/**
 * Created by warrior on 18.11.14.
 */
public class PerformanceTask extends AbstractTask {

    private static final String PERFORMANCE_DIRECTORY = "performance";

    private final static int ROUNDS = 5;
    private final static int FOLDS = 10;

    private final List<FSSAlgorithm> algorithms;
    private final ClassifierWrapper classifier;

    public PerformanceTask(String datasetPath, List<FSSAlgorithm> algorithms, ClassifierWrapper classifier) {
        super(datasetPath);
        this.algorithms = algorithms;
        this.classifier = classifier;
    }

    @Override
    protected void runInternal() {
        try {
            Instances instances = InstancesUtils.createInstances(datasetPath, true);
            Random random = new Random();
            int algorithmNumber = algorithms.size();
            double[] runtime = new double[algorithmNumber];
            double[] accuracy = new double[algorithmNumber];
            double[] resultAttributeNumber = new double[algorithmNumber];
            for (int i = 0; i < ROUNDS; i++) {
                instances.randomize(random);
                for (int j = 0; j < FOLDS; j++) {
                    for (int k = 0; k < algorithmNumber; k++) {
                        Instances train = instances.trainCV(FOLDS, j);
                        Instances test = instances.testCV(FOLDS, j);
                        FSSAlgorithm.Result result = algorithms.get(k).subsetSelection(train);
                        Instances selectedTrain = InstancesUtils.removeAttributes(train, result.instances);
                        Instances selectedTest = InstancesUtils.removeAttributes(test, result.instances);
                        runtime[k] += result.runtime;
                        resultAttributeNumber[k] += selectedTrain.numAttributes() - 1;
                        accuracy[k] += classifier.getAccuracy(selectedTrain, selectedTest);
                    }
                }
            }
            for (int i = 0; i < algorithmNumber; i++) {
                runtime[i] /= ROUNDS * FOLDS;
                accuracy[i] /= ROUNDS * FOLDS;
                resultAttributeNumber[i] /= ROUNDS * FOLDS;
            }

            JSONArray array = new JSONArray();
            for (int i = 0; i < algorithmNumber; i++) {
                array.put(new JSONObject().
                        put(ALGORITHM, algorithms.get(i).getName()).
                        put(MEAN_RUNTIME, runtime[i]).
                        put(MEAN_ACCURACY, accuracy[i]).
                        put(MEAN_ATTRIBUTE_NUMBER, resultAttributeNumber[i]));
            }

            JSONObject result = new JSONObject().
                    put(NAME, instances.relationName()).
                    put(RESULTS, array);
            File directory = new File(RESULT_DIRECTORY, PERFORMANCE_DIRECTORY + File.separator + classifier.getName());
            directory.mkdirs();
            try (PrintWriter writer = new PrintWriter(new File(directory, instances.relationName() + ".json"))) {
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
    protected String getTaskType() {
        return PERFORMANCE_TYPE;
    }
}
