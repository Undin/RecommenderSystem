package com.ifmo.recommendersystem.tasks;

import com.ifmo.recommendersystem.DataSet;
import com.ifmo.recommendersystem.MetaFeatures;
import weka.core.Instances;

import java.util.concurrent.Callable;

/**
 * Created by warrior on 19.11.14.
 */
public class ExtractTask implements Callable<DataSet> {

    private final String datasetName;
    private final MetaFeatures.Set metaFeatureSet;
    private final Instances instances;

    public ExtractTask(String datasetName, Instances instances, MetaFeatures.Set metaFeatureSet) {
        this.datasetName = datasetName;
        this.metaFeatureSet = metaFeatureSet;
        this.instances = instances;
    }

    @Override
    public DataSet call() {
        return DataSet.fromInstances(datasetName, instances, metaFeatureSet);
    }
}
