package com.ifmo.recommendersystem.tasks;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by warrior on 19.11.14.
 */
public abstract class AbstractTask implements Runnable {

    public static final String RESULT_DIRECTORY = "results";

    protected final String datasetName;

    protected AbstractTask(String datasetName) {
        this.datasetName = datasetName;
    }

    @Override
    public void run() {
        Instant start = Instant.now();
        System.out.println(">> DEBUG " + getTaskName() + " start " + start.toEpochMilli());
        runInternal();
        Instant end = Instant.now();
        System.out.println("<< DEBUG " + getTaskName() + " end. time: " + Duration.between(start, end).toMillis());
    }

    protected abstract String getTaskName();
    protected abstract void runInternal();
}
