package stubidp.stubidp.configuration;

import io.dropwizard.configuration.YamlConfigurationFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.dropwizard.jackson.Jackson.newObjectMapper;
import static io.dropwizard.jersey.validation.Validators.newValidator;
import static org.assertj.core.api.Assertions.assertThat;

class DatabaseConfigurationTest {

    private final YamlConfigurationFactory factory = new YamlConfigurationFactory<>(
            DatabaseConfiguration.class, newValidator(), newObjectMapper(), "dw.");

    @Test
    void shouldThrowRTEIfNoUrlorVcap() throws Exception {
        DatabaseConfiguration dbCfg = (DatabaseConfiguration)
                factory.build(new StringConfigurationSourceProvider("aField: {}"), "");

        final Exception exception = Assertions.assertThrows(Exception.class, dbCfg::getUrl);
        assertThat(exception.getMessage()).contains("Neither url nor vcapServices was workable");
    }

    @Test
    void shouldReturnUrlIfOnlyUrlProvided() throws Exception {
        String url = "jdbc:postgresql://db.example.com:5432/broker?password=dbpassword&ssl=true&user=dbuser";

        DatabaseConfiguration dbCfg = (DatabaseConfiguration)
                factory.build(new StringConfigurationSourceProvider("url: " + url), "");

        assertThat(dbCfg.getUrl()).isEqualTo(url);
    }

    @Test
    void shouldReturnVcapIfOnlyVcapProvided() throws Exception {
        String url = "jdbc:postgresql://db.example.com:5432/broker?password=dbpassword&ssl=true&user=dbuser";
        String vcap = "{\"postgres\":[{ \"credentials\": { \"jdbcuri\": \"" + url + "\"}}]}";

        DatabaseConfiguration dbCfg = (DatabaseConfiguration)
                factory.build(new StringConfigurationSourceProvider("vcapServices: '" + vcap + "'"), "");

        assertThat(dbCfg.getUrl()).isEqualTo(url);
    }

    @Test
    void shouldReturnVcapIfBothUrlAndVcapProvided() throws Exception {
        String url = "jdbc:postgresql://db.example.com:5432/broker?password=dbpassword&ssl=true&user=dbuser";
        String vcap = "{\"postgres\":[{ \"credentials\": { \"jdbcuri\": \"" + url + "\"}}]}";

        DatabaseConfiguration dbCfg = (DatabaseConfiguration)
                factory.build(new StringConfigurationSourceProvider("url: idontcare\nvcapServices: '" + vcap + "'"), "");

        assertThat(dbCfg.getUrl()).isEqualTo(url);
    }

    @Test
    void shouldThrowExceptionIfUrlIsEmpty() throws Exception {
        DatabaseConfiguration dbCfg = (DatabaseConfiguration)
                factory.build(new StringConfigurationSourceProvider("url: ''"), "");

        final Exception exception = Assertions.assertThrows(Exception.class, dbCfg::getUrl);
        assertThat(exception.getMessage()).contains("Neither url nor vcapServices was workable");
    }

    @Test
    void shouldThrowExceptionIfVcapIsEmpty() throws Exception {
        DatabaseConfiguration dbCfg = (DatabaseConfiguration)
                factory.build(new StringConfigurationSourceProvider("vcap: ''"), "");

        final Exception exception = Assertions.assertThrows(Exception.class, dbCfg::getUrl);
        assertThat(exception.getMessage()).contains("Neither url nor vcapServices was workable");
    }
}