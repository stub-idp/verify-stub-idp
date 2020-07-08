package stubidp.saml.domain.matching;

import stubidp.saml.domain.IdaStatus;

public enum UnknownUserCreationIdaStatus implements IdaStatus {
    Success,
    CreateFailure,
    NoAttributeFailure,
}
