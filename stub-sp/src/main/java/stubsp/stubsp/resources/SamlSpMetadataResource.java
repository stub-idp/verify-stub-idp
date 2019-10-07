package stubsp.stubsp.resources;

import stubidp.saml.utils.Constants;
import stubsp.stubsp.Urls;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path(Urls.SAML_FEDERATION_METADATA_RESOURCE)
@Produces(Constants.APPLICATION_SAMLMETADATA_XML)
public class SamlSpMetadataResource {

    @Inject
    public SamlSpMetadataResource() {

    }

    @GET
    public String getSamlMetadata() {
        return null;
    }
}
