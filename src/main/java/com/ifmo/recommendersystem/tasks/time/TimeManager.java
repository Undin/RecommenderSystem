package com.ifmo.recommendersystem.tasks.time;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.time.Instant;

/**
 * Created by warrior on 02.05.15.
 */
public class TimeManager {

    private static final TimeManager TIME_MANAGER = new TimeManager();

    private final ThreadMXBean bean;

    private TimeManager() {
        bean = ManagementFactory.getThreadMXBean();
    }

    public Timestamp getTimestamp() {
        if (bean.isCurrentThreadCpuTimeSupported()) {
            return new ThreadTimestamp(bean.getCurrentThreadCpuTime());
        } else {
            return new InstantTimestamp(Instant.now());
        }
    }

    public static TimeManager getInstance() {
        return TIME_MANAGER;
    }

    public static long between(Timestamp start, Timestamp end) {
        if (start instanceof InstantTimestamp && end instanceof InstantTimestamp) {
            return Duration.between(((InstantTimestamp) start).getInstant(), ((InstantTimestamp) end).getInstant()).toNanos();
        } else if (start instanceof ThreadTimestamp && end instanceof ThreadTimestamp) {
            return ((ThreadTimestamp) end).getCpuTime() - ((ThreadTimestamp) start).getCpuTime();
        } else {
            return 0;
        }
    }
}
