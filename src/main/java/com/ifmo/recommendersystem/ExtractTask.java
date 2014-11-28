package com.ifmo.recommendersystem;

import weka.core.Instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by warrior on 19.11.14.
 */
public class ExtractTask extends AbstractTask {

    public static final String META_FEATURES_DIRECTORY = "metaFeatures";

    public ExtractTask(String datasetPath) {
        super(datasetPath);
    }

    @Override
    protected void runInternal() {
        try {
            Instances instances = InstancesUtils.createInstances(datasetPath, InstancesUtils.REMOVE_STRING_ATTRIBUTES |
                    InstancesUtils.REMOVE_UNINFORMATIVE_ATTRIBUTES);
            DataSet dataSet = DataSet.fromInstances(datasetName, instances);
            File directory = new File(RESULT_DIRECTORY, META_FEATURES_DIRECTORY);
            directory.mkdirs();
            try (PrintWriter writer = new PrintWriter(new File(directory, dataSet.getName() + ".json"))) {
                writer.print(dataSet.toJSON().toString(4));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getTaskName() {
        return "extract " + datasetPath;
    }
}
