package uk.gov.ida.integrationtest.interfacetests;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.security.credential.Credential;
import stubidp.saml.test.TestCredentialFactory;
import stubidp.saml.test.builders.AttributeQueryBuilder;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;

import static java.util.Arrays.asList;
import static stubidp.saml.test.builders.AssertionBuilder.anEidasAssertion;
import static stubidp.saml.test.builders.AuthnStatementBuilder.anEidasAuthnStatement;
import static stubidp.saml.test.builders.ConditionsBuilder.aConditions;
import static stubidp.saml.test.builders.IssuerBuilder.anIssuer;
import static stubidp.saml.test.builders.SignatureBuilder.aSignature;
import static stubidp.saml.test.builders.SubjectBuilder.aSubject;
import static stubidp.saml.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static stubidp.saml.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;
import static stubidp.test.devpki.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;
import static stubidp.test.devpki.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_MS_PUBLIC_ENCRYPTION_CERT;
import static stubidp.test.devpki.TestEntityIds.HUB_CONNECTOR_ENTITY_ID;
import static stubidp.test.devpki.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aSubjectWithEncryptedAssertions;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.anEidasSignature;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.aCurrentFamilyNameAttribute;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.aCurrentGivenNameAttribute;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.aDateOfBirthAttribute;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.aPersonIdentifierAttribute;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.matchingserviceadapter.configuration.MatchingServiceAdapterEnvironment.INTEGRATION;

@ExtendWith(DropwizardExtensionsSupport.class)
public class EidasExampleSchemaTests extends BaseTestToolInterfaceTest {
    private static final String REQUEST_ID = "default-match-id";
    private static final String PID = "default-pid";

    private static final Credential MSA_ENCRYPTION_CREDENTIAL = new TestCredentialFactory(
        TEST_RP_MS_PUBLIC_ENCRYPTION_CERT,
        null)
        .getEncryptingCredential();

    private static final Credential COUNTRY_SIGNING_CREDENTIAL = new TestCredentialFactory(
            STUB_COUNTRY_PUBLIC_PRIMARY_CERT,
            STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY)
            .getSigningCredential();

    public static final MatchingServiceAdapterAppExtension appRule = new MatchingServiceAdapterAppExtension(true, configRules);

    @Override
    protected MatchingServiceAdapterAppExtension getAppRule() { return appRule; }

    @Test
    public void shouldProduceLoA2StandardDataset() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithEncryptedAssertions(Collections.singletonList(
                    anEidasAssertion()
                            .withConditions(
                                    aConditions()
                                            .validFor(Duration.ofMinutes(10))
                                            .restrictedToAudience(appRule.getConfiguration().getEuropeanIdentity().getAllAcceptableHubConnectorEntityIds(INTEGRATION).stream().findFirst().get())
                                            .build())
                            .withIssuer(anIssuer().withIssuerId(appRule.getCountryEntityId()).build())
                            .withSignature(anEidasSignature())
                            .addAuthnStatement(anEidasAuthnStatement().build())
                            .withSubject(anEidasSubject(REQUEST_ID))
                            .withoutAttributeStatements()
                            .addAttributeStatement(anAttributeStatement().addAllAttributes(asList(
                                    aCurrentGivenNameAttribute("Joe"),
                                    aCurrentFamilyNameAttribute("Dou"),
                                    aDateOfBirthAttribute(LocalDate.of(1980, 5, 24)),
                                    aPersonIdentifierAttribute(PID)
                                    )).build()
                            ).buildWithEncrypterCredential(MSA_ENCRYPTION_CREDENTIAL)), REQUEST_ID, HUB_ENTITY_ID))
            .build();

        Path path = Paths.get("verify-matching-service-test-tool/src/main/resources/universal-dataset/eIDAS-LoA2-Standard_data_set.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, path);
    }

    @Test
    public void shouldProduceLoA2StandardDatasetWithTransliterationProvidedForNameFields() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithEncryptedAssertions(Collections.singletonList(
                    anEidasAssertion()
                            .withConditions(
                                    aConditions()
                                            .validFor(Duration.ofMinutes(10))
                                            .restrictedToAudience(appRule.getConfiguration().getEuropeanIdentity().getAllAcceptableHubConnectorEntityIds(INTEGRATION).stream().findFirst().get())
                                            .build())
                            .withIssuer(anIssuer().withIssuerId(appRule.getCountryEntityId()).build())
                            .withSignature(aSignature().withSigningCredential(COUNTRY_SIGNING_CREDENTIAL).build())
                            .addAuthnStatement(anEidasAuthnStatement().build())
                            .withSubject(anEidasSubject(REQUEST_ID))
                            .withoutAttributeStatements()
                            .addAttributeStatement(anAttributeStatement().addAllAttributes(asList(
                                    aCurrentGivenNameAttribute("Georgios", "Γεώργιος"),
                                    aCurrentFamilyNameAttribute("Panathinaikos", "Παναθηναϊκός"),
                                    aDateOfBirthAttribute(LocalDate.of(1980, 5, 24)),
                                    aPersonIdentifierAttribute(PID)
                                    )).build()
                            ).buildWithEncrypterCredential(MSA_ENCRYPTION_CREDENTIAL)), REQUEST_ID, HUB_ENTITY_ID))
            .build();

        Path path = Paths.get("verify-matching-service-test-tool/src/main/resources/universal-dataset/eIDAS-LoA2-Standard_data_set-transliteration_provided_for_name_fields.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, path);
    }

    @Test
    public void shouldProduceLoA2StandardDatasetWithSpecialCharactersInNameFields() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithEncryptedAssertions(Collections.singletonList(
                    anEidasAssertion()
                            .withConditions(
                                    aConditions()
                                            .validFor(Duration.ofMinutes(10))
                                            .restrictedToAudience(appRule.getConfiguration().getEuropeanIdentity().getAllAcceptableHubConnectorEntityIds(INTEGRATION).stream().findFirst().get())
                                            .build())
                            .withIssuer(anIssuer().withIssuerId(appRule.getCountryEntityId()).build())
                            .withSignature(aSignature().withSigningCredential(COUNTRY_SIGNING_CREDENTIAL).build())
                            .addAuthnStatement(anEidasAuthnStatement().build())
                            .withSubject(anEidasSubject(REQUEST_ID))
                            .withoutAttributeStatements()
                            .addAttributeStatement(anAttributeStatement().addAllAttributes(asList(
                                    aCurrentGivenNameAttribute("Šarlota"),
                                    aCurrentFamilyNameAttribute("Snježana"),
                                    aDateOfBirthAttribute(LocalDate.of(1980, 5, 24)),
                                    aPersonIdentifierAttribute(PID)
                                    )).build()
                            ).buildWithEncrypterCredential(MSA_ENCRYPTION_CREDENTIAL)), REQUEST_ID, HUB_ENTITY_ID))
            .build();

        Path path = Paths.get("verify-matching-service-test-tool/src/main/resources/universal-dataset/eIDAS-LoA2-Standard_data_set-special_characters_in_name_fields.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, path);
    }

    private Subject anEidasSubject(String inResponseTo) {
        return aSubject().withSubjectConfirmation(
                aSubjectConfirmation().withSubjectConfirmationData(aSubjectConfirmationData()
                        .withRecipient(HUB_CONNECTOR_ENTITY_ID)
                        .withInResponseTo(inResponseTo)
                        .build()).build()).build();
    }
}
