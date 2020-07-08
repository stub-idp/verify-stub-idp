package stubidp.saml.hub.transformers.inbound;

import stubidp.saml.domain.IdaStatus;

public enum MatchingServiceIdaStatus implements IdaStatus {
    NoMatchingServiceMatchFromMatchingService,
    RequesterError,
    MatchingServiceMatch,
    UserAccountCreated,
    UserAccountCreationFailed,
    Healthy
    }
