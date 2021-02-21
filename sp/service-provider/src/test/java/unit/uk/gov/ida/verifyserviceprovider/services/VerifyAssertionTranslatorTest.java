package unit.uk.gov.ida.verifyserviceprovider.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.xmlsec.signature.Signature;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.extensions.extensions.IdaAuthnContext;
import stubidp.saml.hub.core.validators.assertion.AssertionAttributeStatementValidator;
import stubidp.saml.security.SamlAssertionsSignatureValidator;
import stubidp.saml.security.validators.ValidatedAssertions;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.TestCredentialFactory;
import stubidp.saml.test.builders.AssertionBuilder;
import stubidp.saml.utils.core.transformers.MatchingDatasetToNonMatchingAttributesMapper;
import stubidp.saml.utils.core.transformers.VerifyMatchingDatasetUnmarshaller;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import stubidp.saml.utils.hub.factories.UserIdHashFactory;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingScenario;
import uk.gov.ida.verifyserviceprovider.dto.TestTranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.services.AssertionClassifier;
import uk.gov.ida.verifyserviceprovider.services.VerifyAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.util.Lists.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static stubidp.saml.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static stubidp.saml.test.builders.AuthnContextBuilder.anAuthnContext;
import static stubidp.saml.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static stubidp.saml.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static stubidp.saml.test.builders.ConditionsBuilder.aConditions;
import static stubidp.saml.test.builders.IPAddressAttributeBuilder.anIPAddress;
import static stubidp.saml.test.builders.IssuerBuilder.anIssuer;
import static stubidp.saml.test.builders.SignatureBuilder.aSignature;
import static stubidp.saml.test.builders.SubjectBuilder.aSubject;
import static stubidp.saml.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static stubidp.saml.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;
import static stubidp.test.devpki.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT;
import static stubidp.test.devpki.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY;
import static stubidp.test.devpki.TestEntityIds.STUB_IDP_ONE;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_1;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;

@ExtendWith(MockitoExtension.class)
class VerifyAssertionTranslatorTest extends OpenSAMLRunner {

    private VerifyAssertionTranslator verifyAssertionService;

    @Mock
    private SubjectValidator subjectValidator;

    @Mock
    private SamlAssertionsSignatureValidator hubSignatureValidator;

    @Mock
    private AssertionAttributeStatementValidator attributeStatementValidator;

    @Mock
    private VerifyMatchingDatasetUnmarshaller verifyMatchingDatasetUnmarshaller;

    @Mock
    private LevelOfAssuranceValidator levelOfAssuranceValidator;

    @Mock
    private UserIdHashFactory userIdHashFactory;

    @Mock
    private MatchingDatasetToNonMatchingAttributesMapper matchingDatasetToNonMatchingAttributesMapper;

    @BeforeEach
    void setUp() throws Exception {
        verifyAssertionService = new VerifyAssertionTranslator(
                hubSignatureValidator,
                subjectValidator,
                attributeStatementValidator,
                verifyMatchingDatasetUnmarshaller,
                new AssertionClassifier(),
                matchingDatasetToNonMatchingAttributesMapper,
                levelOfAssuranceValidator,
                userIdHashFactory);
    }

    @Test
    void shouldThrowExceptionIfIssueInstantMissingWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssueInstant(null);

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> verifyAssertionService.validateIdpAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME))
                .withMessage("Assertion IssueInstant is missing.");
    }

    @Test
    void shouldThrowExceptionIfAssertionIdIsMissingWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setID(null);

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> verifyAssertionService.validateIdpAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME))
                .withMessage("Assertion Id is missing or blank.");
    }

    @Test
    void shouldThrowExceptionIfAssertionIdIsBlankWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setID("");

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> verifyAssertionService.validateIdpAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME))
                .withMessage("Assertion Id is missing or blank.");
    }

    @Test
    void shouldThrowExceptionIfIssuerMissingWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssuer(null);

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> verifyAssertionService.validateIdpAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME))
                .withMessage("Assertion with id mds-assertion has missing or blank Issuer.");
    }

    @Test
    void shouldThrowExceptionIfIssuerValueMissingWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssuer(anIssuer().withIssuerId(null).build());

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> verifyAssertionService.validateIdpAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME))
                .withMessage("Assertion with id mds-assertion has missing or blank Issuer.");
    }

    @Test
    void shouldThrowExceptionIfIssuerValueIsBlankWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssuer(anIssuer().withIssuerId("").build());

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> verifyAssertionService.validateIdpAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME))
                .withMessage("Assertion with id mds-assertion has missing or blank Issuer.");
    }

    @Test
    void shouldThrowExceptionIfMissingAssertionVersionWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setVersion(null);

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> verifyAssertionService.validateIdpAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME))
                .withMessage("Assertion with id mds-assertion has missing Version.");
    }


    @Test
    void shouldThrowExceptionIfAssertionVersionInvalidWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setVersion(SAMLVersion.VERSION_10);

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> verifyAssertionService.validateIdpAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME))
                .withMessage("Assertion with id mds-assertion declared an illegal Version attribute value.");
    }

    @Test
    void shouldNotThrowExceptionsWhenAssertionsAreValid() {
        doNothing().when(subjectValidator).validate(any(), any());
        when(hubSignatureValidator.validate(any(), any())).thenReturn(mock(ValidatedAssertions.class));

        Assertion authnAssertion = anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, "requestId").buildUnencrypted();
        Assertion mdsAssertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();

        verifyAssertionService.validate(authnAssertion, mdsAssertion, "requestId", LevelOfAssurance.LEVEL_1, LEVEL_2);

        verify(subjectValidator, times(2)).validate(any(), any());
        verify(hubSignatureValidator, times(2)).validate(any(), any());
        verify(levelOfAssuranceValidator, times(1)).validate(LEVEL_2, LEVEL_1);
    }

    @Test
    void shouldCorrectlyExtractLevelOfAssurance() {
        Assertion authnAssertion = anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, "requestId").buildUnencrypted();

        LevelOfAssurance loa = verifyAssertionService.extractLevelOfAssuranceFrom(authnAssertion);

        assertThat(loa).isEqualTo(LevelOfAssurance.LEVEL_2);
    }

    @Test
    void shouldThrowExceptionWhenLevelOfAssuranceNotPresent() {
        Assertion authnAssertion = anAuthnStatementAssertion(null, "requestId").buildUnencrypted();
        Assertion mdsAssertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> verifyAssertionService.translateSuccessResponse(List.of(authnAssertion, mdsAssertion), "requestId", LEVEL_2, "default-entity-id"))
                .withMessage("Expected a level of assurance.");
    }

    @Test
    void shouldThrowExceptionWithUnknownLevelOfAssurance() {
        Assertion authnAssertion = anAuthnStatementAssertion("unknown", "requestId").buildUnencrypted();
        Assertion mdsAssertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> verifyAssertionService.translateSuccessResponse(List.of(authnAssertion, mdsAssertion), "requestId", LEVEL_2, "default-entity-id"))
                .withMessage("Level of assurance 'unknown' is not supported.");
    }

    @Test
    void expectedHashContainedInResponseBodyWhenUserIdFactoryIsCalledOnce() {
        doNothing().when(subjectValidator).validate(any(), any());
        when(hubSignatureValidator.validate(any(), any())).thenReturn(mock(ValidatedAssertions.class));

        String requestId = "requestId";
        String expectedHashed = "a5fbea969c3837a712cbe9e188804796828f369106478e623a436fa07e8fd298";
        TestTranslatedNonMatchingResponseBody expectedNonMatchingResponseBody = new TestTranslatedNonMatchingResponseBody(NonMatchingScenario.IDENTITY_VERIFIED, expectedHashed, LEVEL_2, null);

        Assertion authnAssertion = anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, requestId).buildUnencrypted();
        Assertion mdsAssertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), requestId).buildUnencrypted();

        final String nameId = authnAssertion.getSubject().getNameID().getValue();
        final String issuerId = authnAssertion.getIssuer().getValue();

        when(userIdHashFactory.hashId(eq(issuerId), eq(nameId), eq(Optional.of(AuthnContext.LEVEL_2))))
                .thenReturn(expectedHashed);

        TranslatedNonMatchingResponseBody responseBody = verifyAssertionService.translateSuccessResponse(List.of(authnAssertion, mdsAssertion), "requestId", LEVEL_2, "default-entity-id");

        verify(userIdHashFactory, times(1)).hashId(issuerId, nameId, Optional.of(AuthnContext.LEVEL_2));
        assertThat(responseBody.toString()).contains(expectedNonMatchingResponseBody.getPid());
    }

    private static AssertionBuilder aMatchingDatasetAssertionWithSignature(List<Attribute> attributes, Signature signature, String requestId) {
        return anAssertion()
                .withId("mds-assertion")
                .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build())
                .withSubject(anAssertionSubject(requestId))
                .withSignature(signature)
                .addAttributeStatement(anAttributeStatement().addAllAttributes(attributes).build())
                .withConditions(aConditions().build());
    }

    private static AssertionBuilder anAuthnStatementAssertion(String authnContext, String inResponseTo) {
        return anAssertion()
                .addAuthnStatement(
                        anAuthnStatement()
                                .withAuthnContext(
                                        anAuthnContext()
                                                .withAuthnContextClassRef(
                                                        anAuthnContextClassRef()
                                                                .withAuthnContextClasRefValue(authnContext)
                                                                .build())
                                                .build())
                                .build())
                .withSubject(
                        aSubject()
                                .withSubjectConfirmation(
                                        aSubjectConfirmation()
                                                .withSubjectConfirmationData(
                                                        aSubjectConfirmationData()
                                                                .withInResponseTo(inResponseTo)
                                                                .build()
                                                ).build()
                                ).build())
                .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build())
                .addAttributeStatement(anAttributeStatement().addAttribute(anIPAddress().build()).build());
    }

    private static Subject anAssertionSubject(final String inResponseTo) {
        return aSubject()
                .withSubjectConfirmation(
                        aSubjectConfirmation()
                                .withSubjectConfirmationData(
                                        aSubjectConfirmationData()
                                                .withNotOnOrAfter(Instant.now())
                                                .withInResponseTo(inResponseTo)
                                                .build()
                                ).build()
                ).build();
    }

    private static Signature anIdpSignature() {
        return aSignature().withSigningCredential(
                new TestCredentialFactory(STUB_IDP_PUBLIC_PRIMARY_CERT, STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY)
                        .getSigningCredential()).build();

    }
}
