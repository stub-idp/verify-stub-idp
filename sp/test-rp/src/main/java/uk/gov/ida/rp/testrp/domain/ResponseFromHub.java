package uk.gov.ida.rp.testrp.domain;

import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.TransactionIdaStatus;
import stubidp.utils.rest.common.SessionId;
import uk.gov.ida.rp.testrp.repositories.Session;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public class ResponseFromHub {

    private final TransactionIdaStatus transactionIdaStatus;
    private final List<String> attributes;
    private final Optional<URI> redirectUri;
    private final Optional<Session> session;
    private final Optional<SessionId> sessionId;
    private final Optional<AuthnContext> authnContext;

    public ResponseFromHub(TransactionIdaStatus transactionIdaStatus, List<String> attributes, Optional<URI> redirectUri, Optional<Session> session, Optional<SessionId> sessionId, Optional<AuthnContext> authnContext) {
        this.transactionIdaStatus = transactionIdaStatus;
        this.attributes = attributes;
        this.redirectUri = redirectUri;
        this.session = session;
        this.sessionId = sessionId;
        this.authnContext = authnContext;
    }

    public TransactionIdaStatus getTransactionIdaStatus() {
        return transactionIdaStatus;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public Optional<URI> getRedirectUri() {
        return redirectUri;
    }

    public Optional<Session> getSession() {
        return session;
    }

    public Optional<SessionId> getSessionId() {
        return sessionId;
    }

    public Optional<AuthnContext> getAuthnContext() {
        return authnContext;
    }
}
