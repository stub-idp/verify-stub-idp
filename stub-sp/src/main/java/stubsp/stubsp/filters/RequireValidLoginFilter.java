package stubsp.stubsp.filters;

import org.jboss.logging.MDC;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.UsageType;
import stubidp.shared.cookies.HmacValidator;
import stubidp.shared.domain.SamlRequest;
import stubidp.shared.repositories.MetadataRepository;
import stubidp.shared.views.SamlMessageRedirectViewFactory;
import stubidp.utils.rest.common.SessionId;
import stubsp.stubsp.configuration.StubSpConfiguration;
import stubsp.stubsp.saml.request.IdpAuthnRequestBuilder;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static stubidp.shared.csrf.AbstractCSRFCheckProtectionFilter.IS_SECURE_COOKIE_ENABLED;
import static stubsp.stubsp.StubSpBinder.SP_METADATA_REPOSITORY;
import static stubsp.stubsp.cookies.StubSpCookieNames.SECURE_COOKIE_NAME;
import static stubsp.stubsp.cookies.StubSpCookieNames.SESSION_COOKIE_NAME;

public class RequireValidLoginFilter implements ContainerRequestFilter {

    private final HmacValidator hmacValidator;
    private final boolean isSecureCookieEnabled;
    private final SamlMessageRedirectViewFactory samlMessageRedirectViewFactory;
    private final boolean sessionFound;
    private final MetadataRepository metadataRepository;
    private final StubSpConfiguration stubSpConfiguration;

    public enum Status {VERIFIED, ID_NOT_PRESENT, HASH_NOT_PRESENT, DELETED_SESSION, INVALID_HASH, NOT_FOUND }

    public static final String NO_CURRENT_SESSION_COOKIE_VALUE = "no-current-session";

    @Inject
    public RequireValidLoginFilter(HmacValidator hmacValidator,
                                   @Named(IS_SECURE_COOKIE_ENABLED) Boolean isSecureCookieEnabled,
                                   SamlMessageRedirectViewFactory samlMessageRedirectViewFactory,
                                   @org.jvnet.hk2.annotations.Optional Boolean sessionFound,
                                   @Named(SP_METADATA_REPOSITORY) MetadataRepository metadataRepository,
                                   StubSpConfiguration stubSpConfiguration) {
        this.hmacValidator = hmacValidator;
        this.isSecureCookieEnabled = isSecureCookieEnabled;
        this.samlMessageRedirectViewFactory = samlMessageRedirectViewFactory;
        this.sessionFound = Optional.ofNullable(sessionFound).orElse(false);
        this.metadataRepository = metadataRepository;
        this.stubSpConfiguration = stubSpConfiguration;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {

        // Get SessionId from cookie
        final Optional<String> sessionCookie = Optional.ofNullable(getValueOfPossiblyNullCookie(requestContext.getCookies(), SESSION_COOKIE_NAME));
        // Get SessionId HMAC from cookie
        final Optional<String> secureCookie;
        if (isSecureCookieEnabled) {
            secureCookie = Optional.ofNullable(getValueOfPossiblyNullCookie(requestContext.getCookies(), SECURE_COOKIE_NAME));
        } else {
            secureCookie = Optional.empty();
        }

        if (sessionCookie.isPresent()) {
            MDC.remove("SessionId");
            MDC.put("SessionId", sessionCookie.get());
            if(validateSessionCookies(sessionCookie, secureCookie)) {
                return;
            }
        }

        throw new WebApplicationException(samlMessageRedirectViewFactory.sendSamlRequest(new SamlRequest() {
            @Override
            public String getRequestString() {
                BasicCredential signingCredential = new BasicCredential(createPublicKey(stubSpConfiguration.getSigningKeyPairConfiguration().getCert()), stubSpConfiguration.getSigningKeyPairConfiguration().getPrivateKey());
                signingCredential.setUsageType(UsageType.SIGNING);
                return IdpAuthnRequestBuilder.anAuthnRequest()
                        .withDestination(metadataRepository.getSingleSignOnService().toASCIIString())
                        .withKeyInfo(false) // not expected in Verify authn requests
                        .withIssueInstant(Instant.now())
                        .withEntityId(stubSpConfiguration.getSaml().getEntityId())
                        .withSigningCertificate(stubSpConfiguration.getSigningKeyPairConfiguration().getCert())
                        .withSigningCredential(signingCredential)
                        .build();
            }

            private PublicKey createPublicKey(String partialCert) {
                try {
                    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                    String fullCert;
                    if (partialCert.contains("-----BEGIN CERTIFICATE-----")) {
                        fullCert = partialCert;
                    } else {
                        fullCert = MessageFormat.format("-----BEGIN CERTIFICATE-----\n{0}\n-----END CERTIFICATE-----", partialCert.trim());
                    }
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fullCert.getBytes(StandardCharsets.UTF_8));
                    Certificate certificate = certificateFactory.generateCertificate(byteArrayInputStream);
                    return certificate.getPublicKey();
                } catch (CertificateException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String getRelayState() {
                return "a_relaystate";
            }

            @Override
            public URI getIdpSSOUrl() {
                return metadataRepository.getSingleSignOnService();
            }
        }));

    }

    private boolean validateSessionCookies(Optional<String> sessionCookie, Optional<String> secureCookie) {

        final Status status;

        if (sessionCookie.isEmpty() || sessionCookie.get().isBlank()) {
            status = Status.ID_NOT_PRESENT;
        } else if (isSecureCookieEnabled && (secureCookie.isEmpty() || secureCookie.get().isBlank())) {
            status = Status.HASH_NOT_PRESENT;
        } else if (isSecureCookieEnabled && NO_CURRENT_SESSION_COOKIE_VALUE.equals(secureCookie.get())) {
            status = Status.DELETED_SESSION;
        } else if (isSecureCookieEnabled && !hmacValidator.validateHMACSHA256(secureCookie.get(), sessionCookie.get())) {
            status = Status.INVALID_HASH;
        } else if (!sessionExists(new SessionId(sessionCookie.get()))) {
            status = Status.NOT_FOUND;
        } else {
            status = Status.VERIFIED;
        }

        return switch (status) {
            case VERIFIED -> true;
            default -> false;
        };
    }

    private boolean sessionExists(SessionId sessionId) {
        return sessionFound;
    }

    private String getValueOfPossiblyNullCookie(Map<String, Cookie> cookies, String cookieName) {
        return cookies.containsKey(cookieName) ? cookies.get(cookieName).getValue() : null;
    }
}
