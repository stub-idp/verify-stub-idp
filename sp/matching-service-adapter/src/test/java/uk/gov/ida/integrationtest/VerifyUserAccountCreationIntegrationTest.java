package uk.gov.ida.integrationtest;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Response;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.test.utils.httpstub.HttpStubRule;
import uk.gov.ida.integrationtest.helpers.AttributeFactory;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.opensaml.saml.saml2.core.StatusCode.REQUESTER;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static stubidp.test.devpki.TestEntityIds.HUB_ENTITY_ID;
import static stubidp.test.devpki.TestEntityIds.TEST_RP_MS;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aValidEidasAttributeQueryWithCycle3Attributes;
import static uk.gov.ida.integrationtest.helpers.RequestHelper.makeAttributeQueryRequest;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.ADDRESS_HISTORY;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.CURRENT_ADDRESS;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.CURRENT_ADDRESS_VERIFIED;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.CYCLE_3;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.FIRST_NAME;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.FIRST_NAME_VERIFIED;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.MIDDLE_NAME;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.MIDDLE_NAME_VERIFIED;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.SURNAME;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.SURNAME_VERIFIED;
import static uk.gov.ida.matchingserviceadapter.saml.matchers.SignableSAMLObjectBaseMatcher.signedBy;

@ExtendWith(DropwizardExtensionsSupport.class)
class VerifyUserAccountCreationIntegrationTest extends UserAccountCreationBaseTest {
    private static final boolean IS_COUNTRY_ENABLED = false;

    public static final HttpStubRule localMatchingService = new HttpStubRule();

    private static final MatchingServiceAdapterAppExtension matchingServiceAdapterAppExtension = new MatchingServiceAdapterAppExtension(
            IS_COUNTRY_ENABLED,
            Map.of("localMatchingService.matchUrl", "http://localhost:" + localMatchingService.getPort() + MATCHING_REQUEST_PATH,
                    "localMatchingService.accountCreationUrl", "http://localhost:" + localMatchingService.getPort() + UNKNOWN_USER_MATCHING_PATH)
    );

    @Override
    protected MatchingServiceAdapterAppExtension getAppRule() {
        return matchingServiceAdapterAppExtension;
    }

    @Override
    protected HttpStubRule setUpMatchingService() {
        localMatchingService.register(UNKNOWN_USER_MATCHING_PATH, 200, "application/json", "{\"result\": \"success\"}");
        return localMatchingService;
    }

    @Test
    void shouldReturnFailureResponseWhenPassedEidasFullMatchingDataset() {
        List<Attribute> requiredAttributes = Stream.of(FIRST_NAME, FIRST_NAME_VERIFIED, MIDDLE_NAME, MIDDLE_NAME_VERIFIED, SURNAME, SURNAME_VERIFIED, CURRENT_ADDRESS, CURRENT_ADDRESS_VERIFIED, ADDRESS_HISTORY, CYCLE_3)
                .map(userAccountCreationAttribute -> new AttributeFactory(new OpenSamlXmlObjectFactory()).createAttribute(userAccountCreationAttribute))
                .collect(toList());

        AttributeQuery attributeQuery = aValidEidasAttributeQueryWithCycle3Attributes(REQUEST_ID, matchingServiceAdapterAppExtension.getCountryEntityId(), requiredAttributes).build();

        Response response = makeAttributeQueryRequest(UNKNOWN_USER_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);
        List<Assertion> decryptedAssertions = assertionDecrypter.decryptAssertions(response::getEncryptedAssertions);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(REQUESTER);
        assertThat(decryptedAssertions).hasSize(0);
        assertThat(response.getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(response.getIssuer().getValue()).isEqualTo(TEST_RP_MS);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }
}
