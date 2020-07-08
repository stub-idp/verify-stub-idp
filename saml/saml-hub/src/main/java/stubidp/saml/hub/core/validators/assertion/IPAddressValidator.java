package stubidp.saml.hub.core.validators.assertion;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.IPAddress;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;

import java.util.Objects;

public class IPAddressValidator {
    public void validate(Assertion assertion) {
        for (AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
            for (Attribute attribute : attributeStatement.getAttributes()) {
                if (attribute.getName().equals(IdaConstants.Attributes_1_1.IPAddress.NAME)) {
                    IPAddress ipAddressAttributeValue = (IPAddress) attribute.getAttributeValues().get(0);
                    String addressValue = ipAddressAttributeValue.getValue();
                    if (Objects.nonNull(addressValue) && !addressValue.isBlank()) {
                        return;
                    }

                    SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.emptyIPAddress(assertion.getID());
                    throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
                }
            }
        }
        SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.missingIPAddress(assertion.getID());
        throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
    }
}
