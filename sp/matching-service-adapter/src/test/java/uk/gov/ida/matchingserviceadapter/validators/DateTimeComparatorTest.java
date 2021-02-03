package uk.gov.ida.matchingserviceadapter.validators;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class DateTimeComparatorTest {

    private static final Clock clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));
    private static final Instant baseTime = Instant.now(clock);
    private static final DateTimeComparator comparator = new DateTimeComparator(Duration.ofSeconds(5), clock);

    @Test
    void isAfterFuzzyReturnsTrueWhenAIsAfterB() {
        Instant newTime = baseTime.plus(1, ChronoUnit.MINUTES);

        assertThat(comparator.isAfterFuzzy(newTime, baseTime)).isTrue();
    }

    @Test
    void isAfterFuzzyReturnsFalseWhenAIsBeforeB() {
        Instant newTime = baseTime.minus(1, ChronoUnit.MINUTES);

        assertThat(comparator.isAfterFuzzy(newTime, baseTime)).isFalse();
    }

    @Test
    void isAfterFuzzyReturnsTrueWhenAIsAfterBWithinSkew() {
        Instant newTime = baseTime.plusSeconds(4);

        assertThat(comparator.isAfterFuzzy(newTime, baseTime)).isTrue();
    }

    @Test
    void isAfterFuzzyReturnsTrueWhenAIsBeforeBWithinSkew() {
        Instant newTime = baseTime.minusSeconds(4);

        assertThat(comparator.isAfterFuzzy(newTime, baseTime)).isTrue();
    }


    @Test
    void isBeforeFuzzyReturnsTrueWhenAIsBeforeB() {
        Instant newTime = baseTime.minus(1, ChronoUnit.MINUTES);

        assertThat(comparator.isBeforeFuzzy(newTime, baseTime)).isTrue();
    }

    @Test
    void isBeforeFuzzyReturnsFalseWhenAIsAfterB() {
        Instant newTime = baseTime.plus(1, ChronoUnit.MINUTES);

        assertThat(comparator.isBeforeFuzzy(newTime, baseTime)).isFalse();
    }

    @Test
    void isBeforeFuzzyReturnsTrueWhenAIsBeforeBWithinSkew() {
        Instant newTime = baseTime.minusSeconds(4);

        assertThat(comparator.isAfterFuzzy(newTime, baseTime)).isTrue();
    }

    @Test
    void isBeforeFuzzyReturnsTrueWhenAIsAfterBWithinSkew() {
        Instant newTime = baseTime.plusSeconds(4);

        assertThat(comparator.isAfterFuzzy(newTime, baseTime)).isTrue();
    }


    @Test
    void isBeforeNowReturnsTrueWhenInThePast() {
        Instant pastInstant = Instant.now(clock).minus(1, ChronoUnit.MINUTES);

        assertThat(comparator.isBeforeNow(pastInstant)).isTrue();
    }

    @Test
    void isBeforeNowReturnsTrueWhenInThePastWithinSkew() {
        Instant pastInstant = Instant.now(clock).minusSeconds(2);

        assertThat(comparator.isBeforeNow(pastInstant)).isTrue();
    }


    @Test
    void isBeforeNowReturnsTrueWhenInTheFutureWithinSkew() {
        Instant futureInstant = Instant.now(clock).plusSeconds(2);

        assertThat(comparator.isBeforeNow(futureInstant)).isTrue();
    }

    @Test
    void isBeforeNowReturnsFalseWhenInTheFuture() {
        Instant futureInstant = Instant.now(clock).plus(1, ChronoUnit.MINUTES);

        assertThat(comparator.isBeforeNow(futureInstant)).isFalse();
    }

    @Test
    void isAfterNowReturnsTrueWhenInTheFuture() {
        Instant futureInstant = Instant.now(clock).plus(1, ChronoUnit.MINUTES);

        assertThat(comparator.isAfterNow(futureInstant)).isTrue();
    }

    @Test
    void isAfterNowReturnsTrueWhenInTheFutureWithinSkew() {
        Instant futureInstant = Instant.now(clock).plusSeconds(2);

        assertThat(comparator.isAfterNow(futureInstant)).isTrue();
    }

    @Test
    void isAfterNowReturnsTrueWhenInThePastWithinSkew() {
        Instant pastInstant = Instant.now(clock).minusSeconds(2);

        assertThat(comparator.isAfterNow(pastInstant)).isTrue();
    }

    @Test
    void isAfterNowReturnsFalseWhenInThePast() {
        Instant pastInstant = Instant.now(clock).minus(1, ChronoUnit.MINUTES);

        assertThat(comparator.isAfterNow(pastInstant)).isFalse();
    }

    @Test
    void isAfterSkewedNowReturnsTrueWhenInTheFuture(){
        Instant futureInstant = Instant.now(clock).plus(1, ChronoUnit.MINUTES);

        assertThat(comparator.isAfterSkewedNow(futureInstant)).isTrue();
    }

    @Test
    void isAfterSkewedNowReturnsFalseWhenInTheFutureWithinSkew(){
        Instant futureInstant = Instant.now(clock).plusSeconds(2);

        assertThat(comparator.isAfterSkewedNow(futureInstant)).isFalse();
    }

    @Test
    void isAfterSkewedNowReturnsFalseWhenInThePast(){
        Instant pastInstant = Instant.now(clock).minus(1, ChronoUnit.MINUTES);

        assertThat(comparator.isAfterSkewedNow(pastInstant)).isFalse();
    }

    @Test
    void isAfterSkewedNowReturnsFalseWhenInThePastWithinSkew(){
        Instant pastInstant = Instant.now(clock).minusSeconds(2);

        assertThat(comparator.isAfterSkewedNow(pastInstant)).isFalse();
    }

    @Test
    void isBeforeSkewedNowReturnsFalseWhenInTheFuture(){
        Instant futureInstant = Instant.now(clock).plus(1, ChronoUnit.MINUTES);

        assertThat(comparator.isBeforeSkewedNow(futureInstant)).isFalse();
    }

    @Test
    void isBeforeSkewedNowReturnsFalseWhenInTheFutureWithinSkew(){
        Instant futureInstant = Instant.now(clock).plusSeconds(2);

        assertThat(comparator.isBeforeSkewedNow(futureInstant)).isFalse();
    }

    @Test
    void isBeforeSkewedNowReturnsTrueWhenInThePast(){
        Instant pastInstant = Instant.now(clock).minus(1, ChronoUnit.MINUTES);

        assertThat(comparator.isBeforeSkewedNow(pastInstant)).isTrue();
    }

    @Test
    void isBeforeSkewedNowReturnsFalseWhenInThePastWithinSkew(){
        Instant pastInstant = Instant.now(clock).minusSeconds(2);

        assertThat(comparator.isBeforeSkewedNow(pastInstant)).isFalse();
    }
}
