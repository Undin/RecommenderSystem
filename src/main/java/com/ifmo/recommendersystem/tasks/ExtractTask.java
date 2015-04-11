package com.ifmo.recommendersystem.tasks;

import com.ifmo.recommendersystem.metafeatures.MetaFeatureExtractor;
import weka.core.Instances;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
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
        Instant start = Instant.now();
        System.out.format(">> [%s] %s %s\n",
                start.atZone(ZoneId.of("UTC+3")).toLocalTime().toString(),
                datasetName,
                extractor.getName());
        ExtractResult result = ExtractResult.fromInstances(datasetName, instances, extractor);
        Instant end = Instant.now();
        System.out.format("<< [%s] %s %s. exec time: %d\n",
                end.atZone(ZoneId.of("UTC+3")).toLocalTime().toString(),
                datasetName,
                extractor.getName(),
                Duration.between(start, end).toMillis());
        return result;
    }
}
