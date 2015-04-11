package com.ifmo.recommendersystem.tasks;

import com.ifmo.recommendersystem.metafeatures.MetaFeatureExtractor;
import weka.core.Instances;

import java.util.concurrent.Callable;

/**
 * Created by warrior on 19.11.14.
 */
public class ExtractTask implements Callable<ExtractResult> {

    private final String datasetName;
    private final MetaFeatureExtractor extractor;
    private final Instances instances;

    public ExtractTask(String datasetName, Instances instances, MetaFeatureExtractor extractor) {
        this.datasetName = datasetName;
        this.extractor = extractor;
        this.instances = instances;
    }

    @Override
    public ExtractResult call() {
        return ExtractResult.fromInstances(datasetName, instances, extractor);
    }
}
