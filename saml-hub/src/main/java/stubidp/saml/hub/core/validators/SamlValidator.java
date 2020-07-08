package stubidp.saml.hub.core.validators;


import org.opensaml.saml.common.SAMLObject;

public interface SamlValidator<T extends SAMLObject> {
    void validate(T itemToValidate);
}



