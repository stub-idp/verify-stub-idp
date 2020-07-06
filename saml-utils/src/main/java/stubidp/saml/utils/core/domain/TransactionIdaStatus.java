package stubidp.saml.utils.core.domain;

import stubidp.saml.domain.IdaStatus;

public enum TransactionIdaStatus implements IdaStatus {
    Success,
    RequesterError,
    NoAuthenticationContext,
    NoMatchingServiceMatchFromHub,
    AuthenticationFailed
}
