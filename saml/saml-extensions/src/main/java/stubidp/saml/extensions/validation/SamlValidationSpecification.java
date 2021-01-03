package stubidp.saml.extensions.validation;

import java.text.MessageFormat;

public abstract class SamlValidationSpecification {

    private final String message;
    private final boolean contextExpected;

    protected abstract SamlDocumentReference documentReference();

    SamlValidationSpecification(String message, boolean contextExpected) {
        this.message = message;
        this.contextExpected = contextExpected;
    }

    public String getErrorMessage(){
        return MessageFormat.format("SAML Validation Specification: {0}\n{1}", message, documentReference());
    }

    public boolean isContextExpected() {
        return contextExpected;
    }
}
