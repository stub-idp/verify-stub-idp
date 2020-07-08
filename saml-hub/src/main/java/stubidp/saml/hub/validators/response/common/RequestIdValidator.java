package stubidp.saml.hub.validators.response.common;

import org.opensaml.saml.saml2.core.Response;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.hub.exception.SamlValidationException;

public class RequestIdValidator {

    public static void validate(Response response) {
        String requestId = response.getInResponseTo();
        if (requestId == null) throw new SamlValidationException(SamlTransformationErrorFactory.missingInResponseTo());
        if (requestId.isEmpty()) throw new SamlValidationException(SamlTransformationErrorFactory.emptyInResponseTo());
    }
}
