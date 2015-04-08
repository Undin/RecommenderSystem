package com.ifmo.recommendersystem;

import java.util.concurrent.CountDownLatch;

/**
 * Created by warrior on 19.11.14.
 */
public abstract class AbstractTask implements Runnable {

    public static final String RESULT_DIRECTORY = "results";

    protected final String datasetName;

    protected CountDownLatch latch;

    protected AbstractTask(String datasetName) {
        this.datasetName = datasetName;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        System.out.println(">> DEBUG " + getTaskName() + " start " + start);
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
