package stubidp.saml.domain.response;

import stubidp.saml.domain.assertions.IdentityProviderAssertion;
import stubidp.saml.domain.assertions.IdpIdaStatus;

import java.io.Serializable;
import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class OutboundResponseFromIdp extends IdaSamlResponse implements Serializable {
    private Optional<IdentityProviderAssertion> matchingDatasetAssertion;
    private Optional<IdentityProviderAssertion> authnStatementAssertion;
    private IdpIdaStatus status;

    @SuppressWarnings("unused") // needed for JAXB
    private OutboundResponseFromIdp() {}

    public OutboundResponseFromIdp(
            String id,
            String inResponseTo,
            String issuer,
            Instant issueInstant,
            IdpIdaStatus status,
            Optional<IdentityProviderAssertion> matchingDatasetAssertion,
            Optional<IdentityProviderAssertion> authnStatementAssertion,
            URI destination) {

        super(id, issueInstant, inResponseTo, issuer, destination);

        this.matchingDatasetAssertion = matchingDatasetAssertion;
        this.authnStatementAssertion = authnStatementAssertion;
        this.status = status;
    }

    public Optional<IdentityProviderAssertion> getMatchingDatasetAssertion() {
        return matchingDatasetAssertion;
    }

    public Optional<IdentityProviderAssertion> getAuthnStatementAssertion() {
        return authnStatementAssertion;
    }

    public static OutboundResponseFromIdp createSuccessResponseFromIdp(
            String responseId,
            String inResponseTo,
            String issuerId,
            IdentityProviderAssertion matchingDatasetAssertion,
            IdentityProviderAssertion authnStatementAssertion,
            URI destination) {

        return new OutboundResponseFromIdp(
                responseId,
                inResponseTo,
                issuerId,
                Instant.now(),
                IdpIdaStatus.success(),
                Optional.ofNullable(matchingDatasetAssertion),
                Optional.ofNullable(authnStatementAssertion),
                destination);
    }

    public static OutboundResponseFromIdp createNoAuthnContextResponseIssuedByIdp(
            String responseId,
            String inResponseTo,
            String issuerId,
            URI destination) {
        return getOutboundResponseFromIdpWithNoAssertions(responseId, inResponseTo, issuerId, destination, IdpIdaStatus.noAuthenticationContext());
    }

    public static OutboundResponseFromIdp createUpliftFailedResponseIssuedByIdp(
            String responseId,
            String inResponseTo,
            String issuerId,
            URI destination) {
        return getOutboundResponseFromIdpWithNoAssertions(responseId, inResponseTo, issuerId, destination, IdpIdaStatus.upliftFailed());
    }

    public static OutboundResponseFromIdp createAuthnFailedResponseIssuedByIdp(
            String responseId,
            String inResponseTo,
            String issuerId,
            URI destination) {
        return getOutboundResponseFromIdpWithNoAssertions(
                responseId,
                inResponseTo,
                issuerId,
                destination,
                IdpIdaStatus.authenticationFailed());
    }

    public static OutboundResponseFromIdp createRequesterErrorResponseIssuedByIdp(
            String responseId,
            String inResponseTo,
            String issuerId,
            URI destination, String requesterErrorMessage) {

        String message = (Objects.isNull(requesterErrorMessage) || requesterErrorMessage.isBlank()) ? null : requesterErrorMessage;
        return getOutboundResponseFromIdpWithNoAssertions(
                responseId,
                inResponseTo,
                issuerId,
                destination,
                IdpIdaStatus.requesterError(Optional.ofNullable(message)));
    }

    public static OutboundResponseFromIdp createAuthnCancelResponseIssuedByIdp(String responseId, String inResponseTo, String issuerId, URI destination) {
        return getOutboundResponseFromIdpWithNoAssertions(responseId, inResponseTo, issuerId, destination, IdpIdaStatus.authenticationCancelled());
    }

    public static OutboundResponseFromIdp createAuthnPendingResponseIssuedByIdp(String responseId, String inResponseTo, String issuerId, URI destination) {
        return getOutboundResponseFromIdpWithNoAssertions(responseId, inResponseTo, issuerId, destination, IdpIdaStatus.authenticationPending());
    }

    private static OutboundResponseFromIdp getOutboundResponseFromIdpWithNoAssertions(String responseId, String inResponseTo, String issuerId, URI destination, IdpIdaStatus status) {
        return new OutboundResponseFromIdp(
                responseId,
                inResponseTo,
                issuerId,
                Instant.now(),
                status,
                Optional.empty(),
                Optional.empty(),
                destination);
    }

    public IdpIdaStatus getStatus() {
        return status;
    }
}
