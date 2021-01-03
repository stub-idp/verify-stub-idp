package stubidp.stubidp.saml;

import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.SecurityException;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.saml.hub.core.validators.DestinationValidator;
import stubidp.stubidp.Urls;
import stubidp.stubidp.configuration.EuropeanIdentityConfiguration;
import stubidp.stubidp.exceptions.InvalidEidasAuthnRequestException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import java.util.Objects;

import static stubidp.stubidp.StubIdpEidasBinder.HUB_CONNECTOR_METADATA_RESOLVER;

public class EidasAuthnRequestValidator extends BaseAuthnRequestValidator {

    private static final Logger LOG = LoggerFactory.getLogger(EidasAuthnRequestValidator.class);

    private final String expectedDestinationBaseUri;

    @Inject
    public EidasAuthnRequestValidator(
            @Named(HUB_CONNECTOR_METADATA_RESOLVER) MetadataResolver hubConnectorMetadataResolver,
            EuropeanIdentityConfiguration europeanIdentityConfiguration) {
        super(hubConnectorMetadataResolver, europeanIdentityConfiguration.getMetadata().getSpTrustStore().get());
        this.expectedDestinationBaseUri = europeanIdentityConfiguration.getStubCountryBaseUrl();
    }

    @Override
    protected DestinationValidator getDestinationValidator(String schemeId) {
        return new DestinationValidator(UriBuilder.fromUri(expectedDestinationBaseUri).build(),
                UriBuilder.fromPath(Urls.EIDAS_SAML2_SSO_RESOURCE).build(schemeId).getPath());
    }

    @Override
    protected void validateSpecificQualities(AuthnRequest request) {
        validateKeyInfo(request);
    }

    @Override
    public void validateSignature(AuthnRequest request) {
        try {
            if(!metadataBackedSignatureValidator.validate(request, request.getIssuer().getValue(), SPSSODescriptor.DEFAULT_ELEMENT_NAME)) {
                throw new InvalidEidasAuthnRequestException("signature verification failed");
            }
        } catch (SignatureException | SecurityException e) {
            throw new InvalidEidasAuthnRequestException(e);
        }
    }

    private static void validateKeyInfo(SignableSAMLObject signableSAMLObject) {
        if (Objects.isNull(signableSAMLObject.getSignature().getKeyInfo())) {
            throw new InvalidEidasAuthnRequestException("KeyInfo cannot be null");
        }
        if (signableSAMLObject.getSignature().getKeyInfo().getX509Datas().isEmpty()) {
            throw new InvalidEidasAuthnRequestException("no x509 data found");
        }
        if (signableSAMLObject.getSignature().getKeyInfo().getX509Datas().get(0).getX509Certificates().isEmpty()) {
            throw new InvalidEidasAuthnRequestException("no x509 certificates found in x509 data");
        }
        if (Objects.isNull(signableSAMLObject.getSignature().getKeyInfo().getX509Datas().get(0).getX509Certificates().get(0))) {
            throw new InvalidEidasAuthnRequestException("x509 certificate was invalid");
        }
    }
}
