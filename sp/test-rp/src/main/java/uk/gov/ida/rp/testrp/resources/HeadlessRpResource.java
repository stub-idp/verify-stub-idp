package uk.gov.ida.rp.testrp.resources;

import org.glassfish.jersey.server.ContainerRequest;
import uk.gov.ida.rp.testrp.Urls;
import uk.gov.ida.rp.testrp.controllogic.AuthnRequestSenderHandler;
import uk.gov.ida.rp.testrp.domain.JourneyHint;
import uk.gov.ida.saml.idp.stub.domain.InboundResponseFromHub;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.function.Function;

import static java.text.MessageFormat.format;

@Path(Urls.HeadlessUrls.HEADLESS_ROOT)
@Produces(MediaType.TEXT_HTML)
public class HeadlessRpResource {

    public static final int WORKING_ASSERTION_CONSUMER_SERVICE_INDEX = 1;
    private final Function<String, InboundResponseFromHub> samlResponseDeserialiser;
    private final AuthnRequestSenderHandler authnRequestSenderHandler;

    @Inject
    public HeadlessRpResource(Function<String, InboundResponseFromHub> samlResponseDeserialiser,
                              AuthnRequestSenderHandler authnRequestSenderHandler) {
        this.samlResponseDeserialiser = samlResponseDeserialiser;
        this.authnRequestSenderHandler = authnRequestSenderHandler;
    }

    @GET
    @Path(Urls.SUCCESSFUL_REGISTER_PATH)
    public Response makeAuthnRequest(
            @Context ContainerRequest containerRequest) {
        return authnRequestSenderHandler.sendAuthnRequest(
                containerRequest.getUriInfo().getRequestUri(),
                Optional.ofNullable(WORKING_ASSERTION_CONSUMER_SERVICE_INDEX),
                "headless",
                Optional.empty(),
                Optional.empty(),
                false,
                false,
                false);
    }

    @GET
    @Path(Urls.SUCCESSFUL_IDP_PATH)
    public Response makeIdpAuthnRequest(
            @Context ContainerRequest containerRequest) {
        return authnRequestSenderHandler.sendAuthnRequest(
                containerRequest.getUriInfo().getRequestUri(),
                Optional.ofNullable(WORKING_ASSERTION_CONSUMER_SERVICE_INDEX),
                "headless",
                Optional.empty(),
                Optional.of(JourneyHint.uk_idp_start),
                false,
                false,
                false);
    }

    @POST
    @Path(Urls.HeadlessUrls.LOGIN_PATH)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response doLogin(
            @FormParam(Urls.Params.SAML_RESPONSE_PARAM) @NotNull String samlResponse
    ) {
        InboundResponseFromHub idpResponse = samlResponseDeserialiser.apply(samlResponse);

        final String responseBody;
        switch(idpResponse.getStatus()) {
            case NoMatchingServiceMatchFromHub: {
                responseBody = format("<html><head><title>Headless No Match Response</title></head><body>No Match<p>{0}</body></html>", idpResponse.getStatus());
                break;
            }
            case NoAuthenticationContext: {
                responseBody = format("<html><head><title>Headless Failed Log In</title></head><body>:-(((((<p>{0}</body></html>", idpResponse.getStatus());
                break;
            }
            default: {
                responseBody = format("<html><head><title>Headless Logged In</title></head><body>:-)<p>{0}</body></html>", idpResponse.getStatus());
                break;
            }
        }

        return Response.status(Response.Status.OK).entity(responseBody).build();
    }

}
