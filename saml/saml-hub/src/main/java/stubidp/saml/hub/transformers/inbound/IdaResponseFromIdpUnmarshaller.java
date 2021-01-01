package stubidp.saml.hub.transformers.inbound;

import stubidp.saml.domain.assertions.IdpIdaStatus;
import stubidp.saml.domain.response.InboundResponseFromIdp;
import stubidp.saml.security.validators.ValidatedAssertions;
import stubidp.saml.security.validators.ValidatedResponse;
import stubidp.saml.utils.core.transformers.IdpAssertionUnmarshaller;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

public class IdaResponseFromIdpUnmarshaller<T extends IdpAssertionUnmarshaller<O>, O> {
    private final IdpIdaStatusUnmarshaller statusUnmarshaller;
    private final T passthroughAssertionUnmarshaller;

    public IdaResponseFromIdpUnmarshaller(
            IdpIdaStatusUnmarshaller statusUnmarshaller,
            T passthroughAssertionUnmarshaller) {
        this.statusUnmarshaller = statusUnmarshaller;
        this.passthroughAssertionUnmarshaller = passthroughAssertionUnmarshaller;
    }

    public InboundResponseFromIdp<O> fromSaml(ValidatedResponse validatedResponse, ValidatedAssertions validatedAssertions) {
        Optional<O> matchingDatasetAssertion = validatedAssertions.getMatchingDatasetAssertion()
                .map(passthroughAssertionUnmarshaller::fromAssertion);

        Optional<O> authnStatementAssertion = validatedAssertions.getAuthnStatementAssertion()
                .map(passthroughAssertionUnmarshaller::fromAssertion);

        IdpIdaStatus transformedStatus = statusUnmarshaller.fromSaml(validatedResponse.getStatus());
        URI destination = URI.create(validatedResponse.getDestination());
        Optional<Instant> notOnOrAfter = validatedAssertions.getMatchingDatasetAssertion()
                .flatMap(a -> Optional.ofNullable(a.getSubject()))
                .flatMap(s -> Optional.ofNullable(s.getSubjectConfirmations().get(0).getSubjectConfirmationData().getNotOnOrAfter()));

        return new InboundResponseFromIdp<>(
                validatedResponse.getID(),
                validatedResponse.getInResponseTo(),
                validatedResponse.getIssuer().getValue(),
                validatedResponse.getIssueInstant(),
                notOnOrAfter,
                transformedStatus,
                Optional.ofNullable(validatedResponse.getSignature()),
                matchingDatasetAssertion,
                destination,
                authnStatementAssertion);
    }

}
