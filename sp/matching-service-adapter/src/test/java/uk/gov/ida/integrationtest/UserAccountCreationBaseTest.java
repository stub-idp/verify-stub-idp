package uk.gov.ida.integrationtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import stubidp.saml.security.AssertionDecrypter;
import stubidp.saml.test.builders.AddressAttributeBuilder_1_1;
import stubidp.saml.test.builders.AddressAttributeValueBuilder_1_1;
import stubidp.saml.test.builders.AssertionBuilder;
import stubidp.saml.test.builders.SignatureBuilder;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.test.utils.httpstub.HttpStubRule;
import uk.gov.ida.integrationtest.builders.UserAccountCreationValueAttributeBuilder;
import uk.gov.ida.integrationtest.helpers.AttributeFactory;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppExtension;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.opensaml.saml.saml2.core.StatusCode.RESPONDER;
import static org.opensaml.saml.saml2.core.StatusCode.SUCCESS;
import static stubidp.saml.extensions.domain.SamlStatusCode.CREATED;
import static stubidp.saml.extensions.domain.SamlStatusCode.CREATE_FAILURE;
import static stubidp.saml.test.builders.AddressAttributeValueBuilder_1_1.anAddressAttributeValue;
import static stubidp.saml.test.builders.AttributeQueryBuilder.anAttributeQuery;
import static stubidp.saml.test.builders.IssuerBuilder.anIssuer;
import static stubidp.saml.test.builders.PersonNameAttributeBuilder_1_1.aPersonName_1_1;
import static stubidp.saml.test.builders.PersonNameAttributeValueBuilder.aPersonNameValue;
import static stubidp.saml.test.builders.VerifiedAttributeValueBuilder.aVerifiedValue;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static stubidp.test.devpki.TestEntityIds.HUB_ENTITY_ID;
import static stubidp.test.devpki.TestEntityIds.TEST_RP_MS;
import static uk.gov.ida.integrationtest.builders.UserAccountCreationValueAttributeBuilder.aUserAccountCreationAttributeValue;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aCompleteMatchingDatasetAssertion;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aMatchingDatasetAssertion;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aSubjectWithAssertions;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.anAssertionDecrypter;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.anAuthnStatementAssertion;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.assertionWithOnlyFirstName;
import static uk.gov.ida.integrationtest.helpers.RequestHelper.makeAttributeQueryRequest;
import static uk.gov.ida.integrationtest.helpers.UserAccountCreationTestAssertionHelper.assertThatResponseContainsExpectedUserCreationAttributes;
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

public abstract class UserAccountCreationBaseTest {
    protected static final String UNKNOWN_USER_MATCHING_PATH = "/unknown-user-attribute-query";
    protected static final String MATCHING_REQUEST_PATH = "/matching-request";
    protected static final String REQUEST_ID = "default-request-id";
    protected String UNKNOWN_USER_URI;

    protected AssertionDecrypter assertionDecrypter;
    protected final SignatureAlgorithm signatureAlgorithmForHub = new SignatureRSASHA256();
    protected final DigestAlgorithm digestAlgorithmForHub = new DigestSHA256();

    private HttpStubRule localMatchingService;

    @BeforeEach
    void setUp() throws Exception {
        assertionDecrypter = anAssertionDecrypter();
        localMatchingService = setUpMatchingService();
        UNKNOWN_USER_URI = "http://localhost:" + getAppRule().getLocalPort() + "/unknown-user-attribute-query";
    }

    protected abstract MatchingServiceAdapterAppExtension getAppRule();
    protected abstract HttpStubRule setUpMatchingService() throws Exception;

    @Test
    void shouldReturnCurrentAttributesWhenPassedFullMatchingDataset() {
        List<Attribute> requiredAttributes = attributesFromUacAttributes(Stream.of(
                FIRST_NAME, FIRST_NAME_VERIFIED, MIDDLE_NAME, MIDDLE_NAME_VERIFIED, SURNAME, SURNAME_VERIFIED, CURRENT_ADDRESS, CURRENT_ADDRESS_VERIFIED, ADDRESS_HISTORY, CYCLE_3));
        AttributeQuery attributeQuery = anAttributeQuery()
            .withId(REQUEST_ID)
            .withAttributes(requiredAttributes)
            .withIssuer(anIssuer().withIssuerId(getAppRule().getConfiguration().getHubEntityId()).build())
            .withSubject(aSubjectWithAssertions(asList(
                anAuthnStatementAssertion("default-request-id"),
                aCompleteMatchingDatasetAssertion(REQUEST_ID),
                AssertionBuilder.aCycle3DatasetAssertion("cycle3Name", "cycle3Value").withId(REQUEST_ID).withSignature(SignatureBuilder.aSignature().withDigestAlgorithm(REQUEST_ID, new DigestSHA256()).build()).buildUnencrypted()), REQUEST_ID, HUB_ENTITY_ID))
            .build();

        Response response = makeAttributeQueryRequest(UNKNOWN_USER_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);
        List<Assertion> decryptedAssertions = assertionDecrypter.decryptAssertions(response::getEncryptedAssertions);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(SUCCESS);
        assertThat(response.getStatus().getStatusCode().getStatusCode().getValue()).isEqualTo(CREATED);
        OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

        assertThatResponseContainsExpectedUserCreationAttributes(decryptedAssertions.get(0).getAttributeStatements(), List.of(
                userAccountCreationAttributeFor(aPersonNameValue().withValue("CurrentSurname").build(), SURNAME),
                userAccountCreationAttributeFor(aVerifiedValue().withValue(true).build(), SURNAME_VERIFIED),
                userAccountCreationAttributeFor(aPersonNameValue().withValue("FirstName").build(), FIRST_NAME),
                userAccountCreationAttributeFor(aVerifiedValue().withValue(false).build(), FIRST_NAME_VERIFIED),
                userAccountCreationAttributeFor(anAddressAttributeValue().addLines(List.of("address line 1")).withVerified(false).build(), CURRENT_ADDRESS),
                userAccountCreationAttributeFor(aVerifiedValue().withValue(false).build(), CURRENT_ADDRESS_VERIFIED),
                userAccountCreationAttributeFor(
                        asList(
                                anAddressAttributeValue().addLines(List.of("address line 1")).withVerified(false).build(),
                                anAddressAttributeValue().addLines(List.of("address line 2")).withVerified(true).build()
                        ),
                        ADDRESS_HISTORY),
                userAccountCreationAttributeFor(openSamlXmlObjectFactory.createSimpleMdsAttributeValue("cycle3Value"), CYCLE_3)
        ));
        assertThat(response.getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(response.getIssuer().getValue()).isEqualTo(TEST_RP_MS);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }

    @Test
    void shouldReturnCurrentVerifiedWhenPassedMultipleCurrentAttributes() {
        List<Attribute> requiredAttributes = attributesFromUacAttributes(Stream.of(FIRST_NAME, FIRST_NAME_VERIFIED));
        Assertion datasetAssertion = aMatchingDatasetAssertion(asList(
                aPersonName_1_1().addValue(aPersonNameValue().withValue("OldUnverifiedFirstName").withFrom(LocalDate.of(1980, 1, 30)).withTo(LocalDate.of(1990, 1, 29)).withVerified(false).build()).buildAsFirstname(),
                aPersonName_1_1().addValue(aPersonNameValue().withValue("OldVerifiedFirstName").withFrom(LocalDate.of(1990, 1, 30)).withTo(LocalDate.of(2000, 1, 29)).withVerified(true).build()).buildAsFirstname(),
                aPersonName_1_1().addValue(aPersonNameValue().withValue("CurrentUnverifiedFirstName").withFrom(LocalDate.of(2000, 1, 30)).withVerified(false).build()).buildAsFirstname(),
                aPersonName_1_1().addValue(aPersonNameValue().withValue("CurrentVerifiedFirstName").withFrom(LocalDate.of(2010, 1, 30)).withVerified(true).build()).buildAsFirstname()
        ), false, REQUEST_ID);
        AttributeQuery attributeQuery = anAttributeQuery()
                .withId(REQUEST_ID)
                .withAttributes(requiredAttributes)
                .withIssuer(anIssuer().withIssuerId(getAppRule().getConfiguration().getHubEntityId()).build())
                .withSubject(aSubjectWithAssertions(asList(
                        anAuthnStatementAssertion("default-request-id"),
                        datasetAssertion), REQUEST_ID, HUB_ENTITY_ID))
                .build();

        Response response = makeAttributeQueryRequest(UNKNOWN_USER_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);
        List<Assertion> decryptedAssertions = assertionDecrypter.decryptAssertions(response::getEncryptedAssertions);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(SUCCESS);
        assertThat(response.getStatus().getStatusCode().getStatusCode().getValue()).isEqualTo(CREATED);
        assertThatResponseContainsExpectedUserCreationAttributes(decryptedAssertions.get(0).getAttributeStatements(), List.of(
                userAccountCreationAttributeFor(aPersonNameValue().withValue("CurrentVerifiedFirstName").build(), FIRST_NAME),
                userAccountCreationAttributeFor(aVerifiedValue().withValue(true).build(), FIRST_NAME_VERIFIED)
        ));
        assertThat(response.getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(response.getIssuer().getValue()).isEqualTo(TEST_RP_MS);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }

    @Test
    void shouldNotCareAboutDateWhenPassedMultipleCurrentAttributes() {
        List<Attribute> requiredAttributes = attributesFromUacAttributes(Stream.of(SURNAME, SURNAME_VERIFIED));
        Assertion datasetAssertion = aMatchingDatasetAssertion(asList(
                aPersonName_1_1().addValue(aPersonNameValue().withValue("OldUnverifiedSurname").withFrom(LocalDate.of(1980, 1, 30)).withTo(LocalDate.of(1990, 1, 29)).withVerified(false).build()).buildAsSurname(),
                aPersonName_1_1().addValue(aPersonNameValue().withValue("OldVerifiedSurname").withFrom(LocalDate.of(1990, 1, 30)).withTo(LocalDate.of(2000, 1, 29)).withVerified(true).build()).buildAsSurname(),
                aPersonName_1_1().addValue(aPersonNameValue().withValue("FirstOlderCurrentUnverifiedSurname").withFrom(LocalDate.of(2000, 1, 30)).withVerified(false).build()).buildAsSurname(),
                aPersonName_1_1().addValue(aPersonNameValue().withValue("SecondNewerCurrentUnverifiedSurname").withFrom(LocalDate.of(2010, 1, 30)).withVerified(false).build()).buildAsSurname()
        ), false, REQUEST_ID);
        AttributeQuery attributeQuery = anAttributeQuery()
                .withId(REQUEST_ID)
                .withAttributes(requiredAttributes)
                .withIssuer(anIssuer().withIssuerId(getAppRule().getConfiguration().getHubEntityId()).build())
                .withSubject(aSubjectWithAssertions(asList(
                        anAuthnStatementAssertion("default-request-id"),
                        datasetAssertion), REQUEST_ID, HUB_ENTITY_ID))
                .build();

        Response response = makeAttributeQueryRequest(UNKNOWN_USER_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);
        List<Assertion> decryptedAssertions = assertionDecrypter.decryptAssertions(response::getEncryptedAssertions);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(SUCCESS);
        assertThat(response.getStatus().getStatusCode().getStatusCode().getValue()).isEqualTo(CREATED);
        assertThatResponseContainsExpectedUserCreationAttributes(decryptedAssertions.get(0).getAttributeStatements(), List.of(
                userAccountCreationAttributeFor(aPersonNameValue().withValue("FirstOlderCurrentUnverifiedSurname").build(), SURNAME),
                userAccountCreationAttributeFor(aVerifiedValue().withValue(false).build(), SURNAME_VERIFIED)
        ));
        assertThat(response.getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(response.getIssuer().getValue()).isEqualTo(TEST_RP_MS);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }

    @Test
    void shouldReturnVerifiedAddressWhenPassedMultipleCurrentAddresses() {
        List<Attribute> requiredAttributes = attributesFromUacAttributes(Stream.of(CURRENT_ADDRESS, CURRENT_ADDRESS_VERIFIED, ADDRESS_HISTORY));
        Assertion datasetAssertion = aMatchingDatasetAssertion(asList(
                AddressAttributeBuilder_1_1.anAddressAttribute()
                        .addAddress(new AddressAttributeValueBuilder_1_1().addLines(List.of("first current, unverified address")).withVerified(false).build())
                        .addAddress(new AddressAttributeValueBuilder_1_1().addLines(List.of("second current, verified address")).withVerified(true).build())
                        .buildCurrentAddress(),
                AddressAttributeBuilder_1_1.anAddressAttribute()
                        .addAddress(new AddressAttributeValueBuilder_1_1().addLines(List.of("old verified address")).withVerified(true).build())
                        .buildPreviousAddress()
        ), false, REQUEST_ID);
        AttributeQuery attributeQuery = anAttributeQuery()
                .withId(REQUEST_ID)
                .withAttributes(requiredAttributes)
                .withIssuer(anIssuer().withIssuerId(getAppRule().getConfiguration().getHubEntityId()).build())
                .withSubject(aSubjectWithAssertions(asList(
                        anAuthnStatementAssertion("default-request-id"),
                        datasetAssertion), REQUEST_ID, HUB_ENTITY_ID))
                .build();

        Response response = makeAttributeQueryRequest(UNKNOWN_USER_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);
        List<Assertion> decryptedAssertions = assertionDecrypter.decryptAssertions(response::getEncryptedAssertions);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(SUCCESS);
        assertThat(response.getStatus().getStatusCode().getStatusCode().getValue()).isEqualTo(CREATED);
        assertThatResponseContainsExpectedUserCreationAttributes(decryptedAssertions.get(0).getAttributeStatements(), List.of(
                userAccountCreationAttributeFor(anAddressAttributeValue().addLines(List.of("second current, verified address")).withVerified(true).build(), CURRENT_ADDRESS),
                userAccountCreationAttributeFor(aVerifiedValue().withValue(true).build(), CURRENT_ADDRESS_VERIFIED),
                userAccountCreationAttributeFor(
                        asList(
                                anAddressAttributeValue().addLines(List.of("first current, unverified address")).withVerified(false).build(),
                                anAddressAttributeValue().addLines(List.of("second current, verified address")).withVerified(true).build(),
                                anAddressAttributeValue().addLines(List.of("old verified address")).withVerified(true).build()
                        ),
                        ADDRESS_HISTORY)
        ));
        assertThat(response.getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(response.getIssuer().getValue()).isEqualTo(TEST_RP_MS);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }

    @Test
    void shouldPreserveOrderWhenPassedMultipleCurrentAddressAttributes() {
        List<Attribute> requiredAttributes = attributesFromUacAttributes(Stream.of(CURRENT_ADDRESS, CURRENT_ADDRESS_VERIFIED, ADDRESS_HISTORY));
        Assertion datasetAssertion = aMatchingDatasetAssertion(asList(
                AddressAttributeBuilder_1_1.anAddressAttribute()
                        .addAddress(new AddressAttributeValueBuilder_1_1().addLines(List.of("first current, unverified address")).withVerified(false).build())
                        .addAddress(new AddressAttributeValueBuilder_1_1().addLines(List.of("second current, unverified address")).withVerified(false).build())
                        .buildCurrentAddress(),
                AddressAttributeBuilder_1_1.anAddressAttribute()
                        .addAddress(new AddressAttributeValueBuilder_1_1().addLines(List.of("old, verified address")).withVerified(true).build())
                        .buildPreviousAddress()
        ), false, REQUEST_ID);
        AttributeQuery attributeQuery = anAttributeQuery()
                .withId(REQUEST_ID)
                .withAttributes(requiredAttributes)
                .withIssuer(anIssuer().withIssuerId(getAppRule().getConfiguration().getHubEntityId()).build())
                .withSubject(aSubjectWithAssertions(asList(
                        anAuthnStatementAssertion("default-request-id"),
                        datasetAssertion), REQUEST_ID, HUB_ENTITY_ID))
                .build();

        Response response = makeAttributeQueryRequest(UNKNOWN_USER_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);
        List<Assertion> decryptedAssertions = assertionDecrypter.decryptAssertions(response::getEncryptedAssertions);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(SUCCESS);
        assertThat(response.getStatus().getStatusCode().getStatusCode().getValue()).isEqualTo(CREATED);
        assertThatResponseContainsExpectedUserCreationAttributes(decryptedAssertions.get(0).getAttributeStatements(), List.of(
                userAccountCreationAttributeFor(anAddressAttributeValue().addLines(List.of("first current, unverified address")).withVerified(false).build(), CURRENT_ADDRESS),
                userAccountCreationAttributeFor(aVerifiedValue().withValue(false).build(), CURRENT_ADDRESS_VERIFIED),
                userAccountCreationAttributeFor(
                        asList(
                                anAddressAttributeValue().addLines(List.of("first current, unverified address")).withVerified(false).build(),
                                anAddressAttributeValue().addLines(List.of("second current, unverified address")).withVerified(true).build(),
                                anAddressAttributeValue().addLines(List.of("old, verified address")).withVerified(true).build()
                        ),
                        ADDRESS_HISTORY)
        ));
        assertThat(response.getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(response.getIssuer().getValue()).isEqualTo(TEST_RP_MS);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }


    @Test
    void shouldReturnFailureResponseWhenAttributesRequestedDoNotExist(){
        List<Attribute> requiredAttributes = attributesFromUacAttributes(Stream.of(FIRST_NAME, MIDDLE_NAME));
        AttributeQuery attributeQuery = anAttributeQuery()
                .withId(REQUEST_ID)
                .withAttributes(requiredAttributes)
                .withIssuer(anIssuer().withIssuerId(getAppRule().getConfiguration().getHubEntityId()).build())
                .withSubject(aSubjectWithAssertions(asList(
                        anAuthnStatementAssertion("default-request-id"),
                        assertionWithOnlyFirstName(REQUEST_ID)), REQUEST_ID, HUB_ENTITY_ID))
                .build();

        Response response = makeAttributeQueryRequest(UNKNOWN_USER_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);
        List<Assertion> decryptedAssertions = assertionDecrypter.decryptAssertions(response::getEncryptedAssertions);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(RESPONDER);
        assertThat(decryptedAssertions).hasSize(0);
        assertThat(response.getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(response.getIssuer().getValue()).isEqualTo(TEST_RP_MS);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }

    @Test
    void shouldReturnResponderStatusCodeWhenLocalMatchingServiceIsDown() {
        localMatchingService.reset();
        localMatchingService.register(UNKNOWN_USER_MATCHING_PATH, 200, "application/json", "{\"result\": \"failure\"}");

        List<Attribute> requiredAttributes = singletonList(new AttributeFactory(new OpenSamlXmlObjectFactory()).createAttribute(FIRST_NAME));

        AttributeQuery attributeQuery = anAttributeQuery()
                .withId(REQUEST_ID)
                .withAttributes(requiredAttributes)
                .withIssuer(anIssuer().withIssuerId(getAppRule().getConfiguration().getHubEntityId()).build())
                .withSubject(aSubjectWithAssertions(asList(
                        anAuthnStatementAssertion("default-request-id"),
                        assertionWithOnlyFirstName(REQUEST_ID)
                ), REQUEST_ID, HUB_ENTITY_ID))
                .build();


        Response response = makeAttributeQueryRequest(UNKNOWN_USER_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(RESPONDER);
        assertThat(response.getStatus().getStatusCode().getStatusCode().getValue()).isEqualTo(CREATE_FAILURE);
        assertThat(response.getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(response.getIssuer().getValue()).isEqualTo(TEST_RP_MS);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }

    private List<Attribute> attributesFromUacAttributes(Stream<UserAccountCreationAttribute> uacAttributes) {
        return uacAttributes
                .map(userAccountCreationAttribute -> new AttributeFactory(new OpenSamlXmlObjectFactory()).createAttribute(userAccountCreationAttribute))
                .collect(toList());
    }

    protected Attribute userAccountCreationAttributeFor(AttributeValue attributeValue, UserAccountCreationAttribute userAccountCreationAttribute) {
        return aUserAccountCreationAttributeValue().addValue(attributeValue).buildAsAttribute(userAccountCreationAttribute);
    }

    protected Attribute userAccountCreationAttributeFor(List<AttributeValue> attributeValues, UserAccountCreationAttribute userAccountCreationAttribute) {
        UserAccountCreationValueAttributeBuilder attributeBuilder = aUserAccountCreationAttributeValue();
        for(AttributeValue attributeValue : attributeValues) {
            attributeBuilder.addValue(attributeValue);
        }
        return attributeBuilder.buildAsAttribute(userAccountCreationAttribute);
    }
}
