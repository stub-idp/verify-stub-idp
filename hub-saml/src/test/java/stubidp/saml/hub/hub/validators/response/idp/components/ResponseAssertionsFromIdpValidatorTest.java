package stubidp.saml.hub.hub.validators.response.idp.components;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.hub.core.validators.assertion.AuthnStatementAssertionValidator;
import stubidp.saml.hub.core.validators.assertion.IPAddressValidator;
import stubidp.saml.hub.core.validators.assertion.IdentityProviderAssertionValidator;
import stubidp.saml.hub.core.validators.assertion.MatchingDatasetAssertionValidator;
import stubidp.saml.security.validators.ValidatedAssertions;
import stubidp.saml.security.validators.ValidatedResponse;
import stubidp.test.devpki.TestEntityIds;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static stubidp.saml.utils.core.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.utils.core.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static stubidp.saml.utils.core.test.builders.MatchingDatasetAttributeStatementBuilder_1_1.aMatchingDatasetAttributeStatement_1_1;
import static stubidp.saml.utils.core.test.builders.ResponseBuilder.aResponse;

@ExtendWith(MockitoExtension.class)
public class ResponseAssertionsFromIdpValidatorTest {

    @Mock
    private IdentityProviderAssertionValidator assertionValidator;
    @Mock
    private MatchingDatasetAssertionValidator matchingDatasetAssertionValidator;
    @Mock
    private AuthnStatementAssertionValidator authnStatementValidator;
    @Mock
    private IPAddressValidator ipAddressValidator;

    private ResponseAssertionsFromIdpValidator validator;

    @BeforeEach
    public void setUp() throws Exception {
        validator = new ResponseAssertionsFromIdpValidator(
                assertionValidator,
                matchingDatasetAssertionValidator,
                authnStatementValidator,
                ipAddressValidator,
                TestEntityIds.HUB_ENTITY_ID
        );
    }

    @Test
    public void validate_shouldDelegateAuthnStatementAssertionValidation() throws Exception {
        Response response = aResponse()
                .addEncryptedAssertion(anAssertion().addAttributeStatement(aMatchingDatasetAttributeStatement_1_1().build()).build())
                .addEncryptedAssertion(anAssertion().build())
                .build();
        Assertion authNAssertion = anAssertion().buildUnencrypted();
        Assertion mdsAssertion = anAssertion().addAttributeStatement(aMatchingDatasetAttributeStatement_1_1().build()).buildUnencrypted();
        List<Assertion> assertions = asList(mdsAssertion, authNAssertion);

        validator.validate(new ValidatedResponse(response), new ValidatedAssertions(assertions));

        verify(authnStatementValidator).validate(authNAssertion);
    }

    @Test
    public void validate_shouldDelegateMatchingDatasetAssertionValidation() throws Exception {
        Response response = aResponse()
                .addEncryptedAssertion(anAssertion().addAttributeStatement(aMatchingDatasetAttributeStatement_1_1().build()).build())
                .addEncryptedAssertion(anAssertion().build())
                .build();
        Assertion authNAssertion = anAssertion().buildUnencrypted();
        Assertion mdsAssertion = anAssertion().addAttributeStatement(aMatchingDatasetAttributeStatement_1_1().build()).buildUnencrypted();
        List<Assertion> assertions = asList(mdsAssertion, authNAssertion);

        validator.validate(new ValidatedResponse(response), new ValidatedAssertions(assertions));

        verify(matchingDatasetAssertionValidator).validate(mdsAssertion, response.getIssuer().getValue());
    }

    @Test
    public void validate_shouldThrowExceptionIfMatchingDatasetStatementElementIsMissing() throws Exception {
        final Response response = aResponse()
                .addEncryptedAssertion(anAssertion().addAuthnStatement(anAuthnStatement().build()).build())
                .addEncryptedAssertion(anAssertion().build()).build();
        List<Assertion> assertions = asList(anAssertion().addAuthnStatement(anAuthnStatement().build()).buildUnencrypted(), anAssertion().buildUnencrypted());

        validateThrows(response, assertions, SamlTransformationErrorFactory.missingMatchingMds());
    }

    @Test
    public void validate_shouldThrowExceptionIfAuthnStatementAssertionIsMissing() throws Exception {
        Response response = aResponse()
                .addEncryptedAssertion(anAssertion().addAttributeStatement(aMatchingDatasetAttributeStatement_1_1().build()).build())
                .addEncryptedAssertion(anAssertion().addAttributeStatement(aMatchingDatasetAttributeStatement_1_1().build()).build())
                .build();
        List<Assertion> assertions = asList(
                anAssertion().addAttributeStatement(aMatchingDatasetAttributeStatement_1_1().build()).buildUnencrypted(),
                anAssertion().addAttributeStatement(aMatchingDatasetAttributeStatement_1_1().build()).buildUnencrypted()
        );
        validateThrows(response, assertions, SamlTransformationErrorFactory.missingAuthnStatement());
    }

    @Test
    public void validate_shouldThrowExceptionIfThereAreMultipleAuthnStatementsWithinTheAuthnStatementAssertionPresent() throws Exception {
        Response response = aResponse()
                .addEncryptedAssertion(anAssertion().addAttributeStatement(aMatchingDatasetAttributeStatement_1_1().build()).build())
                .addEncryptedAssertion(anAssertion().addAuthnStatement(anAuthnStatement().build()).addAuthnStatement(anAuthnStatement().build()).build())
                .build();
        List<Assertion> assertions = asList(
                anAssertion().addAttributeStatement(aMatchingDatasetAttributeStatement_1_1().build()).buildUnencrypted(),
                anAssertion().addAuthnStatement(anAuthnStatement().build()).addAuthnStatement(anAuthnStatement().build()).buildUnencrypted()
        );
        validateThrows(response, assertions, SamlTransformationErrorFactory.multipleAuthnStatements());
    }

    @Test
    public void validate_shouldDelegateToIpAddressValidator() throws Exception {
        Assertion authnStatementAssertion = anAssertion().addAuthnStatement(anAuthnStatement().build()).buildUnencrypted();
        Response response = aResponse()
                .addEncryptedAssertion(anAssertion().addAttributeStatement(aMatchingDatasetAttributeStatement_1_1().build()).build())
                .addEncryptedAssertion(anAssertion().addAuthnStatement(anAuthnStatement().build()).build())
                .build();
        List<Assertion> assertions = asList(anAssertion().addAttributeStatement(aMatchingDatasetAttributeStatement_1_1().build()).buildUnencrypted(), authnStatementAssertion);
        validator.validate(new ValidatedResponse(response), new ValidatedAssertions(assertions));
        verify(ipAddressValidator).validate(authnStatementAssertion);
    }

    private void validateThrows(Response response, List<Assertion> assertions, SamlValidationSpecificationFailure samlValidationSpecificationFailure) {
        final SamlTransformationErrorException e = Assertions.assertThrows(SamlTransformationErrorException.class, () -> validator.validate(new ValidatedResponse(response), new ValidatedAssertions(assertions)));
        assertThat(e.getMessage()).isEqualTo(samlValidationSpecificationFailure.getErrorMessage());
        assertThat(e.getLogLevel()).isEqualTo(samlValidationSpecificationFailure.getLogLevel());
    }
}
