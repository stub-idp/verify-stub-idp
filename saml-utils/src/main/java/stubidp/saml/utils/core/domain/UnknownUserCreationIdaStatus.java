package stubidp.saml.utils.core.domain;

import stubidp.saml.domain.IdaStatus;

public enum UnknownUserCreationIdaStatus implements IdaStatus {
    Success,
    CreateFailure,
    NoAttributeFailure,
}
