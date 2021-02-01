package uk.gov.ida.rp.testrp.controllogic;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.saml.domain.request.AuthnRequestFromTransaction;
import stubidp.utils.rest.common.SessionId;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.domain.AccessToken;
import uk.gov.ida.rp.testrp.domain.JourneyHint;
import uk.gov.ida.rp.testrp.repositories.SessionRepository;
import uk.gov.ida.rp.testrp.views.SamlAuthnRequestRedirectViewFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.String.format;

public class AuthnRequestSenderHandler {
    private final TestRpConfiguration configuration;
    private final SamlAuthnRequestRedirectViewFactory samlRequestFactory;
    private final SessionRepository sessionRepository;
    private final MetadataResolver metadataResolver;
    private final Function<AuthnRequestFromTransaction, String> authnRequestToStringTransformer;

    private static final Logger LOG = LoggerFactory.getLogger(AuthnRequestSenderHandler.class);

    @Inject
    public AuthnRequestSenderHandler(
            TestRpConfiguration configuration,
            SamlAuthnRequestRedirectViewFactory samlRequestFactory,
            SessionRepository sessionRepository,
            MetadataResolver metadataResolver,
            Function<AuthnRequestFromTransaction, String> authnRequestToStringTransformer) {

        this.configuration = configuration;
        this.samlRequestFactory = samlRequestFactory;
        this.sessionRepository = sessionRepository;
        this.metadataResolver = metadataResolver;
        this.authnRequestToStringTransformer = authnRequestToStringTransformer;
    }

    public Response sendAuthnRequest(
            URI requestUri,
            Optional<Integer> assertionConsumerServiceIndex,
            final String rpName,
            Optional<AccessToken> accessToken,
            Optional<JourneyHint> journeyHint,
            boolean forceAuthentication,
            boolean forceLMSNoMatch,
            boolean forceLMSUserAccountCreationFail) {

        final String issuerId = format(configuration.getSamlConfiguration().getEntityId(), rpName);
        final AuthnRequestFromTransaction requestToSendToHub = AuthnRequestFromTransaction.createRequestToSendToHub(issuerId, forceAuthentication, Optional.empty(), assertionConsumerServiceIndex, Optional.empty(), getHubSsoUri());
        final String requestId = requestToSendToHub.getId();

        if(accessToken.isPresent()) {
            LOG.info("USED_TOKEN:{},{}", accessToken.get(), requestId);
        }

        final SessionId sessionId = sessionRepository.newSession(requestId, requestUri, issuerId, assertionConsumerServiceIndex, journeyHint, forceAuthentication, forceLMSNoMatch, forceLMSUserAccountCreationFail);

        return samlRequestFactory.sendSamlMessage(authnRequestToStringTransformer.apply(requestToSendToHub), sessionId, getHubSsoUri(), journeyHint);
    }

    private URI getHubSsoUri() {
        URI hubSsoEndpoint;
        try {
            CriteriaSet criteria = new CriteriaSet(new EntityIdCriterion(configuration.getMsaEntityId()));
            EntityDescriptor entityDescriptor = metadataResolver.resolveSingle(criteria);

            hubSsoEndpoint = URI.create(entityDescriptor.getIDPSSODescriptor(SAMLConstants.SAML20P_NS).getSingleSignOnServices().iterator().next().getLocation());
        } catch (ResolverException e) {
            LOG.info(format("Unable to retrieve Verify hub SSO location from MSA metadata; MSA entityId is: {0}", configuration.getMsaEntityId()));
            throw new RuntimeException(e);
        }
        return hubSsoEndpoint;
    }

}
