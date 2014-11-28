package com.ifmo.recommendersystem;

import java.io.File;
import java.util.concurrent.CountDownLatch;

/**
 * Created by warrior on 19.11.14.
 */
public abstract class AbstractTask implements Runnable {

    public static final String RESULT_DIRECTORY = "results";

    protected final String datasetPath;
    protected final String datasetName;

    protected CountDownLatch latch;

    protected AbstractTask(String datasetPath) {
        this.datasetPath = datasetPath;
        int startIdx = datasetPath.lastIndexOf(File.separatorChar);
        int endIdx = datasetPath.lastIndexOf('.');
        this.datasetName = datasetPath.substring(startIdx + 1, endIdx);
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
