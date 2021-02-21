package unit.uk.gov.ida.verifyserviceprovider.utils;

import org.junit.jupiter.api.Test;
import uk.gov.ida.verifyserviceprovider.utils.StringTableFormatter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StringTableFormatterTest {

    @Test
    public void shouldFormatTableToString() {
        String expected = System.lineSeparator() +
            "=====" + System.lineSeparator() +
            "| some-title" + System.lineSeparator() +
            "-----" + System.lineSeparator() +
            "| row-1" + System.lineSeparator() +
            "| row-2" + System.lineSeparator() +
            "=====" + System.lineSeparator();

        String actual = StringTableFormatter.format(
            5,
            "some-title",
            List.of("row-1", "row-2")
        );

        assertThat(actual).isEqualTo(expected);
    }

}