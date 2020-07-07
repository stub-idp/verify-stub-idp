package stubidp.saml.domain;

@SuppressWarnings("rawtypes")
public interface AuthenticationStatusFactory<T extends Enum, U extends IdaStatus> {
    U create(T status, String message);
}
