package stubidp.stubidp.auth;

import com.google.common.base.Splitter;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.stubidp.Urls;
import stubidp.stubidp.configuration.StubIdpConfiguration;
import stubidp.stubidp.configuration.UserCredentials;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.utils.common.string.StringEncoding;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.List;

import static java.text.MessageFormat.format;

public class StubIdpBasicAuthRequiredFilter implements ContainerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(StubIdpBasicAuthRequiredFilter.class);

    private final StubIdpConfiguration stubIdpConfiguration;
    private final IdpStubsRepository idpStubsRepository;
    private final Splitter splitter = Splitter.on(':').limit(2);
    private final int credOffset = "Basic ".length();

    @Inject
    public StubIdpBasicAuthRequiredFilter(StubIdpConfiguration stubIdpConfiguration,
                                          IdpStubsRepository idpStubsRepository) {
        this.stubIdpConfiguration = stubIdpConfiguration;
        this.idpStubsRepository = idpStubsRepository;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (stubIdpConfiguration.isBasicAuthEnabledForUserResource()) {
            final UsernamePassword usernamePasswordFromRequest = getUsernamePasswordFromRequest(requestContext);
            final String friendlyId = requestContext.getUriInfo().getPathParameters().get(Urls.IDP_ID_PARAM).get(0);
            final List<UserCredentials> userCredentialsList = idpStubsRepository.getUserCredentialsForFriendlyId(friendlyId);

            for (UserCredentials userCredentials : userCredentialsList) {
                if (requestAuthMatchesUsernameAndPassword(userCredentials, usernamePasswordFromRequest)) {
                    LOG.info(format("Basic auth login success for IDP {0}, user {1}", friendlyId, usernamePasswordFromRequest.getUsername()));
                    return;
                }
            }

            LOG.error(format("Basic auth login failure for IDP {0}, user {1}", friendlyId, usernamePasswordFromRequest.getUsername()));
            final Response response = Response.status(Response.Status.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Basic realm=\"Admin\"")
                    .build();
            throw new WebApplicationException(response);
        }
    }

    private boolean requestAuthMatchesUsernameAndPassword(UserCredentials userCredentials, UsernamePassword userProvidedCredentials) {
        return (userCredentials.getUser().equalsIgnoreCase(userProvidedCredentials.getUsername())
                && BCrypt.checkpw(userProvidedCredentials.getPassword(), userCredentials.getPassword()));
    }

    private UsernamePassword getUsernamePasswordFromRequest(ContainerRequestContext containerRequestContext) {
        UsernamePassword userProvidedCredentials;
        String providedPassword = null;
        String providedUserName = null;

        String basicAuthHeaderValue = containerRequestContext.getHeaderString("Authorization");
        if (basicAuthHeaderValue != null && basicAuthHeaderValue.startsWith("Basic ")) {
            String combined = StringEncoding.fromBase64Encoded(basicAuthHeaderValue.substring(credOffset));
            Iterator<String> credentials = splitter.split(combined).iterator();

            if (credentials.hasNext()) {
                providedUserName = credentials.next();
                if (credentials.hasNext()) {
                    providedPassword = credentials.next();
                }
            }
        }

        userProvidedCredentials = new UsernamePassword(providedPassword, providedUserName);
        return userProvidedCredentials;
    }

    private static final class UsernamePassword {
        private String password;
        private String username;

        private UsernamePassword(String password, String username) {
            this.password = password;
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public String getUsername() {
            return username;
        }
    }
}
