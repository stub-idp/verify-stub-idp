package stubsp.stubsp.resources;

import stubsp.stubsp.Urls;
import stubsp.stubsp.services.SecureService;
import stubsp.stubsp.views.SecureView;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(Urls.SAML_SSO_RESPONSE_RESOURCE)
@Produces(MediaType.TEXT_HTML)
public class SamlResponseResource {

    @Inject
    public SamlResponseResource() {

    }

    @POST
    public String receiveSamlResponse() {
        return null;
    }
}
