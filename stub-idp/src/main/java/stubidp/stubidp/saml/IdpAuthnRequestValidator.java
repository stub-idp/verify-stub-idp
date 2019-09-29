package stubidp.stubidp.saml;

import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.SecurityException;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.saml.hub.core.validators.DestinationValidator;
import stubidp.saml.metadata.MetadataConfiguration;
import stubidp.saml.stubidp.configuration.SamlConfiguration;
import stubidp.stubidp.Urls;
import stubidp.stubidp.exceptions.InvalidAuthnRequestException;
import stubidp.stubidp.resources.idp.HeadlessIdpResource;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;

import static stubidp.stubidp.StubIdpBinder.HUB_METADATA_CONFIGURATION;
import static stubidp.stubidp.StubIdpBinder.HUB_METADATA_RESOLVER;

public class IdpAuthnRequestValidator extends BaseAuthnRequestValidator {

    private static final Logger LOG = LoggerFactory.getLogger(IdpAuthnRequestValidator.class);

    private final String expectedDestinationBaseUri;

    @Inject
    public IdpAuthnRequestValidator(
            @Named(HUB_METADATA_RESOLVER) MetadataResolver hubMetadataResolver,
            @Named(HUB_METADATA_CONFIGURATION) MetadataConfiguration hubMetadataConfiguration,
            SamlConfiguration samlConfiguration) {
        super(hubMetadataResolver, hubMetadataConfiguration.getSpTrustStore().get());
        this.expectedDestinationBaseUri = samlConfiguration.getExpectedDestinationHost().toASCIIString();
    }

    @Override
    protected DestinationValidator getDestinationValidator(String schemeId) {
        return new DestinationValidator(UriBuilder.fromUri(expectedDestinationBaseUri).build(),
                UriBuilder.fromPath(HeadlessIdpResource.IDP_NAME.equals(schemeId)?Urls.HEADLESS_ROOT:Urls.IDP_SAML2_SSO_RESOURCE).build(schemeId).getPath());
    }

    @Override
    protected void validateSpecificQualities(AuthnRequest request) {
        validateKeyInfo(request);
    }

    @Override
    public void validateSignature(AuthnRequest request) {
        try {
            if(!metadataBackedSignatureValidator.validate(request, request.getIssuer().getValue(), SPSSODescriptor.DEFAULT_ELEMENT_NAME)) {
                throw new InvalidAuthnRequestException("signature verification failed");
            }
        } catch (SignatureException | SecurityException e) {
            throw new InvalidAuthnRequestException(e);
        }
    }

    private void validateKeyInfo(AuthnRequest request) {
        if (request.getSignature().getKeyInfo() != null) {
            throw new InvalidAuthnRequestException("KeyInfo was not null");
        }
    }
}
