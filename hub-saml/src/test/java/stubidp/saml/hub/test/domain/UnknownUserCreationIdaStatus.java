package stubidp.saml.hub.test.domain;

import stubidp.saml.domain.IdaStatus;

public enum UnknownUserCreationIdaStatus implements IdaStatus {
    Success,
    CreateFailure,
    NoAttributeFailure,
}
