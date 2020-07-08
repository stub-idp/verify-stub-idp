package stubidp.saml.extensions.validation.errors;

import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.validation.SamlDocumentReference;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;

import static java.text.MessageFormat.format;

public class InvalidAttributeLanguageInAssertion extends SamlValidationSpecificationFailure {
    public InvalidAttributeLanguageInAssertion(String name, String language) {
        super(format("'Language' for an attribute value with attribute name {0} was set to {1}, and must be set to {2}", name, language, IdaConstants.IDA_LANGUAGE), true);
    }

    @Override
    public SamlDocumentReference documentReference() {
        return SamlDocumentReference.idaAttributes11a("2.3");
    }
}
