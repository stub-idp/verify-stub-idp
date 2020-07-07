package stubidp.saml.hub.hub.transformers.inbound.providers;

import org.opensaml.saml.saml2.core.Response;
import stubidp.saml.domain.response.InboundResponseFromIdp;
import stubidp.saml.hub.core.validators.DestinationValidator;
import stubidp.saml.hub.hub.transformers.inbound.IdaResponseFromIdpUnmarshaller;
import stubidp.saml.hub.hub.validators.response.idp.IdpResponseValidator;
import stubidp.saml.hub.hub.validators.response.idp.components.EncryptedResponseFromIdpValidator;
import stubidp.saml.hub.hub.validators.response.idp.components.ResponseAssertionsFromIdpValidator;
import stubidp.saml.security.AssertionDecrypter;
import stubidp.saml.security.SamlAssertionsSignatureValidator;
import stubidp.saml.security.validators.ValidatedAssertions;
import stubidp.saml.security.validators.ValidatedResponse;
import stubidp.saml.security.validators.signature.SamlResponseSignatureValidator;

import java.util.function.Function;

public class DecoratedSamlResponseToIdaResponseIssuedByIdpTransformer implements Function<Response, InboundResponseFromIdp> {

    private final IdaResponseFromIdpUnmarshaller idaResponseUnmarshaller;
    private IdpResponseValidator idpResponseValidator;

    //Deprecated
    public DecoratedSamlResponseToIdaResponseIssuedByIdpTransformer(
            IdaResponseFromIdpUnmarshaller idaResponseUnmarshaller,
            SamlResponseSignatureValidator samlResponseSignatureValidator,
            AssertionDecrypter assertionDecrypter,
            SamlAssertionsSignatureValidator samlAssertionsSignatureValidator,
            @SuppressWarnings("rawtypes") EncryptedResponseFromIdpValidator responseFromIdpValidator,
            DestinationValidator responseDestinationValidator,
            ResponseAssertionsFromIdpValidator responseAssertionsFromIdpValidator) {

        this(new IdpResponseValidator(
            samlResponseSignatureValidator,
            assertionDecrypter,
            samlAssertionsSignatureValidator,
            responseFromIdpValidator,
            responseDestinationValidator,
            responseAssertionsFromIdpValidator),
            idaResponseUnmarshaller);
    }

    public DecoratedSamlResponseToIdaResponseIssuedByIdpTransformer(IdpResponseValidator idpResponseValidator,
                                                                    IdaResponseFromIdpUnmarshaller idaResponseUnmarshaller){
        this.idaResponseUnmarshaller = idaResponseUnmarshaller;
        this.idpResponseValidator = idpResponseValidator;
    }

    @Override
    public InboundResponseFromIdp apply(Response response) {
        this.idpResponseValidator.validate(response);
        ValidatedResponse validatedResponse = this.idpResponseValidator.getValidatedResponse();
        ValidatedAssertions validatedAssertions = this.idpResponseValidator.getValidatedAssertions();

        return idaResponseUnmarshaller.fromSaml(validatedResponse, validatedAssertions);
    }

}
