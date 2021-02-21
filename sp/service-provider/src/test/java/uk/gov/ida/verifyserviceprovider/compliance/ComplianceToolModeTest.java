package uk.gov.ida.verifyserviceprovider.compliance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.testing.FixtureHelpers;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.junit.jupiter.api.Test;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.compliance.dto.MatchingDataset;
import uk.gov.ida.verifyserviceprovider.compliance.dto.MatchingDatasetBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

public class ComplianceToolModeTest {

    public static final String HTTP_LOCALHOST_8080 = "http://localhost:8080";
    private final ObjectMapper objectMapper = Jackson.newObjectMapper()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    @Test
    public void testThatConfigurationArgumentCanBeParsed() throws ArgumentParserException, JsonProcessingException {
        ComplianceToolMode complianceToolMode = new ComplianceToolMode(objectMapper, Validators.newValidator(), mock(VerifyServiceProviderApplication.class));

        final Subparser subparser = createParser();
        complianceToolMode.configure(subparser);

        String suppliedHost = "127.0.0.1";
        String suppliedCallbackUrl = HTTP_LOCALHOST_8080;
        int suppliedTimeout = 6;
        MatchingDataset suppliedMatchingDataset = new MatchingDatasetBuilder().build();

        int suppliedPort = 0;
        Namespace namespace = subparser.parseArgs(
                createArguments(
                        "--url", suppliedCallbackUrl,
                        "-d", objectMapper.writeValueAsString(suppliedMatchingDataset),
                        "--host", suppliedHost,
                        "-p", String.valueOf(suppliedPort),
                        "-t", String.valueOf(suppliedTimeout)
                )
        );

        MatchingDataset actual = namespace.get(ComplianceToolMode.IDENTITY_DATASET);
        assertThat(actual).isEqualTo(suppliedMatchingDataset);

        String url = namespace.get(ComplianceToolMode.ASSERTION_CONSUMER_URL);
        assertThat(url).isEqualTo(suppliedCallbackUrl);

        Integer timeout = namespace.get(ComplianceToolMode.TIMEOUT);
        assertThat(timeout).isEqualTo(suppliedTimeout);

        Integer port = namespace.get(ComplianceToolMode.PORT);
        assertThat(port).isEqualTo(suppliedPort);

        String host = namespace.get(ComplianceToolMode.BIND_HOST);
        assertThat(host).isEqualTo(suppliedHost);

    }

    @Test
    public void testThatThereAreDefaults() throws Exception {
        ComplianceToolMode complianceToolMode = new ComplianceToolMode(objectMapper, Validators.newValidator(), mock(VerifyServiceProviderApplication.class));

        final Subparser subparser = createParser();
        complianceToolMode.configure(subparser);

        Namespace namespace = subparser.parseArgs(noArguments());

        MatchingDataset receivedMatchingDataset = namespace.get(ComplianceToolMode.IDENTITY_DATASET);
        MatchingDataset expectedMatchingDataset = objectMapper.readValue(FixtureHelpers.fixture("default-test-identity-dataset.json"), MatchingDataset.class);
        assertThat(receivedMatchingDataset).isEqualTo(expectedMatchingDataset);

        String url = namespace.get(ComplianceToolMode.ASSERTION_CONSUMER_URL);
        assertThat(url).isEqualTo(ComplianceToolMode.DEFAULT_CONSUMER_URL);

        Integer timeout = namespace.get(ComplianceToolMode.TIMEOUT);
        assertThat(timeout).isEqualTo(ComplianceToolMode.DEFAULT_TIMEOUT);

        Integer port = namespace.get(ComplianceToolMode.PORT);
        assertThat(port).isEqualTo(ComplianceToolMode.DEFAULT_PORT);

        String host = namespace.get(ComplianceToolMode.BIND_HOST);
        assertThat(host).isEqualTo(ComplianceToolMode.DEFAULT_HOST);

    }

    @Test
    public void itWillErrorIfTheMatchingDatasetIsNotValid() {
        ComplianceToolMode complianceToolMode = new ComplianceToolMode(objectMapper, Validators.newValidator(), mock(VerifyServiceProviderApplication.class));
        final Subparser subparser = createParser();
        complianceToolMode.configure(subparser);

        assertThatThrownBy(()-> subparser.parseArgs(createArguments("-d", "{}")))
                .isInstanceOf(ArgumentParserException.class)
                .hasMessageStartingWith("Matching Dataset argument was not valid:");
    }

    private String[] createArguments(String... args) {
        return args;
    }

    private String[] noArguments() {
        return createArguments();
    }

    private Subparser createParser() {
        final ArgumentParser p = ArgumentParsers.newFor("Usage:").addHelp(false).build();
        return p.addSubparsers().addParser("development", false);
    }

}