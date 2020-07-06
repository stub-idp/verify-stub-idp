package stubidp.saml.utils.core.domain;

import stubidp.saml.domain.IdaStatus;

public enum MatchingServiceIdaStatus implements IdaStatus {
    NoMatchingServiceMatchFromMatchingService,
    RequesterError,
    MatchingServiceMatch,
    UserAccountCreated,
    Healthy
    }
