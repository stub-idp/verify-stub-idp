package stubidp.saml.security.validators.issuer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDType;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.security.errors.SamlTransformationErrorFactory;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.builders.IssuerBuilder;
import stubidp.saml.test.support.SamlTransformationErrorManagerTestHelper;

public class IssuerValidatorTest extends OpenSAMLRunner {

    private IssuerValidator validator;

    @BeforeEach
    void setUp() {
        validator = new IssuerValidator();
    }

    @Test
    void validate_shouldThrowExceptionIfIssuerElementIsMissing() {
        assertExceptionMessage(null, SamlTransformationErrorFactory.missingIssuer());
    }

    @Test
    void validate_shouldThrowExceptionIfIssuerIdIsMissing() {
        Issuer assertionIssuer = IssuerBuilder.anIssuer().withIssuerId(null).build();

        assertExceptionMessage(assertionIssuer, SamlTransformationErrorFactory.emptyIssuer());
    }

    @Test
    void validate_shouldThrowExceptionIfIssuerFormatAttributeHasInvalidValue() {
        String invalidFormat = "invalid";
        Issuer assertionIssuer = IssuerBuilder.anIssuer().withFormat(invalidFormat).build();

        assertExceptionMessage(assertionIssuer, SamlTransformationErrorFactory.illegalIssuerFormat(invalidFormat, NameIDType.ENTITY));
    }

    @Test
    void validate_shouldDoNothingIfIssuerFormatAttributeIsMissing() {
        Issuer assertionIssuer = IssuerBuilder.anIssuer().withFormat(null).build();

        validator.validate(assertionIssuer);
    }

    @Test
    void validate_shouldDoNothingIfIssuerFormatAttributeHasValidValue() {
        Issuer assertionIssuer = IssuerBuilder.anIssuer().withFormat(NameIDType.ENTITY).build();

        validator.validate(assertionIssuer);
    }


    private void assertExceptionMessage(
            final Issuer assertionIssuer,
            SamlValidationSpecificationFailure failure) {

        SamlTransformationErrorManagerTestHelper.validateFail(
                () -> validator.validate(assertionIssuer),
                failure
        );
    }
}
