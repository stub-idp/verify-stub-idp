package stubidp.saml.hub.hub.domain;

import stubidp.saml.domain.IdaStatus;

@SuppressWarnings("rawtypes")
public interface AuthenticationStatusFactory<T extends Enum, U extends IdaStatus> {
    U create(T status, String message);
}
