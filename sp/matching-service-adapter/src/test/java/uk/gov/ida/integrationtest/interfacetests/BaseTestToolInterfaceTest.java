package uk.gov.ida.integrationtest.interfacetests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import io.dropwizard.jackson.Jackson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppExtension;
import uk.gov.ida.matchingserviceadapter.rest.UniversalMatchingServiceRequestDto;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.test.devpki.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.integrationtest.helpers.RequestHelper.makeAttributeQueryRequest;

public abstract class BaseTestToolInterfaceTest {
    private final SignatureAlgorithm signatureAlgorithmForHub = new SignatureRSASHA256();
    private final DigestAlgorithm digestAlgorithmForHub = new DigestSHA256();
    protected String MATCHING_SERVICE_URI;
    protected String UNKNOWN_USER_URI;
    private static final ObjectMapper objectMapper = Jackson.newObjectMapper().setDateFormat(StdDateFormat.getDateInstance());
    protected static final Integer yesterday = 1;
    protected static final Integer inRange405to100 = 100;
    protected static final Integer inRange405to101 = 150;
    protected static final Integer inRange405to200 = 400;
    protected static final Integer inRange180to100 = 100;
    protected static final Integer inRange180to101 = 140;
    protected static final Integer inRange180to150 = 160;

    protected static WireMockClassRule wireMockRule = new WireMockClassRule(wireMockConfig().dynamicPort()) {{
        start();
    }};

    protected static Map<String, String> configRules = Map.ofEntries(
            Map.entry("metadata.environment", "INTEGRATION"),
            Map.entry("localMatchingService.matchUrl", "http://localhost:"+wireMockRule.port()+"/match"),
            Map.entry("localMatchingService.accountCreationUrl", "http://localhost:"+wireMockRule.port()+"/user-account-creation")
    );

    @BeforeAll
    public static void setUp() {
        wireMockRule.stubFor(post("/match").willReturn(okJson("{\"result\": \"match\"}")));
        wireMockRule.stubFor(post("/user-account-creation").willReturn(okJson("{\"result\": \"success\"}")));
    }

    @BeforeEach
    public void reset() {
        wireMockRule.resetRequests();
    }

    protected abstract MatchingServiceAdapterAppExtension getAppRule();

    @BeforeEach
    public void setUris() {
        MATCHING_SERVICE_URI = "http://localhost:" + getAppRule().getLocalPort() + "/matching-service/POST";
        UNKNOWN_USER_URI = "http://localhost:" + getAppRule().getLocalPort() + "/unknown-user-attribute-query";
    }

    protected void assertThatRequestThatWillBeSentIsEquivalentToFile(AttributeQuery attributeQuery, Path file) throws Exception {
        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, file, MATCHING_SERVICE_URI);
    }

    protected void assertThatRequestThatWillBeSentIsEquivalentToFile(AttributeQuery attributeQuery, Path file, String uri) throws Exception {
        makeAttributeQueryRequest(uri, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);

        List<LoggedRequest> requests = wireMockRule.findAll(RequestPatternBuilder.allRequests());

        assertThat(requests.size()).isEqualTo(1);

        UniversalMatchingServiceRequestDto requestSent = objectMapper.readValue(requests.get(0).getBodyAsString(), UniversalMatchingServiceRequestDto.class);
        UniversalMatchingServiceRequestDto requestExpected = objectMapper.readValue(readExpectedJson(file), UniversalMatchingServiceRequestDto.class);

        assertThat(requestSent).isEqualTo(requestExpected);
    }

    private String readExpectedJson(Path filePath) throws Exception {
        return makeDateReplacements(new String(Files.readAllBytes(filePath)));
    }

    private String makeDateReplacements(String input) {
        return input.replace("%yesterdayDate%", getDateReplacement(yesterday).toString())
                .replace("%within405days-100days%", getDateReplacement(inRange405to100).toString())
                .replace("%within405days-101days%", getDateReplacement(inRange405to101).toString())
                .replace("%within405days-200days%", getDateReplacement(inRange405to200).toString())
                .replace("%within180days-100days%", getDateReplacement(inRange180to100).toString())
                .replace("%within180days-101days%", getDateReplacement(inRange180to101).toString())
                .replace("%within180days-150days%", getDateReplacement(inRange180to150).toString());
    }

    protected LocalDate getDateReplacement(Integer daysToSubtract) {
        return LocalDate.now().minusDays(daysToSubtract);
    }
}
