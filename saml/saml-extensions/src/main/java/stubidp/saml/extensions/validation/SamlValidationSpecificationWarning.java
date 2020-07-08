package stubidp.saml.extensions.validation;

public abstract class SamlValidationSpecificationWarning extends SamlValidationSpecification {
    protected SamlValidationSpecificationWarning(String message, Boolean contextExpected) {
        super(message, contextExpected);
    }
}
