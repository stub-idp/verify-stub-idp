package stubidp.saml.hub.transformers.inbound.providers;

import org.opensaml.saml.saml2.core.Response;
import stubidp.saml.domain.response.InboundResponseFromIdp;
import stubidp.saml.hub.core.validators.DestinationValidator;
import stubidp.saml.hub.transformers.inbound.IdaResponseFromIdpUnmarshaller;
import stubidp.saml.hub.validators.response.idp.IdpResponseValidator;
import stubidp.saml.hub.validators.response.idp.components.EncryptedResponseFromIdpValidator;
import stubidp.saml.hub.validators.response.idp.components.ResponseAssertionsFromIdpValidator;
import stubidp.saml.security.AssertionDecrypter;
import stubidp.saml.security.SamlAssertionsSignatureValidator;
import stubidp.saml.security.validators.ValidatedAssertions;
import stubidp.saml.security.validators.ValidatedResponse;
import stubidp.saml.security.validators.signature.SamlResponseSignatureValidator;
import stubidp.saml.utils.core.transformers.IdpAssertionUnmarshaller;

import java.util.function.Function;

public class DecoratedSamlResponseToIdaResponseIssuedByIdpTransformer<T extends IdpAssertionUnmarshaller<O>, O> implements Function<Response, InboundResponseFromIdp<O>> {

    private final IdaResponseFromIdpUnmarshaller<T, O> idaResponseUnmarshaller;
    private final IdpResponseValidator idpResponseValidator;

    //Deprecated
    public DecoratedSamlResponseToIdaResponseIssuedByIdpTransformer(
            IdaResponseFromIdpUnmarshaller<T, O> idaResponseUnmarshaller,
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
                                                                    IdaResponseFromIdpUnmarshaller<T, O> idaResponseUnmarshaller){
        this.idaResponseUnmarshaller = idaResponseUnmarshaller;
        this.idpResponseValidator = idpResponseValidator;
    }

    @Override
    public InboundResponseFromIdp<O> apply(Response response) {
        this.idpResponseValidator.validate(response);
        ValidatedResponse validatedResponse = this.idpResponseValidator.getValidatedResponse();
        ValidatedAssertions validatedAssertions = this.idpResponseValidator.getValidatedAssertions();

        return idaResponseUnmarshaller.fromSaml(validatedResponse, validatedAssertions);
    }

}
