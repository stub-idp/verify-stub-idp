package stubsp.stubsp.resources;

import org.w3c.dom.Document;
import stubidp.saml.constants.Constants;
import stubsp.stubsp.Urls;
import stubsp.stubsp.builders.SpMetadataBuilder;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path(Urls.SAML_FEDERATION_METADATA_RESOURCE)
@Produces(Constants.APPLICATION_SAMLMETADATA_XML)
public class SamlSpMetadataResource {

    private final SpMetadataBuilder spMetadataBuilder;

    @Inject
    public SamlSpMetadataResource(SpMetadataBuilder spMetadataBuilder) {
        this.spMetadataBuilder = spMetadataBuilder;
    }

    @GET
    public Response getSamlMetadata() {
        Document metadata = spMetadataBuilder.getSignedMetadata();
        return Response.ok(metadata).build();
    }
}
