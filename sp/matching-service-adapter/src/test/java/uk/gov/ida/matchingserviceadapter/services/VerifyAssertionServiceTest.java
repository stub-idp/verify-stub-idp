package uk.gov.ida.matchingserviceadapter.services;

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
import stubidp.saml.extensions.extensions.IdaAuthnContext;
import stubidp.saml.security.SamlAssertionsSignatureValidator;
import stubidp.saml.security.validators.ValidatedAssertions;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.TestCredentialFactory;
import stubidp.saml.test.builders.AssertionBuilder;
import stubidp.saml.utils.core.transformers.VerifyMatchingDatasetUnmarshaller;
import stubidp.saml.utils.core.transformers.inbound.Cycle3DatasetFactory;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import uk.gov.ida.matchingserviceadapter.domain.AssertionData;
import uk.gov.ida.matchingserviceadapter.validators.IdpConditionsValidator;
import uk.gov.ida.matchingserviceadapter.validators.InstantValidator;
import uk.gov.ida.matchingserviceadapter.validators.SubjectValidator;

import java.time.Instant;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static stubidp.saml.domain.assertions.AuthnContext.LEVEL_2;
import static stubidp.saml.test.builders.AssertionBuilder.aCycle3DatasetAssertion;
import static stubidp.saml.test.builders.AssertionBuilder.anAssertion;
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
import static stubidp.test.devpki.TestEntityIds.HUB_ENTITY_ID;
import static stubidp.test.devpki.TestEntityIds.STUB_IDP_ONE;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.anAttributeStatement;

@ExtendWith(MockitoExtension.class)
public class VerifyAssertionServiceTest extends OpenSAMLRunner {

    private VerifyAssertionService verifyAssertionService;

    @Mock
    private InstantValidator instantValidator;

    @Mock
    private SubjectValidator subjectValidator;

    @Mock
    private IdpConditionsValidator conditionsValidator;

    @Mock
    private SamlAssertionsSignatureValidator hubSignatureValidator;

    @Mock
    private VerifyMatchingDatasetUnmarshaller verifyMatchingDatasetUnmarshaller;

    @Mock
    private Cycle3DatasetFactory cycle3DatasetFactory;

    @BeforeEach
    public void setUp() throws Exception {
        verifyAssertionService = new VerifyAssertionService(
                instantValidator,
                subjectValidator,
                conditionsValidator,
                hubSignatureValidator,
                cycle3DatasetFactory,
                HUB_ENTITY_ID,
                verifyMatchingDatasetUnmarshaller
        );
    }

    @Test
    public void shouldThrowExceptionIfIssueInstantMissingWhenValidatingHubAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssueInstant(null);

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> verifyAssertionService.validateHubAssertion(assertion, "not-used", "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME))
                .withMessage("Assertion IssueInstant is missing.");
    }

    @Test
    public void shouldThrowExceptionIfAssertionIdIsMissingWhenValidatingHubAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setID(null);

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> verifyAssertionService.validateHubAssertion(assertion, "not-used", "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME))
                .withMessage("Assertion Id is missing or blank.");
    }

    @Test
    public void shouldThrowExceptionIfAssertionIdIsBlankWhenValidatingHubAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setID("");

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> verifyAssertionService.validateHubAssertion(assertion, "not-used", "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME))
                .withMessage("Assertion Id is missing or blank.");
    }

    @Test
    public void shouldThrowExceptionIfIssuerMissingWhenValidatingHubAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssuer(null);

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> verifyAssertionService.validateHubAssertion(assertion, "not-used", "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME))
                .withMessage("Assertion with id mds-assertion has missing or blank Issuer.");
    }

    @Test
    public void shouldThrowExceptionIfIssuerValueMissingWhenValidatingHubAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssuer(anIssuer().withIssuerId(null).build());

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> verifyAssertionService.validateHubAssertion(assertion, "not-used", "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME))
                .withMessage("Assertion with id mds-assertion has missing or blank Issuer.");
    }

    @Test
    public void shouldThrowExceptionIfIssuerValueIsBlankWhenValidatingHubAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssuer(anIssuer().withIssuerId("").build());

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> verifyAssertionService.validateHubAssertion(assertion, "not-used", "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME))
                .withMessage("Assertion with id mds-assertion has missing or blank Issuer.");
    }

    @Test
    public void shouldThrowExceptionIfMissingAssertionVersionWhenValidatingHubAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setVersion(null);

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> verifyAssertionService.validateHubAssertion(assertion, "not-used", "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME))
                .withMessage("Assertion with id mds-assertion has missing Version.");
    }


    @Test
    public void shouldThrowExceptionIfAssertionVersionInvalidWhenValidatingHubAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setVersion(SAMLVersion.VERSION_10);

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> verifyAssertionService.validateHubAssertion(assertion, "not-used", "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME))
                .withMessage("Assertion with id mds-assertion declared an illegal Version attribute value.");
    }

    @Test
    public void shouldCallValidatorsCorrectly() {
        when(hubSignatureValidator.validate(any(), any())).thenReturn(mock(ValidatedAssertions.class));

        List<Assertion> assertions = asList(
                aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted(),
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, "requestId").buildUnencrypted());

        verifyAssertionService.validate("requestId", assertions);
        verify(subjectValidator, times(2)).validate(any(), any());
        verify(hubSignatureValidator, times(2)).validate(any(), any());
    }

    @Test
    public void shouldTranslateVerifyAssertion() {
        Assertion mdsAssertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        Assertion cycle3Assertion = aCycle3DatasetAssertion("NI", "123456").buildUnencrypted();
        List<Assertion> assertions = asList(
                mdsAssertion,
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, "requestId").buildUnencrypted(),
                cycle3Assertion);
        AssertionData assertionData = verifyAssertionService.translate(assertions);

        verify(verifyMatchingDatasetUnmarshaller, times(1)).fromAssertion(mdsAssertion);
        verify(cycle3DatasetFactory, times(1)).createCycle3DataSet(cycle3Assertion);
        assertThat(assertionData.getLevelOfAssurance()).isEqualTo(LEVEL_2);
        assertThat(assertionData.getMatchingDatasetIssuer()).isEqualTo(STUB_IDP_ONE);

    }

    public static AssertionBuilder aMatchingDatasetAssertionWithSignature(List<Attribute> attributes, Signature signature, String requestId) {
        return anAssertion()
                .withId("mds-assertion")
                .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build())
                .withSubject(anAssertionSubject(requestId))
                .withSignature(signature)
                .addAttributeStatement(anAttributeStatement().addAllAttributes(attributes).build())
                .withConditions(aConditions().build());
    }

    public static AssertionBuilder anAuthnStatementAssertion(String authnContext, String inResponseTo) {
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

    public static Subject anAssertionSubject(final String inResponseTo) {
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

    public static Signature anIdpSignature() {
        return aSignature().withSigningCredential(
                new TestCredentialFactory(STUB_IDP_PUBLIC_PRIMARY_CERT, STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY)
                        .getSigningCredential()).build();

    }
}