package stubidp.saml.domain.assertions;

import stubidp.saml.domain.AuthenticationStatusFactory;
import stubidp.saml.domain.IdaStatus;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static stubidp.saml.domain.assertions.IdpIdaStatus.Status.RequesterError;

public final class IdpIdaStatus implements IdaStatus {

    public enum Status {
        Success,
        NoAuthenticationContext,
        RequesterError,
        AuthenticationFailed,
        AuthenticationCancelled,
        AuthenticationPending,
        UpliftFailed
    }
    public static IdpIdaStatus success() { return new IdpIdaStatus(Status.Success);}
    public static IdpIdaStatus authenticationFailed() { return new IdpIdaStatus(Status.AuthenticationFailed);}
    public static IdpIdaStatus noAuthenticationContext() { return new IdpIdaStatus(Status.NoAuthenticationContext);}
    public static IdpIdaStatus requesterError() { return new IdpIdaStatus(RequesterError);}
    public static IdpIdaStatus requesterError(Optional<String> errorMessage) { return new IdpIdaStatus(RequesterError, errorMessage);}
    public static IdpIdaStatus authenticationCancelled() {
        return new IdpIdaStatus(Status.AuthenticationCancelled);
    }
    public static IdpIdaStatus authenticationPending() { return new IdpIdaStatus(Status.AuthenticationPending); }
    public static IdpIdaStatus upliftFailed() { return new IdpIdaStatus(Status.UpliftFailed); }

    private Status status;
    private Optional<String> message = empty();

    private IdpIdaStatus() {
    }

    private IdpIdaStatus(Status status) {
        this(status, empty());
    }

    private IdpIdaStatus(Status status, Optional<String> message) {
        this.status = status;
        this.message = message;
    }

    public Status getStatusCode() {
        return status;
    }

    public Optional<String> getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IdpIdaStatus idpIdaStatus = (IdpIdaStatus) o;

        if (status != idpIdaStatus.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = status.hashCode();
        result = 31 * result;
        return result;
    }

    @Override
    public String toString() {
        return "IdaStatus{" +
            "status=" + status +
            ", message=" + message +
            '}';
    }

    public static class IdpIdaStatusFactory implements AuthenticationStatusFactory<IdpIdaStatus.Status, IdpIdaStatus> {
        public IdpIdaStatus create(
                final IdpIdaStatus.Status statusCode,
                final Optional<String> message) {

            if (statusCode != RequesterError) {
                return new IdpIdaStatus(statusCode);
            }

            return new IdpIdaStatus(statusCode, message);
        }

        @Override
        public IdpIdaStatus create(IdpIdaStatus.Status status, String message) {
            return create(status, ofNullable(message));
        }
    }
}
