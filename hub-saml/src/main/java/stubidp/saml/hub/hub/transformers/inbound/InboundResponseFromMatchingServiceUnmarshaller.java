package stubidp.saml.hub.hub.transformers.inbound;

import java.util.Optional;
import stubidp.saml.domain.assertions.PassthroughAssertion;
import stubidp.saml.hub.hub.domain.InboundResponseFromMatchingService;
import stubidp.saml.security.validators.ValidatedAssertions;
import stubidp.saml.security.validators.ValidatedResponse;

public class InboundResponseFromMatchingServiceUnmarshaller {
    private PassthroughAssertionUnmarshaller passthroughAssertionUnmarshaller;
    private MatchingServiceIdaStatusUnmarshaller statusUnmarshaller;

    public InboundResponseFromMatchingServiceUnmarshaller(
            PassthroughAssertionUnmarshaller passthroughAssertionUnmarshaller,
            MatchingServiceIdaStatusUnmarshaller statusUnmarshaller) {
        this.passthroughAssertionUnmarshaller = passthroughAssertionUnmarshaller;
        this.statusUnmarshaller = statusUnmarshaller;
    }

    public InboundResponseFromMatchingService fromSaml(ValidatedResponse validatedResponse, ValidatedAssertions validatedAssertions) {
        Optional<PassthroughAssertion> idaAssertion = null;
        if (!validatedAssertions.getAssertions().isEmpty()){
            idaAssertion = Optional.ofNullable(passthroughAssertionUnmarshaller.fromAssertion(validatedAssertions.getAssertions().get(0)));
        }

        MatchingServiceIdaStatus transformedStatus = statusUnmarshaller.fromSaml(validatedResponse.getStatus());

        return new InboundResponseFromMatchingService(
                validatedResponse.getID(),
                validatedResponse.getInResponseTo(),
                validatedResponse.getIssuer().getValue(),
                validatedResponse.getIssueInstant(),
                transformedStatus,
                idaAssertion);
    }
}
