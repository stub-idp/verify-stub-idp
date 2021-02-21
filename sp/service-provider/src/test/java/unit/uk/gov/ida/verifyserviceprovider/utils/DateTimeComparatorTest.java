package unit.uk.gov.ida.verifyserviceprovider.utils;

import org.junit.jupiter.api.Test;
import uk.gov.ida.verifyserviceprovider.utils.DateTimeComparator;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class DateTimeComparatorTest {

    private static final Instant baseTime = Instant.now();
    private static final DateTimeComparator comparator = new DateTimeComparator(Duration.ofSeconds(5));

    @Test
    void isAfterReturnsTrueWhenAIsAfterB() {
        Instant newTime = baseTime.plus(1, ChronoUnit.MINUTES);

        assertThat(comparator.isAfterFuzzy(newTime, baseTime)).isTrue();
    }

    @Test
    void isAfterReturnsFalseWhenAIsBeforeB() {
        Instant newTime = baseTime.minus(1, ChronoUnit.MINUTES);

        assertThat(comparator.isAfterFuzzy(newTime, baseTime)).isFalse();
    }

    @Test
    void isAfterReturnsTrueWhenAIsWithinSkewOfB() {
        Instant newTime = baseTime.minusSeconds(4);

        assertThat(comparator.isAfterFuzzy(newTime, baseTime)).isTrue();
    }

    @Test
    void isBeforeReturnsTrueWhenAIsBeforeB() {
        Instant newTime = baseTime.minus(1, ChronoUnit.MINUTES);

        assertThat(comparator.isBeforeFuzzy(newTime, baseTime)).isTrue();
    }

    @Test
    void isBeforeReturnsFalseWhenAIsAfterB() {
        Instant newTime = baseTime.plus(1, ChronoUnit.MINUTES);

        assertThat(comparator.isBeforeFuzzy(newTime, baseTime)).isFalse();
    }

    @Test
    void isBeforeReturnsTrueWhenAIsWithinSkewOfB() {
        Instant newTime = baseTime.plusSeconds(4);

        assertThat(comparator.isBeforeFuzzy(newTime, baseTime)).isTrue();
    }

    @Test
    void isBeforeNowReturnsTrueWhenInThePast() {
        Instant pastDateTime = Instant.now().minus(1, ChronoUnit.MINUTES);

        assertThat(comparator.isBeforeNow(pastDateTime)).isTrue();
    }

    @Test
    void isBeforeNowReturnsFalseWhenInThePast() {
        Instant dateTime = Instant.now().plusMillis(1);

        assertThat(comparator.isBeforeNow(dateTime)).isFalse();
    }

    @Test
    void isAfterNowReturnsTrueWhenInTheFuture() {
        Instant futureDateTime = Instant.now().plus(1, ChronoUnit.MINUTES);

        assertThat(comparator.isAfterNow(futureDateTime)).isTrue();
    }

    @Test
    void isAfterNowReturnsFalseWhenInThePast() {
        Instant pastDateTime = Instant.now().minusMillis(1);

        assertThat(comparator.isAfterNow(pastDateTime)).isFalse();
    }
}