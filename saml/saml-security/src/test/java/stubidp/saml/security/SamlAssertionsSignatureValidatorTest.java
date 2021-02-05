package stubidp.saml.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationResponse;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.builders.AssertionBuilder;
import stubidp.test.devpki.TestEntityIds;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static stubidp.saml.security.errors.SamlTransformationErrorFactory.invalidSignatureForAssertion;
import static stubidp.saml.test.builders.IssuerBuilder.anIssuer;

public class SamlAssertionsSignatureValidatorTest extends OpenSAMLRunner {
    private final String issuerId = TestEntityIds.HUB_ENTITY_ID;
    private final SigningCredentialFactory credentialFactory = new SigningCredentialFactory(new HardCodedKeyStore(issuerId));
    private final CredentialFactorySignatureValidator signatureValidator = new CredentialFactorySignatureValidator(credentialFactory);

    private SamlMessageSignatureValidator samlMessageSignatureValidator;
    private SamlAssertionsSignatureValidator samlAssertionsSignatureValidator;

    @BeforeEach
    void initSpy() {
        samlMessageSignatureValidator = spy(new SamlMessageSignatureValidator(signatureValidator));
        samlAssertionsSignatureValidator = new SamlAssertionsSignatureValidator(samlMessageSignatureValidator);
    }

    @Test
    void shouldValidateAllAssertions() {
        final Assertion assertion1 = AssertionBuilder.anAuthnStatementAssertion().buildUnencrypted();
        final Assertion assertion2 = AssertionBuilder.anAssertion().buildUnencrypted();
        final List<Assertion> assertions = asList(assertion1, assertion2);

        samlAssertionsSignatureValidator.validate(assertions, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);

        verify(samlMessageSignatureValidator).validate(assertion1, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        verify(samlMessageSignatureValidator).validate(assertion2, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldValidateEidasAssertionsWithAndWithoutSignature() {
        final Assertion assertion1 = AssertionBuilder.anAssertion().withSignature(null).buildUnencrypted();
        final Assertion assertion2 = AssertionBuilder.anAssertion().buildUnencrypted();
        final List<Assertion> assertions = asList(assertion1, assertion2);

        samlAssertionsSignatureValidator.validateEidas(assertions, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);

        verify(samlMessageSignatureValidator).validateEidasAssertion(assertion1, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        verify(samlMessageSignatureValidator).validateEidasAssertion(assertion2, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    void shouldFailOnFirstBadlySignedAssertion() {
        final Assertion assertion1 = AssertionBuilder.anAssertion().withoutSigning().buildUnencrypted();
        final Assertion assertion2 = AssertionBuilder.anAuthnStatementAssertion().buildUnencrypted();
        final List<Assertion> assertions = asList(assertion1, assertion2);

        Assertions.assertThrows(SamlTransformationErrorException.class, () -> samlAssertionsSignatureValidator.validate(assertions, IDPSSODescriptor.DEFAULT_ELEMENT_NAME));

        verify(samlMessageSignatureValidator).validate(assertion1, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        verify(samlMessageSignatureValidator, never()).validate(assertion2, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    void shouldFailOnAssertionSignedWithWrongIssuer() {
        final Assertion assertion = AssertionBuilder.anAuthnStatementAssertion().buildUnencrypted();
        when(samlMessageSignatureValidator.validate(assertion, IDPSSODescriptor.DEFAULT_ELEMENT_NAME)).thenReturn(SamlValidationResponse.aValidResponse());

        final Assertion badAssertion = AssertionBuilder
                .anAssertion()
                .withIssuer(anIssuer().withIssuerId(TestEntityIds.HUB_ENTITY_ID).build())
                .buildUnencrypted();

        final SamlValidationSpecificationFailure samlValidationSpecificationFailure = invalidSignatureForAssertion("ID");

        when(samlMessageSignatureValidator.validate(badAssertion, IDPSSODescriptor.DEFAULT_ELEMENT_NAME)).thenReturn(SamlValidationResponse.anInvalidResponse(samlValidationSpecificationFailure));
        final SamlTransformationErrorException exception = Assertions.assertThrows(SamlTransformationErrorException.class, () -> samlAssertionsSignatureValidator.validate(asList(assertion, badAssertion), IDPSSODescriptor.DEFAULT_ELEMENT_NAME));

        final String expected = "SAML Validation Specification: Signature for assertion ID was not valid.\n" +
                "DocumentReference{documentName='Hub Service Profile 1.1a', documentSection=''}";
        assertThat(exception.getMessage()).isEqualTo(expected);
    }
}
