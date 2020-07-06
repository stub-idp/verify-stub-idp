package stubidp.stubidp.domain;

import stubidp.saml.domain.assertions.IdentityProviderAssertion;
import stubidp.saml.utils.core.domain.IdaSamlResponse;
import stubidp.utils.security.security.IdGenerator;

import java.io.Serializable;
import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class OutboundResponseFromIdp extends IdaSamlResponse implements Serializable {
    private static IdGenerator idGenerator = new IdGenerator();
    private Optional<IdentityProviderAssertion> matchingDatasetAssertion;
    private Optional<IdentityProviderAssertion> authnStatementAssertion;
    private IdpIdaStatus status;

    @SuppressWarnings("unused") // needed for JAXB
    private OutboundResponseFromIdp() {
    }

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
            String inResponseTo,
            String issuerId,
            IdentityProviderAssertion matchingDatasetAssertion,
            IdentityProviderAssertion authnStatementAssertion,
            URI destination) {

        return new OutboundResponseFromIdp(
                idGenerator.getId(),
                inResponseTo,
                issuerId,
                Instant.now(),
                IdpIdaStatus.success(),
                Optional.ofNullable(matchingDatasetAssertion),
                Optional.ofNullable(authnStatementAssertion),
                destination);
    }

    public static OutboundResponseFromIdp createNoAuthnContextResponseIssuedByIdp(
            String inResponseTo,
            String issuerId,
            URI destination) {
        return getOutboundResponseFromIdpWithNoAssertions(inResponseTo, issuerId, destination, IdpIdaStatus.noAuthenticationContext());
    }

    public static OutboundResponseFromIdp createUpliftFailedResponseIssuedByIdp(
            String inResponseTo,
            String issuerId,
            URI destination) {
        return getOutboundResponseFromIdpWithNoAssertions(inResponseTo, issuerId, destination, IdpIdaStatus.upliftFailed());
    }

    public static OutboundResponseFromIdp createAuthnFailedResponseIssuedByIdp(
            String inResponseTo,
            String issuerId,
            URI destination) {
        return getOutboundResponseFromIdpWithNoAssertions(
                inResponseTo,
                issuerId,
                destination,
                IdpIdaStatus.authenticationFailed());
    }

    public static OutboundResponseFromIdp createRequesterErrorResponseIssuedByIdp(
            String inResponseTo,
            String issuerId,
            URI destination, String requesterErrorMessage) {

        String message = (Objects.isNull(requesterErrorMessage) || requesterErrorMessage.isBlank()) ? null : requesterErrorMessage;
        return getOutboundResponseFromIdpWithNoAssertions(
                inResponseTo,
                issuerId,
                destination,
                IdpIdaStatus.requesterError(Optional.ofNullable(message)));
    }

    public static OutboundResponseFromIdp createAuthnCancelResponseIssuedByIdp(String inResponseTo, String issuerId, URI destination) {
        return getOutboundResponseFromIdpWithNoAssertions(inResponseTo, issuerId, destination, IdpIdaStatus.authenticationCancelled());
    }

    public static OutboundResponseFromIdp createAuthnPendingResponseIssuedByIdp(String inResponseTo, String issuerId, URI destination) {
        return getOutboundResponseFromIdpWithNoAssertions(inResponseTo, issuerId, destination, IdpIdaStatus.authenticationPending());
    }

    private static OutboundResponseFromIdp getOutboundResponseFromIdpWithNoAssertions(String inResponseTo, String issuerId, URI destination, IdpIdaStatus status) {
        return new OutboundResponseFromIdp(
                idGenerator.getId(),
                inResponseTo,
                issuerId,
                Instant.now(),
                status,
                Optional.<IdentityProviderAssertion>empty(),
                Optional.<IdentityProviderAssertion>empty(),
                destination);
    }

    public IdpIdaStatus getStatus() {
        return status;
    }
}
