package com.ifmo.recommendersystem;

import java.util.concurrent.CountDownLatch;

/**
 * Created by warrior on 19.11.14.
 */
public abstract class AbstractTask implements Runnable {

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
        System.out.println(">> DEBUG " + getTaskName() + " start");
        long start = System.currentTimeMillis();
        runInternal();
        long end = System.currentTimeMillis();
        if (latch != null) {
            latch.countDown();
        }
        System.out.println("<< DEBUG " + getTaskName() + " end. time: " + (end - start));
    }

    protected abstract String getTaskName();
    protected abstract void runInternal();
}
