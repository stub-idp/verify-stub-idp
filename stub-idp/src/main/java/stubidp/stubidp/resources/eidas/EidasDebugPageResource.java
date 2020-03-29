package stubidp.stubidp.resources.eidas;

import stubidp.stubidp.Urls;
import stubidp.stubidp.cookies.StubIdpCookieNames;
import stubidp.stubidp.domain.EidasScheme;
import stubidp.stubidp.exceptions.GenericStubIdpException;
import stubidp.stubidp.exceptions.InvalidEidasSchemeException;
import stubidp.stubidp.filters.SessionCookieValueMustExistAsASession;
import stubidp.stubidp.repositories.EidasSession;
import stubidp.stubidp.repositories.EidasSessionRepository;
import stubidp.stubidp.repositories.StubCountry;
import stubidp.stubidp.repositories.StubCountryRepository;
import stubidp.stubidp.views.EidasDebugPageView;
import stubidp.utils.rest.common.SessionId;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.Optional;

import static java.text.MessageFormat.format;

@Path(Urls.EIDAS_DEBUG_RESOURCE)
@Produces(MediaType.TEXT_HTML)
@SessionCookieValueMustExistAsASession
public class EidasDebugPageResource {

    private final EidasSessionRepository sessionRepository;
    private final StubCountryRepository stubCountryRepository;

    @Inject
    public EidasDebugPageResource(
            EidasSessionRepository sessionRepository,
            StubCountryRepository stubCountryRepository) {
        this.sessionRepository = sessionRepository;
        this.stubCountryRepository = stubCountryRepository;
    }

    @GET
    public Response get(
            @PathParam(Urls.SCHEME_ID_PARAM) @NotNull String schemeId,
            @CookieParam(StubIdpCookieNames.SESSION_COOKIE_NAME) @NotNull SessionId sessionCookie) {

        final Optional<EidasScheme> eidasScheme = EidasScheme.fromString(schemeId);
        if(eidasScheme.isEmpty()) {
            throw new InvalidEidasSchemeException();
        }

        if (Objects.isNull(sessionCookie.toString()) || sessionCookie.toString().isBlank()) {
            throw new GenericStubIdpException(format(("Unable to locate session cookie for " + schemeId)), Response.Status.BAD_REQUEST);
        }

        Optional<EidasSession> session = sessionRepository.get(sessionCookie);

        if (session.isEmpty()) {
            throw new GenericStubIdpException(format(("Session is invalid for " + schemeId)), Response.Status.BAD_REQUEST);
        }

        StubCountry stubCountry = stubCountryRepository.getStubCountryWithFriendlyId(eidasScheme.get());
        return Response.ok(new EidasDebugPageView(stubCountry.getDisplayName(), stubCountry.getFriendlyId(), stubCountry.getAssetId(), session.get())).build();
    }

}
