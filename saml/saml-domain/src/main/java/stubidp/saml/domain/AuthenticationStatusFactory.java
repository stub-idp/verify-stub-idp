package stubidp.saml.domain;

public interface AuthenticationStatusFactory<T, U extends IdaStatus> {
    U create(T status, String message);
}
