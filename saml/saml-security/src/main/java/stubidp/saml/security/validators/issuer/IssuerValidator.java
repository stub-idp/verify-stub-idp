package stubidp.saml.security.validators.issuer;

import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDType;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.security.errors.SamlTransformationErrorFactory;

import java.util.Objects;

public class IssuerValidator {

    public IssuerValidator() {
    }

    public void validate(Issuer assertionIssuer) {
        if (Objects.isNull(assertionIssuer)) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.missingIssuer();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }

        if (Objects.isNull(assertionIssuer.getValue()) || assertionIssuer.getValue().isBlank()) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.emptyIssuer();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }

        if (Objects.nonNull(assertionIssuer.getFormat()) && !NameIDType.ENTITY.equals(assertionIssuer.getFormat())) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.illegalIssuerFormat(assertionIssuer.getFormat(), NameIDType.ENTITY);
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
    }
}
