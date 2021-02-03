package uk.gov.ida.matchingserviceadapter.validators;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class DateTimeComparator {

    private final Duration clockSkew;
    private final Clock clock;

    public DateTimeComparator(Duration clockSkew) {
        this(clockSkew, Clock.systemUTC());
    }

    DateTimeComparator(Duration clockSkew, Clock clock) {
        this.clockSkew = clockSkew;
        this.clock = clock;
    }

    public boolean isAfterFuzzy(Instant source, Instant target) {
        return source.isAfter(target.minus(clockSkew));
    }

    public boolean isBeforeFuzzy(Instant source, Instant target) {
        return source.isBefore(target.plus(clockSkew));
    }

    public boolean isBeforeNow(Instant dateTime) {
        return isBeforeFuzzy(dateTime, Instant.now(clock));
    }

    public boolean isAfterNow(Instant dateTime) {
        return isAfterFuzzy(dateTime, Instant.now(clock));
    }

    public boolean isAfterSkewedNow(Instant dateTime){
        return !isBeforeNow(dateTime);
    }

    public boolean isBeforeSkewedNow(Instant dateTime){
        return !isAfterNow(dateTime);
    }

}
