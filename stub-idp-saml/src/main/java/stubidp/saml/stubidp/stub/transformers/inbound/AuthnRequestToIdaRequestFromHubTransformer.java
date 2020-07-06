package stubidp.saml.stubidp.stub.transformers.inbound;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import stubidp.saml.security.validators.signature.SamlRequestSignatureValidator;
import stubidp.saml.domain.request.IdaAuthnRequestFromHub;

import java.util.function.Function;

public class AuthnRequestToIdaRequestFromHubTransformer implements Function<AuthnRequest, IdaAuthnRequestFromHub> {
    private final IdaAuthnRequestFromHubUnmarshaller idaAuthnRequestFromHubUnmarshaller;
    private final SamlRequestSignatureValidator<AuthnRequest> samlRequestSignatureValidator;

    public AuthnRequestToIdaRequestFromHubTransformer(
            IdaAuthnRequestFromHubUnmarshaller idaAuthnRequestFromHubUnmarshaller,
            SamlRequestSignatureValidator<AuthnRequest> samlRequestSignatureValidator) {

        this.idaAuthnRequestFromHubUnmarshaller = idaAuthnRequestFromHubUnmarshaller;
        this.samlRequestSignatureValidator = samlRequestSignatureValidator;
    }

    @Override
    public IdaAuthnRequestFromHub apply(final AuthnRequest authnRequest) {
        samlRequestSignatureValidator.validate(authnRequest, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        return idaAuthnRequestFromHubUnmarshaller.fromSaml(authnRequest);
    }

}
