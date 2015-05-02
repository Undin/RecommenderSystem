package com.ifmo.recommendersystem.tasks.time;

import java.time.Instant;

/**
 * Created by warrior on 02.05.15.
 */
class InstantTimestamp implements Timestamp {

    private final Instant instant;

    InstantTimestamp(Instant instant) {
        this.instant = instant;
    }

    Instant getInstant() {
        return instant;
    }
}
