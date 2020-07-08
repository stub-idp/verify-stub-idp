package stubidp.saml.extensions.validation.errors;

import stubidp.saml.extensions.validation.SamlDocumentReference;
import stubidp.saml.extensions.validation.SamlValidationSpecificationWarning;

import static java.text.MessageFormat.format;

public class InvalidAttributeNameFormat extends SamlValidationSpecificationWarning {
    public InvalidAttributeNameFormat(String nameFormat) {
        super(format("''{0}'' is not a valid name format.", nameFormat), true);
    }

    @Override
    public SamlDocumentReference documentReference() {
        return SamlDocumentReference.idaAttributes11a("2.5");
    }
}
