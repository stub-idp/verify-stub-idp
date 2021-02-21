package uk.gov.ida.verifyserviceprovider.utils;

import java.time.Duration;
import java.time.Instant;

public class DateTimeComparator {

    public DateTimeComparator(Duration clockSkew) {
        this.clockSkew = clockSkew;
    }

    private final Duration clockSkew;

    public boolean isAfterFuzzy(Instant source, Instant target) {
        return source.isAfter(target.minus(clockSkew));
    }

    public boolean isBeforeFuzzy(Instant source, Instant target) {
        return source.isBefore(target.plus(clockSkew));
    }

    public boolean isBeforeNow(Instant instant) {
        return !isBeforeFuzzy(Instant.now(), instant);
    }

    public boolean isAfterNow(Instant instant) {
        return !isAfterFuzzy(Instant.now(), instant);
    }
}
