package stubidp.stubidp.resources.idp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import stubidp.saml.constants.Constants;
import stubidp.stubidp.Urls;
import stubidp.stubidp.builders.IdpMetadataBuilder;
import stubidp.stubidp.repositories.Idp;
import stubidp.stubidp.repositories.IdpStubsRepository;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path(Urls.IDP_METADATA_RESOURCE)
@Produces(Constants.APPLICATION_SAMLMETADATA_XML)
public class IdpMetadataResource {

    private static final Logger LOG = LoggerFactory.getLogger(IdpMetadataResource.class);
    private final IdpMetadataBuilder idpMetadataBuilder;
    private final IdpStubsRepository idpStubsRepository;

    @Inject
    public IdpMetadataResource(IdpMetadataBuilder idpMetadataBuilder,
                               IdpStubsRepository idpStubsRepository) {
        this.idpMetadataBuilder = idpMetadataBuilder;
        this.idpStubsRepository = idpStubsRepository;
    }

    @GET
    public Response getMetadata(@PathParam(Urls.IDP_ID_PARAM) @NotNull String idpName) {
        Idp idp = idpStubsRepository.getIdpWithFriendlyId(idpName);
        Document metadata = idpMetadataBuilder.getSignedMetadata(idp);
        return Response.ok(metadata).build();
    }
}
