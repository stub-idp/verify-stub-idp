package stubidp.stubidp.resources.eidas;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.security.SecurityException;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.serializers.serializers.XmlObjectToElementTransformer;
import stubidp.saml.constants.Constants;
import stubidp.stubidp.Urls;
import stubidp.stubidp.builders.CountryMetadataBuilder;
import stubidp.stubidp.domain.EidasScheme;
import stubidp.stubidp.exceptions.InvalidEidasSchemeException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.security.cert.CertificateEncodingException;

import static stubidp.stubidp.StubIdpEidasBinder.COUNTRY_SIGNING_KEY_STORE;
import static stubidp.stubidp.StubIdpEidasBinder.STUB_COUNTRY_METADATA_URL;
import static stubidp.stubidp.StubIdpEidasBinder.STUB_COUNTRY_SSO_URL;

@Path(Urls.EIDAS_METADATA_RESOURCE)
@Produces(Constants.APPLICATION_SAMLMETADATA_XML)
public class EidasProxyNodeServiceMetadataResource {

    private static final Logger LOG = LoggerFactory.getLogger(EidasProxyNodeServiceMetadataResource.class);
    private final IdaKeyStore idaKeyStore;
	private final CountryMetadataBuilder countryMetadataBuilder;
	private final String metadataUrlPattern;
	private final String ssoUrlPattern;

	@Inject
    public EidasProxyNodeServiceMetadataResource(@Named(COUNTRY_SIGNING_KEY_STORE) IdaKeyStore idaKeyStore,
                                                 @Named(STUB_COUNTRY_METADATA_URL) String metadataUrlPattern,
                                                 @Named(STUB_COUNTRY_SSO_URL) String ssoUrlPattern,
                                                 CountryMetadataBuilder countryMetadataBuilder) {
        this.countryMetadataBuilder = countryMetadataBuilder;
        this.idaKeyStore = idaKeyStore;
        this.metadataUrlPattern = metadataUrlPattern;
        this.ssoUrlPattern = ssoUrlPattern;
    }

    @GET
    public Response getMetadata(@PathParam(Urls.SCHEME_ID_PARAM) @NotNull String schemeId) {

        if (schemeId == null || schemeId.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if(EidasScheme.fromString(schemeId).isEmpty()) {
            throw new InvalidEidasSchemeException();
        }

        URI ssoEndpoint = UriBuilder.fromUri(ssoUrlPattern).build(schemeId);
        URI metadataUrl = UriBuilder.fromUri(metadataUrlPattern).build(schemeId);

        try {
            Document metadata = getMetadataDocument(metadataUrl, ssoEndpoint);
            return Response.ok(metadata).build();
		} catch (CertificateEncodingException | MarshallingException | SecurityException | SignatureException e) {
            LOG.error("Failed to generate metadata", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Document getMetadataDocument(URI requestPath, URI ssoEndpoint) throws CertificateEncodingException, MarshallingException, SecurityException, SignatureException {
        EntityDescriptor metadata = countryMetadataBuilder.createEntityDescriptorForProxyNodeService(
            requestPath,
            ssoEndpoint,
            idaKeyStore.getSigningCertificate(),
            /* We use signing cert for encryption cert below because we need some certificate to be there
               but the stub currently doesn't have or need an encrypting certificate. */
            idaKeyStore.getSigningCertificate());
        XmlObjectToElementTransformer<EntityDescriptor> transformer = new XmlObjectToElementTransformer<>();
        return transformer.apply(metadata).getOwnerDocument();
    }
}
