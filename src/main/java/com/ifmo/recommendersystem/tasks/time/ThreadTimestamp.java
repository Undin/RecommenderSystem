package com.ifmo.recommendersystem.tasks.time;

/**
 * Created by warrior on 02.05.15.
 */
class ThreadTimestamp implements Timestamp {

    private final long cpuTime;

    ThreadTimestamp(long cpuTime) {
        this.cpuTime = cpuTime;
    }

    long getCpuTime() {
        return cpuTime;
    }
}
