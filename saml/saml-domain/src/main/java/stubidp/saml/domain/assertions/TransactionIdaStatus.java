package stubidp.saml.domain.assertions;

import stubidp.saml.domain.IdaStatus;

public enum TransactionIdaStatus implements IdaStatus {
    Success,
    RequesterError,
    NoAuthenticationContext,
    NoMatchingServiceMatchFromHub,
    AuthenticationFailed
}
