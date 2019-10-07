package stubsp.stubsp.resources;

import stubsp.stubsp.Urls;
import stubsp.stubsp.services.InitiateSingleIdpJourneyService;
import stubsp.stubsp.views.SecureView;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(Urls.INITIATE_SINGLE_IDP_JOURNEY_RESOURCE)
@Produces(MediaType.TEXT_HTML)
public class InitiateSingleIdpJourneyResource {

    private final InitiateSingleIdpJourneyService initiateSingleIdpJourneyService;

    @Inject
    public InitiateSingleIdpJourneyResource(InitiateSingleIdpJourneyService initiateSingleIdpJourneyService) {

        this.initiateSingleIdpJourneyService = initiateSingleIdpJourneyService;
    }

    @GET
    public SecureView getSecureView() {
        return null;
    }
}
