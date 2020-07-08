package stubidp.utils.rest.analytics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomVariableTest {

    @Test
    public void testGetAsJson_shouldFormatJsonToPiwikCvarFormat() throws Exception {
        CustomVariable customVariable = new CustomVariable(1, "foo", "bar");

        String customVariableAsJson = customVariable.getJson();

        assertThat(customVariableAsJson).isEqualTo("{\"1\":[\"foo\",\"bar\"]}");
    }
}
