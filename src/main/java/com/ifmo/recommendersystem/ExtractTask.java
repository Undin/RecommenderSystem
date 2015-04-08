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


    private final MetaFeatures.Set metaFeatureSet;
    private final Instances instances;

    public ExtractTask(String datasetName, Instances instances, MetaFeatures.Set metaFeatureSet) {
        super(datasetName);
        this.metaFeatureSet = metaFeatureSet;
        this.instances = instances;
    }

    @Override
    protected void runInternal() {
        try {
            DataSet dataSet = DataSet.fromInstances(datasetName, instances, metaFeatureSet.getExtractors());
            File directory = new File(RESULT_DIRECTORY, META_FEATURES_DIRECTORY + File.pathSeparator + metaFeatureSet);
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
        return "extract " + datasetName;
    }
}
