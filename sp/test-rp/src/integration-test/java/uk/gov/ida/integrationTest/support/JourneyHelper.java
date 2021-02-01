package uk.gov.ida.integrationTest.support;

import org.glassfish.jersey.client.ClientProperties;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.w3c.dom.Document;
import uk.gov.ida.common.SessionId;
import uk.gov.ida.rp.testrp.Urls;
import uk.gov.ida.rp.testrp.contract.MatchingServiceRequestDto;
import uk.gov.ida.rp.testrp.contract.MatchingServiceResponseDto;
import uk.gov.ida.rp.testrp.contract.UnknownUserCreationRequestDto;
import uk.gov.ida.rp.testrp.contract.UnknownUserCreationResponseDto;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static uk.gov.ida.integrationTest.support.HubResponseFactory.getSignedResponse;
import static uk.gov.ida.rp.testrp.Urls.Cookies.TEST_RP_SESSION_COOKIE_NAME;
import static uk.gov.ida.rp.testrp.builders.Cycle3DatasetDtoBuilder.aCycle3DatasetDto;
import static uk.gov.ida.rp.testrp.builders.MatchingServiceRequestDtoBuilder.aMatchingServiceRequestDto;
import static uk.gov.ida.rp.testrp.contract.MatchingServiceResponseDto.MATCH;

public class JourneyHelper {

    private final Client client;

    public JourneyHelper(Client client) {

        this.client = client;
    }

    public String doASuccessfulMatchInLocalMatchingService(URI matchingUri, String requestId) {
        final String hashedPid = UUID.randomUUID().toString();
        final MatchingServiceRequestDto matchingServiceRequest = aMatchingServiceRequestDto()
                .withHashedPid(hashedPid)
                .withMatchId(requestId)
                .withCycle3Dataset(aCycle3DatasetDto().build())
                .build();

        final MatchingServiceResponseDto responseDto = client
                .target(matchingUri)
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(matchingServiceRequest, MediaType.APPLICATION_JSON_TYPE), MatchingServiceResponseDto.class);

        assertThat(responseDto.getResult()).isEqualTo(MATCH);

        return hashedPid;
    }

    public RequestParamHelper.RequestParams startNewJourneyFromTestRp(URI uri) {
        return startNewJourneyFromTestRp(uri, false, false);
    }

    public RequestParamHelper.RequestParams startNewJourneyFromTestRp(URI requestUri, boolean noMatch, boolean failAccountCreation) {
        return startNewJourneyFromTestRp(requestUri, noMatch, failAccountCreation, Optional.empty());
    }

    /**
     * @param sessionId REQUIRED to test non-repudiation journey...
     */
    public RequestParamHelper.RequestParams startNewJourneyFromTestRp(URI requestUri, boolean noMatch, boolean failAccountCreation, Optional<String> sessionId) {
        UriBuilder uriBuilder = UriBuilder.fromUri(requestUri);
        if (noMatch) uriBuilder.queryParam(Urls.Params.NO_MATCH, "on");
        if (failAccountCreation) uriBuilder.queryParam(Urls.Params.FAIL_ACCOUNT_CREATION, "on");
        final Invocation.Builder request = client.target(uriBuilder.build()).request();
        if(sessionId.isPresent()) {
            request.cookie(TEST_RP_SESSION_COOKIE_NAME, sessionId.get());
        }
        final Response requestResponse = request.get();
        RequestParamHelper.RequestParams requestParams = RequestParamHelper.getParamsFromSamlForm(requestResponse.readEntity(Document.class));
        assertThat(requestParams.getRelayState().isPresent()).isTrue();
        assertThat(requestParams.getRequestId().isPresent()).isTrue();
        return requestParams;
    }

    public UnknownUserCreationResponseDto postUnknownUserCreation(URI uri, UnknownUserCreationRequestDto unknownUserCreationRequestDto) {
        return client
                .target(uri)
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(unknownUserCreationRequestDto, MediaType.APPLICATION_JSON_TYPE), UnknownUserCreationResponseDto.class);
    }

    public MatchingServiceResponseDto postMatchingRequest(URI uri, MatchingServiceRequestDto matchingServiceRequest) {
        return client
                .target(uri)
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(matchingServiceRequest, MediaType.APPLICATION_JSON_TYPE), MatchingServiceResponseDto.class);
    }

    public Response postSuccessAuthnResponseBackFromHub(URI uri, String hashedPid, String relayState) throws MarshallingException, SignatureException {
        Form form = new Form();
        form.param(Urls.Params.SAML_RESPONSE_PARAM, getSignedResponse(hashedPid));
        form.param(Urls.Params.RELAY_STATE_PARAM, relayState);
        Response response = client
                .property(ClientProperties.FOLLOW_REDIRECTS, false)
                .target(uri)
                .request()
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));
        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getCookies().containsKey(TEST_RP_SESSION_COOKIE_NAME)).isTrue();
        return response;
    }

    public Response getSuccessPage(String location, NewCookie newCookie) {
        Response response = client.target(location)
                .property(ClientProperties.FOLLOW_REDIRECTS, false)
                .request()
                .cookie(newCookie)
                .get();
        assertThat(response.getStatus()).isEqualTo(200);
        return response;
    }
}
