package stubidp.saml.domain.matching;

import stubidp.saml.domain.IdaStatus;

public enum MatchingServiceIdaStatus implements IdaStatus {
    NoMatchingServiceMatchFromMatchingService,
    RequesterError,
    MatchingServiceMatch,
    UserAccountCreated,
    Healthy
    }
