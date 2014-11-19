package com.ifmo.recommendersystem;

import java.util.concurrent.CountDownLatch;

/**
 * Created by warrior on 19.11.14.
 */
public abstract class AbstractTask implements Runnable {

    public static final String EXTRACT_TYPE = "extract";
    public static final String PERFORMANCE_TYPE = "performance";

    public static final String RESULT_DIRECTORY = "results";

    protected final String datasetPath;

    protected CountDownLatch latch;

    protected AbstractTask(String datasetPath) {
        this.datasetPath = datasetPath;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void run() {
        System.out.println(">> " + getTaskType() + " task for " + datasetPath + " start");
        runInternal();
        if (latch != null) {
            latch.countDown();
        }
        System.out.println("<< " + getTaskType() + " task for " + datasetPath + " end");
    }

    protected abstract String getTaskType();
    protected abstract void runInternal();
}
