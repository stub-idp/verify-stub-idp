package stubidp.stubidp.auth;

import com.google.common.base.Splitter;
import org.apache.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;
import stubidp.stubidp.Urls;
import stubidp.stubidp.configuration.UserCredentials;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.utils.common.string.StringEncoding;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.text.MessageFormat.format;

public class UserResourceBasicAuthFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(UserResourceBasicAuthFilter.class);
    private static final Pattern userResourcePattern = Pattern.compile("^"+ UriBuilder.fromUri(Urls.USERS_RESOURCE).build("(.+)").toASCIIString()+".*$");

    private final IdpStubsRepository idpStubsRepository;
    private final Splitter splitter = Splitter.on(':').limit(2);
    private final int credOffset = "Basic ".length();

    public UserResourceBasicAuthFilter(IdpStubsRepository idpStubsRepository) {
        this.idpStubsRepository = idpStubsRepository;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        //do nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        UsernamePassword usernamePasswordFromRequest = getUsernamePasswordFromRequest(httpServletRequest);

        // Get the friendly id
        final String requestURI = ((HttpServletRequest) request).getRequestURI();
        final Matcher matcher = userResourcePattern.matcher(requestURI);
        if(matcher.find()) {
            final String friendlyId = matcher.group(1);

            List<UserCredentials> userCredentialsList = idpStubsRepository.getUserCredentialsForFriendlyId(friendlyId);

            for (UserCredentials userCredentials : userCredentialsList) {
                if (requestAuthMatchesUsernameAndPassword(userCredentials, usernamePasswordFromRequest)) {
                    LOG.info(format("Basic auth login success for IDP {0}, user {1}", friendlyId, usernamePasswordFromRequest.getUsername()));
                    chain.doFilter(request, response);
                    return;
                }
            }
            LOG.error(format("Basic auth login failure for IDP {0}, user {1}", friendlyId, usernamePasswordFromRequest.getUsername()));
            HttpServletResponse resp = (HttpServletResponse) response;
            resp.setHeader("WWW-Authenticate", "Basic realm=\"Admin\"");
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // this method intentionally left blank
    }

    private boolean requestAuthMatchesUsernameAndPassword(UserCredentials userCredentials, UsernamePassword userProvidedCredentials) {
        return (userCredentials.getUser().equalsIgnoreCase(userProvidedCredentials.getUsername())
                && BCrypt.checkpw(userProvidedCredentials.getPassword(), userCredentials.getPassword()));
    }

    private UsernamePassword getUsernamePasswordFromRequest(HttpServletRequest request) {
        UsernamePassword userProvidedCredentials;
        String providedPassword = null;
        String providedUserName = null;

        String basicAuthHeaderValue = request.getHeader("Authorization");
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
