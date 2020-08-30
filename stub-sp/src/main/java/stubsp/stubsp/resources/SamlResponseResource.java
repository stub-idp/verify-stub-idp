package stubsp.stubsp.resources;

import stubsp.stubsp.Urls;
import stubsp.stubsp.services.ResponseStatus;
import stubsp.stubsp.services.SamlResponseService;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Path(Urls.SAML_SSO_RESPONSE_RESOURCE)
@Produces(MediaType.TEXT_HTML)
public class SamlResponseResource {

    private final SamlResponseService samlResponseService;

    @Inject
    public SamlResponseResource(SamlResponseService samlResponseService) {
        this.samlResponseService = samlResponseService;
    }

    @POST
    public Response receiveSamlResponse(@FormParam(Urls.SAML_RESPONSE_PARAM) String samlResponse,
                                        @FormParam(Urls.RELAY_STATE_PARAM) String relayState) {
        ResponseStatus responseStatus = samlResponseService.processResponse(samlResponse, relayState);
        switch(responseStatus) {
            case SUCCESS: {
                return Response.seeOther(UriBuilder.fromPath(Urls.SUCCESS_RESOURCE).build())
                        .build();
            }
            case AUTHENTICATION_FAILED: {
                return Response.seeOther(UriBuilder.fromPath(Urls.AUTHENTICATION_FAILURE_RESOURCE).build())
                        .build();
            }
            default: {
                return Response.seeOther(UriBuilder.fromPath(Urls.ROOT_RESOURCE).build())
                        .build();
            }
        }
    }
}
